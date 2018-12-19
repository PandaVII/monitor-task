package com.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


public class PropertiesUtil extends Properties{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2742497912500366976L;
	
	public Logger log = Logger.getLogger(PropertiesUtil.class);
	
	private static PropertiesUtil instance;
	
	public static PropertiesUtil getInstance(){
		if(instance !=null){
			return instance;
		}else {
			instance = new PropertiesUtil();
		}
		return instance;
	}
	
	private PropertiesUtil() {
		InputStream in = null;
		try {
			in = this.getClass().getResourceAsStream("/config.properties");
			this.load(in);
		} catch (IOException e) {
			log.error("加载配置文件失败", e);
		}finally{
			if(in !=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
}
