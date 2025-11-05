package com.dylanvann.fastimage;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.fresco.ImageSource;

public class FastImageSource {

    private final ImageSource imageSource;

    public FastImageSource(ReactContext reactContext, String source, @Nullable double width, @Nullable double height) {
        this.imageSource = new ImageSource(reactContext, source, width, height);
    }

    public FastImageSource(ReactContext reactContext, String source) {
        this.imageSource = new ImageSource(reactContext, source);
    }

    public Uri getUri() {
        return imageSource.getUri();
    }

    public boolean isResource() {
        return imageSource.isResource();
    }

    public String getSource() {
        return imageSource.getSource();
    }

    public double getSize() {
        return imageSource.getSize();
    }

    public ImageSource getImageSource() {
        return imageSource;
    }
}
