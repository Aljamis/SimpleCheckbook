package org.avr.simplecheckbook.dataobjects;

import java.math.BigDecimal;

public class MasterCheckBook {
	
	private String dbName;
	private String dbLocation;
	private String description;
	private String appVersion="";
	
	private BigDecimal startingBalance;
	
	
	
	public MasterCheckBook() { }
	
	public MasterCheckBook(String dbn , String desc , String dbl) {
		this.dbName = dbn;
		this.dbLocation = dbl;
		this.description = desc;
	}
	
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getDbLocation() {
		return dbLocation;
	}
	public void setDbLocation(String dbLocation) {
		this.dbLocation = dbLocation;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		if (appVersion == null ) 
			this.appVersion = "";
		else
			this.appVersion = appVersion;
	}
	public BigDecimal getStartingBalance() {
		return startingBalance;
	}
	public void setStartingBalance(BigDecimal startingBalance) {
		this.startingBalance = startingBalance;
	}
}
