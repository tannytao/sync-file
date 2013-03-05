package com.tnt.test;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
 
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import org.apache.log4j.Logger;
 
/**
 * �����Ի���ѡ�������ļ����и��� �������ļ�����Ŀ��Ŀ¼
 * 
 * @author <a href="mailto:qiyang199132@163.com">qiyang</a>
 * @version $Revision$
 * @create 2011-5-9 ����02:47:21
 */
public class OnProgress1 {
	private final static Logger log = Logger.getLogger(OnProgress1.class);
 
	public static void main(String[] args) {
		OnProgress1 cf = new OnProgress1();
		File file = cf.getFile();
		if (file == null) {
			JOptionPane.showMessageDialog(null, "�������", "��ʾ", 1);
		} else {
			cf.coypFile(file);
		}
	}
 
	/**
	 * �����Ի���ѡ���ļ�
	 * 
	 * @return ����ѡ����ļ�
	 */
	public File getFile() {
		JFileChooser jf = new JFileChooser();
		jf.setDialogTitle("ѡ���ļ�");
		jf.showOpenDialog(null);
		jf.setVisible(true);
		File selectedFile = null;
		selectedFile = jf.getSelectedFile();
		if (selectedFile != null) {
			File[] list = new File(System.getProperty("user.dir")).listFiles();
			for (int i = 0; i < list.length; i++) {
				if (selectedFile.getName().equals(list[i].getName())) {
					JOptionPane.showMessageDialog(null, "���ļ��Ѵ���", "��ʾ", 1);
					return null;
				}
			}
		} else {
			System.out.println("No! You did not select the right file.");
		}
		return selectedFile;
	}
 
	/**
	 * �Դ��������ļ����и��Ʋ�������Ŀ��Ŀ¼
	 * 
	 * @param file ���������ļ�
	 */
	public void coypFile(File file) {
		String path = System.getProperty("user.dir");
		try {
			/**
			 * �ļ��ֽڳ���
			 */
			int length = (int) file.length();
			/**
			 * ������ ��ȡ�ļ�
			 */
			FileInputStream is = new FileInputStream(file);
			/**
			 * �����д�ļ�
			 */
			FileOutputStream os = new FileOutputStream(path + "/" + file.getName());
			/**
			 * ����
			 */
			byte[] bt = new byte[256];
			/**
			 * �����ֽڽ���ÿ�ζ�ȡ�ĳ���
			 */
			int b = 0;
			/**
			 * ����һ�ζ�ȡ�ĳ��� Ȼ��++�õ��Ѿ����ƶ���
			 */
			int size = 0;
			/**
			 * ����frame
			 */
			Frame frame=new Frame("������");
			/**
			 * ����һ���Ի���������ʽ
			 */
			final JDialog dlg = new JDialog(frame, "Progress Dialog", true);
			/**
			 * ����һ�������� size   ��  length  Ϊ ����  
			 */
			JProgressBar dpb = new JProgressBar(size,length);
			/**
			 * ���öԻ�����ʽ
			 */
	        dlg.add(BorderLayout.CENTER, dpb);
	        dlg.add(BorderLayout.NORTH, new JLabel("���ڸ���..."));
	        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	        dlg.setSize(300, 75);
	        /**
	         * �������ֵ ��frame
	         */
	        dlg.setLocationRelativeTo(frame);
	        /**
	         * �����߳�
	         * �Խ��������м��
	         */
	        Thread t=new Thread(new Runnable() {
				@Override
				public void run() {
					dlg.setVisible(true);
				}
			});
	        /**
	         * �����߳�
	         */
	        t.start();
	        /**
	         * ѭ��
	         * ��ȡ�ļ�   ���Զ�ȡ��++
	         * д�ļ� ���� �������Ľ���++
	         */
			while ((b = is.read(bt)) != -1) {
				size += b;
				os.write(bt);
				dpb.setValue(size);
			}
			/**
			 * �߳�����1��
			 */
			t.sleep(1000);
			/**
			 * �ر���
			 */
			os.close();
			is.close();
			/**
			 * ���������˳�����
			 */
			if (dpb.getValue() == length) {
                dlg.setVisible(false);
                System.exit(0);
            }
		} catch (FileNotFoundException e) {
			log.error("�ļ��Ҳ���");
		} catch (IOException e) {
			log.error("IO�쳣");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}