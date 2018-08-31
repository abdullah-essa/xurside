package com.xurside.adrianapp.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.xurside.adrianapp.R;import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.xurside.adrianapp.Config.Constants;
import com.xurside.adrianapp.models.SharedPrefManager;
import com.xurside.adrianapp.network.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.xurside.adrianapp.utils.AppHelper.allowedFileSize;
import static com.xurside.adrianapp.utils.AppHelper.explodeEmails;
import static com.xurside.adrianapp.utils.AppHelper.implodeEmailsArray;
import static com.xurside.adrianapp.utils.AppHelper.validEmails;

public class UploadVideoActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final int SELECT_VIDEO = 3;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private static int COUNT_PERMISSION_DENY = 1;
    private View mLayout;
    TextView textViewResponse;
    int uploaded_videos_by_user, allowed_uploaded_qty;
    Button buttonUpload, buttonRequestPermission;
    private String selectedPath, title, deliver_date, email;
    private VideoView videoView;
    private EditText editTextTitle, editTextDeliveryDate, editTextVideoMails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        mLayout = findViewById(R.id.activity_upload_video_layout);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionbar != null) {
            actionbar.setHomeAsUpIndicator(R.drawable.ic_back_24dp);
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));


//        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
//        AdView mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        //initializing views
        videoView = findViewById(R.id.videoView);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDeliveryDate = findViewById(R.id.editTextDeliveryDate);
        editTextVideoMails = findViewById(R.id.editTextVideoMails);
        textViewResponse = findViewById(R.id.textViewResponse);
        editTextDeliveryDate.setEnabled(false);
        buttonUpload = findViewById(R.id.buttonUpload);
        //checking the permission
        //if the permission is not given we will open setting to add permission
        //else app will not open
