package com.shoutit.app.android.view.videoconversation;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.functions.Functions1;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.widget.CheckableImageButton;
import com.squareup.picasso.Picasso;
import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.MediaTrack;
import com.twilio.conversations.Participant;
import com.twilio.conversations.TwilioConversationsClient;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.VideoRenderer;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.VideoViewRenderer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subjects.PublishSubject;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public abstract class VideoCallActivity extends BaseActivity {

    protected static final String ARGS_CALLED_USER_IMAGE = "called_user_image";
    protected static final String ARGS_PARTICIPANT_NAME = "participant_name";

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    protected static final String TAG = VideoCallActivity.class.getSimpleName();

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

    private VideoViewRenderer participantVideoRenderer;
    private VideoViewRenderer localVideoRenderer;
    private CameraCapturer cameraCapturer;
    private PublishSubject<String> conversationInfoSubject = PublishSubject.create();
    private PublishSubject<String> conversationErrorSubject = PublishSubject.create();

    protected TwilioConversationsClient conversationClient;
    protected Conversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conversation);
        ButterKnife.bind(this);

        initData(getIntent());
    }

    private void initData(@Nonnull Intent intent) {
        checkNotNull(intent);

        final String callerName = intent.getStringExtra(ARGS_PARTICIPANT_NAME);
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

    protected void initializeVideoConversations() {

        conversationInfo.bringToFront();
        conversationClient = mTwilio.getConversationsClient();
        setupAudioVideo();

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
                .subscribe(this::showConversationInfo);

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
    }

    protected LocalMedia setupLocalMedia() {
        final LocalMedia localMedia = new LocalMedia(localMediaListener());
        final LocalVideoTrack localVideoTrack = new LocalVideoTrack(cameraCapturer);
        localMedia.addLocalVideoTrack(localVideoTrack);

        return localMedia;
    }

    protected void showError(String errorMessage) {
        conversationErrorSubject.onNext(errorMessage);
    }

    protected void showConversationInfo(String message) {
        conversationInfoSubject.onNext(message);
    }

    private LocalMedia.Listener localMediaListener() {
        return new LocalMedia.Listener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                LogHelper.logIfDebug(TAG, "onLocalVideoTrackAdded");
                localVideoRenderer = new VideoViewRenderer(VideoCallActivity.this, smallPreviewWindow);
                localVideoTrack.addRenderer(localVideoRenderer);
                showOrHideVideo(shouldShowVideo());
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
                LogHelper.logIfDebug(TAG, "onLocalVideoTrackError");
                for (VideoRenderer videoRenderer : localVideoTrack.getRenderers()) {
                    localVideoTrack.removeRenderer(videoRenderer);
                }
            }
        };
    }

    protected Conversation.Listener conversationListener() {
        return new Conversation.Listener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                LogHelper.logIfDebug(TAG, "onParticipantConnected, convSid:" + conversation.getSid());

                smallPreviewWindow.setVisibility(View.VISIBLE);
                smallPreviewCoverView.setVisibility(shouldShowVideo() ? View.GONE : View.VISIBLE);
                showOrHideVideo(shouldShowVideo());
                startVideoTimer();
                muteMicrophoneIfNeeded();

                participant.setParticipantListener(participantListener());
            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, TwilioConversationsException e) {
                LogHelper.logIfDebug(TAG, "onFailedToConnectParticipant");
                if (e != null) {
                    LogHelper.logIfDebug(TAG, "onF" +
                            "ailedToConnectParticipant " + "mesage: " + e.getMessage()
                            + " code: " + e.getErrorCode() + " convSid:" + conversation.getSid());
                }
            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {
                LogHelper.logIfDebug(TAG, "onParticipantDisconnected"  + " convSid:" + conversation.getSid());

                stopVideoTimer();
                switchToFullScreenMode(false);
                showConversationInfo(getString(R.string.video_calls_participant_disconected));
                closeConversation();
            }

            @Override
            public void onConversationEnded(Conversation conversation, TwilioConversationsException e) {
                LogHelper.logIfDebug(TAG, "onConversationEnded " + "sid " + conversation.getSid());
                if (e != null) {
                    LogHelper.logIfDebug(TAG, "onConversationEnded with error: " + e.getMessage() + " " + e.getErrorCode());
                }
                stopVideoTimer();
                closeConversation();
            }
        };
    }

    private Participant.Listener participantListener() {
        return new Participant.Listener() {
            @Override
            public void onVideoTrackAdded(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                LogHelper.logIfDebug(TAG, "onVideoTrackAdded");
                showOrHideParticipantView(videoTrack.isEnabled());
                participantVideoRenderer = new VideoViewRenderer(VideoCallActivity.this, participantWindow);
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
                LogHelper.logIfDebug(TAG, "onVideoTrackRemoved");
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
                LogHelper.logIfDebug(TAG, "onTrackEnabled");
                if (!isAudioTrack(mediaTrack, participant.getMedia().getAudioTracks())) {
                    showOrHideParticipantView(true);
                }
            }

            @Override
            public void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                LogHelper.logIfDebug(TAG, "onTrackDisabled");
                if (!isAudioTrack(mediaTrack, participant.getMedia().getAudioTracks())) {
                    showOrHideParticipantView(false);
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

    protected void showOrHideVideo(boolean showVideo) {
        LogHelper.logIfDebug(TAG, "Show camera preview:" + showVideo);
        if (conversation != null && conversation.getLocalMedia() != null &&
                conversation.getLocalMedia().getLocalVideoTracks() != null) {
            final LocalVideoTrack track = conversation.getLocalMedia().getLocalVideoTracks().get(0);
            track.enable(showVideo);
            showOrHideSmallPreview(showVideo);
        } else if (isCalling()) {
            showOrHideSmallPreview(showVideo);
            participantImageView.setVisibility(View.VISIBLE);
        } else {
            showOrHideParticipantView(showVideo);
        }
    }

    protected void switchToFullScreenMode(boolean fullscreen) {
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

    private boolean isAudioTrack(@Nonnull MediaTrack mediaTrack, @Nonnull List<AudioTrack> audioTracks) {
        for (AudioTrack track : audioTracks) {
            if (track.getTrackId().equals(mediaTrack.getTrackId())) {
                return true;
            }
        }

        return false;
    }

    private void setupAudioVideo() {
        cameraCapturer = CameraCapturer.create(this,
                isFrontCameraAvailable() ?
                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA :
                        CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA,
                e -> showError(getString(R.string.video_calls_camera_issue)));

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
            showError(e.toString());
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

    protected void showOrHideSmallPreview(boolean show) {
        smallPreviewCoverView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showOrHideParticipantView(boolean show) {
        participantImageView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void closeConversation() {
        LogHelper.logIfDebug(TAG, "closeConversation()");

        mTwilio.setDuringCall(false);
        mTwilio.clearCurrentInvite();

        smallPreviewWindow.removeAllViews();
        smallPreviewCoverView.setVisibility(View.VISIBLE);
        participantWindow.removeAllViews();
        participantImageView.setVisibility(View.VISIBLE);

        if (localVideoRenderer != null) {
            localVideoRenderer.release();
            localVideoRenderer = null;
        }

        if (participantVideoRenderer != null) {
            participantVideoRenderer.release();
            participantVideoRenderer = null;
        }

        cameraCapturer.stopPreview();

        if (conversation != null) {
            conversation.disconnect();
            conversation = null;
        }
    }

    protected abstract boolean isCalling();

    protected abstract boolean isCaller();

    protected boolean shouldShowVideo() {
        return !hideVideoButton.isChecked();
    }

    @Override
    protected void onDestroy() {
        closeConversation();
        super.onDestroy();
    }
}


