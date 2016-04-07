package com.givekesh.baboon.Utils;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Color;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.givekesh.baboon.R;
import com.givekesh.baboon.SelectedPostActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import worldline.com.foldablelayout.FoldableLayout;


public class FeedsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Map<Integer, Boolean> mFoldStates = new HashMap<>();
    private List<Feeds> mFeeds;
    private final Utils utils;
    private final Activity mActivity;
    private boolean isLoading = false;
    private View loading = null;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public FeedsAdapter(List<Feeds> feeds, Activity activity) {
        mFeeds = feeds;
        mActivity = activity;
        utils = new Utils(mActivity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM)
            return new newHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.new_ui, parent, false));
        //return new mHolder(new FoldableLayout(parent.getContext()));
        return new mHolderFooter(loading = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_bar, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position))
            return TYPE_FOOTER;
        return TYPE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return position == getItemCount() - 1 && isLoading;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void disableLoading() {
        isLoading = false;
        loading.setVisibility(View.GONE);
    }

    public void clear() {
        int size = this.mFeeds.size();
        if (size > 0) {
            for (int i = 0; i < size; i++)
                mFeeds.remove(0);

            this.notifyItemRangeRemoved(0, size);
            this.mFoldStates.clear();
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof mHolder) {
            final Feeds feed = mFeeds.get(position);

            Glide.with(mActivity)
                    .load(feed.getContentImage())
                    .error(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade()
                    .into(((mHolder) holder).contentImage);

            ((mHolder) holder).Title.setText(Html.fromHtml(feed.getTitle()));
            ((mHolder) holder).Date.setText(feed.getDate());
            ((mHolder) holder).Title_Detail.setText(Html.fromHtml(feed.getTitle()));
            ((mHolder) holder).Author_Detail.setText(feed.getAuthor());
            ((mHolder) holder).Date_Detail.setText(feed.getDate());
            ((mHolder) holder).Excerpt.setText(Html.fromHtml(feed.getExcerpt().replace("<p>", "<p align=\"justify\">")));

            ((mHolder) holder).open.setImageDrawable(utils.getMaterialIcon(MaterialDrawableBuilder.IconValue.OPEN_IN_NEW, Color.BLACK));

            ((mHolder) holder).open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, SelectedPostActivity.class);
                    intent.putExtra("post_parcelable", feed);
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(mActivity, ((mHolder) holder).contentImage, "post_image");
                    mActivity.startActivity(intent, options.toBundle());
                }
            });


            if (mFoldStates.containsKey(position)) {
                if (mFoldStates.get(position) == Boolean.TRUE) {
                    if (!((mHolder) holder).mFoldableLayout.isFolded()) {
                        ((mHolder) holder).mFoldableLayout.foldWithoutAnimation();
                    }
                } else if (mFoldStates.get(position) == Boolean.FALSE) {
                    if (((mHolder) holder).mFoldableLayout.isFolded()) {
                        ((mHolder) holder).mFoldableLayout.unfoldWithoutAnimation();
                    }
                }
            } else {
                ((mHolder) holder).mFoldableLayout.foldWithoutAnimation();
            }

            ((mHolder) holder).mFoldableLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((mHolder) holder).mFoldableLayout.isFolded()) {
                        ((mHolder) holder).mFoldableLayout.unfoldWithAnimation();
                    } else {
                        ((mHolder) holder).mFoldableLayout.foldWithAnimation();
                    }
                }
            });

            ((mHolder) holder).mFoldableLayout.setFoldListener(new FoldableLayout.FoldListener() {
                @Override
                public void onUnFoldStart() {
                    ((mHolder) holder).cardView.setCardElevation(10);
                }

                @Override
                public void onUnFoldEnd() {
                    mFoldStates.put(holder.getAdapterPosition(), false);
                }

                @Override
                public void onFoldStart() {
                    ((mHolder) holder).cardView.setCardElevation(0);
                }

                @Override
                public void onFoldEnd() {
                    mFoldStates.put(holder.getAdapterPosition(), true);
                }
            });
        } else if (holder instanceof newHolder) {
            final Feeds feed = mFeeds.get(position);
            Glide.with(mActivity)
                    .load(feed.getAuthor_avatar())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(((newHolder) holder).author_avatar);
            Glide.with(mActivity)
                    .load(feed.getContentImage())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(((newHolder) holder).post_image);

            ((newHolder) holder).author_name.setText(feed.getAuthor());
            ((newHolder) holder).post_date.setText(feed.getDate());
            ((newHolder) holder).post_title.setText(Html.fromHtml(feed.getTitle()));
            ((newHolder) holder).post_excerpt.setText(Html.fromHtml(feed.getExcerpt().replace("<p>", "<p align=\"justify\">")));
            ((newHolder) holder).full_article.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            ((newHolder) holder).full_article.setCardElevation(0f);
                            Intent intent = new Intent(mActivity, SelectedPostActivity.class);
                            intent.putExtra("post_parcelable", feed);
                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(mActivity, ((newHolder) holder).post_image, "post_image");
                            mActivity.startActivity(intent, options.toBundle());

                            break;
                        case MotionEvent.ACTION_UP:
                            ((newHolder) holder).full_article.setCardElevation(5f);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int footer = 0;
        if (isLoading) {
            footer++;
        }

        return mFeeds.size() + footer;
    }

    public void refresh(ArrayList<Feeds> feeds) {
        mFeeds = feeds;
        notifyItemRangeInserted(getItemCount() + 1, feeds.size());
    }

    class mHolder extends RecyclerView.ViewHolder {

        private final ImageView contentImage;
        private final TextView Date;
        private final TextView Title;
        private final TextView Author_Detail;
        private final TextView Date_Detail;
        private final TextView Title_Detail;
        private final TextView Excerpt;
        private final ImageButton open;
        private final CardView cardView;
        private final FoldableLayout mFoldableLayout;

        public mHolder(FoldableLayout itemView) {
            super(itemView);
            mFoldableLayout = itemView;

            itemView.setupViews(R.layout.feed_cover, R.layout.feed_detail, R.dimen.card_cover_height, itemView.getContext());

            contentImage = (ImageView) itemView.findViewById(R.id.content_image);
            Date = (TextView) itemView.findViewById(R.id.date);
            Title = (TextView) itemView.findViewById(R.id.title);
            Author_Detail = (TextView) itemView.findViewById(R.id.author_detail);
            Date_Detail = (TextView) itemView.findViewById(R.id.date_detail);
            Title_Detail = (TextView) itemView.findViewById(R.id.title_detail);
            Excerpt = (TextView) itemView.findViewById(R.id.excerpt);
            open = (ImageButton) itemView.findViewById(R.id.open);
            cardView = (CardView) itemView.findViewById(R.id.card_detail);
        }
    }

    class mHolderFooter extends RecyclerView.ViewHolder {
        public mHolderFooter(View itemView) {
            super(itemView);
        }
    }

    class newHolder extends RecyclerView.ViewHolder {

        private ImageView author_avatar;
        private ImageView post_image;
        private TextView author_name;
        private TextView post_date;
        private TextView post_title;
        private TextView post_excerpt;
        private CardView full_article;

        public newHolder(View itemView) {
            super(itemView);
            author_avatar = (ImageView) itemView.findViewById(R.id.author_avatar);
            post_image = (ImageView) itemView.findViewById(R.id.post_image);
            author_name = (TextView) itemView.findViewById(R.id.author_name);
            post_date = (TextView) itemView.findViewById(R.id.post_date);
            post_title = (TextView) itemView.findViewById(R.id.post_title);
            post_excerpt = (TextView) itemView.findViewById(R.id.post_excerpt);
            full_article = (CardView) itemView.findViewById(R.id.full_article);
        }
    }
}
