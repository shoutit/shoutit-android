package com.shoutit.app.android.view.chats.chat_info.chats_participants;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ParticipantDialog {

    private final Context mContext;

    @Bind(R.id.chat_participant_block)
    Button mChatParticipantBlock;
    @Bind(R.id.chat_participant_promote)
    Button mChatParticipantPromote;
    @Bind(R.id.chat_participant_remove)
    Button mChatParticipantRemove;

    @Inject
    public ParticipantDialog(@ForActivity Context context) {
        mContext = context;
    }

    public void show(final String id, final boolean isBlocked, boolean isAdmin, String name, final ChatParticipantsPresenter presenter) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.chat_participant_action_dialog, null, false);
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(name)
                .setView(view)
                .create();
        ButterKnife.bind(this, view);
        mChatParticipantPromote.setVisibility(isAdmin || isBlocked ? View.GONE : View.VISIBLE);
        mChatParticipantPromote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.adminAction(id);
                alertDialog.dismiss();
            }
        });

        mChatParticipantRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.removeUser(id);
                alertDialog.dismiss();
            }
        });

        mChatParticipantBlock.setText(isBlocked ? "Unblock" : "Block");
        mChatParticipantBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.blockAction(id, !isBlocked);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

}
