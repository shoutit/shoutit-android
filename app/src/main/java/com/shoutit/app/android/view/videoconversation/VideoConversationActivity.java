package com.shoutit.app.android.view.videoconversation;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.widget.CheckableImageButton;
import com.squareup.picasso.Picasso;
import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.InviteStatus;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.MediaTrack;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.Participant;
import com.twilio.conversations.TwilioConversationsClient;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.VideoRenderer;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.VideoViewRenderer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class VideoConversationActivity extends BaseActivity {


    private static final String ARGS_CALLED_USER_IMAGE = "args_person_to_call_image";
    private static final String ARGS_CALLED_USER_USERNAME = "args_person_to_call_username";
    private static final String ARGS_CALLER = "args_caller";
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = VideoConversationActivity.class.getSimpleName();


    private VideoViewRenderer participantVideoRenderer;
    private VideoViewRenderer localVideoRenderer;
    private TwilioConversationsClient conversationClient;
    private CameraCapturer cameraCapturer;
    private Conversation conversation;

    private OutgoingInvite outgoingInvite;
    private IncomingInvite invite;

    @Bind(R.id.video_conversation_layout)
    View rootView;
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
    @Bind(R.id.video_conversation_participant_image)
    ImageView participantImageView;

    @Bind(R.id.video_conversation_button_dismiss_call)
    ImageButton dismissCallButton;
    @Bind(R.id.video_conversation_button_audio_btn)
    CheckableImageButton audioButton;
    @Bind(R.id.video_conversation_button_show_preview_btn)
    CheckableImageButton hideVideoButton;
    @Bind(R.id.video_conversation_info)
    TextView conversationInfo;
    @Bind(R.id.video_conversation_info_view)
    View conversationInfoView;
    @Bind(R.id.video_conversation_watermark_iv)
    View watermarkView;
    @Bind(R.id.video_conversation_action_buttons_container)
    View actionButtonsContainer;
    @Bind(R.id.video_conversation_content_local_window_cover)
    View smallPreviewCoverView;

    @Inject
    UserPreferences preferences;
    @Inject
    Twilio mTwilio;
    @Inject
    CameraTool cameraTool;
    @Inject
    VideoConversationActivityPresenter presenter;
    @Inject
    Picasso picasso;

    private String calledUserUsername;
    private String calledUserTwilioIdentity;

    private PublishSubject<String> conversationInfoSubject = PublishSubject.create();
    private PublishSubject<String> conversationErrorSubject = PublishSubject.create();

    public static Intent newIntent(@Nullable String callerName,
                                   @Nullable String calledUserUsername,
                                   @Nullable String calledUserImage,
                                   @Nonnull Context context) {
        return new Intent(context, VideoConversationActivity.class)
                .putExtra(ARGS_CALLED_USER_USERNAME, calledUserUsername)
                .putExtra(ARGS_CALLED_USER_IMAGE, calledUserImage)
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

        final String calledUserImage = intent.getStringExtra(ARGS_CALLED_USER_IMAGE);
        if (!TextUtils.isEmpty(calledUserImage)) {
            picasso.load(calledUserImage)
                    .fit()
                    .centerCrop()
                    .into(participantImageView);
        }

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
                    muteMicrophoneIfNeeded();
                    audioButton.setBackground(getResources().getDrawable(audioButton.isChecked() ?
                            R.drawable.shape_video_enabled : R.drawable.shape_video_disabled));
                });

        RxView.clicks(hideVideoButton)
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    showOrHideVideo(shouldShowVideo());
                    hideVideoButton.setBackground(getResources().getDrawable(hideVideoButton.isChecked() ?
                            R.drawable.shape_video_enabled : R.drawable.shape_video_disabled));
                });

        RxView.clicks(rootView)
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    switchToFullScreenMode(conversationInfoView.getVisibility() == View.VISIBLE);
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
                    ColoredSnackBar.error(rootView, error, Snackbar.LENGTH_LONG).show();
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

    private void showOrHideVideo(boolean showVideo) {
        LogHelper.logIfDebug(TAG, "Show camera preview:" + showVideo);
        if (conversation != null && conversation.getLocalMedia() != null &&
                conversation.getLocalMedia().getLocalVideoTracks() != null) {
            final LocalVideoTrack track = conversation.getLocalMedia().getLocalVideoTracks().get(0);
            track.enable(showVideo);
            showOrHideSmallPreview(showVideo);
            participantImageView.setVisibility(View.GONE);
        } else if (isCalling()) {
            showOrHideSmallPreview(showVideo);
            participantImageView.setVisibility(View.VISIBLE);
        } else {
            showOrHideParticipantView(showVideo);
        }
    }

    private boolean isCalling() {
        return conversation == null && outgoingInvite != null;
    }

    private boolean shouldShowVideo() {
        return !hideVideoButton.isChecked();
    }

    private void switchToFullScreenMode(boolean fullscreen) {
        if (conversation == null) {
            return;
        }

        final int startPosition = fullscreen ? 0 : -conversationInfoView.getHeight();
        final int endPosition = fullscreen ? -conversationInfoView.getHeight() : 0;
        final ObjectAnimator topViewAnimator = ObjectAnimator.ofFloat(
                conversationInfoView, "translationY", startPosition, endPosition);
        topViewAnimator.setDuration(250);
        topViewAnimator.setInterpolator(new AccelerateInterpolator());

        final int bottomStartPosition = fullscreen ? 0 : actionButtonsContainer.getHeight();
        final int bottomEndPosition = fullscreen ? actionButtonsContainer.getHeight() : 0;
        final ObjectAnimator bottomViewAnimator = ObjectAnimator.ofFloat(
                actionButtonsContainer, "translationY", bottomStartPosition, bottomEndPosition);
        bottomViewAnimator.setDuration(250);
        bottomViewAnimator.setInterpolator(new AccelerateInterpolator());

        final int startAlpha = fullscreen ? 0 : 1;
        final int endAlpha = fullscreen ? 1 : 0;
        final ObjectAnimator waterMarkAnimator = ObjectAnimator.ofFloat(watermarkView, "alpha", startAlpha, endAlpha);
        waterMarkAnimator.setDuration(250);
        waterMarkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (fullscreen) {
                    watermarkView.setVisibility(View.VISIBLE);
                } else {
                    conversationInfoView.setVisibility(View.VISIBLE);
                    actionButtonsContainer.setVisibility(View.VISIBLE);
                }
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!fullscreen) {
                    watermarkView.setVisibility(View.GONE);
                } else {
                    conversationInfoView.setVisibility(View.GONE);
                    actionButtonsContainer.setVisibility(View.GONE);
                }
                super.onAnimationEnd(animation);
            }
        });

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(topViewAnimator, bottomViewAnimator, waterMarkAnimator);
        animatorSet.start();
    }

    private CapturerErrorListener capturerErrorListener() {
        return e -> conversationErrorSubject.onNext(getString(R.string.video_calls_camera_issue));
    }

    private LocalMedia setupLocalMedia() {
        final LocalMedia localMedia = new LocalMedia(localMediaListener());
        final LocalVideoTrack localVideoTrack = new LocalVideoTrack(cameraCapturer);
        localMedia.addLocalVideoTrack(localVideoTrack);

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

                smallPreviewWindow.setVisibility(View.VISIBLE);
                smallPreviewCoverView.setVisibility(View.VISIBLE);
                showOrHideSmallPreview(true);

                outgoingInvite = conversationClient
                        .inviteToConversation(participants, setupLocalMedia(), new ConversationCallback() {
                            @Override
                            public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                                if (exception == null) {
                                    presenter.finishRetries();
                                    VideoConversationActivity.this.conversation = conversation;
                                    conversation.setConversationListener(conversationListener());
                                    Log.d(TAG, "Succesfully connected");
                                } else if (isLastRetry) {
                                    handleTwilioError(exception);
                                    switchToFullScreenMode(false);
                                    mTwilio.rejectCall(calledUserTwilioIdentity);
                                    Log.d(TAG, "Failed on last retry with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                } else if (exception.getErrorCode() == Twilio.ERROR_PARTICIPANT_UNAVAILABLE) {
                                    Log.d(TAG, "Participant unavailable? with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                    presenter.retryCall();
                                } else {
                                    Log.d(TAG, "Error? with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                    switchToFullScreenMode(false);
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

    private LocalMedia.Listener localMediaListener() {
        return new LocalMedia.Listener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                LogHelper.logIfDebug(TAG, "onLocalVideoTrackAdded");
                showOrHideVideo(shouldShowVideo());
                localVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, smallPreviewWindow);
                localVideoTrack.addRenderer(localVideoRenderer);
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                LogHelper.logIfDebug(TAG, "onLocalVideoTrackRemoved");
                for (VideoRenderer videoRenderer : localVideoTrack.getRenderers()) {
                    localVideoTrack.removeRenderer(videoRenderer);
                }
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {
                for (VideoRenderer videoRenderer : localVideoTrack.getRenderers()) {
                    localVideoTrack.removeRenderer(videoRenderer);
                }
            }
        };
    }

    private Conversation.Listener conversationListener() {
        return new Conversation.Listener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                smallPreviewWindow.setVisibility(View.VISIBLE);
                smallPreviewCoverView.setVisibility(shouldShowVideo() ? View.GONE : View.VISIBLE);
                showOrHideVideo(shouldShowVideo());
                startVideoTimer();
                muteMicrophoneIfNeeded();

                participant.setParticipantListener(participantListener());
            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, TwilioConversationsException e) {
            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {
                stopVideoTimer();
                switchToFullScreenMode(false);
                conversationInfoSubject.onNext(getString(R.string.video_calls_participant_disconected));
            }

            @Override
            public void onConversationEnded(Conversation conversation, TwilioConversationsException e) {
                stopVideoTimer();
                closeConversation();
            }
        };
    }

    private void stopVideoTimer() {
        presenter.stopTimer();
    }

    private void startVideoTimer() {
        presenter.startTimer();
    }

    private Participant.Listener participantListener() {
        return new Participant.Listener() {
            @Override
            public void onVideoTrackAdded(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                participantVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, participantWindow);
                participantVideoRenderer.setObserver(new VideoRenderer.Observer() {

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
                if (!isAudioTrack(mediaTrack, participant.getMedia().getAudioTracks())) {
                    showOrHideParticipantView(true);
                }
            }

            @Override
            public void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                if (!isAudioTrack(mediaTrack, participant.getMedia().getAudioTracks())) {
                    showOrHideParticipantView(false);
                }
            }
        };
    }

    private boolean isAudioTrack(@Nonnull MediaTrack mediaTrack, @Nonnull List<AudioTrack> audioTracks) {
        for (AudioTrack track : audioTracks) {
            if (track.getTrackId().equals(mediaTrack.getTrackId())) {
                return true;
            }
        }

        return false;
    }

    private void setupAudioVideo() {

        if (isFrontCameraAvailable()) {
            cameraCapturer = CameraCapturer.create(VideoConversationActivity.this,
                    CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, capturerErrorListener());
        } else {
            cameraCapturer = CameraCapturer.create(VideoConversationActivity.this,
                    CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA, capturerErrorListener());
        }

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        startPreview();
    }

    private void muteMicrophoneIfNeeded() {
        if (conversation == null || conversation.getLocalMedia() == null) {
            return;
        }

        conversation.getLocalMedia().mute(audioButton.isChecked());
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

        if (TwilioConversationsClient.isInitialized() &&
                conversationClient != null &&
                !conversationClient.isListening()) {
            conversationClient.listen();
        }

        startPreview();
    }

    private void startPreview() {
        if (cameraCapturer != null) {
            cameraCapturer.startPreview(bigPreviewWindow);
        }
    }

    private void stopPreview() {
        if (cameraCapturer != null) {
            cameraCapturer.stopPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (TwilioConversationsClient.isInitialized() &&
                conversationClient != null  &&
                conversationClient.isListening() &&
                conversation == null) {
            conversationClient.unlisten();
        }

        stopPreview();
    }

    private void showOrHideSmallPreview(boolean show) {
        smallPreviewCoverView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showOrHideParticipantView(boolean show) {
        participantImageView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void closeConversation() {

        smallPreviewWindow.removeAllViews();
        smallPreviewCoverView.setVisibility(View.VISIBLE);
        participantWindow.removeAllViews();
        participantImageView.setVisibility(View.VISIBLE);

        localVideoRenderer = null;
        participantVideoRenderer = null;

        if (conversation != null) {
            conversation.disconnect();
            conversation = null;
        }

        if (invite != null) {
            invite.reject();
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


