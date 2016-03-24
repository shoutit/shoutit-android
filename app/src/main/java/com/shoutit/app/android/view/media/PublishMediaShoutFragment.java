package com.shoutit.app.android.view.media;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.functions.BothParams;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreateOfferShoutWithImageRequest;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.EditShoutPriceRequest;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.widget.SpinnerAdapter;

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

public class PublishMediaShoutFragment extends Fragment {

    private static final String ARGS_IS_VIDEO = "args_video";
    private static final String ARGS_FILE = "args_file";

    @Bind(R.id.publish_media_shout_preview)
    ImageView mPublishMediaShoutPreview;
    @Bind(R.id.camera_published_price)
    EditText mCameraPublishedPrice;
    @Bind(R.id.camera_published_currency)
    Spinner mCameraPublishedCurrency;
    @Bind(R.id.camera_progress)
    View progress;
    @Bind(R.id.camera_published_done)
    Button mCameraPublishedDone;

    @Inject
    ApiService mApiService;

    @Inject
    AmazonHelper mAmazonHelper;

    private SpinnerAdapter mCurrencyAdapter;

    private String mFile;
    private boolean mIsVideo;

    private String createdShoutOfferId;

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

        final Bundle arguments = getArguments();
        mFile = arguments.getString(ARGS_FILE);
        mIsVideo = arguments.getBoolean(ARGS_IS_VIDEO);

        mCurrencyAdapter = new SpinnerAdapter(
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
        mCameraPublishedCurrency.setAdapter(mCurrencyAdapter);

        downloadCurrencies();

        uploadMedia();
    }

    private void uploadMedia() {
        progress.setVisibility(View.VISIBLE);
        final Observable<CreateShoutResponse> observable;
        if (mIsVideo) {
            observable = uploadVideoObservable();
        } else {
            observable = uploadImageObservable();
        }
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(MyAndroidSchedulers.mainThread())
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        progress.setVisibility(View.GONE);
                    }
                })
                .subscribe(
                        new Action1<CreateShoutResponse>() {
                            @Override
                            public void call(CreateShoutResponse createShoutResponse) {
                                createdShoutOfferId = createShoutResponse.getId();
                                mCameraPublishedDone.setEnabled(true);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                ColoredSnackBar.error(
                                        ColoredSnackBar.contentView(getActivity()),
                                        R.string.error_default,
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    private Observable<CreateShoutResponse> uploadImageObservable() {
        return mAmazonHelper.uploadShoutMediaObservable(new File(mFile))
                .flatMap(new Func1<String, Observable<CreateShoutResponse>>() {
                    @Override
                    public Observable<CreateShoutResponse> call(String url) {
                        return mApiService.createShoutOffer(CreateOfferShoutWithImageRequest.withImage(url))
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
                        return mAmazonHelper.uploadShoutMediaObservable(file)
                                .subscribeOn(Schedulers.io())
                                .observeOn(MyAndroidSchedulers.mainThread());
                    }
                })
                .flatMap(new Func1<String, Observable<BothParams<String, String>>>() {
                    @Override
                    public Observable<BothParams<String, String>> call(final String thumb) {
                        return mAmazonHelper.uploadShoutMediaObservable(new File(mFile))
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
                        return mApiService.createShoutOffer(CreateOfferShoutWithImageRequest.withVideo(urls.param1(), urls.param2()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(MyAndroidSchedulers.mainThread());
                    }
                });
    }

    private void downloadCurrencies() {
        mApiService.getCurrencies()
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
                                ColoredSnackBar.error(
                                        ColoredSnackBar.contentView(getActivity()),
                                        R.string.error_default,
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    @SuppressWarnings("unchecked")
    @OnClick(R.id.camera_published_done)
    public void done() {
        Preconditions.checkNotNull(mCameraPublishedPrice);

        final String price = mCameraPublishedPrice.getText().toString();
        if (!Strings.isNullOrEmpty(price)) {
            final long priceInCents = PriceUtils.getPriceInCents(price);
            final Pair<String, String> selectedItem = (Pair<String, String>) mCameraPublishedCurrency.getSelectedItem();
            mApiService.editShoutPrice(createdShoutOfferId, new EditShoutPriceRequest(priceInCents, selectedItem.first))
                    .subscribeOn(Schedulers.io())
                    .observeOn(MyAndroidSchedulers.mainThread())
                    .subscribe(new Action1<CreateShoutResponse>() {
                        @Override
                        public void call(CreateShoutResponse createShoutResponse) {
                            getActivity().finish();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            ColoredSnackBar.error(
                                    ColoredSnackBar.contentView(getActivity()),
                                    R.string.error_default,
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    });
        } else {
            ColoredSnackBar.error(
                    ColoredSnackBar.contentView(getActivity()),
                    R.string.error_default,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}