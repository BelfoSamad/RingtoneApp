package com.devalutix.ringtoneapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.RingtonesContract;
import com.devalutix.ringtoneapp.di.components.DaggerMVPComponent;
import com.devalutix.ringtoneapp.di.components.MVPComponent;
import com.devalutix.ringtoneapp.di.modules.ApplicationModule;
import com.devalutix.ringtoneapp.di.modules.MVPModule;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.presenters.RingtonesPresenter;
import com.devalutix.ringtoneapp.ui.adapters.RingtonesAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RingtonesActivity extends AppCompatActivity implements RingtonesContract.View {
    private static final String TAG = "RingtonesActivity";

    /**************************************** Declarations ****************************************/
    private MVPComponent mvpComponent;
    private RingtonesAdapter mAdapter;
    @Inject
    RingtonesPresenter mPresenter;
    private BottomSheetBehavior retry_behavior;
    private BottomSheetBehavior actions_behavior;

    /**************************************** View Declarations ***********************************/
    @BindView(R.id.ringtones_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.ringtones_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_to_refresh_ringtones)
    SwipeRefreshLayout mRefresh;
    @BindView(R.id.no_network_ringtones)
    ImageView noNetworkLayout;
    @BindView(R.id.empty_ringtones)
    TextView emptyCollectionLayout;

    //Actions Card
    @BindView(R.id.actions_card)
    CardView actions_card;

    //Retry
    @BindView(R.id.retry_card)
    ConstraintLayout retry_card;
    @BindView(R.id.retry_msg)
    TextView retry_msg;

    /**************************************** Click Listeners *************************************/
    @OnClick(R.id.retry)
    public void retry(){
        hideRetryCard();
        mRefresh.setRefreshing(true);
        mPresenter.updateRecyclerView(mPresenter.getMode());
    }

    //Actions
    @OnClick(R.id.set)
    void openActionsCard() {
        if (actions_behavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED)
            expandActionsCard();
        else halfExpandActionsCard(-1, null);
    }

    @OnClick(R.id.download_ringtone)
    void downloadRingtone() {
        mPresenter.saveRingtone(null);
        collapseActionsCard();
    }

    @OnClick(R.id.share_ringtone)
    void shareRingtone() {
        mPresenter.shareRingtone();
        collapseActionsCard();
    }

    @OnClick(R.id.set_ringtone)
    void setRingtone() {
        mPresenter.setAsRingtone();
        collapseActionsCard();
    }

    @OnClick(R.id.set_notification)
    void setNotification() {
        mPresenter.setAsNotification();
        collapseActionsCard();
    }

    @OnClick(R.id.set_alarm)
    void setContactRingtone() {
        mPresenter.setAsAlarm();
        collapseActionsCard();
    }

    /**************************************** Essential Methods ***********************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtones);

        //Set ButterKnife
        ButterKnife.bind(this);

        //Initialize Dagger For Application
        mvpComponent = getComponent();

        //Inject the Component Here
        mvpComponent.inject(this);

        //Attach View To Presenter
        mPresenter.attach(this);
        mPresenter.setMode(getIntent().getStringExtra("mode"));

        //Set Toolbar
        setToolbar();

        //Init Retry Sheet
        initRetrySheet();

        //Set Page Name
        setPageName(getIntent().getStringExtra("mode"), getIntent().getStringExtra("name"));


        //init RecyclerView
        mRefresh.setRefreshing(true);
        mPresenter.initRecyclerView(getIntent().getStringExtra("name"));

        //Init Actions
        initActionsPopUp();

        //Pull To Refresh Listener
        mRefresh.setOnRefreshListener(() -> {
            hideRetryCard();
            mPresenter.updateRecyclerView(getIntent().getStringExtra("name"));
        });
    }

    @Override
    public MVPComponent getComponent() {
        if (mvpComponent == null) {
            mvpComponent = DaggerMVPComponent
                    .builder()
                    .applicationModule(new ApplicationModule(getApplication()))
                    .mVPModule(new MVPModule(this))
                    .build();
        }
        return mvpComponent;
    }

    @Override
    public void setToolbar() {
        Log.d(TAG, "setToolbar: Setting the Toolbar.");

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: User Clicks on Options Item.");
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPageName(String mode, String name) {
        Log.d(TAG, "setPageName: Setting Page Name : " + name);
        if (mode.equals("search"))
            Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.search_toolbar) + " " + name);
        else Objects.requireNonNull(getSupportActionBar()).setTitle(name);
    }

    @Override
    public void initRecyclerView(ArrayList<Ringtone> ringtones) {
        //Declarations
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        mAdapter = new RingtonesAdapter(ringtones, this, mPresenter.getMode());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new RingtonesActivity.MyItemDecoration());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void initRetrySheet() {
        retry_behavior = BottomSheetBehavior.from(retry_card);
        hideRetryCard();
    }

    @Override
    public void initActionsPopUp() {
        actions_behavior = BottomSheetBehavior.from(actions_card);
        actions_behavior.setHalfExpandedRatio(0.09f);

        collapseActionsCard();
    }

    @Override
    public void updateRecyclerView(ArrayList<Ringtone> ringtones) {
        //Deleting the List of the Categories
        mAdapter.clearAll();

        // Adding The New List of Categories
        mAdapter.addAll(ringtones);

        /*
         * Stop Refreshing the Animations
         */
        mRefresh.setRefreshing(false);
    }

    @Override
    public void showRingtones() {
        mRefresh.setRefreshing(false);

        mRecyclerView.setVisibility(View.VISIBLE);
        noNetworkLayout.setVisibility(View.GONE);
        emptyCollectionLayout.setVisibility(View.GONE);
    }

    @Override
    public void showNoRingtone() {
        mRefresh.setRefreshing(false);

        mRecyclerView.setVisibility(View.GONE);
        noNetworkLayout.setVisibility(View.VISIBLE);
        emptyCollectionLayout.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyResults(String message) {
        mRefresh.setRefreshing(false);

        mRecyclerView.setVisibility(View.GONE);
        noNetworkLayout.setVisibility(View.GONE);

        emptyCollectionLayout.setText(message);
        emptyCollectionLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRetryCard(String message) {
        retry_msg.setText(message);
        retry_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void hideRetryCard() {
        retry_behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void collapseActionsCard() {
        actions_behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void halfExpandActionsCard(int position, String mode) {
        Log.d(TAG, "halfExpandActionsCard...");

        actions_behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

        if (position != -1)
            mPresenter.setRingtoneObject(position);
    }

    @Override
    public void expandActionsCard() {
        actions_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public class MyItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            // only for the last one
            outRect.bottom = 16;
            outRect.left = 16;
        }
    }
}
