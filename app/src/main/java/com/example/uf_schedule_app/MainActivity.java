package com.example.uf_schedule_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //Create a dbUpdater object
    DatabaseUpdater dbUpdater = new DatabaseUpdater();

    //Spinner Objects (Drop Down Lists)
    Spinner spinner;
    Spinner spinnerCourses;

    //Used in the Spinner
    ArrayList<String> courseNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Update the database and grab the courses for later
        dbUpdater.getUFCourses("Spring 2021");

        //Update the lists
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerCourses = (Spinner) findViewById(R.id.spinnerCourses);
        spinner.setOnItemSelectedListener(this);
        spinnerCourses.setOnItemSelectedListener(this);
        String[] semesters = new String[]{"Select a Course", "Spring 2021"};
        courseNames.add("Please Select a Semester First");

        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, semesters);
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, courseNames);

        // Specify the layout to use when the list of choices appears
        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerArrayAdapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerArrayAdapter);
        spinnerCourses.setAdapter(spinnerArrayAdapter2);
    }

    //When an item is selected in either list
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // The top spinner
        if (parent.getId() == R.id.spinner && !parent.getItemAtPosition(pos).toString().equals("Select a Course")) {
            String valueFromSpinner = parent.getItemAtPosition(pos).toString();
            System.out.println("Spinner: " + valueFromSpinner);

            //Add the database information to the list and update the spinner
            courseNames.addAll(dbUpdater.courseNames);
            courseNames.set(0, "Choose a Course");
            ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, courseNames);
            spinnerArrayAdapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinnerCourses.setAdapter(spinnerArrayAdapter2);
        }

        // The bottom spinner
        if (parent.getId() == R.id.spinnerCourses && !parent.getItemAtPosition(pos).toString().equals("Please Select a Semester First") && !parent.getItemAtPosition(pos).toString().equals("Choose a Course")) {
            System.out.println("Spinner: Course Chosen");
            boolean courseChosen = false;

            //Change the text one by one
            EditText editText = (EditText) findViewById(R.id.course1);
            if(editText.getText().toString().equals("Course 1")) {
                editText.setText(parent.getItemAtPosition(pos).toString());
                courseChosen = true;
            }

            editText = (EditText) findViewById(R.id.course2);
            if(editText.getText().toString().equals("Course 2") && !courseChosen) {
                editText.setText(parent.getItemAtPosition(pos).toString());
                courseChosen = true;
            }

            editText = (EditText) findViewById(R.id.course3);
            if(editText.getText().toString().equals("Course 3") && !courseChosen) {
                editText.setText(parent.getItemAtPosition(pos).toString());
                courseChosen = true;
            }

            editText = (EditText) findViewById(R.id.course4);
            if(editText.getText().toString().equals("Course 4") && !courseChosen) {
                editText.setText(parent.getItemAtPosition(pos).toString());
                courseChosen = true;
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }

}
