package com.dylanvann.fastimage;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.facebook.react.bridge.ReadableMap;

class FastImageSource {
    private final Uri uri;
    private final GlideUrl glideUrl;

    FastImageSource(@Nullable ReadableMap source) {
        if (source != null && source.hasKey("uri")) {
            String uriStr = source.getString("uri");
            if (uriStr != null && !uriStr.isEmpty()) {
                uri = Uri.parse(uriStr);
            } else {
                uri = Uri.EMPTY;
            }

            if (source.hasKey("headers")) {
                ReadableMap headers = source.getMap("headers");
                LazyHeaders.Builder builder = new LazyHeaders.Builder();
                if (headers != null) {
                    for (String key : headers.toHashMap().keySet()) {
                        String value = headers.getString(key);
                        if (!TextUtils.isEmpty(key) && value != null) {
                            builder.addHeader(key, value);
                        }
                    }
                }
                glideUrl = new GlideUrl(uri.toString(), builder.build());
            } else {
                glideUrl = new GlideUrl(uri.toString());
            }
        } else {
            uri = Uri.EMPTY;
            glideUrl = null;
        }
    }

    public Uri getUri() {
        return uri;
    }

    public GlideUrl getGlideUrl() {
        return glideUrl;
    }

    public Object getSourceForLoad() {
        return glideUrl != null ? glideUrl : uri;
    }
}
