package com.google.ar.core.examples.java.ml.classification.utils;

import androidx.core.util.Pair;

import com.google.cloud.vision.v1.NormalizedVertex;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;


public final class VertexUtils {
    @NotNull
    public static final VertexUtils INSTANCE;

    @NotNull
    public final Pair<Integer, Integer> toAbsoluteCoordinates(@NotNull NormalizedVertex $this$toAbsoluteCoordinates, int imageWidth, int imageHeight) {
        return new Pair<>(
                (int) ($this$toAbsoluteCoordinates.getX() * (float) imageWidth),
                (int) ($this$toAbsoluteCoordinates.getY() * (float) imageHeight)
        );
    }

    @NotNull
    public final Pair<Integer, Integer> rotateCoordinates(@NotNull Pair<Integer, Integer> $this$rotateCoordinates, int imageWidth, int imageHeight, int imageRotation) {
        int x = ((Number) $this$rotateCoordinates.first).intValue();
        int y = ((Number) $this$rotateCoordinates.second).intValue();
        Pair<Integer, Integer> var10000;
        switch (imageRotation) {
            case 0:
                var10000 = new Pair<>(x, y);
                break;
            case 90:
                var10000 = new Pair<>(y, imageWidth - x);
                break;
            case 180:
                var10000 = new Pair<>(imageWidth - x, imageHeight - y);
                break;
            case 270:
                var10000 = new Pair<>(imageHeight - y, x);
                break;
            default:
                String var7 = "Invalid imageRotation " + imageRotation;
                throw new IllegalStateException(var7.toString());
        }

        return var10000;
    }

    @NotNull
    public final NormalizedVertex calculateAverage(@NotNull List<NormalizedVertex> $this$calculateAverage) {
        float averageX = 0.0F;
        float averageY = 0.0F;

        NormalizedVertex vertex;
        for (Iterator<NormalizedVertex> var5 = $this$calculateAverage.iterator(); var5.hasNext(); averageY += vertex.getY() / (float) $this$calculateAverage.size()) {
            vertex = (NormalizedVertex) var5.next();
            averageX += vertex.getX() / (float) $this$calculateAverage.size();
        }

        return NormalizedVertex.newBuilder().setX(averageX).setY(averageY).build();
    }

    private VertexUtils() {
    }

    static {
        INSTANCE = new VertexUtils();
    }
}
