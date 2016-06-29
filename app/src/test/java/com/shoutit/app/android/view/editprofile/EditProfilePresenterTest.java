package com.shoutit.app.android.view.editprofile;

import android.graphics.Bitmap;
import android.net.Uri;

import com.appunite.rx.ResponseOrError;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UpdateUserRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.FileHelper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EditProfilePresenterTest {

    @Mock
    UserPreferences userPreferences;
    @Mock
    ApiService apiService;
    @Mock
    FileHelper fileHelper;
    @Mock
    AmazonHelper amazonHelper;

    private EditProfilePresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(userPreferences.getPageOrUserObservable()).thenReturn(Observable.just(getUser()));

        when(apiService.updateUser(any(UpdateUserRequest.class)))
                .thenReturn(Observable.just(getUser()));

        when(fileHelper.createTempFileAndStoreUri(any(Uri.class)))
                .thenReturn("file");
        when(fileHelper.saveBitmapToTempFileObservable(any(Bitmap.class)))
                .thenReturn(Observable.just(ResponseOrError.fromData(new File("path"))));

        when(amazonHelper.uploadUserImageObservable(any(File.class)))
                .thenReturn(Observable.just("fileUrl"));

        presenter = new EditProfilePresenter(userPreferences, apiService,
                fileHelper, amazonHelper, Schedulers.immediate(), Schedulers.immediate(), null);
    }

    // TODO write some tests
    @Test
    public void testName() throws Exception {
        TestSubscriber subscriber = new TestSubscriber();

        presenter.getFirstNameObserver().onNext("name");
        presenter.getBioObserver().onNext("bio");



    }

    private User getUser() {
        return new User("z", null, null, null, null, null, null, null, false, null,
                null, false, false, false, null, 1, null, 1, null, false, null, null, null, null, null, null, null, null, null);
    }
}
