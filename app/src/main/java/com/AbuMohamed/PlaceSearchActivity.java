package com.AbuMohamed;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.AbuMohamed.common.Common;

import java.util.Arrays;
import java.util.List;

public class PlaceSearchActivity extends AppCompatActivity {

    AutocompleteSupportFragment autocompleteSupportFragment;
    PlacesClient placesClient;
    List<Place.Field> placeFeilds = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_place_search);

        Places.initialize(this, getString(R.string.placeApiKey));

        placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_location_pickup);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i("PLACENAME", "Place: " + place.getName() + ", " + place.getLatLng());
                Common.placeName = place.getName();
                startActivity(new Intent(PlaceSearchActivity.this, LocationPermissionActivity.class));
                finish();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("PLACE_ERROR", "An error occurred: " + status);
            }
        });
    }
}
