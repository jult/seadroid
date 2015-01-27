/*
 * App passcode library for Android, master branch
 * Dual licensed under MIT, and GPL.
 * See https://github.com/wordpress-mobile/Android-PasscodeLock
 */
package com.seafile.seadroid2.gesturelock;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.ui.activity.UnlockGesturePasswordActivity;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of AppLock
 */
public class DefaultAppLock extends AbstractAppLock {
    public static final String DEBUG_TAG = "DefaultAppLock";

    private Application currentApp; //Keep a reference to the app that invoked the locker
    private SettingsManager settingsMgr;
    private static ConcurrentMap<Object, Long> mCheckedActivities = new MapMaker()
            .weakKeys()
            .makeMap();

    public DefaultAppLock(Application currentApp) {
        super();
        this.currentApp = currentApp;
        this.settingsMgr = SettingsManager.instance();
    }

    public void enable() {
        if (android.os.Build.VERSION.SDK_INT < 14)
            return;

        if (isPasswordLocked()) {
            currentApp.unregisterActivityLifecycleCallbacks(this);
            currentApp.registerActivityLifecycleCallbacks(this);
        }
    }

    @Override
    public void disable() {
        if (android.os.Build.VERSION.SDK_INT < 14)
            return;

        currentApp.unregisterActivityLifecycleCallbacks(this);
    }

    //Check if we need to show the lock screen at startup
    public boolean isPasswordLocked() {
        return SettingsManager.instance().isGestureLockEnabled();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(DEBUG_TAG, "onActivityPaused");

        if (activity.getClass() == UnlockGesturePasswordActivity.class)
            return;

        if (!isActiviyBeingChecked(activity)) {
            settingsMgr.saveGestureLockTimeStamp();
        }
    }

    private boolean isActiviyBeingChecked(Activity activity) {
        if (!mCheckedActivities.containsKey(activity)) {
            return false;
        }
        long ts = mCheckedActivities.get(activity);
        return ts + 2000 > System.currentTimeMillis();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(DEBUG_TAG, "onActivityResumed");

        if (activity.getClass() == UnlockGesturePasswordActivity.class)
            return;

        if (mustShowUnlockSceen()) {
            mCheckedActivities.put(activity, System.currentTimeMillis());
            Intent i = new Intent(activity, UnlockGesturePasswordActivity.class);
            activity.startActivity(i);
        }

    }

    private boolean mustShowUnlockSceen() {

        return settingsMgr.isGestureLockRequired();
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}