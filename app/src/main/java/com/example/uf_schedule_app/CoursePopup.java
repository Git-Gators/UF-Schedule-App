package com.example.uf_schedule_app;

import androidx.annotation.RequiresApi;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import android.content.Intent;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class CoursePopup extends MainActivity {
    ArrayList<String> coursesPicked = new ArrayList<>();
    ArrayList<String> departmentPicked = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();

    ProgressBar load;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_course);


        //If we're coming from the main, we grab the info
        Bundle b = getIntent().getExtras();
        coursesPicked = b.getStringArrayList("coursesPicked");
        departmentPicked = b.getStringArrayList("departmentPicked");
        /*if(b != null){
            if(b.getStringArrayList("coursesPicked") != null && b.getStringArrayList("departmentPicked") != null){
                coursesPicked = b.getStringArrayList("coursesPicked");
                departmentPicked = b.getStringArrayList("departmentPicked");

                System.out.println(coursesPicked.toString());
                System.out.println(departmentPicked.toString());

                //Gets the courses added after a short delay.
                for(int i = 0; i < coursesPicked.size(); i++)
                    getCourse(coursesPicked.get(i), departmentPicked.get(i));
                if (coursesPicked.size() == 0) {

                    TextView text = null;
                    text = findViewById(R.id.courseText1);
                    text.setText("No courses selected.");
                }
            }
            else {
                load.setVisibility(View.VISIBLE);
                TextView text = null;
                text = findViewById(R.id.courseText1);
                text.setText("No courses selected.");
            }
        }
        else {
            load.setVisibility(View.VISIBLE);
            TextView text = null;
            text = findViewById(R.id.courseText1);
            text.setText("No courses selected.");
        }*/
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }

    /** Called when the user taps the Send button */
    public void goToSchedule(View view) {
        Intent intent = new Intent(this, ViewSchedule.class);
        Bundle b = new Bundle();
        b.putStringArrayList("coursesPicked", coursesPicked);
        b.putStringArrayList("departmentPicked", departmentPicked);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }

    public void getCourse(String course, String department){
        //Get the course objects from that
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(department);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.getValue(Course.class).courseInfo.get("name").equals(course)){
                        courses.add(ds.getValue(Course.class));
                    }
                }
                System.out.println(courses.toString());

                //We have all the courses
                if(courses.size() == coursesPicked.size()){
                    load.setVisibility(View.INVISIBLE);

                    //Edit all the courseTexts
                    for(int i = 0; i < courses.size(); i++){
                        TextView text = null;
                        if(i == 0){
                            text = findViewById(R.id.courseText1);
                        } else if(i == 1){
                            text = findViewById(R.id.courseText2);
                        } else if(i == 2){
                            text = findViewById(R.id.courseText3);
                        } else if(i == 3){
                            text = findViewById(R.id.courseText4);
                        }
                        if (text == null && i == 0) {
                            text = findViewById(R.id.courseText1);
                            text.setText("No courses selected.");
                        }
                        else if(text != null)
                            text.setText(courses.get(i).courseInfo.get("name"));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        mDatabase.addValueEventListener(postListener);
    }


}