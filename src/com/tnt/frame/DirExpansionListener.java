package com.tnt.frame;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.tnt.util.StaticUtil;

public class DirExpansionListener implements TreeExpansionListener {
	
	private DefaultTreeModel m_model;
	
	public DirExpansionListener(DefaultTreeModel m_model){
		this.m_model=m_model;
	}
	
	public void treeExpanded(TreeExpansionEvent event) {
		final DefaultMutableTreeNode node = StaticUtil.getTreeNode(event.getPath());
		final FileNode fnode = (FileNode)StaticUtil.getNode(node);

		Thread runner = new Thread() {
			public void run() {
				if (fnode != null && fnode.expand(node)) {
					Runnable runnable = new Runnable() {
						public void run() {
							m_model.reload(node);
						}
					};
					SwingUtilities.invokeLater(runnable);
				}
			}
		};
		runner.start();
	}

	public void treeCollapsed(TreeExpansionEvent event) {
	}
}