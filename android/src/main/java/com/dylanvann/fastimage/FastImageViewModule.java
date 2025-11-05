package com.dylanvann.fastimage;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

/**
 * React Native module for FastImage.
 * Handles preloading and cache management (memory + disk).
 */
class FastImageViewModule extends ReactContextBaseJavaModule {

    private static final String REACT_CLASS = "FastImageView";

    FastImageViewModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    /**
     * Preload multiple image sources into Glide's memory/disk cache.
     */
    @ReactMethod
    public void preload(final ReadableArray sources) {
        final Activity activity = getCurrentActivity();
        if (activity == null) return;

        activity.runOnUiThread(() -> {
            for (int i = 0; i < sources.size(); i++) {
                final ReadableMap source = sources.getMap(i);
                if (source == null) continue;

                final FastImageSource imageSource =
                        FastImageViewConverter.getImageSource(activity, source);

                if (imageSource == null) continue;

                Object loadSource;

                // Determine source type for Glide
                if (imageSource.isBase64Resource()) {
                    loadSource = imageSource.getSource();
                } else if (imageSource.isResource()) {
                    loadSource = imageSource.getUri();
                } else {
                    loadSource = imageSource.getGlideUrl();
                }

                Glide.with(activity.getApplicationContext())
                        .load(loadSource)
                        .apply(FastImageViewConverter.getOptions(activity, imageSource, source))
                        .preload();
            }
        });
    }

    /**
     * Clears Glide's memory cache (must be on UI thread).
     */
    @ReactMethod
    public void clearMemoryCache(final Promise promise) {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.resolve(null);
            return;
        }

        activity.runOnUiThread(() -> {
            Glide.get(activity.getApplicationContext()).clearMemory();
            promise.resolve(null);
        });
    }

    /**
     * Clears Glide's disk cache (must be off the UI thread).
     */
    @ReactMethod
    public void clearDiskCache(final Promise promise) {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.resolve(null);
            return;
        }

        new Thread(() -> {
            Glide.get(activity.getApplicationContext()).clearDiskCache();
            promise.resolve(null);
        }).start();
    }
}
