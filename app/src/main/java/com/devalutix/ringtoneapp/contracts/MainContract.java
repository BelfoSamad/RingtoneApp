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

        void initViewPager();

        void initRecyclerView();

        void updateAll();

        void updateViewPager();

        void updateRecyclerView();

        ArrayList<Category> getCategories();

        ArrayList<Ringtone> getPopular();

        ArrayList<Ringtone> getRecent();

        String getUserName();

        void searchRingtone(String query);

        void saveRingtone();

        void shareRingtone();

        void setAsRingtone();

        void setAsNotification();

        void setAsContactRingtone();

        int dipToPx(int dp);

        void setCurrent(int position);

        void setMode(String mode);

        String getMode();

        int getPosition();

    }

    interface View extends BaseView {

        void initUI();

        void initViewPager(ArrayList<Category> categories);

        void initRecyclerView(ArrayList<Ringtone> ringtones, String mode);

        void updateViewPager(ArrayList<Category> categories);

        void updateRecyclerView(ArrayList<Ringtone> ringtones, String mode);

        void initActionsPopUp();

        void initRetrySheet();

        void initSearchBar();

        void showCategories();

        void showNoCatgeories();

        void showRingtones();

        void showNoRingtones();

        void showRetryCard(String message);

        void hideRetryCard();

        void collapseActionsCard();

        void halfExpandActionsCard(int position, String mode);

        void expandActionsCard();

        void refresh();
    }
}
