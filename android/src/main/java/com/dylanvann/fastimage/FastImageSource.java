package com.dylanvann.fastimage;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.facebook.react.bridge.ReadableMap;

class FastImageSource {
    private final Uri uri;
    private final GlideUrl glideUrl;
    private final Context context;

    FastImageSource(Context context, @Nullable String uriString, @Nullable com.bumptech.glide.load.model.Headers headers) {
        this.context = context;

        if (uriString != null && !uriString.isEmpty()) {
            uri = Uri.parse(uriString);
        } else {
            uri = Uri.EMPTY;
        }

        if (headers != null) {
            glideUrl = new GlideUrl(uri.toString(), headers);
        } else {
            glideUrl = new GlideUrl(uri.toString());
        }
    }

    // ✅ Add these 3 methods to match FastImageViewModule expectations
    boolean isBase64Resource() {
        return uri.toString().startsWith("data:");
    }

    boolean isResource() {
        return uri.toString().startsWith("res:") || uri.toString().startsWith("android.resource://");
    }

    Object getSource() {
        return glideUrl != null ? glideUrl : uri;
    }

    public Uri getUri() {
        return uri;
    }

    public GlideUrl getGlideUrl() {
        return glideUrl;
    }
}
