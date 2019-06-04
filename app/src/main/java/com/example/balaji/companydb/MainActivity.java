package com.example.balaji.companydb;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

//Initializing the Variables

    EditText txtMsg;
    Button btnSearch;
    SQLiteDatabase db;
    final Context context = this;
    final String DATABASE_NAME = "/Company";
    int intData;
    String fullDBName;
    String SEP = "      , ";
    Button btnUpdate;


//Handler Class

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_user_details);
            String message= (String) msg.obj;
            TextView txtUser = (TextView) dialog.findViewById(R.id.txtUser);
            txtUser.setText("\n"+(String)msg.obj);
            dialog.show();
            Button dialogButtonOK = (Button) dialog.findViewById(R.id.btnOk);
            dialogButtonOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        }
    };//Handler



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Asking permission to access the hardware

        askExplicitPermissionToUseSDCard();

        //Plumbing UI
       txtMsg = (EditText) findViewById (R.id.txtMsg);
       btnSearch = (Button) findViewById (R.id.btnSearch);
       btnUpdate = (Button) findViewById(R.id.btnUpdate);



        //Searching
       btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Conversion of string to Integer
                String strData = txtMsg.getText().toString();
                intData = Integer.parseInt(strData);
                //Passing the Integer value(User ID) to the thread
                fullDBName = utilGetFileFullName();
                parallelDatabaseThread(intData).start();

            }
        });

        //Update
       btnUpdate.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               // Starting an Intent

               Intent intentA1A2 = new Intent(MainActivity.this,Main2Activity.class);
               intentA1A2.putExtra("arg1",intData);
               startActivity(intentA1A2);


           }
       });

    }//OnCreate

    private void askExplicitPermissionToUseSDCard() {
        // Asking permission
        String[] neededPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(MainActivity.this,
                neededPermissions,
                123);
    }//Explicit

    private String utilGetFileFullName(){

        // Accessing the file by using the absolute path method.

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Download";
        String fileFullName = externalPath + DATABASE_NAME;  // assuming external location


        return fileFullName;
    }//utilGetFileFullName

    private Thread parallelDatabaseThread(final int intData){

        //Thread creation

        Thread databaseThread = new Thread() {
            @Override
            public void run() {
                super.run();

                action00GetTableEmployee(intData);
            }
        };
        return databaseThread;
    }//makeDatabaseThread



    private void utilShowViaHandler(String text){

        //send message from background thread to handler attached to main thread
        Message msg = handler.obtainMessage();
        msg.obj = text;
        handler.sendMessage(msg);
    }



    private void action00GetTableEmployee(int intData){


        db = SQLiteDatabase.openDatabase(fullDBName, null,SQLiteDatabase.CREATE_IF_NECESSARY);

        db.beginTransaction();
        try {

            String sqlSelect = "select * from employee where emp_id =?" ;
            String[] params = {" "+intData};
            Cursor cursor = db.rawQuery(sqlSelect,params);
            cursor.moveToFirst();


                int emp_id = cursor.getInt(0);
                String emp_name = cursor.getString(1);
                double emp_salary = cursor.getDouble(cursor.getColumnIndex("emp_salary"));
                String emp_sex = cursor.getString(cursor.getColumnIndex("emp_sex"));
                String emp_dno = cursor.getString(cursor.getColumnIndex("emp_dno"));
                // do something with the employee record here...
                utilShowViaHandler(emp_id+"  "+SEP   + emp_name + SEP +"  " +emp_salary  +SEP+"  "+
                        emp_sex + SEP+"  "+ emp_dno);

        } catch (SQLiteException e) {
            utilShowViaHandler("Error " + e.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }
}//Main activity
