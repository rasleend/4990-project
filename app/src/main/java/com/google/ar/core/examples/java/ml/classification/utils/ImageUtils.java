package com.google.ar.core.examples.java.ml.classification.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;

public final class ImageUtils {
  @NotNull
  public static final ImageUtils INSTANCE;

  @NotNull
  public final Bitmap rotateBitmap(@NotNull Bitmap bitmap, int rotation) {
    if (rotation == 0) {
      return bitmap;
    } else {
      Matrix matrix = new Matrix();
      matrix.postRotate((float)rotation);
      return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }
  }

  public final byte[] toByteArray(@NotNull Bitmap $this$toByteArray) {
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      $this$toByteArray.compress(CompressFormat.JPEG, 100, (OutputStream)stream);
      return stream.toByteArray();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private ImageUtils() {
  }

  static {
    INSTANCE = new ImageUtils();
  }
}
