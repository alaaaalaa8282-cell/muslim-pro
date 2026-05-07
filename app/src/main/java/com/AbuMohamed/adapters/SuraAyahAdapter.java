package com.AbuMohamed.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.AbuMohamed.R;
import com.AbuMohamed.models.SuraAyah;

import java.util.List;

public class SuraAyahAdapter extends RecyclerView.Adapter<SuraAyahAdapter.ViewHolder> {
    private List<SuraAyah> ayahList;

    public SuraAyahAdapter(List<SuraAyah> ayahList) {
        this.ayahList = ayahList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sura_ayah_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final SuraAyah items = ayahList.get(i);
        final String number = String.valueOf(items.getNumberInSurah());
        final String arabicText = items.getArabicText();
        final String engText = items.getEngText();
        final String arabicNum = number.replace("1", "١").replace("2", "٢")
                .replace("3", "٣").replace("4", "٤").replace("5", "٥")
                .replace("6", "٦").replace("7", "٧").replace("8", "٨")
                .replace("9", "٩").replace("0", "٠");
        viewHolder.setAyahDetails(arabicText, engText, number, arabicNum);
    }

    @Override
    public int getItemCount() {
        return ayahList == null ? 0 : ayahList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAyahInArabic, tvArabicNum, tvAyahInEng;
        View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setAyahDetails(String arabicAyah, String engAyah, String num, String arabicText) {
            tvAyahInArabic = mView.findViewById(R.id.tvAyahInArabic);
            tvAyahInArabic.setText(arabicAyah);
            tvArabicNum = mView.findViewById(R.id.tvArabicNum);
            tvArabicNum.setText(arabicText);
            tvAyahInEng = mView.findViewById(R.id.tvAyahInEng);
            tvAyahInEng.setText(num + ". " + engAyah);
        }
    }
}
