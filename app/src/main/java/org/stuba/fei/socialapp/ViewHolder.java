package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 16.11.2018.
 */
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView user,date;
    ImageView img_android;
    PlayerView playerView;
    SimpleExoPlayer player = null;

    public ViewHolder(View itemView) {
        super(itemView);
        user = (TextView) itemView.findViewById(R.id.textView3);
        date = (TextView) itemView.findViewById(R.id.item_text);
        img_android = (ImageView) itemView.findViewById(R.id.img_android);
        playerView = itemView.findViewById(R.id.playerView);
    }
}