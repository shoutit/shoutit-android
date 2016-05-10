package com.shoutit.app.android.view.chats.public_chat;

import android.content.Intent;
import android.net.Uri;

import com.google.common.base.Optional;
import com.shoutit.app.android.utils.ImageCaptureHelper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreatePublicChatPresenterTest {

    @Mock
    CreatePublicChatPresenter.CreatePublicChatView listener;
    @Mock
    Intent intent;
    @Mock
    Uri uri;
    @Mock
    ImageCaptureHelper imageCaptureHelper;

    private CreatePublicChatPresenter mCreatePublicChatPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mCreatePublicChatPresenter = new CreatePublicChatPresenter(imageCaptureHelper);
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
        mCreatePublicChatPresenter.onImageActivityFinished(0, intent);

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
        mCreatePublicChatPresenter.onImageActivityFinished(0, intent);
        mCreatePublicChatPresenter.selectImageClicked();

        //then
        verify(listener).setImage(null);
    }

}