package com.common.utils;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class FileUpload {

	
	public boolean upload(String file,String url,String nodeIp){
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		int code = 0 ;
		try {
		    httpclient = HttpClients.createDefault();
		    HttpPost post = new HttpPost(url);
		    HttpEntity data = getMultiDefaultFileEntity(file,nodeIp);
		    post.setEntity(data);
		    response = httpclient.execute(post);
		    code = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
		    e.printStackTrace();
		    return false;
		} finally {
			try {
		        if (response != null) response.close();
		        if (httpclient != null) httpclient.close();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		return code == 200 ? true:false;
   }
	
	/**
	 * File文件格式上传（缺省）
	*/
	public HttpEntity getMultiDefaultFileEntity(String path,String ip) {
		File file = new File(path);
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	    builder.setMode(HttpMultipartMode.RFC6532); //以浏览器兼容方式 防止文字乱码
	    builder.addBinaryBody("file", file);
	    builder.addTextBody("ipAddress", ip);
//	    builder.setCharset(Charset.forName("utf-8"));
	    return builder.build();
	}


	public static void main(String[] args) {
		String url = "http://localhost:82/demo/upload";
		new FileUpload().upload("D:\\日志\\2018四季度\\2018-12-4\\客调支援数据.png", url,"10.2.8.52");
	}
	
}
