package com.xurside.adrianapp.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.xurside.adrianapp.Config.Constants;
import com.xurside.adrianapp.R;
import com.xurside.adrianapp.models.SharedPrefManager;
import com.xurside.adrianapp.network.VolleyMultipartRequest;
import com.xurside.adrianapp.utils.CameraUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xurside.adrianapp.utils.AppHelper.allowedFileSize;
import static com.xurside.adrianapp.utils.AppHelper.explodeEmails;
import static com.xurside.adrianapp.utils.AppHelper.fileToBytes;
import static com.xurside.adrianapp.utils.AppHelper.implodeEmailsArray;
import static com.xurside.adrianapp.utils.AppHelper.is_allowed_to_upload;
import static com.xurside.adrianapp.utils.AppHelper.validEmails;

public class VideoRecordActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    // Activity request codes
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    // key to store image path in savedInstance state
    public static final String KEY_VIDEO_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_VIDEO = 2;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "xurSide";

    // Image and Video file extensions
    public static final String VIDEO_EXTENSION = "mp4";

    private static String videoStoragePath = "";

    private VideoView videoPreview;
    private Button btnRecordVideo;
    private TextView txtDescription,textViewResponse;
    private String title, deliver_date, email,videoName;
    private EditText editTextTitle, editTextDeliveryDate, editTextVideoMails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        //startMyService();
        // Checking availability of the camera
        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionbar != null) {
            actionbar.setHomeAsUpIndicator(R.drawable.ic_back_24dp);
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        txtDescription = findViewById(R.id.txt_desc);
        textViewResponse = findViewById(R.id.textViewResponse);
        videoPreview = findViewById(R.id.videoPreview);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDeliveryDate = findViewById(R.id.editTextDeliveryDate);
        editTextDeliveryDate.setEnabled(false);
        editTextVideoMails = findViewById(R.id.editTextVideoMails);

        /**
         * Record video on button click
         */
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureVideo();
                } else {
                    requestCameraPermission(MEDIA_TYPE_VIDEO);
                }
            }
        });

        // restoring storage image path from saved instance state
        // otherwise the path will be null on device rotation
        restoreFromBundle(savedInstanceState);
    }
