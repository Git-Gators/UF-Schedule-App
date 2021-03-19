package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Tag:";
    //Create a dbUpdater object
    DatabaseUpdater dbUpdater = new DatabaseUpdater();
    ListView courseList;
    ListView chosenCourses;

    String department;
    String userId;
    static FirebaseUser firebaseUser;
    static DocumentReference documentReference;
    static Map<String, Object> user = new HashMap<>();

    // Courses retrieved from the DB for the user to choose
    ArrayList<Course> crses = new ArrayList<>();

    //Courses the user has already chosen
    ArrayList<Course> coursesPicked = new ArrayList<>();


    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView loginPopup_title;
    private Button loginPopup_SignIn, loginPopup_create_account, loginBtnHomePage, logoutBtnHomePage;
    private EditText loginPopup_email, loginPopup_password;



    FirebaseAuth fAuth;
    FirebaseFirestore userdb = FirebaseFirestore.getInstance();


    public void addCourseToDatabase(ArrayList<Course> course) {
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
                        if (documentSnapshot.get("Courses") != null)
                        {
                            System.out.println(documentSnapshot.get("Courses"));
                            //This has to be like the single worst piece of code that I have ever written

                            //For some reason the courses load as a Hashmap of a Hashmap of strings as opposed to course objects.
                            //So to fix this we need to load the Hashmap of a Hashmap of strings into a Map, which I called wierd map because nothing here makes sense
                            ArrayList<HashMap<String, HashMap<String, String>>> wierdMap = (ArrayList<HashMap<String, HashMap<String, String>>>) documentSnapshot.get("Courses");

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
                        user.put("Courses", coursesPicked);
                        //If there's course data in the database try to load it
                        if (user.get("Courses") != null)
                        {
                            //Typecasting the object from the course to a database should be fine
                            //assuming we store it correctly in the first place

                            for (int i = 0; i < coursesPicked.size(); i++)
                            {
                                Course course = (Course) coursesPicked.get(i);
                                coursesPicked.set(i, course);
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getNames(coursesPicked));
                            chosenCourses.setAdapter(arrayAdapter);
                        }
                    }else
                    {
                        //Otherwise print an error message
                        Toast.makeText(MainActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                    }
                    displayHelp();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    displayHelp();
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                }
            });

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        try {
//            dbUpdater.updateDB(getBaseContext());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //Course List
        courseList = findViewById(R.id.courseList);
        chosenCourses = findViewById(R.id.chosenCourses);

        loginBtnHomePage = findViewById(R.id.login);
        logoutBtnHomePage = findViewById(R.id.logout);


        //If we're coming from the filter, we grab the info
        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        if(b != null){
            if(b.getSerializable("coursesPicked") != null) {
                coursesPicked = (ArrayList<Course>) intent.getSerializableExtra("coursesPicked");

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getNames(coursesPicked));
                chosenCourses.setAdapter(arrayAdapter);
            }
            if(b.getSerializable("crses") != null) {
                crses = (ArrayList<Course>) intent.getSerializableExtra("crses");

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getNames(crses));
                courseList.setAdapter(arrayAdapter);
            }
            displayHelp();
        }

        chosenCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                coursesPicked.remove(position);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getNames(coursesPicked));
                chosenCourses.setAdapter(arrayAdapter);
                displayHelp();

                //Change data in database to reflect deleted course.
                user.put("Courses", coursesPicked);
                //Store the user's information (name, email, and list of course names for now) in the database
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Course Successfully deleted" + userId);
                    }
                });
            }
        });

        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(coursesPicked.size() < 5){
                    coursesPicked.add(crses.get(position));
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getNames(coursesPicked));
                    chosenCourses.setAdapter(arrayAdapter);
                    //Add the course to the database
                    addCourseToDatabase(coursesPicked);
                    displayHelp();
                }
            }
        });
    }

        /** Called when the user taps the Filter button */
        public void goToFilter (View view){
            Intent intent = new Intent(this, FilterActivity.class);
            Bundle b = new Bundle();
            intent.putExtra("coursesPicked", coursesPicked);
            intent.putExtra("crses", crses);
            intent.putExtras(b);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        public void createPopup (View view){
            fAuth = FirebaseAuth.getInstance();
            //*Define elements within popup
            dialogBuilder = new AlertDialog.Builder(this);
            final View LoginPopupView = getLayoutInflater().inflate(R.layout.login_popup, null);
            loginPopup_title = (TextView) LoginPopupView.findViewById(R.id.sign_in_button);
            loginPopup_email = (EditText) LoginPopupView.findViewById(R.id.input_email);
            loginPopup_password = (EditText) LoginPopupView.findViewById(R.id.input_password);
            loginPopup_SignIn = (Button) LoginPopupView.findViewById(R.id.sign_in_button);
            loginPopup_create_account = (Button) LoginPopupView.findViewById(R.id.create_account_button);

            dialogBuilder.setView(LoginPopupView);
            dialog = dialogBuilder.create();
            dialog.show();


            loginPopup_SignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //extract & validate
                    if (loginPopup_email.getText().toString().isEmpty()) {
                        loginPopup_email.setError("Email is Required");
                        return;
                    }
                    if (loginPopup_password.getText().toString().isEmpty()) {
                        loginPopup_password.setError("Password is Required");
                        return;
                    }
                    //valid data
                    //login user
                    fAuth.signInWithEmailAndPassword(loginPopup_email.getText().toString(), loginPopup_password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            //Load the data from the database
                            loadData();
                            dialog.dismiss();
                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

            loginPopup_create_account.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), Register.class));
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

                        switch (item.getItemId()) {
                            case R.id.nav_home:
                                id = R.id.nav_home;
                                break;
                            case R.id.nav_schedule:
                                id = R.id.nav_schedule;
                                in = new Intent(getBaseContext(), ViewSchedule.class);
                                in.putExtra("coursesPicked", coursesPicked);
                                in.putExtras(b);
                                startActivity(in);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                                break;
                            case R.id.nav_calendar:
                                id = R.id.nav_calendar;
                                in = new Intent(getBaseContext(), CalendarView.class);
                                in.putExtra("coursesPicked", coursesPicked);
                                in.putExtras(b);
                                startActivity(in);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                                break;
                        }
                        System.out.println(id);
                        return false;
                    }
                };
    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loginBtnHomePage.setVisibility(View.GONE);
            logoutBtnHomePage.setVisibility(View.VISIBLE);
            loadData();
        } else {
            loginBtnHomePage.setVisibility(View.VISIBLE);
            logoutBtnHomePage.setVisibility(View.GONE);
        }
        logoutBtnHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                userId = "";
                documentReference = null;
                firebaseUser = null;
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    private ArrayList<String> getNames(ArrayList<Course> courses){
        ArrayList<String> coursesSTR = new ArrayList<>();
        for(int i = 0; i < courses.size(); i++)
            coursesSTR.add(courses.get(i).toString());

        return coursesSTR;
    }


    //Displays the help menu if the other lists are empty.
    //Also controls the text above the lists
    private void displayHelp(){
        try {
            TextView getStartedText = findViewById(R.id.getStarted);
            TextView chosenCoursesText = findViewById(R.id.courseText);
            TextView addACourseText = findViewById(R.id.addACourse);
            TextView instrText = findViewById(R.id.instrText);
            TextView instrText2 = findViewById(R.id.instrText2);

            //Top List
            if(coursesPicked.isEmpty()){
                chosenCoursesText.setVisibility(View.INVISIBLE);
            } else {
                chosenCoursesText.setVisibility(View.VISIBLE);
            }

            //Bottom List
            if(crses.isEmpty()){
                addACourseText.setVisibility(View.INVISIBLE);
            } else {
                addACourseText.setVisibility(View.VISIBLE);
            }

            //Both are empty
            if(coursesPicked.isEmpty() && crses.isEmpty()) {
                getStartedText.setVisibility(View.VISIBLE);
                instrText.setVisibility(View.VISIBLE);
                instrText2.setVisibility(View.VISIBLE);
            } else {
                getStartedText.setVisibility(View.INVISIBLE);
                instrText.setVisibility(View.INVISIBLE);
                instrText2.setVisibility(View.INVISIBLE);
            }
        } catch (NullPointerException e){
            //Throws exception when changing to a different view. We gotta catch it.
        }
    }
}
