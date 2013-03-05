package com.tnt.db;

import java.sql.Timestamp;

/**
 * SyncDetail entity. @author MyEclipse Persistence Tools
 */

public class SyncDetail implements java.io.Serializable {

	// Fields

	private Integer id;
	private String sourceFile;
	private String targetFile;
	private Timestamp operTime;
	private Integer syncCount;
	private String operDesc;
	private Boolean isFile;

	// Constructors

	/** default constructor */
	public SyncDetail() {
	}

	/** minimal constructor */
	public SyncDetail(String sourceFile, String targetFile, Timestamp operTime) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
		this.operTime = operTime;
	}

	/** full constructor */
	public SyncDetail(String sourceFile, String targetFile, Timestamp operTime,
			Integer syncCount) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
		this.operTime = operTime;
		this.syncCount = syncCount;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSourceFile() {
		return this.sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public String getTargetFile() {
		return this.targetFile;
	}

	public void setTargetFile(String targetFile) {
		this.targetFile = targetFile;
	}

	public Timestamp getOperTime() {
		return this.operTime;
	}

	public void setOperTime(Timestamp operTime) {
		this.operTime = operTime;
	}

	public Integer getSyncCount() {
		return this.syncCount;
	}

	public void setSyncCount(Integer syncCount) {
		this.syncCount = syncCount;
	}

	public String getOperDesc() {
		return operDesc;
	}

	public void setOperDesc(String operDesc) {
		this.operDesc = operDesc;
	}

	public Boolean getIsFile() {
		return isFile;
	}

	public void setIsFile(Boolean isFile) {
		this.isFile = isFile;
	}

}