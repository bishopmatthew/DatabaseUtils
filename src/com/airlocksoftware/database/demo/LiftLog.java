package com.airlocksoftware.database.demo;

import java.util.ArrayList;

import com.airlocksoftware.database.HierarchicalSqlObject;

public class LiftLog extends HierarchicalSqlObject {

	// Instance Variables
	public String liftName;
	public String liftType;

	public int sets;
	public int reps;
	public float weight;
	public boolean isMissed;
	public boolean isFinished;
	public boolean isWarmup;

	public transient LiftLog parent;
	public transient ArrayList<SetLog> children;

	public LiftLog createNew(long parentId, String liftName, String liftType, int sets, int reps, float weight,
			boolean isMissed, boolean isFinished, boolean isWarmup) {
		this.parentId = parentId;
		this.liftName = liftName;
		this.liftType = liftType;
		this.sets = sets;
		this.reps = reps;
		this.weight = weight;
		this.isMissed = isMissed;
		this.isFinished = isFinished;
		this.isWarmup = isWarmup;
		try {
			super.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

}
