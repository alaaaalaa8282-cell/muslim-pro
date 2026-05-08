package com.AbuMohamed.introSlider;
import com.AbuMohamed.App.IslamicProHelper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import com.AbuMohamed.LocationPermissionActivity;
import com.AbuMohamed.PlaceSearchActivity;
import com.AbuMohamed.R;
import com.AbuMohamed.common.Common;

import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class SecondSlider extends Fragment {

    TextView user_city_name;
    Button buttonLocationPermission, btnChange;
    private ProgressBar progress_circular;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public final static int REQUEST_LOCATION = 199;
    private Geocoder geocoder;
    public String locationName = "null";
    private double latitude, longitude;
    Handler handler = new Handler();
    Runnable runnable;
    private int isClick = 0;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    public SecondSlider() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second_slider, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        settings = getActivity().getSharedPreferences(IslamicProHelper.PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        buttonLocationPermission = view.findViewById(R.id.buttonLocationPermission);
        user_city_name = view.findViewById(R.id.user_city_name);
        btnChange = view.findViewById(R.id.btn_changePage);
        progress_circular = view.findViewById(R.id.progress_circular);

        if (!TextUtils.isEmpty(Common.placeName)) {
            user_city_name.setText(Common.placeName);
            btnChange.setVisibility(View.VISIBLE);
            btnChange.setTextColor(getResources().getColor(R.color.colorPrimary));
            editor.putString(IslamicProHelper.USER_LAT, getLatitudeFromAddress(getContext(), Common.placeName));
            editor.putString(IslamicProHelper.USER_LNG, getLogitudeFromAddress(getContext(), Common.placeName));
            editor.commit();
        }

        user_city_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PlaceSearchActivity.class));
                getActivity().finish();
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_city_name.getText().length() != 0) {
                    buttonLocationPermission.setEnabled(false);
                    editor.putString(IslamicProHelper.USER_CITY, user_city_name.getText().toString());
                    editor.commit();
                    LocationPermissionActivity.viewPager.setCurrentItem(2);
                } else if (Common.city.equals("GPS") || !settings.getString(IslamicProHelper.USER_CITY, "").equals("")) {
                    LocationPermissionActivity.viewPager.setCurrentItem(2);
                } else {
                    Toast.makeText(getContext(), "You must need to turn on your location or input your city", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLocationPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermission();
                buttonLocationPermission.setVisibility(View.INVISIBLE);
                progress_circular.setVisibility(View.VISIBLE);
                isClick = 1;
                getCurrentLocation();
            }
        });

        return view;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    try {
                        geocoder = new Geocoder(getContext());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses != null && addresses.size() > 0) {
                            locationName = addresses.get(0).getLocality();
                            editor.putString(IslamicProHelper.USER_CITY, locationName);
                            editor.putString(IslamicProHelper.USER_LAT, String.valueOf(latitude));
                            editor.putString(IslamicProHelper.USER_LNG, String.valueOf(longitude));
                            editor.commit();

                            progress_circular.setVisibility(View.GONE);
                            user_city_name.setText(locationName);
                            user_city_name.setVisibility(View.VISIBLE);
                            user_city_name.setEnabled(false);
                            btnChange.setVisibility(View.VISIBLE);
                            btnChange.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("ERROR_HERE", e.getMessage());
                    }
                } else {
                    Toast.makeText(getContext(), "Please turn on your GPS", Toast.LENGTH_SHORT).show();
                    progress_circular.setVisibility(View.GONE);
                    buttonLocationPermission.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public String getLogitudeFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        String lng = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) return null;
            Address location = address.get(0);
            lng = String.valueOf(location.getLongitude());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return lng;
    }

    public String getLatitudeFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        String lat = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) return null;
            Address location = address.get(0);
            lat = String.valueOf(location.getLatitude());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return lat;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            checkLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                checkLocationPermission();
            }
        }
    }
}
