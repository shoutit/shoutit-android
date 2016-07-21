package com.shoutit.app.android.view.invitefriends.contactsinvite;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteContactsAdapter extends BaseAdapter {

    @Inject
    public InviteContactsAdapter(@ForActivity @Nonnull Context context) {
        super(context);
    }

    public static class ContactViewHolder extends ViewHolderManager.BaseViewHolder<InviteContactsPresenter.ContactAdapterItem> {

        @Bind(R.id.invite_contacts_name_tv)
        TextView nameTv;

        private InviteContactsPresenter.ContactAdapterItem item;

        public ContactViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull InviteContactsPresenter.ContactAdapterItem contactAdapterItem) {
            item = contactAdapterItem;
            nameTv.setText(contactAdapterItem.getContact().getName());
        }

        @OnClick(R.id.invite_contacts_name_tv)
        public void onItemClicked() {
            item.onContactSelected();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(layoutInflater.inflate(R.layout.invite_contacts_item, parent, false));
    }
}
