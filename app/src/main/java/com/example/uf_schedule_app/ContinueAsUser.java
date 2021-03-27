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


public class ContinueAsUser extends AppCompatActivity {
    private Button continueAsUser_continue, continueAsUser_logout;
    private TextView textView2, continueAsUser_name, textView4;

    FirebaseAuth fAuth;
    String userId;
    static FirebaseUser firebaseUser;
    static DocumentReference documentReference;
    static Map<String, Object> user = new HashMap<>();

    //Courses the user has already chosen
    ArrayList<Course> coursesPicked = new ArrayList<>();

    FirebaseFirestore userdb = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.continue_as_user);
        fAuth = FirebaseAuth.getInstance();
        continueAsUser_name = (TextView) findViewById(R.id.textView3);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView4 = (TextView) findViewById(R.id.textView4);
        continueAsUser_continue = (Button) findViewById(R.id.continue1);
        continueAsUser_logout = (Button) findViewById(R.id.logout2);

        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();

        if (b != null)
        {
            if (b.getSerializable("fAuth") != null)
            {
                fAuth = (FirebaseAuth) intent.getSerializableExtra("fAuth");
            }
            else
            {
                fAuth = FirebaseAuth.getInstance();
            }
        }
        else
        {
            fAuth = FirebaseAuth.getInstance();
        }

        if (fAuth.getCurrentUser() != null && fAuth.getCurrentUser().getDisplayName() != null)
        {
            continueAsUser_name.setText(fAuth.getCurrentUser().getDisplayName());
        }
        else
        {
            String nullString = "NULL";
            continueAsUser_name.setText(nullString);
        }

        continueAsUser_continue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                Intent in;
                in = new Intent(getBaseContext(), MainActivity.class);
                in.putExtras(b);
                startActivity(in);
                finish();
            }
        });

        continueAsUser_logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                userId = "";
                documentReference = null;
                firebaseUser = null;
                startActivity(new Intent(getApplicationContext(), LoginPage.class));
                finish();
            }
        });
    }
}
