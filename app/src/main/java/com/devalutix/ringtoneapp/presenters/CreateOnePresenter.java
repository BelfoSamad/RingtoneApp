package com.devalutix.ringtoneapp.presenters;

import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.CreateOneContract;
import com.devalutix.ringtoneapp.ui.activities.CreateOneActivity;
import com.devalutix.ringtoneapp.utils.SamplePlayer;
import com.devalutix.ringtoneapp.utils.SoundFile;

import java.io.File;

public class CreateOnePresenter implements CreateOneContract.Presenter {

    /************************************** Declarations ******************************************/
    private CreateOneActivity mView;
    private SamplePlayer mPlayer;
    private boolean mIsPlaying;
    private Thread mLoadSoundFileThread;
    private Thread mRecordAudioThread;
    private Thread mSaveSoundFileThread;
    private SoundFile mSoundFile;
    private Handler mHandler;
    private File mFile;

    /************************************** Constructor *******************************************/
    public CreateOnePresenter() {
    }

    /********************************** Essential Methods *****************************************/
    @Override
    public void attach(CreateOneContract.View view) {
        mView = (CreateOneActivity) view;
    }

    @Override
    public void dettach() {
        mView = null;
    }

    @Override
    public boolean isAttached() {
        return !(mView == null);
    }

    /************************************** Methods ***********************************************/
    @Override
    public void loadFile(Uri uri) {
        mPlayer = null;
        mIsPlaying = false;

        mLoadSoundFileThread = null;
        mRecordAudioThread = null;
        mSaveSoundFileThread = null;

        mSoundFile = null;

        mHandler = new Handler();

        mView.loadGUI();

        String mFilename = uri.toString().replaceFirst("file://", "").replaceAll("%20", " ");

        //mHandler.postDelayed(mTimerRunnable, 100);

        mFile = new File(mFilename);

        SoundFile.ProgressListener listener = mView.initProgressDialog();

        // Load the sound file in a background thread
        mLoadSoundFileThread = new Thread() {
            public void run() {
                try {
                    mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);

                    if (mSoundFile == null) {
                        mView.dismissProgressDialog();
                        String name = mFile.getName().toLowerCase();
                        String[] components = name.split("\\.");
                        String err;
                        if (components.length < 2) {
                            err = mView.getResources().getString(
                                    R.string.no_extension_error);
                        } else {
                            err = mView.getResources().getString(
                                    R.string.bad_extension_error) + " " +
                                    components[components.length - 1];
                        }
                        final String finalErr = err;
                        Runnable runnable = () -> Toast.makeText(mView, finalErr, Toast.LENGTH_SHORT).show();
                        mHandler.post(runnable);
                        return;
                    }
                    mPlayer = new SamplePlayer(mSoundFile);
                } catch (final Exception e) {
                    mView.dismissProgressDialog();
                    e.printStackTrace();
                    String mInfoContent = e.toString();

                    //                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            mView.setInfo(mInfoContent);
//                        }
//                    });

                    Runnable runnable = () -> Toast.makeText(mView, mView.getResources().getText(R.string.read_error), Toast.LENGTH_SHORT).show();
                    mHandler.post(runnable);
                    return;
                }
                mView.dismissProgressDialog();
                if (mView.getmLoadingKeepGoing()) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            mView.finishOpeningSoundFile(mSoundFile);
                        }
                    };
                    mHandler.post(runnable);
                } else if (mView.getmFinishActivity()){
                    mView.finish();
                }
            }
        };
        mLoadSoundFileThread.start();
    }

    @Override
    public synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            // Not initialized yet
            return;
        }

        try {
            mView.playingSound(startPosition);
        } catch (Exception e) {
            Toast.makeText(mView, mView.getResources().getString(R.string.play_error), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mView.disableWaveform();
        mIsPlaying = false;
        mView.enableDisableButtons();
    }

    @Override
    public boolean isPlaying() {
        return mIsPlaying;
    }

    @Override
    public SoundFile getSoundFile() {
        return null;
    }

    @Override
    public SamplePlayer getPlayer() {
        return null;
    }

    @Override
    public long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    @Override
    public String formatDecimal(double x) {
        int xWhole = (int)x;
        int xFrac = (int)(100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    @Override
    public void setPlaying(boolean playing) {
        mIsPlaying = playing;
    }

    @Override
    public Handler getHandler(){
        return mHandler;
    }
}
