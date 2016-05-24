package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

import java.util.List;

import javax.annotation.Nullable;

public class LinkedAccounts {
    @Nullable
    private final Facebook facebook;

    private LinkedAccounts(@Nullable Facebook facebook) {
        this.facebook = facebook;
    }

    @Nullable
    public Facebook getFacebook() {
        return facebook;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkedAccounts)) return false;
        final LinkedAccounts that = (LinkedAccounts) o;
        return Objects.equal(facebook, that.facebook);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(facebook);
    }

    public class Facebook {
        private final List<String> scopes;
        private final int expiresAt;
        private final String facebookId;

        public Facebook(List<String> scopes, int expiresAt, String facebookId) {
            this.scopes = scopes;
            this.expiresAt = expiresAt;
            this.facebookId = facebookId;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public int getExpiresAt() {
            return expiresAt;
        }

        public String getFacebookId() {
            return facebookId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Facebook)) return false;
            final Facebook facebook = (Facebook) o;
            return expiresAt == facebook.expiresAt &&
                    Objects.equal(scopes, facebook.scopes) &&
                    Objects.equal(facebookId, facebook.facebookId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(scopes, expiresAt, facebookId);
        }
    }
}