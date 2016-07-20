package com.shoutit.app.android.view.invitefriends.contactsinvite;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.Contact;
import com.shoutit.app.android.view.invitefriends.contactsfriends.PhoneContactsHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class InviteContactsPresenter {

    @Nonnull
    private final PublishSubject<Object> fetchLocalContactsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Contact> contactSelectedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> contactsObservable;
    private final Observable<Boolean> progressObservable;

    @Inject
    public InviteContactsPresenter(PhoneContactsHelper contactsHelper) {

        contactsObservable = fetchLocalContactsSubject
                .switchMap(initFetch -> Observable.just(contactsHelper.getAllPhoneContactsSorted()))
                .map(new Func1<List<Contact>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Contact> contacts) {
                        return ImmutableList.copyOf(
                                Lists.transform(contacts, (Function<Contact, BaseAdapterItem>) input ->
                                        new ContactAdapterItem(input, contactSelectedSubject))
                        );
                    }
                });

        progressObservable = contactsObservable.map(Functions1.returnFalse())
                .startWith(true);
    }

    public void fetchContacts() {
        fetchLocalContactsSubject.onNext(null);
    }

    @Nonnull
    public Observable<Contact> getContactSelectedObservable() {
        return contactSelectedSubject;
    }

    public Observable<List<BaseAdapterItem>> getContactsObservable() {
        return contactsObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public static class ContactAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Contact contact;
        @Nonnull
        private final Observer<Contact> onContactSelectedObserver;

        public ContactAdapterItem(@Nonnull Contact contact,
                                  @Nonnull Observer<Contact> onContactSelectedObserver) {
            this.contact = contact;
            this.onContactSelectedObserver = onContactSelectedObserver;
        }

        @Nonnull
        public Contact getContact() {
            return contact;
        }

        public void onContactSelected() {
            onContactSelectedObserver.onNext(contact);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof ContactAdapterItem &&
                    ((ContactAdapterItem) baseAdapterItem).contact.equals(contact);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return ((ContactAdapterItem) baseAdapterItem).contact.equals(contact);
        }
    }
}
