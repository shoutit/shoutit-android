package com.shoutit.app.android.view.postlogininterest;

import com.appunite.rx.functions.BothParams;
import com.google.common.collect.Iterables;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import rx.observers.TestSubscriber;

import static com.google.common.truth.Truth.assert_;

public class SelectionHelperTest extends TestCase {

    private SelectionHelper<Integer> mIntegerSelectionHelper;

    @Before
    public void setUp() {
        mIntegerSelectionHelper = new SelectionHelper<>();
    }

    @Test
    public void testAfterSelecting_containsSelected() {
        final TestSubscriber<Set<Integer>> selectedItems = new TestSubscriber<>();
        mIntegerSelectionHelper.getSelectedItems().subscribe(selectedItems);

        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(1, true));

        final Set<Integer> last = Iterables.getLast(selectedItems.getOnNextEvents());
        assert_().that(last).contains(1);
    }

    @Test
    public void testAfterSelectingAndDeselecting_doesntContain() {
        final TestSubscriber<Set<Integer>> selectedItems = new TestSubscriber<>();
        mIntegerSelectionHelper.getSelectedItems().subscribe(selectedItems);

        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(1, true));
        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(1, false));

        final Set<Integer> last = Iterables.getLast(selectedItems.getOnNextEvents());
        assert_().that(last).doesNotContain(1);
    }

    @Test
    public void testAfterSelectingSeveral_containSeveral() {
        final TestSubscriber<Set<Integer>> selectedItems = new TestSubscriber<>();
        mIntegerSelectionHelper.getSelectedItems().subscribe(selectedItems);

        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(1, true));
        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(2, true));
        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(3, true));

        final Set<Integer> last = Iterables.getLast(selectedItems.getOnNextEvents());
        assert_().that(last).hasSize(3);
    }

    @Test
    public void testAfterSelectingSeveralAndClear_isEmpty() {
        final TestSubscriber<Set<Integer>> selectedItems = new TestSubscriber<>();
        mIntegerSelectionHelper.getSelectedItems().subscribe(selectedItems);

        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(1, true));
        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(2, true));
        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(3, true));
        mIntegerSelectionHelper.getItemClearObserver().onNext(new Object());

        final Set<Integer> last = Iterables.getLast(selectedItems.getOnNextEvents());
        assert_().that(last).isEmpty();
    }

    @Test
    public void testAfterSelectingAndDeselecting_selectionObservableNotifies() {
        final TestSubscriber<Boolean> selectedObserver = new TestSubscriber<>();
        mIntegerSelectionHelper.itemSelectionObservable(1).subscribe(selectedObserver);

        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(1, true));
        assert_().that(Iterables.getLast(selectedObserver.getOnNextEvents())).isTrue();

        mIntegerSelectionHelper.getToggleObserver().onNext(BothParams.of(1, false));
        assert_().that(Iterables.getLast(selectedObserver.getOnNextEvents())).isFalse();
    }
}