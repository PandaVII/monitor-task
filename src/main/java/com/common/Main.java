package com.common;

import com.common.thread.NodeMonitorThread;

/**
 * Hello world!
 *
 */
public class Main 
{
	
	public static void main( String[] args )
    {
    	// 启动节点运行监控线程
    	NodeMonitorThread.getInstance().start();
    }
}
