package cn.leeffee.feige.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileMD5 {

	protected static char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};  
    protected static MessageDigest messageDigest = null;  
    static{  
        try{  
            messageDigest = MessageDigest.getInstance("MD5");  
        }catch (NoSuchAlgorithmException e) {  
            System.err.println(FileMD5.class.getName()+"初始化失败，MessageDigest不支持MD5Util.");  
            e.printStackTrace();  
        }  
    }  
      
    /**
     * 计算文件的MD5
     * @param fileName 文件的绝对路径
     * @return
     * @throws IOException
     */  
    public static String getFileMD5String(String fileName) throws IOException{  
        File f = new File(fileName);  
        return getFileMD5String(f);  
    }  
      
    /**
     * 计算文件的MD5，重载方法
     * @param file 文件对象
     * @return
     * @throws IOException
     */  
    public static String getFileMD5String(File file){  
    	FileInputStream in = null;
        byte[] buffer = new byte[8192];
        int length = 0;
        try {
        	in = new FileInputStream(file);
        	while( (length = in.read(buffer, 0, buffer.length)) != -1){
        		messageDigest.update(buffer, 0, length);
        	}
        	return bufferToHex(messageDigest.digest());  
		} catch (Exception e) {
			return null;
		}finally{
			if (in != null){
				try {
					in.close();
					in = null;
				} catch (Exception e3) {
				}
			}
		}  
    } 
    
    /**
     * 分块算文件MD5，解决大文件传输问题
     * 算法：如果文件小于100M，直接算MD5；如果文件大于100M,获取该文件的前16M，中间16M，最后16M算出一个MD5值。
     * 特别说明：大于100M的文件一般都是二进制文件，中间修改一个字节的场景很少，因此此算法能处理绝大部分情况
     * @param file
     * @return
     */
    public static String getFileMD5ByBlock(File file){
    	FileInputStream in = null;
        int length = 0;
        try {
        	long fileSize = file.length();
        	in = new FileInputStream(file);
        	if (fileSize > 1024*1024*100) {
    			int each = 1024 * 1024 * 16;
    			byte[] buffer2 = new byte[each];
    			int len = 0;
    			len = in.read(buffer2, 0, each);
    			messageDigest.update(buffer2, 0, len);
    			long x1 = fileSize>>2;
    			long skip1 = x1-each;
    			in.skip(skip1);
    			len = in.read(buffer2, 0, each);
    			messageDigest.update(buffer2, 0, len);
    			long x2 = fileSize - each;
    			long skip2 = x2 - x1 - each;
    			in.skip(skip2);
    			len = in.read(buffer2, 0, each);
    			messageDigest.update(buffer2, 0, len);
    			return bufferToHex(messageDigest.digest());
    		} else {
    			byte[] buffer = new byte[8192];
    			while( (length = in.read(buffer, 0, buffer.length)) != -1){
    				messageDigest.update(buffer, 0, length);
    			}
    			return bufferToHex(messageDigest.digest());  
    		}
		} catch (Exception e) {
			return null;
		}finally{
			if (in != null){
				try {
					in.close();
					in = null;
				} catch (Exception e3) {
				}
			}
		}  
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
       char c0 = hexDigits[(bt & 0xf0) >> 4];  
       char c1 = hexDigits[bt & 0xf];  
       stringbuffer.append(c0);  
       stringbuffer.append(c1);  
    }  
      
    public static void main(String[] args) throws IOException {  
        String fileName = "F:\\test.txt";  
        long start = System.currentTimeMillis();  
        System.out.println("md5:"+getFileMD5String(fileName));  
        long end = System.currentTimeMillis();  
        System.out.println("Consume " + (end - start) + "ms");  
    }  

}
