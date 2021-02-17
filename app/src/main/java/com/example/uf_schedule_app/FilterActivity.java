package com.example.uf_schedule_app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.r0adkll.slidr.Slidr;

public class FilterActivity extends MainActivity {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_view);

        getSupportActionBar().setTitle("Filters");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Swipe Right to go back to Main
        Slidr.attach(this);
    }

    /** Called when the user taps the GO BACK button */
    public void goToMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}