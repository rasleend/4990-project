package com.google.ar.core.examples.java.ml.classification;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;

import com.example.android.camera.utils.YuvToRgbConverter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class ObjectDetector {

  private final Context context;
  private final YuvToRgbConverter yuvConverter;

  public ObjectDetector(Context context) {
    this.context = context;
    this.yuvConverter = new YuvToRgbConverter(context);
  }

  public abstract CompletableFuture<List<DetectedObjectResult>> analyze(Image image, Integer imageRotation);

  public Bitmap convertYuv(Image image) {
    Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    yuvConverter.yuvToRgb(image, bitmap);

    return bitmap;
  }

  public Context getContext() {
    return context;
  }

  public YuvToRgbConverter getYuvConverter() {
    return yuvConverter;
  }
}
