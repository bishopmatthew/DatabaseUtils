package com.airlocksoftware.database.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class TestActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		LiftLog liftLog = new LiftLog();
		liftLog.createTable();
		
		LiftLog one = new LiftLog();
		one.createNew(1, "Lorem", "Ipsum", 3, 5, 45.0f, false, false, false);
		
		LiftLog two = new LiftLog();
		two.read(1);
		
		Log.d("Test", one.toString());
		Log.d("Test", two.toString());
	}

}
