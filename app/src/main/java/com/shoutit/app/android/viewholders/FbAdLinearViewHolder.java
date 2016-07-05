package com.shoutit.app.android.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.FbAdAdapterItem;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FbAdLinearViewHolder extends ViewHolderManager.BaseViewHolder<FbAdAdapterItem> {

    @Bind(R.id.ad_action_view)
    TextView actionButtonTv;
    @Bind(R.id.ad_icon)
    ImageView iconIv;
    @Bind(R.id.ad_title_tv)
    TextView titleTv;
    @Bind(R.id.ad_text_tv)
    TextView textTv;
    @Bind(R.id.ad_container)
    ViewGroup adContainer;
    @Bind(R.id.ad_adchoice_container)
    ViewGroup addChoiceContainer;
    @Bind(R.id.ad_media_view)
    MediaView mediaView;

    private final Context context;

    public FbAdLinearViewHolder(@Nonnull View itemView,
                                Context context) {
        super(itemView);
        this.context = context;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull FbAdAdapterItem item) {
        final NativeAd ad = item.getAd();

        titleTv.setText(ad.getAdTitle());
        textTv.setText(ad.getAdBody());
        actionButtonTv.setText(ad.getAdCallToAction());

        final NativeAd.Image adIcon = ad.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, iconIv);

        mediaView.setNativeAd(ad);

        final AdChoicesView adChoicesView = new AdChoicesView(context, ad, true);
        addChoiceContainer.addView(adChoicesView);

        ad.registerViewForInteraction(adContainer);
    }
}