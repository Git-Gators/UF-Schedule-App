package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.Objects;

public class FilterActivity extends MainActivity implements AdapterView.OnItemSelectedListener {

    String department = "";
    String courseName = "";
    String semester;

    //Spinner Objects (Drop Down Lists)
    Spinner spinner;
    Spinner spinnerDept;
    Spinner spinnerCrse;

    //Progress Bars (Spinning load icon)
    private ProgressBar pSpinner;
    private ProgressBar pSpinner2;
    private ProgressBar pSpinner3;

    //Used in the Spinner
    ArrayList<String> deptNames = new ArrayList<>();
    ArrayList<String> coursesNames = new ArrayList<>();
    ArrayList<String> courses = new ArrayList<>();
    ArrayList<String> coursesPicked = new ArrayList<>();

    //Used in the filters
    EditText courseCodeText;
    EditText courseCreditsText;
    EditText courseNameText;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_view);

        getSupportActionBar().setTitle("Filters");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ProgressBar filterLoad = findViewById(R.id.filterLoad);
        filterLoad.setVisibility(View.INVISIBLE);
        Button filterButton = findViewById(R.id.button3);
        filterButton.setVisibility(View.VISIBLE);

        //If we're coming from the filter, we grab the info
        Bundle b = getIntent().getExtras();
        if(b != null){
            if(b.getStringArrayList("coursesPicked") != null){
                coursesPicked = b.getStringArrayList("coursesPicked");
            }
        }

        pSpinner = findViewById(R.id.progressBar);
        pSpinner.setVisibility(View.INVISIBLE);
        pSpinner2 = findViewById(R.id.progressBar2);
        pSpinner2.setVisibility(View.INVISIBLE);

        //Update the lists
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerDept = (Spinner) findViewById(R.id.spinnerDepartments);
        spinnerCrse = (Spinner) findViewById(R.id.spinnerCourse);

        spinner.setOnItemSelectedListener(this);
        spinnerDept.setOnItemSelectedListener(this);
        spinnerCrse.setOnItemSelectedListener(this);
        String[] semesters = new String[]{"Select a Semester", "Spring 2021"};
        deptNames.add("Please Select a Semester First");
        coursesNames.add("Please Select a Semester First");

        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, semesters);
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, deptNames);
        final ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, coursesNames);

        // Specify the layout to use when the list of choices appears
        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerArrayAdapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerArrayAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerArrayAdapter);
        spinnerDept.setAdapter(spinnerArrayAdapter2);
        spinnerCrse.setAdapter(spinnerArrayAdapter1);


        //Text Listeners
        courseCodeText = (EditText) findViewById(R.id.courseCode);
        courseCreditsText = (EditText) findViewById(R.id.courseCredits);
        courseNameText = (EditText) findViewById(R.id.courseTitle);
    }

    public void filterCourses(String code, String credits, String name){
        System.out.println(code + " " + credits + " "  + name);

        //Hide the filter button and show the load
        ProgressBar filterLoad = findViewById(R.id.filterLoad);
        filterLoad.setVisibility(View.VISIBLE);
        Button filterButton = findViewById(R.id.button3);
        filterButton.setVisibility(View.INVISIBLE);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean match;
                System.out.println(department);
                courses.clear();

                for (DataSnapshot dep : dataSnapshot.getChildren()) {
                        for (DataSnapshot ds : dep.getChildren()) {
                            if(!dep.getKey().equals(department) && !department.equals("")) {
                                if(!department.equals("Choose a Department"))
                                    continue;
                            }
                            match = false;
                            //Only one field entered
                            if (ds.getValue(Course.class).courseInfo.get("name").contains(name) && credits.equals("") && code.equals("")) {
                                match = true;
                                System.out.println("MATCH: " + name + " " + ds.getValue(Course.class).courseInfo.get("name") + ";");
                                System.out.println("Matched on only name");
                            } else if (Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSections.get(0).get("credits"), credits) && name.equals("") && code.equals("")) {
                                match = true;
                                System.out.println("Matched on only credits");
                            } else if (Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code) && name.equals("") && credits.equals("")) {
                                match = true;
                                System.out.println("Matched on only code");
                            } else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name")).contains(name) && Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSections.get(0).get("credits"), credits)
                                    && code.equals("")){
                                match = true;
                                System.out.println("Matched on name and credits");
                            } else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name")).contains(name) && Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code)
                                    && credits.equals("")){
                                match = true;
                                System.out.println("Matched on name and code");
                            } else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code) && Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSections.get(0).get("credits"), credits)
                                    && name.equals("")){
                                match = true;
                                System.out.println("Matched on code and credits");
                            }  else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name")).contains(name) && Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSections.get(0).get("credits"), credits) &&
                                    Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code)){
                                match = true;
                                System.out.println("Matched on all fields");
                            }

                            if(match) {
                                if(!courses.contains(ds.getValue(Course.class).courseInfo.get("name"))) {
                                    courses.add(ds.getValue(Course.class).courseInfo.get("name"));
                                    System.out.println("Added course");
                                }
                            }
                        }
                    }

                ProgressBar filterLoad = findViewById(R.id.filterLoad);
                filterLoad.setVisibility(View.INVISIBLE);
                Button filterButton = findViewById(R.id.button3);
                filterButton.setVisibility(View.VISIBLE);

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("courses", courses);
                b.putStringArrayList("coursesPicked", coursesPicked);
                b.putString("course", courseName);
                b.putString("department", department);
                b.putString("semester", semester);
                intent.putExtras(b);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("courses", courses);
                System.out.println("coursesPicked from goToMain: " + coursesPicked.toString());
                b.putStringArrayList("coursesPicked", coursesPicked);
                b.putString("course", courseName);
                b.putString("department", department);
                b.putString("semester", semester);
                intent.putExtras(b);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }

    //When an item is selected in either list
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // The top spinner
        if (parent.getId() == R.id.spinner && !parent.getItemAtPosition(pos).toString().equals("Select a Semester")) {
            String valueFromSpinner = parent.getItemAtPosition(pos).toString();
            System.out.println("Spinner: " + valueFromSpinner);
            semester = valueFromSpinner;

            spinnerDept.setEnabled(false);
            spinnerCrse.setEnabled(false);

            //Add the database information to the list and update the spinner
            dbUpdater.getDepNames(deptNames, pSpinner, spinnerDept, spinnerCrse);
            deptNames.set(0, "Choose a Department");

            coursesNames.set(0, "Choose a Department");

            ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, deptNames);
            spinnerArrayAdapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinnerDept.setAdapter(spinnerArrayAdapter2);


            ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, coursesNames);
            spinnerArrayAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinnerCrse.setAdapter(spinnerArrayAdapter1);
        }

        // The middle spinner
        if (parent.getId() == R.id.spinnerDepartments && !parent.getItemAtPosition(pos).toString().equals("Please Select a Semester First")) {
            if(!parent.getItemAtPosition(pos).toString().equals("Choose a Department")) {
                System.out.println("Spinner: Department Chosen");
                coursesNames.clear();
                coursesNames.add("Choose a Course");
                courses.clear();
                department = parent.getItemAtPosition(pos).toString();
                try {
                    spinnerCrse.setEnabled(false);
                    dbUpdater.getCourseNames(parent.getItemAtPosition(pos).toString(), coursesNames, pSpinner2, courses, spinnerCrse);
                    ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, coursesNames);
                    spinnerArrayAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinnerCrse.setAdapter(spinnerArrayAdapter1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                department = "";
            }
        }

        // The bottom spinner
        if (parent.getId() == R.id.spinnerCourse && !parent.getItemAtPosition(pos).toString().equals("Please Select a Semester First") && !parent.getItemAtPosition(pos).toString().equals("Choose a Department") && !parent.getItemAtPosition(pos).toString().equals("Choose a Course")) {
            System.out.println("Spinner: Course Chosen");

            courses.clear();
            courseName = parent.getItemAtPosition(pos).toString();
            System.out.println("Set courseName: " + courseName);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }


    /** Called when the user taps the FILTER button */
    public void goToMain(View view){
        if(!courseName.equals("Please Select a Semester First") && !courseName.equals("Choose a Department") && !courseName.equals("Choose a Course") && !courseName.equals("")) {
            System.out.println("USING SPINNER TEXT");
            filterCourses("", "", courseName);
        } else {
            System.out.println("Filtering with code: " + courseCodeText + " credits: " + courseCreditsText + " name: " + courseNameText);
            filterCourses(courseCodeText.getText().toString(), courseCreditsText.getText().toString(), courseNameText.getText().toString());
        }
    }
}