package com.xurside.adrianapp.network;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xurside.adrianapp.Config.Constants;
import com.xurside.adrianapp.R;
import com.xurside.adrianapp.models.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        //Toast.makeText(getApplicationContext(),"Service Started",Toast.LENGTH_LONG).show();
        appUpdate();
        return START_STICKY;
    }

    private void appUpdate() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.UPDATE_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Log.e("App DATA", response);
                            JSONObject obj = new JSONObject(response);

                            if (obj.getBoolean("response")) {
                                SharedPrefManager.getInstance(getApplicationContext())
                                        .userLogin(
                                                obj.getInt("id"),
                                                obj.getString("username"),
                                                obj.getString("fullname"),
                                                obj.getString("contact"),
                                                obj.getString("email"),
                                                obj.getInt("user_uploaded_qty"),
                                                obj.getInt("video_max_size"),
                                                obj.getInt("allowed_qty"),
                                                obj.getInt("banned")
                                        );
                            } else {
                                Toast.makeText(getApplicationContext(),obj.getString("message"),Toast.LENGTH_LONG).show();
                                if (obj.getString("message").equals("Wrong")) {
                                    Log.e("App DATA", "Wrong App Update Service - Session Expired");
                                    //SharedPrefManager.getInstance(getApplicationContext()).logout();
                                    //Toast.makeText(getApplicationContext(),"Session Expired",Toast.LENGTH_LONG).show();
                                }
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

                        Toast.makeText(
                                getApplicationContext(),
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                //Log.i("Userid",SharedPrefManager.getInstance(getApplicationContext()).getUserid());
                params.put("id", SharedPrefManager.getInstance(getApplicationContext()).getUserid());
                return params;
            }

        };
        RequestQueue queue = Volley.newRequestQueue(MyService.this);
        queue.add(stringRequest);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                appUpdate();
            }
        }, 600000);
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(getApplicationContext(),"Stop Service",Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

}
