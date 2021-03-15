package com.example.uf_schedule_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.PopupWindow;
import android.app.AlertDialog;

import android.content.Intent;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CalendarView extends MainActivity {
    RecyclerView recyclerView;
    Map<String, ArrayList<CourseEvent>> courseTimes = new HashMap<>();

    int numPeriods = 16;
    String periods[], courseViews[], daysOfWeek[];
    int images[] = {R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24};

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_calendar);
        BottomNavigationView dayNav = findViewById(R.id.day_of_week);
        dayNav.setOnNavigationItemSelectedListener(dayListener);
        dayNav.setSelectedItemId(R.id.monday);
        recyclerView = findViewById(R.id.recyclerView);

        List<String> dayInitials = Arrays.asList(getResources().getStringArray(R.array.dayInitials));



        //If you're reading this, it was every bit as painful to write as it is to read

        //We're sorting course sections by day.

        //For the purposes of this code, a CourseEvent is the time a course takes place and its
        //course code.

        //What I'm trying to do here is get a list of all the course events that take place on
        //each day (Monday, Tuesday, Wednesday, Thursday, Friday), and store them in a map whose
        //key is the day, and whose value is a list of all the events taking place that day

        //Start by initializing arrays for every day of the week
        courseTimes.put("Monday", new ArrayList<>());
        courseTimes.put("Tuesday", new ArrayList<>());
        courseTimes.put("Wednesday", new ArrayList<>());
        courseTimes.put("Thursday", new ArrayList<>());
        courseTimes.put("Friday", new ArrayList<>());
        courseTimes.put("Saturday", new ArrayList<>());

        for (int i = 0; i < coursesPicked.size(); i++)
        {
            String courseCode = coursesPicked.get(i).courseInfo.get("code");

            //If a course is an online course, it won't have meetDays.  So if the meetDays variable
            //is empty, then the course must be online.
            if (coursesPicked.get(i).classSection.get("meetDays").equals(""))
            {
                //Online courses are counted as taking place every day, at a time section labeled
                //online, so we add the course to every day's list under the time online
                String time = "Online";
                CourseEvent event = new CourseEvent(time, courseCode);
                courseTimes.get("Monday").add(event);
                courseTimes.get("Tuesday").add(event);
                courseTimes.get("Wednesday").add(event);
                courseTimes.get("Thursday").add(event);
                courseTimes.get("Friday").add(event);
                courseTimes.get("Saturday").add(event);
            }
            else
            {
                //So if a course isn't online, we want to get the times and days each course takes
                //place.  Courses can have a regular course meeting, a lab, and a discussion, which
                //is something we're going to have to parse manually.
                String daysParser = coursesPicked.get(i).classSection.get("meetDays");
                String timesParser = coursesPicked.get(i).classSection.get("meetTime");

                //startDay, startTime, endDay, and endTime are, respectively, the indices corresponding
                //to the start of the day and time substrings and the end of the day and time
                //substrings we will be taking
                int startDay = 0;
                int startTime = 0;
                int endDay;
                int endTime;

                //The structure of the day string in the courses object is as follows:
                //[day1,day2, ... ,dayn][day1,day2, ... ,dayn]...
                //with each set of brackets corresponding to a new class/lab/discussion section

                //The structure of the time string in the courses object is as follows:
                //[startTime-endTime][startTime-endTime]...
                //with each starting and ending time having a corresponding day

                //So, to find each group of days and times, we search for the closing brackets
                while (daysParser.indexOf(']') != -1 && timesParser.indexOf(']') != -1)
                {
                    endDay = daysParser.indexOf(']');
                    endTime = timesParser.indexOf(']');

                    //This creates a pair of strings based around each section/discussion section/
                    //lab section
                    String daySection = daysParser.substring(startDay, endDay);

                    //Re-formating the timesSection makes storing and using the data a little easier
                    String timesSection = timesParser.substring(startTime, endTime).replace("[", "").replace("]","").replace(" ","");

                    //Now that we have the days each section/discussion/lab takes place, we check
                    //each day of the week, and add the times to our event list for each day
                    if (daySection.contains("M"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Monday").add(event);
                    }
                    if (daySection.contains("T"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Tuesday").add(event);
                    }
                    if (daySection.contains("W"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Wednesday").add(event);
                    }
                    if (daySection.contains("R"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Thursday").add(event);
                    }
                    if (daySection.contains("F"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Friday").add(event);
                    }
                    if (daySection.contains("S"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Saturday").add(event);
                    }

                    //If we are at the end of the days string or times string, we're done for this
                    //course
                    if (endDay == daysParser.length() || endTime == timesParser.length())
                    {
                        break;
                    }
                    //Otherwise, go to the next section/discussion/lab
                    else
                    {
                        daysParser = daysParser.substring(endDay + 1);
                        timesParser = timesParser.substring(endTime + 1);
                    }
                }
            }

            //courseTimes[coursesPicked.get("")]
        }

        periods = getResources().getStringArray(R.array.periods);
        daysOfWeek = getResources().getStringArray(R.array.daysOfWeek);
        courseViews = new String[numPeriods];

        courseViews[0] = daysOfWeek[0];

        for (int i = 1; i < numPeriods; i++)
        {
            courseViews[i] = "";
        }

        ArrayList<CourseEvent> events = courseTimes.get("Monday");
        for (int i = 0; i < events.size(); i++)
        {
            for (int j = 1; j < numPeriods - 1; j++)
            {
                String times = events.get(i).time;
                String beginningTime = "";
                String endTime = "";
                if (times.indexOf('-') != -1)
                {
                    beginningTime = times.substring(0, times.indexOf('-'));
                    endTime = times.substring(times.indexOf('-') + 1);
                }


                String periodStart = periods[j].substring(0, periods[j].indexOf('-'));
                String periodEnd = periods[j].substring(periods[j].indexOf('-') + 1);

                if (beginningTime.equals(periodStart) || endTime.equals(periodEnd))
                {
                    courseViews[j] = courseTimes.get("Monday").get(i).courseCode;
                }
            }
            if (events.get(i).time.equals("Online"))
            {
                courseViews[numPeriods - 1] = courseTimes.get("Monday").get(i).courseCode;
            }
        }

        Calendar_Adapter calendarAdapter = new Calendar_Adapter(this, periods, courseViews);
        recyclerView.setAdapter(calendarAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = 0;
                    Intent in;
                    Bundle b = new Bundle();
                    switch(item.getItemId()){
                        case R.id.nav_home:
                            in = new Intent(getBaseContext(), MainActivity.class);
                            in.putExtra("coursesPicked", coursesPicked);
                            in.putExtras(b);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                            break;
                        case R.id.nav_schedule:
                            id = R.id.nav_schedule;
                            in = new Intent(getBaseContext(), ViewSchedule.class);
                            in.putExtra("coursesPicked", coursesPicked);
                            in.putExtras(b);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                            break;
                        case R.id.nav_calendar:
                            id = R.id.nav_calendar;
                            break;
                    }
                    System.out.println(id);
                    return true;
                }
            };
    //@Override
    private void dayUpdate(String day) {
        recyclerView = findViewById(R.id.recyclerView);

        List<String> dayInitials = Arrays.asList(getResources().getStringArray(R.array.dayInitials));



        //If you're reading this, it was every bit as painful to write as it is to read

        //We're sorting course sections by day.

        //For the purposes of this code, a CourseEvent is the time a course takes place and its
        //course code.

        //What I'm trying to do here is get a list of all the course events that take place on
        //each day (Monday, Tuesday, Wednesday, Thursday, Friday), and store them in a map whose
        //key is the day, and whose value is a list of all the events taking place that day

        //Start by initializing arrays for every day of the week
        courseTimes.put("Monday", new ArrayList<>());
        courseTimes.put("Tuesday", new ArrayList<>());
        courseTimes.put("Wednesday", new ArrayList<>());
        courseTimes.put("Thursday", new ArrayList<>());
        courseTimes.put("Friday", new ArrayList<>());
        courseTimes.put("Saturday", new ArrayList<>());

        for (int i = 0; i < coursesPicked.size(); i++)
        {
            String courseCode = coursesPicked.get(i).courseInfo.get("code");

            //If a course is an online course, it won't have meetDays.  So if the meetDays variable
            //is empty, then the course must be online.
            if (coursesPicked.get(i).classSection.get("meetDays").equals(""))
            {
                //Online courses are counted as taking place every day, at a time section labeled
                //online, so we add the course to every day's list under the time online
                String time = "Online";
                CourseEvent event = new CourseEvent(time, courseCode);
                courseTimes.get("Monday").add(event);
                courseTimes.get("Tuesday").add(event);
                courseTimes.get("Wednesday").add(event);
                courseTimes.get("Thursday").add(event);
                courseTimes.get("Friday").add(event);
                courseTimes.get("Saturday").add(event);
            }
            else
            {
                //So if a course isn't online, we want to get the times and days each course takes
                //place.  Courses can have a regular course meeting, a lab, and a discussion, which
                //is something we're going to have to parse manually.
                String daysParser = coursesPicked.get(i).classSection.get("meetDays");
                String timesParser = coursesPicked.get(i).classSection.get("meetTime");

                //startDay, startTime, endDay, and endTime are, respectively, the indices corresponding
                //to the start of the day and time substrings and the end of the day and time
                //substrings we will be taking
                int startDay = 0;
                int startTime = 0;
                int endDay;
                int endTime;

                //The structure of the day string in the courses object is as follows:
                //[day1,day2, ... ,dayn][day1,day2, ... ,dayn]...
                //with each set of brackets corresponding to a new class/lab/discussion section

                //The structure of the time string in the courses object is as follows:
                //[startTime-endTime][startTime-endTime]...
                //with each starting and ending time having a corresponding day

                //So, to find each group of days and times, we search for the closing brackets
                while (daysParser.indexOf(']') != -1 && timesParser.indexOf(']') != -1)
                {
                    endDay = daysParser.indexOf(']');
                    endTime = timesParser.indexOf(']');

                    //This creates a pair of strings based around each section/discussion section/
                    //lab section
                    String daySection = daysParser.substring(startDay, endDay);

                    //Re-formating the timesSection makes storing and using the data a little easier
                    String timesSection = timesParser.substring(startTime, endTime).replace("[", "").replace("]","").replace(" ","");

                    //Now that we have the days each section/discussion/lab takes place, we check
                    //each day of the week, and add the times to our event list for each day
                    if (daySection.contains("M"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Monday").add(event);
                    }
                    if (daySection.contains("T"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Tuesday").add(event);
                    }
                    if (daySection.contains("W"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Wednesday").add(event);
                    }
                    if (daySection.contains("R"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Thursday").add(event);
                    }
                    if (daySection.contains("F"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Friday").add(event);
                    }
                    if (daySection.contains("S"))
                    {
                        CourseEvent event = new CourseEvent(timesSection, courseCode);
                        courseTimes.get("Saturday").add(event);
                    }

                    //If we are at the end of the days string or times string, we're done for this
                    //course
                    if (endDay == daysParser.length() || endTime == timesParser.length())
                    {
                        break;
                    }
                    //Otherwise, go to the next section/discussion/lab
                    else
                    {
                        daysParser = daysParser.substring(endDay + 1);
                        timesParser = timesParser.substring(endTime + 1);
                    }
                }
            }

            //courseTimes[coursesPicked.get("")]
        }

        periods = getResources().getStringArray(R.array.periods);
        daysOfWeek = getResources().getStringArray(R.array.daysOfWeek);
        courseViews = new String[numPeriods];

        courseViews[0] = daysOfWeek[0];

        for (int i = 1; i < numPeriods; i++)
        {
            courseViews[i] = "";
        }

        ArrayList<CourseEvent> events = courseTimes.get(day);
        for (int i = 0; i < events.size(); i++)
        {
            for (int j = 1; j < numPeriods - 1; j++)
            {
                String times = events.get(i).time;
                String beginningTime = "";
                String endTime = "";
                if (times.indexOf('-') != -1)
                {
                    beginningTime = times.substring(0, times.indexOf('-'));
                    endTime = times.substring(times.indexOf('-') + 1);
                }


                String periodStart = periods[j].substring(0, periods[j].indexOf('-'));
                String periodEnd = periods[j].substring(periods[j].indexOf('-') + 1);

                if (beginningTime.equals(periodStart) || endTime.equals(periodEnd))
                {
                    courseViews[j] = courseTimes.get(day).get(i).courseCode;
                }
            }
            if (events.get(i).time.equals("Online"))
            {
                courseViews[numPeriods - 1] = courseTimes.get(day).get(i).courseCode;
            }
        }
        courseViews[0] = day;
        Calendar_Adapter calendarAdapter = new Calendar_Adapter(this, periods, courseViews);
        recyclerView.setAdapter(calendarAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        System.out.println("\nDay of Week Changed!");
    }
    private BottomNavigationView.OnNavigationItemSelectedListener dayListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = 0;
                    switch(item.getItemId()){
                        case R.id.monday:
                            id = R.id.monday;
                            dayUpdate("Monday");
                            break;
                        case R.id.tuesday:
                            id = R.id.tuesday;
                            dayUpdate("Tuesday");
                            break;
                        case R.id.wednesday:
                            id = R.id.wednesday;
                            dayUpdate("Wednesday");
                            break;
                        case R.id.thursday:
                            id = R.id.thursday;
                            dayUpdate("Thursday");
                            break;
                        case R.id.friday:
                            id = R.id.friday;
                            dayUpdate("Friday");
                            break;
                    }
                    System.out.println(id);
                    return true;
                }
            };
}
