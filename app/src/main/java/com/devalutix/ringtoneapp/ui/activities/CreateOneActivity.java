package com.devalutix.ringtoneapp.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.CreateOneContract;
import com.devalutix.ringtoneapp.di.components.DaggerMVPComponent;
import com.devalutix.ringtoneapp.di.components.MVPComponent;
import com.devalutix.ringtoneapp.di.modules.ApplicationModule;
import com.devalutix.ringtoneapp.di.modules.MVPModule;
import com.devalutix.ringtoneapp.presenters.CreateOnePresenter;
import com.devalutix.ringtoneapp.ui.custom.MarkerView;
import com.devalutix.ringtoneapp.ui.custom.WaveformView;
import com.devalutix.ringtoneapp.utils.SoundFile;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateOneActivity extends AppCompatActivity implements CreateOneContract.View, MarkerView.MarkerListener,
        WaveformView.WaveformListener {

    /***************************************** Declarations ***************************************/
    private MVPComponent mvpComponent;
    @Inject
    CreateOnePresenter mPresenter;
    private float mDensity;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private int mMarkerBottomOffset;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private int mMaxPos;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private String mCaption = "";
    private int mStartPos;
    private int mEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mWidth;
    private boolean mTouchDragging;
    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private ProgressDialog mProgressDialog;
    private boolean mFinishActivity;
    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private long mWaveformTouchStartMsec;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private boolean mKeyDown;

    /***************************************** View Declarations **********************************/
    @BindView(R.id.waveform)
    WaveformView mWaveformView;
    @BindView(R.id.starttext)
    TextView mStartText;
    @BindView(R.id.endtext)
    TextView mEndText;
    @BindView(R.id.play)
    ImageButton mPlayButton;
    @BindView(R.id.mark_start)
    TextView markStartButton;
    @BindView(R.id.mark_end)
    TextView markEndButton;
    @BindView(R.id.startmarker)
    MarkerView mStartMarker;
    @BindView(R.id.endmarker)
    MarkerView mEndMarker;
    @BindView(R.id.info)
    TextView mInfo;

    /***************************************** ClickListeners *************************************/

    /*********************************** Essential Methods ****************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize Dagger For Application
        mvpComponent = getComponent();

        //Inject the Component Here
        mvpComponent.inject(this);

        //Attach View To Presenter
        mPresenter.attach(this);

        //Pick File
        pickFile();
    }

    private void pickFile() {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                mPresenter.loadFile(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    /********************************************* Methods ****************************************/
    @Override
    public void loadGUI() {
        setContentView(R.layout.activity_create_one);
        ButterKnife.bind(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;

        mMarkerLeftInset = (int) (46 * mDensity);
        mMarkerRightInset = (int) (48 * mDensity);
        mMarkerTopOffset = (int) (10 * mDensity);
        mMarkerBottomOffset = (int) (10 * mDensity);

        mStartText.addTextChangedListener(mTextWatcher);
        mEndText.addTextChangedListener(mTextWatcher);
        mPlayButton.setOnClickListener(mPlayListener);
        markStartButton.setOnClickListener(mMarkStartListener);
        markEndButton.setOnClickListener(mMarkEndListener);

        enableDisableButtons();

        mWaveformView.setListener(this);
        mInfo.setText(mCaption);

        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        if (mPresenter.getSoundFile() != null && !mWaveformView.hasSoundFile()) {
            mWaveformView.setSoundFile(mPresenter.getSoundFile());
            mWaveformView.recomputeHeights(mDensity);
            mMaxPos = mWaveformView.maxPos();
        }

        mStartMarker.setListener(this);
        mStartMarker.setAlpha(1f);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = true;

        updateDisplay();
    }

    @Override
    public void enableDisableButtons() {
        if (mPresenter.isPlaying()) {
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            mPlayButton.setContentDescription(getResources().getText(R.string.stop));
        } else {
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            mPlayButton.setContentDescription(getResources().getText(R.string.play));
        }
    }

    private synchronized void updateDisplay() {
        if (mPresenter.isPlaying()) {
            int now = mPresenter.getPlayer().getCurrentPosition();
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                mPresenter.handlePause();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }
    }

    @Override
    public SoundFile.ProgressListener initProgressDialog() {
        mLoadingLastUpdateTime = mPresenter.getCurrentTime();
        mLoadingKeepGoing = true;
        mFinishActivity = false;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(
                dialog -> {
                    mLoadingKeepGoing = false;
                    mFinishActivity = true;
                });
        mProgressDialog.show();

        final SoundFile.ProgressListener listener =
                fractionComplete -> {
                    long now = mPresenter.getCurrentTime();
                    if (now - mLoadingLastUpdateTime > 100) {
                        mProgressDialog.setProgress(
                                (int) (mProgressDialog.getMax() * fractionComplete));
                        mLoadingLastUpdateTime = now;
                    }
                    return mLoadingKeepGoing;
                };

        return listener;
    }

    @Override
    public void showProgressDialog() {
        mProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public boolean getmLoadingKeepGoing() {
        return mLoadingKeepGoing;
    }

    @Override
    public boolean getmFinishActivity() {
        return mFinishActivity;
    }

    @Override
    public void setInfo(String mInfoContent) {
        mInfo.setText(mInfoContent);
    }

    @Override
    public void finishOpeningSoundFile(SoundFile mSoundFile) {
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        resetPositions();
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;

        mCaption =
                mSoundFile.getFiletype() + ", " +
                        mSoundFile.getSampleRate() + " Hz, " +
                        mSoundFile.getAvgBitrateKbps() + " kbps, " +
                        formatTime(mMaxPos) + " " +
                        getResources().getString(R.string.time_seconds);
        mInfo.setText(mCaption);

        updateDisplay();
    }

    @Override
    public void playingSound(int startPosition) {
        mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
        if (startPosition < mStartPos) {
            mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
        } else if (startPosition > mEndPos) {
            mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
        } else {
            mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
        }
        mPresenter.getPlayer().setOnCompletionListener(() -> mPresenter.handlePause());
        mPresenter.setPlaying(true);

        mPresenter.getPlayer().seekTo(mPlayStartMsec);
        mPresenter.getPlayer().start();
        updateDisplay();
        enableDisableButtons();
    }

    @Override
    public void disableWaveform() {
        mWaveformView.setPlayback(-1);
    }

    /***************************************** Wave Form ******************************************/
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mPresenter.isPlaying()) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = mPresenter.getCurrentTime();
    }

    public void waveformTouchMove(float x) {
        mOffset = trap((int)(mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = mPresenter.getCurrentTime() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mPresenter.isPlaying()) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int)(mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                        seekMsec < mPlayEndMsec) {
                    mPresenter.getPlayer().seekTo(seekMsec);
                } else {
                    mPresenter.handlePause();
                }
            } else {
                mPresenter.onPlay((int)(mTouchStart + mOffset));
            }
        }
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int)(-vx);
        updateDisplay();
    }

    public void waveformZoomIn() {
        mWaveformView.zoomIn();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        updateDisplay();
    }

    public void waveformZoomOut() {
        mWaveformView.zoomOut();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        updateDisplay();
    }

    /***************************************** Marker *********************************************/
    public void markerDraw() {
    }

    public void markerTouchStart(MarkerView marker, float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int)(mTouchInitialStartPos + delta));
            mEndPos = trap((int)(mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int)(mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerEnter(MarkerView marker) {
    }

    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mPresenter.getHandler().postDelayed(new Runnable() {
            public void run() {
                updateDisplay();
            }
        }, 100);
    }

    /***************************************** Listeners ******************************************/
    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        public void onClick(View sender) {
            mPresenter.onPlay(mStartPos);
        }
    };

    private View.OnClickListener mMarkStartListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mPresenter.isPlaying()) {
                mStartPos = mWaveformView.millisecsToPixels(
                        mPresenter.getPlayer().getCurrentPosition());
                updateDisplay();
            }
        }
    };

    private View.OnClickListener mMarkEndListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mPresenter.isPlaying()) {
                mEndPos = mWaveformView.millisecsToPixels(
                        mPresenter.getPlayer().getCurrentPosition());
                updateDisplay();
                mPresenter.handlePause();
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {
        }

        public void onTextChanged(CharSequence s,
                                  int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (mStartText.hasFocus()) {
                try {
                    mStartPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mStartText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
            if (mEndText.hasFocus()) {
                try {
                    mEndPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mEndText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
        }
    };

    /***************************************** Others *********************************************/
    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(15.0);
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return mPresenter.formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

}