package com.xurside.adrianapp.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.xurside.adrianapp.models.SharedPrefManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppHelper {

    private static boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static byte[] fileToBytes(String filepath) {
        byte[] fileBytes = new byte[0];
        Log.e("filepath",filepath);
        File initialFile = new File(filepath);
        try {
            InputStream inputStream = new FileInputStream(initialFile);
            fileBytes = toByteArray(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileBytes;
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1)
                out.write(buffer, 0, read);
        }
        out.close();
        return out.toByteArray();
    }
    public static boolean is_allowed_to_upload(Context context) {
        int uploaded_videos_by_user = SharedPrefManager.getInstance(context).getUserUploadedQty();
        int allowed_uploaded_qty = SharedPrefManager.getInstance(context).getAllowedQty();

        return uploaded_videos_by_user < allowed_uploaded_qty;
    }
    public static boolean allowedFileSize(String filePath, int video_max_size) {
        if (filePath.isEmpty())
            return false;
        File file = new File(filePath);
        int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
        Log.e("Size", String.valueOf(file_size));
        Log.e("video_max_size", String.valueOf(video_max_size));
        return file_size <= video_max_size;
    }

    public static String[] explodeEmails(String textEmails) {
        return textEmails.split("\\s*(\\s|,)\\s*");
    }

    public static boolean validEmails(String[] emails) {
        //int counter = 0;
        for (String email : emails) {
            //Log.e("Emails_"+ counter++, email);
            if (!isEmailValid(email))
                return false;
            //else Log.e("EmailsValid_"+ counter++, email);
        }
        return true;
    }

    public static String implodeEmailsArray(String[] emails) {
        return TextUtils.join(", ", emails);
    }
}