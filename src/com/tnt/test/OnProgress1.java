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
 * 弹出对话框选择任意文件进行复制 复制新文件在项目跟目录
 * 
 * @author <a href="mailto:qiyang199132@163.com">qiyang</a>
 * @version $Revision$
 * @create 2011-5-9 下午02:47:21
 */
public class OnProgress1 {
	private final static Logger log = Logger.getLogger(OnProgress1.class);
 
	public static void main(String[] args) {
		OnProgress1 cf = new OnProgress1();
		File file = cf.getFile();
		if (file == null) {
			JOptionPane.showMessageDialog(null, "错误操作", "提示", 1);
		} else {
			cf.coypFile(file);
		}
	}
 
	/**
	 * 弹出对话框选择文件
	 * 
	 * @return 返回选择的文件
	 */
	public File getFile() {
		JFileChooser jf = new JFileChooser();
		jf.setDialogTitle("选择文件");
		jf.showOpenDialog(null);
		jf.setVisible(true);
		File selectedFile = null;
		selectedFile = jf.getSelectedFile();
		if (selectedFile != null) {
			File[] list = new File(System.getProperty("user.dir")).listFiles();
			for (int i = 0; i < list.length; i++) {
				if (selectedFile.getName().equals(list[i].getName())) {
					JOptionPane.showMessageDialog(null, "改文件已存在", "提示", 1);
					return null;
				}
			}
		} else {
			System.out.println("No! You did not select the right file.");
		}
		return selectedFile;
	}
 
	/**
	 * 对传进来的文件进行复制并放入项目跟目录
	 * 
	 * @param file 传进来的文件
	 */
	public void coypFile(File file) {
		String path = System.getProperty("user.dir");
		try {
			/**
			 * 文件字节长度
			 */
			int length = (int) file.length();
			/**
			 * 输入流 读取文件
			 */
			FileInputStream is = new FileInputStream(file);
			/**
			 * 输出流写文件
			 */
			FileOutputStream os = new FileOutputStream(path + "/" + file.getName());
			/**
			 * 缓存
			 */
			byte[] bt = new byte[256];
			/**
			 * 定义字节接受每次读取的长度
			 */
			int b = 0;
			/**
			 * 定义一次读取的长度 然后++得到已经复制多少
			 */
			int size = 0;
			/**
			 * 定义frame
			 */
			Frame frame=new Frame("进度条");
			/**
			 * 定义一个对话框并设置样式
			 */
			final JDialog dlg = new JDialog(frame, "Progress Dialog", true);
			/**
			 * 定义一个进度条 size   和  length  为 参数  
			 */
			JProgressBar dpb = new JProgressBar(size,length);
			/**
			 * 设置对话框样式
			 */
	        dlg.add(BorderLayout.CENTER, dpb);
	        dlg.add(BorderLayout.NORTH, new JLabel("正在复制..."));
	        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	        dlg.setSize(300, 75);
	        /**
	         * 设置相对值 给frame
	         */
	        dlg.setLocationRelativeTo(frame);
	        /**
	         * 定义线程
	         * 对进度条进行监控
	         */
	        Thread t=new Thread(new Runnable() {
				@Override
				public void run() {
					dlg.setVisible(true);
				}
			});
	        /**
	         * 开启线程
	         */
	        t.start();
	        /**
	         * 循环
	         * 读取文件   让以读取的++
	         * 写文件 设置 进度条的进度++
	         */
			while ((b = is.read(bt)) != -1) {
				size += b;
				os.write(bt);
				dpb.setValue(size);
			}
			/**
			 * 线程休眠1秒
			 */
			t.sleep(1000);
			/**
			 * 关闭流
			 */
			os.close();
			is.close();
			/**
			 * 复制完了退出程序
			 */
			if (dpb.getValue() == length) {
                dlg.setVisible(false);
                System.exit(0);
            }
		} catch (FileNotFoundException e) {
			log.error("文件找不到");
		} catch (IOException e) {
			log.error("IO异常");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}