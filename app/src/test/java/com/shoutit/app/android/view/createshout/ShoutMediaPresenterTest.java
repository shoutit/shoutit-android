package com.shoutit.app.android.view.createshout;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ShoutMediaPresenterTest {

    public abstract class StubMediaListener implements ShoutMediaPresenter.MediaListener {

    }

    private ShoutMediaPresenter mShoutMediaPresenter = new ShoutMediaPresenter();

    @Mock
    StubMediaListener mMediaListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenFirstShown_allItemsAreNotSet() {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        mShoutMediaPresenter.register(mMediaListener);

        verify(mMediaListener).setImages(argumentCaptor.capture());
        final Map<Integer, ShoutMediaPresenter.Item> value = argumentCaptor.getValue();

        for (ShoutMediaPresenter.Item image : value.values()) {
            assert_().that(image).isInstanceOf(ShoutMediaPresenter.AddImageItem.class);
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

        mShoutMediaPresenter.addMediaItem("test");

        verify(mMediaListener, times(2)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.Item target = (ShoutMediaPresenter.Item) argumentCaptor.getValue().get(0);
        assert_().that(target).isInstanceOf(ShoutMediaPresenter.ImageItem.class);
        assert_().that(((ShoutMediaPresenter.ImageItem) target).getMedia()).isEqualTo("test");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenMediaClicked_mediaWasDeleted() {
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test");

        verify(mMediaListener, times(2)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.ImageItem target = (ShoutMediaPresenter.ImageItem) argumentCaptor.getValue().get(0);
        target.click();

        verify(mMediaListener, times(3)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.Item removedItem = (ShoutMediaPresenter.Item) argumentCaptor.getValue().get(0);
        assert_().that(removedItem).isInstanceOf(ShoutMediaPresenter.AddImageItem.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenTwoMediaAddedAndFirstRemoved_mediaAtFirstPosition(){
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        mShoutMediaPresenter.register(mMediaListener);

        mShoutMediaPresenter.addMediaItem("test");
        mShoutMediaPresenter.addMediaItem("test2");

        verify(mMediaListener, times(3)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.ImageItem target = (ShoutMediaPresenter.ImageItem) argumentCaptor.getValue().get(0);
        target.click();

        verify(mMediaListener, times(4)).setImages(argumentCaptor.capture());
        final ShoutMediaPresenter.Item imageItem = (ShoutMediaPresenter.Item) argumentCaptor.getValue().get(0);
        assert_().that(imageItem).isInstanceOf(ShoutMediaPresenter.ImageItem.class);

    }

}