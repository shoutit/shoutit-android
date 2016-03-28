package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.net.Uri;

import com.google.common.collect.ImmutableList;
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
import java.util.Map;

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

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Uri.class);
        PowerMockito.mockStatic(MediaUtils.class);
        when(Uri.parse(anyString())).thenReturn(null);
        when(MediaUtils.createVideoThumbnail(any(Context.class), any(Uri.class))).thenReturn(new File(""));
        mShoutMediaPresenter = new ShoutMediaPresenter(context);
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
        assert_().that(((ShoutMediaPresenter.MediaItem) target).getThumb()).isEqualTo("test");
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
    public void whenSecondImageAdded_DontDisplayAlert() {
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test", true);
        mShoutMediaPresenter.addMediaItem("test2", false);

        verify(mMediaListener, times(0)).onlyOneVideoAllowedAlert();
    }
}