package com.example.wera.generatorliczblosowych;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static Random random = new Random();
    private static SQLiteDatabase db;
    private int minimum;
    private int maximum;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = openOrCreateDatabase("KilometersDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS kilometers (day INT, month INT, year INT, number INT)");
        sp = this.getPreferences(Context.MODE_PRIVATE);
        minimum = sp.getInt("min", 0);
        maximum = sp.getInt("max", 1);
        EditText etmin = (EditText) findViewById(R.id.min);
        EditText etmax = (EditText) findViewById(R.id.max);
        etmin.setText(String.valueOf(minimum));
        etmax.setText(String.valueOf(maximum));
        etmin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                minimum = s.toString().equals("") ? 0 : Integer.valueOf(s.toString());
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("min", minimum);
                editor.commit();
            }
        });
        etmax.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                maximum = s.toString().equals("") ? 0 : Integer.valueOf(s.toString());
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("max", maximum);
                editor.commit();
            }
        });
        updateLastNumbers();
    }

    public void generate(View view) {
        if (isThereFreeNumber() && maximum > minimum) {
            while (!saveNumberToDatabase((random.nextInt(maximum - minimum) + minimum))) ;
            updateLastNumbers();
        } else {
            Toast.makeText(getApplicationContext(), "brak wolnych numerów", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isThereFreeNumber() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        Cursor cursor = db.rawQuery("SELECT * FROM kilometers WHERE month="+month+" AND year="+year+" AND number BETWEEN "+minimum+" AND "+(maximum - 1), null);
        return cursor.getCount() < maximum - minimum;
    }

    private boolean saveNumberToDatabase(int i){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        Cursor cursor = db.rawQuery("SELECT * FROM kilometers WHERE month="+month+" AND year="+year+" AND number="+i, null);
        if(cursor.getCount()==0){
            db.execSQL("INSERT INTO kilometers VALUES("+day+","+month+","+year+","+i+")");
            Cursor c = db.rawQuery("SELECT * FROM kilometers", null);
            Toast.makeText(getApplicationContext(), Integer.toString(i), Toast.LENGTH_LONG).show();
            return true;
        }
        else return false;
    }

    private void updateLastNumbers(){
        Cursor cursor = db.rawQuery("SELECT * FROM kilometers", null);
        StringBuffer sbnumbers = new StringBuffer();
        StringBuffer sbdates = new StringBuffer();
        TextView tvnumbers = (TextView) findViewById(R.id.lastnumbers);
        TextView tvdates = (TextView) findViewById(R.id.lastdates);
        try {
            cursor.moveToLast();
            for(int i = 0; i < 12; i++){
                sbdates.append(cursor.getInt(cursor.getColumnIndex("day")));
                sbdates.append('/');
                sbdates.append(cursor.getInt(cursor.getColumnIndex("month"))+1);
                sbdates.append('/');
                sbdates.append(cursor.getInt(cursor.getColumnIndex("year")));
                sbdates.append('\n');
                sbnumbers.append(cursor.getInt(cursor.getColumnIndex("number")));
                sbnumbers.append('\n');
                cursor.moveToPrevious();
            }
        } catch (Exception e){}
        tvnumbers.setText(sbnumbers);
        tvdates.setText(sbdates);
    }

    @Override
    protected void onDestroy() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        Cursor cursor = db.rawQuery("SELECT * FROM kilometers", null);
        int amount = cursor.getCount()-10;
        if(amount > 0) db.rawQuery("DELETE FROM kilometers WHERE EXISTS (SELECT * FROM kilometers WHERE year<>"+year+" OR month<>"+month+" LIMIT "+amount+")", null);
        super.onDestroy();
    }
}
