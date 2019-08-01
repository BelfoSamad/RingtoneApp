package com.devalutix.ringtoneapp.presenters;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.CreateOneContract;
import com.devalutix.ringtoneapp.ui.activities.CreateOneActivity;
import com.devalutix.ringtoneapp.utils.SamplePlayer;
import com.devalutix.ringtoneapp.utils.SoundFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class CreateOnePresenter implements CreateOneContract.Presenter {
    private static final String TAG = "CreateOnePresenter";

    /************************************** Declarations ******************************************/
    private CreateOneActivity mView;
    private SamplePlayer mPlayer;
    private SoundFile mSoundFile;
    private Handler mHandler;
    private File mFile;
    private Thread mLoadSoundFileThread;
    private Thread mSaveSoundFileThread;
    private boolean mIsPlaying;

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

        //Init
        mPlayer = null;
        mIsPlaying = false;
        mLoadSoundFileThread = null;
        mSaveSoundFileThread = null;
        mSoundFile = null;
        //TODO: Key Down
        mHandler = new Handler();

        //Load UI
        mView.loadGUI();

        mHandler.postDelayed(mTimerRunnable, 100);
        //.replaceFirst("file://", "").replaceAll("%20", " ");
        String mFilename = getRealPathFromURI(mView, uri);
        Log.d(TAG, "loadFile: File Name = " + mFilename);
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
                        Log.d(TAG, "run: File Name = " + name);
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
                    Runnable runnable = () -> mView.finishOpeningSoundFile(mSoundFile);
                    mHandler.post(runnable);
                } else if (mView.getmFinishActivity()) {
                    mView.finish();
                }
            }
        };
        mLoadSoundFileThread.start();
    }

    private static String getRealPathFromURI(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
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
    public void setPlaying(boolean playing) {
        mIsPlaying = playing;
    }

    @Override
    public boolean isPlaying() {
        return mIsPlaying;
    }

    @Override
    public SoundFile getSoundFile() {
        return mSoundFile;
    }

    @Override
    public SamplePlayer getPlayer() {
        return mPlayer;
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    /**************************************** Tools ***********************************************/
    @Override
    public long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    @Override
    public String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

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

    /*************************************** Others ***********************************************/
    private Runnable mTimerRunnable = new Runnable() {
        public void run() {

            mView.updateEditText();

            mHandler.postDelayed(mTimerRunnable, 100);
        }
    };

    private void closeThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
