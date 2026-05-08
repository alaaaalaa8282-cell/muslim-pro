package com.AbuMohamed;
import com.AbuMohamed.App.IslamicProHelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.IslamicCalendar;
import android.icu.util.TimeZone;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.AbuMohamed.App.Apis;
import com.AbuMohamed.common.Common;
import com.AbuMohamed.helper.NoInternet;
import com.AbuMohamed.models.Ramadan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SplashScreenActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private List<String> prayerTimeList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private String mDate, mDate2, share_date;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    private Gson gson = new Gson();
    private AlertDialog appControlDialog;
    private ImageView ivWifi, ivMobileData, ivCancel;
    private WifiManager wifi;
    private String city;
    private Runnable runnable;
    private String latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        Common.ramadanList = new ArrayList<>();

        settings = getSharedPreferences(IslamicProHelper.PREFS_NAME, Context.MODE_PRIVATE);
        city = settings.getString(IslamicProHelper.USER_CITY, "");

        if (city.equals("")) {
            Common.permission = "request";
            startActivity(new Intent(SplashScreenActivity.this, LocationPermissionActivity.class));
            finish();
            return;
        }

        sharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mRequestQueue = Volley.newRequestQueue(this);
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        mDate2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        latitude = settings.getString(IslamicProHelper.USER_LAT, "");
        longitude = settings.getString(IslamicProHelper.USER_LNG, "");

        if (latitude.isEmpty() && longitude.isEmpty()) {
            Common.ramadanList.add(new Ramadan("0", "0", "0", "0", "0", "0"));
        }

        wifi = (WifiManager) SplashScreenActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (NoInternet.isConnected(SplashScreenActivity.this)) {
            isEmptycheck();
        } else {
            showAlert();
        }
    }

    private void isEmptycheck() {
        String json = sharedPreferences.getString("Set", "");
        if (json.isEmpty()) {
            prayerTimeParsing(mDate, latitude, longitude);
        } else {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> arrayData = gson.fromJson(json, type);
            for (int i = 0; i < arrayData.size(); i++) {
                share_date = arrayData.get(5);
            }
            if (!mDate2.equals(share_date)) {
                prayerTimeParsing(mDate, latitude, longitude);
            } else {
                ramajanJson(latitude, longitude);
            }
        }
    }

    private void startActivity() {
        startActivity(new Intent(SplashScreenActivity.this, HomePage.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convert_date(String raw_date) {
        Date final_date = null, final_date2 = null;
        String isMOnthName = null;
        String isdayth = null;
        try {
            final_date = new SimpleDateFormat("yyyy-MM-dd").parse(raw_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(final_date);
        int eYear = calendar.get(Calendar.YEAR);
        int eMonth = calendar.get(Calendar.MONTH);
        int eday = calendar.get(Calendar.DAY_OF_MONTH);
        String new_date = String.valueOf(eYear + "-" + eMonth + "-" + eday);
        try {
            final_date2 = new SimpleDateFormat("yyyy-MM-dd").parse(new_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        IslamicCalendar calendar1 = new IslamicCalendar();
        calendar1.setTimeZone(TimeZone.getDefault());
        calendar1.setTime(final_date2);
        int isYear = calendar1.get(IslamicCalendar.YEAR);
        int isMontth = calendar1.get(IslamicCalendar.MONTH) + 1;
        int isDay = calendar1.get(IslamicCalendar.DAY_OF_MONTH);

        if (isMontth == 0) isMOnthName = "Muharram";
        else if (isMontth == 1) isMOnthName = "Safar";
        else if (isMontth == 2) isMOnthName = "Rabi-ul-Awwal";
        else if (isMontth == 3) isMOnthName = "Rabi-ul-Aakhir";
        else if (isMontth == 4) isMOnthName = "Jamadi-ul-Awwal";
        else if (isMontth == 5) isMOnthName = "Jamadi-ul-Aakhir";
        else if (isMontth == 6) isMOnthName = "Rajab";
        else if (isMontth == 7) isMOnthName = "Shaban";
        else if (isMontth == 8) isMOnthName = "Ramadan";
        else if (isMontth == 9) isMOnthName = "Shawwal";
        else if (isMontth == 10) isMOnthName = "Zulqaida";
        else if (isMontth == 11) isMOnthName = "Zulhijja";

        if (isDay == 1) isdayth = "st ";
        else if (isDay == 2) isdayth = "nd ";
        else if (isDay == 3) isdayth = "rd ";
        else if (3 < isDay && isDay < 31) isdayth = "th ";

        return String.valueOf(isDay + isdayth + isMOnthName + ", " + isYear);
    }

    private void prayerTimeParsing(final String mDate, final String lat, final String lng) {
        String url = Apis.prayerTime + mDate + "&latitude=" + lat + "&longitude=" + lng;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String fajr = null, dhuhr = null, asr = null, maghrib = null, isha = null;
                            String hijri_date = null;

                            JSONObject data = response.getJSONObject("data");
                            JSONObject timings = data.getJSONObject("timings");
                            JSONObject date = data.getJSONObject("date");
                            JSONObject hijri = date.getJSONObject("hijri");
                            JSONObject gregorian = date.getJSONObject("gregorian");

                            fajr = timings.getString("Fajr");
                            dhuhr = timings.getString("Dhuhr");
                            asr = timings.getString("Asr");
                            maghrib = timings.getString("Maghrib");
                            isha = timings.getString("Isha");

                            String hijriDay = hijri.getString("day");
                            String hijriMonth = hijri.getJSONObject("month").getString("en");
                            String hijriYear = hijri.getString("year");
                            hijri_date = hijriDay + " " + hijriMonth + " " + hijriYear;

                            String gregDate = gregorian.getString("date");
                            String cityName = city;

                            prayerTimeList.clear();
                            prayerTimeList.add(fajr);
                            prayerTimeList.add(dhuhr);
                            prayerTimeList.add(asr);
                            prayerTimeList.add(maghrib);
                            prayerTimeList.add(isha);
                            prayerTimeList.add(mDate2);
                            prayerTimeList.add(cityName);

                            String json = gson.toJson(prayerTimeList);
                            editor.putString("Set", json);
                            editor.commit();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ramajanJson(lat, lng);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ramajanJson(lat, lng);
                    }
                });
        mRequestQueue.add(request);
    }

    private void ramajanJson(final String lat, final String longi) {
        if (lat.isEmpty() || longi.isEmpty()) {
            startActivity();
            return;
        }

        String url = Apis.ramadan + lat + "&longitude=" + longi + "&month=" +
                (Calendar.getInstance().get(Calendar.MONTH) + 1) +
                "&year=" + Calendar.getInstance().get(Calendar.YEAR);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray dataArray = response.getJSONArray("data");
                            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
                            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");

                            for (int i = 0; i < dataArray.length(); i++) {
                                try {
                                    JSONObject object = dataArray.getJSONObject(i);
                                    String toDaySehri = null, toDayIftar = null;
                                    String currDate = null, hijriDate = null, hijriDay = null, engDay = null;

                                    JSONObject timings = object.getJSONObject("timings");
                                    toDaySehri = timings.getString("Imsak");
                                    toDayIftar = timings.getString("Maghrib");

                                    if (toDaySehri.length() > 5) toDaySehri = toDaySehri.substring(0, 5);
                                    if (toDayIftar.length() > 5) toDayIftar = toDayIftar.substring(0, 5);

                                    try {
                                        Date d1 = _24HourSDF.parse(toDaySehri);
                                        toDaySehri = _12HourSDF.format(d1);
                                        Date d2 = _24HourSDF.parse(toDayIftar);
                                        toDayIftar = _12HourSDF.format(d2);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    JSONObject date = object.getJSONObject("date");
                                    currDate = date.getString("readable");
                                    engDay = date.getJSONObject("gregorian").getJSONObject("weekday").getString("en");

                                    JSONObject hijri = date.getJSONObject("hijri");
                                    hijriDate = hijri.getString("date");
                                    hijriDay = hijri.getString("day");

                                    Common.ramadanList.add(new Ramadan(currDate, hijriDate, hijriDay, toDaySehri, toDayIftar, engDay));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        startActivity();
                    }
                });
        mRequestQueue.add(request);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void showAlert() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View controlDialogView = factory.inflate(R.layout.internet_checker_dialog, null);
        appControlDialog = new AlertDialog.Builder(this).create();
        appControlDialog.setView(controlDialogView);

        ivWifi = controlDialogView.findViewById(R.id.ivWifi);
        ivMobileData = controlDialogView.findViewById(R.id.ivMobileData);
        ivCancel = controlDialogView.findViewById(R.id.ivCancel);

        ivWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifi.setWifiEnabled(true);
                appControlDialog.dismiss();
                try {
                    Handler handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            handler.postDelayed(this, 5000);
                            if (NoInternet.isConnected(SplashScreenActivity.this)) {
                                Refresh.refreshActivity(SplashScreenActivity.this);
                                handler.removeCallbacks(runnable);
                            }
                        }
                    };
                    handler.postDelayed(runnable, 0);
                    Toast.makeText(SplashScreenActivity.this, "WiFi Enabling in 5sec Please Wait", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.d("WIFI_ERROR", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });

        ivMobileData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NoInternet.isConnected(SplashScreenActivity.this) || wifi.isWifiEnabled()) {
                    isEmptycheck();
                } else {
                    Intent dataSettings = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                    startActivity(dataSettings);
                    appControlDialog.dismiss();
                }
            }
        });

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appControlDialog.dismiss();
            }
        });
        appControlDialog.show();
    }
}
