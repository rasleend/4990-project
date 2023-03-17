package com.google.ar.core.examples.java.ml;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper;

import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final String TAG = "MainActivity";

    // Helper to manage lifecycle of the session
    private ARCoreSessionLifecycleHelper arCoreSessionHelper;

    // Renderer to help rendering during detection
    private AppRenderer renderer;

    // Responsible class related to view/layout
    private MainActivityView view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arCoreSessionHelper = new ARCoreSessionLifecycleHelper(this);

        // Callback that will be called when an exception is thrown
        arCoreSessionHelper.setExceptionCallback(e -> {
            String message = "Something went wrong";
            Log.e(TAG, message, e);
            Toast.makeText(arCoreSessionHelper.getActivity(), message, Toast.LENGTH_LONG).show();
        });

        // Callback that will be called before a session is resumed
        arCoreSessionHelper.setBeforeSessionResume(session -> {

            // Set up camera config
            Config config = session.getConfig();
            config.setFocusMode(Config.FocusMode.AUTO);
            if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                config.setDepthMode(Config.DepthMode.AUTOMATIC);
            }

            session.configure(config);

            CameraConfigFilter filter = new CameraConfigFilter(session);
            filter.setFacingDirection(CameraConfig.FacingDirection.BACK);
            List<CameraConfig> configs = session.getSupportedCameraConfigs(filter);

            CameraConfig bestConfig = null;
            for (CameraConfig theConfig : configs) {
                if (bestConfig == null) {
                    bestConfig = theConfig;

                } else {
                    if (theConfig.getImageSize().getHeight() > bestConfig.getImageSize().getHeight()) {
                        bestConfig = theConfig;
                    }
                }
            }
            session.setCameraConfig(bestConfig);
        });

        // Enable session helper to observe the activity's lifecycle
        getLifecycle().addObserver(arCoreSessionHelper);

        renderer = new AppRenderer(this);

        // Enable renderer to observe the activity's lifecycle
        getLifecycle().addObserver(renderer);
        view = new MainActivityView(this, renderer);
        setContentView(view.getRoot());
        renderer.bindView(view);

        // Enable view to observe the activity's lifecycle
        getLifecycle().addObserver(view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        arCoreSessionHelper.onRequestPermissionsResult();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    public ARCoreSessionLifecycleHelper getArCoreSessionHelper() {
        return arCoreSessionHelper;
    }

    public AppRenderer getRenderer() {
        return renderer;
    }

    public MainActivityView getView() {
        return view;
    }
}
