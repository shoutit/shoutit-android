package com.appunite.appunitegcm;

public interface GcmPushHandler {
    String getServerKey();

    /**
     * Push date should be json object, for example :
     * {
     *     "message" : "GCM push test",
     *     "time" : "15:10"
     * }
     * @return
     */
    String getGcmPushData();
}
