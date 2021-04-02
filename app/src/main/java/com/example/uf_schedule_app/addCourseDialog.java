package com.example.uf_schedule_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.w3c.dom.Text;

import java.util.Objects;

public class addCourseDialog extends AppCompatDialogFragment {
    private Course course;
    private DialogListener listener;

    addCourseDialog(Course courseInput){
        course = courseInput;
    }

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


        builder.setView(view).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                course = null;
            }
        }).setPositiveButton("Add Course", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
