package com.shoutit.app.android.view.videoconversation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.shoutit.app.android.utils.VersionUtils;
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
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class VideoConversationActivity extends BaseActivity {

    private static final String ARGS_USERNAME = "args_username";
    private static final String ARGS_CALLER = "args_caller";
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;


    private VideoViewRenderer participantVideoRenderer;
    private VideoViewRenderer localVideoRenderer;
    private ConversationsClient conversationClient;
    private CameraCapturer cameraCapturer;
    private CameraManager cameraManager;
    private Conversation conversation;

    private OutgoingInvite outgoingInvite;
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
    @Bind(R.id.video_conversation_info)
    TextView conversationInfo;

    @Inject
    UserPreferences preferences;
    @Inject
    Twilio mTwilio;

    private String shoutOnwerId;
    private String caller;

    private BehaviorSubject<String> conversationInfoSubject = BehaviorSubject.create();
    private BehaviorSubject<String> conversationErrorSubject = BehaviorSubject.create();

    public static Intent newIntent(@Nullable String callerName, @Nullable String id, @Nonnull Context context) {
        return new Intent(context, VideoConversationActivity.class).putExtra(ARGS_USERNAME, id).putExtra(ARGS_CALLER, callerName);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conversation);
        ButterKnife.bind(this);

        if (hasVideoCallPermissions()) {
            initializeVideoConversations();
        }
    }

    private void initializeVideoConversations() {

        conversationInfo.bringToFront();
        setupVariablesFromApp();
        setupAudioVideo();

        if (shoutOnwerId == null) {
            acceptIncomingCall();
        }

        RxView.clicks(callButton)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        makeOutgoingCall();
                    }
                });

        RxView.clicks(dismissCallButton)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        closeConversation();
                        finish();
                    }
                });

        Observable<String> conversationInfoObservable = conversationInfoSubject
                .filter(Functions1.isNotNull());

        Observable<String> conversationErrorObservable = conversationErrorSubject
                .filter(Functions1.isNotNull());

        conversationInfoObservable
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(conversationInfo));

        conversationErrorObservable
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String error) {
                        ColoredSnackBar.error(videoCallView, error, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private CapturerErrorListener capturerErrorListener() {
        return new CapturerErrorListener() {
            @Override
            public void onError(CapturerException e) {
                conversationErrorSubject.onNext(getString(R.string.video_calls_camera_issue));
                cameraCapturer.startPreview();
            }
        };
    }

    private LocalMedia setupLocalMedia() {
        final LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());
        final LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
        localMedia.addLocalVideoTrack(localVideoTrack);
        return localMedia;
    }

    private void acceptIncomingCall() {

        if (invite != null) {

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

    private void makeOutgoingCall() {

        if (shoutOnwerId != null && conversationClient != null) {
            callButton.setVisibility(View.GONE);

            Set<String> participants = new HashSet<>();
            participants.add(shoutOnwerId);

            outgoingInvite = conversationClient
                    .sendConversationInvite(participants, setupLocalMedia(), new ConversationCallback() {
                        @Override
                        public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                            if (exception == null) {
                                VideoConversationActivity.this.conversation = conversation;
                                conversation.setConversationListener(conversationListener());
                            } else if (exception.getErrorCode() == 109) {
                                conversationInfoSubject.onNext(getString(R.string.video_calls_participant_reject));
                            } else {
                                conversationErrorSubject.onNext(TextHelper.formatErrorMessage(exception.getMessage()));
                            }
                        }
                    });
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.video_calls_cannot_start_conversation, Snackbar.LENGTH_SHORT).show();
        }
    }

    private LocalMediaListener localMediaListener() {
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                conversationInfoSubject.onNext(getString(R.string.video_calls_connecting));
                localVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, localWindow);
                localVideoTrack.addRenderer(localVideoRenderer);
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                localWindow.removeAllViews();
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
                participant.setParticipantListener(participantListener());
                if (shoutOnwerId != null) {
                    conversationInfoSubject.onNext(String.format(getString(R.string.video_calls_connected), preferences.getShoutOwnerName()));
                } else {
                    conversationInfoSubject.onNext(String.format(getString(R.string.video_calls_connected), caller));
                }
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
                if (conversation != null) {
                    conversation.disconnect();
                }
            }
        };
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
                    CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, localVideoPreview, capturerErrorListener());
        } else {
            cameraCapturer = CameraCapturerFactory.createCameraCapturer(VideoConversationActivity.this,
                    CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA, localVideoPreview, capturerErrorListener());
        }
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        cameraCapturer.startPreview();
    }

    private void setupVariablesFromApp() {
        final Intent intent = getIntent();
        shoutOnwerId = intent.getStringExtra(ARGS_USERNAME);
        caller = intent.getStringExtra(ARGS_CALLER);
        conversationClient = mTwilio.getConversationsClient();
        invite = mTwilio.getInvite();
        cameraManager = (CameraManager) getApplicationContext().getSystemService(CAMERA_SERVICE);
    }

    @SuppressWarnings("deprecation")
    private boolean isFrontCameraAvailableBelowLollipop() {
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            final Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (Camera.CameraInfo.CAMERA_FACING_FRONT == info.facing) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isFrontCameraAvailableAboveLollipop() {
        try {
            for (final String cameraID : cameraManager.getCameraIdList()) {
                final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
                final int cameraOrientation = checkNotNull(characteristics.get(CameraCharacteristics.LENS_FACING));
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) return true;
            }
        } catch (CameraAccessException e) {
            conversationErrorSubject.onNext(e.toString());
        }
        return false;
    }

    private boolean isFrontCameraAvailable() {
        if (VersionUtils.isAtLeastL()) {
            return isFrontCameraAvailableAboveLollipop();
        } else {
            return isFrontCameraAvailableBelowLollipop();
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
        if (TwilioConversations.isInitialized() && conversationClient != null) {
            conversationClient.listen();
        }
        if (cameraCapturer != null) {
            cameraCapturer.startPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (TwilioConversations.isInitialized() && conversationClient != null && conversation == null) {
            conversationClient.unlisten();
        }

        if (cameraCapturer != null) {
            cameraCapturer.stopPreview();
        }
    }

    private void closeConversation() {

        localWindow.removeAllViews();
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


