package com.example.uf_schedule_app;

import android.os.Build;

import androidx.annotation.RequiresApi;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setSections(String input){
        //Two meeting times breaks it
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
                String deptName = sections[i].split("=")[1];
                sectionMaps.get(sectionMaps.size()-1).put("deptName", spaceAdder(deptName));
            }
            if(sections[i].split("=")[0].equals("number")){
                sectionMaps.get(sectionMaps.size()-1).put("number", sections[i].split("=")[1]);
            }
            if(sections[i].split("=")[0].equals("credits")){
                sectionMaps.get(sectionMaps.size()-1).put("credits", sections[i].split("=")[1]);
            }
            if(sections[i].split("=")[0].equals("classNumber")){
                sectionMaps.get(sectionMaps.size()-1).put("classNumber", sections[i].split("=")[1]);
            }
            if(sections[i].split("=")[0].equals("instructors")){
                String instructors = "[";
                int j = 1;
                try {
                    while(true){
                        String[] inst = sections[i+j].split("=");
                        if(inst[0].equals(inst[1])){
                            instructors += inst[0] + ", ";
                            j++;
                        } else {
                            break;
                        }
                    }
                } catch (Exception e){

                }
                StringBuffer fix = new StringBuffer(spaceAdder(instructors));
                fix.deleteCharAt(1);
                fix.deleteCharAt(fix.length()-1);
                fix.deleteCharAt(fix.length()-1);
                fix.insert(fix.length(), ']');
                sectionMaps.get(sectionMaps.size()-1).put("instructors", fix.toString());
            }
            if(sections[i].contains("meetPeriod")){
                if(!sectionMaps.get(sectionMaps.size()-1).containsKey("meetPeriod")){
                    String meet = sections[i+1].split("=")[1] + "-" + sections[i].split("=")[1];
                    sectionMaps.get(sectionMaps.size()-1).put("meetPeriod", meet);
                    i +=1;
                } else {
                    String meet = sections[i+1].split("=")[1] + "-" + sections[i].split("=")[1];
                    sectionMaps.get(sectionMaps.size()-1).replace("meetPeriod", "[" + sectionMaps.get(sectionMaps.size()-1).get("meetPeriod") + ", " + meet + "]");
                    i +=1;
                }
            }
            if(sections[i].contains("meetTimeBegin")){
                if(!sectionMaps.get(sectionMaps.size()-1).containsKey("meetTimes")){
                    sectionMaps.get(sectionMaps.size()-1).put("meetTimes", sections[i].split("=")[1]);
                } else {
                    sectionMaps.get(sectionMaps.size()-1).replace("meetTimes", "[" + sectionMaps.get(sectionMaps.size()-1).get("meetTimes") + ", " + sections[i].split("=")[1]);
                }
            }
            if(sections[i].contains("meetTimeEnd")){
                if(sectionMaps.get(sectionMaps.size()-1).get("meetTimes").contains(","))
                    sectionMaps.get(sectionMaps.size()-1).replace("meetTimes", sectionMaps.get(sectionMaps.size()-1).get("meetTimes") + "-" + sections[i].split("=")[1] + "]");
                else
                    sectionMaps.get(sectionMaps.size()-1).replace("meetTimes", sectionMaps.get(sectionMaps.size()-1).get("meetTimes") + "-" + sections[i].split("=")[1]);
            }
            if(sections[i].contains("meetDays")){
                if(!sectionMaps.get(sectionMaps.size()-1).containsKey("meetDays")) {
                    StringBuilder days = new StringBuilder();
                    days.append(sections[i].split("=")[1]);
                    while(true) {
                        days.append(", " + sections[i + 1]);
                        i++;
                        if(!sections[i + 1].contains("]"))
                            break;
                    }
                    sectionMaps.get(sectionMaps.size() - 1).put("meetDays", days.toString());
                } else {
                    StringBuilder days = new StringBuilder();
                    days.append(sectionMaps.get(sectionMaps.size()-1).get("meetDays") + sections[i].split("=")[1]);
                    while(true) {
                        days.append(", " + sections[i + 1]);
                        i++;
                        if(!sections[i + 1].contains("]"))
                            break;
                    }
                    sectionMaps.get(sectionMaps.size() - 1).put("meetDays", days.toString());
                }
            }
        }

        for(int i = 0; i < sectionMaps.size(); i++){
            System.out.println(sectionMaps.get(i).keySet() + sectionMaps.get(i).values().toString());
        }
    }

    private String spaceAdder(String oldStr){
        StringBuffer fixer = new StringBuffer(oldStr);
        int changes = 0;
        for(int j = 1; j < oldStr.length(); j++){
            if(oldStr.charAt(j) >= 'A' && oldStr.charAt(j) <= 'Z'){
                if(oldStr.charAt(j-1) != '-'){
                    fixer.insert(j+changes, " ");
                    changes++;
                }
            }
            if(oldStr.charAt(j) == 'a' && oldStr.charAt(j+1) == 'n' && oldStr.charAt(j+2) == 'd'){
                fixer.insert(j+changes, " ");
                changes++;
            }
        }
        return fixer.toString();
    }
}
