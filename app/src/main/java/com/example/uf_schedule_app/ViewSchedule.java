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
import java.util.Objects;


public class ViewSchedule extends MainActivity {
    ArrayList<String> coursesPicked = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();

    ProgressBar load;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView courseInfopopup_Name, courseInfopopup_nameBox;
    private TextView courseInfopopup_Course_Description, courseInfopopup_courseDescriptionBox;
    private TextView courseInfopopup_courseCode_box, courseInfopopup_CourseCode;
    private TextView courseInfopopup_courseID, courseInfopopup_Course_ID;
    private TextView courseInfopopup_Title;
    private TextView courseInfopopup_Instructor, courseInfopopup_Instructor_box;
    private TextView courseInfopopup_section_number, courseInfopopup_section_number_box;
    private TextView courseInfopopup_num_credits, courseInfopopup_num_credits_box;
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
            if(b.getStringArrayList("coursesPicked") != null){
                coursesPicked = b.getStringArrayList("coursesPicked");

                System.out.println(coursesPicked.toString());

                //Gets the courses added after a short delay.
                load.setVisibility(View.VISIBLE);
                for(int i = 0; i < coursesPicked.size(); i++)
                    getCourse(coursesPicked.get(i));
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
        }

        Intent intent = new Intent(this, ViewSchedule.class);
        Bundle b = new Bundle();
        b.putStringArrayList("coursesPicked", coursesPicked);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }

    public void getCourse(String course){
        //Get the course objects from that
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot dep : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds : dep.getChildren()) {
                        if(Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name"), course)){
                            courses.add(ds.getValue(Course.class));
                            found = true;
                            break;
                        }
                    }
                    if(found)
                        break;
                }
                System.out.println(courses.get(courses.size()-1).courseInfo.get("name"));
                System.out.println(courses.size() + " " + coursesPicked.size());

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
                            text.setVisibility(View.VISIBLE);
                            text.setText(courses.get(i).courseInfo.get("name"));
                        }
                        //If index exists, enable delete button
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

    public void createPopup(View view) {
        //Define elements within popup
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
        courseInfopopup_Instructor = (TextView) CourseInfoPopupView.findViewById(R.id.course_Instructor);
        courseInfopopup_Instructor_box = (TextView)  CourseInfoPopupView.findViewById(R.id.course_Instructor_box);
        courseInfopopup_section_number = (TextView)  CourseInfoPopupView.findViewById(R.id.course_section_number);
        courseInfopopup_section_number_box = (TextView)  CourseInfoPopupView.findViewById(R.id.course_section_number_box);
        courseInfopopup_num_credits = (TextView)  CourseInfoPopupView.findViewById(R.id.num_credits);
        courseInfopopup_num_credits_box = (TextView)  CourseInfoPopupView.findViewById(R.id.num_credits_box);

        courseInfopopup_Back2Sched = (Button) CourseInfoPopupView.findViewById(R.id.Back2Sched);

        //Find course info from database
        if(view.getTag() == null)
            return;

        //Call tag of delete button and use that as index
        String index_num = view.getTag().toString();
        int index = Integer.parseInt(index_num);
        courseInfopopup_nameBox.setText(courses.get(index).courseInfo.get("name"));
        courseInfopopup_courseDescriptionBox.setText(courses.get(index).courseInfo.get("description"));
        courseInfopopup_courseID.setText(courses.get(index).courseInfo.get("courseId"));
        courseInfopopup_courseCode_box.setText(courses.get(index).courseInfo.get("code"));
        courseInfopopup_Instructor_box.setText(Objects.requireNonNull(courses.get(index).classSection.get("Instructors")).replace("[", "").replace("]",""));
        courseInfopopup_section_number_box.setText(courses.get(index).classSection.get("classNumber"));
        courseInfopopup_num_credits_box.setText(courses.get(index).classSection.get("credits"));
        


        //Create popup
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
