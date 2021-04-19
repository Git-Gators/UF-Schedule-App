package com.example.uf_schedule_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class Schedule_Adapter extends RecyclerView.Adapter<Schedule_Adapter.ScheduleViewHolder>{

    String data1[], data2[];
    int colorVal[];
    Context context;
    ArrayList<Course> courses;
    RecyclerView recyclerView;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView courseInfopopup_Name, courseInfopopup_nameBox;
    private TextView courseInfopopup_Course_Description, courseInfopopup_courseDescriptionBox;
    private TextView courseInfopopup_courseCode_box, courseInfopopup_CourseCode;
    private TextView courseInfopopup_courseID, courseInfopopup_Course_ID;
    private TextView courseInfopopup_Title;
    private TextView courseInfopopup_Instructor, courseInfopopup_Instructor_box;
    private TextView courseInfopopup_section_number, courseInfopopup_section_number_box;
    private TextView courseInfopopup_num_credits, courseInfopopup_num_credits_box;
    private Button courseInfopopup_Back2Sched;

    public Schedule_Adapter(Context ct, String[] s1, ArrayList<Course> coursesPicked, RecyclerView recyclerView_) {
        context = ct;
        data1 = s1;
        courses = coursesPicked;
        recyclerView = recyclerView_;
        //data2 = s2;
        //colorVal = colors;
        //images = img;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.sched_array, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        holder.course.setText(data1[position]);
        holder.index = position;
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder{

        public View row_linearlayout;
        public View constraintLayout;
        int index;
        TextView course;
        Button delete, info;
        //ImageView calendarImage;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            course = itemView.findViewById(R.id.courseText);
            delete = itemView.findViewById(R.id.course_field);
            info = itemView.findViewById(R.id.details);
            row_linearlayout = (LinearLayout)itemView.findViewById(R.id.row_linearLayout);
            //constraintLayout = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout2);
            //calendarImage = itemView.findViewById(R.id.calendar_image_view);
            itemView.findViewById(R.id.details).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Define elements within popup
                    dialogBuilder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View CourseInfoPopupView = inflater.inflate(R.layout.popup_course, null);
                    courseInfopopup_Name = (TextView) CourseInfoPopupView.findViewById(R.id.Name);
                    courseInfopopup_nameBox = (TextView) CourseInfoPopupView.findViewById(R.id.nameBox);
                    courseInfopopup_Course_Description = (TextView) CourseInfoPopupView.findViewById(R.id.Course_Description);
                    courseInfopopup_courseDescriptionBox = (TextView) CourseInfoPopupView.findViewById(R.id.courseDescription_box);
                    courseInfopopup_courseCode_box = (TextView) CourseInfoPopupView.findViewById(R.id.courseCode_box);
                    courseInfopopup_CourseCode = (TextView) CourseInfoPopupView.findViewById(R.id.Course_Code);
                    courseInfopopup_courseID = (TextView) CourseInfoPopupView.findViewById(R.id.courseID);
                    courseInfopopup_Course_ID = (TextView) CourseInfoPopupView.findViewById(R.id.Course_ID);
                    courseInfopopup_Title = (TextView) CourseInfoPopupView.findViewById(R.id.Title);
                    courseInfopopup_Instructor = (TextView) CourseInfoPopupView.findViewById(R.id.course_Instructor);
                    courseInfopopup_Instructor_box = (TextView)  CourseInfoPopupView.findViewById(R.id.course_Instructor_box);
                    courseInfopopup_section_number = (TextView)  CourseInfoPopupView.findViewById(R.id.course_section_number);
                    courseInfopopup_section_number_box = (TextView)  CourseInfoPopupView.findViewById(R.id.course_section_number_box);
                    courseInfopopup_num_credits = (TextView)  CourseInfoPopupView.findViewById(R.id.num_credits);
                    courseInfopopup_num_credits_box = (TextView)  CourseInfoPopupView.findViewById(R.id.num_credits_box);

                    courseInfopopup_Back2Sched = (Button) CourseInfoPopupView.findViewById(R.id.Back2Sched);


                    courseInfopopup_nameBox.setText(courses.get(index).courseInfo.get("name"));
                    courseInfopopup_courseDescriptionBox.setText(courses.get(index).courseInfo.get("description"));
                    courseInfopopup_courseID.setText(courses.get(index).courseInfo.get("courseId"));
                    courseInfopopup_courseCode_box.setText(courses.get(index).courseInfo.get("code"));
                    courseInfopopup_Instructor_box.setText(Objects.requireNonNull(courses.get(index).classSection.get("Instructors")).replace("[", "").replace("]",""));
                    courseInfopopup_section_number_box.setText(courses.get(index).classSection.get("classNumber"));
                    courseInfopopup_num_credits_box.setText(courses.get(index).classSection.get("credits"));



                    //Create popup
                    dialogBuilder.setView(CourseInfoPopupView);
                    dialog = dialogBuilder.create();
                    dialog.show();



                    courseInfopopup_Back2Sched.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }
            });

            itemView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //courses.get(index)
                    ViewSchedule schedule = new ViewSchedule();
                    schedule.deleteCourseFromRecycler(index);
                }
            });
        }
    }
}
