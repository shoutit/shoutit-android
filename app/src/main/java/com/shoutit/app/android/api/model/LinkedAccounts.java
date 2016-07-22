package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

import java.util.List;

import javax.annotation.Nullable;

public class LinkedAccounts {

    @Nullable
    private final Facebook facebook;
    @Nullable
    private final Gplus gplus;
    @Nullable
    private final FacebookPage facebookPage;

    public LinkedAccounts(@Nullable Facebook facebook, @Nullable final Gplus gplus, @Nullable FacebookPage facebookPage) {
        this.facebook = facebook;
        this.gplus = gplus;
        this.facebookPage = facebookPage;
    }

    public LinkedAccounts unlinkedFacebook(){
        return new LinkedAccounts(null, gplus, facebookPage);
    }

    public LinkedAccounts unlinkedGoogle(){
        return new LinkedAccounts(facebook, null, facebookPage);
    }
    public LinkedAccounts updatedGoogle(String token){
        return new LinkedAccounts(facebook, new Gplus(token), facebookPage);
    }
    @Nullable
    public Facebook getFacebook() {
        return facebook;
    }

    @Nullable
    public Gplus getGplus() {return  gplus;}

    @Nullable
    public FacebookPage getFacebookPage() {
        return facebookPage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkedAccounts)) return false;
        LinkedAccounts that = (LinkedAccounts) o;
        return Objects.equal(facebook, that.facebook) &&
                Objects.equal(gplus, that.gplus) &&
                Objects.equal(facebookPage, that.facebookPage);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(facebook, gplus, facebookPage);
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

    public class Gplus {
        private final String gplusId;

        public String getGplusId() {
            return gplusId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Gplus gplus = (Gplus) o;
            return Objects.equal(gplusId, gplus.gplusId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(gplusId);
        }

        public Gplus(final String gplusId) {
            this.gplusId = gplusId;

        }
    }


}