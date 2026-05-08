package com.AbuMohamed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.IslamicCalendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.AbuMohamed.App.Apis;
import com.AbuMohamed.App.IslamicProHelper;
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
    private String city;
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

        if (NoInternet.isConnected(SplashScreenActivity.this)) {
            isEmptycheck();
        } else {
            showAlert();
        }
    }

    private void isEmptycheck() {
        String json = sharedPreferences.getString("Set", "");
        if (json.isEmpty()) {
            prayerTimeParsing(mDate, city);
        } else {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> arrayData = gson.fromJson(json, type);
            for (int i = 0; i < arrayData.size(); i++) {
                share_date = arrayData.get(5);
            }
            if (!mDate2.equals(share_date)) {
                prayerTimeParsing(mDate, city);
            } else {
                goToHome();
            }
        }
    }

    private void goToHome() {
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
        String new_date = eYear + "-" + eMonth + "-" + eday;
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

        String[] months = {"Muharram","Safar","Rabi-ul-Awwal","Rabi-ul-Aakhir","Jamadi-ul-Awwal",
                "Jamadi-ul-Aakhir","Rajab","Shaban","Ramadan","Shawwal","Zulqaida","Zulhijja"};
        if (isMontth >= 1 && isMontth <= 12) isMOnthName = months[isMontth - 1];

        if (isDay == 1) isdayth = "st ";
        else if (isDay == 2) isdayth = "nd ";
        else if (isDay == 3) isdayth = "rd ";
        else isdayth = "th ";

        return isDay + isdayth + isMOnthName + ", " + isYear;
    }

    private void prayerTimeParsing(final String mDate, final String locat) {
        StringRequest request = new StringRequest(Request.Method.GET, Apis.prayerTime + mDate + "&latitude=" + latitude + "&longitude=" + longitude,
                response -> {
                    try {
                        String j_date = null, hijri_date = null, fajr = null, dhuhr = null, asr = null, maghrib = null, isha = null;
                        JSONObject object = new JSONObject(response);
                        String cityName = object.getString("city");
                        if (object.has("items")) {
                            JSONArray jsonArray = object.getJSONArray("items");
                            for (int j = 0; j < jsonArray.length(); j++) {
                                JSONObject arrayObject = jsonArray.getJSONObject(j);
                                j_date = arrayObject.getString("date_for");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    hijri_date = convert_date(j_date);
                                }
                                fajr = arrayObject.getString("fajr");
                                dhuhr = arrayObject.getString("dhuhr");
                                asr = arrayObject.getString("asr");
                                maghrib = arrayObject.getString("maghrib");
                                isha = arrayObject.getString("isha");
                            }
                        }
                        prayerTimeList.add(fajr);
                        prayerTimeList.add(dhuhr);
                        prayerTimeList.add(asr);
                        prayerTimeList.add(maghrib);
                        prayerTimeList.add(isha);
                        prayerTimeList.add(hijri_date);
                        prayerTimeList.add(cityName);
                        editor.putString("Set", gson.toJson(prayerTimeList));
                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ramajanJson(latitude, longitude);
                },
                error -> ramajanJson(latitude, longitude));
        mRequestQueue.add(request);
    }

    private void ramajanJson(final String lat, final String longi) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, Apis.ramadan + lat + "&long=" + longi, null,
                response -> {
                    SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
                    SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject object = response.getJSONObject(i);
                            String toDaySehri = null, toDayIftar = null, currDate = null, hijriDate = null, hijriDay = null, engDay = null;
                            if (object.has("timings")) {
                                JSONObject object1 = object.getJSONObject("timings");
                                toDaySehri = object1.getString("Imsak");
                                if (toDaySehri.length() > 0) {
                                    toDaySehri = toDaySehri.substring(0, toDaySehri.length() - 5);
                                    toDaySehri = _12HourSDF.format(_24HourSDF.parse(toDaySehri));
                                }
                                toDayIftar = object1.getString("Maghrib");
                                if (toDayIftar.length() > 0) {
                                    toDayIftar = toDayIftar.substring(0, toDayIftar.length() - 5);
                                    toDayIftar = _12HourSDF.format(_24HourSDF.parse(toDayIftar));
                                }
                            }
                            if (object.has("date")) {
                                JSONObject objectDate = object.getJSONObject("date");
                                String[] dddd = ramadanDate(objectDate.getString("readable")).split("/");
                                currDate = dddd[0];
                                engDay = dddd[1];
                                if (objectDate.has("hijri")) {
                                    JSONObject hijriObject = objectDate.getJSONObject("hijri");
                                    hijriDate = hijriObject.getString("date");
                                    hijriDay = hijriObject.getString("day") + "th";
                                }
                            }
                            Common.ramadanList.add(new Ramadan(currDate, hijriDate, hijriDay, toDaySehri, toDayIftar, engDay));
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    goToHome();
                },
                error -> goToHome());
        mRequestQueue.add(request);
    }

    private void showAlert() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View controlDialogView = factory.inflate(R.layout.internet_checker_dialog, null);
        appControlDialog = new AlertDialog.Builder(this).create();
        appControlDialog.setView(controlDialogView);

        ivWifi = controlDialogView.findViewById(R.id.ivWifi);
        ivMobileData = controlDialogView.findViewById(R.id.ivMobileData);
        ivCancel = controlDialogView.findViewById(R.id.ivCancel);

        // Android 10+ - can't enable WiFi programmatically, open settings instead
        ivWifi.setOnClickListener(v -> {
            appControlDialog.dismiss();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });

        ivMobileData.setOnClickListener(v -> {
            if (NoInternet.isConnected(SplashScreenActivity.this)) {
                appControlDialog.dismiss();
                isEmptycheck();
            } else {
                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                appControlDialog.dismiss();
            }
        });

        ivCancel.setOnClickListener(v -> appControlDialog.dismiss());
        appControlDialog.show();
    }

    private String ramadanDate(String raw_date) {
        Date final_date = null, final_date2 = null;
        try {
            final_date = new SimpleDateFormat("dd MMM yyyy").parse(raw_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(final_date);
        String new_date = calendar.get(Calendar.YEAR) + " " + (calendar.get(Calendar.MONTH) + 1) + " " + (calendar.get(Calendar.DAY_OF_MONTH) + 1);
        try {
            final_date2 = new SimpleDateFormat("yyyy MM dd").parse(new_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat("dd MMM yyyy/EEEE").format(final_date2);
    }
}
