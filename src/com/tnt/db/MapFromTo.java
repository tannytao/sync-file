package com.tnt.db;

/**
 * MapFromTo entity. @author MyEclipse Persistence Tools
 */

public class MapFromTo implements java.io.Serializable {

	// Fields

	private Integer id;
	private String SFold;
	private String DFold;

	// Constructors

	/** default constructor */
	public MapFromTo() {
	}

	/** full constructor */
	public MapFromTo(String SFold, String DFold) {
		this.SFold = SFold;
		this.DFold = DFold;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSFold() {
		return this.SFold;
	}

	public void setSFold(String SFold) {
		this.SFold = SFold;
	}

	public String getDFold() {
		return this.DFold;
	}

	public void setDFold(String DFold) {
		this.DFold = DFold;
	}

}