package com.shoutit.app.android.view.chats;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pusher.client.channel.PresenceChannel;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.AboutShout;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessageAttachment;
import com.shoutit.app.android.api.model.MessagesResponse;
import com.shoutit.app.android.api.model.PostMessage;
import com.shoutit.app.android.api.model.PusherMessage;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.utils.pusher.TypingInfo;
import com.shoutit.app.android.view.chats.message_models.TypingItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class ChatsPresenter {

    private static final int PAGE_SIZE = 20;

    final OperatorMergeNextToken<MessagesResponse, Object> loadMoreOperator =
            OperatorMergeNextToken.create(new Func1<MessagesResponse, Observable<MessagesResponse>>() {

                @Override
                public Observable<MessagesResponse> call(MessagesResponse conversationsResponse) {
                    if (conversationsResponse == null || conversationsResponse.getPrevious() == null) {
                        return mApiService.getMessages(conversationId, PAGE_SIZE)
                                .subscribeOn(mNetworkScheduler)
                                .observeOn(mUiScheduler);
                    } else {
                        final String before = Uri.parse(conversationsResponse.getPrevious()).getQueryParameter("before");
                        return Observable.just(
                                conversationsResponse)
                                .zipWith(
                                        mApiService.getMessages(conversationId, before, PAGE_SIZE)
                                                .subscribeOn(mNetworkScheduler)
                                                .observeOn(mUiScheduler),
                                        (previousResponses, newResponse) -> {
                                            return new MessagesResponse(newResponse.getNext(),
                                                    newResponse.getPrevious(),
                                                    ImmutableList.copyOf(Iterables.concat(
                                                            newResponse.getResults(),
                                                            previousResponses.getResults())));
                                        });
                    }
                }
            });

    @NonNull
    private final String conversationId;
    @NonNull
    private final ApiService mApiService;
    private final Scheduler mUiScheduler;
    private final Scheduler mNetworkScheduler;
    private final UserPreferences mUserPreferences;
    private final Resources mResources;
    private final Context mContext;
    private final PusherHelper mPusher;
    private Listener mListener;
    private CompositeSubscription mSubscribe = new CompositeSubscription();
    private final PublishSubject<Object> requestSubject = PublishSubject.create();
    private final PublishSubject<PusherMessage> newMessagesSubject = PublishSubject.create();
    private final BehaviorSubject<ConversationProfile> chatParticipantUsernameSubject = BehaviorSubject.create();
    private final Observable<ConversationProfile> calledPersonNameAndUsernameObservable;
    private final ChatsDelegate mChatsDelegate;

    @Inject
    public ChatsPresenter(@NonNull String conversationId,
                          @NonNull ApiService apiService,
                          @UiScheduler Scheduler uiScheduler,
                          @NetworkScheduler Scheduler networkScheduler,
                          final UserPreferences userPreferences,
                          @ForActivity Resources resources,
                          @ForActivity Context context,
                          AmazonHelper amazonHelper,
                          PusherHelper pusher,
                          LocalMessageBus bus) {
        this.conversationId = conversationId;
        mApiService = apiService;
        mUiScheduler = uiScheduler;
        mNetworkScheduler = networkScheduler;
        mUserPreferences = userPreferences;
        mResources = resources;
        mContext = context;
        mPusher = pusher;

        //noinspection ConstantConditions
        calledPersonNameAndUsernameObservable = chatParticipantUsernameSubject
                .filter(Functions1.isNotNull())
                .filter(calledPersonNameAndUsername ->
                        !Objects.equal(userPreferences.getUser().getUsername(), calledPersonNameAndUsername.getUsername()));

        mChatsDelegate = new ChatsDelegate(pusher, uiScheduler, networkScheduler, apiService, resources, userPreferences, context, amazonHelper, newMessagesSubject, bus);
    }

    public void register(@NonNull Listener listener) {
        final User user = mUserPreferences.getUser();
        assert user != null;

        mListener = listener;
        mListener.showProgress(true);
        mChatsDelegate.setListener(listener);

        subscribeToMessages();

        getConversation(user);
    }

    private void getConversation(final User user) {
        mSubscribe.add(mApiService.getConversation(conversationId)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(conversationResponse -> {
                    final ConversationDetails.Display display = conversationResponse.getDisplay();
                    mListener.setToolbarInfo(display.getTitle(), display.getSubTitle());
                    if (conversationResponse.isShoutChat()) {
                        final AboutShout about = conversationResponse.getAbout();
                        final String id = about.getId();

                        if (!Strings.isNullOrEmpty(id)) {
                            final String title = about.getTitle();
                            final String thumbnail = Strings.emptyToNull(about.getThumbnail());
                            final String type = about.getType().equals(Shout.TYPE_OFFER) ? mContext.getString(R.string.chat_offer) : mContext.getString(R.string.chat_request);
                            final String price = PriceUtils.formatPriceWithCurrency(about.getPrice(), mResources, about.getCurrency());
                            final String authorAndTime = about.getProfile().getName() + " - " + DateUtils.getRelativeTimeSpanString(mContext, about.getDatePublished() * 1000);
                            mListener.setAboutShoutData(title, thumbnail, type, price, authorAndTime, id);
                            mListener.showVideoChatIcon();
                        }
                    }
                    setupUserForVideoChat(conversationResponse);
                }, getOnError()));
    }

    private void subscribeToMessages() {
        final PresenceChannel presenceChannel = mChatsDelegate.getConversationChannel(conversationId);

        final Observable<PusherMessage> pusherMessageObservable = mChatsDelegate.getPusherMessageObservable(presenceChannel);

        final Observable<TypingInfo> isTyping = mChatsDelegate.getTypingObservable(presenceChannel);

        final Observable<MessagesResponse> apiMessages = requestSubject
                .startWith(new Object())
                .lift(loadMoreOperator)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler);

        final Observable<List<PusherMessage>> localAndPusherMessages = pusherMessageObservable.mergeWith(newMessagesSubject)
                .compose(mChatsDelegate.transformToScan());

        mSubscribe.add(Observable.combineLatest(apiMessages, localAndPusherMessages.startWith((List<PusherMessage>) null), isTyping,
                (messagesResponse, pusherMessage, isTyping1) -> {
                    final ImmutableList.Builder<Message> builder = ImmutableList.<Message>builder()
                            .addAll(messagesResponse.getResults());

                    if (pusherMessage != null) {
                        for (PusherMessage message : pusherMessage) {
                            builder.add(new Message(
                                    conversationId, message.getProfile(),
                                    message.getId(),
                                    message.getText(),
                                    message.getAttachments(),
                                    message.getCreatedAt()));
                        }
                    }

                    final List<BaseAdapterItem> baseAdapterItemList = mChatsDelegate.transform(builder.build());

                    if (isTyping1.isTyping()) {
                        return ImmutableList.<BaseAdapterItem>builder()
                                .addAll(baseAdapterItemList)
                                .add(new TypingItem(isTyping1.getUsername()))
                                .build();
                    } else {
                        return baseAdapterItemList;
                    }
                })
                .subscribe(baseAdapterItems -> {
                    mChatsDelegate.messagesSuccess(baseAdapterItems, mListener);
                }, throwable -> {
                    mChatsDelegate.messagesError(throwable, mListener);
                }));
    }

    private void setupUserForVideoChat(@NonNull ConversationDetails conversation) {
        final List<ConversationProfile> profiles = conversation.getProfiles();
        if (profiles.size() == 2 && !conversation.isPublicChat()) {
            final ConversationProfile participant;
            //noinspection ConstantConditions
            if (profiles.get(0).getUsername()
                    .equals(mUserPreferences.getUser().getUsername())) {
                participant = profiles.get(1);
            } else {
                participant = profiles.get(0);
            }

            chatParticipantUsernameSubject.onNext(participant);
            mListener.showVideoChatIcon();
        }
    }

    @NonNull
    private Action1<Throwable> getOnError() {
        return throwable -> mListener.error(throwable);
    }

    @NonNull
    public Observer<Object> getRequestSubject() {
        return requestSubject;
    }

    public void postTextMessage(@NonNull String text) {
        mSubscribe.add(mApiService.postMessage(conversationId, new PostMessage(text, ImmutableList.<MessageAttachment>of()))
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(messagesResponse -> {
                    mChatsDelegate.postLocalMessage(messagesResponse, conversationId);
                }, getOnError()));
    }

    public void unregister() {
        mListener = null;
        mSubscribe.unsubscribe();
        mPusher.unsubscribeConversationChannel(conversationId);
        mChatsDelegate.removeListener();
    }

    public void addMedia(@NonNull String media, boolean isVideo) {
        mSubscribe.add(mChatsDelegate.addMedia(media, isVideo, video -> mApiService.postMessage(conversationId, new PostMessage(null, ImmutableList.of(
                new MessageAttachment(MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, null, ImmutableList.of(video), null))))
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler), url -> mApiService.postMessage(
                        conversationId,
                        new PostMessage(null, ImmutableList.of(new MessageAttachment(
                                MessageAttachment.ATTACHMENT_TYPE_MEDIA, null, null, ImmutableList.of(url), null, null))))
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler), conversationId));
    }

    public void sendLocation(double latitude, double longitude) {
        mSubscribe.add(mApiService.postMessage(conversationId, mChatsDelegate.getLocationMessage(latitude, longitude))
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(message -> {
                    mChatsDelegate.postLocalMessage(message, conversationId);
                    mListener.hideAttatchentsMenu();
                }, getOnError()));
    }


    public void deleteConversation() {
        mSubscribe.add(mChatsDelegate.deleteConversation(conversationId));
    }

    public void sendShout(final String shoutId) {
        mSubscribe.add(
                mApiService.postMessage(conversationId, mChatsDelegate.getShoutMessage(shoutId))
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler)
                        .subscribe(message -> {
                            mChatsDelegate.postLocalMessage(message, conversationId);
                            mListener.hideAttatchentsMenu();
                        }, getOnError()));
    }

    public void sendProfile(@Nonnull String profileId) {
        mSubscribe.add(
                mApiService.postMessage(conversationId, mChatsDelegate.getProfileMessage(profileId))
                        .subscribeOn(mNetworkScheduler)
                        .observeOn(mUiScheduler)
                        .subscribe(message -> {
                            mChatsDelegate.postLocalMessage(message, conversationId);
                            mListener.hideAttatchentsMenu();
                        }, getOnError())
        );
    }

    public void sendTyping() {
        mChatsDelegate.sendTyping(conversationId);
    }

    public Observable<ConversationProfile> getCalledPersonNameObservable() {
        return calledPersonNameAndUsernameObservable;
    }

}
