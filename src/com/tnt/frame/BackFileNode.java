package com.tnt.frame;

public class BackFileNode {

	private String fullPath;
	private String fileName;
	
	public BackFileNode(String fullPath,String fileName){
		this.fullPath=fullPath;
		this.fileName=fileName;
	}
	
	public String toString() {
		return fileName;
	}

	public String getFullPath() {
		return fullPath;
	}

}
