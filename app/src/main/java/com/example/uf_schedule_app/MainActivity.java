package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Button;
import android.app.AlertDialog;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Tag:";
    //Create a dbUpdater object
    DatabaseUpdater dbUpdater = new DatabaseUpdater();
    ListView courseList;
    ListView chosenCourses;
    ArrayList<String> courses = new ArrayList<>();
    //This variable contains everything anyone could possibly want to know about the user's courses
    ArrayList<Course> courseObjects = new ArrayList<>();
    ArrayList<String> coursesPicked = new ArrayList<>();
    String department;
    String userId;
    static FirebaseUser firebaseUser;
    static DocumentReference documentReference;
    static Map<String, Object> user = new HashMap<>();


    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView loginPopup_title;
    private Button loginPopup_SignIn, loginPopup_create_account, loginBtnHomePage, logoutBtnHomePage;
    private EditText loginPopup_email, loginPopup_password;



    FirebaseAuth fAuth;
    FirebaseFirestore userdb = FirebaseFirestore.getInstance();


    public void addCourseToDatabase(String course) {
        //Some code I "borrowed" from Jason to set the course objects
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot dep : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds : dep.getChildren()) {
                        if (Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name"), course)) {
                            courseObjects.add(ds.getValue(Course.class));
                            found = true;
                            break;
                        }
                    }
                    //We want to make sure the course has actually been loaded before we push it to
                    //the database or we'll get a bunch of nullptrexceptions
                    if (found) {
                        //Add user's updated course information to the user's map
                        user.put("Courses", courseObjects);

                        //Push the map named user to the database
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Course Successfully Added" + userId);
                            }
                        });
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        mDatabase.addValueEventListener(postListener);
    }

    //Loads data from database to a hashmap named user
    public void loadData()
    {
        //Find the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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
                        //If there's course data in the database try to load it
                        if (user.get("courses") != null)
                        {
                            //Typecasting the object from the course to a database should be fine
                            //assuming we store it correctly in the first place
                            courseObjects = (ArrayList<Course>) user.get("courses");
                        }
                    }else
                    {
                        //Otherwise print an error message
                        Toast.makeText(MainActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
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
            if(b.getStringArrayList("coursesPicked") != null){
                coursesPicked = b.getStringArrayList("coursesPicked");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coursesPicked);
                chosenCourses.setAdapter(arrayAdapter);
            }
            if(b.getStringArrayList("courses") != null){
                courses.remove("");
                courses = b.getStringArrayList("courses");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, courses);
                courseList.setAdapter(arrayAdapter);
            }
            if(b.getSerializable("courseList") != null) {
                courseObjects = (ArrayList<Course>) intent.getSerializableExtra("courseList");
            }
        }

        chosenCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                coursesPicked.remove(position);
                courseObjects.remove(position);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, coursesPicked);
                chosenCourses.setAdapter(arrayAdapter);

                //Change data in database to reflect deleted course.

                user.put("Courses", courseObjects);
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
                if(coursesPicked.size() < 4){
                    coursesPicked.add(courses.get(position));
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, coursesPicked);
                    chosenCourses.setAdapter(arrayAdapter);
                    //Add the course to the database
                    addCourseToDatabase(coursesPicked.get(coursesPicked.size()-1));
                }
            }
        });
    }

        /** Called when the user taps the Filter button */
        public void goToFilter (View view){
            Intent intent = new Intent(this, FilterActivity.class);
            Bundle b = new Bundle();
            b.putStringArrayList("coursesPicked", coursesPicked);
            intent.putExtra("courseList", courseObjects);
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

                            /*//Find the current user
                            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                            if (firebaseUser != null)
                            {
                                //Get the userId and find their data in Firestore
                                userId = firebaseUser.getUid();
                                documentReference = userdb.collection("users").document(userId);
                                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists())
                                        {
                                            //If the document snapshot exists, load the data into the user map
                                            user = documentSnapshot.getData();
                                        }else
                                        {
                                            //Otherwise print an error message
                                            Toast.makeText(MainActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, e.toString());
                                    }
                                });

                            }*/

                            loadData();

                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
                        switch (item.getItemId()) {
                            case R.id.nav_home:
                                id = R.id.nav_home;
                                break;
                            case R.id.nav_schedule:
                                id = R.id.nav_schedule;
                                getCourse();
                                break;
                            case R.id.nav_calendar:
                                id = R.id.nav_calendar;
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


    public void getCourse(){
        ArrayList<Course> courseList = new ArrayList<>();
        //Get the course objects from that
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(int i = 0; i < coursesPicked.size(); i++){
                    boolean found = false;
                    for (DataSnapshot dep : dataSnapshot.getChildren()) {
                        for (DataSnapshot ds : dep.getChildren()) {
                            if(Objects.equals(Objects.requireNonNull(ds.getValue(Course.class)).courseInfo.get("name"), coursesPicked.get(i))){
                                courseList.add(ds.getValue(Course.class));
                                found = true;
                                break;
                            }
                        }
                        if(found)
                            break;
                    }
                }

                //We have all the courses
                if(courseList.size() == coursesPicked.size()){
                    System.out.println("Course List: " + courseList);
                    Intent in;
                    in = new Intent(getBaseContext(), ViewSchedule.class);
                    Bundle b = new Bundle();
                    b.putStringArrayList("coursesPicked", coursesPicked);
                    in.putExtra("courses", courseList);
                    in.putExtras(b);
                    startActivity(in);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        mDatabase.addValueEventListener(postListener);
    }


}
