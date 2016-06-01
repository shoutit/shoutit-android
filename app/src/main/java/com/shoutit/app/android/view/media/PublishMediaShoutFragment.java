package com.shoutit.app.android.view.media;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.facebook.CallbackManager;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreateOfferShoutWithImageRequest;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.EditShoutPriceRequest;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.view.createshout.publish.PublishShoutActivity;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.widget.CurrencySpinnerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PublishMediaShoutFragment extends Fragment {

    private static final String ARGS_IS_VIDEO = "args_video";
    private static final String ARGS_FILE = "args_file";
    private static final String TAG = PublishMediaShoutFragment.class.getCanonicalName();

    @Bind(R.id.publish_media_shout_preview)
    ImageView mPublishMediaShoutPreview;
    @Bind(R.id.camera_cool_icon)
    ImageView mPublishMediaCool;
    @Bind(R.id.camera_published_price)
    EditText mCameraPublishedPrice;
    @Bind(R.id.camera_published_currency)
    Spinner mCameraPublishedCurrency;
    @Bind(R.id.camera_progress)
    View progress;
    @Bind(R.id.camera_published_done)
    Button mCameraPublishedDone;
    @Bind(R.id.fragment_camera_close)
    ImageButton closeButton;
    @Bind(R.id.camera_published_facebook_checkbox)
    CheckBox faceBookCheckbox;

    @Inject
    ApiService mApiService;

    @Inject
    AmazonHelper mAmazonHelper;

    @Inject
    FacebookHelper mFacebookHelper;

    @Inject
    UserPreferences mUserPreferences;

    private CurrencySpinnerAdapter mCurrencyAdapter;

    private String mFile;
    private boolean mIsVideo;

    private String createdShoutOfferId;
    private String mWebUrl;
    private CallbackManager mCallbackManager;

    private CompositeSubscription mCompositeSubscription;

    public static Fragment newInstance(@Nonnull String file, boolean isVideo) {
        final PublishMediaShoutFragment fragment = new PublishMediaShoutFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(ARGS_FILE, file);
        bundle.putBoolean(ARGS_IS_VIDEO, isVideo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerPublishMediaShoutFragmentComponent.builder()
                .appComponent(App.getAppComponent(getActivity().getApplication()))
                .build()
                .inject(this);

        mCallbackManager = CallbackManager.Factory.create();

        final Bundle arguments = getArguments();
        mFile = arguments.getString(ARGS_FILE);
        mIsVideo = arguments.getBoolean(ARGS_IS_VIDEO);

        mCurrencyAdapter = new CurrencySpinnerAdapter(
                R.string.camera_publish_currency,
                getActivity(),
                R.layout.camera_publish_currency_item,
                android.R.layout.simple_list_item_1);
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.publish_media_shout_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCompositeSubscription = new CompositeSubscription();

        setUpFacebookCheckbox();

        mCameraPublishedCurrency.setAdapter(mCurrencyAdapter);
        mCameraPublishedCurrency.setEnabled(false);

        mCameraPublishedPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCameraPublishedCurrency.setEnabled(s.length() != 0);
            }
        });

        downloadCurrencies();

        uploadMedia();

        if (!mIsVideo) {
            mPublishMediaShoutPreview.setImageURI(Uri.parse(mFile));
        }

        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        mPublishMediaCool.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    private void setUpFacebookCheckbox() {
        //noinspection ConstantConditions
        faceBookCheckbox.setChecked(mFacebookHelper.hasRequiredPermissionInApi(
                mUserPreferences.getUser(), FacebookHelper.PERMISSION_PUBLISH_ACTIONS));

        mCompositeSubscription.add(
                RxView.clicks(faceBookCheckbox)
                        .map(new Func1<Void, Boolean>() {
                            @Override
                            public Boolean call(Void aVoid) {
                                return faceBookCheckbox.isChecked();
                            }
                        })
                        .filter(Functions1.isTrue())
                        .switchMap(new Func1<Boolean, Observable<ResponseOrError<Boolean>>>() {
                            @Override
                            public Observable<ResponseOrError<Boolean>> call(Boolean aBoolean) {
                                showProgress(true);

                                return mFacebookHelper.askForPermissionIfNeeded(getActivity(),
                                        FacebookHelper.PERMISSION_PUBLISH_ACTIONS, mCallbackManager, true)
                                        .observeOn(MyAndroidSchedulers.mainThread());
                            }
                        })
                        .subscribe(new Action1<ResponseOrError<Boolean>>() {
                            @Override
                            public void call(ResponseOrError<Boolean> responseOrError) {
                                showProgress(false);

                                if (responseOrError.isData()) {
                                    final Boolean isPermissionGranted = responseOrError.data();
                                    if (!isPermissionGranted) {
                                        faceBookCheckbox.setChecked(false);
                                        showPermissionNotGrantedError();
                                    }
                                } else {
                                    faceBookCheckbox.setChecked(false);
                                    showApiError(responseOrError.error());
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                showProgress(false);
                                faceBookCheckbox.setChecked(false);
                                showApiError(throwable);
                            }
                        })
        );
    }

    public void showPermissionNotGrantedError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()),
                R.string.request_activity_facebook_permission_error, Snackbar.LENGTH_SHORT).show();
    }


    private void showProgress(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void uploadMedia() {
        showProgress(true);
        final Observable<CreateShoutResponse> observable;
        if (mIsVideo) {
            observable = uploadVideoObservable();
        } else {
            observable = uploadImageObservable();
        }
        mCompositeSubscription.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(MyAndroidSchedulers.mainThread())
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        showProgress(false);
                    }
                })
                .subscribe(
                        new Action1<CreateShoutResponse>() {
                            @Override
                            public void call(CreateShoutResponse createShoutResponse) {
                                createdShoutOfferId = createShoutResponse.getId();
                                mWebUrl = createShoutResponse.getWebUrl();
                                mCameraPublishedDone.setEnabled(true);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, "error", throwable);
                                showApiError(throwable);
                            }
                        }));
    }

    private Observable<CreateShoutResponse> uploadImageObservable() {
        return mAmazonHelper.uploadShoutMediaImageObservable(new File(mFile))
                .flatMap(new Func1<String, Observable<CreateShoutResponse>>() {
                    @Override
                    public Observable<CreateShoutResponse> call(String url) {
                        return mApiService.createShoutOffer(CreateOfferShoutWithImageRequest.withImage(url, faceBookCheckbox.isChecked()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(MyAndroidSchedulers.mainThread());
                    }
                });
    }

    private Observable<CreateShoutResponse> uploadVideoObservable() {
        return Observable
                .defer(new Func0<Observable<File>>() {
                    @Override
                    public Observable<File> call() {
                        try {
                            final File videoThumbnail = MediaUtils.createVideoThumbnail(getActivity(), Uri.parse(mFile));
                            return Observable.just(videoThumbnail);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(MyAndroidSchedulers.mainThread())
                .doOnNext(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        mPublishMediaShoutPreview.setImageURI(Uri.fromFile(file));
                    }
                })
                .flatMap(new Func1<File, Observable<String>>() {
                    @Override
                    public Observable<String> call(File file) {
                        return mAmazonHelper.uploadShoutMediaImageObservable(file)
                                .subscribeOn(Schedulers.io())
                                .observeOn(MyAndroidSchedulers.mainThread());
                    }
                })
                .flatMap(new Func1<String, Observable<BothParams<String, String>>>() {
                    @Override
                    public Observable<BothParams<String, String>> call(final String thumb) {
                        return mAmazonHelper.uploadShoutMediaVideoObservable(new File(mFile))
                                .map(new Func1<String, BothParams<String, String>>() {
                                    @Override
                                    public BothParams<String, String> call(String file) {
                                        return BothParams.of(thumb, file);
                                    }
                                });
                    }
                })
                .flatMap(new Func1<BothParams<String, String>, Observable<CreateShoutResponse>>() {
                    @Override
                    public Observable<CreateShoutResponse> call(BothParams<String, String> urls) {
                        return mApiService.createShoutOffer(CreateOfferShoutWithImageRequest.withVideo(urls.param1(), urls.param2(), faceBookCheckbox.isChecked()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(MyAndroidSchedulers.mainThread());
                    }
                });
    }

    private void downloadCurrencies() {
        mCompositeSubscription.add(mApiService.getCurrencies()
                .subscribeOn(Schedulers.io())
                .observeOn(MyAndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<Currency>>() {
                            @Override
                            public void call(List<Currency> currencies) {
                                mCurrencyAdapter.setData(PriceUtils.transformCurrencyToPair(currencies));
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, "error", throwable);
                                showApiError(throwable);
                            }
                        }));
    }

    @SuppressWarnings("unchecked")
    @OnClick(R.id.camera_published_done)
    public void done() {
        Preconditions.checkNotNull(mCameraPublishedPrice);

        final String price = mCameraPublishedPrice.getText().toString();
        if (!Strings.isNullOrEmpty(price)) {
            showProgress(true);
            final long priceInCents = PriceUtils.getPriceInCents(price);
            final PriceUtils.SpinnerCurrency selectedItem = (PriceUtils.SpinnerCurrency) mCameraPublishedCurrency.getSelectedItem();
            mCompositeSubscription.add(mApiService.editShoutPrice(createdShoutOfferId, new EditShoutPriceRequest(priceInCents, selectedItem.getCode()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(MyAndroidSchedulers.mainThread())
                    .subscribe(new Action1<CreateShoutResponse>() {
                        @Override
                        public void call(CreateShoutResponse createShoutResponse) {
                            getActivity().finish();
                            startActivity(PublishShoutActivity.newIntent(getActivity(), createdShoutOfferId, mWebUrl, false, null));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "error", throwable);
                            showProgress(false);
                            showApiError(throwable);
                        }
                    }));
        } else {
            getActivity().finish();
            startActivity(PublishShoutActivity.newIntent(getActivity(), createdShoutOfferId, mWebUrl, false, null));
        }
    }

    public void showApiError(Throwable throwable) {
        ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()), throwable, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.fragment_camera_close)
    void close() {
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        mCompositeSubscription.unsubscribe();
    }
}
