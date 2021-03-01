package com.example.uf_schedule_app;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class Course implements java.io.Serializable{
    public HashMap<String, String> courseInfo = new HashMap<String, String>();
    public HashMap<String, String> classSection = new HashMap<String, String>();
}
