package com.devalutix.ringtoneapp.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import com.devalutix.ringtoneapp.utils.Config;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindFloat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainContract.View {
    private static String TAG = "MainActivity";
    private static final int REQUEST_WRITE_STORAGE = 1;

    //Declarations
    private MVPComponent mvpComponent;
    @Inject
    MainPresenter mPresenter;
    private CategoriesPagerAdapter mAdapter;
    private RingtonesAdapter popularRingtonesAdapter;
    private RingtonesAdapter recentRingtonesAdapter;
    private ArrayList<Ringtone> ringtones;

    //View Declarations
    @BindView(R.id.top_bar)
    ConstraintLayout top_bar;
    @BindView(R.id.user_welcome)
    TextView user_welcome_text;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.categories_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.popular_recyclerview)
    RecyclerView popularRecyclerView;
    @BindView(R.id.recent_recyclerview)
    RecyclerView recentRecyclerView;
    @BindView(R.id.adView_main)
    AdView ad;

    //Listeners
    @OnClick(R.id.drawer_menu)
    void openDrawer() {
        mDrawer.openDrawer(GravityCompat.START);
    }

    @OnClick(R.id.see_all_popular)
    void goToPopularRingtones() {
        Intent i = new Intent(this, RingtonesActivity.class);
        i.putExtra("mode", "popular");

        startActivity(i);
    }

    @OnClick(R.id.see_all_recent)
    void goToRecentRingtones() {
        Intent i = new Intent(this, RingtonesActivity.class);
        i.putExtra("mode", "recent");

        startActivity(i);
    }

    @OnClick(R.id.profile)
    void goToProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    //Essential Methods
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
        AdView ad1;
        if (Config.ENABLE_AD_BANNER) {
            if (Config.ENABLE_GDPR) {
                //If Gdpr enabled get the Variable banner ad from the Class Gdpr
                //else use the current one
                ad1 = mPresenter.getGDPR().getmAd();
            } else ad1 = ad;
            ad1.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdView ad1;
        if (Config.ENABLE_AD_BANNER) {
            if (Config.ENABLE_GDPR) {
                //If Gdpr enabled get the Variable banner ad from the Class Gdpr
                //else use the current one
                ad1 = mPresenter.getGDPR().getmAd();
            } else ad1 = ad;
            ad1.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdView ad1;
        if (Config.ENABLE_AD_BANNER) {
            if (Config.ENABLE_GDPR) {
                //If Gdpr enabled get the Variable banner ad from the Class Gdpr
                //else use the current one
                ad1 = mPresenter.getGDPR().getmAd();
            } else ad1 = ad;
            ad1.resume();
        }
    }

    @Override
    public void initUI() {
        Log.d(TAG, "initUI: Initializing the UI");

        //Init Ad Banner
        initAdBanner();

        //Set User Welcome Text
        user_welcome_text.setText("Welcome Mr " + mPresenter.getUserName() + ",");

        //Init Category ViewPager
        mPresenter.initViewPager();

        //Init Popular/Recent Ringtones RecyclerView
        mPresenter.initRecyclerView();

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


        //Set the List
        this.ringtones = ringtones;

        //Declarations
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        switch (mode) {
            case "popular": {
                popularRingtonesAdapter = new RingtonesAdapter(ringtones, this);
                popularRecyclerView.setLayoutManager(mLayoutManager);
                popularRecyclerView.addItemDecoration(new MyItemDecoration());
                popularRecyclerView.setAdapter(popularRingtonesAdapter);
            }
            break;
            case "recent": {
                recentRingtonesAdapter = new RingtonesAdapter(ringtones, this);
                recentRecyclerView.setLayoutManager(mLayoutManager);
                recentRecyclerView.addItemDecoration(new MyItemDecoration());
                recentRecyclerView.setAdapter(recentRingtonesAdapter);
            }
            break;
        }


    }

    @Override
    public void initAdBanner() {

    }

    @Override
    public void initSearchBar() {

    }

    @Override
    public void showCategories() {

    }

    @Override
    public void showNoCatgeories() {

    }

    @Override
    public void showRingtones() {

    }

    @Override
    public void hideRingtones() {

    }

    public class MyItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // only for the last one
            outRect.bottom = 16;
            outRect.left = 16;
        }
    }
}
