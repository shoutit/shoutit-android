package com.shoutit.app.android.view.videoconversation;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
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
import com.shoutit.app.android.utils.ColoredSnackBar;
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
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class VideoConversationActivity extends BaseActivity {

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
    @Bind(R.id.video_conversation_info)
    TextView conversationInfo;

    @Inject
    VideoConversationPresenter presenter;
    @Inject
    UserPreferences preferences;

    private String username;
    private ConversationsClient conversationClient;
    private CameraManager cameraManager;



    private BehaviorSubject<String> conversationInfoSubject = BehaviorSubject.create();
    private BehaviorSubject<String> conversationErrorSubject = BehaviorSubject.create();

    public static Intent newIntent(@Nullable String username, @Nonnull Context context) {
        return new Intent(context, VideoConversationActivity.class).putExtra(ARGS_USERNAME, username);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conversation);
        ButterKnife.bind(this);

        conversationInfo.bringToFront();
        setupVariablesFromApp();
        setupAudioVideo();

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
                        closeConversation();
                        finish();
                    }
                });

        /** Conversation Info **/
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
                        ColoredSnackBar.colorSnackBar(videoCallView, error, Snackbar.LENGTH_LONG,R.color.snackbar_error).show();
                    }
                });
    }

    private CapturerErrorListener capturerErrorListener() {
        return new CapturerErrorListener() {
            @Override
            public void onError(CapturerException e) {
                conversationErrorSubject.onNext(getString(R.string.video_calls_camera_issue));

                if(cameraCapturer.isPreviewing()){
                    cameraCapturer.stopPreview();
                }
                cameraCapturer.startPreview();
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
        if (invite != null) {
            invite.accept(setupLocalMedia(), new ConversationCallback() {
                @Override
                public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                    if (exception == null) {
                        VideoConversationActivity.this.conversation = conversation;
                        conversation.setConversationListener(conversationListener());
                    } else {
                        conversationErrorSubject.onNext(exception.getMessage().substring(18, exception.getMessage().length()));
                    }
                }
            });
        }
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
                                if(exception.getErrorCode() == 109){
                                    conversationInfoSubject.onNext(getString(R.string.video_calls_participant_reject));
                                }else {
                                    conversationErrorSubject.onNext(exception.getMessage().substring(18, exception.getMessage().length()));
                                }
                            }
                        }
                    });
        }
    }

    private LocalMediaListener localMediaListener() {
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                cameraCapturer.stopPreview();
                localVideoRenderer = new VideoViewRenderer(VideoConversationActivity.this, localWindow);
                localVideoTrack.addRenderer(localVideoRenderer);
                conversationInfoSubject.onNext(String.format(getString(R.string.video_calls_connecting), username));

            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                localWindow.removeAllViews();
                if (conversation != null) {
                    conversation.disconnect();
                }
                cameraCapturer.stopPreview();

            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {
            }
        };
    }

    private ConversationListener conversationListener() {
        return new ConversationListener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                participant.setParticipantListener(participantListener());
                if(username != null) {
                    conversationInfoSubject.onNext(String.format(getString(R.string.video_calls_connected), username));
                }else{
                    conversationInfoSubject.onNext(String.format(getString(R.string.video_calls_connected), invite.getParticipants().toString()));
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
        }else{
            cameraCapturer = CameraCapturerFactory.createCameraCapturer(VideoConversationActivity.this,
                    CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA, localVideoPreview, capturerErrorListener());
        }
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        cameraCapturer.startPreview();
        conversationClient.setAudioOutput(AudioOutput.SPEAKERPHONE);
    }

    private void setupVariablesFromApp() {
        final Intent intent = getIntent();
        username = intent.getStringExtra(ARGS_USERNAME);
        conversationClient = ((App) getApplication()).getConversationsClient();
        invite = ((App) getApplication()).getInvite();
        cameraManager = (CameraManager) getApplicationContext().getSystemService(CAMERA_SERVICE);
    }

    private boolean isFrontCameraAvailable(){
        try {
            for (final String cameraID : cameraManager.getCameraIdList()){
                final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
                final int cameraOrientation = checkNotNull(characteristics.get(CameraCharacteristics.LENS_FACING));
                if(cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) return true;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
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
    }

    private void closeConversation() {
        if (conversation != null) {
            conversation.disconnect();
        }
        cameraCapturer.stopPreview();
        ((App) getApplication()).getConversationsClient().listen();
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


