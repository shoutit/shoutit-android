package com.shoutit.app.android.api.model;


public class TagDetail {
    private final String id;
    private final String name;
    private final String apiUrl;
    private final String image;
    private final String webUrl;
    private final int listenersCount;
    private final boolean isListening;

    public TagDetail(String id, String name, String apiUrl, String image, String webUrl, int listenersCount, boolean isListening) {
        this.id = id;
        this.name = name;
        this.apiUrl = apiUrl;
        this.image = image;
        this.webUrl = webUrl;
        this.listenersCount = listenersCount;
        this.isListening = isListening;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getImage() {
        return image;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public int getListenersCount() {
        return listenersCount;
    }

    public boolean isListening() {
        return isListening;
    }

    public TagDetail toLisenedTag() {
        final boolean newIsListening = !isListening;
        int listenersCount = isListening ? user.listenersCount + 1 : user.listenersCount - 1;
        return null;
    }
}
