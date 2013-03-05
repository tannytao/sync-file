package com.tnt.util;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tnt.db.MapFromTo;
import com.tnt.db.SyncDetail;
import com.tnt.db.SyncDetailDAO;
import com.tnt.db.Syncpara;
import com.tnt.frame.BackFileNode;
import com.tnt.frame.DetailTableModel;
import com.tnt.frame.FileNode;
import com.tnt.frame.IconData;
import com.tnt.job.FileSyncJob;

public class StaticUtil {

	private static final Log log = LogFactory.getLog(StaticUtil.class);

	public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");
	public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");
	public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");
	public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon("expandedfolder.gif");
	public static final SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final int SUFFIX_LENGTH = 12;
	

	public static DefaultMutableTreeNode getTreeNode(TreePath path) {
		return (DefaultMutableTreeNode) (path.getLastPathComponent());
	}

	public static Object getNode(DefaultMutableTreeNode node) {
		if (node == null)
			return null;
		Object obj = node.getUserObject();
		if (obj instanceof IconData)
			obj = ((IconData) obj).getObject();
		if (obj instanceof FileNode)
			return (FileNode) obj;
		else if (obj instanceof BackFileNode)
			return (BackFileNode) obj;
		else
			return null;
	}

	/**
	 * 根据from文件夹找到对应的to文件夹进行比对
	 * @param fromroot
	 * @param toroot
	 * @param pathFrom
	 * @param pathTo
	 * @param syncDetailDao
	 * @throws IOException
	 */
	
