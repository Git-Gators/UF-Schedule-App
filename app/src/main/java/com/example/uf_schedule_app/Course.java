package com.example.uf_schedule_app;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class Course implements java.io.Serializable{
    public HashMap<String, String> courseInfo = new HashMap<String, String>();
    public HashMap<String, String> classSection = new HashMap<String, String>();

    @NonNull
    @Override
    public String toString() {
        return this.courseInfo.get("code") + " - " + this.courseInfo.get("name") +
                "\n" + this.getInstructor() +
                "\n" + this.getMeetDays();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!obj.getClass().equals(Course.class))
            return false;
        Course right = (Course) obj;
        if(!this.courseInfo.get("name").equals(right.courseInfo.get("name")))
            return false;
        if(!this.courseInfo.get("code").equals(right.courseInfo.get("code")))
            return false;
        if(!this.courseInfo.get("courseId").equals(right.courseInfo.get("courseId")))
            return false;
        if(!this.classSection.get("number").equals(right.classSection.get("number")))
            return false;
        return true;
    }

    public String getSectionNumber(){
        return this.classSection.get("number");
    }

    public String getInstructor(){
        return this.classSection.get("Instructors").replace("[", "").replace("]", "");
    }

    public String getMeetDays(){
        String[] days = this.classSection.get("meetDays").split("]");
        String[] meetTimes = this.classSection.get("meetTime").split("]");

        ArrayList<String> fixedTimes = new ArrayList<>();

        try{
            System.out.println(Arrays.toString(days));
            System.out.println(Arrays.toString(meetTimes));
            for(int i = 0; i < days.length; i++){
                fixedTimes.add(days[i].replace("[", "").replace("\"", "") + ": " + meetTimes[i].replace("[", "").replace("\"", ""));
            }
        } catch (Exception e) {
            System.out.println("Error concating meet times");
        }
        return fixedTimes.toString().replace("[", "").replace("]", "");
    }
}
