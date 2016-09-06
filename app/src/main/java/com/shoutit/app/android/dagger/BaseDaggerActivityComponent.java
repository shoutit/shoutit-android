package com.shoutit.app.android.dagger;

import com.shoutit.app.android.view.CreateShoutDeepLinkDispatchActivity;
import com.shoutit.app.android.view.conversations.ConversationsActivity;
import com.shoutit.app.android.view.createpage.pagecategory.CreatePageCategoryActivity;
import com.shoutit.app.android.view.credits.transactions.TransactionsActivity;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.interests.InterestsActivity;
import com.shoutit.app.android.view.intro.IntroActivity;
import com.shoutit.app.android.view.invitefriends.contactsinvite.InviteContactsActivity;
import com.shoutit.app.android.view.location.LocationActivity;
import com.shoutit.app.android.view.location.LocationActivityForResult;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.notifications.NotificationsActivity;
import com.shoutit.app.android.view.postlogininterest.PostSignUpActivity;
import com.shoutit.app.android.view.promote.promoted.PromotedActivity;
import com.shoutit.app.android.view.settings.SettingsActivity;
import com.shoutit.app.android.view.settings.account.AccountActivity;
import com.shoutit.app.android.view.shouts.selectshout.SelectShoutActivity;
import com.shoutit.app.android.view.signin.forgotpassword.ForgotPasswordActivity;
import com.shoutit.app.android.view.verifybusiness.VerifyBusinessActivity;
import com.shoutit.app.android.view.verifyemail.VerifyEmailActivity;
import com.shoutit.app.android.view.videoconversation.DialogCallActivity;
import com.shoutit.app.android.view.videoconversation.IncomingVideoCallActivity;
import com.shoutit.app.android.view.videoconversation.OutgoingVideoCallActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
        }
)
/**
 * Universal Component for activities that don't need additional modules.
 * Add inject(Activity activity) method for your activity
 */
public interface BaseDaggerActivityComponent extends BaseActivityComponent {

        void inject(VerifyBusinessActivity activity);
        void inject(PostSignUpActivity activity);
        void inject(OutgoingVideoCallActivity activity);
        void inject(IncomingVideoCallActivity activity);
        void inject(DialogCallActivity activity);
        void inject(VerifyEmailActivity activity);
        void inject(ForgotPasswordActivity forgotPasswordActivity);
        void inject(SelectShoutActivity activity);
        void inject(AccountActivity activity);
        void inject(SettingsActivity activity);
        void inject(PromotedActivity activity);
        void inject(NotificationsActivity activity);
        void inject(LoginIntroActivity activity);
        void inject(LocationActivity activity);
        void inject(LocationActivityForResult activity);
        void inject(IntroActivity activity);
        void inject(DiscoverActivity activity);
        void inject(TransactionsActivity activity);
        void inject(CreatePageCategoryActivity activity);
        void inject(ConversationsActivity activity);
        void inject(InterestsActivity activity);
        void inject(InviteContactsActivity activity);
        void inject(CreateShoutDeepLinkDispatchActivity activity);
}
