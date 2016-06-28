package com.shoutit.app.android.view.admins;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.chooseprofile.SelectProfileActivity;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.profileslist.BaseProfileListFragment;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class AdminsFragment extends BaseProfileListFragment implements AdminsDialog.AdminsDialogListener {

    private static final int REQUEST_CODE_SELECT_ADMIN = 2;
    public static final String EXTRA_SELECTED_ADMIN_USERNAME = "slected_admin_username";

    @Inject
    AdminsDialog adminsDialog;

    private AdminsFragmentPresenter presenter;

    private List<MenuItem> mMenuItems = Lists.newArrayList();
    private AdminsFragmentComponent component;

    public static Fragment newInstance() {
        return new AdminsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        presenter = (AdminsFragmentPresenter) component.profileListPresenter();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.getProfileSelectedObservable()
                .compose(bindToLifecycle())
                .subscribe(this::showActionsDialog);

        presenter.getSuccessRemoveAdminObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getSuccessAddAdminObservable()
                .compose(bindToLifecycle())
                .subscribe();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        for (int i = 0; i < menu.size(); i++) {
            final MenuItem item = menu.getItem(i);
            item.setVisible(false);
            mMenuItems.add(item);
        }

        inflater.inflate(R.menu.menu_admin, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.admins_menu_add:
                startActivityForResult(SelectProfileActivity.newIntent(getActivity()), REQUEST_CODE_SELECT_ADMIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showActionsDialog(String userName) {
        adminsDialog.show(userName, this);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        component = DaggerAdminsFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .adminsFragmentModule(new AdminsFragmentModule(this))
                .build();

        component.inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (MenuItem item : mMenuItems) {
            item.setVisible(true);
        }
    }

    @Override
    public void showProfile(String userName) {
        startActivityForResult(UserOrPageProfileActivity.newIntent(getActivity(), userName), REQUEST_OPENED_PROFILE_WAS_LISTENED);
    }

    @Override
    public void removeAdmin(String userName) {
        presenter.removeAdmin(userName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SELECT_ADMIN) {
            final String selectedAdminUserName = checkNotNull(data.getStringExtra(EXTRA_SELECTED_ADMIN_USERNAME));
            presenter.addAdmin(selectedAdminUserName);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
