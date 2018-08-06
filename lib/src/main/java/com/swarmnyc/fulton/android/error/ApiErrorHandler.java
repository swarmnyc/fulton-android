package com.swarmnyc.fulton.android.error;

@FunctionalInterface
public interface ApiErrorHandler {
    void onError(Throwable error);
}