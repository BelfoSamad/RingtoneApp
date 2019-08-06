package com.devalutix.ringtoneapp.presenters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.CreateOneContract;
import com.devalutix.ringtoneapp.ui.activities.CreateOneActivity;
import com.devalutix.ringtoneapp.utils.SamplePlayer;
import com.devalutix.ringtoneapp.utils.SoundFile;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class CreateOnePresenter implements CreateOneContract.Presenter {
    private static final String TAG = "CreateOnePresenter";
    private static final int REQUEST_CODE_CHOOSE_CONTACT = 1;

    /************************************** Declarations ******************************************/
    private CreateOneActivity mView;
    private SamplePlayer mPlayer;
    private SoundFile mSoundFile;
    private Handler mHandler;
    private File mFile;
    private Thread mLoadSoundFileThread;
    private Thread mSaveSoundFileThread;
    private boolean mIsPlaying;
    private String mTitle;

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
        String mFilename = getRealPathFromURI(mView, uri);
        //Set Song Title
        setName(mFilename);
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

    @Override
    public void setRingtone(int startFrame, int endFrame, int duration,
                            boolean isRingtone,
                            boolean isNotification, boolean isAlarm, float factor) {
        mSaveSoundFileThread = new Thread() {
            public void run() {
                // Try AAC first.
                String outPath = makeRingtoneFilename(mTitle, ".m4a");
                if (outPath == null) {
                    Runnable runnable = () -> Toast.makeText(mView, mView.getString(R.string.no_unique_filename), Toast.LENGTH_SHORT).show();
                    mHandler.post(runnable);
                    return;
                }
                File outFile = new File(outPath);
                Boolean fallbackToWAV = false;
                try {
                    // Write the new file
                    mSoundFile.WriteFile(outFile, startFrame, endFrame - startFrame, factor);
                } catch (Exception e) {
                    // log the error and try to create a .wav file instead
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    StringWriter writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    Log.e("Ringdroid", "Error: Failed to create " + outPath);
                    Log.e("Ringdroid", writer.toString());
                    fallbackToWAV = true;
                }

                // Try to create a .wav file if creating a .m4a file failed.
                if (fallbackToWAV) {
                    outPath = makeRingtoneFilename(mTitle, ".wav");
                    if (outPath == null) {
                        Runnable runnable = () -> Toast.makeText(mView, mView.getString(R.string.no_unique_filename), Toast.LENGTH_SHORT).show();
                        mHandler.post(runnable);
                        return;
                    }
                    outFile = new File(outPath);
                    try {
                        // create the .wav file
                        mSoundFile.WriteWAVFile(outFile, startFrame, endFrame - startFrame, factor);
                    } catch (Exception e) {

                        // Creating the .wav file also failed. Stop the progress dialog, show an
                        // error message and exit.
                        mView.dismissProgressDialog();

                        if (outFile.exists()) {
                            outFile.delete();
                        }

                        CharSequence errorMessage;
                        if (e.getMessage() != null
                                && e.getMessage().equals("No space left on device")) {
                            errorMessage = mView.getResources().getText(R.string.no_space_error);
                            e = null;
                        } else {
                            errorMessage = mView.getResources().getText(R.string.write_error);
                        }
                        final CharSequence finalErrorMessage = errorMessage;

                        Runnable runnable = () -> Toast.makeText(mView, finalErrorMessage, Toast.LENGTH_SHORT).show();
                        mHandler.post(runnable);
                        return;
                    }
                }

                // Try to load the new file to make sure it worked
                try {
                    final SoundFile.ProgressListener listener =
                            frac -> {
                                // Do nothing - we're not going to try to
                                // estimate when reloading a saved sound
                                // since it's usually fast, but hard to
                                // estimate anyway.
                                return true;  // Keep going
                            };
                    SoundFile.create(outPath, listener);
                } catch (final Exception e) {
                    mView.dismissProgressDialog();
                    e.printStackTrace();

                    Runnable runnable = () -> Toast.makeText(mView, mView.getString(R.string.write_error), Toast.LENGTH_SHORT).show();
                    mHandler.post(runnable);
                    return;
                }

                mView.dismissProgressDialog();

                final String finalOutPath = outPath;
                Runnable runnable = () -> afterSavingRingtone(mTitle,
                        finalOutPath,
                        duration, isRingtone, isNotification, isAlarm);
                mHandler.post(runnable);
            }
        };
        mSaveSoundFileThread.start();
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

    private String makeRingtoneFilename(CharSequence title, String extension) {

        String externalRootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getPath();

        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }

        String parentdir = externalRootDir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        parentDirFile.mkdirs();

        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory()) {
            parentdir = externalRootDir;
        }

        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }

        // Try to make the filename unique
        String path = null;
        for (int i = 0; i < 100; i++) {
            String testPath;
            if (i > 0)
                testPath = parentdir + filename + i + extension;
            else
                testPath = parentdir + filename + extension;

            try {
                RandomAccessFile f = new RandomAccessFile(new File(testPath), "r");
                f.close();
            } catch (Exception e) {
                // Good, the file didn't exist
                path = testPath;
                break;
            }
        }

        return path;
    }

    private void setName(String mFilename) {
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(mFilename);
        Cursor c = mView.getContentResolver().query(
                uri,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.DATA + " LIKE \"" + mFilename + "\"",
                null, null);
        if (c.getCount() == 0) {
            mTitle = getBasename(mFilename);
            return;
        }
        c.moveToFirst();
        mTitle = getStringFromColumn(c, MediaStore.Audio.Media.TITLE);
        if (mTitle == null || mTitle.length() == 0) {
            mTitle = getBasename(mFilename);
        }
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        mTitle = mTitle + "-" + n;
    }

    private String getBasename(String filename) {
        return filename.substring(filename.lastIndexOf('/') + 1,
                filename.lastIndexOf('.'));
    }

    private String getStringFromColumn(Cursor c, String columnName) {
        int index = c.getColumnIndexOrThrow(columnName);
        String value = c.getString(index);
        if (value != null && value.length() > 0) {
            return value;
        } else {
            return null;
        }
    }

    private void afterSavingRingtone(CharSequence title,
                                     String outPath,
                                     int duration,
                                     boolean isRingtone,
                                     boolean isNotification, boolean isAlarm) {
        File outFile = new File(outPath);
        long fileSize = outFile.length();
        if (fileSize <= 512) {
            outFile.delete();
            Toast.makeText(mView, "Too Small File", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the database record, pointing to the existing file path
        String mimeType;
        if (outPath.endsWith(".m4a")) {
            mimeType = "audio/mp4a-latm";
        } else if (outPath.endsWith(".wav")) {
            mimeType = "audio/wav";
        } else {
            // This should never happen.
            mimeType = "audio/mpeg";
        }

        String artist = "" + mView.getResources().getText(R.string.app_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outPath);
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, duration);

        values.put(MediaStore.Audio.Media.IS_RINGTONE,isRingtone);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION,isNotification);
        values.put(MediaStore.Audio.Media.IS_ALARM,isAlarm);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        // Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outPath);
        final Uri newUri = mView.getContentResolver().insert(uri, values);
        mView.setResult(RESULT_OK, new Intent().setData(newUri));

        // There's nothing more to do with music or an alarm.  Show a
        // success message and then quit.
        if (!isRingtone && !isNotification) {
            Toast.makeText(mView, "Done", Toast.LENGTH_SHORT).show();
            mView.onBackPressed();
            return;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(mView)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + mView.getPackageName()));
                mView.startActivity(intent);
            }
        }

        // If it's a notification, give the user the option of making
        // this their default notification.  If they say no, we're finished.
        if (isNotification) {
            new AlertDialog.Builder(mView)
                    .setTitle(R.string.alert_title_success)
                    .setMessage(R.string.set_default_notification)
                    .setPositiveButton(R.string.alert_yes_button,
                            (dialog, whichButton) -> {
                                RingtoneManager.setActualDefaultRingtoneUri(
                                        mView,
                                        RingtoneManager.TYPE_NOTIFICATION,
                                        newUri);
                                mView.onBackPressed();
                            })
                    .setNegativeButton(
                            R.string.alert_no_button,
                            (dialog, whichButton) -> mView.onBackPressed())
                    .setCancelable(false)
                    .show();
            return;
        }

        // If we get here, that means the type is a ringtone.  There are
        // three choices: make this your default ringtone, assign it to a
        // contact, or do nothing.

        if (isRingtone){
            RingtoneManager.setActualDefaultRingtoneUri(
                    mView,
                    RingtoneManager.TYPE_RINGTONE,
                    newUri);
            Toast.makeText(
                    mView,
                    mView.getString(R.string.default_ringtone_success_message),
                    Toast.LENGTH_SHORT)
                    .show();
            mView.onBackPressed();
        }
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
