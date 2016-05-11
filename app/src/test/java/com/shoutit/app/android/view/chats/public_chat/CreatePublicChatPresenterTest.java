package com.shoutit.app.android.view.chats.public_chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.common.base.Optional;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.view.createshout.location.LocationResultHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocationResultHelper.class, ResourcesHelper.class})
public class CreatePublicChatPresenterTest {

    @Mock
    CreatePublicChatPresenter.CreatePublicChatView listener;
    @Mock
    Intent intent;
    @Mock
    Uri uri;
    @Mock
    Context mContext;
    @Mock
    ImageCaptureHelper imageCaptureHelper;

    private CreatePublicChatPresenter mCreatePublicChatPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(LocationResultHelper.class);
        PowerMockito.mockStatic(ResourcesHelper.class);
        mCreatePublicChatPresenter = new CreatePublicChatPresenter(imageCaptureHelper, mContext);
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
        when(LocationResultHelper.getLocationFromIntent(any(Intent.class))).thenReturn(new UserLocation(0, 0, "", "", "", "", ""));
        when(ResourcesHelper.getCountryResId(any(Context.class), any(UserLocation.class))).thenReturn(Optional.of(1));
        mCreatePublicChatPresenter.register(listener);

        //when
        mCreatePublicChatPresenter.selectLocationClicked();
        mCreatePublicChatPresenter.onLocationActivityFinished(CreatePublicChatPresenter.RESULT_OK, intent);

        //then
        verify(listener).setLocation(anyInt(), anyString());
    }
}