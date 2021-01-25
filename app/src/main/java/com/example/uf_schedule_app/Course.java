package com.example.uf_schedule_app;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class Course {
    private HashMap<String, String> courseInfo;
    private ArrayList<HashMap<String, String>> sectionMaps = new ArrayList<HashMap<String, String>>();

    public void updateCourseInfo(HashMap<String, String> info){
        courseInfo = info;
    }

    public HashMap<String, String> getCourseInfo(){
        return courseInfo;
    }

    public void setSections(String input){
        input = input.replace(" ", "");
        System.out.println("Sections: " + input);
        String[] sections = input.split("[{},]");

        for(int i = 0; i < sections.length; i++) {
            System.out.println("Section: " + sections[i]);
        }

        for(int i = 0; i < sections.length; i++) {
            System.out.println("Section: " + sections[i]);
            if(sections[i].isEmpty()) {
                continue;
            }
            if(sections[i].split("=")[0].equals("deptName")){
                sectionMaps.add(new HashMap<String, String>());
                sectionMaps.get(sectionMaps.size()-1).put("deptName", sections[i+1].split("=")[1]);
            }
            if(sections[i].split("=")[0].equals("number")){
                sectionMaps.get(sectionMaps.size()-1).put("number", sections[i+1].split("=")[1]);
            }
            if(sections[i].split("=")[0].equals("credits")){
                sectionMaps.get(sectionMaps.size()-1).put("credits", sections[i+1].split("=")[1]);
            }
        }

        for(int i = 0; i < sectionMaps.size(); i++){
            System.out.println("=1="+ sectionMaps.get(i).keySet() + sectionMaps.get(i).values().toString());
        }
    }
}
