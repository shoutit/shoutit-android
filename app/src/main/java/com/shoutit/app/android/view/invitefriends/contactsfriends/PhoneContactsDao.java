package com.shoutit.app.android.view.invitefriends.contactsfriends;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.subjects.CacheSubject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

@Singleton
public class PhoneContactsDao {

    @Nonnull
    private final Observable<ResponseOrError<List<PhoneContact>>> contactsObservable;
    @Nonnull
    private final PublishSubject<InvitationsRequest> sendInvitationsSubject = PublishSubject.create();
    @Nonnull
    private final LoadingCache<String, PhoneContactDao> cache;
    @Nonnull
    private final PhoneContactsDatabase phoneContactsDatabase;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final PermissionTest permissionTest;


    @Inject
    public PhoneContactsDao(@Nonnull final PhoneContactsDatabase phoneContactsDatabase,
                            @Nonnull final KingschatService kingschatService,
                            @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                            @Nonnull final CacheProvider cacheProvider,
                            @Nonnull final PermissionTest permissionTest) {
        this.phoneContactsDatabase = phoneContactsDatabase;
        this.networkScheduler = networkScheduler;
        this.permissionTest = permissionTest;

        final Observable<Object> contactPermissionGranted = permissionTest.permissionGrantResultObservable()
                .filter(new Func1<List<PermissionTest.PermissionGrantResult>, Boolean>() {
                    @Override
                    public Boolean call(List<PermissionTest.PermissionGrantResult> permissionGrantResults) {
                        for (PermissionTest.PermissionGrantResult grantResult : permissionGrantResults) {
                            if (grantResult.permission().equals(Permission.READ_CONTACTS)
                                    && grantResult.status().equals(PermissionTest.PermissionGrantStatus.GRANTED)) {
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .map(Functions1.toObject());

        final Observable<Object> fetchContactsAgain = Observable.merge(
                contactPermissionGranted,
                phoneContactsDatabase.phoneContactsChangedObservable())
                .throttleLast(5, TimeUnit.SECONDS, networkScheduler);
        contactsObservable = Observable
                .create(RxOperators.fromAction(new Func0<List<PhoneContact>>() {
                    @Override
                    public List<PhoneContact> call() {
                        final boolean hasPermission = permissionTest.checkHasPermission(Permission.READ_CONTACTS);
                        if (hasPermission) {
                            return phoneContactsDatabase.getAllPhoneContacts();
                        } else {
                            throw new RuntimeException("Do not have permissions");
                        }
                    }
                }))
                .compose(CacheSubject.behaviorRefCount(cacheProvider.<List<PhoneContact>>getCacheCreatorForKey("phone_contacts",
                        new TypeToken<List<PhoneContact>>() {
                        }.getType())))
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<List<PhoneContact>>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<List<PhoneContact>>>refresh(fetchContactsAgain))
                .compose(MoreOperators.<ResponseOrError<List<PhoneContact>>>cacheWithTimeout(networkScheduler))
                .distinctUntilChanged();

        sendInvitationsSubject
                .flatMap(new Func1<InvitationsRequest, Observable<?>>() {
                    @Override
                    public Observable<?> call(InvitationsRequest invitationsRequest) {
                        return kingschatService.sendPhoneContactInvitations(invitationsRequest)
                                .map(Functions1.toObject())
                                .compose(ResponseOrError.toResponseOrErrorObservable())
                                .compose(MoreOperators.repeatOnError(networkScheduler))
                                .subscribeOn(networkScheduler)
                                .unsubscribeOn(networkScheduler);
                    }
                })
                .subscribe();


        cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, PhoneContactDao>() {
                    @Override
                    public PhoneContactDao load(@Nonnull final String rawContactId) throws Exception {
                        return new PhoneContactDao(rawContactId);
                    }
                });
    }

    public class PhoneContactDao {
        @Nonnull
        private final String rawContactId;

        private PhoneContactDao(@Nonnull String rawContactId) {
            this.rawContactId = rawContactId;
        }


        @Nonnull
        private Observable<List<PhoneContact>> getContactsByContactId() {
            return contactsObservable
                    .map(new Func1<ResponseOrError<List<PhoneContact>>, List<PhoneContact>>() {
                        @Override
                        public List<PhoneContact> call(ResponseOrError<List<PhoneContact>> immutableListResponseOrError) {
                            return immutableListResponseOrError.isData() ? immutableListResponseOrError.data() : ImmutableList.<PhoneContact>of();
                        }
                    })
                    .flatMap(new Func1<List<PhoneContact>, Observable<List<PhoneContact>>>() {
                        @Override
                        public Observable<List<PhoneContact>> call(List<PhoneContact> phoneContacts) {
                            return Observable.from(phoneContacts)
                                    .filter(new Func1<PhoneContact, Boolean>() {
                                        @Override
                                        public Boolean call(PhoneContact phoneContact) {
                                            return Objects.equal(phoneContact.userId(), rawContactId);
                                        }
                                    })
                                    .toList();
                        }
                    });
        }

        @Nonnull
        public Observable<List<PhoneNumber>> getPhoneNumbersByContactId() {
            return getContactsByContactId().flatMap(new Func1<List<PhoneContact>, Observable<List<PhoneNumber>>>() {
                @Override
                public Observable<List<PhoneNumber>> call(List<PhoneContact> phoneContacts) {
                    return Observable.from(phoneContacts)
                            .filter(new Func1<PhoneContact, Boolean>() {
                                @Override
                                public Boolean call(PhoneContact phoneContact) {
                                    return phoneContact.phone() != null;
                                }
                            })
                            .map(new Func1<PhoneContact, PhoneNumber>() {
                                @Override
                                public PhoneNumber call(PhoneContact phoneContact) {
                                    return new PhoneNumber(phoneContact.phone());
                                }
                            })
                            .toList();
                }
            });
        }

    }

    @Nonnull
    public Observable<ResponseOrError<List<PhoneContact>>> contactsObservable() {
        return contactsObservable;
    }

    @Nonnull
    public Observable<ResponseOrError<PhoneContactToShare>> phoneContactForUri(@Nonnull String uri) {
        return Observable.just(uri)
                .flatMap(new Func1<String, Observable<ResponseOrError<PhoneContactToShare>>>() {
                    @Override
                    public Observable<ResponseOrError<PhoneContactToShare>> call(final String contactUri) {
                        return Observable
                                .create(new Observable.OnSubscribe<PhoneContactToShare>() {
                                    @Override
                                    public void call(Subscriber<? super PhoneContactToShare> subscriber) {
                                        final boolean hasPermission = permissionTest.checkHasPermission(Permission.READ_CONTACTS);
                                        if (hasPermission) {
                                            final PhoneContactToShare phoneContactToShare = phoneContactsDatabase.getPhoneContactForUri(contactUri);
                                            if (phoneContactToShare != null) {
                                                subscriber.onNext(phoneContactsDatabase.getPhoneContactForUri(contactUri));
                                                subscriber.onCompleted();
                                            } else {
                                                subscriber.onError(new IOException("Could not get contact!"));
                                            }
                                        } else {
                                            subscriber.onError(new IOException("You do not have permission!"));
                                        }
                                    }
                                })
                                .compose(ResponseOrError.<PhoneContactToShare>toResponseOrErrorObservable())
                                .subscribeOn(networkScheduler);
                    }
                });
    }

    @Nonnull
    public PhoneContactDao contactDao(@Nonnull String rawContactId) {
        return cache.getUnchecked(rawContactId);
    }

    @Nonnull
    public Observer<InvitationsRequest> sendInvitationsObserver() {
        return sendInvitationsSubject;
    }

    public static class PhoneContactToShare {
        @Nonnull
        private final String name;
        @Nonnull
        private final String phone;

        public PhoneContactToShare(@Nonnull String name, @Nonnull String phone) {
            this.name = name;
            this.phone = phone;
        }

        @Nonnull
        public String name() {
            return name;
        }

        @Nonnull
        public String phone() {
            return phone;
        }

    }

    public interface PhoneContactsDatabase {

        @Nonnull
        Observable<Object> phoneContactsChangedObservable();

        @Nonnull
        List<PhoneContact> getAllPhoneContacts();

        @Nullable
        PhoneContactToShare getPhoneContactForUri(@Nonnull final String contactUri);
    }

}