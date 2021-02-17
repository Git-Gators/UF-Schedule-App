package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class ViewSchedule extends MainActivity {


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_view);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_schedule);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }

    /** Called when the user taps the Send button */
    public void goToCourseFinder(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = 0;
                    Intent in;
                    switch(item.getItemId()){
                        case R.id.nav_home:
                            id = R.id.nav_schedule;
                            in = new Intent(getBaseContext(), MainActivity.class);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            break;
                        case R.id.nav_schedule:
                            break;
                        case R.id.nav_calendar:
                            id = R.id.nav_calendar;
                            break;
                    }
                    System.out.println(id);
                    return true;
                }
            };
}
