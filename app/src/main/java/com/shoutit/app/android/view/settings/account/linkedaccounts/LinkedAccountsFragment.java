package com.shoutit.app.android.view.settings.account.linkedaccounts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.facebook.FacebookHelper;
import com.shoutit.app.android.facebook.FacebookPages;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.loginintro.GoogleHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class LinkedAccountsFragment extends BaseFragment implements LinkedAccountsPresenter.Listener {

    private static final int GOOGLE_SIGN_IN = 0;

    @Bind(R.id.linked_accounts_facebook)
    View facebookView;
    @Bind(R.id.linked_accounts_profile_fb_tv)
    TextView fbProfileTv;
    @Bind(R.id.linked_accounts_google)
    View googleView;
    @Bind(R.id.linked_accounts_profile_g_tv)
    TextView googleProfileTv;
    @Bind(R.id.linked_accounts_facebook_page)
    View facebookPageView;
    @Bind(R.id.linked_accounts_profile_fbpage_tv)
    TextView facebookPageProfileTv;
    @Bind(R.id.main_layout)
    View mainView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    LinkedAccountsPresenter presenter;
    @Inject
    UserPreferences preferences;
    @Inject
    FacebookHelper facebookHelper;
    @Inject
    LayoutInflater inflater;

    private CallbackManager callbackManager;

    public static Fragment newInstance() {
        return new LinkedAccountsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_linked_accounts, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.register(this);

        callbackManager = CallbackManager.Factory.create();

        RxView.clicks(facebookView)
                .compose(this.bindToLifecycle())
                .subscribe(presenter.clickFacebookSubject());

        RxView.clicks(googleView)
                .compose(this.bindToLifecycle())
                .subscribe(presenter.clickGoogleSubject());

        RxView.clicks(facebookPageView)
                .compose(bindToLifecycle())
                .subscribe(presenter.clickFacebookPageSubject());

        presenter.facebookLinkedInfoObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(fbProfileTv));

        presenter.googleLinkedInfoObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(googleProfileTv));

        presenter.getFacebookPageLinkInfoObservable()
                .compose(bindToLifecycle())
                .subscribe(RxTextView.text(facebookPageProfileTv));

        presenter.askForFbTokenObservable()
                .compose(bindToLifecycle())
                .switchMap(o -> FacebookHelper.getToken(getActivity(), callbackManager))
                .subscribe(onGetFacebookTokenSuccessAction(), onGetFacebookTokenFailureAction());

        presenter.askForPagesPermissionsObservable()
                .compose(bindToLifecycle())
                .switchMap(o -> facebookHelper.askForPermissionIfNeeded(
                                getActivity(), FacebookHelper.PAGES_PERMISSIONS, callbackManager, true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseOrError -> {
                    if (responseOrError.isData()) {
                        presenter.showPagesList();
                    } else {
                        progressView.setVisibility(View.GONE);
                        ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()), responseOrError.error()).show();
                    }
                });

        presenter.getPagesListSuccessObservable()
                .compose(bindToLifecycle())
                .subscribe(this::showFacebookPagesDialog);

        presenter.apiMessageObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.errorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(mainView));

        presenter.getLinkgGoogleFailedObservable()
                .compose(bindToLifecycle())
                .subscribe(throwable -> {
                    GoogleHelper.logOutGoogle(getActivity());
                });

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getPagesListEmptyObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    Snackbar.make(ColoredSnackBar.contentView(getActivity()), R.string.linked_account_no_pages, Snackbar.LENGTH_LONG).show();
                });
    }

    private void showFacebookPagesDialog(FacebookPages facebookPages) {
        if (getActivity().isFinishing()) {
            return;
        }

        final List<FacebookPages.FacebookPage> pages = facebookPages.getData();

        class FacebookPagesAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return pages.size();
            }

            @Override
            public FacebookPages.FacebookPage getItem(int position) {
                return pages.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TextView view = (TextView) inflater.inflate(android.R.layout.select_dialog_singlechoice, parent, false);
                view.setText(getItem(position).getName());

                return view;
            }
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.linked_account_select_page))
                .setSingleChoiceItems(new FacebookPagesAdapter(), -1, (dialog, which) -> {
                    presenter.linkFacebookPageSubject(pages.get(which));
                    dialog.dismiss();
                })
                .show();
    }

    @Nonnull
    private Action1<String> onGetFacebookTokenSuccessAction() {
        return token -> presenter.linkFacebookSubject().onNext(token);
    }

    @NonNull
    private Action1<Throwable> onGetFacebookTokenFailureAction() {
        return throwable -> ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()), throwable,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == GOOGLE_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            final GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null && acct.getId() != null) {
                presenter.linkGoogleSubject().onNext(acct.getServerAuthCode());
            }

        } else {
            final boolean handled = callbackManager.onActivityResult(requestCode, resultCode, data);
            if (!handled) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void triggerSignInGoogle() {
        GoogleHelper.loginGoogle(getActivity(), GOOGLE_SIGN_IN);
    }

    @Override
    public void unlinkFacebookDialog() {
        showDialog(getString(R.string.linked_accounts_unlink_facebook), presenter.unlinkFacebookSubject());
    }

    @Override
    public void unlinkGoogleDialog() {
        showDialog(getString(R.string.linked_accounts_unlink_google), presenter.unlinkGoogleSubject());
    }

    @Override
    public void unLinkFacebookPageDialog() {
        showDialog(getString(R.string.linked_accounts_unlink_facebook_page), presenter.unlinkFacebookPageSubject());
    }

    private void showDialog(@Nonnull final String message, @Nonnull PublishSubject<Object> subject) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("yes", (dialog, which) -> {
                    subject.onNext(new Object());
                })
                .setNegativeButton("no", (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unsubscribe();
    }

    @Override
    protected void injectComponent(@Nonnull final BaseActivityComponent baseActivityComponent, @Nonnull final FragmentModule fragmentModule, @Nullable final Bundle savedInstanceState) {
        DaggerLinkedAccountsComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(new FragmentModule(this))
                .build()
                .inject(this);
    }
}
