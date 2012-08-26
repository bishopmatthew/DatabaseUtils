package com.airlocksoftware.database.demo;

import com.airlocksoftware.database.SqlObject;

public class SetLog extends SqlObject {

	// Instance Variables
	public long setLogId;
	public long liftLogId;// parent
	public int reps;
	public float weight;
	public boolean isWarmup;
	public boolean isMarked;
	public boolean isMissed;

	public transient LiftLog parent;

}
