package com.dylanvann.fastimage;

import static com.bumptech.glide.request.RequestOptions.signatureOf;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ApplicationVersionSignature;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.facebook.react.bridge.ReadableMapKeySetIterator;

import com.bumptech.glide.load.model.Headers;



class FastImageViewConverter {
    private static final Drawable TRANSPARENT_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);

    private static final Map<String, FastImageCacheControl> FAST_IMAGE_CACHE_CONTROL_MAP =
            new HashMap<String, FastImageCacheControl>() {{
                put("immutable", FastImageCacheControl.IMMUTABLE);
                put("web", FastImageCacheControl.WEB);
                put("cacheOnly", FastImageCacheControl.CACHE_ONLY);
            }};

    private static final Map<String, Priority> FAST_IMAGE_PRIORITY_MAP =
            new HashMap<String, Priority>() {{
                put("low", Priority.LOW);
                put("normal", Priority.NORMAL);
                put("high", Priority.HIGH);
            }};

    private static final Map<String, ImageView.ScaleType> FAST_IMAGE_RESIZE_MODE_MAP =
            new HashMap<String, ImageView.ScaleType>() {{
                put("contain", ScaleType.FIT_CENTER);
                put("cover", ScaleType.CENTER_CROP);
                put("stretch", ScaleType.FIT_XY);
                put("center", ScaleType.CENTER_INSIDE);
            }};

static FastImageSource getImageSource(Context context, ReadableMap source) {
    if (source == null) return null;

    String uri = source.hasKey("uri") ? source.getString("uri") : null;

    // Extract headers from JS
    ReadableMap headersMap = source.hasKey("headers") ? source.getMap("headers") : null;
    Map<String, String> headerMap = new HashMap<>();
    if (headersMap != null) {
        ReadableMapKeySetIterator iterator = headersMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            headerMap.put(key, headersMap.getString(key));
        }
    }

    // ✅ Use Glide’s Headers interface properly (anonymous implementation)
    Headers glideHeaders = new Headers() {
        @Override
        public Map<String, String> getHeaders() {
            return headerMap;
        }
    };

    // ✅ Pass Glide Headers, not okhttp3.Headers
    return new FastImageSource(context, uri, glideHeaders);
}

    static RequestOptions getOptions(Context context, @Nullable FastImageSource imageSource, ReadableMap source) {
        final Priority priority = FastImageViewConverter.getPriority(source);
        final FastImageCacheControl cacheControl = FastImageViewConverter.getCacheControl(source);

        DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.AUTOMATIC;
        boolean onlyFromCache = false;
        boolean skipMemoryCache = false;

        switch (cacheControl) {
            case WEB:
                diskCacheStrategy = DiskCacheStrategy.NONE;
                skipMemoryCache = true;
                break;
            case CACHE_ONLY:
                onlyFromCache = true;
                break;
            case IMMUTABLE:
                break;
        }

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(diskCacheStrategy)
                .onlyRetrieveFromCache(onlyFromCache)
                .skipMemoryCache(skipMemoryCache)
                .priority(priority)
                .placeholder(TRANSPARENT_DRAWABLE);

        //  Handle Android resource URIs properly
        if (imageSource != null && imageSource.getUri() != null &&
                "android.resource".equals(imageSource.getUri().getScheme())) {
            options = options.apply(signatureOf(ApplicationVersionSignature.obtain(context)));
        }

        return options;
    }

    private static FastImageCacheControl getCacheControl(ReadableMap source) {
        return getValueFromSource("cache", "immutable", FAST_IMAGE_CACHE_CONTROL_MAP, source);
    }

    private static Priority getPriority(ReadableMap source) {
        return getValueFromSource("priority", "normal", FAST_IMAGE_PRIORITY_MAP, source);
    }

    static ScaleType getScaleType(String propValue) {
        return getValue("resizeMode", "cover", FAST_IMAGE_RESIZE_MODE_MAP, propValue);
    }

    private static <T> T getValue(String propName, String defaultPropValue, Map<String, T> map, String propValue) {
        if (propValue == null) propValue = defaultPropValue;
        T value = map.get(propValue);
        if (value == null)
            throw new JSApplicationIllegalArgumentException("FastImage, invalid " + propName + " : " + propValue);
        return value;
    }

    private static <T> T getValueFromSource(String propName, String defaultProp, Map<String, T> map, ReadableMap source) {
        String propValue;
        try {
            propValue = source != null ? source.getString(propName) : null;
        } catch (NoSuchKeyException e) {
            propValue = null;
        }
        return getValue(propName, defaultProp, map, propValue);
    }
}
