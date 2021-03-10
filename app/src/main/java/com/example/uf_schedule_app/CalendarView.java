package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.PopupWindow;
import android.app.AlertDialog;

import android.content.Intent;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class CalendarView extends MainActivity {
    RecyclerView recyclerView;

    String s1[], s2[];
    int images[] = {R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24};

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        recyclerView = findViewById(R.id.recyclerView);

        s1 = getResources().getStringArray(R.array.programming_languages);
        s2 = getResources().getStringArray(R.array.description);

        Calendar_Adapter calendarAdapter = new Calendar_Adapter(this, s1, s2, images);
        recyclerView.setAdapter(calendarAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}
