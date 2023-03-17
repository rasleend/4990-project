package com.google.ar.core.examples.java.ml;

import android.opengl.GLSurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.core.examples.java.common.samplerender.SampleRender;

public class MainActivityView implements DefaultLifecycleObserver {

    private final MainActivity activity;
    private final AppRenderer renderer;

    private final View root;
    private final GLSurfaceView surfaceView;
    private final AppCompatButton scanButton;
    private final AppCompatButton resetButton;
    private final SnackbarHelper snackbarHelper = new SnackbarHelper();

    public MainActivityView(MainActivity activity, AppRenderer renderer) {
        this.activity = activity;
        this.renderer = renderer;

        root = View.inflate(activity, R.layout.activity_main, null);
        surfaceView = root.findViewById(R.id.surfaceview);
        scanButton = root.findViewById(R.id.scanButton);
        resetButton = root.findViewById(R.id.clearButton);

        new SampleRender(surfaceView, renderer, activity.getAssets());

        snackbarHelper.setParentView(root.findViewById(R.id.coordinatorLayout));
        snackbarHelper.setMaxLines(6);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        surfaceView.onResume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        surfaceView.onPause();
    }

    public void post(Runnable action) {
        root.post(action);
    }

    /**
     * When the app is scanning, disable scan button
     */
    public void setScanningActive(Boolean active) {
        if (active) {
            scanButton.setEnabled(false);
            scanButton.setText(activity.getString(R.string.scan_busy));

        } else {
            scanButton.setEnabled(true);
            scanButton.setText(activity.getString(R.string.scan_available));
        }
    }

    public MainActivity getActivity() {
        return activity;
    }

    public AppRenderer getRenderer() {
        return renderer;
    }

    public View getRoot() {
        return root;
    }

    public GLSurfaceView getSurfaceView() {
        return surfaceView;
    }

    public AppCompatButton getScanButton() {
        return scanButton;
    }

    public AppCompatButton getResetButton() {
        return resetButton;
    }

    public SnackbarHelper getSnackbarHelper() {
        return snackbarHelper;
    }
}
