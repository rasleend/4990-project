package com.google.ar.core.examples.java.ml.classification;

import androidx.core.util.Pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DetectedObjectResult {
  private final float confidence;
  @NotNull
  private final String label;
  @NotNull
  private final Pair<Integer, Integer> centerCoordinate;

  public final float getConfidence() {
    return this.confidence;
  }

  @NotNull
  public final String getLabel() {
    return this.label;
  }

  @NotNull
  public final Pair<Integer, Integer> getCenterCoordinate() {
    return this.centerCoordinate;
  }

  public DetectedObjectResult(float confidence, @NotNull String label, @NotNull Pair<Integer, Integer> centerCoordinate) {
    super();
    this.confidence = confidence;
    this.label = label;
    this.centerCoordinate = centerCoordinate;
  }

  @NotNull
  public final DetectedObjectResult copy(float confidence, @NotNull String label, @NotNull Pair<Integer, Integer> centerCoordinate) {
    return new DetectedObjectResult(confidence, label, centerCoordinate);
  }

  // $FF: synthetic method
  public static DetectedObjectResult copy$default(DetectedObjectResult var0, float var1, String var2, Pair<Integer, Integer> var3, int var4, Object var5) {
    if ((var4 & 1) != 0) {
      var1 = var0.confidence;
    }

    if ((var4 & 2) != 0) {
      var2 = var0.label;
    }

    if ((var4 & 4) != 0) {
      var3 = var0.centerCoordinate;
    }

    return var0.copy(var1, var2, var3);
  }

  @NotNull
  public String toString() {
    return "DetectedObjectResult(confidence=" + this.confidence + ", label=" + this.label + ", centerCoordinate=" + this.centerCoordinate + ")";
  }

  public int hashCode() {
    int var10000 = Float.hashCode(this.confidence) * 31;
    String var10001 = this.label;
    var10000 = (var10000 + (var10001 != null ? var10001.hashCode() : 0)) * 31;
    Pair<Integer, Integer> var1 = this.centerCoordinate;
    return var10000 + (var1 != null ? var1.hashCode() : 0);
  }

  public boolean equals(@Nullable Object var1) {
    if (this != var1) {
      if (var1 instanceof DetectedObjectResult) {
        DetectedObjectResult var2 = (DetectedObjectResult)var1;
        return Float.compare(this.confidence, var2.confidence) == 0 && this.label.equals(var2.label) && this.centerCoordinate == var2.centerCoordinate;
      }

      return false;
    } else {
      return true;
    }
  }
}
