package com.example.uf_schedule_app;

import androidx.annotation.RequiresApi;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;

public class FilterActivity extends MainActivity implements AdapterView.OnItemSelectedListener {

    String department;
    String courseName;
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
    ArrayList<String> departmentPicked = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_view);

        getSupportActionBar().setTitle("Filters");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Swipe Right to go back to Main
        Slidr.attach(this);

        //If we're coming from the filter, we grab the info
        Bundle b = getIntent().getExtras();
        if(b != null){
            if(b.getStringArrayList("coursesPicked") != null){
                coursesPicked = b.getStringArrayList("coursesPicked");
            }
            if(b.getStringArrayList("departmentPicked") != null){
                departmentPicked = b.getStringArrayList("departmentPicked");
            }
        }

        pSpinner = findViewById(R.id.progressBar);
        pSpinner.setVisibility(View.INVISIBLE);
        pSpinner2 = findViewById(R.id.progressBar2);
        pSpinner2.setVisibility(View.INVISIBLE);
        pSpinner3 = findViewById(R.id.progressBar3);
        pSpinner3.setVisibility(View.INVISIBLE);

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
    }

    //When an item is selected in either list
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // The top spinner
        if (parent.getId() == R.id.spinner && !parent.getItemAtPosition(pos).toString().equals("Select a Semester")) {
            String valueFromSpinner = parent.getItemAtPosition(pos).toString();
            System.out.println("Spinner: " + valueFromSpinner);
            semester = valueFromSpinner;

            //Add the database information to the list and update the spinner
            dbUpdater.getDepNames(deptNames, pSpinner);
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
        if (parent.getId() == R.id.spinnerDepartments && !parent.getItemAtPosition(pos).toString().equals("Please Select a Semester First") && !parent.getItemAtPosition(pos).toString().equals("Choose a Department")) {
            System.out.println("Spinner: Department Chosen");
            coursesNames.clear();
            coursesNames.add("Choose a Course");
            department = parent.getItemAtPosition(pos).toString();
            try {
                dbUpdater.getCourseNames(parent.getItemAtPosition(pos).toString(), coursesNames, pSpinner2);
                ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, coursesNames);
                spinnerArrayAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerCrse.setAdapter(spinnerArrayAdapter1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // The bottom spinner
        if (parent.getId() == R.id.spinnerCourse && !parent.getItemAtPosition(pos).toString().equals("Please Select a Semester First") && !parent.getItemAtPosition(pos).toString().equals("Choose a Department") && !parent.getItemAtPosition(pos).toString().equals("Choose a Course")) {
            System.out.println("Spinner: Course Chosen");

            courseName = parent.getItemAtPosition(pos).toString();

            //Name of the course
            EditText course1 = (EditText) findViewById(R.id.courseText1);

            //courseID
            EditText course2 = (EditText) findViewById(R.id.course2);

            //Instructors
            EditText course3 = (EditText) findViewById(R.id.course3);

            //Class Number
            EditText course4 = (EditText) findViewById(R.id.course4);

            Spinner depSpin = (Spinner) findViewById(R.id.spinnerDepartments);
            String deptName = depSpin.getSelectedItem().toString();
            String courseName = parent.getItemAtPosition(pos).toString();
            dbUpdater.setTextFields(course1, course2, course3, course4, deptName, courseName, pSpinner3);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }

    /** Called when the user taps the GO BACK button */
    public void goToMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        b.putStringArrayList("courses", courses);
        System.out.println("coursesPicked from goToMain: " + coursesPicked.toString());
        b.putStringArrayList("coursesPicked", coursesPicked);
        b.putStringArrayList("departmentPicked", departmentPicked);
        b.putString("course", courseName);
        b.putString("department", department);
        b.putString("semester", semester);
        intent.putExtras(b);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}