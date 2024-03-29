package com.example.uf_schedule_app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class DatabaseUpdater extends Context {
    public ArrayList<Course> retrCourses = new ArrayList<>();
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private void updateDatabase(String url, String semester) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // prepare the Request
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(int c = 0; c < response.getJSONObject(0).getJSONArray("COURSES").length(); c++){
                                Course courseObj = new Course();

                                JSONObject courseDir = response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c);

                                courseObj.courseInfo.put("code", (String) courseDir.get("code"));
                                //String code = (String) courseDir.get("code");

                                courseObj.courseInfo.put("courseId", (String) courseDir.get("courseId"));
                                //String courseID = (String) courseDir.get("courseId");

                                String name = response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).get("name").toString();
                                courseObj.courseInfo.put("name", name);

                                courseObj.courseInfo.put("termInd", courseDir.get("termInd").toString());

                                courseObj.courseInfo.put("description", courseDir.get("description").toString());
                                //String description = courseDir.get("description").toString();

                                String prereq = courseDir.get("prerequisites").toString();
                                prereq = prereq.replace("Prereq: ", "");
                                courseObj.courseInfo.put("prerequisites", prereq);

                                //Get Sections
                                JSONArray sections = courseDir.getJSONArray("sections");

                                for(int j = 0; j < sections.length(); j++){
                                    JSONObject section = sections.getJSONObject(j);
                                    HashMap<String, String> sectionMap = new HashMap<String, String>();

                                    sectionMap.put("number", section.get("number").toString());
                                    sectionMap.put("classNumber", section.get("classNumber").toString());
                                    sectionMap.put("credits", section.get("credits").toString());

                                    String deptName = section.get("deptName").toString().replace("/", "&");
                                    sectionMap.put("deptName", deptName);

                                    //Get Instructor for the section
                                    JSONArray instructors = sections.getJSONObject(j).getJSONArray("instructors");
                                    StringBuilder instructorStr = new StringBuilder();
                                    instructorStr.append("[");
                                    for(int k = 0; k < instructors.length(); k++)
                                        instructorStr.append(instructors.getJSONObject(k).get("name").toString()).append(",");

                                    if(!instructorStr.toString().equals("[]"))
                                        instructorStr.deleteCharAt(instructorStr.length()-1);
                                    instructorStr.append("]");
                                    sectionMap.put("Instructors", instructorStr.toString());

                                    //Get meeting times for the section
                                    JSONArray meetTimes = sections.getJSONObject(j).getJSONArray("meetTimes");
                                    StringBuilder meetDays = new StringBuilder();
                                    StringBuilder meetPeriod = new StringBuilder();
                                    StringBuilder meetTime = new StringBuilder();

                                    for(int i = 0; i < meetTimes.length(); i++){
                                        meetDays.append(meetTimes.getJSONObject(i).get("meetDays").toString());
                                        meetPeriod.append("[").append(meetTimes.getJSONObject(i).get("meetPeriodBegin").toString()).append("-").append(meetTimes.getJSONObject(i).get("meetPeriodEnd").toString()).append("]");
                                        meetTime.append("[").append(meetTimes.getJSONObject(i).get("meetTimeBegin").toString()).append("-").append(meetTimes.getJSONObject(i).get("meetTimeEnd").toString()).append("]");
                                    }
                                    sectionMap.put("meetDays", meetDays.toString());
                                    sectionMap.put("meetPeriod", meetPeriod.toString());
                                    sectionMap.put("meetTime", meetTime.toString());

                                    courseObj.classSection = sectionMap;
                                    mDatabase.child(semester).child(deptName).child(section.get("classNumber").toString()).setValue(courseObj);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Error.Response: " + error.toString());
                    }
                }
        );

        // Access the RequestQueue through your singleton class.
        getRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getRequest);
    }

    public void updateDB(Context context, String semesterName, int semesterCode) throws IOException {
        ArrayList<String> depCodes = new ArrayList<>();

        String string = "";
        InputStream is = context.getAssets().open("departments.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (true) {
            try {
                if ((string = reader.readLine()) == null) break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            depCodes.add(string);
        }
        is.close();



        try {
            for(int i = 0; i < depCodes.size(); i++){
                String url = "https://one.ufl.edu/apix/soc/schedule/?category=CWSP&term=" + semesterCode + "&dept=" + depCodes.get(i).replace("\"", "");
                updateDatabase(url, semesterName);
            }
        } catch (Exception E){
            System.out.println("Exception caught when updating DB: " + E.toString());
        }
    }

    public void getDepNames(ArrayList<String> deptNames, Spinner spinnerDept, Spinner spinnerCrse, Context context) throws IOException {
        deptNames.clear();
        deptNames.add("Choose a Department");

        String string = "";
        InputStream is = context.getAssets().open("depNames.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (true) {
            try {
                if ((string = reader.readLine()) == null) break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            deptNames.add(string);
        }
        is.close();

        spinnerDept.setEnabled(true);
        spinnerCrse.setEnabled(true);
    }

    public void getCourseNames(String deptName, ArrayList<String> coursesNames, ProgressBar spinner, Spinner spinnerCrse, ArrayList<Course> crses, String semester){
        spinner.setVisibility(View.VISIBLE);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(semester).child(deptName);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    coursesNames.add(Objects.requireNonNull(ds.getValue(Course.class).toString()));
                    crses.add(ds.getValue(Course.class));
                }
                spinner.setVisibility(View.INVISIBLE);
                spinnerCrse.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    public void deleteCourses(){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.removeValue();
    }

    public void setTextFields(EditText course1, EditText course2, EditText course3, EditText course4, String deptName, String courseName, ProgressBar pSpinner3, String semester) {
        pSpinner3.setVisibility(View.VISIBLE);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(semester).child(deptName);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if((ds.child("courseInfo").child("name").getValue()).toString().equals(courseName)){
                        Course course = ds.getValue(Course.class);

                        //Name of the course
                        String text = "Course Code: " + course.courseInfo.get("code");
                        course1.setText(text);
                        course1.setInputType(InputType.TYPE_NULL);

                        //courseID
                        text = "CourseID: " + course.courseInfo.get("courseId");
                        course2.setText(text);
                        course2.setInputType(InputType.TYPE_NULL);

                        //Instructors
                        text = "Instructors: " + course.classSection.get("Instructors").replace("[", "").replace("]", "");
                        course3.setText(text);
                        course3.setInputType(InputType.TYPE_NULL);

                        //Class Number
                        text = "Class Number: " + course.classSection.get("number");
                        course4.setText(text);
                        course4.setInputType(InputType.TYPE_NULL);
                    }
                }
                pSpinner3.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);
    }


    @Override
    public AssetManager getAssets() {
        return null;
    }

    @Override
    public Resources getResources() {
        return null;
    }

    @Override
    public PackageManager getPackageManager() {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    @Override
    public Looper getMainLooper() {
        return null;
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void setTheme(int resid) {

    }

    @Override
    public Resources.Theme getTheme() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }

    @Override
    public String getPackageResourcePath() {
        return null;
    }

    @Override
    public String getPackageCodePath() {
        return null;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return null;
    }

    @Override
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteSharedPreferences(String name) {
        return false;
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return null;
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean deleteFile(String name) {
        return false;
    }

    @Override
    public File getFileStreamPath(String name) {
        return null;
    }

    @Override
    public File getDataDir() {
        return null;
    }

    @Override
    public File getFilesDir() {
        return null;
    }

    @Override
    public File getNoBackupFilesDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalFilesDir(@Nullable String type) {
        return null;
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return new File[0];
    }

    @Override
    public File getObbDir() {
        return null;
    }

    @Override
    public File[] getObbDirs() {
        return new File[0];
    }

    @Override
    public File getCacheDir() {
        return null;
    }

    @Override
    public File getCodeCacheDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return null;
    }

    @Override
    public File[] getExternalCacheDirs() {
        return new File[0];
    }

    @Override
    public File[] getExternalMediaDirs() {
        return new File[0];
    }

    @Override
    public String[] fileList() {
        return new String[0];
    }

    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
        return null;
    }

    @Override
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteDatabase(String name) {
        return false;
    }

    @Override
    public File getDatabasePath(String name) {
        return null;
    }

    @Override
    public String[] databaseList() {
        return new String[0];
    }

    @Override
    public Drawable getWallpaper() {
        return null;
    }

    @Override
    public Drawable peekWallpaper() {
        return null;
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return 0;
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return 0;
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {

    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {

    }

    @Override
    public void clearWallpaper() throws IOException {

    }

    @Override
    public void startActivity(Intent intent) {

    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {

    }

    @Override
    public void startActivities(Intent[] intents) {

    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {

    }

    @Override
    public void sendBroadcast(Intent intent) {

    }

    @Override
    public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
        return null;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

    }

    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return null;
    }

    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return null;
    }

    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    @Override
    public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
        return false;
    }

    @Override
    public void unbindService(@NonNull ServiceConnection conn) {

    }

    @Override
    public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
        return false;
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        return null;
    }

    @Nullable
    @Override
    public String getSystemServiceName(@NonNull Class<?> serviceClass) {
        return null;
    }

    @Override
    public int checkPermission(@NonNull String permission, int pid, int uid) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingPermission(@NonNull String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingOrSelfPermission(@NonNull String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkSelfPermission(@NonNull String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {

    }

    @Override
    public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {

    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return null;
    }

    @Override
    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        return null;
    }

    @Override
    public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
        return null;
    }

    @Override
    public Context createDisplayContext(@NonNull Display display) {
        return null;
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        return null;
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        return false;
    }
}
