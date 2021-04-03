package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.app.AlertDialog;

import android.content.Intent;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;


public class ViewSchedule extends MainActivity implements AdapterView.OnItemSelectedListener {
    // Courses retrieved from the DB for the user to choose
    ArrayList<Course> crses = new ArrayList<>();
    String[] semesterNames;

    //Courses the user has already chosen
    ArrayList<Course> coursesPicked = new ArrayList<>();

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
        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        if(b != null){
            if(b.getSerializable("coursesPicked") != null){
                coursesPicked = (ArrayList<Course>) intent.getSerializableExtra("coursesPicked");

                //Edit all the courseTexts
                for(int i = 0; i < coursesPicked.size(); i++){
                    TextView text = null;
                    Button delete = null;
                    Button info = null;

                    if(i == 0){
                        text = findViewById(R.id.courseText1);
                        delete = findViewById(R.id.delete1);
                        info = findViewById(R.id.details1);
                    } else if(i == 1){
                        text = findViewById(R.id.courseText2);
                        delete = findViewById(R.id.delete2);
                        info = findViewById(R.id.details2);
                    } else if(i == 2){
                        text = findViewById(R.id.courseText3);
                        delete = findViewById(R.id.delete3);
                        info = findViewById(R.id.details3);
                    } else if(i == 3){
                        text = findViewById(R.id.courseText4);
                        delete = findViewById(R.id.delete4);
                        info = findViewById(R.id.details4);
                    } else if(i == 4) {
                        text = findViewById(R.id.courseText5);
                        delete = findViewById(R.id.delete5);
                        info = findViewById(R.id.details5);
                    }

                    if (text == null && i == 0) {
                        text = findViewById(R.id.courseText6);
                        text.setText("No courses selected.");
                    }
                    else if(text != null) {
                        text.setVisibility(View.VISIBLE);
                        text.setText(coursesPicked.get(i).toString());
                    }
                    //If index exists, enable delete button
                    Button deleteAll = findViewById(R.id.delete);
                    deleteAll.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    info.setVisibility(View.VISIBLE);
                }
            } else {
                load.setVisibility(View.VISIBLE);
                TextView text = null;
                text = findViewById(R.id.courseText6);
                text.setText("No courses selected.");
            }
            if(b.getSerializable("crses") != null) {
                crses = (ArrayList<Course>) intent.getSerializableExtra("crses");
            }
            if(b.getSerializable("semesters") != null){
                semesterNames = (String[]) intent.getSerializableExtra("semesters");
                Spinner semesterSpinner = findViewById(R.id.semesterSpinner);
                semesterSpinner.setOnItemSelectedListener(this);
                ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, semesterNames);
                spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                semesterSpinner.setAdapter(spinnerArrayAdapter);
            }
            if(b.getSerializable("semester") != null) {
                semester = (String) intent.getSerializableExtra("semester");
            }
        }
        else {
            load.setVisibility(View.VISIBLE);
            TextView text = null;
            text = findViewById(R.id.courseText6);
            text.setText("No courses selected.");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.semesterSpinner) {
            System.out.println("TRUE");
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("Spinner: onNothingSelected");
    }

    //Delete all courses
    public void goToDelete(View view) {
        coursesPicked.clear();

        user.put("Courses", coursesPicked);

        //Push the map named user to the database
        if (firebaseUser != null)
        {
            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Course Successfully Added" + userId);
                }
            });
        }

        Intent intent = new Intent(this, ViewSchedule.class);
        Bundle b = new Bundle();
        intent.putExtra("coursesPicked", coursesPicked);
        intent.putExtra("crses", crses);
        intent.putExtras(b);
        startActivity(intent);
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
        user.put("Courses", coursesPicked);

        //Push the map named user to the database
        if (firebaseUser != null)
        {
            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Course Successfully Added" + userId);
                }
            });
        }

        Intent intent = new Intent(this, ViewSchedule.class);
        Bundle b = new Bundle();
        intent.putExtra("coursesPicked", coursesPicked);
        intent.putExtra("crses", crses);
        intent.putExtras(b);
        startActivity(intent);
        finish();
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
        courseInfopopup_nameBox.setText(coursesPicked.get(index).courseInfo.get("name"));
        courseInfopopup_courseDescriptionBox.setText(coursesPicked.get(index).courseInfo.get("description"));
        courseInfopopup_courseID.setText(coursesPicked.get(index).courseInfo.get("courseId"));
        courseInfopopup_courseCode_box.setText(coursesPicked.get(index).courseInfo.get("code"));
        courseInfopopup_Instructor_box.setText(Objects.requireNonNull(coursesPicked.get(index).classSection.get("Instructors")).replace("[", "").replace("]",""));
        courseInfopopup_section_number_box.setText(coursesPicked.get(index).classSection.get("classNumber"));
        courseInfopopup_num_credits_box.setText(coursesPicked.get(index).classSection.get("credits"));
        


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
                    Bundle b = new Bundle();
                    switch(item.getItemId()){
                        case R.id.nav_home:
                            in = new Intent(getBaseContext(), MainActivity.class);
                            in.putExtra("coursesPicked", coursesPicked);
                            in.putExtras(b);
                            in.putExtra("semesters", semesterNames);
                            in.putExtra("semester", semester);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                            break;
                        case R.id.nav_schedule:
                            id = R.id.nav_schedule;
                            break;
                        case R.id.nav_calendar:
                            id = R.id.nav_calendar;
                            in = new Intent(getBaseContext(), CalendarView.class);
                            in.putExtra("coursesPicked", coursesPicked);
                            in.putExtra("semesters", semesterNames);
                            in.putExtra("semester", semester);
                            in.putExtras(b);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                            break;
                    }
                    System.out.println(id);
                    return true;
                }
            };

    private ArrayList<String> getNames(ArrayList<Course> courses){
        ArrayList<String> coursesSTR = new ArrayList<>();
        for(int i = 0; i < courses.size(); i++)
            coursesSTR.add(courses.get(i).toString());

        return coursesSTR;
    }
}
