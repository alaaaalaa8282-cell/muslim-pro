package com.AbuMohamed.homePageFragments;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.AbuMohamed.App.Apis;
import com.AbuMohamed.R;
import com.AbuMohamed.adapters.MakkaLiveAdapter;
import com.AbuMohamed.common.Common;
import com.AbuMohamed.models.MakkaLive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MakkaLiveFragment extends Fragment {

    private View mView;
    private MakkaLiveAdapter makkaLiveAdapter;
    private List<MakkaLive> makkaLives;
    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private RecyclerView rvLiveTvlist;
    private RequestQueue mRequestQueue;
    private ProgressDialog mProgressDialog;
    private ImageView landscapBtn;
    private boolean isClicked = false;
    private String tvName, liveTvUrl;

    public MakkaLiveFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_makka_live, container, false);

        Common.toolbarVisibility("visible", "Makka Live", 1);

        rvLiveTvlist = mView.findViewById(R.id.rvLiveTvlist);
        landscapBtn = mView.findViewById(R.id.landscapBtn);
        playerView = mView.findViewById(R.id.video_view);
        makkaLives = new ArrayList<>();

        mRequestQueue = Volley.newRequestQueue(getActivity());
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage("Please Wait...!");

        landscapBtn.setOnClickListener(view -> {});

        liveTvM();
        return mView;
    }

    private void liveTvM() {
        mProgressDialog.show();
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Apis.tvUrl, null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("items");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject hit = jsonArray.getJSONObject(i);
                            String name = hit.getString("name");
                            String url = hit.getString("url");
                            String alt_url = hit.getString("alt_url");
                            String image = hit.getString("image");
                            String alt_image = hit.getString("alt_image");
                            String apk = hit.getString("apk");
                            String yitid = hit.getString("yitid");
                            String id = hit.getString("id");

                            if (i == 0 && !isClicked) {
                                startTv(name, url);
                            }
                            mProgressDialog.dismiss();
                            makkaLives.add(new MakkaLive(name, url, alt_url, image, alt_image, apk, yitid, id));
                        }
                        makkaLiveAdapter = new MakkaLiveAdapter(makkaLives, getActivity(), (view, position) -> {
                            liveTvUrl = makkaLives.get(position).getUrl();
                            isClicked = true;
                            tvName = makkaLives.get(position).getName();
                            startTv(tvName, liveTvUrl);
                            mProgressDialog.dismiss();
                        });
                        rvLiveTvlist.setLayoutManager(new LinearLayoutManager(getActivity()));
                        rvLiveTvlist.setAdapter(makkaLiveAdapter);
                        makkaLiveAdapter.notifyDataSetChanged();
                        mProgressDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mProgressDialog.dismiss();
                    }
                }, error -> mProgressDialog.dismiss());
        mRequestQueue.add(request);
    }

    private void startTv(final String tvName, final String liveTvUrl) {
        if (exoPlayer != null) exoPlayer.release();
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        playerView.setPlayer(exoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(liveTvUrl));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.prepare();
        exoPlayer.play();
        Common.plyTvName = tvName;
        Common.plyTvUrl = liveTvUrl;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (exoPlayer != null) exoPlayer.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
