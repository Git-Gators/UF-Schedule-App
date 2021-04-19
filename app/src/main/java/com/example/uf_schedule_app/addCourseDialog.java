package com.example.uf_schedule_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class addCourseDialog extends AppCompatDialogFragment {
    private Course course;
    private ArrayList<Course> coursesPicked = new ArrayList<>();;
    private DialogListener listener;

    addCourseDialog(Course courseInput, ArrayList<Course> courseSchedule){
        course = courseInput;
        coursesPicked = courseSchedule;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_course, null);

        TextView nameBox = view.findViewById(R.id.nameBox);
        nameBox.setText(course.courseInfo.get("name"));

        TextView courseCode_box = view.findViewById(R.id.courseCode_box);
        courseCode_box.setText(course.courseInfo.get("code"));

        TextView courseID = view.findViewById(R.id.courseID);
        courseID.setText(course.courseInfo.get("courseId"));

        TextView course_section_number_box = view.findViewById(R.id.course_section_number_box);
        course_section_number_box.setText(course.classSection.get("classNumber"));

        TextView num_credits_box = view.findViewById(R.id.num_credits_box);
        num_credits_box.setText(course.classSection.get("credits"));

        TextView course_Instructor_box = view.findViewById(R.id.course_Instructor_box);
        course_Instructor_box.setText(Objects.requireNonNull(course.classSection.get("Instructors")).replace("[", "").replace("]",""));

        TextView courseDescription_box = view.findViewById(R.id.courseDescription_box);
        courseDescription_box.setText(course.courseInfo.get("description"));

        builder.setView(view).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                course = null;
            }
        });

        boolean overlap = checkTimeOverlap();
        if(!overlap){
            view.findViewById(R.id.overlapText).setVisibility(View.INVISIBLE);
            builder.setView(view).setPositiveButton("Add Course", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.applyCourse(course);
                }
            });
        }

        return builder.create();
    }

    public interface DialogListener {
        void applyCourse(Course course);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DialogListener");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkTimeOverlap(){
        boolean overlap = false;
        String meetDays = course.getMeetDays();
        if(meetDays.equals("Online")) {
            return false;
        }

        //Have to split M,F into M, F
        ArrayList<String> wantedDays = new ArrayList<>();
        ArrayList<String> wantedTimes = new ArrayList<>();
        String[] wantedMeetTiems = meetDays.split("(, )");
        for(int j = 0; j < wantedMeetTiems.length; j++){
            String potentialDay = wantedMeetTiems[j].split(": ")[0];
            if(potentialDay.contains(",")){
                String[] twoDays = potentialDay.split(",");
                wantedDays.add(twoDays[0]);
                wantedDays.add(twoDays[1]);
                wantedTimes.add(wantedMeetTiems[j].split(": ")[1].replace("AM", "" ).replace("PM", "").replace(" ",""));
            } else {
                wantedDays.add(potentialDay);
            }
            wantedTimes.add(wantedMeetTiems[j].split(": ")[1].replace("AM", "" ).replace("PM", "").replace(" ",""));
        }

        for(int i = 0; i < coursesPicked.size(); i++){
            String[] meetTimes = coursesPicked.get(i).getMeetDays().split("(, )");
            if(Arrays.toString(meetTimes).equals("[Online]"))
                continue;

            ArrayList<String> days = new ArrayList<>();
            ArrayList<String> times = new ArrayList<>();

            for(int j = 0; j < meetTimes.length; j++){
                String potentialDay = meetTimes[j].split(": ")[0];
                if(potentialDay.contains(",")){
                    String[] twoDays = potentialDay.split(",");
                    days.add(twoDays[0]);
                    days.add(twoDays[1]);
                    times.add(meetTimes[j].split(": ")[1].replace("AM", "" ).replace("PM", "").replace(" ",""));
                } else {
                    days.add(potentialDay);
                }
                times.add(meetTimes[j].split(": ")[1].replace("AM", "" ).replace("PM", "").replace(" ",""));
            }

            //For each day in wantedDays => We check if the current course has that day
            for(int k = 0; k < wantedDays.size(); k++) {
                if(days.contains(wantedDays.get(k))){
                    //If they have that day => we compare the time for that day
                    String[] newTime = wantedTimes.get(k).split("-");
                    String[] oldTime = times.get(days.indexOf(wantedDays.get(k))).split("-");

                    LocalTime startA = LocalTime.of(Integer.parseInt(newTime[0].split(":")[0]), Integer.parseInt(newTime[0].split(":")[1]));
                    LocalTime stopA = LocalTime.of(Integer.parseInt(newTime[1].split(":")[0]), Integer.parseInt(newTime[1].split(":")[1]));
                    LocalTime startB = LocalTime.of(Integer.parseInt(oldTime[0].split(":")[0]), Integer.parseInt(oldTime[0].split(":")[1]));
                    LocalTime stopB = LocalTime.of(Integer.parseInt(oldTime[1].split(":")[0]), Integer.parseInt(oldTime[1].split(":")[1]));

                    if(startA.isBefore(stopB) && stopA.isAfter(startB))
                        overlap = true;
                }
            }
        }

        return overlap;
    }
}
