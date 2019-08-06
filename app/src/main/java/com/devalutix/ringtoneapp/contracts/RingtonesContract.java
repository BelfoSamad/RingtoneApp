package com.devalutix.ringtoneapp.contracts;

import com.devalutix.ringtoneapp.base.BasePresenter;
import com.devalutix.ringtoneapp.base.BaseView;
import com.devalutix.ringtoneapp.pojo.Ringtone;

import java.util.ArrayList;

public interface RingtonesContract {

    interface Presenter extends BasePresenter<View> {

        void initRecyclerView(String name);

        void updateRecyclerView(String name);

        void saveRingtone(String type);

        void shareRingtone();

        void setAsRingtone();

        void setAsNotification();

        void setAsAlarm();

        void setRingtoneObject(int position);

        void setMode(String mode);

        String getMode();
    }

    interface View extends BaseView {

        void setToolbar();

        void setPageName(String mode, String name);

        void initRecyclerView(ArrayList<Ringtone> ringtones);

        void initActionsPopUp();

        void initRetrySheet();

        void updateRecyclerView(ArrayList<Ringtone> ringtones);

        void showRingtones();

        void showNoRingtone();

        void showEmptyResults(String string);

        void showRetryCard(String message);

        void collapseActionsCard();

        void halfExpandActionsCard(int position, String mode);

        void expandActionsCard();

        void hideRetryCard();
    }
}
