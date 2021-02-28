package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    FirebaseAuth fAuth;
    FirebaseFirestore userdb = FirebaseFirestore.getInstance();
    String userId;

    private static final String TAG = MainActivity.class.getSimpleName();

    EditText registerFullName, registerEmail, registerPassword, registerConfPassword;
    Button registerBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //connects views and variables
        registerFullName = findViewById(R.id.registerFullName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfPassword = findViewById(R.id.registerConfPassword);
        registerBtn = findViewById(R.id.registerBtn);

        fAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //extract data from form
                String fullName = registerFullName.getText().toString();
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String confPassword = registerConfPassword.getText().toString();



                if(fullName.isEmpty()){
                    registerFullName.setError("Full Name is Required");
                    return;
                }
                if(email.isEmpty()){
                    registerFullName.setError("Email is Required");
                    return;
                }
                if(password.isEmpty()){
                    registerFullName.setError("Password is Required");
                    return;
                }
                if(confPassword.isEmpty()){
                    registerFullName.setError("Password is Required");
                    return;
                }
                if(!password.equals(confPassword)){
                    registerConfPassword.setError("Passwords Do not Match");
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //send user to home page on success
                        userId = fAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = userdb.collection("users").document(userId);

                        //Create a map with the user's information
                        Map<String, Object> user = new HashMap<>();
                        user.put("Full Name", fullName);
                        user.put("Email", email);

                        //Store the user's information (name and email for now) in the database
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSucess: user profile is created for " + userId);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "An error occurred, courses not uploaded to database");

                            }
                        });

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}