//    void startMyService() {
//        Intent mService = new Intent(getApplicationContext(), MyService.class);
//        startService(mService);
//    }
    /**
     * Restoring store image path from saved instance state
     */
    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_VIDEO_STORAGE_PATH)) {
                videoStoragePath = savedInstanceState.getString(KEY_VIDEO_STORAGE_PATH);
                if (!TextUtils.isEmpty(videoStoragePath)) {
                    if (videoStoragePath.substring(videoStoragePath.lastIndexOf(".")).equals("." + VIDEO_EXTENSION)) {
                        previewVideo();
                    }
                }
            }
        }
    }

    /**
     * Requesting permissions using Dexter library
     */
    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            if (type == MEDIA_TYPE_VIDEO) {
                                // capture video
                                captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Saving stored image path to saved instance state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString(KEY_VIDEO_STORAGE_PATH, videoStoragePath);
    }

    /**
     * Restoring video path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        videoStoragePath = savedInstanceState.getString(KEY_VIDEO_STORAGE_PATH);
    }

    /**
     * Launching camera app to record video
     */
    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        if (file != null) {
            videoStoragePath = file.getAbsolutePath();
            videoName = file.getName();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Video
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), videoStoragePath);

                // video successfully recorded
                // preview the recorded video
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Displaying video in VideoView
     */
    private void previewVideo() {
        try {
            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(videoStoragePath);
            // setting controllers
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoPreview);
            videoPreview.setMediaController(mediaController);
            // start playing
            videoPreview.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_permission_is_required_title)
                .setMessage(R.string.alert_permission_request_message)
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(VideoRecordActivity.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }


    public void buttonUploadRecordedVideo(View view) {

        if (editTextTitle.getText().toString().trim().isEmpty()) {
            editTextTitle.setError(getString(R.string.alert_enter_video_title_first));
            editTextTitle.requestFocus();
            return;
        } else {
            title = editTextTitle.getText().toString().trim();
            editTextTitle.setError(null);
        }
        if (editTextDeliveryDate.getText().toString().trim().isEmpty()) {
            editTextDeliveryDate.setError(getString(R.string.alert_enter_delivery_date));
            editTextDeliveryDate.requestFocus();
            return;
        } else {
            deliver_date = editTextDeliveryDate.getText().toString().trim();
            editTextDeliveryDate.setError(null);
        }

        if (editTextVideoMails.getText().toString().trim().isEmpty())
        {
            editTextVideoMails.setError(getString(R.string.alert_email_is_required));
            Toast.makeText(VideoRecordActivity.this,R.string.alert_email_is_required, Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            email = editTextVideoMails.getText().toString().trim();
            Log.e("Submitted Emails", email);
            String[] exploded_emails = explodeEmails(email);
            if (exploded_emails.length == 0) {
                editTextVideoMails.setError(getString(R.string.alert_email_is_required));
                Toast.makeText(VideoRecordActivity.this, R.string.alert_email_is_required, Toast.LENGTH_LONG).show();
                return;
            } else {
                if (!validEmails(exploded_emails)) {
                    editTextVideoMails.setError(getString(R.string.alert_enter_valid_email));
                    Toast.makeText(VideoRecordActivity.this, R.string.alert_enter_valid_email, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    editTextVideoMails.setError(null);
                }
                email = implodeEmailsArray(exploded_emails);
            }
        }

        if (!is_allowed_to_upload(this)) {
            String stm = "Allowed Videos to upload are [" + SharedPrefManager.getInstance(this).getAllowedQty() + "]";
            textViewResponse.setText(stm);
            textViewResponse.setVisibility(View.VISIBLE);
            return;
        }
        int video_max_size = SharedPrefManager.getInstance(getApplicationContext()).getVideoMaxSize();
        if (videoStoragePath.equals("")) {
            textViewResponse.setText(R.string.alert_no_video_recorded);
            return;
        }
        if (allowedFileSize(videoStoragePath,video_max_size)) {
            uploadVideo();
            showUploadingLayout();
        } else {
            hideUploadingLayout();
            textViewResponse.setText(R.string.alert_bigFileSize);
        }
    }

    private void uploadVideo() {
        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constants.UPLOAD_VIDEO_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        hideUploadingLayout();
                        try {
                            SharedPrefManager.getInstance(VideoRecordActivity.this).updateUploadedVideosByUser("+");
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(VideoRecordActivity.this, VideosActivity.class));
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("onErrorResponse", "VolleyError");
                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.alert_unkown_error);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.alert_request_timeout);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = getString(R.string.alert_failed_connection_to_server);
                            }
                        } else {
                            String result = new String(networkResponse.data);
                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");

                                Log.e("Error Status", status);
                                Log.e("Error Message", message);

                                if (networkResponse.statusCode == 404) {
                                    errorMessage = getString(R.string.alert_resource_not_found);
                                } else if (networkResponse.statusCode == 401) {
                                    errorMessage = message + getString(R.string.stm_login_again);
                                } else if (networkResponse.statusCode == 400) {
                                    errorMessage = message + getString(R.string.stm_check_your_inputs);
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = message + getString(R.string.stm_its_getting_wrong);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        hideUploadingLayout();
                        Toast.makeText(
                                VideoRecordActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                        error.printStackTrace();
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", SharedPrefManager.getInstance(getApplicationContext()).getUserid());
                params.put("title", title);
                params.put("deliver_date", deliver_date);
                params.put("emails", email);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                byte[] bytes = fileToBytes(videoStoragePath);
                params.put("myFile", new DataPart(videoName, bytes));

                return params;
            }
        };
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //adding the request to volley
        RequestQueue queue = Volley.newRequestQueue(VideoRecordActivity.this);
        queue.add(volleyMultipartRequest);
//        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(volleyMultipartRequest);
        textViewResponse.setVisibility(View.INVISIBLE);
        txtDescription.setText(R.string.video_is_uploading);
    }
    public void buttonPickDate(View view) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                VideoRecordActivity.this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Initial day selection
        );
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(getFragmentManager(), "DatePickerDialog2");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = (monthOfYear + 1) + "/" + (dayOfMonth) + "/" + year;
        editTextDeliveryDate.setText(date);
    }
    public void showUploadingLayout() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    public void hideUploadingLayout() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
//        Intent mService = new Intent(getApplicationContext(), MyService.class);
//        stopService(mService);
//        Toast.makeText(this, "Record Activity exit", Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }
}