//        requestStoragePermission();
        checkStoragePermission();


        //adding click listener to button
        buttonUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                title = editTextTitle.getText().toString().trim();
                deliver_date = editTextDeliveryDate.getText().toString().trim();
                email = editTextVideoMails.getText().toString().trim();

                // Reset errors.
                editTextTitle.setError(null);
                editTextDeliveryDate.setError(null);
                editTextVideoMails.setError(null);

                if (title.isEmpty()) {
                    editTextTitle.setError("Enter a Title first");
                    editTextTitle.requestFocus();
                    return;
                }
                if (deliver_date.isEmpty()) {
                    editTextDeliveryDate.setError("Enter a Date first");
                    editTextDeliveryDate.requestFocus();
                    return;
                }
                Log.e("Submitted Emails", email);
                String[] exploded_emails = explodeEmails(email);
                if (exploded_emails.length == 0) {
                    editTextVideoMails.setError("Email(s) must not be empty");
                    Toast.makeText(UploadVideoActivity.this, "Email(s) must not be empty", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (!validEmails(exploded_emails)) {
                        editTextVideoMails.setError("Enter valid email(s) and separate them with comma (,)");
                        Toast.makeText(UploadVideoActivity.this, "Enter valid email(s) and separate them with comma (,)", Toast.LENGTH_LONG).show();
                        return;
                    }
                    email = implodeEmailsArray(exploded_emails);
                }
                if (!is_allowed_to_upload()) {
                    String stm = "Allowed Videos to upload [" + allowed_uploaded_qty + "] | Videos Uploaded By You [" + allowed_uploaded_qty + "]";
                    textViewResponse.setText(stm);
                    textViewResponse.setVisibility(View.VISIBLE);
                    return;
                }
                //if everything is ok we will open video chooser
                chooseVideo();
            }
        });
    }


    private void checkStoragePermission() {
        // Check if the READ_EXTERNAL_STORAGE permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available
            buttonUpload.setVisibility(View.VISIBLE);
            buttonRequestPermission = findViewById(R.id.buttonRequestPermission);
            buttonRequestPermission.setVisibility(View.GONE);
        } else {
            // Permission is missing and must be requested.
            requestStoragePermission();
        }
        // END_INCLUDE(startCamera)
    }

    private void requestStoragePermission() {
        // Permission has not been granted and must be requested.
        // Request the permission. The result will be received in onRequestPermissionResult().
        ActivityCompat.requestPermissions(UploadVideoActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            // Request for Storage permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Show Upload button.
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_LONG).show();
                Snackbar.make(mLayout, R.string.permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                buttonUpload.setVisibility(View.VISIBLE);
                buttonRequestPermission = findViewById(R.id.buttonRequestPermission);
                buttonRequestPermission.setVisibility(View.GONE);
            } else {
                // Permission request was denied.
                COUNT_PERMISSION_DENY++;
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
                Snackbar.make(mLayout, R.string.permission_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                buttonUpload.setVisibility(View.INVISIBLE);
                if (COUNT_PERMISSION_DENY < 3)
                    requestStoragePermission();
                else {
                    buttonRequestPermission = findViewById(R.id.buttonRequestPermission);
                    buttonRequestPermission.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select mp4 Video "), SELECT_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_VIDEO && resultCode == RESULT_OK && data != null) {

            System.out.println("SELECT_VIDEO");
            Uri selectedImageUri = data.getData();
            try {
                selectedPath = getPath(selectedImageUri);
                System.out.println(selectedImageUri);

                if (selectedPath != null) {
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setVideoURI(selectedImageUri);
                    videoView.setVideoPath(selectedPath);
                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                    videoView.start();
                    textViewResponse.setText(selectedPath);
                    int video_max_size = SharedPrefManager.getInstance(getApplicationContext()).getVideoMaxSize();
                    if (allowedFileSize(selectedPath,video_max_size)) {
                        uploadVideo();
                        showUploadingLayout();
                    } else {
                        hideUploadingLayout();
                        textViewResponse.setText(R.string.alert_bigFileSize);
                    }
                } else {
                    hideUploadingLayout();

                    textViewResponse.setText(R.string.alert_no_file_selected);
                }
            } catch (Exception e) {
                e.printStackTrace();
                hideUploadingLayout();
            }

        }
    }

    private boolean is_allowed_to_upload() {
        uploaded_videos_by_user = SharedPrefManager.getInstance(getApplicationContext()).getUserUploadedQty();
        allowed_uploaded_qty = SharedPrefManager.getInstance(getApplicationContext()).getAllowedQty();

        return uploaded_videos_by_user < allowed_uploaded_qty;
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Video.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        cursor.close();

        return path;
    }

    private void uploadVideo() {
        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constants.UPLOAD_VIDEO_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        hideUploadingLayout();
                        try {
                            SharedPrefManager.getInstance(UploadVideoActivity.this).updateUploadedVideosByUser("+");
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UploadVideoActivity.this, VideosActivity.class));
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
//                        progressDialog.dismiss();
                        Toast.makeText(
                                UploadVideoActivity.this,
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
                long videoname = System.currentTimeMillis();
                byte[] bytes = aaa(selectedPath);
                params.put("myFile", new DataPart(videoname + ".mp4", bytes));

                return params;
            }
        };
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //adding the request to volley
        RequestQueue queue = Volley.newRequestQueue(UploadVideoActivity.this);
        queue.add(volleyMultipartRequest);
//        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(volleyMultipartRequest);
        textViewResponse.setVisibility(View.INVISIBLE);

    }

    public void showUploadingLayout() {
        findViewById(R.id.layout_video_uploading).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    public void hideUploadingLayout() {
        findViewById(R.id.layout_video_uploading).setVisibility(View.GONE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    public byte[] aaa(String filepath) {
        byte[] soundBytes = new byte[0];
        File initialFile = new File(filepath);
        try {
            InputStream inputStream = new FileInputStream(initialFile);
            soundBytes = toByteArray(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return soundBytes;
    }

    public byte[] toByteArray(InputStream in) throws IOException {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_secondary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the VideosActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.home) {
            startActivity(new Intent(this, VideosActivity.class));
            finish();
            return true;
        }
        if (id == R.id.menu_help) {
            try {
                InputStream inputStream = getAssets().open("help.txt");
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader BR = new BufferedReader(inputStreamReader);
                String line;
                StringBuilder msg = new StringBuilder();
                while ((line = BR.readLine()) != null) {
                    msg.append(line).append("\n");
                }
                AlertDialog.Builder build = new AlertDialog.Builder(UploadVideoActivity.this);
                build.setTitle(R.string.help);
                build.setIcon(R.mipmap.ic_launcher);
                build.setMessage(Html.fromHtml(msg + ""));
                build.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Negative
                    }
                }).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.menu_logout) {
            SharedPrefManager.getInstance(getApplicationContext()).logout();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void requestStoragePermission(View view) {
        requestStoragePermission();
    }

    public void buttonPickDate(View view) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                UploadVideoActivity.this,
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


}
