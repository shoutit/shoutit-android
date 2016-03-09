package com.shoutit.app.android.view.settings.account.email;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.rx.Actions1;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class ChangeEmailFragment extends BaseFragment {

    @Bind(R.id.account_change_email_et)
    EditText emailEditText;
    @Bind(R.id.account_change_email_til)
    TextInputLayout emailInput;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.account_change_email_confirm_btn)
    Button confirmButton;

    @Inject
    ChangeEmailPresenter presenter;

    public static Fragment newInstance() {
        return new ChangeEmailFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_email, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RxTextView.textChangeEvents(emailEditText)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getEmailObserver());

        RxView.clicks(confirmButton)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(presenter.getConfirmClickSubject());

        presenter.getSuccessObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Toast.makeText(getActivity(), R.string.account_email_changed, Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                });

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getWrongEmailErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.showOrHideError(emailInput, getString(R.string.account_wrong_email)));

        presenter.getApiErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerChangeEmailFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
