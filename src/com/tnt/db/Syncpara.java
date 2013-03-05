package com.tnt.db;

/**
 * Syncpara entity. @author MyEclipse Persistence Tools
 */

public class Syncpara implements java.io.Serializable {

	// Fields

	private Integer id;
	private String type;
	private String data;

	// Constructors

	/** default constructor */
	public Syncpara() {
	}

	/** full constructor */
	public Syncpara(String type, String data) {
		this.type = type;
		this.data = data;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}

}