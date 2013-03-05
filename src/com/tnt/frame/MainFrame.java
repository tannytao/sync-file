package com.tnt.frame;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jvnet.substance.skin.SubstanceFieldOfWheatLookAndFeel;
import org.jvnet.substance.skin.SubstanceMagmaLookAndFeel;
import org.jvnet.substance.skin.SubstanceMangoLookAndFeel;
import org.jvnet.substance.skin.SubstanceMistAquaLookAndFeel;
import org.jvnet.substance.skin.SubstanceMistSilverLookAndFeel;
import org.jvnet.substance.skin.SubstanceModerateLookAndFeel;
import org.jvnet.substance.skin.SubstanceNebulaLookAndFeel;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.tnt.db.MapFromTo;
import com.tnt.db.MapFromToDAO;
import com.tnt.db.SyncDetail;
import com.tnt.db.SyncDetailDAO;
import com.tnt.job.FileSyncJob;
import com.tnt.util.DirWatcher;
import com.tnt.util.SpringFactory;
import com.tnt.util.StaticUtil;

public class MainFrame extends JFrame {

	private static SchedulerFactory sf = new StdSchedulerFactory();
	private static final Log log = LogFactory.getLog(MainFrame.class);

	private Image icon;// ����ͼ��
	private TrayIcon trayIcon;
	private SystemTray systemTray;// ϵͳ����
	private PopupMenu pop = new PopupMenu(); // ����һ���Ҽ�����ʽ�˵�
	private MenuItem exit = new MenuItem("�˳�����");
	private MenuItem show = new MenuItem("��ʾ����");

	protected JTree m_tree;
	protected DefaultTreeModel m_model;
	private static DefaultTreeModel bak_m_model;
	private static JTree bak_m_tree;

	// private List<String> existBakLst;
	// private List<String> prepareToCreateLst;
	// private Map<String,DefaultMutableTreeNode> map;

	public MainFrame() {

		// ����ͼ�겿�ֽ���
		// icon��ʼ��
		icon = Toolkit.getDefaultToolkit().getImage("C://disk5.png");

		// Toolkit.getDefaultToolkit().getImage(
		// this.getClass().getResource("C://disk5.gif"));//����ͼ����ʾ��ͼƬ

		if (SystemTray.isSupported()) {
			systemTray = SystemTray.getSystemTray();// ���ϵͳ���̵�ʵ��
			trayIcon = new TrayIcon(icon, "���ŵ�����ͼ���ϵ�����", pop);
			// wasw100
			pop.add(show);
			pop.add(exit);

			try {
				systemTray.add(trayIcon); // �������̵�ͼ��
			} catch (AWTException e1) {
				e1.printStackTrace();
			}
			addWindowListener(new WindowAdapter() {
				public void windowIconified(WindowEvent e) {
					dispose();// ������С��ʱdispose�ô���
				}
			});

			trayIcon.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 1
							&& e.getButton() != MouseEvent.BUTTON3) {// ��������̴������֣������˫������e.getClickCount()
																		// == 2
						setVisible(true);
						setExtendedState(JFrame.NORMAL);// ���ô� frame ��״̬��
					}
				}
			});

			show.addActionListener(new ActionListener() { // ���"��ʾ����"�˵��󽫴�����ʾ����

				public void actionPerformed(ActionEvent e) {
					// systemTray.remove(trayIcon); // ��ϵͳ������ʵ�����Ƴ�����ͼ��
					setVisible(true); // ��ʾ����
					setExtendedState(JFrame.NORMAL);
				}
			});
			exit.addActionListener(new ActionListener() { // ������˳���ʾ���˵����Ƴ�����

				public void actionPerformed(ActionEvent e) {
					// �������дִ���˳�ʱִ�еĲ���
					System.exit(0); // �˳�����
				}
			});
		}// ����ͼ�겿�ֽ���

		// ������swing����
		setIconImage(icon);// ���ĳ���ͼ��

		final JDialog dlg = new JDialog(this, "Progress Dialog", true);
		final JProgressBar dpb = new JProgressBar(0, 100);
