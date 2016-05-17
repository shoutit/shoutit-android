package com.shoutit.app.android.utils;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.squareup.okhttp.ResponseBody;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Func1;

import static com.google.common.truth.Truth.assert_;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class AmazonHelperTest {

    @Mock
    UserPreferences mUserPreferences;

    @Mock
    TransferUtility mTransferUtility;

    @Mock
    User user;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        when(mUserPreferences.getUser()).thenReturn(user);
    }

    @Test
    public void tessttT(){
        Locale.setDefault(Locale.forLanguageTag("ar"));
        final AmazonHelper amazonHelper = new AmazonHelper(mTransferUtility, mUserPreferences);
        final String fileName = amazonHelper.getFileName("userId", "format");
        assert_().that(fileName).isEqualTo("");
    }

}