package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

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


public class ViewSchedule extends MainActivity {
    ArrayList<String> coursesPicked = new ArrayList<>();
    ArrayList<String> departmentPicked = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();

    ProgressBar load;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView courseInfopopup_Name, courseInfopopup_nameBox, courseInfopopup_Course_Description, courseInfopopup_courseDescriptionBox, courseInfopopup_courseCode_box, courseInfopopup_CourseCode, courseInfopopup_courseID, courseInfopopup_Course_ID, courseInfopopup_Title;
    private Button courseInfopopup_Back2Sched;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_view);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_schedule);

        load = findViewById(R.id.progressBarSchedule);
        load.setVisibility(View.INVISIBLE);



        //If we're coming from the main, we grab the info
        Bundle b = getIntent().getExtras();
        if(b != null){
            if(b.getStringArrayList("coursesPicked") != null && b.getStringArrayList("departmentPicked") != null){
                coursesPicked = b.getStringArrayList("coursesPicked");
                departmentPicked = b.getStringArrayList("departmentPicked");

                System.out.println(coursesPicked.toString());
                System.out.println(departmentPicked.toString());

                //Gets the courses added after a short delay.
                load.setVisibility(View.VISIBLE);
                for(int i = 0; i < coursesPicked.size(); i++)
                    getCourse(coursesPicked.get(i), departmentPicked.get(i));
                if (coursesPicked.size() == 0) {
                    load.setVisibility(View.INVISIBLE);
                    TextView text = null;
                    text = findViewById(R.id.courseText5);
                    text.setText("No courses selected.");
                }
            }
            else {
                load.setVisibility(View.VISIBLE);
                TextView text = null;
                text = findViewById(R.id.courseText5);
                text.setText("No courses selected.");
            }
        }
        else {
            load.setVisibility(View.VISIBLE);
            TextView text = null;
            text = findViewById(R.id.courseText5);
            text.setText("No courses selected.");
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }

    /** Called when the user taps the Send button */
    public void goToCourseFinder(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        b.putStringArrayList("coursesPicked", coursesPicked);
        intent.putExtras(b);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
    //Delete all courses
    public void goToDelete(View view) {
        coursesPicked.clear();

        Intent intent = new Intent(this, ViewSchedule.class);
        Bundle b = new Bundle();
        b.putStringArrayList("coursesPicked", coursesPicked);
        b.putStringArrayList("departmentPicked", departmentPicked);
        intent.putExtras(b);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    //Delete button next to course
    public void deleteCourse(View view) {
        if(view.getTag() == null)
            return;

        //Call tag of delete button and use that as index
        String index = view.getTag().toString();
        if (coursesPicked.size() > Integer.parseInt(index)) {

            coursesPicked.remove(Integer.parseInt(index));
            departmentPicked.remove(Integer.parseInt(index));
        }

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
                        Button button = null;

                        if(i == 0){
                            text = findViewById(R.id.courseText1);
                            button = findViewById(R.id.delete1);
                        } else if(i == 1){
                            text = findViewById(R.id.courseText2);
                            button = findViewById(R.id.delete2);
                        } else if(i == 2){
                            text = findViewById(R.id.courseText3);
                            button = findViewById(R.id.delete6);
                        } else if(i == 3){
                            text = findViewById(R.id.courseText4);
                            button = findViewById(R.id.delete7);
                        }
                        if (text == null && i == 0) {
                            text = findViewById(R.id.courseText5);
                            text.setText("No courses selected.");
                        }
                        else if(text != null) {
                            text.setText(courses.get(i).courseInfo.get("name"));
                        }
                        Button deleteAll = findViewById(R.id.delete);
                        deleteAll.setVisibility(View.VISIBLE);
                        button.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        mDatabase.addValueEventListener(postListener);
    }
    //Bruh
    public void createPopup(View view) {
        //System.out.println("item click success");
        dialogBuilder = new AlertDialog.Builder(this);
        final View CourseInfoPopupView = getLayoutInflater().inflate(R.layout.popup_course, null);
        courseInfopopup_Name = (TextView) CourseInfoPopupView.findViewById(R.id.Name);
        courseInfopopup_nameBox = (TextView) CourseInfoPopupView.findViewById(R.id.nameBox);
        courseInfopopup_Course_Description = (TextView) CourseInfoPopupView.findViewById(R.id.Course_Description);
        courseInfopopup_courseDescriptionBox = (TextView) CourseInfoPopupView.findViewById(R.id.courseDescription_box);
        courseInfopopup_courseCode_box = (TextView) CourseInfoPopupView.findViewById(R.id.courseCode_box);
        courseInfopopup_CourseCode = (TextView) CourseInfoPopupView.findViewById(R.id.Course_Code);
        courseInfopopup_courseID = (TextView) CourseInfoPopupView.findViewById(R.id.courseID);
        courseInfopopup_Course_ID = (TextView) CourseInfoPopupView.findViewById(R.id.Course_ID);
        courseInfopopup_Title = (TextView) CourseInfoPopupView.findViewById(R.id.Title);

        courseInfopopup_Back2Sched = (Button) CourseInfoPopupView.findViewById(R.id.Back2Sched);

        dialogBuilder.setView(CourseInfoPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        courseInfopopup_Back2Sched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = 0;
                    Intent in;
                    switch(item.getItemId()){
                        case R.id.nav_home:
                            in = new Intent(getBaseContext(), MainActivity.class);
                            Bundle b = new Bundle();
                            b.putStringArrayList("coursesPicked", coursesPicked);
                            b.putStringArrayList("departmentPicked", departmentPicked);
                            in.putExtras(b);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
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
