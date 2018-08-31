package com.xurside.adrianapp.models;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "mysharedpref12";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_CONTACT = "contact";
    private static final String KEY_USER_EMAIL = "useremail";
    private static final String KEY_USER_ID = "userid";
    private static final String KEY_BANNED = "banned";
    private static final String KEY_USER_UPLOADED_QTY = "user_uploaded_qty";
    private static final String KEY_VIDEO_MAX_SIZE = "video_max_size";
    private static final String KEY_ALLOWED_QTY = "allowed_qty";
    private static SharedPrefManager mInstance;
    private Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;

    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public void userLogin(int id, String username, String fullName, String contact, String email, int user_uploaded_qty, int video_max_size, int allowed_qty,int banned) {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_USER_ID, id);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULLNAME, fullName);
        editor.putString(KEY_CONTACT, contact);
        editor.putInt(KEY_USER_UPLOADED_QTY, user_uploaded_qty);
        editor.putInt(KEY_VIDEO_MAX_SIZE, video_max_size);
        editor.putInt(KEY_ALLOWED_QTY, allowed_qty);
        editor.putInt(KEY_BANNED, banned);

        editor.apply();

//        return true;
    }

    public boolean isAuthenticated() {
        return (isLoggedIn() && !isBanned());
    }
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return (sharedPreferences.getString(KEY_USERNAME, null) != null)
                && (sharedPreferences.getInt(KEY_BANNED, 0) != 1);
//                return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
//        return true;
    }

    public String getUserid() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return String.valueOf(sharedPreferences.getInt(KEY_USER_ID, 0));
//        sharedPreferences.getString(KEY_USER_ID, null);
    }

    public int getVideoMaxSize() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_VIDEO_MAX_SIZE, 1);
    }
    public int getUserUploadedQty() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_UPLOADED_QTY, 0);
    }

    public int getAllowedQty() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_ALLOWED_QTY, 0);
    }

    private void setKeyUserUploadedQty(int qty) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_USER_UPLOADED_QTY, qty);
        editor.apply();
    }

    public void updateUploadedVideosByUser(String operation) {
        int uploaded_videos_by_user = getUserUploadedQty();
        if (operation.equals("+"))
            uploaded_videos_by_user = uploaded_videos_by_user + 1;
        else if (operation.equals("-"))
            uploaded_videos_by_user = uploaded_videos_by_user - 1;
        setKeyUserUploadedQty(uploaded_videos_by_user);
    }

//    public void setKeyAllowedQty(int qty) {
//        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        editor.putInt(KEY_ALLOWED_QTY, qty);
//        editor.apply();
//    }

    public String getUsername() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getUserEmail() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserContact() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_CONTACT, null);
    }

    public String getUserFullName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_FULLNAME, null);
    }

    private boolean isBanned() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return (sharedPreferences.getInt(KEY_BANNED, 0) != 1);
//        return sharedPreferences.getInt(KEY_BANNED, 0);
    }

//    public void setBanned(int banned) {
//        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(KEY_BANNED, banned);
//        editor.apply();
//    }
    public void userProfile(int user_id, String username, String fullName, String contact, String email) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_USER_ID, user_id);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULLNAME, fullName);
        editor.putString(KEY_CONTACT, contact);
        editor.apply();
    }

}
