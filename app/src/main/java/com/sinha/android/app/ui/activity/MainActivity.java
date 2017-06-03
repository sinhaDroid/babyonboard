package com.sinha.android.app.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;

import com.sinha.android.app.BabyOnBoardApplication;
import com.sinha.android.app.NetworkStateChangeReceiver;
import com.sinha.android.app.R;
import com.sinha.android.app.Utils;
import com.sinha.android.app.ui.adapter.FeedAdapter;
import com.sinha.android.app.ui.adapter.FeedItemAnimator;
import com.sinha.android.app.ui.view.FeedContextMenu;
import com.sinha.android.app.ui.view.FeedContextMenuManager;

import butterknife.BindView;

public class MainActivity extends BaseDrawerActivity implements FeedAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener {
    public static final String ACTION_SHOW_LOADING_ITEM = "action_show_loading_item";

    private static final int ANIM_DURATION_TOOLBAR = 300;

    @BindView(R.id.rvFeed)
    RecyclerView rvFeed;
    @BindView(R.id.content)
    CoordinatorLayout clContent;

    private FeedAdapter feedAdapter;

    private boolean pendingIntroAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupFeed();

        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        } else {
            feedAdapter.updateItems(false);
        }

        // register the broadcast
        IntentFilter intentFilter = new IntentFilter(NetworkStateChangeReceiver.NETWORK_AVAILABLE_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void setupFeed() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        rvFeed.setLayoutManager(linearLayoutManager);

        feedAdapter = new FeedAdapter(this);
        feedAdapter.setOnFeedItemClickListener(this);
        rvFeed.setAdapter(feedAdapter);
        rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
            }
        });
        rvFeed.setItemAnimator(new FeedItemAnimator());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_SHOW_LOADING_ITEM.equals(intent.getAction())) {
            showFeedLoadingItemDelayed();
        }
    }

    private void showFeedLoadingItemDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rvFeed.smoothScrollToPosition(0);
                feedAdapter.showLoadingView();
            }
        }, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (pendingIntroAnimation) {
            pendingIntroAnimation = false;
            startIntroAnimation();
        }
        return true;
    }

    private void startIntroAnimation() {
        int actionbarSize = Utils.dpToPx(56);
        getToolbar().setTranslationY(-actionbarSize);
        getTvLogo().setTranslationY(-actionbarSize);
        getInboxMenuItem().getActionView().setTranslationY(-actionbarSize);

        getToolbar().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);
        getTvLogo().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(400);
        getInboxMenuItem().getActionView().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startContentAnimation();
                    }
                })
                .start();
    }

    private void startContentAnimation() {
        feedAdapter.updateItems(true);
    }

    @Override
    public void onCommentsClick(View v, int position) {
        final Intent intent = new Intent(this, CommentsActivity.class);
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(CommentsActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(View v, int itemPosition) {
        FeedContextMenuManager.getInstance().toggleContextMenuFromView(v, itemPosition, this);
    }

    @Override
    public void onReportClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onSharePhotoClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCopyShareUrlClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCancelClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    public void showLikedSnackbar() {
        Snackbar.make(clContent, "Liked!", Snackbar.LENGTH_SHORT).show();
    }

    public void showUnLikedSnackbar() {
        Snackbar.make(clContent, "UnLiked!", Snackbar.LENGTH_SHORT).show();
    }

    public void checkInternetAndSendData(boolean isLiked, int likesCount) {

        String connected;
        if (BabyOnBoardApplication.getInstance().hasNetworkConnection()) {
            connected = "Connected";
        } else {
            connected = "Not Connected";
        }

        // write to shared preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.save_message), " Like : " + isLiked + " likecount : " + likesCount);
        editor.apply();

        if (BabyOnBoardApplication.getInstance().hasNetworkConnection()) {
            Snackbar.make(clContent, "Network Status: " + connected + sharedPref.getString(getString(R.string.save_message), "Not Found Key"), Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(clContent, "Network Status: " + connected + sharedPref.getString(getString(R.string.save_message), "Not Found Key"), Snackbar.LENGTH_LONG).show();

            // send broadcast
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent());
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String connected;
            if (BabyOnBoardApplication.getInstance().hasNetworkConnection()) {
                connected = "Connected";
            } else {
                connected = "Not Connected";
            }

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

            Snackbar.make(clContent, "Network Status: " + connected + sharedPref.getString(getString(R.string.save_message), "Not Found Key"), Snackbar.LENGTH_LONG).show();
        }
    };
}