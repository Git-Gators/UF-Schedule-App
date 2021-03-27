package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends AppCompatActivity {

    private static final String TAG = "TAG";
    private TextView loginPopup_title;
    private Button loginPopup_SignIn, loginPopup_create_account, loginBtnHomePage, logoutBtnHomePage;
    private EditText loginPopup_email, loginPopup_password;
    FirebaseAuth fAuth;
    String userId;
    static FirebaseUser firebaseUser;
    static DocumentReference documentReference;
    static Map<String, Object> user = new HashMap<>();

    //Courses the user has already chosen
    ArrayList<Course> coursesPicked = new ArrayList<>();

    FirebaseFirestore userdb = FirebaseFirestore.getInstance();

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
                        }
                    }else
                    {
                        //Otherwise print an error message
                        Toast.makeText(LoginPage.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginPage.this, "Error", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                }
            });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_popup);
        fAuth = FirebaseAuth.getInstance();
        //*Define elements within popup
        loginPopup_title = (TextView) findViewById(R.id.sign_in_title);
        loginPopup_email = (EditText) findViewById(R.id.input_email);
        loginPopup_password = (EditText) findViewById(R.id.input_password);
        loginPopup_SignIn = (Button) findViewById(R.id.sign_in_button);
        loginPopup_create_account = (Button) findViewById(R.id.create_account_button);

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
                        startMain(view);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void startMain(View view){
        Intent in;
        Bundle b = new Bundle();
        in = new Intent(getBaseContext(), MainActivity.class);
        in.putExtras(b);
        startActivity(in);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}