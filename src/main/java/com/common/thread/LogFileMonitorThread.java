package com.common.thread;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.utils.FileUpload;
import com.common.utils.PropertiesUtil;


public class LogFileMonitorThread {
	
	private static Logger logger = LoggerFactory.getLogger(LogFileMonitorThread.class);

	private static PropertiesUtil prop = PropertiesUtil.getInstance();
    private static LogFileMonitorThread instance = new LogFileMonitorThread();
    public static LogFileMonitorThread getInstance(){
        return instance;
    }

    private Thread localThread;
    private volatile boolean toStop = false;
    
    public void start(){
    	
    	localThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while(!toStop){
					File logDir = new File(prop.getProperty("logPath"));
					if(!logDir.exists()){
						logDir.mkdirs();
					}
					File[] childDirs = logDir.listFiles();
					if (childDirs!=null && childDirs.length>0) {
						
						FileUpload upload = new FileUpload();
                        for (File childFile: childDirs) {
                        	
                        	if(childFile.isDirectory())continue;
                        	if(upload.upload(childFile.getAbsolutePath(), prop.getProperty("uploadURL"),prop.getProperty("nodeIP"))){
                        		childFile.delete();
                        	}
                        	
                        }
                    }
					
					
					try {
						TimeUnit.DAYS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
    	});
//    	localThread.setDaemon(true);
        localThread.start();
    	
    }
    
    public void toStop() {
        toStop = true;

        if (localThread == null) {
            return;
        }

        // interrupt and wait
        localThread.interrupt();
        try {
            localThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
	
    public static void main(String[] args) {
		LogFileMonitorThread.getInstance().start();
	}
}
