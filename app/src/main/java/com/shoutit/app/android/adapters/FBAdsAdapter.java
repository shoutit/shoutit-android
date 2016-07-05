package com.shoutit.app.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.FbAdAdapterItem;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.viewholders.FbAdLinearViewHolder;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FBAdsAdapter extends ChangeableLayoutManagerAdapter {
    public static final int VIEW_TYPE_AD = 200;

    public FBAdsAdapter(@ForActivity @Nonnull Context context) {
        super(context);
    }

    public class FbAdGridViewHolder extends ViewHolderManager.BaseViewHolder<FbAdAdapterItem> {

        @Bind(R.id.ad_action_view)
        TextView actionButtonTv;
        @Bind(R.id.ad_title_tv)
        TextView titleTv;
        @Bind(R.id.ad_container)
        ViewGroup adContainer;
        @Bind(R.id.ad_adchoice_container)
        ViewGroup addChoiceContainer;
        @Bind(R.id.ad_media_view)
        MediaView mediaView;

        public FbAdGridViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull FbAdAdapterItem item) {
            final NativeAd ad = item.getAd();
            ad.unregisterView();

            titleTv.setText(ad.getAdTitle());
            actionButtonTv.setText(ad.getAdCallToAction());

            mediaView.setNativeAd(ad);

            final AdChoicesView adChoicesView = new AdChoicesView(context, ad, true);
            addChoiceContainer.addView(adChoicesView);

            ad.registerViewForInteraction(adContainer);
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (VIEW_TYPE_AD == viewType) {
            return isLinearLayoutManager ?
                    new FbAdLinearViewHolder(layoutInflater.inflate(R.layout.ad_list_layout, parent, false), context) :
                    new FbAdGridViewHolder(layoutInflater.inflate(R.layout.ad_grid_layout, parent, false));
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof FbAdAdapterItem) {
            return VIEW_TYPE_AD;
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }


}