	public static void fileSync(boolean fullBak,String fromroot, String toroot,
			String pathFrom, String pathTo, SyncDetailDAO syncDetailDao)
			throws IOException {

		File fileFrom = new File(pathFrom);
		File fileTo = new File(pathTo);

		if (fromroot.equals(pathFrom)) {
			for (String subFile : fileFrom.list()) {
				fileSync(fullBak,fromroot, toroot, pathFrom + "\\" + subFile, pathTo
						+ "\\" + subFile, syncDetailDao);
			}
		} else {
			if (fileFrom.isDirectory()) {
				List<SyncDetail> list = syncDetailDao.findBySourceFile(pathFrom);
				if (list.size() > 0) {// 有过备份,取最后一次进行比较
					pathTo = list.get(list.size() - 1).getTargetFile();
					fileTo = new File(pathTo);
//				}
//				if (fileTo.exists()) {
					if (fileTo.isDirectory()) {
						for (String subFile : fileFrom.list()) {
							fileSync(fullBak,fromroot, toroot, pathFrom + "\\"
									+ subFile, pathTo + "\\" + subFile,
									syncDetailDao);
						}
					} else {
						fileTo.mkdir();
						for (String subFile : fileFrom.list()) {
							fileSync(fullBak,fromroot, toroot, pathFrom + "\\"
									+ subFile, pathTo + "\\" + subFile,
									syncDetailDao);
						}
					}
					
					
				} else {
					
//					pathTo = pathTo.substring(0, pathTo.lastIndexOf("."));

//					pathTo = pathTo.substring(0, pathTo.length() - 12);
					pathTo = pathTo + getFileSuffix();
					fileTo = new File(pathTo);
					
					try {
						fileTo.mkdir();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					
					SyncDetail sd = new SyncDetail();
					sd.setOperTime(new Timestamp(new Date().getTime()));
					sd.setSourceFile(pathFrom);
					sd.setTargetFile(pathTo);
					sd.setOperDesc("create");
					sd.setIsFile(false);
					syncDetailDao.save(sd);
					for (String subFile : fileFrom.list()) {
						fileSync(fullBak,fromroot, toroot, pathFrom + "\\" + subFile,
								pathTo + "\\" + subFile, syncDetailDao);
					}
					
					
				}
			} else {// 源是文件
				List<SyncDetail> list = syncDetailDao.findBySourceFile(pathFrom);
				if (list.size() > 0) {// 有过备份,取最后一次进行比较
					pathTo = list.get(list.size() - 1).getTargetFile();
					fileTo = new File(pathTo);
					if (fullBak||(!Md5Util.getFileMD5String(fileFrom).equals(Md5Util.getCodedFileMD5String(fileTo)))) {//判断文件是否一致
						String extendName = pathTo.substring(pathTo.lastIndexOf("."));
						pathTo = pathTo.substring(0, pathTo.lastIndexOf("."));

						pathTo = pathTo.substring(0, pathTo.length() - SUFFIX_LENGTH);
						pathTo = pathTo + getFileSuffix() + extendName;
						log.info("not match,file from " + fileFrom+ ", file to" + fileTo);
						
						//判断硬盘空间
						long needSpace = fileFrom.length();
						File targetDisk = fileTo;
						while(targetDisk.getParentFile()!=null){
							targetDisk = targetDisk.getParentFile();
						}
						if(needSpace > targetDisk.getUsableSpace()){
							log.warn("disk usable space is not enough,From:"+pathFrom+" To:"+pathTo);
							return;
						}
						
						OutputStream os = new FileOutputStream(pathTo);
						InputStream is = new FileInputStream(pathFrom);
						try {
														
							copyFromToStreams(is, os);

							SyncDetail sd = new SyncDetail();
							sd.setOperTime(new Timestamp(new Date().getTime()));
							sd.setSourceFile(pathFrom);
							sd.setTargetFile(pathTo);
							sd.setOperDesc("update");
							sd.setIsFile(true);
														
							syncDetailDao.save(sd);
						}catch(Exception e){
							e.printStackTrace();
							return;
						} finally {
							os.close();
							is.close();
						}					
					}
					
				} else {// 没有备份过,第一次备份

					pathTo = pathTo.substring(0, pathTo.lastIndexOf("."))
							+ getFileSuffix()
							+ pathTo.substring(pathTo.lastIndexOf("."));
					log.info("first backup,file from " + pathFrom + ",file to "
							+ pathTo);
					
					//判断硬盘空间
					long needSpace = fileFrom.length();
					File targetDisk = fileTo;
					while(targetDisk.getParentFile()!=null){
						targetDisk = targetDisk.getParentFile();
					}
					if(needSpace > targetDisk.getUsableSpace()){
						log.warn("disk usable space is not enough,From:"+pathFrom+" To:"+pathTo);
						return;
					}
					
					OutputStream os = new FileOutputStream(pathTo);
					InputStream is = new FileInputStream(pathFrom);
					try {
						copyFromToStreams(is, os);

						SyncDetail sd = new SyncDetail();
						sd.setOperTime(new Timestamp(new Date().getTime()));
						sd.setSourceFile(pathFrom);
						sd.setTargetFile(pathTo);
						sd.setOperDesc("create");
						sd.setIsFile(true);
						syncDetailDao.save(sd);
					}catch(Exception e){
						e.printStackTrace();
						return;
					} finally {
						os.close();
						is.close();
					}					
				}
			}
		}

		//设置文件属性
		try {
			syncFileAttrib(pathFrom,pathTo);							
		} catch (Exception e) {
			log.info(e);
			e.printStackTrace();
		}
	}
	
	public static void findDeleteFile(SyncDetailDAO syncDetailDao){
		List<SyncDetail> list = syncDetailDao.findLastBackupDetailFilterDel();
		List<String> delFiles = new ArrayList<String>();//记录此次同步中已经删除过的文件名
		for(SyncDetail sdObj:list){
			File file = new File(sdObj.getSourceFile());
			if(!file.exists()){
				if(!delFiles.contains(sdObj.getSourceFile())){
					SyncDetail sd = new SyncDetail();
					sd.setOperTime(new Timestamp(new Date().getTime()));
					sd.setSourceFile(sdObj.getSourceFile());
					sd.setTargetFile("");
					sd.setOperDesc("delete");
					sd.setIsFile(sdObj.getIsFile());
					syncDetailDao.save(sd);
					delFiles.add(sdObj.getSourceFile());
				}				
			}
		}
	}

	public static void copyFromToStreams(final InputStream in,
			final OutputStream out) throws IOException {
		final byte[] bytes = new byte[1024];

		int bytesRead = in.read(bytes);
		while (bytesRead > -1) {
			for (int i = 0; i < bytesRead; i++) {
				int b = 0;
				for (int j = 0; j < 8; j++) {
					int bit = (bytes[i] >> j & 1) == 0 ? 1 : 0;
					b += (1 << j) * bit;
				}
				bytes[i] = (byte) b;
			}

			out.write(bytes, 0, bytesRead);
			bytesRead = in.read(bytes);
		}
	}

	// public static void restore(final InputStream in, final OutputStream out)
	// throws IOException {
	//
	// final byte[] bytes = new byte[1024];
	// log.info("restore start");
	// int bytesRead = in.read(bytes);
	// while (bytesRead > -1) {
	// for(int i=0;i<bytesRead;i++){
	// int b=0;
	// for (int j=0;j<8;j++){
	// int bit = (bytes[i]>>j&1)==0?1:0;
	// b += (1<<j)*bit;
	// }
	// bytes[i]=(byte)b;
	// }
	// out.write(bytes, 0, bytesRead);
	// bytesRead = in.read(bytes);
	// }
	// log.info("restore over");
	// }
	public static void restore(final InputStream in, final OutputStream out,
			final JProgressBar dpb) throws IOException {

		final byte[] bytes = new byte[1024];
		log.info("restore start");
		int hasRead = 0;
		int bytesRead = in.read(bytes);
		while (bytesRead > -1) {
			hasRead += bytesRead;
			dpb.setValue(hasRead);
			for (int i = 0; i < bytesRead; i++) {
				int b = 0;
				for (int j = 0; j < 8; j++) {
					int bit = (bytes[i] >> j & 1) == 0 ? 1 : 0;
					b += (1 << j) * bit;
				}
				bytes[i] = (byte) b;
			}
			out.write(bytes, 0, bytesRead);
			bytesRead = in.read(bytes);
		}
	}

	private static String getFileSuffix() {
		List<Syncpara> spLst = SpringFactory.getSyncparadao().findByType(
				"suffix");
		Syncpara sp = spLst.get(0);

		String suffix = sp.getData();
		if (sp.getData().equals("99999"))
			sp.setData("1");
		else
			sp.setData((Long.parseLong(sp.getData()) + 1) + "");
		SpringFactory.getSyncparadao().save(sp);
		Calendar calendar = Calendar.getInstance();
		String year = (calendar.get(Calendar.YEAR) + "").substring(2);
		String month = (calendar.get(Calendar.MONTH) + 1) + "";
		if (month.length() == 1)
			month = "0" + month;
		String day = calendar.get(Calendar.DATE) + "";
		if (day.length() == 1)
			day = "0" + day;
		switch (suffix.length()) {
		case 1:
			suffix = "0000" + suffix;
			break;
		case 2:
			suffix = "000" + suffix;
			break;
		case 3:
			suffix = "00" + suffix;
			break;
		case 4:
			suffix = "0" + suffix;
			break;
		}
		return "E" + year + month + day + suffix;
	}

	// public static void restoreFile(String bakName,String restoreName) throws
	// IOException{
	// InputStream is = new FileInputStream(bakName);
	// OutputStream os = new FileOutputStream(restoreName);
	//
	// try{
	// restore(is,os);
	// }finally{
	// is.close();
	// os.close();
	// }
	// }
	
	
	public static void restoreDirectory(String topDirectory, String restoreName,
			final JProgressBar dpb,String date) throws IOException {
		
		List<SyncDetail> restoreFilesPath = SpringFactory.getSyncdetaildao().findLastBackupDetailByTargetFile(topDirectory,date);
		List<File> restoreFiles = new ArrayList<File>();
		File topFile = null;
		int filesSize = 0 ;
		for(SyncDetail sdObj:restoreFilesPath){
			File file = new File(sdObj.getTargetFile());
			if(file.getAbsolutePath().equals(topDirectory)){
				topFile = file;
			}
			filesSize += (int)(file.length());
			restoreFiles.add(file);
		}
		
		dpb.setMaximum(filesSize);
		
		if(topFile.isDirectory()){
			File file = new File(restoreName);
			file.mkdir();
			for(File subFile:topFile.listFiles()){
				String subrestoreName = restoreName+"\\"+subFile.getName().substring(0,subFile.getName().length()-SUFFIX_LENGTH);//文件夹的恢复名
				if(!subFile.isDirectory()){//文件的恢复名
					String filename=subFile.getName().substring(0,subFile.getName().lastIndexOf("."));
					String extendName = subFile.getName().substring(subFile.getName().lastIndexOf("."));
					filename=filename.substring(0,filename.length()-SUFFIX_LENGTH);
					subrestoreName = restoreName+"\\"+filename+extendName;
				}
				restoreFromTopFold(subFile.getAbsolutePath(),subrestoreName,restoreFilesPath,restoreFiles,dpb);
			}			
		}else{
			restoreFile(topDirectory, restoreName,dpb);
		}
		
		
				
	}
	
	public static void restoreFromTopFold(String topDirectory,String restoreName,List<SyncDetail> restoreFilesPath,List<File> restoreFiles,JProgressBar dpb){
		for(File fileObj:restoreFiles){
			if(fileObj.getAbsolutePath().equals(topDirectory)){
				if(fileObj.isDirectory()){
					File file = new File(restoreName);
					file.mkdir();
					//设置文件属性
					try {
						syncFileAttrib(topDirectory,restoreName);							
					} catch (Exception e) {
						log.info(e);
						e.printStackTrace();
					}
					for(File subFile:fileObj.listFiles()){
						String subrestoreName = restoreName+"\\"+subFile.getName();//文件夹的恢复名
						if(!subFile.isDirectory()){//文件的恢复名
							String filename=subFile.getName().substring(0,subFile.getName().lastIndexOf("."));
							String extendName = subFile.getName().substring(subFile.getName().lastIndexOf("."));
							filename=filename.substring(0,filename.length()-SUFFIX_LENGTH);
							subrestoreName = restoreName+"\\"+filename+extendName;
						}
						restoreFromTopFold(subFile.getAbsolutePath(),subrestoreName,restoreFilesPath,restoreFiles,dpb);
					}
				}else{
					try {
						restoreFoldFile(topDirectory,restoreName,dpb);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
		}
		
	}
	
	public static void restoreFile(String bakName, String restoreName,
			final JProgressBar dpb) throws IOException {
		InputStream is = new FileInputStream(bakName);
		OutputStream os = new FileOutputStream(restoreName);

		dpb.setMaximum((int) (new File(bakName).length()));
		try {
			restore(is, os, dpb);
		} finally {
			is.close();
			os.close();
		}
		
		//设置文件属性
		try {
			syncFileAttrib(bakName,restoreName);							
		} catch (Exception e) {
			log.info(e);
			e.printStackTrace();
		}
	}
	
	public static void restoreFoldFile(String bakName, String restoreName,
			final JProgressBar dpb) throws IOException {
		InputStream is = new FileInputStream(bakName);
		OutputStream os = new FileOutputStream(restoreName);

//		dpb.setMaximum((int) (new File(bakName).length()));
		try {
			restore(is, os, dpb);
		} finally {
			is.close();
			os.close();
		}
		
		//设置文件属性
		try {
			syncFileAttrib(bakName,restoreName);							
		} catch (Exception e) {
			log.info(e);
			e.printStackTrace();
		}
	}

	public static synchronized void refreshMTreeTop(DefaultTreeModel model,
			final JTree jtree) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(
				StaticUtil.ICON_COMPUTER, null, "目录"));

		// DefaultMutableTreeNode node;
		List<MapFromTo> mftLst = SpringFactory.getMapfromtodao().findAll();

		for (int k = 0; k < mftLst.size(); k++) {
			File rootFile = new File(mftLst.get(k).getSFold());
			FileNode nd = new FileNode(rootFile);
			IconData idata = new IconData(StaticUtil.ICON_FOLDER,
					StaticUtil.ICON_EXPANDEDFOLDER, nd);
			DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(idata);
			top.add(node1);
			if (nd.hasSubFile())
				node1.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		model.setRoot(null);
		model.setRoot(top);
		jtree.setModel(model);
		SwingUtilities.invokeLater(new Runnable() {// 实例化更新组件的线程
					public void run() {
						jtree.updateUI();
					}
				});

	}

	public static synchronized void refreshBakTreeTop(
			DefaultTreeModel bak_m_model, final JTree bak_m_tree) {
		DefaultMutableTreeNode baktop = new DefaultMutableTreeNode(
				new IconData(StaticUtil.ICON_COMPUTER, null, "目录"));

		List<String> existBakLst = new ArrayList<String>();
		List<String> prepareToCreateLst = new ArrayList<String>();
		Map<String, DefaultMutableTreeNode> map = new HashMap<String, DefaultMutableTreeNode>();
		List<String> bakLst = SpringFactory.getSyncdetaildao()
				.findBackedFileList();
		List<MapFromTo> mapFromTo = SpringFactory.getMapfromtodao().findAll();
		for (String element : bakLst) {
			try {
				getBackTreeNode(element, existBakLst, prepareToCreateLst, map,
						mapFromTo);
				processUndealNode(prepareToCreateLst, existBakLst, map,
						mapFromTo);
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

		bak_m_model.setRoot(null);
		bak_m_model.setRoot(baktop);
		bak_m_tree.setModel(bak_m_model);
		SwingUtilities.invokeLater(new Runnable() {// 实例化更新组件的线程
					public void run() {
						bak_m_tree.updateUI();
					}
				});
	}

	public static void getBackTreeNode(String element,
			List<String> existBakLst, List<String> prepareToCreateLst,
			Map<String, DefaultMutableTreeNode> map, List<MapFromTo> fromToLst)
			throws Exception {
		String[] strArray = element.split("\\\\");
//		log.info(element);

		boolean topFlag = false;
		for (MapFromTo mft : fromToLst) {
			if (mft.getSFold().equals(element)) {
				topFlag = true;
				break;
			}
		}

		// if(strArray.length==1){
		if (topFlag) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(
					new IconData(StaticUtil.ICON_COMPUTER, null, element));
			map.put(element, node);
			existBakLst.add(element);
		} else {
			// String parent = getParentPath(0,strArray,fromToLst);
			String parent = element.substring(0,
					element.lastIndexOf(strArray[strArray.length - 1]) - 1);
			if (existBakLst.contains(parent)) {
				DefaultMutableTreeNode rootNode = map.get(parent);
				// if(strArray.length==2){
				if (strArray.length
						- rootNode.getUserObject().toString().split("\\\\").length == 1) {
					boolean flag = false;
					List list = Collections.list(rootNode.children());
					for (Object childObj : list) {
						DefaultMutableTreeNode nodeChild = (DefaultMutableTreeNode) childObj;
						if (((IconData) nodeChild.getUserObject()).getObject()
								.toString()
								.equals(strArray[strArray.length - 1])) {
							flag = true;
							break;
						}
					}

					if (!flag) {
						rootNode.add(new DefaultMutableTreeNode(new IconData(
								StaticUtil.ICON_COMPUTER, null,
								new BackFileNode(element,
										strArray[strArray.length - 1]))));
						map.put(element, rootNode);
					}
				} else {
					DefaultMutableTreeNode parentNode = rootNode;
					for (int i = 0; i < strArray.length - 1; i++) {
						List list = Collections.list(parentNode.children());
						for (Object childObj : list) {
							DefaultMutableTreeNode nodeChild = (DefaultMutableTreeNode) childObj;
							if (((IconData) nodeChild.getUserObject())
									.getObject().toString().equals(strArray[i])) {
								parentNode = nodeChild;
								break;
							}
						}
					}
					if (parentNode != null) {
						parentNode.add(new DefaultMutableTreeNode(new IconData(
								StaticUtil.ICON_COMPUTER, null,
								new BackFileNode(element,
										strArray[strArray.length - 1]))));
						map.put(element, rootNode);
					}
				}

				existBakLst.add(element);
				prepareToCreateLst.remove(element);
			} else {
				if (!prepareToCreateLst.contains(element))
					prepareToCreateLst.add(element);
				getBackTreeNode(parent, existBakLst, prepareToCreateLst, map,
						fromToLst);
			}
		}
	}

	public static void processUndealNode(List<String> prepareToCreateLst,
			List<String> existBakLst, Map<String, DefaultMutableTreeNode> map,
			List<MapFromTo> fromToLst) throws Exception {
		List<String> list = new ArrayList<String>();
		list.addAll(prepareToCreateLst);
		for (String uncreate : list) {

			try {
				getBackTreeNode(uncreate, existBakLst, prepareToCreateLst, map,
						fromToLst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (prepareToCreateLst.size() > 0)
			processUndealNode(prepareToCreateLst, existBakLst, map, fromToLst);
	}

	private static String getParentPath(int startPos, String[] array,
			List<MapFromTo> mapFromToLst) {
		String path = "";
		for (int i = 0; i < startPos; i++) {
			if (i == 0) {
				path += array[i];
			} else {
				path += "\\" + array[i];
			}
		}

		for (MapFromTo mft : mapFromToLst) {
			if (mft.getSFold().equals(path)) {
				return path;
			}
		}
		if (startPos + 1 > array.length) {
			return null;
		} else
			return getParentPath(startPos + 1, array, mapFromToLst);
	}
	
	private static void syncFileAttrib (String from,String to) throws Exception{
		Path pathfrom=Paths.get(from);  
		DosFileAttributeView fromview=Files.getFileAttributeView(pathfrom, DosFileAttributeView.class);  
		DosFileAttributes fromfile =  fromview.readAttributes();
        
        Path pathto=Paths.get(to);  
        DosFileAttributeView toview=Files.getFileAttributeView(pathto, DosFileAttributeView.class);        
        DosFileAttributes tofile = toview.readAttributes();

        if(tofile.isArchive()!=fromfile.isArchive())
        	toview.setArchive(fromfile.isArchive());
        if(tofile.isHidden()!=fromfile.isHidden())
        	toview.setHidden(fromfile.isHidden());
        if(tofile.isReadOnly()!=fromfile.isReadOnly())
        	toview.setReadOnly(fromfile.isReadOnly());
        if(tofile.isSystem()!=fromfile.isSystem())
        	toview.setSystem(fromfile.isSystem());
	}
	
	public static void refreshDetailTable(JTable jt,String source,String date){
		
		((DetailTableModel)jt.getModel()).setRowCount(0);//清除表内容
		
		SyncDetail sd = StaticUtil.findSyncDetailBySourceName(source,date);
		if(sd==null)
			return;
		boolean isFile = sd.getIsFile();
		
		List<SyncDetail> list = null;
		if(isFile){
			 list = SpringFactory.getSyncdetaildao().findBySourceFileWithoutDelFile(source,date);
		}else{
			 list = SpringFactory.getSyncdetaildao().findLastBackupDetailBySourceFile(source,date);
		}
		
		for(SyncDetail detail : list){
			((DetailTableModel)jt.getModel()).addRow(new String[]{detail.getTargetFile(),StaticUtil.Timestamp2String(detail.getOperTime())}); 
		}
	}
	
	public static SyncDetail findSyncDetailBySourceName(String source,String date){
		List<SyncDetail> list = SpringFactory.getSyncdetaildao().findBySourceFileWithoutDelFile(source,date);
		if(list.size()>0)
			return list.get(0);
		else
			return null;
	}

	public static String Timestamp2String(Timestamp stamp) {
		return sdfDateTime.format(stamp);
	}
}
