package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 16.11.2018.
 */

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Collections;
import java.util.List;

public class ExtraItemsAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final int VIEW_TYPE_PADDING = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private boolean main_recycler;
    private Context context;
    List<Data> title = Collections.EMPTY_LIST;

    public ExtraItemsAdapter(Context context, List<Data> title, boolean main_recycler) {
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
            if (position  == -1 || position == getItemCount()*(-1)) {
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
            Data currentCard = title.get(position);
            holder.user.setText(currentCard.getUser());
            holder.date.setText(currentCard.getDate());
            String url = Uri.parse(currentCard.getUrl())
                    .buildUpon()
                    .build()
                    .toString();
            Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .crossFade()
                    //.placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.img_android);
        }
    }

}