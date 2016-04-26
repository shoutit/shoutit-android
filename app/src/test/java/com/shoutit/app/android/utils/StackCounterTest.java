package com.shoutit.app.android.utils;

import com.google.common.collect.Iterables;
import com.shoutit.app.android.utils.stackcounter.StackCounter;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static com.google.common.truth.Truth.assert_;

public class StackCounterTest {

    private StackCounter mStackCounter;
    private TestScheduler mScheduler;

    @Before
    public void setUp() {
        mScheduler = new TestScheduler();
        mStackCounter = new StackCounter(mScheduler);
    }

    @Test
    public void testWhenResumed_Foreground() {
        // given
        final TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mStackCounter.getSubject().subscribe(testSubscriber);

        // when
        mStackCounter.onActivityResumed();

        // then
        final List<Boolean> onNextEvents = testSubscriber.getOnNextEvents();
        assert_().that(onNextEvents).hasSize(1);
        final Boolean last = Iterables.getLast(onNextEvents);
        assert_().that(last).isTrue();
    }

    @Test
    public void testWhenResumedTwice_OneForeground() {
        // given
        final TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mStackCounter.getSubject().subscribe(testSubscriber);

        // when
        mStackCounter.onActivityResumed();
        mStackCounter.onActivityResumed();

        // then
        final List<Boolean> onNextEvents = testSubscriber.getOnNextEvents();
        assert_().that(onNextEvents).hasSize(1);
        final Boolean last = Iterables.getLast(onNextEvents);
        assert_().that(last).isTrue();
    }

    @Test
    public void testWhenResumedAndPaused_OneForegroundAndOneBackground() {
        // given
        final TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mStackCounter.getSubject().subscribe(testSubscriber);

        // when
        mStackCounter.onActivityResumed();
        mStackCounter.onActivityPaused();
        mScheduler.advanceTimeBy(StackCounter.DELAY, TimeUnit.SECONDS);

        // then
        final List<Boolean> onNextEvents = testSubscriber.getOnNextEvents();
        assert_().that(onNextEvents).hasSize(2);

        final Boolean last = Iterables.getLast(onNextEvents);
        assert_().that(last).isFalse();

        final Boolean preLast = onNextEvents.get(onNextEvents.size() - 2);
        assert_().that(preLast).isTrue();
    }

    @Test
    public void testWhenTwoResumedAndPaused_OneForegroundAndNoBackground() {
        // given
        final TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mStackCounter.getSubject().subscribe(testSubscriber);

        // when
        mStackCounter.onActivityResumed();
        mStackCounter.onActivityResumed();
        mStackCounter.onActivityPaused();
        mScheduler.advanceTimeBy(StackCounter.DELAY, TimeUnit.SECONDS);

        // then
        final List<Boolean> onNextEvents = testSubscriber.getOnNextEvents();
        assert_().that(onNextEvents).hasSize(1);

        final Boolean last = Iterables.getLast(onNextEvents);
        assert_().that(last).isTrue();
    }


    @Test
    public void testWhenResumedAndPausedAndResumed_OneForegroundAndNoBackground() {
        // given
        final TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mStackCounter.getSubject().subscribe(testSubscriber);

        // when
        mStackCounter.onActivityResumed();
        mStackCounter.onActivityPaused();
        mStackCounter.onActivityResumed();
        mScheduler.advanceTimeBy(StackCounter.DELAY, TimeUnit.SECONDS);

        // then
        final List<Boolean> onNextEvents = testSubscriber.getOnNextEvents();
        assert_().that(onNextEvents).hasSize(1);

        final Boolean last = Iterables.getLast(onNextEvents);
        assert_().that(last).isTrue();
    }

    @Test
    public void testWhenResumedAndPausedAndResumedAfterTime_OneForegroundAndOneBackgroundAndOneForeground() {
        // given
        final TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mStackCounter.getSubject().subscribe(testSubscriber);

        // when
        mStackCounter.onActivityResumed();
        mStackCounter.onActivityPaused();
        mScheduler.advanceTimeBy(StackCounter.DELAY, TimeUnit.SECONDS);
        mStackCounter.onActivityResumed();

        // then
        final List<Boolean> onNextEvents = testSubscriber.getOnNextEvents();
        assert_().that(onNextEvents).hasSize(3);

        final Boolean last = Iterables.getLast(onNextEvents);
        assert_().that(last).isTrue();

        final Boolean preLast = onNextEvents.get(onNextEvents.size() - 2);
        assert_().that(preLast).isFalse();

        final Boolean prePreLast = onNextEvents.get(onNextEvents.size() - 3);
        assert_().that(prePreLast).isTrue();
    }
}