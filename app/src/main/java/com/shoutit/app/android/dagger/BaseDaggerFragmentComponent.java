package com.shoutit.app.android.dagger;

import com.shoutit.app.android.view.authorization.SignUpChooseFragment;
import com.shoutit.app.android.view.authorization.loginsignup.LogInSignUpFragment;
import com.shoutit.app.android.view.authorization.signup.SignUpFragment;
import com.shoutit.app.android.view.bookmarks.BookmarkedShoutsFragment;
import com.shoutit.app.android.view.credits.CreditsFragment;
import com.shoutit.app.android.view.invitefriends.InviteFacebookFriendsFragment;
import com.shoutit.app.android.view.pages.PagesPagerFragment;
import com.shoutit.app.android.view.pages.my.MyPagesFragment;
import com.shoutit.app.android.view.pages.publics.PublicPagesFragment;
import com.shoutit.app.android.view.postsignup.interests.PostSignUpInterestsFragment;
import com.shoutit.app.android.view.postsignup.users.PostSignUpUsersFragment;
import com.shoutit.app.android.view.search.categories.SearchCategoriesFragment;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = BaseActivityComponent.class,
        modules = {
                FragmentModule.class,
        }
)
public interface BaseDaggerFragmentComponent extends BaseFragmentComponent {

    void inject(BookmarkedShoutsFragment fragment);
    void inject(PostSignUpUsersFragment fragment);
    void inject(PostSignUpInterestsFragment fragment);
    void inject(SignUpChooseFragment fragment);
    void inject(SignUpFragment fragment);
    void inject(LogInSignUpFragment fragment);
    void inject(SearchCategoriesFragment fragment);
    void inject(PagesPagerFragment fragment);
    void inject(PublicPagesFragment fragment);
    void inject(MyPagesFragment fragment);
    void inject(InviteFacebookFriendsFragment fragment);
    void inject(CreditsFragment fragment);
}
