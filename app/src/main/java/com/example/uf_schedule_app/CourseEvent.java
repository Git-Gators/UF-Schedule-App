package com.example.uf_schedule_app;

public class CourseEvent {
    String time;
    String courseCode;
    int position;

    public CourseEvent(String time, String courseCode, int position)
    {
        this.time = time;
        this.courseCode = courseCode;
        this.position = position;
    }
}
