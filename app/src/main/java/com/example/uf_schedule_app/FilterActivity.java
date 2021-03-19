package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class FilterActivity extends MainActivity implements AdapterView.OnItemSelectedListener {

    String department = "";
    Course courseName = null;
    String semester = "Spring 2021";
    ArrayList<Course> coursesInSchedule = new ArrayList<>();

    // Courses retrieved from the DB
    ArrayList<Course> crses = new ArrayList<>();

    //Courses the user has already chosen
    ArrayList<Course> coursesPicked = new ArrayList<>();


    //Spinner Objects (Drop Down Lists)
    Spinner spinner;
    Spinner spinnerDept;
    Spinner spinnerCrse;

    //Progress Bars (Spinning load icon)
    private ProgressBar pSpinner2;

    //Used in the Spinner
    ArrayList<String> deptNames = new ArrayList<>();

    //Used in the filters
    EditText courseCodeText;
    EditText courseCreditsText;
    EditText courseNameText;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_view);

        //Display the top section
        getSupportActionBar().setTitle("Filters");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Filter Button & Loading inside the linear layout
        ProgressBar filterLoad = findViewById(R.id.filterLoad);
        filterLoad.setVisibility(View.INVISIBLE);
        Button filterButton = findViewById(R.id.button3);
        filterButton.setVisibility(View.VISIBLE);

        //Filter Button & Loading outside the linear layout
        ProgressBar filterLoadOut = findViewById(R.id.outerFilterLoad);
        filterLoadOut.setVisibility(View.INVISIBLE);
        Button filterButtonOut = findViewById(R.id.outerFilter);
        filterButtonOut.setVisibility(View.INVISIBLE);


        //Grab info from other activities.
        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        if(b != null){
            //coursesPicked
            if(b.getSerializable("coursesPicked") != null) {
                coursesInSchedule = (ArrayList<Course>) intent.getSerializableExtra("coursesPicked");
            }
            if(b.getSerializable("courseList") != null) {
                coursesInSchedule = (ArrayList<Course>) intent.getSerializableExtra("courseList");
            }
        }

        //Set the progress bars for spinners to be invisible
        pSpinner2 = findViewById(R.id.progressBar2);
        pSpinner2.setVisibility(View.INVISIBLE);

        //Update the lists
        spinnerDept = (Spinner) findViewById(R.id.spinnerDepartments);
        spinnerCrse = (Spinner) findViewById(R.id.spinnerCourse);

        spinnerDept.setOnItemSelectedListener(this);
        spinnerCrse.setOnItemSelectedListener(this);

        //Add the database information to the list and update the spinner
        try {
            dbUpdater.getDepNames(deptNames, spinnerDept, spinnerCrse, getBaseContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        deptNames.set(0, " -- ");
        String[] courses = {" -- "};
        spinnerCrse.setEnabled(false);

        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, deptNames);
        final ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, courses);

        // Specify the layout to use when the list of choices appears
        spinnerArrayAdapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerArrayAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerDept.setAdapter(spinnerArrayAdapter2);
        spinnerCrse.setAdapter(spinnerArrayAdapter1);

        //Text Listeners
        courseCodeText = (EditText) findViewById(R.id.courseCode);
        courseCreditsText = (EditText) findViewById(R.id.courseCredits);
        courseNameText = (EditText) findViewById(R.id.courseTitle);


        Spinner minSpinner = findViewById(R.id.levelMinSpinner);
        ArrayAdapter<CharSequence> minSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.level_minimum, android.R.layout.simple_spinner_item);
        minSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minSpinner.setAdapter(minSpinnerAdapter);

        Spinner maxSpinner = findViewById(R.id.levelMaxSpinner);
        ArrayAdapter<CharSequence> maxSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.level_maximum, android.R.layout.simple_spinner_item);
        maxSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxSpinner.setAdapter(maxSpinnerAdapter);

        Spinner periodStart = findViewById(R.id.periodStartSpinner);
        ArrayAdapter<CharSequence> periodStartAdapter = ArrayAdapter.createFromResource(this, R.array.periods_filter, android.R.layout.simple_spinner_item);
        maxSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodStart.setAdapter(periodStartAdapter);

        Spinner periodEnd = findViewById(R.id.periodEndSpinner);
        ArrayAdapter<CharSequence> periodEndAdapter = ArrayAdapter.createFromResource(this, R.array.periods_filter, android.R.layout.simple_spinner_item);
        periodEndAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodEnd.setAdapter(periodEndAdapter);
    }

    public void filterCourses(String code, String credits, String name){
        System.out.println(code + " " + credits + " "  + name);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean match;
                System.out.println(department);
                crses.clear();

                if(courseName != null){
                    crses.add(courseName);
                } else {
                    boolean depMatched = false;
                    for (DataSnapshot dep : dataSnapshot.getChildren()) {
                        //if the department isn't right
                        if(!dep.getKey().equals(department) && !department.equals("") && !department.equals("Choose a Department")) {
                            System.out.println("Skipping on the basis of department not being equal");
                            continue;
                        } else if(dep.getKey().equals(department) && !department.equals("") && !department.equals("Choose a Department")) {
                            depMatched = true;
                        }

                        for (DataSnapshot ds : dep.getChildren()) {
                            match = false;

                            //Based on the fields entered we match on things.
                            if (ds.getValue(Course.class).courseInfo.get("name").contains(name) && credits.equals("") && code.equals("")) {
                                match = true;
                                System.out.println("MATCH: " + name + " " + ds.getValue(Course.class).courseInfo.get("name") + ";");
                                System.out.println("Matched on only name");
                            } else if (Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSection.get("credits"), credits) && name.equals("") && code.equals("")) {
                                match = true;
                                System.out.println("Matched on only credits");
                            } else if (Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code) && name.equals("") && credits.equals("")) {
                                match = true;
                                System.out.println("Matched on only code");
                            } else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name")).contains(name) && Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSection.get("credits"), credits)
                                    && code.equals("")){
                                match = true;
                                System.out.println("Matched on name and credits");
                            } else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name")).contains(name) && Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code)
                                    && credits.equals("")){
                                match = true;
                                System.out.println("Matched on name and code");
                            } else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code) && Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSection.get("credits"), credits)
                                    && name.equals("")){
                                match = true;
                                System.out.println("Matched on code and credits");
                            }  else if(Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name")).contains(name) && Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).classSection.get("credits"), credits) &&
                                    Objects.requireNonNull(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("code")).contains(code)){
                                match = true;
                                System.out.println("Matched on all fields");
                            }

                            if(match) {
                                crses.add(ds.getValue(Course.class));
                            }
                        }

                        //If we've found all the departments courses we're done.
                        if(depMatched) {
                            System.out.println("Stopping search because we've found all the dep courses.");
                            break;
                        }
                    }
                }

                startMain();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        mDatabase.addValueEventListener(postListener);
    }

    /** When The Back Button In The Top Right Is Pressed **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //If the course was chosen in the spinner
                if(courseName != null) {
                    System.out.println("USING SPINNER TEXT");
                    filterCourses("", "", "");
                } else {
                    //The course spinner wasn't chosen
                    System.out.println("Filtering with code: " + courseCodeText + " credits: " + courseCreditsText + " name: " + courseNameText);
                    filterCourses(courseCodeText.getText().toString(), courseCreditsText.getText().toString(), courseNameText.getText().toString());
                }
                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }

    //When an item is selected in any list
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // The middle spinner
        if (parent.getId() == R.id.spinnerDepartments && !parent.getItemAtPosition(pos).toString().equals("Please Select a Semester First")) {
            if(!parent.getItemAtPosition(pos).toString().equals("Choose a Department")) {
                System.out.println("Spinner: Department Chosen");
                ArrayList<String> coursesNames = new ArrayList<>();
                coursesNames.add(" -- ");
                crses.clear();
                department = parent.getItemAtPosition(pos).toString();
                try {
                    spinnerCrse.setEnabled(false);
                    dbUpdater.getCourseNames(parent.getItemAtPosition(pos).toString(), coursesNames, pSpinner2, spinnerCrse, crses);
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
        if (parent.getId() == R.id.spinnerCourse) {
            System.out.println("Spinner: Course Chosen");

            if(parent.getItemAtPosition(pos).toString().equals(" -- ")) {
                courseName = null;
                System.out.println("Set courseName: null");
            } else {
                courseName = crses.get(pos);
                crses.clear();
                System.out.println("Set courseName: " + courseName.toString());
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }


    /** Called when the user taps the FILTER button */
    public void goToMain(View view){
        //If the course was chosen in the spinner
        if(view.getId() == R.id.outerFilter){
            //Hide the filter button and show the load
            Button filterButton = findViewById(R.id.outerFilter);
            filterButton.setVisibility(View.INVISIBLE);
            ProgressBar load = findViewById(R.id.outerFilterLoad);
            load.setVisibility(View.VISIBLE);
        } else {
            //Hide the filter button and show the load
            Button filterButton = findViewById(R.id.button3);
            filterButton.setVisibility(View.INVISIBLE);
            ProgressBar load = findViewById(R.id.filterLoad);
            load.setVisibility(View.VISIBLE);
        }




        if(courseName != null) {
            System.out.println("USING SPINNER TEXT");
            filterCourses("", "", "");
        } else {
            //The course spinner wasn't chosen
            System.out.println("Filtering with code: " + courseCodeText + " credits: " + courseCreditsText + " name: " + courseNameText);
            filterCourses(courseCodeText.getText().toString(), courseCreditsText.getText().toString(), courseNameText.getText().toString());
        }
    }

    private void startMain(){
        Intent intent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        intent.putExtra("crses", crses);
        intent.putExtra("coursesPicked", coursesPicked);
        intent.putExtras(b);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void nextFilter(View view){
        ConstraintLayout top = findViewById(R.id.top);
        ConstraintLayout middle = findViewById(R.id.middle);
        ConstraintLayout bottom = findViewById(R.id.bottom);

        if(view.getId() == R.id.topBar){
            if(top.getVisibility() == View.VISIBLE){
                top.setVisibility(View.GONE);
                TextView bar = findViewById(R.id.topBar);
                bar.setText(R.string.filterBarClosed);
            } else {
                top.setVisibility(View.VISIBLE);
                TextView bar = findViewById(R.id.topBar);
                bar.setText(R.string.filterBarOpen);
            }
        } else if(view.getId() == R.id.middleBar){
            if(middle.getVisibility() == View.VISIBLE){
                middle.setVisibility(View.GONE);
                TextView bar = findViewById(R.id.middleBar);
                bar.setText(R.string.courseFilterClosed);
            } else {
                middle.setVisibility(View.VISIBLE);
                TextView bar = findViewById(R.id.middleBar);
                bar.setText(R.string.courseFilterOpen);
            }
        } else if(view.getId() == R.id.bottomBar){
            if(bottom.getVisibility() == View.VISIBLE){
                bottom.setVisibility(View.GONE);
                TextView bar = findViewById(R.id.bottomBar);
                bar.setText(R.string.meetingClosed);
            } else {
                bottom.setVisibility(View.VISIBLE);
                TextView bar = findViewById(R.id.bottomBar);
                bar.setText(R.string.meetingOpen);
            }
        }

        Button filterButton = findViewById(R.id.button3);
        Button filterButtonOut = findViewById(R.id.outerFilter);
        if(top.getVisibility() == View.GONE && bottom.getVisibility() == View.GONE){
            filterButton.setVisibility(View.GONE);
            filterButtonOut.setVisibility(View.VISIBLE);
        } else {
            filterButton.setVisibility(View.VISIBLE);
            filterButtonOut.setVisibility(View.GONE);
        }
    }


    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.mondayCheckBox:
                if (checked){

                }
                break;
        }
    }

}