package com.google.ar.core.examples.java.ml;

import android.content.Context;
import android.media.Image;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.samplerender.SampleRender;
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer;
import com.google.ar.core.examples.java.ml.classification.DetectedObjectResult;
import com.google.ar.core.examples.java.ml.classification.MLKitObjectDetector;
import com.google.ar.core.examples.java.ml.classification.ObjectDetector;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AppRenderer implements DefaultLifecycleObserver, SampleRender.Renderer {

    private static final String TAG = "HelloArRenderer";

    // List of possible detected shapes
    private static final List<Map<String, String>> SHAPES = Arrays.asList(
            new HashMap<String, String>() {{
                put("id", "circle");
                put("name", "Circle");
                put("description", "Area- pi x radius x radius\n" +
                        "Perimeter- Circumference- 2 x pi x radius\n" +
                        "Fun Fact- A circle is the only one sided shape containing an area!");
            }},
            new HashMap<String, String>() {{
                put("id", "square");
                put("name", "Square");
                put("description", "All sides are equal.\n" +
                        "Area - side x side\n" +
                        "Perimeter - 4 x side\n" +
                        "Fun Fact - A square is also a rectangle with equal sides and a rhombus with right angles!");
            }},
            new HashMap<String, String>() {{
                put("id", "triangle");
                put("name", "Triangle");
                put("description", "Area- 1/2 x base x height\n" +
                        "Perimeter- side1+ side2 + side3\n" +
                        "Fun fact- Triangles have least number of sides among all polygons!");
            }}
    );

    private final MainActivity activity;
    private MainActivityView view = null;
    DisplayRotationHelper displayRotationHelper;
    BackgroundRenderer backgroundRenderer;

    float[] viewMatrix = new float[16];
    float[] projectionMatrix = new float[16];
    float[] viewProjectionMatrix = new float[16];

    boolean scanButtonWasPressed = false;

    // Analyzer by Google ML Kit which analyze the shape in the frame that captured by camera
    MLKitObjectDetector mlKitAnalyzer = null;
    ObjectDetector currentAnalyzer = null;

    List<DetectedObjectResult> objectResults = null;

    public AppRenderer(MainActivity activity) {
        this.activity = activity;

        this.displayRotationHelper = new DisplayRotationHelper(activity);

        for (int i = 0; i < 16; i++) {
            viewMatrix[i] = 0f;
            projectionMatrix[i] = 0f;
            viewProjectionMatrix[i] = 0f;
        }

        mlKitAnalyzer = new MLKitObjectDetector(activity);
        currentAnalyzer = mlKitAnalyzer;
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        displayRotationHelper.onPause();
    }

    void bindView(MainActivityView view) {
        this.view = view;

        view.getScanButton().setOnClickListener(scanBtn -> {
            // frame.acquireCameraImage is dependent on an ARCore Frame, which is only available in onDrawFrame.
            // Use a boolean and check its state in onDrawFrame to interact with the camera image.
            scanButtonWasPressed = true;
            view.setScanningActive(true);
            hideSnackbar();
        });

        view.getResetButton().setOnClickListener(resetBtn -> {
            view.getResetButton().setEnabled(false);
            hideSnackbar();
        });
    }

    @Override
    public void onSurfaceCreated(SampleRender render) {
        backgroundRenderer = new BackgroundRenderer(render);
        try {
            backgroundRenderer.setUseDepthVisualization(render, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        Session session = activity.getArCoreSessionHelper().getSessionCache();
        if (session == null) return;

        session.setCameraTextureNames(new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});

        displayRotationHelper.updateSessionIfNeeded(session);

        Frame frame = null;
        try {
            frame = session.update();

        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            showSnackbar("Camera not available. Try restarting the app.");
            return;
        }

        backgroundRenderer.updateDisplayGeometry(frame);
        backgroundRenderer.drawBackground(render);

        // Get camera and projection matrices.
        Camera camera = frame.getCamera();
        camera.getViewMatrix(viewMatrix, 0);
        camera.getProjectionMatrix(projectionMatrix, 0, 0.01f, 100.0f);
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Handle tracking failures.
        if (camera.getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        // Frame.acquireCameraImage must be used on the GL thread.
        // Check if the button was pressed last frame to start processing the camera image.
        if (scanButtonWasPressed) {
            scanButtonWasPressed = false;

            // When the scan button is clicked, the frame is captured/acquired to be analyzed
            Image cameraImage = tryAcquireCameraImage(frame);
            if (cameraImage != null) {
                // Call our ML model on an IO thread.
                String cameraId = session.getCameraConfig().getCameraId();
                int imageRotation = displayRotationHelper.getCameraSensorToDisplayRotation(cameraId);

                // Analyze asynchronously what shape is in the frame
                currentAnalyzer.analyze(cameraImage, imageRotation).thenApply((Function<List<DetectedObjectResult>, Object>) r -> {
                    Log.i("TAG", "Hello 3");
                    objectResults = r;
                    cameraImage.close();
                    return null;
                });
            }
        }

        List<DetectedObjectResult> objects = objectResults;
        if (objects != null) {
            objectResults = null;
            Log.i(TAG, "$currentAnalyzer got objects: $objects");

            // Once the shape is detected, show the info in a dialog
            objects.forEach(obj -> {
                Log.i(TAG, "Shape: " + obj.getLabel());
                showDialog(activity, obj.getLabel());
            });

            // Do after the analysis
            view.post(() -> {
                view.setScanningActive(false);

                if (objects.isEmpty() && currentAnalyzer == mlKitAnalyzer && !mlKitAnalyzer.hasCustomModel()) {
                    showSnackbar("Default ML Kit classification model returned no results. " +
                            "For better classification performance, see the README to configure a custom model.");

                } else if (objects.isEmpty()) {
                    showSnackbar("Classification model returned no results.");
                }
            });
        }
    }

    private Image tryAcquireCameraImage(Frame frame) {
        try {
            return frame.acquireCameraImage();

        } catch (NotYetAvailableException e) {
            return null;

        } catch (Throwable e) {
            throw e;
        }
    }

    private void showSnackbar(String message) {
        activity.getView().getSnackbarHelper().showError(activity, message);
    }

    private void hideSnackbar() {
        activity.getView().getSnackbarHelper().hide(activity);
    }

    /**
     * Show dialog containing information about detected shape
     */
    private void showDialog(Context context, String label) {
        Map<String, String> shape = new HashMap<>();
        for (Map<String, String> s : SHAPES) {
            if (s.get("id").equals(label)) shape = s;
        }

        Map<String, String> finalShape = shape;
        activity.runOnUiThread(() -> new MaterialAlertDialogBuilder(context)
                .setTitle(finalShape.get("name"))
                .setMessage(finalShape.get("description"))
                .setPositiveButton("Close", (dialogInterface, i) -> dialogInterface.cancel())
                .show()
        );
    }
}
