package com.tnt.frame;

import java.util.List;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import com.tnt.db.SyncDetail;
import com.tnt.util.SpringFactory;
import com.tnt.util.StaticUtil;

public class DirSelectionListener implements TreeSelectionListener {
		
	private JTable jt;
	private DateChooser datechooser;
	
	public DirSelectionListener(){
		
	}
	
	public void valueChanged(TreeSelectionEvent event) {
		DefaultMutableTreeNode node = StaticUtil.getTreeNode(event.getPath());
		String source = "";
		boolean isDirectory=false;
		Object fnode = StaticUtil.getNode(node);
		if(fnode instanceof FileNode){
			source= ((FileNode)fnode).getFile().getAbsolutePath();
			
		}else if(fnode instanceof BackFileNode){
			source= ((BackFileNode)fnode).getFullPath();
			
		}
		
		datechooser.setSource(source);
		datechooser.setDetailTable(jt);
		
		StaticUtil.refreshDetailTable(jt, source, datechooser.getSelectedDateText());
		
		
		
	}
	
	public void setJTable(JTable jt){
		this.jt = jt;
	}

	public DateChooser getDatechooser() {
		return datechooser;
	}

	public void setDatechooser(DateChooser datechooser) {
		this.datechooser = datechooser;
	}
}