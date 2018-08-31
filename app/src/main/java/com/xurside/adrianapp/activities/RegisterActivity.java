package com.xurside.adrianapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.xurside.adrianapp.R;import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xurside.adrianapp.Config.Constants;
import com.xurside.adrianapp.models.SharedPrefManager;
import com.xurside.adrianapp.utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword, editTextFullname, editTextContact;
    private Button buttonRegister;
    private ProgressDialog progressDialog;
    private String username, fullname, email, contact, password, cpassword;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, ProfileActivity.class));
            return;
        }
        setContentView(R.layout.activity_register);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionbar != null) {
            actionbar.setHomeAsUpIndicator(R.drawable.ic_back_24dp);
        }
        if (actionbar != null) {
            actionbar.setTitle("Registration");
        }
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xffff8800));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));


//        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
//        AdView mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);


        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextFullname = findViewById(R.id.editTextFullname);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextContact = findViewById(R.id.editTextContact);

        textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister = findViewById(R.id.buttonRegister);

        progressDialog = new ProgressDialog(this);

        buttonRegister.setOnClickListener(this);
        textViewLogin.setOnClickListener(this);
    }

    private boolean validateInputs() {
        email = editTextEmail.getText().toString().trim();
        username = editTextUsername.getText().toString().trim();
        fullname = editTextFullname.getText().toString().trim();
        contact = editTextContact.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        cpassword = editTextConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            this.editTextUsername.setError("Username is required");
            Toast.makeText(RegisterActivity.this, "Username is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            this.editTextEmail.setError("email is required");
            Toast.makeText(RegisterActivity.this, "email is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(fullname)) {
            this.editTextFullname.setError("Fullname is required");
            Toast.makeText(RegisterActivity.this, "Fullname is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(contact)) {
            this.editTextContact.setError("contact is required");
            Toast.makeText(RegisterActivity.this, "contact is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!password.equals(cpassword) && !password.isEmpty() && !cpassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Password and Confirm Password must be matched", Toast.LENGTH_LONG).show();
            this.editTextPassword.setError("");
            this.editTextConfirmPassword.setError("");
            return false;
        } else {
            if (TextUtils.isEmpty(password)) {
                this.editTextPassword.setError("Password is required");
                Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_LONG).show();
                return false;
            }
            if (TextUtils.isEmpty(cpassword)) {
                this.editTextConfirmPassword.setError("Confirm Password is required");
                Toast.makeText(RegisterActivity.this, "Confirm Password is required", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void registerUser() {
        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject obj = new JSONObject(response);
//                            Log.e("msg", obj.getString("message"));
                            String message = obj.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            if (obj.getBoolean("response")) {
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
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
                params.put("username", username);
                params.put("email", email);
                params.put("fname", fullname);
                params.put("contact", contact);
                params.put("password", password);
                params.put("cpassword", cpassword);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
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
        Log.e("id", String.valueOf(id));
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();
                break;
            case R.id.homeAsUp:
                onBackPressed();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_help:
                try {
                    InputStream inputStream = getAssets().open("help.txt");
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader BR = new BufferedReader(inputStreamReader);
                    String line;
                    StringBuilder msg = new StringBuilder();
                    while ((line = BR.readLine()) != null) {
                        msg.append(line).append("\n");
                    }
                    AlertDialog.Builder build = new AlertDialog.Builder(RegisterActivity.this);
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
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        if (view == buttonRegister) {
            if (validateInputs())
                registerUser();
        }
        if (view == textViewLogin) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }

    }
}
