package com.shoutit.app.android.view.videoconversation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.widget.CheckableImageButton;
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
import com.twilio.conversations.InviteStatus;
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
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class VideoConversationActivity extends BaseActivity {


    private static final String ARGS_CALLED_USER_USERNAME = "args_person_to_call_username";
    private static final String ARGS_CALLER = "args_caller";
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = VideoConversationActivity.class.getSimpleName();


    private VideoViewRenderer participantVideoRenderer;
    private VideoViewRenderer localVideoRenderer;
    private ConversationsClient conversationClient;
    private CameraCapturer cameraCapturer;
    private Conversation conversation;

    private OutgoingInvite outgoingInvite;
    private IncomingInvite invite;

    @Bind(R.id.video_conversation_layout)
    View videoCallView;
    @Bind(R.id.video_conversation_content_local_window)
    ViewGroup smallPreviewWindow;
    @Bind(R.id.video_conversation_local_preview)
    FrameLayout bigPreviewWindow;
    @Bind(R.id.video_conversation_content_participant_window)
    ViewGroup participantWindow;
    @Bind(R.id.video_conversation_button_call)
    Button callButton;
    @Bind(R.id.conversation_participant_name_tv)
    TextView participantNameTv;

    @Bind(R.id.video_conversation_button_dismiss_call)
    ImageButton dismissCallButton;
    @Bind(R.id.video_conversation_button_audio_btn)
    CheckableImageButton audioButton;
    @Bind(R.id.video_conversation_button_show_preview_btn)
    CheckableImageButton showPreviewButton;
    @Bind(R.id.video_conversation_info)
    TextView conversationInfo;

    @Inject
    UserPreferences preferences;
    @Inject
    Twilio mTwilio;
    @Inject
    CameraTool cameraTool;
    @Inject
    VideoConversationActivityPresenter presenter;

    private String calledUserUsername;
    private String calledUserTwilioIdentity;

    private BehaviorSubject<String> conversationInfoSubject = BehaviorSubject.create();
    private BehaviorSubject<String> conversationErrorSubject = BehaviorSubject.create();

    public static Intent newIntent(@Nullable String callerName,
                                   @Nullable String calledUserUsername,
                                   @Nonnull Context context) {
        return new Intent(context, VideoConversationActivity.class)
                .putExtra(ARGS_CALLED_USER_USERNAME, calledUserUsername)
                .putExtra(ARGS_CALLER, callerName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conversation);
        ButterKnife.bind(this);

        final Intent intent = checkNotNull(getIntent());
        calledUserUsername = intent.getStringExtra(ARGS_CALLED_USER_USERNAME);
        final String callerName = intent.getStringExtra(ARGS_CALLER);
        participantNameTv.setText(callerName);

        if (hasVideoCallPermissions()) {
            initializeVideoConversations();
        }
    }

    private void initializeVideoConversations() {

        conversationInfo.bringToFront();
        conversationClient = mTwilio.getConversationsClient();
        invite = mTwilio.getInvite();
        setupAudioVideo();

        if (calledUserUsername == null) {
            acceptIncomingCall();
        }

        RxView.clicks(callButton)
                .throttleFirst(5, TimeUnit.SECONDS)
                .compose(this.<Void>bindToLifecycle())
                .filter(ignore -> calledUserUsername != null)
                .subscribe(ignore -> {
                    mTwilio.initCalledPersonTwilioIdentityRequest(calledUserUsername);
                    conversationInfoSubject.onNext(getString(R.string.video_call_calling));
                });

        RxView.clicks(dismissCallButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(aVoid -> {
                    stopVideoTimer();
                    closeConversation();
                    finish();
                });

        RxView.clicks(audioButton)
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    muteMicrophone(audioButton.isChecked());
                    audioButton.setBackground(getResources().getDrawable(audioButton.isChecked() ?
                            R.drawable.shape_video_enabled : R.drawable.shape_video_disabled));
                });

        RxView.clicks(showPreviewButton)
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    showCameraPreview(!showPreviewButton.isChecked());
                    showPreviewButton.setBackground(getResources().getDrawable(showPreviewButton.isChecked() ?
                            R.drawable.shape_video_enabled : R.drawable.shape_video_disabled));
                });

        presenter
                .getTimerObservable()
                .compose(bindToLifecycle())
                .subscribe(time -> {
                    conversationInfoSubject.onNext(time);
                });

        mTwilio.getSuccessCalledPersonIdentity()
                .compose(this.<String>bindToLifecycle())
                .subscribe(calledUserIdentity -> {
                    calledUserTwilioIdentity = calledUserIdentity;
                    presenter.getCalledUserTwilioIdentityObserver().onNext(calledUserIdentity);
                    initOutgoingCall();
                });

        conversationInfoSubject
                .filter(Functions1.isNotNull())
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(conversationInfo));

        conversationErrorSubject
                .filter(Functions1.isNotNull())
                .compose(this.<String>bindToLifecycle())
                .subscribe(error -> {
                    ColoredSnackBar.error(videoCallView, error, Snackbar.LENGTH_LONG).show();
                });

        mTwilio.getErrorCalledPersonIdentity()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getMakeCallObservable()
                .compose(this.<BothParams<Set<String>, Boolean>>bindToLifecycle())
                .subscribe(makeOutgoingCall());

        presenter.getRejectCallObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    mTwilio.rejectCall(calledUserTwilioIdentity);
                });
    }

    private void showCameraPreview(boolean showCameraPreview) {
        if (conversation == null || conversation.getLocalMedia() == null ||
                conversation.getLocalMedia().getLocalVideoTracks() == null) {
            if (showCameraPreview) {
                cameraCapturer.startPreview(bigPreviewWindow);
            } else {
                cameraCapturer.stopPreview();
            }
        } else {
            if (showCameraPreview) {
                final LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
                conversation.getLocalMedia().addLocalVideoTrack(localVideoTrack);
            } else {
                for (LocalVideoTrack videoTrack : conversation.getLocalMedia().getLocalVideoTracks()) {
                    conversation.getLocalMedia().removeLocalVideoTrack(videoTrack);
                }
            }
        }
    }

    private CapturerErrorListener capturerErrorListener() {
        return e -> conversationErrorSubject.onNext(getString(R.string.video_calls_camera_issue));
    }

    private LocalMedia setupLocalMedia() {
        final LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        if (!showPreviewButton.isChecked()) {
            final LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
            localMedia.addLocalVideoTrack(localVideoTrack);
        }

        return localMedia;
    }

    private void acceptIncomingCall() {
        if (invite != null && invite.getInviteStatus().equals(InviteStatus.PENDING)) {

            callButton.setVisibility(View.GONE);

            invite.accept(setupLocalMedia(), new ConversationCallback() {
                @Override
                public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                    if (exception == null) {
                        VideoConversationActivity
                                .this
                                .conversation = conversation;
                        conversation
                                .setConversationListener(conversationListener());
                    } else {
                        conversationErrorSubject.onNext(TextHelper.formatErrorMessage(exception.getMessage()));
                    }
                }
            });
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.video_call_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void initOutgoingCall() {

        if (calledUserTwilioIdentity != null && conversationClient != null) {
            callButton.setVisibility(View.GONE);

            final Set<String> participants = new HashSet<>();
            participants.add(calledUserTwilioIdentity);

            presenter.getParticipantsObserver().onNext(participants);
            presenter.getMakeOutgoingCallObserver().onNext(null);
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this),
                    R.string.video_calls_cannot_start_conversation,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @NonNull
    private Action1<BothParams<Set<String>, Boolean>> makeOutgoingCall() {
        return new Action1<BothParams<Set<String>, Boolean>>() {
            @Override
            public void call(BothParams<Set<String>, Boolean> participantsWithIsLastRetry) {
                final Set<String> participants = participantsWithIsLastRetry.param1();
                final Boolean isLastRetry = participantsWithIsLastRetry.param2();

                outgoingInvite = conversationClient
                        .sendConversationInvite(participants, setupLocalMedia(), new ConversationCallback() {
                            @Override
                            public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                                if (exception == null) {
                                    presenter.finishRetries();
                                    VideoConversationActivity.this.conversation = conversation;
                                    conversation.setConversationListener(conversationListener());
                                    Log.d(TAG, "Succesfully connected");
                                } else if (isLastRetry) {
                                    handleTwilioError(exception);
                                    mTwilio.rejectCall(calledUserTwilioIdentity);
                                    Log.d(TAG, "Failed on last retry with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                } else if (exception.getErrorCode() == Twilio.ERROR_PARTICIPANT_UNAVAILABLE) {
                                    Log.d(TAG, "Participant unavailable? with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                    presenter.retryCall();
                                } else {
                                    Log.d(TAG, "Error? with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                    handleTwilioError(exception);
                                    presenter.finishRetries();
                                    mTwilio.rejectCall(calledUserTwilioIdentity);
                                }
                            }
                        });
            }
        };
    }

    private void handleTwilioError(TwilioConversationsException exception) {
        if (exception.getErrorCode() == Twilio.ERROR_PARTICIPANT_REJECTED_CALL) {
            conversationInfoSubject.onNext(getString(R.string.video_calls_participant_reject));
        } else {
            conversationInfoSubject.onNext(TextHelper.formatErrorMessage(exception.getMessage()));
        }
    }

    private LocalMediaListener localMediaListener() {
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                localVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, smallPreviewWindow);
                localVideoTrack.addRenderer(localVideoRenderer);
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                localVideoTrack.removeRenderer(localVideoRenderer);
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {
                localVideoTrack.removeRenderer(localVideoRenderer);
            }
        };
    }

    private ConversationListener conversationListener() {
        return new ConversationListener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                smallPreviewWindow.setVisibility(View.VISIBLE);
                startVideoTimer();

                participant.setParticipantListener(participantListener());
            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, TwilioConversationsException e) {
            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {
                conversationInfoSubject.onNext(getString(R.string.video_calls_participant_disconected));
            }

            @Override
            public void onConversationEnded(Conversation conversation, TwilioConversationsException e) {
                stopVideoTimer();
                if (conversation != null) {
                    conversation.disconnect();
                }
            }
        };
    }

    private void stopVideoTimer() {
        presenter.stopTimer();
    }

    private void startVideoTimer() {
        presenter.startTimer();
    }

    private ParticipantListener participantListener() {
        return new ParticipantListener() {
            @Override
            public void onVideoTrackAdded(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                participantVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, participantWindow);
                participantVideoRenderer.setObserver(new VideoRendererObserver() {

                    @Override
                    public void onFirstFrame() {
                    }

                    @Override
                    public void onFrameDimensionsChanged(int width, int height, int rotation) {
                    }
                });
                videoTrack.addRenderer(participantVideoRenderer);
            }

            @Override
            public void onVideoTrackRemoved(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                participantWindow.removeAllViews();
                videoTrack.removeRenderer(participantVideoRenderer);
            }

            @Override
            public void onAudioTrackAdded(Conversation conversation, Participant participant, AudioTrack audioTrack) {
            }

            @Override
            public void onAudioTrackRemoved(Conversation conversation, Participant participant, AudioTrack audioTrack) {
            }

            @Override
            public void onTrackEnabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
            }

            @Override
            public void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
            }
        };
    }

    private void setupAudioVideo() {

        if (isFrontCameraAvailable()) {
            cameraCapturer = CameraCapturerFactory.createCameraCapturer(VideoConversationActivity.this,
                    CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, capturerErrorListener());
        } else {
            cameraCapturer = CameraCapturerFactory.createCameraCapturer(VideoConversationActivity.this,
                    CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA, capturerErrorListener());
        }
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        showCameraPreview(true);
    }

    private void muteMicrophone(boolean mute) {
        if (conversation == null || conversation.getLocalMedia() == null) {
            return;
        }

        conversation.getLocalMedia().mute(mute);
    }

    private boolean isFrontCameraAvailable() {
        try {
            return cameraTool.isFrontCameraAvailable();
        } catch (CameraTool.CameraException e) {
            conversationErrorSubject.onNext(e.toString());
            return false;
        }
    }

    private boolean hasVideoCallPermissions() {
        return PermissionHelper.checkPermissions(this, CAMERA_MIC_PERMISSION_REQUEST_CODE,
                ColoredSnackBar.contentView(this), R.string.video_calls_no_premissions,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(findViewById(android.R.id.content), R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
                final PackageManager packageManager = getApplicationContext().getPackageManager();
                if (packageManager.checkPermission(Manifest.permission.CAMERA, getApplicationContext().getPackageName()) == PackageManager.PERMISSION_GRANTED &&
                        packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, getApplicationContext().getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    initializeVideoConversations();
                }
            } else {
                ColoredSnackBar.error(findViewById(android.R.id.content), R.string.permission_not_granted, Snackbar.LENGTH_SHORT);
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TwilioConversations.isInitialized() &&
                conversationClient != null &&
                !conversationClient.isListening()) {
            conversationClient.listen();
        }

        if (cameraCapturer != null && !cameraCapturer.isPreviewing()) {
            cameraCapturer.startPreview(bigPreviewWindow);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (TwilioConversations.isInitialized() &&
                conversationClient != null  &&
                conversationClient.isListening() &&
                conversation == null) {
            conversationClient.unlisten();
        }

        if (cameraCapturer != null && cameraCapturer.isPreviewing()) {
            cameraCapturer.stopPreview();
        }
    }

    private void closeConversation() {

        smallPreviewWindow.removeAllViews();
        participantWindow.removeAllViews();
        localVideoRenderer = null;
        participantVideoRenderer = null;

        if (conversation != null) {
            conversation.disconnect();
        }
        if (invite != null) {
            invite.reject();
        }
        if (outgoingInvite != null) {
            outgoingInvite.cancel();
        }

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


