package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    //Create a dbUpdater object
    DatabaseUpdater dbUpdater = new DatabaseUpdater();
    ListView courseList;
    ListView chosenCourses;
    ArrayList<String> courses = new ArrayList<>();
    ArrayList<String> coursesPicked = new ArrayList<>();
    String department;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView loginPopup_title, verifyMsg;
    private Button loginPopup_SignIn, loginPopup_create_account, loginBtnHomePage, logoutBtnHomePage, verifyEmailBtn, loginPopupForgotPasswordBtn;
    private EditText loginPopup_email, loginPopup_password;
    AlertDialog.Builder reset_alert;
    LayoutInflater loutInflater;



    FirebaseAuth fAuth;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //Course List
        courseList = findViewById(R.id.courseList);
        chosenCourses = findViewById(R.id.chosenCourses);

        loginBtnHomePage = findViewById(R.id.login);
        logoutBtnHomePage = findViewById(R.id.logout);


        //If we're coming from the filter, we grab the info
        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getStringArrayList("coursesPicked") != null) {
                coursesPicked = b.getStringArrayList("coursesPicked");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coursesPicked);
                chosenCourses.setAdapter(arrayAdapter);
            }
            if (b.getStringArrayList("courses") != null) {
                courses.remove("");
                courses = b.getStringArrayList("courses");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, courses);
                courseList.setAdapter(arrayAdapter);
            }
            if (b.getString("course") != null) {
                if (!b.getString("course").equals("")) {
                    courses.add(b.getString("course"));
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, courses);
                    courseList.setAdapter(arrayAdapter);
                }
            }
            if (b.getString("semester") != null) {
                //
            }
            if (b.getString("department") != null) {
                department = b.getString("department");
            }
        }

        chosenCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                coursesPicked.remove(position);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, coursesPicked);
                chosenCourses.setAdapter(arrayAdapter);
            }
        });

        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (coursesPicked.size() < 4) {
                    coursesPicked.add(courses.get(position));
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, coursesPicked);
                    chosenCourses.setAdapter(arrayAdapter);
                }
            }
        });

        verifyMsg = findViewById(R.id.verifyEmailMsg);
        verifyEmailBtn = findViewById(R.id.verifyEmailBtn);
        fAuth = FirebaseAuth.getInstance();
        reset_alert = new AlertDialog.Builder(this);
        loutInflater = this.getLayoutInflater();

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            if (!fAuth.getCurrentUser().isEmailVerified()) {
                verifyMsg.setVisibility(View.VISIBLE);
                verifyEmailBtn.setVisibility(View.VISIBLE);

            }

            verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //send verification email
                    fAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                            verifyMsg.setVisibility(View.GONE);
                            verifyEmailBtn.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    }




        /** Called when the user taps the Filter button */
        public void goToFilter (View view){
            Intent intent = new Intent(this, FilterActivity.class);
            Bundle b = new Bundle();
            b.putStringArrayList("coursesPicked", coursesPicked);
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
            loginPopupForgotPasswordBtn = (Button) LoginPopupView.findViewById(R.id.forgotPasswordBtn);


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

            loginPopupForgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //start alert dialog
                    View v = loutInflater.inflate(R.layout.forgot_password_email_form_popup, null);
                    reset_alert.setTitle("Forgot Password")
                            .setMessage("Enter Email for Password Reset Link")
                            .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //validate email address
                                    EditText email = v.findViewById(R.id.forgot_password_email_popup);
                                    if(email.getText().toString().isEmpty()){
                                        email.setError("Required");
                                        return;
                                    }
                                    //send reset link
                                    fAuth.sendPasswordResetEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "Reset Email Sent", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }).setNegativeButton("Cancel", null)
                            .setView(v)
                            .create().show();
                }
            });
        }

        private BottomNavigationView.OnNavigationItemSelectedListener navListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = 0;
                        Intent in;
                        switch (item.getItemId()) {
                            case R.id.nav_home:
                                id = R.id.nav_home;
                                break;
                            case R.id.nav_schedule:
                                id = R.id.nav_schedule;
                                in = new Intent(getBaseContext(), ViewSchedule.class);
                                Bundle b = new Bundle();
                                b.putStringArrayList("coursesPicked", coursesPicked);
                                in.putExtras(b);
                                startActivity(in);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
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



        } else {
            loginBtnHomePage.setVisibility(View.VISIBLE);
            logoutBtnHomePage.setVisibility(View.GONE);
        }
        logoutBtnHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.resetPassword){
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                startActivity(new Intent(getApplicationContext(), ResetPassword.class));
            }
            else {
                Toast.makeText(MainActivity.this, "Please Login First", Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
