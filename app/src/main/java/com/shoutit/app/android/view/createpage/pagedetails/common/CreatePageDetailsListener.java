package com.shoutit.app.android.view.createpage.pagedetails.common;

import java.util.List;

public interface CreatePageDetailsListener {

    void setCategories(List<CategoryInfo> categoryInfoss);

    void showProgress(boolean show);

    void error();

    void startMainActivity();

    void nameEmpty();

    void setToolbarTitle(String title);
}