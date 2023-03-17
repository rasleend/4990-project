package com.google.ar.core.examples.java.ml.classification;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import androidx.core.util.Pair;

import com.google.ar.core.examples.java.ml.classification.utils.ImageUtils;
import com.google.ar.core.examples.java.ml.classification.utils.VertexUtils;
import com.google.common.collect.Lists;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MLKitObjectDetector extends com.google.ar.core.examples.java.ml.classification.ObjectDetector {

    private final LocalModel model = new LocalModel.Builder().setAssetFilePath("model_v3.tflite").build();
    private final CustomObjectDetectorOptions.Builder builder = new CustomObjectDetectorOptions.Builder(model);

    private CustomObjectDetectorOptions options = builder
            .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
            .enableMultipleObjects()
            .build();
    private final ObjectDetector detector = ObjectDetection.getClient(options);

    public MLKitObjectDetector(Context context) {
        super(context);
    }

    @Override
    public CompletableFuture<List<DetectedObjectResult>> analyze(Image image, Integer imageRotation) {
        Log.i("TAG", "Hello 5");

        CompletableFuture<List<DetectedObjectResult>> completableFuture = new CompletableFuture<>();

        Bitmap convertYuv = convertYuv(image);
        Bitmap rotatedImage = ImageUtils.INSTANCE.rotateBitmap(convertYuv, imageRotation);
        InputImage inputImage = InputImage.fromBitmap(rotatedImage, 0);

        detector.process(inputImage)
                .addOnSuccessListener(mlKitDetectedObjects -> {
                    if (mlKitDetectedObjects != null) {
                        Log.i("TAG", "Hello 1");
                        List<DetectedObjectResult> results = Lists.transform(mlKitDetectedObjects, obj -> {
                            DetectedObject.Label bestLabel = obj.getLabels().stream().max((l1, l2) -> Math.round(l1.getConfidence() - l2.getConfidence())).orElse(null);

                            if (bestLabel == null) {
                                bestLabel = obj.getLabels().get(0);
                            }


                            Pair<Integer, Integer> coords = new Pair<>(
                                    Math.round(obj.getBoundingBox().exactCenterX()),
                                    Math.round(obj.getBoundingBox().exactCenterY())
                            );

                            Pair<Integer, Integer> rotatedCoordinates = VertexUtils.INSTANCE.rotateCoordinates(coords, rotatedImage.getWidth(), rotatedImage.getHeight(), imageRotation);

                            return new DetectedObjectResult(bestLabel.getConfidence(), bestLabel.getText(), rotatedCoordinates);
                        });

                        completableFuture.complete(results);

                    } else {
                        Log.i("TAG", "Hello 2");
                        completableFuture.complete(new ArrayList<>());
                    }
                });

        Log.i("TAG", "Hello 6");

        return completableFuture;
    }

    public boolean hasCustomModel() {
        return builder instanceof CustomObjectDetectorOptions.Builder;
    }
}