//		dpb.setIndeterminate(true);
		dlg.add(BorderLayout.CENTER, dpb);
		dlg.add(BorderLayout.NORTH, new JLabel("���ڸ���..."));
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.setSize(300, 75);
		dlg.setLocationRelativeTo(this);

		// tree����
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(
				StaticUtil.ICON_COMPUTER, null, "Ŀ¼"));

		// DefaultMutableTreeNode node;
		List<MapFromTo> mftLst = SpringFactory.getMapfromtodao().findAll();

		DirWatcher[] watcherArray = new DirWatcher[mftLst.size()];

		for (int k = 0; k < mftLst.size(); k++) {
			watcherArray[k] = new DirWatcher(
					Paths.get(mftLst.get(k).getSFold()), true);
			File rootFile = new File(mftLst.get(k).getSFold());
			FileNode nd = new FileNode(rootFile);
			nd.setFileName(rootFile.getAbsolutePath());
			IconData idata = new IconData(StaticUtil.ICON_FOLDER,
					StaticUtil.ICON_EXPANDEDFOLDER, nd);
			// IconData idata = new IconData(StaticUtil.ICON_COMPUTER, null,
			// nd);

			DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(idata);
			top.add(node1);
			if (nd.hasSubFile())
				node1.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		m_model = new DefaultTreeModel(top);
		m_tree = new JTree(m_model);
		for (DirWatcher watcher : watcherArray) {
			watcher.setModel(m_model);
			watcher.setJtree(m_tree);
			watcher.startMonitorDir();
		}
		
		 final DateChooser mp = new DateChooser("yyyy-MM-dd");

		// m_tree.putClientProperty("JTree.lineStyle", "Angled");
		TreeCellRenderer renderer = new IconCellRenderer();
		// m_tree.setCellRenderer(renderer);
		m_tree.addTreeExpansionListener(new DirExpansionListener(m_model));
		DirSelectionListener treeSelLsn = new DirSelectionListener();
		treeSelLsn.setDatechooser(mp);
		m_tree.addTreeSelectionListener(treeSelLsn);
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(true);
		m_tree.setEditable(false);
		

		JScrollPane s = new JScrollPane();
		s.getViewport().add(m_tree);

		JScrollPane baks = new JScrollPane();

		DefaultMutableTreeNode baktop = new DefaultMutableTreeNode(
				new IconData(StaticUtil.ICON_COMPUTER, null, "Ŀ¼"));

		List<String> existBakLst = new ArrayList<String>();
		List<String> prepareToCreateLst = new ArrayList<String>();
		Map<String, DefaultMutableTreeNode> map = new HashMap<String, DefaultMutableTreeNode>();
		List<String> bakLst = SpringFactory.getSyncdetaildao().findBackedFileList();
		List<MapFromTo> mapFromTo = SpringFactory.getMapfromtodao().findAll();
		for (String element : bakLst) {
			try {
				StaticUtil.getBackTreeNode(element, existBakLst,prepareToCreateLst, map, mapFromTo);
				StaticUtil.processUndealNode(prepareToCreateLst, existBakLst,map, mapFromTo);
			} catch (Exception e1) {
				log.warn(e1);
				e1.printStackTrace();
			}

		}

		for (String key : map.keySet()) {
			Object obj = map.get(key);
			DefaultMutableTreeNode rootnode = (DefaultMutableTreeNode) obj;
			if (((IconData) rootnode.getUserObject()).getObject() instanceof String) {
				baktop.add(rootnode);
			}
		}

		bak_m_model = new DefaultTreeModel(baktop);
		bak_m_tree = new JTree(bak_m_model);
		bak_m_tree.addTreeSelectionListener(treeSelLsn);
		bak_m_tree.setShowsRootHandles(true);
		bak_m_tree.setEditable(false);
		baks.getViewport().add(bak_m_tree);

		// tree����--end
		// list
		final DetailTableModel tableData = new DetailTableModel();
		final JTable jt = new JTable(tableData);
		treeSelLsn.setJTable(jt);

		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightPane.setEnabled(false);
		JScrollPane jsp = new JScrollPane(jt);
		// JToolBar toolbar = new JToolBar();
		JPanel toolbar = new JPanel();
		
		
		 toolbar.add(mp);
				
		JButton btnToDefault = new JButton("�ָ���Դ�ļ���");
		btnToDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jt.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "��ѡ����Ҫ�ָ����ļ�", "��ʾ",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String toPath = jt.getValueAt(jt.getSelectedRow(), 0)
						.toString();
				List<SyncDetail> list = SpringFactory.getSyncdetaildao().findByTargetFile(toPath);
				String fromPath = "";
				for (SyncDetail detail : list) {
					fromPath = detail.getSourceFile();
					break;
				}
				if (fromPath.length() > 0) {
					try {

						fromPath = fromPath.substring(0,
								fromPath.lastIndexOf("\\"))
								+ toPath.substring(toPath.lastIndexOf("\\"));
						final String to = toPath;
						final String from = fromPath;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dlg.setVisible(true);
							}
						});

						new Thread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									StaticUtil.restoreDirectory(to, from, dpb,mp.getSelectedDateText());
									Thread.sleep(500);
									dlg.setVisible(false);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();

					} catch (Exception e1) {
						log.warn(e1);
						e1.printStackTrace();
					}
					// try {
					//
					// fromPath =
					// fromPath.substring(0,fromPath.lastIndexOf("\\"))+toPath.substring(toPath.lastIndexOf("\\"));
					//
					// StaticUtil.restoreFile(toPath, fromPath);
					// } catch (IOException e1) {
					// log.warn(e1);
					// e1.printStackTrace();
					// }
				}
			}

		});
		JButton btnSaveTo = new JButton("�ָ���");
		final JFileChooser j2 = new JFileChooser();
		btnSaveTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jt.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "��ѡ����Ҫ�ָ����ļ�", "��ʾ",JOptionPane.ERROR_MESSAGE);
					return;
				}
				final String toPath = jt.getValueAt(jt.getSelectedRow(), 0).toString();
				log.info(toPath);
				List<SyncDetail> list = SpringFactory.getSyncdetaildao().findByTargetFile(toPath);
				String fromPath = "";
				boolean isFile=true;
				for (SyncDetail detail : list) {
					fromPath = detail.getSourceFile();
					isFile = detail.getIsFile();
					log.info(fromPath);
					break;
				}
				if (fromPath.length() > 0 && isFile) {
					j2.setCurrentDirectory(new File(fromPath));// ���ô򿪶Ի����Ĭ��·��
					j2.setSelectedFile(new File(fromPath.substring(0,
							fromPath.lastIndexOf("\\"))
							+ toPath.substring(toPath.lastIndexOf("\\"))));// ����ѡ��ԭ�����ļ�
					int n = j2.showSaveDialog(null);
					final String filename2 = j2.getSelectedFile().toString();
					try {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dlg.setVisible(true);
							}
						});
						new Thread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									StaticUtil.restoreDirectory(toPath, filename2,dpb,mp.getSelectedDateText());
									Thread.sleep(2000);
									dlg.setVisible(false);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// j2.setCurrentDirectory(new File(fromPath));//
					// ���ô򿪶Ի����Ĭ��·��
					// j2.setSelectedFile(new
					// File(fromPath.substring(0,fromPath.lastIndexOf("\\"))+toPath.substring(toPath.lastIndexOf("\\"))));//
					// ����ѡ��ԭ�����ļ�
					// int n = j2.showSaveDialog(null);
					// String filename2 = j2.getSelectedFile().toString();
					// try {
					//
					// Thread t=new Thread(new Runnable() {
					// @Override
					// public void run() {
					// dlg.setVisible(true);
					// }
					// });
					// t.start();
					// StaticUtil.restoreFile(toPath, filename2);
					// dlg.setVisible(false);
					// } catch (IOException e1) {
					// // TODO Auto-generated catch block
					// e1.printStackTrace();
					// }
				}else if(fromPath.length() > 0 && !isFile){//�ָ�����Ŀ¼
					j2.setCurrentDirectory(new File(fromPath));// ���ô򿪶Ի����Ĭ��·��
					j2.setSelectedFile(new File(fromPath.substring(0,
							fromPath.lastIndexOf("\\"))
							+ toPath.substring(toPath.lastIndexOf("\\"))));// ����ѡ��ԭ�����ļ�
					int n = j2.showSaveDialog(null);
					final String filename2 = j2.getSelectedFile().toString();
					try {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dlg.setVisible(true);
							}
						});
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									StaticUtil.restoreDirectory(toPath, filename2,dpb,mp.getSelectedDateText());
									Thread.sleep(2000);
									dlg.setVisible(false);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}).start();

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}
		});
		JButton btnSync = new JButton("ͬ��");
		btnSync.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				sync(false);
				StaticUtil.refreshBakTreeTop(bak_m_model, bak_m_tree);
			}

		});
		
		JButton btnFullSync = new JButton("ȫͬ��");
		btnFullSync.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				sync(true);
				StaticUtil.refreshBakTreeTop(bak_m_model, bak_m_tree);
			}

		});

		toolbar.add(btnToDefault);
		toolbar.add(btnSaveTo);
		toolbar.add(btnSync);

		rightPane.add(toolbar);
		rightPane.add(jsp);

		JSplitPane toppanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		toppanel.setDividerLocation(300);
		JTabbedPane jtp = new JTabbedPane();
		jtp.addTab("����Ŀ���ļ���", s);
		jtp.addTab("��ʷ�����ļ���", baks);

		toppanel.add(jtp);
		toppanel.add(rightPane);

		add(toppanel);

		setTitle("FileSync");
		setSize(1000, 600);
		// �Զ�ȷ������λ��
		setLocationByPlatform(true);
		// ����رհ�ť�ܹ��Զ��˳�
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public static void main(String[] args) {

		initSkin();
		new MainFrame();
		fileSyncTread();
	}

	private static void initSkin() {
		LookAndFeel lookAndFeel = new SubstanceMistAquaLookAndFeel();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// SwingUtilities.updateComponentTreeUI(frame);
	}

	public static void fileSyncTread() {
		try {
			// ��ʱ����
			Scheduler sched = sf.getScheduler();
			JobDetail job = new JobDetail("FileSync", "GPFileSync",FileSyncJob.class);
			JobDataMap jobData = new JobDataMap();
			jobData.put("model", bak_m_model);
			jobData.put("tree", bak_m_tree);
			job.setJobDataMap(jobData);
			CronTrigger trigger = new CronTrigger("FileSync", "GPFileSync",
					"FileSync", "GPFileSync", "0 0 1 * * ?");
			sched.addJob(job, true);
			Date ft = sched.scheduleJob(trigger);
			log.info("File sync is running..");

			sched.start();
		} catch (Exception e) {

		}
	}

	private static void sync(boolean fullBak) {
		MapFromToDAO mapFromToDAO = SpringFactory.getMapfromtodao();
		SyncDetailDAO syncDetailDao = SpringFactory.getSyncdetaildao();
		List<MapFromTo> list = mapFromToDAO.findAll();
		for (MapFromTo mft : list) {
			try {
				log.info("Ready for Syncing");
				StaticUtil.fileSync(fullBak,mft.getSFold(), mft.getDFold(),
						mft.getSFold(), mft.getDFold(), syncDetailDao);				
				log.info("Sync over");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.warn(e);
				e.printStackTrace();
			}
		}
		StaticUtil.findDeleteFile(syncDetailDao);
	}
}