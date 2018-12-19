package com.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class FileUpload {

	
	public void upload(String file,String url){
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		try {
		    httpclient = HttpClients.createDefault();
		    HttpPost post = new HttpPost(url);
		    HttpEntity data = getMultiDefaultFileEntity(file);
		    post.setEntity(data);
		    response = httpclient.execute(post);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
			try {
		        if (response != null) response.close();
		        if (httpclient != null) httpclient.close();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		}
   }
	
	/**
	 * File文件格式上传（缺省）
	*/
	public HttpEntity getMultiDefaultFileEntity(String path) {
		File file = new File(path);
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	    builder.setMode(HttpMultipartMode.RFC6532); //以浏览器兼容方式 防止文字乱码
	    builder.addBinaryBody("file", file);
//	    builder.setCharset(Charset.forName("utf-8"));
	    return builder.build();
	}


	public static void main(String[] args) {
		String url = "http://localhost:82/demo/upload";
		new FileUpload().upload("D:\\日志\\2018四季度\\2018-12-4\\客调支援数据.png", url);
	}
	
}
