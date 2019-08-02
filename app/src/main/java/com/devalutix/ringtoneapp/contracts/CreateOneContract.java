package com.devalutix.ringtoneapp.contracts;

import android.net.Uri;
import android.os.Handler;

import com.devalutix.ringtoneapp.base.BasePresenter;
import com.devalutix.ringtoneapp.base.BaseView;
import com.devalutix.ringtoneapp.utils.SamplePlayer;
import com.devalutix.ringtoneapp.utils.SoundFile;

public interface CreateOneContract {

    interface Presenter extends BasePresenter<CreateOneContract.View> {

        void loadFile(Uri uri);

        void onPlay(int mStartPos);

        void handlePause();

        boolean isPlaying();

        SoundFile getSoundFile();

        SamplePlayer getPlayer();

        long getCurrentTime();

        String formatDecimal(double pixelsToSeconds);

        void setPlaying(boolean playing);

        Handler getHandler();

        void setRingtone(int startFrame, int endFrame, int duration,
                         boolean isRingtone,
                         boolean isNotification, boolean isAlarm);
    }

    interface View extends BaseView {

        void loadGUI();

        SoundFile.ProgressListener initProgressDialog();

        void showProgressDialog(int message);

        void dismissProgressDialog();

        boolean getmLoadingKeepGoing();

        boolean getmFinishActivity();

        void setInfo(String mInfoContent);

        void finishOpeningSoundFile(SoundFile mSoundFile);

        void playingSound(int startPosition);

        void disableWaveform();

        void enableDisableButtons();

        void updateEditText();
    }

}
