package com.shoutit.app.android.utils;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;


public class AmazonRequestTransfomerTest {

    private AmazonRequestTransfomer mAmazonRequestTransfomer;

    @Before
    public void setUp() {
        mAmazonRequestTransfomer = new AmazonRequestTransfomer();
    }

    @Test
    public void testFormatingUrl() {
        final String transformUrl = mAmazonRequestTransfomer.transformUrl("https://user-image.static.shoutit.com/image_filename.jpg", "image_filename.jpg" ,"small");
        assert_().that(transformUrl).isEqualTo("https://user-image.static.shoutit.com/image_filename_small.jpg");
    }

}