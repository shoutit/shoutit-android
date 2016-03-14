package com.shoutit.app.android.api.model;


public class TagDetail implements ProfileType {
    private final String id;
    private final String name;
    private final String apiUrl;
    private final String image;
    private final String cover;
    private final String webUrl;
    private final int listenersCount;
    private final boolean isListening;

    public TagDetail(String id, String name, String apiUrl, String image, String cover, String webUrl, int listenersCount, boolean isListening) {
        this.id = id;
        this.name = name;
        this.apiUrl = apiUrl;
        this.image = image;
        this.cover = cover;
        this.webUrl = webUrl;
        this.listenersCount = listenersCount;
        this.isListening = isListening;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return name;
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

    @Override
    public String getType() {
        return ProfileType.TAG;
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

    public String getCover() {
        return cover;
    }

    public TagDetail toListenedTag() {
        final boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new TagDetail(id, name, apiUrl, image, cover, webUrl, newListenersCount, newIsListening);
    }
}
