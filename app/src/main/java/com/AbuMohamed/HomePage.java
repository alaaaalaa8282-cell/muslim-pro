package com.AbuMohamed;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.AbuMohamed.common.Common;
import com.AbuMohamed.fragments.CompassFragment;
import com.AbuMohamed.fragments.IslamicCalendarFragment;
import com.AbuMohamed.fragments.NearByMosqueFragment;
import com.AbuMohamed.fragments.NientyNineNameFragment;
import com.AbuMohamed.fragments.PrayerTimeFragment;
import com.AbuMohamed.fragments.RamadanFragment;
import com.AbuMohamed.homePageFragments.MakkaLiveFragment;
import com.AbuMohamed.homePageFragments.PrayerFragment;
import com.AbuMohamed.homePageFragments.QuranFragment;
import com.AbuMohamed.homePageFragments.TazbiFragment;
import com.AbuMohamed.recever.Alarm;

import static com.AbuMohamed.introSlider.SecondSlider.REQUEST_LOCATION;

public class HomePage extends AppCompatActivity {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    public static ConstraintLayout constraintLayout, mainBacground;
    private ActionBarDrawerToggle toggle;
    public static ImageView ivMenu, ivDropdown, ivNavigation;
    private LinearLayout tvAllahName, tvRamadan, tvIslamicCalender, tvMakkahLive, tvTazbi, tvCivla, tvQuran, tvDues,
            tvSettingd, tvPraysTime, tvHajjAndUmrah, tvZaket;

    public static ConstraintLayout mainActToolbar;
    public static TextView tvToolbar_Name;
    public static NavigationView nav_view;
    Alarm alarm = new Alarm();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home_page);

        alarm.setAlarm(this);

        /* Set Onclick Navigation Drawer Layout */
        drawer = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // findViewById
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        /* First Time Selected Item View in BottomNavigationView */
        bottomNavigationView.setSelectedItemId(R.id.nav_prayer);

        // Dropdown Menu
        ivDropdown = findViewById(R.id.ivDropdown);
        constraintLayout = findViewById(R.id.constraintLayout);
        mainBacground = findViewById(R.id.mainBacground);
        tvToolbar_Name = findViewById(R.id.tvToolbar_Name);

        ivMenu = findViewById(R.id.ivMenu);
        ivNavigation = findViewById(R.id.ivNavigation);

        //------navigation drawer------
        tvAllahName = findViewById(R.id.tvAllahName);
        tvRamadan = findViewById(R.id.tvRamadan);
        tvIslamicCalender = findViewById(R.id.tvIslamicCalender);
        tvMakkahLive = findViewById(R.id.tvMakkahLive);
        tvTazbi = findViewById(R.id.tvTazbi);
        tvCivla = findViewById(R.id.tvCivla);
        tvQuran = findViewById(R.id.tvQuran);
        tvDues = findViewById(R.id.tvDues);
        tvSettingd = findViewById(R.id.tvSettingd);
        tvPraysTime = findViewById(R.id.tvPraysTime);
        tvHajjAndUmrah = findViewById(R.id.tvHajjAndUmrah);
        tvZaket = findViewById(R.id.tvZaket);

        //mainActToolbar = findViewById(R.id.mainActToolbar);

        // ivMenu view On Click
        ivMenu.setOnClickListener((View view) -> {
                drawer.openDrawer(GravityCompat.START);

        });

        ivNavigation.setOnClickListener((View v) ->{
//                SharedPreferences settings = getSharedPreferences(IslamicProHelper.PREFS_NAME, Context.MODE_PRIVATE);
//                String city = settings.getString(IslamicProHelper.USER_CITY, "");

                mChangeFragment_2(new CompassFragment());
                Common.toolbarVisibility("visible", "Qibla", 1);

        });


        // NavigationDrawer Button On Click below

        //=====Allah 99 name On click======
        tvAllahName.setOnClickListener((View view) -> {
            mChangeFragment_2(new NientyNineNameFragment());
            drawer.closeDrawers();
            Common.toolbarVisibility("visible", "99 Names Of Allah", 1);
        });

        //=====Ramadan On click======
        tvRamadan.setOnClickListener((View view) -> {
                drawer.closeDrawers();
                mChangeFragment_2(new RamadanFragment());
                Common.toolbarVisibility("visible", "", 2);
        });

        //=====Calender On click======
        tvIslamicCalender.setOnClickListener((View view) ->{
                drawer.closeDrawers();
                mChangeFragment_2(new IslamicCalendarFragment());
                Common.toolbarVisibility("visible", "Islamic Calender", 1);

        });

        //=====M On click======
        tvMakkahLive.setOnClickListener((View view)-> {
                drawer.closeDrawers();
                mChangeFragment_2(new MakkaLiveFragment());
                Common.toolbarVisibility("visible", "Makkah Live", 1);

        });

        //=====Tazbi On click======
        tvTazbi.setOnClickListener((View view)-> {
                mChangeFragment_2(new TazbiFragment());
                drawer.closeDrawers();
                Common.toolbarVisibility("visible", "Tazbi", 1);

        });

        //=====Kibla On click======
        tvCivla.setOnClickListener((View view) ->{
                mChangeFragment_2(new CompassFragment());
                drawer.closeDrawers();
                Common.toolbarVisibility("visible", "Qibla", 1);

        });

        //=====Quran On click======
        tvQuran.setOnClickListener((View v) ->{
                mChangeFragment_2(new QuranFragment());
                drawer.closeDrawers();
                Common.toolbarVisibility("visible", "Al-Quran", 1);

        });

        //=====Dues On click======
        tvDues.setOnClickListener((View v)-> {
                drawer.closeDrawers();
                // Common.toolbarVisibility("visible","",1);


        });

        //=====Near by Mosque On click======
        tvSettingd.setOnClickListener((View v) ->{
                mChangeFragment_2(new NearByMosqueFragment());
                drawer.closeDrawers();
                Common.toolbarVisibility("visible", "Near Mosque", 1);

        });

        //=====PrayerTime On click======
        tvPraysTime.setOnClickListener((View view)-> {
                mChangeFragment_2(new PrayerTimeFragment());
                drawer.closeDrawers();
                Common.toolbarVisibility("visible", "Prayer Time", 1);

        });

        //=====Hajj and Ummrah On click======
        tvHajjAndUmrah.setOnClickListener((View view) ->{

                drawer.closeDrawers();
                Common.toolbarVisibility("visible", "HAjj And Ummrah", 1);

        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            //finish();
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            int id = menuItem.getItemId();
            if (id == R.id.nav_tazbi) {
                mChangeFragment(new TazbiFragment());
            } else if (id == R.id.nav_quran) {
                mChangeFragment(new QuranFragment());
            } else if (id == R.id.nav_prayer) {
                mChangeFragment(new PrayerFragment());
            } else if (id == R.id.nav_hajj_umrah) {
                mChangeFragment(new NearByMosqueFragment());
            } else if (id == R.id.nav_makka_live) {
                mChangeFragment(new MakkaLiveFragment());
            }
            return true;
        }
    };


    public void mChangeFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }

    public void mChangeFragment_2(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }


}

