package com.shoutit.app.android.utils;

import android.graphics.Color;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.verify;

public class SpanUtilsTest extends InstrumentationTestCase {

    @Mock
    SpanUtils.OnClickListener clickListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @SmallTest
    public void testHasClickSpan() {
        //given
        final SpannableString spannableString = getTestSpannableString();

        //when
        final Object[] spans = spannableString.getSpans(0, spannableString.length(), ClickableSpan.class);

        //then
        assert_().that(spans).hasLength(1);
        ((ClickableSpan) spans[0]).onClick(null);
        verify(clickListener).onClick();
    }

    @SmallTest
    public void testHasColorSpan() {
        //given
        final SpannableString spannableString = getTestSpannableString();

        //when
        final Object[] spans = spannableString.getSpans(0, spannableString.length(), ForegroundColorSpan.class);

        //then
        assert_().that(spans).hasLength(1);
    }

    private SpannableString getTestSpannableString() {
        return SpanUtils.clickableColoredSpan("All text and clickable part", "clickable part", Color.RED, clickListener);
    }

}