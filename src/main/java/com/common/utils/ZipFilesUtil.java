package com.common.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
//定时将指定文件夹下的所有文件压缩
public class ZipFilesUtil {
	private static Logger log = LoggerFactory.getLogger(ZipFilesUtil.class);
	
	
	//将指定文件夹下的所有文件(除压缩文件除外)压缩，该文件夹下没有子文件夹，否则需递归进行处理
	//压缩文件名为日期名
	public static void zipFiles(String sourceFilePath){
		
		
		//判断是否有指定文件夹
		File sourceFile = new File(sourceFilePath);
		if(!sourceFile.exists())
		{
			//tmpFile.mkdirs();
			log.info("待压缩的文件目录："+sourceFile+"不存在");
			return;
		}
 
 
		String zipName =  generateId();//生产压缩文件名
		File zipFile = new File(sourceFile+File.separator+zipName+".zip");
		File[] sourceFiles = sourceFile.listFiles();
		if(null == sourceFiles || sourceFiles.length<1){
			log.info("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
			return;
		}
		
		
		BufferedInputStream bis = null;  
		ZipOutputStream zos = null; 
		byte[] bufs = new byte[1024*10];
		FileInputStream fis = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(zipFile));
			for(int i=0; i<sourceFiles.length; i++){
				//创建zip实体，并添加进压缩包
				String tmpFileName = sourceFiles[i].getName();
				if(tmpFileName.endsWith(".zip"))
					continue;
				ZipEntry zipEntry = new ZipEntry(tmpFileName);
				zos.putNextEntry(zipEntry);	
				//读取待压缩的文件并写进压缩包里
				fis = new FileInputStream(sourceFiles[i]);
				bis = new BufferedInputStream(fis, 1024*10);
				int read = 0;
				while((read=bis.read(bufs, 0, 1024*10))!=-1){
					zos.write(bufs, 0, read);
				}
				fis.close();//很重要，否则删除不掉!
				sourceFiles[i].delete();//将要进行压缩的源文件删除
			}//end for
			log.debug("文件打包成功！");
			
		} catch (IOException e) {
			log.error("文件打包失败",e);
		} finally{
			//关闭流
			try {
				if(null!=bis)
					bis.close();
				if(null!=zos)
					zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		
	}
	
 
	//删除指定文件夹下的压缩文件
	public static void deleteZipFiles(String filePath) throws ParseException{
		File delFile = new File(filePath);
		if(!delFile.exists())
		{
			//tmpFile.mkdirs();
			log.info("待删除的文件目录："+delFile+"不存在");
			return;
			
		}
		
		File[] delFiles = delFile.listFiles();
		if(null == delFiles || delFiles.length<1){
			log.info(filePath+"下没有要删除的文件.");
			return;
		}
		
		
		//收集压缩文件，过滤掉非压缩的文件以及文件夹
		List<File> delFilesTarget = new ArrayList<File>();
		for(int i=0; i<delFiles.length; i++){
			String tmpFileName = delFiles[i].getName();
			if(tmpFileName.endsWith(".zip"))//是压缩文件
				delFilesTarget.add(delFiles[i]);
		}
		
		orderByNameDate(delFilesTarget);//按文件名的日期排序（倒序）
 
		//计算文件大小总量，然后检查总量是否超过阈值（100KB）。
		//如果超过，则不断删除最老的文件，直至文件总量不再超过阈值
		long len = 0;
		for(int i=0; i<delFilesTarget.size(); i++){
			len += delFilesTarget.get(i).length();//返回字节数
		}
		int threshold = 100000;//100KB，阈值
		int lastIndex = delFilesTarget.size()-1;
		while(len>threshold){
			File delF = delFilesTarget.remove(lastIndex);
			len -= delF.length();
			lastIndex -= 1;
			
			if(!delF.delete()){
				log.info("文件"+delF.getName()+"删除失败！");
			}else{
				log.info("文件"+delF.getName()+"删除成功！");
			}
			
		}
 
	}
	
	//以日期生成文件名
	public static String generateId(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//设置日期格式
		String dateStr = df.format(new Date());//new Date()为获取当前系统时间
		return dateStr;
	}
	
	//按文件名的日期排序（倒序）
	public static void orderByNameDate(List<File> files){
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		Collections.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				// TODO Auto-generated method stub
				try {
					Date f1Date = sdf.parse(f1.getName());
					Date f2Date = sdf.parse(f2.getName());
					//return f1Date.compareTo(f2Date);//正序
					return f2Date.compareTo(f1Date);//逆序
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return 0;
			}
			
		});
	}
	
}