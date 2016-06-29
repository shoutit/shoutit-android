package com.shoutit.app.android.model;

import com.google.common.base.Objects;
import com.shoutit.app.android.view.listenings.ListeningsPresenter;

public class ListeningsPointer {
    private final ListeningsPresenter.ListeningsType listeningsType;
    private final String userName;

    public ListeningsPointer(ListeningsPresenter.ListeningsType listeningsType, String userName) {
        this.listeningsType = listeningsType;
        this.userName = userName;
    }

    public ListeningsPresenter.ListeningsType getListeningsType() {
        return listeningsType;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListeningsPointer)) return false;
        ListeningsPointer that = (ListeningsPointer) o;
        return listeningsType == that.listeningsType &&
                Objects.equal(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(listeningsType, userName);
    }
}