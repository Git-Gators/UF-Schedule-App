package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import android.content.Intent;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    //Create a dbUpdater object
    DatabaseUpdater dbUpdater = new DatabaseUpdater();
    ListView courseList;
    ListView chosenCourses;
    ArrayList<String> courses = new ArrayList<>();
    ArrayList<String> coursesPicked = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //Course List
        courseList = findViewById(R.id.courseList);
        chosenCourses = findViewById(R.id.chosenCourses);

        //If we're coming from the filter, we grab the info
        Bundle b = getIntent().getExtras();
        if(b != null){
            if(b.getStringArrayList("coursesPicked") != null){
                coursesPicked = b.getStringArrayList("coursesPicked");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coursesPicked);
                chosenCourses.setAdapter(arrayAdapter);
                System.out.println("coursesPicked from onCreate Main: " + coursesPicked.toString());
            }
            if(b.getStringArrayList("courses") != null){
                courses = b.getStringArrayList("courses");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, courses);
                courseList.setAdapter(arrayAdapter);
            }
            if(b.getString("course") != null){
                courses.add(b.getString("course"));
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, courses);
                courseList.setAdapter(arrayAdapter);
            }
//            if(b.getString("semester") != null){
//                TextView semester = findViewById(R.id.semesterText);
//                semester.setText(b.getString("semester"));
//            }
//            if(b.getString("department") != null){
//               TextView department = findViewById(R.id.departmentText);
//               department.setText(b.getString("department"));
//            }
            //if courses == null and department isn't => Grab all department courses and put them into the list
        }

        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                coursesPicked.add(courses.get(position));
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, coursesPicked);
                chosenCourses.setAdapter(arrayAdapter);
            }
        });
    }

    /** Called when the user taps the Filter button */
    public void goToFilter(View view){
        Intent intent = new Intent(this, FilterActivity.class);
        Bundle b = new Bundle();
        b.putStringArrayList("coursesPicked", coursesPicked);
        System.out.println("coursesPicked from goToFilter: " + coursesPicked.toString());
        intent.putExtras(b);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = 0;
                    Intent in;
                    switch(item.getItemId()){
                        case R.id.nav_home:
                            id = R.id.nav_home;
                            break;
                        case R.id.nav_schedule:
                            id = R.id.nav_schedule;
                            in = new Intent(getBaseContext(), ViewSchedule.class);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            break;
                        case R.id.nav_calendar:
                            id = R.id.nav_calendar;
                            break;
                    }
                    System.out.println(id);
                    return false;
                }
            };
}
