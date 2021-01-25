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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUpdater extends Context {

    public void getUFCourses() {
        // Instantiate the RequestQueue.
        String url ="https://one.ufl.edu/apix/soc/schedule/?category=RES&term=2211";
        RequestQueue queue = Volley.newRequestQueue(this);

        // prepare the Request
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response
                        System.out.println("Response JSON" + response.toString());
                        try {
                            for(int c = 0; c < response.getJSONObject(0).getJSONArray("COURSES").length(); c++){
                                String name = response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).get("name").toString();

                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                mDatabase.child("Courses").child(name).child("code").setValue(response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).get("code").toString());
                                mDatabase.child("Courses").child(name).child("courseId").setValue(response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).get("courseId").toString());
                                mDatabase.child("Courses").child(name).child("name").setValue(name);
                                mDatabase.child("Courses").child(name).child("termInd").setValue(response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).get("termInd").toString());
                                mDatabase.child("Courses").child(name).child("description").setValue(response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).get("description").toString());
                                mDatabase.child("Courses").child(name).child("prerequisites").setValue(response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).get("prerequisites").toString());

                                //Get Sections
                                JSONArray sections = response.getJSONObject(0).getJSONArray("COURSES").getJSONObject(c).getJSONArray("sections");

                                for(int j = 0; j < sections.length(); j++){
                                    JSONObject section = sections.getJSONObject(j);
                                    mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("number").setValue(section.get("number").toString());
                                    mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("classNumber").setValue(section.get("classNumber").toString());
                                    mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("credits").setValue(section.get("credits").toString());
                                    mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("deptName").setValue(section.get("deptName").toString());

                                    //Get Instructor for the section
                                    JSONArray instructors = sections.getJSONObject(j).getJSONArray("instructors");
                                    for(int k = 0; k < instructors.length(); k++)
                                        mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("instructors").child(instructors.getJSONObject(k).get("name").toString()).setValue(instructors.getJSONObject(k).get("name"));

                                    //Get meeting times for the section
                                    JSONArray meetTimes = sections.getJSONObject(j).getJSONArray("meetTimes");
                                    for(int i = 0; i < meetTimes.length(); i++){
                                        mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("meetTimes").child(meetTimes.getJSONObject(i).get("meetNo").toString()).child("meetNo").setValue(meetTimes.getJSONObject(i).get("meetNo").toString());

                                        //Meet days
                                        mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("meetTimes").child(meetTimes.getJSONObject(i).get("meetNo").toString()).child("meetDays").setValue(meetTimes.getJSONObject(i).get("meetDays").toString());
                                        mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("meetTimes").child(meetTimes.getJSONObject(i).get("meetNo").toString()).child("meetTimeBegin").setValue(meetTimes.getJSONObject(i).get("meetTimeBegin").toString());
                                        mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("meetTimes").child(meetTimes.getJSONObject(i).get("meetNo").toString()).child("meetTimeEnd").setValue(meetTimes.getJSONObject(i).get("meetTimeEnd").toString());
                                        mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("meetTimes").child(meetTimes.getJSONObject(i).get("meetNo").toString()).child("meetPeriodBegin").setValue(meetTimes.getJSONObject(i).get("meetPeriodBegin").toString());
                                        mDatabase.child("Courses").child(name).child("sections").child(section.get("number").toString()).child("meetTimes").child(meetTimes.getJSONObject(i).get("meetNo").toString()).child("meetPeriodEnd").setValue(meetTimes.getJSONObject(i).get("meetPeriodEnd").toString());
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Error.Response" + error.toString());
                    }
                }
        );

        // Access the RequestQueue through your singleton class.
        queue.add(getRequest);
    }

    public void getUFCourse(String courseName) {
        DatabaseReference course = FirebaseDatabase.getInstance().getReference().child("Courses").child(courseName);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Course courseObj = new Course();
                courseObj.updateCourseInfo((HashMap<String, String>) dataSnapshot.getValue());

                // Get Post object and use the values to update the UI
                HashMap<String, String> courseInfo = (HashMap<String, String>) dataSnapshot.getValue();
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getValue().getClass().equals(courseInfo.getClass())){
                        //This is the sections
                        System.out.println("Child Super: " + child.getValue().getClass() + " " + child.getValue());
                        courseObj.setSections(child.getValue().toString());
                    }
                }

                for (Map.Entry<String, String> set : courseObj.getCourseInfo().entrySet()) {
                    System.out.println(set.getKey() + " = " + set.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
            }
        };
        course.addValueEventListener(postListener);
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
        return 0;
    }

    @Override
    public int checkCallingPermission(@NonNull String permission) {
        return 0;
    }

    @Override
    public int checkCallingOrSelfPermission(@NonNull String permission) {
        return 0;
    }

    @Override
    public int checkSelfPermission(@NonNull String permission) {
        return 0;
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
        return 0;
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
        return 0;
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
