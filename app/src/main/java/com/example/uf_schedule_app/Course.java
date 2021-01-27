package com.example.uf_schedule_app;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class Course {
    public HashMap<String, String> courseInfo = new HashMap<String, String>();
    public ArrayList<HashMap<String, String>> classSections = new ArrayList<HashMap<String, String>>();
}
