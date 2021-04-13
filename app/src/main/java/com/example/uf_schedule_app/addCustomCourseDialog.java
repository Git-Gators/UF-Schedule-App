package com.example.uf_schedule_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class addCustomCourseDialog extends AppCompatDialogFragment {
    private Course course;
    private ArrayList<Course> coursesPicked = new ArrayList<>();;
    private DialogListener listener;


    addCustomCourseDialog(Course courseInput, ArrayList<Course> courseSchedule){
        course = courseInput;
        coursesPicked = courseSchedule;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_custom_course, null);

        EditText nameBox = view.findViewById(R.id.nameBox);
        nameBox.setText("");

        EditText courseCode_box = view.findViewById(R.id.courseCode_box);
        courseCode_box.setText("");

        EditText courseID = view.findViewById(R.id.courseID);
        courseID.setText("");

        EditText course_section_number_box = view.findViewById(R.id.course_section_number_box);
        course_section_number_box.setText("");

        EditText num_credits_box = view.findViewById(R.id.num_credits_box);
        num_credits_box.setText("");

        EditText course_Instructor_box = view.findViewById(R.id.course_Instructor_box);
        course_Instructor_box.setText("");

        EditText courseDescription_box = view.findViewById(R.id.courseDescription_box);
        courseDescription_box.setText("");

        builder.setView(view).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                course = null;
            }
        });

        builder.setView(view).setPositiveButton("Add Course", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText nameBox = view.findViewById(R.id.nameBox);
                EditText courseCode_box = view.findViewById(R.id.courseCode_box);
                EditText courseID = view.findViewById(R.id.courseID);
                EditText course_section_number_box = view.findViewById(R.id.course_section_number_box);
                EditText num_credits_box = view.findViewById(R.id.num_credits_box);
                EditText course_Instructor_box = view.findViewById(R.id.course_Instructor_box);
                EditText courseDescription_box = view.findViewById(R.id.courseDescription_box);

                course.classSection.put("classNumber", course_section_number_box.getText().toString());
                course.classSection.put("credits", num_credits_box.getText().toString());
                course.classSection.put("deptName", "");
                course.classSection.put("Instructors", course_Instructor_box.getText().toString());
                course.classSection.put("meetDays", "");
                course.classSection.put("meetPeriod", "");
                course.classSection.put("meetTime", "");
                course.classSection.put("number", "");
                course.courseInfo.put("code", courseCode_box.getText().toString());
                course.courseInfo.put("courseId", courseID.getText().toString());
                course.courseInfo.put("description", courseDescription_box.getText().toString());
                course.courseInfo.put("name", nameBox.getText().toString());
                course.courseInfo.put("prerequisites", "");
                course.courseInfo.put("termInd", "");
                listener.applyCourse(course);
            }
        });

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
}
