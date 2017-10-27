package com.shoutit.app.android.view.chats.public_chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.common.base.Optional;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.CreatePublicChatRequest;
import com.shoutit.app.android.api.model.UpdateLocationRequest;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.view.conversations.RefreshConversationBus;
import com.shoutit.app.android.view.location.LocationHelper;
import com.shoutit.app.android.view.media.MediaUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocationHelper.class, ResourcesHelper.class, MediaUtils.class})
public class CreatePublicChatPresenterTest {

    @Mock
    CreatePublicChatPresenter.CreatePublicChatView listener;
    @Mock
    ApiService mApiService;
    @Mock
    Intent intent;
    @Mock
    Uri uri;
    @Mock
    Context mContext;
    @Mock
    ImageCaptureHelper imageCaptureHelper;
    @Mock
    AmazonHelper mAmazonHelper;
    @Mock
    UserPreferences mUserPreferences;
    @Mock
    RefreshConversationBus mRefreshConversationBus;

    private CreatePublicChatPresenter mCreatePublicChatPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(LocationHelper.class);
        PowerMockito.mockStatic(ResourcesHelper.class);
        PowerMockito.mockStatic(MediaUtils.class);
        when(MediaUtils.createFileFromUri(any(Context.class), any(Uri.class), anyInt())).thenReturn(new File(""));
        when(mAmazonHelper.uploadGroupChatObservable(any(File.class))).thenReturn(Observable.just("url"));
        mCreatePublicChatPresenter = new CreatePublicChatPresenter(imageCaptureHelper, mContext, mApiService, Schedulers.immediate(), Schedulers.immediate(), mAmazonHelper, mUserPreferences, mRefreshConversationBus);
    }

    @Test
    public void whenImageEmptyAndClicked_openSelectImageActivity() {
        //given
        mCreatePublicChatPresenter.register(listener);

        //when
        mCreatePublicChatPresenter.selectImageClicked();

        //then
        verify(listener).startSelectImageActivity();
    }


    @Test
    public void whenImageEmptyAndClickedAndImagePassed_imageIsSet() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(imageCaptureHelper.onResult(anyInt(), any(Intent.class))).thenReturn(Optional.of(uri));

        //when
        mCreatePublicChatPresenter.selectImageClicked();
        mCreatePublicChatPresenter.onImageActivityFinished(CreatePublicChatPresenter.RESULT_OK, intent);

        //then
        verify(listener).setImage(uri);
    }

    @Test
    public void whenImageNotEmptyAndClicked_clearImage() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(imageCaptureHelper.onResult(anyInt(), any(Intent.class))).thenReturn(Optional.of(uri));

        //when
        mCreatePublicChatPresenter.selectImageClicked();
        mCreatePublicChatPresenter.onImageActivityFinished(CreatePublicChatPresenter.RESULT_OK, intent);
        mCreatePublicChatPresenter.selectImageClicked();

        //then
        verify(listener).setImage(null);
    }

    @Test
    public void whenLocationClicked_openSelectLocationActivity() {
        //given
        mCreatePublicChatPresenter.register(listener);

        //when
        mCreatePublicChatPresenter.selectLocationClicked();

        //then
        verify(listener).startSelectLocationActivity();
    }

    @Test
    public void whenLocationSelected_locationChanged() {
        //given
        when(LocationHelper.getLocationFromIntent(any(Intent.class))).thenReturn(new UserLocation(0, 0, "", "", "", "", ""));
        when(ResourcesHelper.getCountryResId(any(Context.class), any(UserLocation.class))).thenReturn(Optional.of(1));
        mCreatePublicChatPresenter.register(listener);

        //when
        mCreatePublicChatPresenter.selectLocationClicked();
        mCreatePublicChatPresenter.onLocationActivityFinished(CreatePublicChatPresenter.RESULT_OK, intent);

        //then
        verify(listener).setLocation(anyInt(), anyString());
    }

    @Test
    public void whenConfirmClickedAndSubjectEmpty_showError() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(listener.getData()).thenReturn(new CreatePublicChatPresenter.CreatePublicChatData(null, false, false));

        //when
        mCreatePublicChatPresenter.createClicked();

        //then
        verify(listener).subjectEmptyError();
    }

    @Test
    public void whenConfirmClickedAndDataCorrect_showProgress() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(listener.getData()).thenReturn(new CreatePublicChatPresenter.CreatePublicChatData("subject", false, false));

        when(mApiService.createPublicChat(any(CreatePublicChatRequest.class))).thenReturn(Observable.just(ResponseBody.create(MediaType.parse("*/*"), "")));
        when(mApiService.updateUserLocation(any(UpdateLocationRequest.class))).thenReturn(Observable.<BaseProfile>just(null));

        //when
        mCreatePublicChatPresenter.createClicked();

        //then
        verify(listener).showProgress(true);
    }

    @Test
    public void whenConfirmClickedAndDataCorrectAndCreatingSuccesful_finishActivity() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(listener.getData()).thenReturn(new CreatePublicChatPresenter.CreatePublicChatData("subject", false, false));

        when(mApiService.createPublicChat(any(CreatePublicChatRequest.class))).thenReturn(Observable.just(ResponseBody.create(MediaType.parse("*/*"), "")));
        when(mApiService.updateUserLocation(any(UpdateLocationRequest.class))).thenReturn(Observable.<BaseProfile>just(null));

        //when
        mCreatePublicChatPresenter.createClicked();

        //then
        verify(listener).finish();
    }

    @Test
    public void whenConfirmClickedAndDataCorrectAndCreatingFailed_showErrror() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(listener.getData()).thenReturn(new CreatePublicChatPresenter.CreatePublicChatData("subject", false, false));

        when(mApiService.updateUserLocation(any(UpdateLocationRequest.class))).thenReturn(Observable.<BaseProfile>just(null));

        when(mApiService.createPublicChat(any(CreatePublicChatRequest.class))).thenReturn(Observable.<ResponseBody>error(new RuntimeException("")));

        //when
        mCreatePublicChatPresenter.createClicked();

        //then
        verify(listener).createRequestError();
        verify(listener).showProgress(false);
    }

    @Test
    public void whenImageAddedAndConfirmClicked_imageIsUploaded() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(imageCaptureHelper.onResult(anyInt(), any(Intent.class))).thenReturn(Optional.of(uri));
        when(listener.getData()).thenReturn(new CreatePublicChatPresenter.CreatePublicChatData("subject", false, false));

        when(mApiService.createPublicChat(any(CreatePublicChatRequest.class))).thenReturn(Observable.just(ResponseBody.create(MediaType.parse("*/*"), "")));
        when(mApiService.updateUserLocation(any(UpdateLocationRequest.class))).thenReturn(Observable.<BaseProfile>just(null));

        //when
        mCreatePublicChatPresenter.selectImageClicked();
        mCreatePublicChatPresenter.onImageActivityFinished(CreatePublicChatPresenter.RESULT_OK, intent);

        mCreatePublicChatPresenter.createClicked();

        //then
        verify(mAmazonHelper).uploadGroupChatObservable(any(File.class));
    }

    @Test
    public void whenImageNotAddedAndConfirmClicked_imageIsNotUploaded() {
        //given
        mCreatePublicChatPresenter.register(listener);
        when(imageCaptureHelper.onResult(anyInt(), any(Intent.class))).thenReturn(Optional.of(uri));
        when(listener.getData()).thenReturn(new CreatePublicChatPresenter.CreatePublicChatData("subject", false, false));

        when(mApiService.createPublicChat(any(CreatePublicChatRequest.class))).thenReturn(Observable.just(ResponseBody.create(MediaType.parse("*/*"), "")));
        when(mApiService.updateUserLocation(any(UpdateLocationRequest.class))).thenReturn(Observable.<BaseProfile>just(null));

        //when
        mCreatePublicChatPresenter.createClicked();

        //then
        verify(mAmazonHelper, times(0)).uploadGroupChatObservable(any(File.class));
    }
}