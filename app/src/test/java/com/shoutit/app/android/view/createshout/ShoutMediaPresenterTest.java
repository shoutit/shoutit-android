package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.net.Uri;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.view.media.MediaUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import rx.Observable;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Uri.class, MediaUtils.class})
public class ShoutMediaPresenterTest {

    public abstract class StubMediaListener implements ShoutMediaPresenter.MediaListener {

    }

    private ShoutMediaPresenter mShoutMediaPresenter;

    @Mock
    StubMediaListener mMediaListener;

    @Mock
    Context context;

    @Mock
    AmazonHelper mAmazonHelper;

    @Mock
    Uri uri;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Uri.class);
        PowerMockito.mockStatic(MediaUtils.class);
        when(Uri.parse(anyString())).thenReturn(uri);
        when(uri.getLastPathSegment()).thenReturn("video");
        when(MediaUtils.createVideoThumbnail(any(Context.class), any(Uri.class))).thenReturn(new File(""));
        when(mAmazonHelper.uploadShoutMediaImageObservable(any(File.class))).thenReturn(Observable.just("test"));
        when(mAmazonHelper.uploadShoutMediaVideoObservable(any(File.class))).thenReturn(Observable.just("test"));
        mShoutMediaPresenter = new ShoutMediaPresenter(context, mAmazonHelper);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenFirstShown_firstItemIsAddAndRestIsBlank() {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        mShoutMediaPresenter.register(mMediaListener);

        verify(mMediaListener).setImages(argumentCaptor.capture());
        final Map<Integer, ShoutMediaPresenter.Item> value = argumentCaptor.getValue();

        final ImmutableList<ShoutMediaPresenter.Item> items = ImmutableList.copyOf(value.values());
        assert_().that(items.get(0)).isInstanceOf(ShoutMediaPresenter.AddImageItem.class);

        for (int i = 1; i < items.size(); i++) {
            final ShoutMediaPresenter.Item image = items.get(i);
            assert_().that(image).isInstanceOf(ShoutMediaPresenter.BlankItem.class);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenAddClicked_callStartActivity() {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        mShoutMediaPresenter.register(mMediaListener);

        verify(mMediaListener).setImages(argumentCaptor.capture());
        final Map<Integer, ShoutMediaPresenter.Item> value = argumentCaptor.getValue();

        final ShoutMediaPresenter.Item item = value.get(0);
        item.click();

        verify(mMediaListener).openSelectMediaActivity();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenItemAdded_ListWithMediaReturned() {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", false);

        verify(mMediaListener, times(2)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.Item target = (ShoutMediaPresenter.Item) argumentCaptor.getValue().get(0);
        assert_().that(target).isInstanceOf(ShoutMediaPresenter.MediaItem.class);
        assert_().that(((ShoutMediaPresenter.MediaItem) target).getThumb()).isEqualTo("file://test");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenMediaClicked_mediaWasDeleted() {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", false);

        verify(mMediaListener, times(2)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.MediaItem target = (ShoutMediaPresenter.MediaItem) argumentCaptor.getValue().get(0);
        target.click();

        verify(mMediaListener, times(3)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.Item removedItem = (ShoutMediaPresenter.Item) argumentCaptor.getValue().get(0);
        assert_().that(removedItem).isInstanceOf(ShoutMediaPresenter.AddImageItem.class);

        final ShoutMediaPresenter.Item nextItem = (ShoutMediaPresenter.Item) argumentCaptor.getValue().get(1);
        assert_().that(nextItem).isInstanceOf(ShoutMediaPresenter.BlankItem.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenTwoMediaAddedAndFirstRemoved_mediaAtFirstPosition() {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", false);
        mShoutMediaPresenter.addMediaItem("test2", false);

        verify(mMediaListener, times(3)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.MediaItem target = (ShoutMediaPresenter.MediaItem) argumentCaptor.getValue().get(0);
        target.click();

        verify(mMediaListener, times(4)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.Item imageItem = (ShoutMediaPresenter.Item) argumentCaptor.getValue().get(0);
        assert_().that(imageItem).isInstanceOf(ShoutMediaPresenter.MediaItem.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenSecondVideoAdded_DisplayAlert() {
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", true);
        mShoutMediaPresenter.addMediaItem("test2", true);

        verify(mMediaListener).onlyOneVideoAllowedAlert();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenVideoAddedAndRemoteVideoAlreadySet_DisplayAlert() {
        mShoutMediaPresenter.register(mMediaListener);
        mShoutMediaPresenter.addRemoteMedia(ImmutableList.<String>of(), ImmutableList.of(Video.createVideo("", "", 1)));

        mShoutMediaPresenter.addMediaItem("test", true);

        verify(mMediaListener).onlyOneVideoAllowedAlert();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void whenSecondImageAdded_DontDisplayAlert() {
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", true);
        mShoutMediaPresenter.addMediaItem("test2", false);

        verify(mMediaListener, times(0)).onlyOneVideoAllowedAlert();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSendOnlyVideo_videoReturned() throws Exception {
        ArgumentCaptor<List> imagesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> videoCaptor = ArgumentCaptor.forClass(List.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", true);

        mShoutMediaPresenter.send();

        verify(mMediaListener).mediaEditionCompleted(imagesCaptor.capture(), videoCaptor.capture());
        assert_().that(imagesCaptor.getValue()).isEmpty();
        assert_().that(videoCaptor.getValue()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSendOnlyImages_imagesReturned() throws Exception {
        ArgumentCaptor<List> imagesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> videoCaptor = ArgumentCaptor.forClass(List.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", false);

        mShoutMediaPresenter.send();

        verify(mMediaListener).mediaEditionCompleted(imagesCaptor.capture(), videoCaptor.capture());
        assert_().that(videoCaptor.getValue()).isEmpty();
        assert_().that(imagesCaptor.getValue()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSendVideoAndImages_videoAndImagesReturned() throws Exception {
        ArgumentCaptor<List> imagesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> videoCaptor = ArgumentCaptor.forClass(List.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", false);
        mShoutMediaPresenter.addMediaItem("test", true);

        mShoutMediaPresenter.send();

        verify(mMediaListener).mediaEditionCompleted(imagesCaptor.capture(), videoCaptor.capture());
        assert_().that(videoCaptor.getValue()).hasSize(1);
        assert_().that(imagesCaptor.getValue()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSendVideoAndImagesWithRemoteMedia_videoAndImagesAndRemotesReturned() throws Exception {
        ArgumentCaptor<List> imagesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> videoCaptor = ArgumentCaptor.forClass(List.class);
        mShoutMediaPresenter.register(mMediaListener);
        mShoutMediaPresenter.addRemoteMedia(ImmutableList.of("a"), ImmutableList.of(Video.createVideo("a", "a", 1)));

        mShoutMediaPresenter.addMediaItem("test", false);

        mShoutMediaPresenter.send();

        verify(mMediaListener).mediaEditionCompleted(imagesCaptor.capture(), videoCaptor.capture());
        assert_().that(videoCaptor.getValue()).hasSize(1);
        assert_().that(imagesCaptor.getValue()).hasSize(2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testWhenAllMediaAddedAndFirstRemoved_addAtLastItem() throws Exception {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", false);
        mShoutMediaPresenter.addMediaItem("test", false);
        mShoutMediaPresenter.addMediaItem("test", false);
        mShoutMediaPresenter.addMediaItem("test", false);
        mShoutMediaPresenter.addMediaItem("test", false);

        verify(mMediaListener, times(6)).setImages(argumentCaptor.capture());

        final ShoutMediaPresenter.MediaItem target = (ShoutMediaPresenter.MediaItem) argumentCaptor.getValue().get(0);
        target.click();

        verify(mMediaListener, times(7)).setImages(argumentCaptor.capture());
        assert_().that(argumentCaptor.getValue().get(4)).isInstanceOf(ShoutMediaPresenter.AddImageItem.class);
    }
}