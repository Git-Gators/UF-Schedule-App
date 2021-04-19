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
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterActivity extends MainActivity implements AdapterView.OnItemSelectedListener {

    String department = "";
    Course courseName = null;
    String semester;
    ArrayList<Course> coursesInSchedule = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_view);

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
            if(b.getSerializable("semester") != null) {
                semester = (String) intent.getSerializableExtra("semester");
            }
        }

        setupInterface();
    }

    public void filterCourses(){
        //Grab the current semester
        String originalSemester = semester;
        checkSemesterRegistry();

        //Text Listeners
        EditText courseCodeText = (EditText) findViewById(R.id.courseCode);
        EditText courseCreditsText = (EditText) findViewById(R.id.courseCredits);
        EditText courseNameText = (EditText) findViewById(R.id.courseTitle);
        EditText instructorText = (EditText) findViewById(R.id.instructorInput);

        String code = courseCodeText.getText().toString();
        String credits = courseCreditsText.getText().toString();
        String name = courseNameText.getText().toString();
        String instructor = instructorText.getText().toString();
        String levelMin = ((Spinner)findViewById(R.id.levelMinSpinner)).getSelectedItem().toString();
        String levelMax = ((Spinner)findViewById(R.id.levelMaxSpinner)).getSelectedItem().toString();
        String periodStart = ((Spinner)findViewById(R.id.periodStartSpinner)).getSelectedItem().toString();
        String periodEnd = ((Spinner)findViewById(R.id.periodEndSpinner)).getSelectedItem().toString();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(semester);
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                crses.clear();

                if(courseName != null){
                    crses.add(courseName);
                } else {
                    boolean depMatched = false;
                    for (DataSnapshot dep : dataSnapshot.getChildren()) {
                        //if the department isn't right
                        if(!dep.getKey().equals(department) && !department.equals(" -- ")) {
                            continue;
                        } else if(dep.getKey().equals(department) && !department.equals(" -- ")) {
                            depMatched = true;
                        }

                        for (DataSnapshot ds : dep.getChildren()) {
                            Course crsToBeAdded = ds.getValue(Course.class);

                            //Based on the fields entered we match on things.
                            if (!name.isEmpty()) {
                                //There's a name
                                if(!crsToBeAdded.courseInfo.get("name").contains(name)) {
                                    //The name doesn't match the course name
                                    continue;
                                }
                            }
                            if (!code.isEmpty()) {
                                //There's a code

                                //We've not matched on code.
                                if(!crsToBeAdded.courseInfo.get("code").contains(code)) {
                                    continue;
                                }
                            }
                            if (!credits.isEmpty()) {
                                //There's a code and we haven't matched yet.

                                //We've not matched on code.
                                if(!crsToBeAdded.classSection.get("credits").contains(credits)) {
                                    continue;
                                }
                            }
                            if (!instructor.isEmpty()) {
                                //There's a instructor and we haven't matched yet.

                                //We've not matched on instructor.
                                if(!crsToBeAdded.classSection.get("Instructors").contains(instructor)) {
                                    continue;
                                }
                            }

                            String level = ds.getValue(Course.class).courseInfo.get("code");
                            level = level.substring(3, 7);
                            int levelInt = Integer.parseInt(level);
                            if (!levelMin.equals("--")) {
                                int levelMinInt = Integer.parseInt(levelMin);
                                if(levelInt < levelMinInt)
                                    continue;
                            }
                            if (!levelMax.equals("--")) {
                                int levelMaxInt = Integer.parseInt(levelMax);
                                if(levelMaxInt < levelInt)
                                    continue;
                            }

                            String meetTime = crsToBeAdded.classSection.get("meetPeriod");
                            Matcher regexPeriods = Pattern.compile("[\\[]([^\\]]+)[\\]]").matcher(meetTime);
                            ArrayList<String> meetTimes = new ArrayList<>();
                            while(regexPeriods.find()) {
                                meetTimes.add(regexPeriods.group(1));
                            }

                            for(int i = 0; i < meetTimes.size(); i++){
                                meetTimes.set(i, meetTimes.get(i).replace("E1", "12").replace("E2", "13").replace("E3", "14"));
                            }

                            if(!periodStart.equals("--")){
                                String fixedPeriodStart = periodStart.replace("E1", "12").replace("E2", "13").replace("E3", "14");

                                boolean cont = false;
                                for(int i = 0; i < meetTimes.size(); i++){
                                    if(Integer.parseInt(String.valueOf(meetTimes.get(i).charAt(0))) <  Integer.parseInt(fixedPeriodStart))
                                        cont = true;
                                }
                                if(cont)
                                    continue;
                            }
                            if(!periodEnd.equals("--")){
                                String fixedPeriodEnd = periodEnd.replace("E1", "12").replace("E2", "13").replace("E3", "14");

                                boolean cont = false;
                                for(int i = 0; i < meetTimes.size(); i++){
                                    if(Integer.parseInt(String.valueOf(meetTimes.get(i).charAt(meetTimes.get(i).length()-1))) >  Integer.parseInt(fixedPeriodEnd))
                                        cont = true;
                                }
                                if(cont)
                                    continue;
                            }

                            if(!days.isEmpty()){
                                //If the days checkboxes are checked
                                //One+ days checkboxes are clicked
                                String meetDays = crsToBeAdded.classSection.get("meetDays");
                                if(days.contains("Monday") && !meetDays.contains("M") || meetDays.contains("M") && !days.contains("Monday")) {
                                    continue;
                                }
                                if(days.contains("Tuesday") && !meetDays.contains("T") || meetDays.contains("T") && !days.contains("Tuesday")) {
                                    continue;
                                }
                                if(days.contains("Wednesday") && !meetDays.contains("W") || meetDays.contains("W") && !days.contains("Wednesday")) {
                                    continue;
                                }
                                if(days.contains("Thursday") && !meetDays.contains("R") || meetDays.contains("R") && !days.contains("Thursday")) {
                                    continue;
                                }
                                if(days.contains("Friday") && !meetDays.contains("F") || meetDays.contains("F") && !days.contains("Friday")) {
                                    continue;
                                }
                                if(days.contains("Saturday") && !meetDays.contains("S") || meetDays.contains("S") && !days.contains("Saturday")) {
                                    continue;
                                }
                            }

                            crses.add(crsToBeAdded);
                        }

                        //If we've found the department, we don't need to check for more courses.
                        if(depMatched || crses.size() >= 400) {
                            break;
                        }
                    }
                }

                semester = originalSemester;
                startMain();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        mDatabase.addValueEventListener(postListener);
    }

    //If the semester has no data in our database
    //If the semester is farther ahead than the most recent database
    private void checkSemesterRegistry(){
        //TODO This is hardcoded to the newest semester
        int currentIndex = Arrays.asList(semesterNames).indexOf(semester);
        if(currentIndex > Arrays.asList(semesterNames).indexOf("Fall 2021"))
            semester = "Fall 2021";

        
    }

    /** When The Back Button In The Top Right Is Pressed **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startMain();
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
                ArrayList<String> coursesNames = new ArrayList<>();
                coursesNames.add(" -- ");
                crses.clear();
                department = parent.getItemAtPosition(pos).toString();
                try {
                    spinnerCrse.setEnabled(false);
                    //System.out.println("Semester: " + semester);
                    dbUpdater.getCourseNames(parent.getItemAtPosition(pos).toString(), coursesNames, pSpinner2, spinnerCrse, crses, semester);
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

            if(parent.getItemAtPosition(pos).toString().equals(" -- ")) {
                courseName = null;
            } else {
                courseName = crses.get(pos);
                crses.clear();
            }
        }
    }

    //This might not be needed.
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        //System.out.println("Spinner: onNothingSelected");
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
            filterCourses();
        } else {
            //The course spinner wasn't chosen
            filterCourses();
        }
    }

    //Function that triggers the transition back to the main activity.
    private void startMain(){
        Intent intent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        intent.putExtra("crses", crses);
        intent.putExtra("coursesPicked", coursesPicked);
        intent.putExtra("semester", semester);
        intent.putExtras(b);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    //This is for the manipulation of the filter menus.
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

    //This is for the Days checkboxes.
    //When they're clicked this is activated.
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.mondayCheckBox:
                if(checked)
                    days.add("Monday");
                else
                    days.remove("Monday");
                break;
            case R.id.tuesdayCheckBox:
                if(checked)
                    days.add("Tuesday");
                else
                    days.remove("Tuesday");
                break;
            case R.id.wednesdayCheckBox:
                if(checked)
                    days.add("Wednesday");
                else
                    days.remove("Wednesday");
                break;
            case R.id.thursdayCheckbox:
                if(checked)
                    days.add("Thursday");
                else
                    days.remove("Thursday");
                break;
            case R.id.fridayCheckbox:
                if(checked)
                    days.add("Friday");
                else
                    days.remove("Friday");
                break;
            case R.id.saturdayCheckbox:
                if(checked)
                    days.add("Saturday");
                else
                    days.remove("Saturday");
                break;
        }
    }

    //Just using this as a way to clean up onCreate().
    //Sets up spinners and other user interface elements.
    private void setupInterface(){
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

        //Level Minimum
        Spinner minSpinner = findViewById(R.id.levelMinSpinner);
        ArrayAdapter<CharSequence> minSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.level_minimum, android.R.layout.simple_spinner_item);
        minSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minSpinner.setAdapter(minSpinnerAdapter);

        //Level Maximum
        Spinner maxSpinner = findViewById(R.id.levelMaxSpinner);
        ArrayAdapter<CharSequence> maxSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.level_maximum, android.R.layout.simple_spinner_item);
        maxSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxSpinner.setAdapter(maxSpinnerAdapter);

        //Period Start
        Spinner periodStart = findViewById(R.id.periodStartSpinner);
        Spinner periodEnd = findViewById(R.id.periodEndSpinner);

        ArrayAdapter<CharSequence> periodStartAdapter = ArrayAdapter.createFromResource(this, R.array.periods_filter, android.R.layout.simple_spinner_item);
        maxSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodStart.setAdapter(periodStartAdapter);
        periodEnd.setAdapter(periodStartAdapter);
    }
}