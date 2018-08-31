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

import com.xurside.adrianapp.R;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xurside.adrianapp.Config.Constants;
import com.xurside.adrianapp.utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.xurside.adrianapp.models.SharedPrefManager.getInstance;

public class ProfileActivity extends AppCompatActivity {
    Button buttonUpdate;
    private EditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword, editTextFullname, editTextContact;
    private String username, fullname, email, contact, password, cpassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionbar != null) {
            actionbar.setHomeAsUpIndicator(R.drawable.ic_back_24dp);
        }
        actionbar.setTitle("Profile");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        progressDialog = new ProgressDialog(this);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextFullname = findViewById(R.id.editTextFullname);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextContact = findViewById(R.id.editTextContact);
        TextView textViewUploadedQty = findViewById(R.id.textViewUploadedQty);
        TextView textViewAllowedQty = findViewById(R.id.textViewAllowedQty);

        textViewUploadedQty.setText(String.valueOf(getInstance(getApplicationContext()).getUserUploadedQty()));
        textViewAllowedQty.setText(String.valueOf(getInstance(getApplicationContext()).getAllowedQty()));

        editTextUsername.setText(getInstance(this).getUsername());
        editTextEmail.setText(getInstance(this).getUserEmail());
        editTextFullname.setText(getInstance(this).getUserFullName());
        editTextContact.setText(getInstance(this).getUserContact());
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs())
                    updateProfile();
            }
        });
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
            Toast.makeText(this, "Username is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            this.editTextEmail.setError("email is required");
            Toast.makeText(this, "email is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(fullname)) {
            this.editTextFullname.setError("Fullname is required");
            Toast.makeText(this, "Fullname is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(contact)) {
            this.editTextContact.setError("contact is required");
            Toast.makeText(this, "contact is required", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!password.isEmpty() || !cpassword.isEmpty()) {
            if (!password.equals(cpassword)) {
                Toast.makeText(this, "Password and Confirm Password must be matched", Toast.LENGTH_LONG).show();
                if (TextUtils.isEmpty(password))
                    this.editTextPassword.setError("Password is required");
                if (TextUtils.isEmpty(cpassword))
                    this.editTextConfirmPassword.setError("Confirm Password is required");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_secondary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();
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
                AlertDialog.Builder build = new AlertDialog.Builder(ProfileActivity.this);
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
            getInstance(getApplicationContext()).logout();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateProfile() {
        progressDialog.setMessage("Updating Your Profile...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.UPDATE_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            Log.e("OBJECT", response);
                            JSONObject obj = new JSONObject(response);
                            String message = obj.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            if (obj.getBoolean("response")) {
                                int user_id = Integer.parseInt(getInstance(getApplicationContext()).getUserid());
                                getInstance(getApplicationContext())
                                        .userProfile(user_id, username, fullname, contact, email);
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
                Log.e("email", email);
                params.put("id", getInstance(getApplicationContext()).getUserid());
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
}
