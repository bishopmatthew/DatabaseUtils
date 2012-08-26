package com.airlocksoftware.database.demo;

import java.util.ArrayList;
import java.util.Date;

import com.airlocksoftware.database.SqlObject;

public class WorkoutLog extends SqlObject {

	// Instance Variables
	public long id;
	public String name;
	public Date date;
	public String note;
	public boolean isFinished;

	public transient ArrayList<LiftLog> children;

	// CONSTANTS
	private static final String TAG = WorkoutLog.class.getSimpleName();

}
