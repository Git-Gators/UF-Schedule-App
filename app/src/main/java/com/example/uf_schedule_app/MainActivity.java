package com.example.uf_schedule_app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import android.content.Intent;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //Create a dbUpdater object
    DatabaseUpdater dbUpdater = new DatabaseUpdater();

    //Spinner Objects (Drop Down Lists)
    Spinner spinner;
    Spinner spinnerDept;
    Spinner spinnerCrse;

    //Used in the Spinner
    ArrayList<String> deptNames = new ArrayList<>();
    ArrayList<String> coursesNames = new ArrayList<>();
    ArrayList<Course> crsRet = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Update the database
        //dbUpdater.getDatabase(getBaseContext());

        //Delete old files
        //dbUpdater.deleteOldFiles(getBaseContext());

        //Get JSON files from Database if they're not there
        String[] files = getBaseContext().fileList();
        if(files.length < 3){
            System.out.println(Arrays.asList(files).toString());
            System.out.println("Getting Courses From Database");
            try {
                dbUpdater.getCourseJSON(getBaseContext());
            } catch (FileNotFoundException e) {
                System.out.println("Error getting courseJSON " + e);
            }
        } else {
            System.out.println("Courses Found");
            System.out.println(Arrays.asList(files).toString());
        }

        //Get a departments courses
        ArrayList<Course> deptCourses = new ArrayList<>();
        try {
            deptCourses = dbUpdater.getDepartmentCourses(getBaseContext(), "Agronomy");
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
        System.out.println(deptCourses);

        //Get a specific course
        Course testCourse = null;
        try {
            testCourse = dbUpdater.getCourse(getBaseContext(), "Financial Accounting and Reporting 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert testCourse != null;
        System.out.println("testCourse: " + testCourse.courseInfo.get("name"));

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

            //Add the database information to the list and update the spinner
            deptNames.addAll(dbUpdater.getDepNames(getBaseContext()));
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
            try {
                crsRet = dbUpdater.getDepartmentCourses(getBaseContext(), parent.getItemAtPosition(pos).toString());
                ArrayList<String> courseRet = new ArrayList<>();
                for(int i = 0; i < crsRet.size(); i++){
                    courseRet.add(crsRet.get(i).courseInfo.get("name"));
                }
                coursesNames.addAll(courseRet);

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

            //Use the getUFCourse function to get the course values
            Course course = new Course();
            for(int i = 0; i < crsRet.size(); i++){
                if(Objects.equals(crsRet.get(i).courseInfo.get("name"), parent.getItemAtPosition(pos).toString())){
                    course = crsRet.get(i);
                }
            }

            //Name of the course
            EditText editText = (EditText) findViewById(R.id.course1);
            String text = "Course Code: " + course.courseInfo.get("code");
            editText.setText(text);
            editText.setInputType(InputType.TYPE_NULL);

            //courseID
            editText = (EditText) findViewById(R.id.course2);
            text = "CourseID: " + course.courseInfo.get("courseId");
            editText.setText(text);
            editText.setInputType(InputType.TYPE_NULL);

            //Instructors
            editText = (EditText) findViewById(R.id.course3);
            text = "Instructors: " + course.classSections.get(0).get("Instructors").replace("[", "").replace("]", "");
            editText.setText(text);
            editText.setInputType(InputType.TYPE_NULL);

            //Class Number
            editText = (EditText) findViewById(R.id.course4);
            text = "Class Number: " + course.classSections.get(0).get("number");
            editText.setText(text);
            editText.setInputType(InputType.TYPE_NULL);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }

    /** Called when the user taps the My Schedule button */
    public void goToSchedule(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, ViewSchedule.class);
        startActivity(intent);
    }
}
