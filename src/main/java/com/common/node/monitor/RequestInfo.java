package com.common.node.monitor;

import java.io.Serializable;
import java.util.HashMap;
 
public class RequestInfo implements Serializable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -1632033769067213882L;
	private String ip ;
	private HashMap<String, Object> cpuPercMap ;
	private HashMap<String, Object> memoryMap;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public HashMap<String, Object> getCpuPercMap() {
		return cpuPercMap;
	}
	public void setCpuPercMap(HashMap<String, Object> cpuPercMap) {
		this.cpuPercMap = cpuPercMap;
	}
	public HashMap<String, Object> getMemoryMap() {
		return memoryMap;
	}
	public void setMemoryMap(HashMap<String, Object> memoryMap) {
		this.memoryMap = memoryMap;
	}
	
}
