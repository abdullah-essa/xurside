package com.xurside.adrianapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.xurside.adrianapp.Config.Constants;
import com.xurside.adrianapp.R;
import com.xurside.adrianapp.models.SharedPrefManager;
import com.xurside.adrianapp.utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.xurside.adrianapp.utils.AppHelper.explodeEmails;
import static com.xurside.adrianapp.utils.AppHelper.implodeEmailsArray;
import static com.xurside.adrianapp.utils.AppHelper.validEmails;

public class VideoActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {
    VideoView videoView;
    TextView textViewVideoTitle;
    String video_id, vid_title, vid_emails, deliver_date;
    Button btn_cancel;
    LinearLayout layout_video_edit;
    RelativeLayout layout_video_watch;
    MediaController mediaController;
    private ProgressDialog progressDialog;
    private EditText editTextVideoTitle, editTextDeliveryDate, editTextVideoMails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

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

        btn_cancel = findViewById(R.id.btn_cancel);

        Intent intentData = getIntent();
        video_id = intentData.getExtras().getString("video_id");
        String title = intentData.getExtras().getString("title");
        String action = intentData.getExtras().getString("action");

        textViewVideoTitle = findViewById(R.id.textViewVideoTitle);
        editTextDeliveryDate = findViewById(R.id.editTextDeliveryDate);

        layout_video_edit = findViewById(R.id.layout_video_edit);
        layout_video_watch = findViewById(R.id.layout_video_watch);

//        Log.e("Title", title);
        progressDialog = new ProgressDialog(this);


        if (action != null && action.equals("edit")) {
            if (actionbar != null) {
                actionbar.setTitle("Edit Video");
            }

            layout_video_edit.setVisibility(View.VISIBLE);
            layout_video_watch.setVisibility(View.GONE);
            editTextVideoTitle = findViewById(R.id.vid_title);


//            editTextVideoEmail = findViewById(R.id.editTextVideoEmail);
            editTextVideoMails = findViewById(R.id.editTextVideoMails);

            editTextVideoTitle.setText(title);
            get_video();


        }

        if (action != null && action.equals("watch")) {
            showProgress();
            if (actionbar != null) {
                actionbar.setTitle("Watching Video");
            }
            textViewVideoTitle.setText(title);

            layout_video_watch.setVisibility(View.VISIBLE);
            layout_video_edit.setVisibility(View.GONE);

            videoView = findViewById(R.id.videoView);
            String vid_relative_link = intentData.getExtras().getString("vid_relative_link");

            mediaController = new MediaController(this);
            videoView.setMediaController(new MediaController(this));
            mediaController.setAnchorView(videoView);
            videoView.requestFocus();
            videoView.setVideoPath(vid_relative_link);

            videoView.start();

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    hideProgress();
                }
            });
        }
    }

    public void showProgress() {
        findViewById(R.id.ProgressBar).setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        findViewById(R.id.ProgressBar).setVisibility(View.GONE);
    }

    public void get_video() {
        progressDialog.setMessage("Fetching Video ...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.GET_VIDEO_URL + video_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            String message = obj.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            if (obj.getBoolean("response")) {
                                editTextVideoMails.setText(obj.getString("emails"));
//                                editTextVideoEmail.setText(obj.getString("emails"));
                                editTextDeliveryDate.setText(obj.getString("deliver_date"));
                                editTextDeliveryDate.setEnabled(false);
                            } else {
                                TextView textViewResponse = findViewById(R.id.textViewResponse);
                                textViewResponse.setText(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                        progressDialog.dismiss();
                        Toast.makeText(
                                getApplicationContext(),
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                        error.printStackTrace();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("video_id", video_id);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void btn_update(View view) {

        if (validateInputs()) {
            progressDialog.setMessage("Fetching Video ...");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.UPDATE_VIDEO_URL + video_id,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Log.e("response", response);
                            try {
                                JSONObject obj = new JSONObject(response);
                                String message = obj.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                if (obj.getBoolean("response")) {
                                    startActivity(new Intent(VideoActivity.this, VideosActivity.class));
                                    finish();
                                } else {
                                    TextView textViewResponse = findViewById(R.id.textViewResponse);
                                    textViewResponse.setText(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
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
                                Log.e("Error", result);
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
                            progressDialog.dismiss();
                            Toast.makeText(
                                    getApplicationContext(),
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                            error.printStackTrace();

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", video_id);
                    params.put("title", vid_title);
                    params.put("emails", vid_emails);
                    params.put("deliver_date", deliver_date);
                    return params;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }
    }

    private boolean validateInputs() {
        vid_title = editTextVideoTitle.getText().toString().trim();
        vid_emails = editTextVideoMails.getText().toString().trim();
        deliver_date = editTextDeliveryDate.getText().toString().trim();

        editTextVideoTitle.setError(null);
        editTextVideoMails.setError(null);
        editTextDeliveryDate.setError(null);

        if (TextUtils.isEmpty(vid_title)) {
            this.editTextVideoTitle.setError("Title must not be empty");
            Toast.makeText(VideoActivity.this, "Title must not be empty", Toast.LENGTH_LONG).show();
            return false;
        }

        Log.e("Submitted Emails", vid_emails);
        String[] exploded_emails = explodeEmails(vid_emails);
        if (exploded_emails.length == 0) {
            this.editTextVideoMails.setError("emails must not be empty");
            Toast.makeText(VideoActivity.this, "emails must not be empty", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (!validEmails(exploded_emails)) {
                editTextVideoMails.setError("Enter valid email(s) and separate them with comma (,)");
                Toast.makeText(VideoActivity.this, "Enter valid email(s) and separate them with comma (,)", Toast.LENGTH_LONG).show();
                return false;
            }
            vid_emails = implodeEmailsArray(exploded_emails);
        }
        if (deliver_date.isEmpty()) {
            editTextDeliveryDate.setError("Enter Valid Delivery Date");
            editTextDeliveryDate.requestFocus();
            return false;
        }
        return true;
    }

    public void btn_cancel(View view) {
        startActivity(new Intent(VideoActivity.this, VideosActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the VideosActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            Toast.makeText(
                    getApplicationContext(),
                    "You Clicked Refresh Option",
                    Toast.LENGTH_LONG
            ).show();
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
                AlertDialog.Builder build = new AlertDialog.Builder(VideoActivity.this);
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


    public void buttonPickDate(View view) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                VideoActivity.this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Initial day selection
        );
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(getFragmentManager(), "DatePickerDialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = (monthOfYear + 1) + "/" + (dayOfMonth) + "/" + year;
        editTextDeliveryDate.setText(date);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
