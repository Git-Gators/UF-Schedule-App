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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class ViewSchedule extends MainActivity implements AdapterView.OnItemSelectedListener, addCustomCourseDialog.DialogListener {
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
                updateScreen();
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
                ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, semesterNames);
                spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                semesterSpinner.setAdapter(spinnerArrayAdapter);

                for(int i = 0; i < semesterNames.length; i++){
                    if(semesterNames[i].equals(semester))
                        semesterSpinner.setSelection(i);
                }
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
            semester = parent.getItemAtPosition(pos).toString();
            coursesPicked.clear();
            loadData();
        }
    }

    public void updateScreen(){
        TextView noCourseText = findViewById(R.id.courseText6);
        if(coursesPicked.isEmpty()){
            noCourseText.setText("No courses selected.");
            noCourseText.setVisibility(View.VISIBLE);
        } else {
            noCourseText.setVisibility(View.INVISIBLE);
        }
        findViewById(R.id.courseText1).setVisibility(View.INVISIBLE);
        findViewById(R.id.delete1).setVisibility(View.INVISIBLE);
        findViewById(R.id.details1).setVisibility(View.INVISIBLE);
        findViewById(R.id.courseText2).setVisibility(View.INVISIBLE);
        findViewById(R.id.delete2).setVisibility(View.INVISIBLE);
        findViewById(R.id.details2).setVisibility(View.INVISIBLE);
        findViewById(R.id.courseText3).setVisibility(View.INVISIBLE);
        findViewById(R.id.delete3).setVisibility(View.INVISIBLE);
        findViewById(R.id.details3).setVisibility(View.INVISIBLE);
        findViewById(R.id.courseText4).setVisibility(View.INVISIBLE);
        findViewById(R.id.delete4).setVisibility(View.INVISIBLE);
        findViewById(R.id.details4).setVisibility(View.INVISIBLE);
        findViewById(R.id.courseText5).setVisibility(View.INVISIBLE);
        findViewById(R.id.delete5).setVisibility(View.INVISIBLE);
        findViewById(R.id.details5).setVisibility(View.INVISIBLE);


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


            if(text != null) {
                text.setVisibility(View.VISIBLE);
                text.setText(coursesPicked.get(i).toString());
                //System.out.println("update screen: " + coursesPicked.get(i).toString());
            }
            //If index exists, enable delete button
            Button deleteAll = findViewById(R.id.delete);
            deleteAll.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            info.setVisibility(View.VISIBLE);
        }
    }

    //Loads data from database to a hashmap named user
    public void loadData()
    {
        //Find the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //TODO fix loading
        if (firebaseUser != null)
        {
            //Get the userId and find their data in Firestore
            this.userId = firebaseUser.getUid();
            documentReference = userdb.collection("users").document(userId);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists())
                    {
                        //If the document snapshot exists, load the data into the user map
                        user = documentSnapshot.getData();
                        if (documentSnapshot.get(semester) != null)
                        {
                            //System.out.println("doc snapshot: " + semester + documentSnapshot.get(semester));
                            //This has to be like the single worst piece of code that I have ever written

                            //For some reason the courses load as a Hashmap of a Hashmap of strings as opposed to course objects.
                            //So to fix this we need to load the Hashmap of a Hashmap of strings into a Map, which I called wierd map because nothing here makes sense
                            ArrayList<HashMap<String, HashMap<String, String>>> wierdMap = (ArrayList<HashMap<String, HashMap<String, String>>>) documentSnapshot.get(semester);

                            //This part is just to make sure we have enough space in the arraylist to store data on our courses
                            int courseObjectsSize = coursesPicked.size();
                            if (courseObjectsSize < wierdMap.size())
                            {
                                for (int i = 0; i < wierdMap.size() - courseObjectsSize; i++)
                                {
                                    Course course = new Course();
                                    coursesPicked.add(course);
                                }
                            }

                            //So since wierdmap has a hashmap containing two hashmaps, courseInfo and classSection,
                            //we copy our data from wierdmap into courseObjects for each course.
                            for (int i = 0; i < wierdMap.size(); i++)
                            {
                                coursesPicked.get(i).courseInfo = wierdMap.get(i).get("courseInfo");
                                coursesPicked.get(i).classSection = wierdMap.get(i).get("classSection");
                            }

                        }
                        user.put(semester, coursesPicked);
                        //If there's course data in the database try to load it
                        if (user.get(semester) != null)
                        {
                            //Typecasting the object from the course to a database should be fine
                            //assuming we store it correctly in the first place

                            for (int i = 0; i < coursesPicked.size(); i++)
                            {
                                Course course = (Course) coursesPicked.get(i);
                                coursesPicked.set(i, course);
                            }
                        }
                        //System.out.println("Courses Picked: " + coursesPicked);
                        updateScreen();
                    }
                }
            });
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        //System.out.println("Spinner: onNothingSelected");
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

        restartSchedule();
    }

    private void restartSchedule(){
        Intent intent = new Intent(this, ViewSchedule.class);
        Bundle b = new Bundle();
        intent.putExtra("coursesPicked", coursesPicked);
        intent.putExtra("crses", crses);
        intent.putExtra("semester", semester);
        intent.putExtra("semesters", semesterNames);
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

        restartSchedule();
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
                    //System.out.println(id);
                    return true;
                }
            };

    private ArrayList<String> getNames(ArrayList<Course> courses){
        ArrayList<String> coursesSTR = new ArrayList<>();
        for(int i = 0; i < courses.size(); i++)
            coursesSTR.add(courses.get(i).toString());

        return coursesSTR;
    }

    public void addCourse(View view){
        Course courseCreated = new Course();
        addCustomCourseDialog addCustomCourseDialog = new addCustomCourseDialog(courseCreated, coursesPicked);
        addCustomCourseDialog.show(getSupportFragmentManager(), "Add Custom Course Dialog");
    }

    @Override
    public void applyCourse(Course course) {
        //Popup stuff
        //coursesPicked.add(crses.get(position));
        coursesPicked.add(course);
        //Add the course to the database
        addCourseToDatabase(coursesPicked);
    }

    public void addCourseToDatabase(ArrayList<Course> course) {
        user.put(semester, coursesPicked);

        //Push the map named user to the database
        if (firebaseUser != null)
        {
            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Course Successfully Added" + userId);
                    restartSchedule();
                }
            });
        }
    }
}
