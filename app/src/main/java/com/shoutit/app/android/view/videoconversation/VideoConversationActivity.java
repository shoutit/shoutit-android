package com.shoutit.app.android.view.videoconversation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PermissionHelper;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.ConversationListener;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.MediaTrack;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.Participant;
import com.twilio.conversations.ParticipantListener;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.VideoRendererObserver;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.VideoViewRenderer;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class VideoConversationActivity extends BaseActivity {

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final String VC = "VIDEO_CALL_TWILIO";
    private static final String VC_ERROR ="VIDEO_CALL_ERROR";
    private static final String ARGS_USERNAME = "args_username";

    private Conversation conversation;
    private OutgoingInvite outgoingInvite;
    private VideoViewRenderer participantVideoRenderer;
    private VideoViewRenderer localVideoRenderer;
    private CameraCapturer cameraCapturer;
    private IncomingInvite invite;

    @Bind(R.id.video_conversation_layout)
    View videoCallView;
    @Bind(R.id.video_conversation_local_preview)
    FrameLayout localVideoPreview;
    @Bind(R.id.video_conversation_content_local_window)
    ViewGroup localWindow;
    @Bind(R.id.video_conversation_content_participant_window)
    ViewGroup participantWindow;
    @Bind(R.id.video_conversation_button_call)
    Button callButton;
    @Bind(R.id.video_conversation_button_dismiss_call)
    ImageButton dismissCallButton;

    @Inject
    VideoConversationPresenter presenter;
    @Inject
    UserPreferences preferences;

    private String username;
    private ConversationsClient conversationClient;

    public static Intent newIntent(@Nullable String username, @Nonnull Context context) {
        return new Intent(context, VideoConversationActivity.class).putExtra(ARGS_USERNAME, username);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conversation);
        ButterKnife.bind(this);

        setupVariablesFromApp();
        setupAudioVideo();

        PermissionHelper.
                checkPermissions(this, CAMERA_MIC_PERMISSION_REQUEST_CODE,
                        ColoredSnackBar.contentView(this), R.string.video_calls_no_premissions,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA});

        if (username == null) {
            AcceptIncomingCall();
        }

        RxView.clicks(callButton)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        MakeOutgoingCall();
                    }
                });

        RxView.clicks(dismissCallButton)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        closeActivity();
                    }
                });
    }

    private CapturerErrorListener capturerErrorListener() {
        return new CapturerErrorListener() {
            @Override
            public void onError(CapturerException e) {
                ColoredSnackBar.colorSnackBar(videoCallView, R.string.video_calls_camera_issue, Snackbar.LENGTH_SHORT, R.color.snackbar_error);
            }
        };
    }

    private LocalMedia setupLocalMedia() {
        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());
        LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
        localMedia.addLocalVideoTrack(localVideoTrack);
        return localMedia;
    }

    private void AcceptIncomingCall() {

        callButton.setVisibility(View.GONE);
        invite.accept(setupLocalMedia(), new ConversationCallback() {
            @Override
            public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                if (exception == null) {
                    VideoConversationActivity.this.conversation = conversation;
                    conversation.setConversationListener(conversationListener());
                } else {
                    Log.e(VC_ERROR, "AcceptIncomingCall" + exception.getMessage());
                    closeActivity();
                }
            }
        });
    }

    private void MakeOutgoingCall() {

        callButton.setVisibility(View.GONE);

        if (!username.isEmpty() && conversationClient != null) {
            cameraCapturer.stopPreview();

            Set<String> participants = new HashSet<>();
            participants.add(username);

            outgoingInvite = conversationClient.sendConversationInvite(participants,
                    setupLocalMedia(), new ConversationCallback() {
                        @Override
                        public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                            if (exception == null) {
                                VideoConversationActivity.this.conversation = conversation;
                                conversation.setConversationListener(conversationListener());
                            } else {
                                Log.e(VC_ERROR,"MakeOutgoingCall: " + exception.getMessage());
                                closeActivity();
                            }
                        }
                    });
        }
    }

    private LocalMediaListener localMediaListener() {
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                Log.d(VC, "localMediaListener : onLocalVideoTrackAdded");
                cameraCapturer.stopPreview();
                localVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, localWindow);
                localVideoTrack.addRenderer(localVideoRenderer);

            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                Log.d(VC, "localMediaListener : onLocalVideoTrackRemoved");
                localWindow.removeAllViews();
                ((App) getApplication()).getConversationsClient().listen();

            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {
                Log.e(VC_ERROR, "localMediaListener : LocalVideoTrackError: " + e.getMessage());
            }
        };
    }

    private ConversationListener conversationListener() {
        return new ConversationListener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                Log.d(VC, "conversationListener : onParticipantConnected " + participant.getIdentity());
                participant.setParticipantListener(participantListener());
            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, TwilioConversationsException e) {
                Log.d(VC, "conversationListener : onFailedToConnectParticipant " + participant.getIdentity());
            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {
                Log.d(VC, "conversationListener : onParticipantDisconnected " + participant.getIdentity());
                closeActivity();

            }

            @Override
            public void onConversationEnded(Conversation conversation, TwilioConversationsException e) {
                Log.d(VC, "conversationListener : onConversationEnded");
//                cameraCapturer.stopPreview();
//                ((App) getApplication()).getConversationsClient().listen();
//                if (conversation != null) {
//                    conversation.disconnect();
//                }
            }
        };
    }

    private ParticipantListener participantListener() {
        return new ParticipantListener() {
            @Override
            public void onVideoTrackAdded(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                Log.i(VC, "participantListener : onVideoTrackAdded " + participant.getIdentity());

                participantVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, participantWindow);
                participantVideoRenderer.setObserver(new VideoRendererObserver() {

                    @Override
                    public void onFirstFrame() {
                        Log.d(VC, "Participant onFirstFrame");
                    }

                    @Override
                    public void onFrameDimensionsChanged(int width, int height, int rotation) {
                    }
                });
                videoTrack.addRenderer(participantVideoRenderer);
            }

            @Override
            public void onVideoTrackRemoved(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                Log.d(VC, "participantListener : onVideoTrackRemoved " + participant.getIdentity());
                participantWindow.removeAllViews();

            }

            @Override
            public void onAudioTrackAdded(Conversation conversation, Participant participant, AudioTrack audioTrack) {
                Log.d(VC, "participantListener : onAudioTrackAdded " + participant.getIdentity());
            }

            @Override
            public void onAudioTrackRemoved(Conversation conversation, Participant participant, AudioTrack audioTrack) {
                Log.d(VC, "participantListener :  onAudioTrackRemoved " + participant.getIdentity());
            }

            @Override
            public void onTrackEnabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                Log.i(VC, "participantListener : onTrackEnabled " + participant.getIdentity());
            }

            @Override
            public void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                Log.i(VC, "participantListener :  onTrackDisabled " + participant.getIdentity());
            }
        };
    }

    private void setupAudioVideo() {
        cameraCapturer = CameraCapturerFactory.createCameraCapturer(VideoConversationActivity.this,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, localVideoPreview, capturerErrorListener());
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        conversationClient.setAudioOutput(AudioOutput.SPEAKERPHONE);
        cameraCapturer.startPreview();
    }

    private void setupVariablesFromApp() {
        final Intent intent = getIntent();
        username = intent.getStringExtra(ARGS_USERNAME);
        conversationClient = ((App) getApplication()).getConversationsClient();
        invite = ((App) getApplication()).getInvite();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TwilioConversations.isInitialized() && conversationClient != null && !conversationClient.isListening()) {
            conversationClient.listen();
        }
            cameraCapturer.startPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (TwilioConversations.isInitialized() && conversationClient != null
                && conversationClient.isListening() && conversation == null) {
            conversationClient.unlisten();
        }
        if (cameraCapturer != null && cameraCapturer.isPreviewing()) {
            cameraCapturer.stopPreview();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (cameraCapturer != null && cameraCapturer.isPreviewing()) {
            cameraCapturer.stopPreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraCapturer.stopPreview();
        ((App) getApplication()).getConversationsClient().listen();
    }

    private void closeActivity() {
        if (conversation != null) {
            conversation.disconnect();
        }
        else if (outgoingInvite != null) {
                 outgoingInvite.cancel();
        }
        finish();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final VideoConversationComponent component = DaggerVideoConversationComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}


