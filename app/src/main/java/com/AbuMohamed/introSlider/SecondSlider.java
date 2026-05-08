package com.AbuMohamed.introSlider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.AbuMohamed.App.IslamicProHelper;
import com.AbuMohamed.LocationPermissionActivity;
import com.AbuMohamed.PlaceSearchActivity;
import com.AbuMohamed.R;
import com.AbuMohamed.common.Common;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SecondSlider extends Fragment {

    TextView user_city_name;
    Button buttonLocationPermission, btnChange;
    private ProgressBar progress_circular;

    public static final int REQUEST_LOCATION = 199;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public SecondSlider() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second_slider, container, false);

        settings = getActivity().getSharedPreferences(IslamicProHelper.PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        buttonLocationPermission = view.findViewById(R.id.buttonLocationPermission);
        user_city_name = view.findViewById(R.id.user_city_name);
        btnChange = view.findViewById(R.id.btn_changePage);
        progress_circular = view.findViewById(R.id.progress_circular);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (!TextUtils.isEmpty(Common.placeName)) {
            user_city_name.setText(Common.placeName);
            btnChange.setVisibility(View.VISIBLE);
            btnChange.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            new Thread(() -> {
                String lat = getLatitudeFromAddress(requireContext(), Common.placeName);
                String lng = getLogitudeFromAddress(requireContext(), Common.placeName);
                new Handler(Looper.getMainLooper()).post(() -> {
                    editor.putString(IslamicProHelper.USER_LAT, lat);
                    editor.putString(IslamicProHelper.USER_LNG, lng);
                    editor.apply();
                });
            }).start();
        }

        user_city_name.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), PlaceSearchActivity.class));
            getActivity().finish();
        });

        btnChange.setOnClickListener(v -> {
            if (user_city_name.getText().length() != 0) {
                buttonLocationPermission.setEnabled(false);
                editor.putString(IslamicProHelper.USER_CITY, user_city_name.getText().toString());
                editor.apply();
                LocationPermissionActivity.viewPager.setCurrentItem(2);
            } else if (Common.city.equals("GPS") || !settings.getString(IslamicProHelper.USER_CITY, "").equals("")) {
                LocationPermissionActivity.viewPager.setCurrentItem(2);
            } else {
                Toast.makeText(getContext(), "You must need to turn on your location or input your city", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLocationPermission.setOnClickListener(v -> {
            buttonLocationPermission.setVisibility(View.INVISIBLE);
            progress_circular.setVisibility(View.VISIBLE);
            checkAndRequestLocation();
        });

        return view;
    }

    private void checkAndRequestLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialog, i) ->
                                ActivityCompat.requestPermissions(requireActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION))
                        .show();
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2500)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    new Thread(() -> {
                        try {
                            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String cityName = addresses.get(0).getLocality();
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    editor.putString(IslamicProHelper.USER_CITY, cityName);
                                    editor.putString(IslamicProHelper.USER_LAT, String.valueOf(location.getLatitude()));
                                    editor.putString(IslamicProHelper.USER_LNG, String.valueOf(location.getLongitude()));
                                    editor.apply();
                                    progress_circular.setVisibility(View.GONE);
                                    user_city_name.setText(cityName);
                                    user_city_name.setVisibility(View.VISIBLE);
                                    btnChange.setVisibility(View.VISIBLE);
                                    btnChange.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                                });
                            }
                        } catch (IOException e) {
                            Log.e("GEOCODER", e.getMessage());
                        }
                    }).start();
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public String getLogitudeFromAddress(Context context, String strAddress) {
        try {
            Geocoder coder = new Geocoder(context);
            List<Address> address = coder.getFromLocationName(strAddress, 1);
            if (address != null && !address.isEmpty())
                return String.valueOf(address.get(0).getLongitude());
        } catch (IOException ex) { Log.e("GEOCODER", ex.getMessage()); }
        return null;
    }

    public String getLatitudeFromAddress(Context context, String strAddress) {
        try {
            Geocoder coder = new Geocoder(context);
            List<Address> address = coder.getFromLocationName(strAddress, 1);
            if (address != null && !address.isEmpty())
                return String.valueOf(address.get(0).getLatitude());
        } catch (IOException ex) { Log.e("GEOCODER", ex.getMessage()); }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                progress_circular.setVisibility(View.GONE);
                buttonLocationPermission.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
