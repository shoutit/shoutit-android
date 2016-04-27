package com.shoutit.app.android.utils;

import com.shoutit.app.android.BuildConfig;

public class BuildTypeUtils {

    private static final String DEBUG = "debug";
    private static final String LOCAL = "local";
    private static final String STAGING = "staging";
    private static final String RELEASE = "release";

    public static boolean isDebug() {
        return BuildConfig.BUILD_TYPE.equals(DEBUG);
    }

    public static boolean isLocal() {
        return BuildConfig.BUILD_TYPE.equals(LOCAL);
    }

    public static boolean isStaging() {
        return BuildConfig.BUILD_TYPE.equals(STAGING);
    }

    public static boolean isStagingOrDebug() {
        return BuildTypeUtils.isDebug() || BuildTypeUtils.isStaging();
    }

    public static boolean isRelease() {
        return BuildConfig.BUILD_TYPE.equals(RELEASE);
    }

    public static RuntimeException unknownTypeException() {
        return new RuntimeException("unknown type : " + BuildConfig.BUILD_TYPE);
    }
}
