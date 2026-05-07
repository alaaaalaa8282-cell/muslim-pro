package com.AbuMohamed.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.AbuMohamed.R;
import com.AbuMohamed.adapters.RamadanAdapter;
import com.AbuMohamed.common.Common;
import com.AbuMohamed.models.Ramadan;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RamadanFragment extends Fragment {
    private Toolbar tollbar;
    private RecyclerView rvRamadan;
    private RamadanAdapter ramadanAdapter;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();
    public RamadanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ramadan, container, false);

        rvRamadan = view.findViewById(R.id.rvRamadan);
        rvRamadan.setLayoutManager(new LinearLayoutManager(getContext()));
        ramadanAdapter = new RamadanAdapter(getContext(), Common.ramadanList);
        rvRamadan.setAdapter(ramadanAdapter);
        ramadanAdapter.notifyDataSetChanged();
        rvRamadan.hasFixedSize();

        return view;
    }

}
