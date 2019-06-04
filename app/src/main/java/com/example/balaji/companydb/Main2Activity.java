package com.example.balaji.companydb;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity {

    //Initialize Data

    TextView txtUserId;
    Intent intentA1A2;
    final String DATABASE_NAME = "/Company";
    String fullDBName;
    SQLiteDatabase db;
    Button btnUpdate;
    Button btnBack;
    EditText txtName;
    EditText txtSalary;
    EditText txtDept;
    EditText txtSex;
    Button btnCancel;

    // Handler class

    Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            txtName.append("\n "+(String)msg.obj);
            txtSalary.append("\n "+(String)msg.obj);
            txtDept.append("\n "+(String)msg.obj);
            txtSex.append("\n "+(String)msg.obj);

        }


    };//handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //Plumbing UI
        txtUserId= (TextView) findViewById(R.id.txtUserId);
        txtName=(EditText) findViewById(R.id.txtName);
        txtSalary=(EditText) findViewById(R.id.txtSalary);
        txtDept=(EditText) findViewById(R.id.txtDept);
        txtSex=(EditText) findViewById(R.id.txtSex);

        btnUpdate= (Button) findViewById(R.id.btnUpdate);
        btnBack=(Button) findViewById(R.id.btnBack);
        btnCancel=(Button) findViewById(R.id.btnCancel);
        //Calling Intent to get the Data with a new activity

        intentA1A2 = getIntent();
        final int intData= intentA1A2.getIntExtra("arg1",0);
        txtUserId.setText("" +intData);
        fullDBName = utilGetFileFullName();

        // Sending the Values sent by Intent to the thread

        parallelDatabaseThread(intData).start();
        parallelupdation(intData).start();


        //Updating the values

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String strName = txtName.getText().toString();
                String strSalary = txtSalary.getText().toString();
                int intSalary = Integer.parseInt(strSalary);
                String strDept = txtDept.getText().toString();
                String strSex = txtSex.getText().toString();

                UpdateEmployee(intData,strName,intSalary,strDept,strSex);

            }
        });

        //Going Back to the MainActivity

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentA2A1 = new Intent(Main2Activity.this,MainActivity.class);
                startActivity(intentA2A1);

            }
        });

        // Clearing the field's on the screen

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtUserId.setText("");
                txtName.setText("");
                txtSalary.setText("");
                txtDept.setText("");
                txtSex.setText("");
            }
        });


    }//onCreate



   private String utilGetFileFullName(){

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Download";

        String fileFullName = externalPath + DATABASE_NAME;  // assuming external location


        return fileFullName;
    }//utilGetFileFullName

    private Thread parallelDatabaseThread(final int intData) {
        Thread databaseThread = new Thread() {
            @Override
            public void run() {
                super.run();

                GetTableEmployee(intData);

                }
            };


        return databaseThread;
        }//makeupdateThread

    private Thread parallelupdation(final   int intData){
        Thread databaseThread = new Thread(){
            @Override
            public void run() {
                super.run();


                GetTableEmployee(intData);

            }
        };


        return databaseThread;

    }
    private void utilShowViaHandler(String text){

        //send message from background thread to handler attached to main thread
        Message msg = handler.obtainMessage();
        msg.obj = text;
        handler.sendMessage(msg);


    }





    private void GetTableEmployee(int intData){


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

            txtName.setText(emp_name);
            txtDept.setText(emp_dno);
            txtSex.setText(emp_sex);
            txtSalary.setText(""+emp_salary);


        } catch (SQLiteException e) {
            utilShowViaHandler("Error " + e.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

    private void UpdateEmployee(final int intData, String strName,int intSalary,String strDept,String strSex) {

        if (!db.isOpen()) {
            db = SQLiteDatabase.openDatabase(fullDBName, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        }

        db.beginTransaction();

        try {
            String[] whereArgs = {"" + intData};   // emp_ids
            ContentValues updateValues = new ContentValues();
            updateValues.put("emp_name",strName);
            updateValues.put("emp_salary", intSalary); //new salary
            updateValues.put("emp_dno", strDept);
            updateValues.put("emp_sex",strSex);// relocate to new department

            int recAffected = db.update("Employee", updateValues, "emp_id = ?", whereArgs);

            db.setTransactionSuccessful(); //commit your changes

            utilShowViaHandler("Employee updated affected" + recAffected);

        } catch (SQLiteException e) {
            utilShowViaHandler("Error " + e.getMessage());
        } finally {
            db.endTransaction();
            }
        }

}//main2activity
