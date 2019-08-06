package com.devalutix.ringtoneapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.MainContract;
import com.devalutix.ringtoneapp.di.components.DaggerMVPComponent;
import com.devalutix.ringtoneapp.di.components.MVPComponent;
import com.devalutix.ringtoneapp.di.modules.ApplicationModule;
import com.devalutix.ringtoneapp.di.modules.MVPModule;
import com.devalutix.ringtoneapp.pojo.Category;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.presenters.MainPresenter;
import com.devalutix.ringtoneapp.ui.adapters.CategoriesPagerAdapter;
import com.devalutix.ringtoneapp.ui.adapters.RingtonesAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements MainContract.View {
    private static String TAG = "MainActivity";
    private static final int REQUEST_WRITE_STORAGE = 1;

    /***************************************** Declarations ***************************************/
    private MVPComponent mvpComponent;
    @Inject
    MainPresenter mPresenter;
    private CategoriesPagerAdapter mAdapter;
    private RingtonesAdapter popularRingtonesAdapter;
    private RingtonesAdapter recentRingtonesAdapter;
    private BottomSheetBehavior actions_behavior;
    private BottomSheetBehavior retry_behavior;
    private boolean doubleBackToExitPressedOnce;

    /***************************************** View Declarations **********************************/
    //Main
    @BindView(R.id.top_bar)
    ConstraintLayout top_bar;
    @BindView(R.id.user_welcome)
    TextView user_welcome_text;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.drawer_menu)
    ImageButton drawerTrigger;
    @BindView(R.id.search)
    SearchView search;

    //Content
    @BindView(R.id.categories_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.popular_recyclerview)
    RecyclerView popularRecyclerView;
    @BindView(R.id.recent_recyclerview)
    RecyclerView recentRecyclerView;

    //No Content Layout
    @BindView(R.id.no_categories)
    ImageView noCategoriesLayout;
    @BindView(R.id.no_popular_ringtones)
    ImageView noPopularLayout;
    @BindView(R.id.no_recent_ringtones)
    ImageView noRecentLayout;

    //Actions Card
    @BindView(R.id.actions_card)
    CardView actions_card;

    //Retry Card
    @BindView(R.id.retry_card)
    ConstraintLayout retry_card;
    @BindView(R.id.retry_msg)
    TextView retry_msg;

    /***************************************** ClickListeners *************************************/
    //Main
    @OnClick(R.id.drawer_menu)
    void openDrawer() {
        mDrawer.openDrawer(GravityCompat.START);
    }

    @OnClick(R.id.add)
    void goToProfile() {
        startActivity(new Intent(this, CreateOneActivity.class));
    }

    //See All
    @OnClick(R.id.see_all_popular)
    void goToPopularRingtones() {
        Intent i = new Intent(this, RingtonesActivity.class);
        i.putExtra("mode", "ringtones");
        i.putExtra("name", "popular");
        startActivity(i);
    }

    @OnClick(R.id.see_all_recent)
    void goToRecentRingtones() {
        Intent i = new Intent(this, RingtonesActivity.class);
        i.putExtra("mode", "ringtones");
        i.putExtra("name", "recent");
        startActivity(i);
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

    //Retry
    @OnClick(R.id.retry)
    public void retry() {
        refresh();
    }

    /*********************************** Essential Methods ****************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Creating Views");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Dagger For Application
        mvpComponent = getComponent();

        //Inject the Component Here
        mvpComponent.inject(this);

        //Set ButterKnife
        ButterKnife.bind(this);

        //Attach View To Presenter
        mPresenter.attach(this);

        //Request Storage Permission
        mPresenter.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_STORAGE);

        //Init UI
        initUI();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.click_back_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    /********************************************* Methods ****************************************/
    @Override
    public void initUI() {
        Log.d(TAG, "initUI: Initializing the UI");

        //Set User Welcome Text
        String welcome_text = "Welcome Mr " + mPresenter.getUserName();
        user_welcome_text.setText(welcome_text);

        //Init Category ViewPager
        mPresenter.initViewPager();

        //Init Popular/Recent Ringtone RecyclerView
        mPresenter.initRecyclerView();

        //Init Search Bar
        initSearchBar();

        //Init Actions Popup
        initActionsPopUp();

        //Init Retry Card
        initRetrySheet();

        //Bring top bar to front
        top_bar.bringToFront();
    }

    @Override
    public void initViewPager(ArrayList<Category> categories) {
        mAdapter = new CategoriesPagerAdapter(this, categories);
        mViewPager.setPageMargin(mPresenter.dipToPx(16));
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public void initRecyclerView(ArrayList<Ringtone> ringtones, String mode) {
        //Declarations
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        switch (mode) {
            case "popular": {
                popularRingtonesAdapter = new RingtonesAdapter(ringtones, this, mode);
                popularRecyclerView.setLayoutManager(mLayoutManager);
                popularRecyclerView.addItemDecoration(new MyItemDecoration());
                popularRecyclerView.setAdapter(popularRingtonesAdapter);
            }
            break;
            case "recent": {
                recentRingtonesAdapter = new RingtonesAdapter(ringtones, this, mode);
                recentRecyclerView.setLayoutManager(mLayoutManager);
                recentRecyclerView.addItemDecoration(new MyItemDecoration());
                recentRecyclerView.setAdapter(recentRingtonesAdapter);
            }
            break;
        }
    }

    @Override
    public void updateViewPager(ArrayList<Category> categories) {
        mAdapter = new CategoriesPagerAdapter(this, categories);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public void updateRecyclerView(ArrayList<Ringtone> ringtones, String mode) {
        switch (mode) {
            case "recent": {
                recentRingtonesAdapter.clearAll();
                recentRingtonesAdapter.addAll(ringtones);
            }
            break;
            case "popular": {
                popularRingtonesAdapter.clearAll();
                popularRingtonesAdapter.addAll(ringtones);
            }
            break;
        }
    }

    @Override
    public void initActionsPopUp() {
        actions_behavior = BottomSheetBehavior.from(actions_card);
        actions_behavior.setHalfExpandedRatio(0.09f);

        actions_behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                dimBackground(slideOffset);
            }
        });

        collapseActionsCard();
    }

    @Override
    public void initRetrySheet() {
        retry_behavior = BottomSheetBehavior.from(retry_card);
        hideRetryCard();
    }

    @Override
    public void initSearchBar() {
        EditText searchEditText = search.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.colorAccent));
        searchEditText.setHintTextColor(getResources().getColor(R.color.colorAccent));

        search.setOnSearchClickListener(v -> drawerTrigger.setVisibility(View.GONE));

        search.setOnCloseListener(() -> {
            drawerTrigger.setVisibility(VISIBLE);
            return false;
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mPresenter.searchRingtone(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void showCategories() {
        noCategoriesLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(VISIBLE);
    }

    @Override
    public void showNoCatgeories() {
        noCategoriesLayout.setVisibility(VISIBLE);
        mViewPager.setVisibility(View.GONE);
    }

    @Override
    public void showRingtones() {
        noPopularLayout.setVisibility(View.GONE);
        popularRecyclerView.setVisibility(VISIBLE);
        noRecentLayout.setVisibility(View.GONE);
        recentRecyclerView.setVisibility(VISIBLE);

    }

    @Override
    public void showNoRingtones() {
        noPopularLayout.setVisibility(VISIBLE);
        popularRecyclerView.setVisibility(View.GONE);
        noRecentLayout.setVisibility(VISIBLE);
        recentRecyclerView.setVisibility(View.GONE);
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

        if (position != -1 && mode != null)
            mPresenter.setRingtoneObject(mode, position);
    }

    @Override
    public void expandActionsCard() {
        actions_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void dimBackground(float factor) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = factor * 0.75f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(layoutParams);
    }

    @Override
    public void refresh() {
        hideRetryCard();
        /// mRefresh.setRefreshing(true);
        mPresenter.updateAll();
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
