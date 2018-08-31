package com.xurside.adrianapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.xurside.adrianapp.R;import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xurside.adrianapp.Config.Constants;
import com.xurside.adrianapp.activities.VideoActivity;
import com.xurside.adrianapp.activities.VideosActivity;
import com.xurside.adrianapp.models.SharedPrefManager;
import com.xurside.adrianapp.models.VideoList;
import com.xurside.adrianapp.utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {


    //this context we will use to inflate the layout
    private Context mCtx;

    //we are storing all the products in a list
    private List<VideoList> videosList;

    //getting the context and product list with constructor
    public VideoAdapter(Context mCtx, List<VideoList> videoList) {
        this.mCtx = mCtx;
        this.videosList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.list_videos, parent, false);
//        View view = inflater.inflate(R.layout.list_videos, parent,false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        //getting the product of the specified position
        final VideoList video = videosList.get(position);
        final int list_id = position;

        //binding the data with the viewholder views
        holder.textViewVideoTitle.setText(video.getTitle());
        holder.textViewDeliverDate.setText(video.getDeliver_date());
        holder.textViewSent.setText(video.getSent());

        holder.buttonWatchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showProgress();
                Intent OpenMovie = new Intent(mCtx.getApplicationContext(), VideoActivity.class);
                OpenMovie.putExtra("video_id", video.getId());
                OpenMovie.putExtra("title", video.getTitle());
                OpenMovie.putExtra("action", "watch");
                OpenMovie.putExtra("vid_relative_link", video.getLink());
                mCtx.startActivity(OpenMovie);

            }
        });
        holder.buttonEditVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showProgress();
                Intent OpenMovie = new Intent(mCtx, VideoActivity.class);
                OpenMovie.putExtra("video_id", video.getId());
                OpenMovie.putExtra("title", video.getTitle());
                OpenMovie.putExtra("action", "edit");
                mCtx.startActivity(OpenMovie);

            }
        });
        holder.buttonDeleteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int video_id = Integer.parseInt(video.getId());
                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        Constants.DELETE_VIDEO_URL + video_id,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (obj.getBoolean("response")) {
                                        // remove the item
                                        videosList.remove(list_id);
                                        // subtract the videos qty uploaded by the user
                                        SharedPrefManager.getInstance(mCtx).updateUploadedVideosByUser("-");
                                        // return to the VideosActivity if there is no more items in the list
                                        if (getItemCount() == 0)
                                            mCtx.startActivity(new Intent(mCtx, VideosActivity.class));
                                        // notify
                                        notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(
                                                mCtx,
                                                obj.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(mCtx, R.string.alert_danger_deleting_video, Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(
                                        mCtx,
                                        error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
//                                hideProgress();
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        String video_id = video.getId();
                        params.put("id", video_id);
                        return params;
                    }
                };

                VolleySingleton.getInstance(mCtx).addToRequestQueue(stringRequest);
            }
        });

    }


    @Override
    public int getItemCount() {
        return videosList.size();
    }


    class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView textViewVideoTitle, textViewDeliverDate, textViewSent;
        ImageButton buttonDeleteVideo, buttonEditVideo, buttonWatchVideo;

        private VideoViewHolder(View itemView) {
            super(itemView);
            textViewVideoTitle = itemView.findViewById(R.id.textViewVideoTitle);
            textViewDeliverDate = itemView.findViewById(R.id.textViewDeliverDate);
            textViewSent = itemView.findViewById(R.id.textViewSent);
            buttonDeleteVideo = itemView.findViewById(R.id.buttonDeleteVideo);
            buttonEditVideo = itemView.findViewById(R.id.buttonEditVideo);
            buttonWatchVideo = itemView.findViewById(R.id.buttonWatchVideo);
        }
    }
}
