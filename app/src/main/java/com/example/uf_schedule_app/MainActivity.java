package com.example.uf_schedule_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    DatabaseUpdater dbUpdater = new DatabaseUpdater();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //dbUpdater.getUFCourses();
        ArrayList<String> courseCodes = new ArrayList<String>();
        courseCodes.add("ABE3000C");
        courseCodes.add("ABE4034");
        courseCodes.add("ABE4413C");
        courseCodes.add("ABE4932");
        dbUpdater.getCourseFromDB(courseCodes);
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void refreshCourses(View view) {
        for(int i = 0; i < dbUpdater.coursesRetr.size(); i++){
            //There has to be a smarter way
            EditText editText = null;
            if(i == 0)
                editText = (EditText) findViewById(R.id.course1);
            if(i == 1)
                editText = (EditText) findViewById(R.id.course2);
            if(i == 2)
                editText = (EditText) findViewById(R.id.course3);
            if(i == 3)
                editText = (EditText) findViewById(R.id.course4);

            editText.setText(dbUpdater.coursesRetr.get(i).courseInfo.get("name"));
        }
    }
}
