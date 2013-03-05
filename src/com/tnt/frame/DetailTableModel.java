package com.tnt.frame;

import javax.swing.table.DefaultTableModel;

public class DetailTableModel extends DefaultTableModel {
	/**
   * 
   */
	private static final long serialVersionUID = 679265889547674796L;
	public final String[] COLUMN_NAMES = new String[] { "�ļ���", "ͬ��ʱ��"};

	public DetailTableModel() {
	}

	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}

	// ��Table���ֻ����
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}