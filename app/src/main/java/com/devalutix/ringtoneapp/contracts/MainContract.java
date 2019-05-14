package com.devalutix.ringtoneapp.contracts;

import com.devalutix.ringtoneapp.base.BasePresenter;
import com.devalutix.ringtoneapp.base.BaseView;
import com.devalutix.ringtoneapp.pojo.Category;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.ui.activities.MainActivity;
import com.devalutix.ringtoneapp.utils.GDPR;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public interface MainContract {

    interface Presenter extends BasePresenter<View> {

        void requestPermission(String writeExternalStorage, int requestWriteStorage);

        void initGDPR(AdView ad);

        GDPR getGDPR();

        void initViewPager();

        void initRecyclerView();

        void updateAll();

        ArrayList<Category> getCategories();

        ArrayList<Ringtone> getPopular();

        ArrayList<Ringtone> getRecent();

        String getUserName();

        int dipToPx(int dp);
    }

    interface View extends BaseView {

        void initUI();

        void initViewPager(ArrayList<Category> categories);

        void initRecyclerView(ArrayList<Ringtone> ringtones, String mode);

        void initAdBanner();

        void initSearchBar();

        void showCategories();

        void showNoCatgeories();

        void showRingtones();

        void hideRingtones();
    }
}
