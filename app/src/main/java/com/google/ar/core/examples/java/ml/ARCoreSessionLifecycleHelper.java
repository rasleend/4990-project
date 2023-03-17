package com.google.ar.core.examples.java.ml;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ARCoreSessionLifecycleHelper implements DefaultLifecycleObserver {

    private final Activity activity;
    private final Set<Session.Feature> features = new HashSet<>();
    boolean installRequested = false;
    Session sessionCache = null;

    // Creating a Session may fail. In this case, sessionCache will remain null, and this function will be called with an exception.
    // See https://developers.google.com/ar/reference/java/com/google/ar/core/Session#Session(android.content.Context)
    // for more information.
    Consumer<Exception> exceptionCallback = null;

    // After creating a session, but before Session.resume is called is the perfect time to setup a session.
    // Generally, you would use Session.configure or setCameraConfig here.
    // https://developers.google.com/ar/reference/java/com/google/ar/core/Session#public-void-configure-config-config
    // https://developers.google.com/ar/reference/java/com/google/ar/core/Session#setCameraConfig(com.google.ar.core.CameraConfig)
    Consumer<Session> beforeSessionResume = null;

    public ARCoreSessionLifecycleHelper(Activity activity) {
        this.activity = activity;
    }

    // Creates a session. If ARCore is not installed, an installation will be requested.
    public Session tryCreateSession() {
        try {
            switch (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    return null;

                case INSTALLED:
                    break;
            }

            return new Session(activity, features);

        } catch (Exception e) {
            if (exceptionCallback != null) {
                exceptionCallback.accept(e);
            }

            return null;
        }
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        // Ensure camera access is permitted
        if (!CameraPermissionHelper.hasCameraPermission(activity)) {
            CameraPermissionHelper.requestCameraPermission(activity);
            return;
        }

        Session session = tryCreateSession();
        if (session == null) return;

        try {
            if (beforeSessionResume != null) {
                beforeSessionResume.accept(session);
            }
            session.resume();
            sessionCache = session;

        } catch (CameraNotAvailableException e) {
            if (exceptionCallback != null) {
                exceptionCallback.accept(e);
            }
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (sessionCache != null) {
            sessionCache.pause();
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if (sessionCache != null) {
            sessionCache.close();
            sessionCache = null;
        }
    }

    void onRequestPermissionsResult() {
        // Info to user if the permission is still not given
        if (!CameraPermissionHelper.hasCameraPermission(activity)) {
            Toast.makeText(activity, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(activity)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(activity);
            }
            activity.finish();
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public Set<Session.Feature> getFeatures() {
        return features;
    }

    public boolean isInstallRequested() {
        return installRequested;
    }

    public void setInstallRequested(boolean installRequested) {
        this.installRequested = installRequested;
    }

    public Session getSessionCache() {
        return sessionCache;
    }

    public void setSessionCache(Session sessionCache) {
        this.sessionCache = sessionCache;
    }

    public Consumer<Exception> getExceptionCallback() {
        return exceptionCallback;
    }

    public void setExceptionCallback(Consumer<Exception> exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
    }

    public Consumer<Session> getBeforeSessionResume() {
        return beforeSessionResume;
    }

    public void setBeforeSessionResume(Consumer<Session> beforeSessionResume) {
        this.beforeSessionResume = beforeSessionResume;
    }
}
