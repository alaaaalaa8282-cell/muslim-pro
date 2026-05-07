package com.AbuMohamed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.AbuMohamed.App.Apis;
import com.AbuMohamed.App.AppController;
import com.AbuMohamed.adapters.CustomLinearLayoutManager;
import com.AbuMohamed.adapters.SuraAyahAdapter;
import com.AbuMohamed.homePageFragments.QuranFragment;
import com.AbuMohamed.models.SuraAyah;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FullSuraActivity extends AppCompatActivity {
    private RecyclerView rvAyahList;
    private List<SuraAyah> ayahList;
    private SuraAyahAdapter suraAyahAdapter;
    private ProgressDialog progressDialog;
    private String suraNo, suraName, suraArabicName;
    private TextView tvSuraName;
    private List<String> engAyahList;
    private List<String> audioUrls = new ArrayList<>();
    private static final String MY_PREFS_NAME = "MuslimPro";
    SharedPreferences.Editor editor;
    int lastVisibleItem;
    int lastReadAyat = 0;
    int count;
    private Cursor cursor;
    String databaseName;
    int numberInSurah;
    private File root;
    private String fileName = null;
    boolean stop = false;
    LinearLayoutManager layoutManager;

    private MediaPlayer mediaPlayer;
    private ImageButton btnPlay, btnNext, btnPrevious;
    private SeekBar seekBar;
    private TextView tvAudioTitle;
    private int currentAudioIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_sura);

        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        tvSuraName = findViewById(R.id.tvSuraName);
        engAyahList = new ArrayList<>();

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        seekBar = findViewById(R.id.seekBar);
        tvAudioTitle = findViewById(R.id.tvAudioTitle);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            suraNo = bundle.getString("SURA_NO");
            suraName = bundle.getString("SURA_NAME");
            suraArabicName = bundle.getString("ARABIC_NAME");
            if (bundle.getInt("AYAT_POS") != 0) {
                lastReadAyat = bundle.getInt("AYAT_POS");
            }
            tvSuraName.setText(suraName);
        }

        databaseName = suraName.replace("-", "_").replace("'", "_");
        QuranFragment.quranDBHelper.queryData("CREATE TABLE IF NOT EXISTS " + databaseName + "(Id INTEGER PRIMARY KEY AUTOINCREMENT, number VARCHAR, arabicAyah VARCHAR, engAyah VARCHAR, numberInSurah VARCHAR, url VARCHAR)");
        cursor = QuranFragment.quranDBHelper.getFullSura("SELECT * FROM " + databaseName);

        rvAyahList = findViewById(R.id.rvAyahList);
        ayahList = new ArrayList<>();
        layoutManager = new CustomLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        suraAyahAdapter = new SuraAyahAdapter(ayahList);
        rvAyahList.setLayoutManager(layoutManager);
        rvAyahList.setHasFixedSize(true);
        rvAyahList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (count < lastVisibleItem) count = lastVisibleItem;
            }
        });
        ((SimpleItemAnimator) rvAyahList.getItemAnimator()).setSupportsChangeAnimations(false);
        rvAyahList.setAdapter(suraAyahAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        btnPlay.setOnClickListener(v -> togglePlay());
        btnNext.setOnClickListener(v -> playNext());
        btnPrevious.setOnClickListener(v -> playPrevious());

        if (cursor.getCount() == 0) {
            parseEngAyah();
        } else {
            getFullSura();
        }
    }

    private void togglePlay() {
        if (mediaPlayer == null) {
            playAudio(currentAudioIndex);
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlay.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mediaPlayer.start();
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void playAudio(int index) {
        if (index < 0 || index >= audioUrls.size()) return;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentAudioIndex = index;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrls.get(index));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                seekBar.setMax(mp.getDuration());
            });
            mediaPlayer.setOnCompletionListener(mp -> playNext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playNext() {
        if (currentAudioIndex < audioUrls.size() - 1) {
            playAudio(++currentAudioIndex);
        }
    }

    private void playPrevious() {
        if (currentAudioIndex > 0) {
            playAudio(--currentAudioIndex);
        }
    }

    private void getFullSura() {
        while (cursor.moveToNext()) {
            String arabicAyah = cursor.getString(2);
            String engAyah = cursor.getString(3);
            int number = Integer.parseInt(cursor.getString(1));
            numberInSurah = Integer.parseInt(cursor.getString(4));
            String url = cursor.getString(5);
            ayahList.add(new SuraAyah(number, arabicAyah, engAyah, numberInSurah, 0, 0, 0, 0, 0, false, url));
            if (numberInSurah >= lastReadAyat) audioUrls.add(url);
        }
        rvAyahList.scrollToPosition(lastReadAyat - 1);
        suraAyahAdapter.notifyDataSetChanged();
    }

    private void parseEngAyah() {
        progressDialog.show();
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(Request.Method.GET, Apis.suraAyahInEnglish + suraNo, null, response -> {
            try {
                JSONArray ayahArray = response.getJSONArray("ayahs");
                for (int i = 0; i < ayahArray.length(); i++) {
                    engAyahList.add(ayahArray.getJSONObject(i).getString("text"));
                }
                parseAllAyah();
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, error -> {
            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
        AppController.getInstance().addToRequestQueue(request);
    }

    private void parseAllAyah() {
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(Request.Method.GET, Apis.suraAyahInArabic + suraNo, null, response -> {
            try {
                JSONArray ayahArray = response.getJSONArray("ayahs");
                for (int i = 0; i < ayahArray.length(); i++) {
                    JSONObject object = ayahArray.getJSONObject(i);
                    String arabicAyah = object.getString("text");
                    String engAyah = engAyahList.get(i);
                    int number = object.getInt("number");
                    numberInSurah = object.getInt("numberInSurah");
                    String url = Apis.ayahAudio + number;
                    ayahList.add(new SuraAyah(number, arabicAyah, engAyah, numberInSurah, object.getInt("juz"), object.getInt("manzil"), object.getInt("page"), object.getInt("ruku"), object.getInt("hizbQuarter"), object.getBoolean("sajda"), url));
                    QuranFragment.quranDBHelper.insertSura(databaseName, String.valueOf(number), arabicAyah, engAyah, String.valueOf(numberInSurah), url);
                    if (numberInSurah >= lastReadAyat) audioUrls.add(url);
                }
                rvAyahList.scrollToPosition(lastReadAyat - 1);
                suraAyahAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, error -> {
            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.putString("suraNo", suraNo);
        editor.putString("suraName", suraName);
        editor.putString("suraArabicName", suraArabicName);
        editor.putInt("ayatPos", lastVisibleItem);
        editor.commit();
    }
            }
