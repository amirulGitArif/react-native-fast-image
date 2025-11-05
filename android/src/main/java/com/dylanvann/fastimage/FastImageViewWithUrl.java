package com.dylanvann.fastimage;

import static com.dylanvann.fastimage.FastImageRequestListener.REACT_ON_ERROR_EVENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

class FastImageViewWithUrl extends AppCompatImageView {
    private boolean mNeedsReload = false;
    private ReadableMap mSource = null;
    private Drawable mDefaultSource = null;

    public GlideUrl glideUrl;

    public FastImageViewWithUrl(Context context) {
        super(context);
    }

    public void setSource(@Nullable ReadableMap source) {
        mNeedsReload = true;
        mSource = source;
    }

    public void setDefaultSource(@Nullable Drawable source) {
        mNeedsReload = true;
        mDefaultSource = source;
    }

    private boolean isNullOrEmpty(final String url) {
        return url == null || url.trim().isEmpty();
    }

    @SuppressLint("CheckResult")
    public void onAfterUpdate(
            @Nonnull FastImageViewManager manager,
            @Nullable RequestManager requestManager,
            @Nonnull Map<String, List<FastImageViewWithUrl>> viewsForUrlsMap) {
        if (!mNeedsReload)
            return;

        if ((mSource == null ||
                !mSource.hasKey("uri") ||
                isNullOrEmpty(mSource.getString("uri"))) &&
                mDefaultSource == null) {

            clearView(requestManager);

            if (glideUrl != null) {
                FastImageOkHttpProgressGlideModule.forget(glideUrl.toStringUrl());
            }

            setImageDrawable(null);
            return;
        }

        // Create the new FastImageSource
        final FastImageSource imageSource = FastImageViewConverter.getImageSource(getContext(), mSource);

        if (imageSource != null && imageSource.getUri().toString().length() == 0) {
            ThemedReactContext context = (ThemedReactContext) getContext();
            RCTEventEmitter eventEmitter = context.getJSModule(RCTEventEmitter.class);
            int viewId = getId();
            WritableMap event = new WritableNativeMap();
            event.putString("message", "Invalid source prop:" + mSource);
            eventEmitter.receiveEvent(viewId, REACT_ON_ERROR_EVENT, event);

            clearView(requestManager);
            if (glideUrl != null) {
                FastImageOkHttpProgressGlideModule.forget(glideUrl.toStringUrl());
            }
            setImageDrawable(null);
            return;
        }

        // Get glideUrl directly from FastImageSource
        final GlideUrl glideUrl = imageSource == null ? null : imageSource.getGlideUrl();
        this.glideUrl = glideUrl;
        clearView(requestManager);

        String key = glideUrl == null ? null : glideUrl.toStringUrl();

        if (glideUrl != null) {
            FastImageOkHttpProgressGlideModule.expect(key, manager);
            List<FastImageViewWithUrl> viewsForKey = viewsForUrlsMap.get(key);
            if (viewsForKey != null && !viewsForKey.contains(this)) {
                viewsForKey.add(this);
            } else if (viewsForKey == null) {
                List<FastImageViewWithUrl> newViewsForKeys = new ArrayList<>(Collections.singletonList(this));
                viewsForUrlsMap.put(key, newViewsForKeys);
            }
        }

        ThemedReactContext context = (ThemedReactContext) getContext();
        if (imageSource != null) {
            RCTEventEmitter eventEmitter = context.getJSModule(RCTEventEmitter.class);
            int viewId = this.getId();
            eventEmitter.receiveEvent(viewId,
                    FastImageViewManager.REACT_ON_LOAD_START_EVENT,
                    new WritableNativeMap());
        }

        if (requestManager != null) {
            Object sourceForLoad = imageSource != null
                    ? (imageSource.getGlideUrl() != null
                        ? imageSource.getGlideUrl()
                        : imageSource.getUri())
                    : null;

            RequestBuilder<Drawable> builder =
                    requestManager
                            .load(sourceForLoad)
                            .apply(FastImageViewConverter
                                    .getOptions(context, imageSource, mSource)
                                    .placeholder(mDefaultSource)
                                    .fallback(mDefaultSource));

            if (key != null)
                builder.listener(new FastImageRequestListener(key));

            builder.into(this);
        }
    }

    public void clearView(@Nullable RequestManager requestManager) {
        if (requestManager != null && getTag() != null && getTag() instanceof com.bumptech.glide.request.Request) {
            requestManager.clear(this);
        }
    }
}
