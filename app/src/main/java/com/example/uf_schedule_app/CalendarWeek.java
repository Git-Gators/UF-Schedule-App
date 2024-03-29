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


public class CalendarWeek extends MainActivity {
    RecyclerView recyclerView, recyclerView2;
    Map<String, ArrayList<CourseEvent>> courseSections = loadCourseEvents(coursesPicked);
    ArrayList<String> Online;
    int numPeriods = 16;
    int[] colors = {
            R.drawable.border2,
            R.drawable.border3,
            R.drawable.border4,
            R.drawable.border5,
            R.drawable.border6,
            R.drawable.border7,
            R.drawable.border8,
            R.drawable.border9,
            R.drawable.border10,
            R.drawable.border11
    };
    String periods[], daysOfWeek[], abbr[], period_abbr[], week[][];
    int images[] = {R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24, R.drawable.ic_baseline_calendar_view_day_24,
            R.drawable.ic_baseline_calendar_view_day_24};

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_week);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_calendar);
        BottomNavigationView dayWeekNav = findViewById(R.id.week_day);
        dayWeekNav.setOnNavigationItemSelectedListener(dayWeekListener);
        dayWeekNav.setSelectedItemId(R.id.week);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);
        courseSections = loadCourseEvents(coursesPicked);
/**
 *     <color name="purple_200">#FFBB86FC</color>
 *     <color name="purple_500">#FF6200EE</color>
 *     <color name="purple_700">#FF3700B3</color>
 *     <color name="teal_200">#FF03DAC5</color>
 *     <color name="teal_700">#FF018786</color>
 *     <color name="black">#FF000000</color>
 *     <color name="white">#FFFFFFFF</color>
 *     <color name="gray">#373636</color>
 *     <color name="light_gray">#E4FCFF</color>
 *     <color name="orange">#FF5722</color>
 */


        weekUpdate(courseSections);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener dayWeekListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = 0;
                    Intent in;
                    Bundle b = new Bundle();
                    switch (item.getItemId()) {
                        case R.id.day:
                            id = R.id.day;
                            in = new Intent(getBaseContext(), CalendarView.class);
                            in.putExtra("coursesPicked", coursesPicked);
                            in.putExtras(b);
                            startActivity(in);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                            break;
                        case R.id.week:
                            id = R.id.week;
                            break;
                    }
                    System.out.println(id);
                    return true;
                }
            };
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = 0;
                    Intent in;
                    Bundle b = new Bundle();
                    switch (item.getItemId()) {
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

    public Map<String, ArrayList<CourseEvent>> loadCourseEvents(ArrayList<Course> coursesPicked) {
        Map<String, ArrayList<CourseEvent>> courseTimes = new HashMap<>();

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

        for (int i = 0; i < coursesPicked.size(); i++) {
            String courseCode = coursesPicked.get(i).courseInfo.get("code");

            //If a course is an online course, it won't have meetDays.  So if the meetDays variable
            //is empty, then the course must be online.
            if (coursesPicked.get(i).classSection.get("meetDays").equals("")) {
                //Online courses are counted as taking place every day, at a time section labeled
                //online, so we add the course to every day's list under the time online
                String time = "Online";
                CourseEvent event = new CourseEvent(time, courseCode, i);
                courseTimes.get("Monday").add(event);
                courseTimes.get("Tuesday").add(event);
                courseTimes.get("Wednesday").add(event);
                courseTimes.get("Thursday").add(event);
                courseTimes.get("Friday").add(event);
                courseTimes.get("Saturday").add(event);
            } else {
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
                while (daysParser.indexOf(']') != -1 && timesParser.indexOf(']') != -1) {
                    endDay = daysParser.indexOf(']');
                    endTime = timesParser.indexOf(']');

                    //This creates a pair of strings based around each section/discussion section/
                    //lab section
                    String daySection = daysParser.substring(startDay, endDay);

                    //Re-formating the timesSection makes storing and using the data a little easier
                    String timesSection = timesParser.substring(startTime, endTime).replace("[", "").replace("]", "").replace(" ", "");

                    //Now that we have the days each section/discussion/lab takes place, we check
                    //each day of the week, and add the times to our event list for each day
                    if (daySection.contains("M")) {
                        CourseEvent event = new CourseEvent(timesSection, courseCode, i);
                        courseTimes.get("Monday").add(event);
                    }
                    if (daySection.contains("T")) {
                        CourseEvent event = new CourseEvent(timesSection, courseCode, i);
                        courseTimes.get("Tuesday").add(event);
                    }
                    if (daySection.contains("W")) {
                        CourseEvent event = new CourseEvent(timesSection, courseCode, i);
                        courseTimes.get("Wednesday").add(event);
                    }
                    if (daySection.contains("R")) {
                        CourseEvent event = new CourseEvent(timesSection, courseCode, i);
                        courseTimes.get("Thursday").add(event);
                    }
                    if (daySection.contains("F")) {
                        CourseEvent event = new CourseEvent(timesSection, courseCode, i);
                        courseTimes.get("Friday").add(event);
                    }
                    if (daySection.contains("S")) {
                        CourseEvent event = new CourseEvent(timesSection, courseCode, i);
                        courseTimes.get("Saturday").add(event);
                    }

                    //If we are at the end of the days string or times string, we're done for this
                    //course
                    if (endDay == daysParser.length() || endTime == timesParser.length()) {
                        break;
                    }
                    //Otherwise, go to the next section/discussion/lab
                    else {
                        daysParser = daysParser.substring(endDay + 1);
                        timesParser = timesParser.substring(endTime + 1);
                    }
                }
            }

            //courseTimes[coursesPicked.get("")]
        }
        return courseTimes;
    }



    //@Override
    private void weekUpdate(Map<String, ArrayList<CourseEvent>> courseTimes) {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);

        periods = getResources().getStringArray(R.array.periods);
        period_abbr = getResources().getStringArray(R.array.periods_filter);
        daysOfWeek = getResources().getStringArray(R.array.daysOfWeek);
        abbr = getResources().getStringArray(R.array.dayInitials);
        week = new String[6][numPeriods];
        Online = new ArrayList<String>(1);
        ArrayList<String> online_sched = new ArrayList<String>(1);
        ArrayList<Integer> color_online = new ArrayList<Integer>(1);

        int[][] color = new int[6][numPeriods];

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < numPeriods; x++) {
                color[y][x] = R.drawable.border;
            }
        }

        for (int j = 0; j < 6; j++) {
            week[j][0] = abbr[j];
            for (int i = 1; i < numPeriods; i++) {
                week[j][i] = "";
            }
        }
        int counter = 0;
        for(String day : daysOfWeek) {
            ArrayList<CourseEvent> events = courseTimes.get(day);
            for (int i = 0; i < events.size(); i++) {
                boolean isNow = false;
                //j is numPeriods - 1 because the first time slot and final time slot don't contain dashes
                for (int j = 1; j < numPeriods - 1; j++) {
                    String times = events.get(i).time;
                    String beginningTime = "";
                    String endTime = "";
                    if (times.indexOf('-') != -1) {
                        beginningTime = times.substring(0, times.indexOf('-'));
                        endTime = times.substring(times.indexOf('-') + 1);
                    }


                    String periodStart = periods[j].substring(0, periods[j].indexOf('-'));
                    String periodEnd = periods[j].substring(periods[j].indexOf('-') + 1);

                    if (beginningTime.equals(periodStart) && endTime.equals(periodEnd)) {
                        week[counter][j] = courseTimes.get(day).get(i).courseCode;
                        color[counter][j] = colors[courseTimes.get(day).get(i).position % 10];
                    } else if (beginningTime.equals(periodStart) && !endTime.equals(periodEnd)) {
                        isNow = true;
                        week[counter][j] = courseTimes.get(day).get(i).courseCode;
                        color[counter][j] = colors[courseTimes.get(day).get(i).position % 10];
                    } else if (isNow && !endTime.equals(periodEnd)) {
                        color[counter][j] = colors[courseTimes.get(day).get(i).position % 10];
                    } else if (isNow && endTime.equals(periodEnd)) {
                        isNow = false;
                        color[counter][j] = colors[courseTimes.get(day).get(i).position % 10];
                    }
                }
                if (events.get(i).time.equals("Online") && day.equals("Monday")) {
                    //week[0][numPeriods - 1] = courseTimes.get(day).get(i).courseCode;
                    Online.add(courseTimes.get(day).get(i).courseCode);
                    color_online.add(colors[courseTimes.get(day).get(i).position % 10]);
                    online_sched.add("Online");
                }
            }
            counter++;
        }
        String[] Online_array = new String[Online.size()];
        Online_array = Online.toArray(Online_array);
        String[] sched_array = new String[online_sched.size()];
        sched_array = online_sched.toArray(sched_array);
        Integer[] color_online_array = new Integer[color_online.size()];
        color_online_array = color_online.toArray(color_online_array);
        int[] color_o = new int[color_online_array.length];
        for (int i = 0; i < color_online_array.length; i++) {
            color_o[i] = color_online_array[i].intValue();
        }
        Week_Adapter weekAdapter = new Week_Adapter(this, period_abbr, week, color);
        Calendar_Adapter calendarAdapter = new Calendar_Adapter(this, sched_array, Online_array, color_o);
        recyclerView.setAdapter(weekAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(calendarAdapter);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));

        System.out.println("\nBruh");
    }
}
