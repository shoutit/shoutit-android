package com.shoutit.app.android.api.model;

public class UpdatePage {

    private final String name;
    private final String username;
    private final String website;
    private final String about;
    private final String description;
    private final String phone;
    private final String founded;
    private final String impressum;
    private final String overview;
    private final String mission;
    private final String generalInfo;
    private final String cover;
    private final String image;
    private final boolean isVerified;
    private final boolean isPublished;
    private final UserLocation location;


    public UpdatePage(String name, String username, String website, String about, String description,
                      String phone, String founded, String impressum, String overview, String mission,
                      String generalInfo, String cover, String image, boolean isVerified,
                      boolean isPublished, UserLocation location) {
        this.name = name;
        this.username = username;
        this.website = website;
        this.about = about;
        this.description = description;
        this.phone = phone;
        this.founded = founded;
        this.impressum = impressum;
        this.overview = overview;
        this.mission = mission;
        this.generalInfo = generalInfo;
        this.cover = cover;
        this.image = image;
        this.isVerified = isVerified;
        this.isPublished = isPublished;
        this.location = location;
    }

    public String getAbout() {
        return about;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getFounded() {
        return founded;
    }

    public String getImpressum() {
        return impressum;
    }

    public String getOverview() {
        return overview;
    }

    public String getMission() {
        return mission;
    }

    public String getGeneralInfo() {
        return generalInfo;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public String getCover() {
        return cover;
    }

    public String getImage() {
        return image;
    }
}
