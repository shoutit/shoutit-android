package com.shoutit.app.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shoutit.app.android.api.model.BlockedProfilesResposne;
import com.shoutit.app.android.api.model.CallerProfile;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.ChangePasswordRequest;
import com.shoutit.app.android.api.model.ConversationMediaResponse;
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.ConversationsResponse;
import com.shoutit.app.android.api.model.CreateOfferShoutWithImageRequest;
import com.shoutit.app.android.api.model.CreatePublicChatRequest;
import com.shoutit.app.android.api.model.CreateRequestShoutRequest;
import com.shoutit.app.android.api.model.CreateRequestShoutWithPriceRequest;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.EditPublicChatRequest;
import com.shoutit.app.android.api.model.EditShoutPriceRequest;
import com.shoutit.app.android.api.model.EditShoutPublishToFacebook;
import com.shoutit.app.android.api.model.EditShoutRequest;
import com.shoutit.app.android.api.model.EditShoutRequestWithPrice;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.GuestSignupRequest;
import com.shoutit.app.android.api.model.InvitationCodeResponse;
import com.shoutit.app.android.api.model.ListenersResponse;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.Message;
import com.shoutit.app.android.api.model.MessagesResponse;
import com.shoutit.app.android.api.model.NotificationsResponse;
import com.shoutit.app.android.api.model.PagesResponse;
import com.shoutit.app.android.api.model.PostMessage;
import com.shoutit.app.android.api.model.ProfileRequest;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.PromoteLabel;
import com.shoutit.app.android.api.model.PromoteOption;
import com.shoutit.app.android.api.model.PromoteRequest;
import com.shoutit.app.android.api.model.PromoteResponse;
import com.shoutit.app.android.api.model.RegisterDeviceRequest;
import com.shoutit.app.android.api.model.RelatedTagsResponse;
import com.shoutit.app.android.api.model.ResetPasswordRequest;
import com.shoutit.app.android.api.model.SearchProfileResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutResponse;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.Suggestion;
import com.shoutit.app.android.api.model.SuggestionsResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.TagsRequest;
import com.shoutit.app.android.api.model.TransactionRsponse;
import com.shoutit.app.android.api.model.TwilioResponse;
import com.shoutit.app.android.api.model.UpdateFacebookTokenRequest;
import com.shoutit.app.android.api.model.UpdateLocationRequest;
import com.shoutit.app.android.api.model.UpdateUserRequest;
import com.shoutit.app.android.api.model.UploadContactsRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserIdentity;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.VerifyEmailRequest;
import com.shoutit.app.android.api.model.VerifyEmailResponse;
import com.shoutit.app.android.api.model.VideoCallRequest;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;
import com.shoutit.app.android.api.model.login.FacebookLogin;
import com.shoutit.app.android.api.model.login.GoogleLogin;
import com.shoutit.app.android.model.MobilePhoneResponse;
import com.shoutit.app.android.model.ReportBody;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface ApiService {

    /**
     * Discover
     **/
    @GET("discover")
    Observable<DiscoverResponse> discovers(@Query("country") String country,
                                           @Query("page") Integer page,
                                           @Query("page_size") Integer pageSize);

    @GET("discover/{id}")
    Observable<DiscoverItemDetailsResponse> discoverItem(@Path("id") String id);


    /**
     * Shouts
     **/
    @GET("shouts")
    Observable<ShoutsResponse> shoutsForLocation(@Query("country") String countryCode,
                                                 @Query("city") String city,
                                                 @Query("state") String state,
                                                 @Query("page") Integer page,
                                                 @Query("page_size") Integer pageSize,
                                                 @Query("min_price") Integer minPrice,
                                                 @Query("max_price") Integer maxPrice,
                                                 @Query("within") Integer distance,
                                                 @Query("shout_type") String shoutType,
                                                 @Query("sort") String sortBy,
                                                 @Query("category") String categorySlug,
                                                 @QueryMap Map<String, String> filtersMap);

    @GET("shouts")
    Observable<ShoutsResponse> shoutsForDiscoverItem(@Query("discover") @NonNull String discoverId,
                                                     @Query("page") @Nullable Integer page,
                                                     @Query("page_size") @Nullable Integer pageSize);

    @GET("shouts/{id}")
    Observable<Shout> shout(@Path("id") String shoutId);

    @GET("shouts")
    Observable<ShoutsResponse> shoutsForUser(@Query("profile") String userName,
                                             @Query("page") Integer page,
                                             @Query("page_size") Integer pageSize);

    @GET("shouts/{id}/related")
    Observable<ShoutsResponse> shoutsRelated(@Path("id") String shoutId,
                                             @Query("page") Integer page,
                                             @Query("page_size") Integer pageSize);

    @GET("shouts/categories")
    Observable<List<Category>> categories();

    @GET("shouts")
    Observable<ShoutsResponse> searchShouts(@Query("search") String query,
                                            @Query("page") Integer page,
                                            @Query("page_size") Integer pageSize,
                                            @Query("country") String countryCode,
                                            @Query("city") String city,
                                            @Query("state") String state,
                                            @Query("min_price") Integer minPrice,
                                            @Query("max_price") Integer maxPrice,
                                            @Query("within") Integer distance,
                                            @Query("shout_type") String shoutType,
                                            @Query("sort") String sortBy,
                                            @Query("category") String categorySlug,
                                            @QueryMap Map<String, String> filtersMap);

    @GET("shouts")
    Observable<ShoutsResponse> searchProfileShouts(@Query("search") String query,
                                                   @Query("page") Integer page,
                                                   @Query("page_size") Integer pageSize,
                                                   @Query("profile") String userName);

    @GET("shouts")
    Observable<ShoutsResponse> searchTagShouts(@Query("search") String query,
                                               @Query("page") Integer page,
                                               @Query("page_size") Integer pageSize,
                                               @Query("tags") String tagNameOrCategorySlug,
                                               @Query("country") String countryCode,
                                               @Query("city") String city,
                                               @Query("state") String state,
                                               @Query("min_price") Integer minPrice,
                                               @Query("max_price") Integer maxPrice,
                                               @Query("within") Integer distance,
                                               @Query("shout_type") String shoutType,
                                               @Query("sort") String sortBy,
                                               @Query("tags") String tagsNames,
                                               @QueryMap Map<String, String> filtersMap);

    @GET("shouts")
    Observable<ShoutsResponse> searchCategoriesShouts(@Query("search") String query,
                                                      @Query("page") Integer page,
                                                      @Query("page_size") Integer pageSize,
                                                      @Query("country") String countryCode,
                                                      @Query("city") String city,
                                                      @Query("state") String state,
                                                      @Query("min_price") Integer minPrice,
                                                      @Query("max_price") Integer maxPrice,
                                                      @Query("within") Integer distance,
                                                      @Query("shout_type") String shoutType,
                                                      @Query("sort") String sortBy,
                                                      @Query("category") String categorySlug,
                                                      @QueryMap Map<String, String> filtersMap);

    @GET("shouts")
    Observable<ShoutsResponse> searchDiscoverShouts(@Query("search") String query,
                                                    @Query("page") Integer page,
                                                    @Query("page_size") Integer pageSize,
                                                    @Query("discover") String userName,
                                                    @Query("min_price") Integer minPrice,
                                                    @Query("max_price") Integer maxPrice,
                                                    @Query("within") Integer distance,
                                                    @Query("shout_type") String shoutType,
                                                    @Query("sort") String sortBy,
                                                    @Query("category") String categorySlug,
                                                    @QueryMap Map<String, String> filtersMap);

    @GET("shouts")
    Observable<ShoutsResponse> tagShouts(@Query("tags") String tagName,
                                         @Query("page") Integer page,
                                         @Query("page_size") Integer pageSize);

    @GET("shouts/autocomplete")
    Observable<List<Suggestion>> searchSuggestions(@Query("search") String searchQuery,
                                                   @Query("category") String categoryName,
                                                   @Query("country") String country);

    @GET("shouts/sort_types")
    Observable<List<SortType>> sortTypes();

    @GET("shouts/promote_labels")
    Observable<List<PromoteLabel>> promoteLabels();

    @GET("shouts/promote_options")
    Observable<List<PromoteOption>> promoteOptions();

    @PATCH("shouts/{id}/promote")
    Observable<PromoteResponse> promote(@Path("id") String shoutId,
                                        @Body PromoteRequest promoteRequest);

    /**
     * OAuth
     **/
    @POST("oauth2/access_token")
    Observable<SignResponse> login(@Body EmailLoginRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> loginGuest(@Body GuestSignupRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> signup(@Body EmailSignupRequest request);

    @POST("oauth2/access_token")
    Observable<SignResponse> facebookLogin(@Body FacebookLogin request);

    @POST("oauth2/access_token")
    Observable<SignResponse> googleLogin(@Body GoogleLogin request);

    /**
     * Auth
     **/

    @POST("auth/reset_password")
    Observable<ResponseBody> resetPassword(@Body ResetPasswordRequest request);

    @POST("auth/change_password")
    Observable<ResponseBody> changePassword(@Body ChangePasswordRequest changePasswordRequest);

    @POST("auth/verify_email")
    Observable<VerifyEmailResponse> verifyEmail(@Body VerifyEmailRequest verifyEmailRequest);


    /**
     * Profile
     **/
    @GET("profiles/{user_name}")
    Observable<User> getUser(@Path("user_name") String userName);

    @GET("profiles/me")
    Observable<User> getMyUser();

    @PATCH("profiles/me")
    Observable<User> updateUserLocation(@Body UpdateLocationRequest updateLocationRequest);

    @GET("profiles/{user_name}/home")
    Observable<ShoutsResponse> home(@Path("user_name") String userName,
                                    @Query("page") Integer page,
                                    @Query("page_size") Integer pageSize);

    @PATCH("profiles/me")
    Observable<User> updateUser(@Body UpdateUserRequest updateUserRequest);

    @PATCH("profiles/me")
    Observable<User> registerGcmToken(@Body RegisterDeviceRequest registerDeviceRequest);

    @PATCH("profiles/me")
    Observable<User> unregisterGcmToken(@Body RequestBody registerDeviceRequest);

    @POST("profiles/{username}/listen")
    Observable<ResponseBody> listenProfile(@Path("username") String username);

    @DELETE("profiles/{username}/listen")
    Observable<ResponseBody> unlistenProfile(@Path("username") String username);

    @GET("profiles/{user_name}")
    Observable<User> getProfile(@Path("user_name") String userName);

    @GET("profiles")
    Observable<SearchProfileResponse> searchProfiles(@Query("search") String searchQuery,
                                                     @Query("page") Integer page,
                                                     @Query("page_size") Integer pageSize);

    @GET("profiles/me/listening")
    Observable<ProfilesListResponse> profilesListenings(@Query("page") Integer page,
                                                     @Query("page_size") Integer pageSize);

    @GET("profiles/me/interests")
    Observable<ProfilesListResponse> tagsListenings(@Query("page") Integer page,
                                                 @Query("page_size") Integer pageSize);

    @GET("profiles/{user_name}/listeners")
    Observable<ProfilesListResponse> listeners(@Path("user_name") String userName,
                                            @Query("page") Integer page,
                                            @Query("page_size") Integer pageSize);

    @GET("profiles/{user_name}/mutual_friends")
    Observable<ProfilesListResponse> facebookFriends(@Path("user_name") String userName,
                                                      @Query("page") Integer page,
                                                      @Query("page_size") Integer pageSize);

    @PATCH("profiles/{user_name}/contacts")
    Observable<ResponseBody> uploadContacts(@Path("user_name") String userName,
                                            @Body UploadContactsRequest uploadContactsRequest);

    @GET("profiles/{user_name}/mutual_contacts")
    Observable<ProfilesListResponse> mutualContacts(@Path("user_name") String userName,
                                                    @Query("page") Integer page,
                                                    @Query("page_size") Integer pageSize);

    @GET("profiles/{user_name}/pages")
    Observable<PagesResponse> myPages(@Path("user_name") String userName,
                                      @Query("page") Integer page,
                                      @Query("page_size") Integer pageSize);

    /**
     * Misc
     **/
    @GET("misc/geocode")
    Observable<UserLocation> geocode(@Query("latlng") String latlng); // format like latlng=40.722100,-74.046900

    @GET("misc/geocode?latlng=0,0")
    Observable<UserLocation> geocodeDefault();

    @GET("misc/suggestions?type=users,pages")
    Observable<SuggestionsResponse> suggestions(@Query("country") String country,
                                                @Query("state") String state,
                                                @Query("city") String city,
                                                @Query("page") Integer page,
                                                @Query("page_size") Integer pageSize);

    @GET("misc/currencies")
    Observable<List<Currency>> getCurrencies();

    @POST("misc/reports")
    Observable<Response<Object>> report(@Body ReportBody reportShoutBody);


    /**
     * create shout
     */
    @POST("shouts")
    Observable<CreateShoutResponse> createShoutRequest(@Body CreateRequestShoutRequest offer);

    @POST("shouts")
    Observable<CreateShoutResponse> createShoutRequest(@Body CreateRequestShoutWithPriceRequest offer);

    @POST("shouts")
    Observable<CreateShoutResponse> createShoutOffer(@Body CreateOfferShoutWithImageRequest request);

    @PATCH("shouts/{id}")
    Observable<CreateShoutResponse> editShout(@Path("id") String id, @Body EditShoutRequest request);

    @PATCH("shouts/{id}")
    Observable<CreateShoutResponse> editShout(@Path("id") String id, @Body EditShoutRequestWithPrice request);

    @PATCH("shouts/{id}")
    Observable<CreateShoutResponse> editShoutPrice(@Path("id") String id, @Body EditShoutPriceRequest request);

    @PATCH("shouts/{id}")
    Observable<CreateShoutResponse> editShoutPublishToFacebook(@Path("id") String id, @Body EditShoutPublishToFacebook body);

    @GET("shouts/{id}")
    Observable<ShoutResponse> getShout(@Path("id") String id);

    @DELETE("shouts/{id}")
    Observable<Response<Object>> deleteShout(@Path("id") String id);

    @GET("shouts/{id}/call")
    Observable<MobilePhoneResponse> shoutCall(@Path("id") String id);

    /**
     * Tags
     **/
    @GET("tags/{name}")
    Observable<TagDetail> tagDetail(@Path("name") String tagName);

    @POST("tags/{name}/listen")
    Observable<ResponseBody> listenTag(@Path("name") String tagName);

    @DELETE("tags/{name}/listen")
    Observable<ResponseBody> unlistenTag(@Path("name") String tagName);

    @GET("tags/{name}/related")
    Observable<RelatedTagsResponse> relatedTags(@Path("name") String tagName);

    @POST("tags/batch_listen")
    Observable<Object> batchListen(@Body TagsRequest request);

    /**
     * Conversations
     */
    @GET("conversations")
    Observable<ConversationsResponse> getConversations(@Nullable @Query("before") String timestamp,
                                              @Query("page_size") Integer pageSize);

    @GET("conversations/{id}")
    Observable<ConversationDetails> getConversation(@NonNull @Path("id") String id);

    @PATCH("conversations/{id}")
    Observable<ResponseBody> updateConversation(@NonNull @Path("id") String id, @Body EditPublicChatRequest request);

    @GET("conversations/{id}/messages")
    Observable<MessagesResponse> getMessages(@NonNull @Path("id") String conversationId,
                                             @Query("page_size") Integer pageSize);

    @GET("conversations/{id}/messages")
    Observable<MessagesResponse> getMessages(@NonNull @Path("id") String conversationId,
                                             @NonNull @Query("before") String timestamp,
                                             @Query("page_size") Integer pageSize);

    @POST("conversations/{id}/reply")
    Observable<Message> postMessage(@NonNull @Path("id") String conversationId, @NonNull @Body PostMessage message);

    @POST("shouts/{id}/reply")
    Observable<Message> createShoutConversation(@NonNull @Path("id") String shoutId, @NonNull @Body PostMessage message);

    @POST("profiles/{id}/chat")
    Observable<Message> createChatConversation(@NonNull @Path("id") String userName, @NonNull @Body PostMessage message);

    @DELETE("conversations/{id}")
    Observable<ResponseBody> deleteConversation(@Path("id") String conversationId);

    @POST("conversations/{id}/add_profile")
    Observable<ResponseBody> addProfile(@Path("id") String conversationId, @Body ProfileRequest removeProfileRequest);

    @POST("conversations/{id}/remove_profile")
    Observable<ResponseBody> removeProfile(@Path("id") String conversationId, @Body ProfileRequest removeProfileRequest);

    @POST("messages/{id}/read")
    Observable<ResponseBody> readMessage(@Path("id") String messageId);

    @POST("public_chats")
    Observable<ResponseBody> createPublicChat(@Body CreatePublicChatRequest createPublicChatRequest);

    @GET("conversations/{id}/shouts")
    Observable<ShoutsResponse> conversationShouts(@Path("id") String conversationId,
                                                  @Query("page") Integer page,
                                                  @Query("page_size") Integer pageSize);

    @GET("conversations/{id}/media")
    Observable<ConversationMediaResponse> conversationMedia(@Path("id") String conversationId,
                                                            @Query("page") Integer page,
                                                            @Query("page_size") Integer pageSize);

    @POST("conversations/{id}/promote_admin")
    Observable<ResponseBody> promoteAdmin(@Path("id") String conversationId, @Body ProfileRequest profileRequest);

    @POST("conversations/{id}/block_profile")
    Observable<ResponseBody> blockProfile(@Path("id") String conversationId, @Body ProfileRequest profileRequest);

    @POST("conversations/{id}/unblock_profile")
    Observable<ResponseBody> unblockProfile(@Path("id") String conversationId, @Body ProfileRequest profileRequest);

    @GET("conversations/{id}/blocked")
    Observable<BlockedProfilesResposne> getBlockedProfiles(@Path("id") String conversationId);

    /**
     * Public Chats
     */
    @GET("public_chats")
    Observable<ConversationsResponse> publicChats(@Nullable @Query("before") String timestamp,
                                                  @Query("page_size") Integer pageSize);

    /**
     * Notifications
     **/
    @GET("notifications")
    Observable<NotificationsResponse> notifications(@Query("before") Integer beforeTimestamp,
                                                    @Query("page_size") Integer pageSize);

    @POST("notifications/reset")
    Observable<ResponseBody> markAllNotificationsAsRead();

    @POST("notifications/{id}/read")
    Observable<ResponseBody> markNotificationAsRead(@Path("id") String notificationId);

    /**
     * Twilio
     **/
    @POST("twilio/video_auth")
    Observable<TwilioResponse> getTokenAndIdentity();

    @GET("twilio/video_identity")
    Observable<UserIdentity> getUserIdentity(@Query("profile") String username);

    @GET("twilio/profile")
    Observable<CallerProfile> getUserByIdentity(@Query("identity") String identity);


    @POST("twilio/video_call")
    Observable<ResponseBody> videoCall(@Body VideoCallRequest rejectRequest);

    @PATCH("profiles/me/link")
    Observable<ResponseBody> updateFacebookToken(@Body UpdateFacebookTokenRequest updateFacebookTokenRequest);

    /**
     * Credit
     **/
    @GET("credit/transactions")
    Observable<TransactionRsponse> getTransactions(@Nullable @Query("before") String timestamp);

    @GET("credit/invitation_code")
    Observable<InvitationCodeResponse> getInvitationCode();

    /**
     * Pages
     **/
    @GET("pages")
    Observable<ProfilesListResponse> getPublicPages(@Query("country") String countryCode,
                                             @Query("page") Integer page,
                                             @Query("page_size") Integer pageSize);
}
