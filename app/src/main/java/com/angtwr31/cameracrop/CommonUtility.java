package com.angtwr31.cameracrop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TableRow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.angtwr31.cameracrop.R;

public class CommonUtility {

    private static final String PREFS_NAME = "Camera&Crop";
    private static final String TAG = "CommonUtility";

    public static void disableDoubleClick(final View view) {
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 400);
    }

    // Method to share either text or URL.
    public static void shareTextUrl(Context context, String subject, String textMessage) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, subject);
        share.putExtra(Intent.EXTRA_TEXT, textMessage);
        context.startActivity(Intent.createChooser(share, "Share"));
    }

    // validating email id
    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // validating password with retype password
    public static boolean isValidPassword(String pass) {
        return pass != null && pass.length() >= 8;
    }

    //validate username...which is continues alphabets only
    public static boolean isValidUsername(String username) {
        return Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*$", username);
    }

    public static boolean isConnectingToInternet(Context context) {
        Context _context = context;
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void showAlertDialog(Context context, String msg) {
        final AlertDialog.Builder saveDialog = new AlertDialog.Builder(context);
        saveDialog.setCancelable(false);
        saveDialog.setMessage(msg);
        saveDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        saveDialog.show();
    }

    /**
     * preferences methods (getter and setter)
     *
     * @param context
     * @param name
     * @param value
     */
    public static void setApplicationPreferences(Context context, String name,
                                                 String value) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        // Commit the edits!
        editor.commit();
    }

    public static void setApplicationPreferencesBooleanValue(Context context, String name,
                                                             boolean value) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        // Commit the edits!
        editor.commit();
    }

    /**
     * preference for lat/long
     *
     * @param context
     * @param name
     * @param value
     */
    public static void setApplicationPreferencesDouble(Context context, String name,
                                                       float value) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(name, value);
        // Commit the edits!
        editor.commit();
    }

    public static float getApplicationPreferencesDouble(Context context, String name, float defaultValue) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);

        return settings.getFloat(name, defaultValue);

    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static String getApplicationPreferences(Context context, String name, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

        return settings.getString(name, defaultValue);

    }

    public static void removeSharedPreference(Context context, String key) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        // Commit the edits!
        editor.commit();
    }

    public static void clearSharedPreference(Context context) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }


    public static boolean getApplicationPreferencesBooleanValues(
            Context context, String name) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(name, false);
    }

    /**
     * Angad Tiwari 4-Aug-2015
     */

    public static Date localToGMTDate(Date date) {
        Date convertedDate = new Date();
        try {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd'T'HH:mm:ssZZZ");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            convertedDate = dateFormatGmt.parse(dateFormatGmt.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        gmtToLocalDate(convertedDate);
        return convertedDate;
    }

    public static Date gmtToLocalDate(Date date) {
        Date convertedDate = new Date();
        try {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd'T'HH:mm:ssZZZ");
            dateFormatGmt.setTimeZone(TimeZone.getDefault());
            convertedDate = dateFormatGmt.parse(dateFormatGmt.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    /**
     * Compute from string date in the format of yyyy-MM-dd HH:mm:ss the age of a person.
     * @author Rajkumari
     * @date 07/08/2015
     */

    /**
     * This Method is unit tested properly for very different cases ,
     * taking care of Leap Year days difference in a year,
     * and date cases month and Year boundary cases (12/31/1980, 01/01/1980 etc)
     **/

    public static int getAge(Date dateOfBirth) {

        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        int age;

        birthDate.setTime(dateOfBirth);
        if (birthDate.after(today)) {
            throw new IllegalArgumentException("Can't be born in the future");
        }

        age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If birth date is greater than todays date (after 2 days adjustment of leap year) then decrement age one year
        if ((birthDate.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) > 3) ||
                (birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH))) {
            age--;

            // If birth date and todays date are of same month and birth day of month is greater than todays day of month then decrement age
        } else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)) &&
                (birthDate.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }

    // function to decode string encoded by base 64
    public static String decodeStringBase64(String str)
    {
        if (str == null) {
            return "";
        }
        byte[] data = Base64.decode(str, Base64.NO_WRAP);

        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    // function to encode string by base 64
    public static String encodeStringBase64(String str) {
        if (str == null) {
            return "";
        }
        str = str.trim();
        byte[] data = new byte[0];
        try {
            data = str.getBytes("UTF-8");
            return Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void showToast(Context context, String message) {
        //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Shows the soft keyboard
     */
    public static void showSoftKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }


    public static int generateRandomNumber(int min, int max) {
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * app compact dialog (flat dialog like lollipop dialog)
     *
     * @param context
     * @param title
     * @param msg
     * @param cancellable
     * @param positiveBtnText
     * @param positiveBtnListener
     * @param showNegativeBtn
     * @param negativeBtnText
     * @param negativeBtnListener
     * @return
     */
    public static android.support.v7.app.AlertDialog.Builder createDialog(Context context,
                                                                          String title,
                                                                          String msg,
                                                                          boolean cancellable,
                                                                          String positiveBtnText,
                                                                          DialogInterface.OnClickListener positiveBtnListener,
                                                                          boolean showNegativeBtn,
                                                                          String negativeBtnText,
                                                                          DialogInterface.OnClickListener negativeBtnListener) {

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(msg);

        if (!cancellable)
            builder.setCancelable(false);

        builder.setPositiveButton(positiveBtnText, positiveBtnListener);
        if (showNegativeBtn)
            builder.setNegativeButton(negativeBtnText, negativeBtnListener);

        return builder;
    }

    /**
     * format the EXPIRE_DATE to from "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" to long
     *
     * @param dateString
     * @return
     */
    public static Date formatDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static void setcardWidthHeight(Context context, View view, int width_perc, int height_perc) {
        int width = (int) getWidth(context) * width_perc / 100;
        TableRow.LayoutParams params = new TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT); // (width, height)
        view.setLayoutParams(params);
    }

    public static int getWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Log.e("Width", "" + width);
        return width;
    }

    public static InputStream bitmapToInputStream(Bitmap bitmap) {
        int size = bitmap.getHeight() * bitmap.getRowBytes();
        ByteBuffer buffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(buffer);
        return new ByteArrayInputStream(buffer.array());
    }

    /**
     * parse the audio input stream and save the data to "sparkup_audio.ogg" file inside cache dir of the app
     *
     * @param input
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean convertStreamToFile(Context context, InputStream input, String name) {
        try {
            File file = new File(context.getCacheDir(), name);//"sparkup_audio.ogg");
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // handle exception, define IOException and others
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return true;
    }

    /**
     * Gets the file path of the given URI.
     *
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    @SuppressWarnings("NewApi")
    public static String _getPath(Uri uri, Context context) throws URISyntaxException {
        Log.d(TAG, ">> _getPath()");
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;

        if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {                                    // URI is different in versions after KITKAT (Android 4.4): we need to deal with different Uris.
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        Log.d(TAG, ">> isExternalStorageDocument()");
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        Log.d(TAG, ">> isDownloadsDocument()");
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        Log.d(TAG, ">> isMediaDocument()");
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
