package com.tnt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {  
	  
    /** 
     * Ĭ�ϵ������ַ�����ϣ��������ֽ�ת���� 16 ���Ʊ�ʾ���ַ�,apacheУ�����ص��ļ�����ȷ���õľ���Ĭ�ϵ������� 
     */  
    protected static char          hexDigits[]   = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',  
            'd', 'e', 'f'                       };  
    protected static MessageDigest messagedigest = null;  
    static {  
        try {  
            messagedigest = MessageDigest.getInstance("MD5");  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        }  
    }  
  
    public static String getFileMD5String(File file) throws IOException {  
        InputStream fis;  
        fis = new FileInputStream(file);  
        byte[] buffer = new byte[1024];  
        int numRead = 0;  
        while ((numRead = fis.read(buffer)) > 0) {  
            messagedigest.update(buffer, 0, numRead);  
        }  
        fis.close();  
        return bufferToHex(messagedigest.digest());  
    }  
    
    public static String getCodedFileMD5String(File file) throws IOException {  
        InputStream fis;  
        fis = new FileInputStream(file);  
        byte[] buffer = new byte[1024];  
        int numRead = 0;  
        while ((numRead = fis.read(buffer)) > 0) {  
        	
        	for(int i=0;i<numRead;i++){
				int b=0;
				for (int j=0;j<8;j++){
					int bit = (buffer[i]>>j&1)==0?1:0;
					b += (1<<j)*bit;
				}
				buffer[i]=(byte)b;
			}
        	
            messagedigest.update(buffer, 0, numRead);  
        }  
        fis.close();  
        return bufferToHex(messagedigest.digest());  
    }  
  
    private static String bufferToHex(byte bytes[]) {  
        return bufferToHex(bytes, 0, bytes.length);  
    }  
  
    private static String bufferToHex(byte bytes[], int m, int n) {  
        StringBuffer stringbuffer = new StringBuffer(2 * n);  
        int k = m + n;  
        for (int l = m; l < k; l++) {  
            appendHexPair(bytes[l], stringbuffer);  
        }  
        return stringbuffer.toString();  
    }  
  
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {  
        char c0 = hexDigits[(bt & 0xf0) >> 4];// ȡ�ֽ��и� 4 λ������ת��  
        // Ϊ�߼����ƣ�������λһ������,�˴�δ�������ַ����кβ�ͬ  
        char c1 = hexDigits[bt & 0xf];// ȡ�ֽ��е� 4 λ������ת��  
        stringbuffer.append(c0);  
        stringbuffer.append(c1);  
    }  
  
//    public static void main(String[] args) throws IOException {  
//        File file = new File("E:/test/crm_account_YYYY_MM_DD.txt");  
//        String md5 = getFileMD5String(file);  
//        System.out.println("md5:" + md5);  
//    }  
}
