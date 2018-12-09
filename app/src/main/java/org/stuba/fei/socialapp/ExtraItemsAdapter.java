package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 16.11.2018.
 */

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Debug;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Collections;
import java.util.List;

public class ExtraItemsAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final int VIEW_TYPE_PADDING = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private boolean main_recycler;
    private Context context;
    List<PostPojo> title;

    public ExtraItemsAdapter(Context context, List<PostPojo> title, boolean main_recycler) {
        this.title=title;
        this.context=context;
        this.main_recycler=main_recycler;
    }

    @Override
    public int getItemCount() {
        return title.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (main_recycler){
            if (position  == -1 || position == getItemCount()) {
                return VIEW_TYPE_PADDING;
            }
            return VIEW_TYPE_ITEM;
        }
        else {
            if (position  == -1 || position  == getItemCount()) {
                return VIEW_TYPE_PADDING;
            }
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_padding, parent, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            PostPojo currentCard = title.get(position);
            holder.user.setText(currentCard.getUsername());
            holder.date.setText(currentCard.getDate());


            if (currentCard.getType().equalsIgnoreCase("video"))
            {
                holder.playerView.setPlayer(null);

                holder.player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
                holder.playerView.setPlayer(holder.player);

                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "app"));

                ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(currentCard.getVideourl()));

                holder.player.prepare(mediaSource);
                holder.player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                holder.player.setPlayWhenReady(false);
                holder.imgButton.setVisibility(View.VISIBLE);
                holder.playerView.setVisibility(View.VISIBLE);
                holder.img_android.setVisibility(View.GONE);

            }
            else if(currentCard.getType().equalsIgnoreCase("image")){
                holder.imgButton.setVisibility(View.INVISIBLE);
                String url = Uri.parse(currentCard.getImageurl())
                        .buildUpon()
                        .build()
                        .toString();
                holder.playerView.setVisibility(View.GONE);
                holder.img_android.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(url)
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .crossFade()
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.img_android);
            }
        }

    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
            if (holder.player == null && getItemViewType(holder.getAdapterPosition()) == VIEW_TYPE_ITEM) {

                PostPojo currentCard = title.get(holder.getAdapterPosition());

                if (currentCard.getType().equalsIgnoreCase("video")) {

                    holder.user.setText(currentCard.getUsername());
                    holder.date.setText(currentCard.getDate());

                    holder.player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());

                    holder.playerView.setPlayer(holder.player);

                    DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "app"));

                    ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(currentCard.getVideourl()));

                    holder.player.prepare(mediaSource);
                    holder.player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    holder.player.setPlayWhenReady(false);

                    holder.imgButton.setVisibility(View.VISIBLE);
                    holder.playerView.setVisibility(View.VISIBLE);
                    holder.img_android.setVisibility(View.GONE);
                }
            }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder)
    {
        if (holder.player != null)
        {
            holder.playerView.setPlayer(null);
            holder.player.release();
            holder.player = null;
        }
    }

}