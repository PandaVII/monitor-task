package com.common.thread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.utils.JDBCUtil;
import com.common.utils.PropertiesUtil;
import com.common.utils.QuartzPlugin;
import com.common.utils.constant.NodeStatus;

/**
 * 节点监控 
 * 节点定时更新：状态、时间、上传状态信息
 *
 */
public class NodeMonitorThread {
	private static Logger logger = LoggerFactory.getLogger(NodeMonitorThread.class);
	
	private static PropertiesUtil prop = PropertiesUtil.getInstance();
	private static NodeMonitorThread instance = new NodeMonitorThread();
	private String nodeIP = prop.getProperty("nodeIP");
	private String nodeName = prop.getProperty("nodeName");
	public static NodeMonitorThread getInstance(){
		return instance;
	}
	
	private Thread monitorThread;
	private volatile boolean toStop = false;
	public void start(){
		monitorThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!toStop) {
					try {
						List<Map<String,Object>> list = JDBCUtil.query(" select * from TSM_NODE_INFO where NODE_NAME= ? and IP_ADDRESS= ? ", nodeName,nodeIP);
						for (Map<String, Object> map : list) {
							int nodeStatus= Integer.valueOf(map.get("NODE_STATUS").toString());
							//节点运行状态控制
							controlNode(nodeStatus,map);
							
							//更新时间、上传日志
							if(nodeStatus != NodeStatus.STOP){
								JDBCUtil.update(" update TSM_NODE_INFO set UPDATE_TIME = now() WHERE ID=?",map.get("ID"));
								//上传节点信息后续完成
								
							}
							
							
						}
						TimeUnit.SECONDS.sleep(5);
					}catch (InterruptedException e) {
						logger.info("线程：{},已中断",NodeMonitorThread.class);
					} catch (Exception e) {
						logger.error("job monitor error:{}", e);
					}
					
				}
			}
			
		});
		monitorThread.start();
	}
	
	public void toStop(){
		toStop = true;
		monitorThread.interrupt();
		try {
			monitorThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 控制节点启动、停止
	 * @param status
	 */
	private void controlNode(int status,Map<String, Object> map){
		switch (status) {
		case NodeStatus.START:
			if(QuartzPlugin.scheduler == null){
				QuartzPlugin.start();
			    JobFailMonitorThread.getInstance().start(); // 启动任务失败邮件监控线程
			    uploadNodeLog(map, "节点启动");
			}
			break;
		case NodeStatus.STOP:
			if(QuartzPlugin.scheduler != null){
				JobFailMonitorThread.getInstance().toStop();
				QuartzPlugin.stop();
				uploadNodeLog(map, "节点停止");
			}
			break;
		default:
			break;
		}
	}
	
	private void uploadNodeLog(Map<String, Object> map,String msg){
		JDBCUtil.update(" insert into TSM_NODE_LOG (NODE_ID,NODE_NAME,NODE_CPU,NODE_MEM,NODE_OPER_MSG,UPDATE_TIME)"
				+ " VALUES(?,?,?,?,?,now()) ", map.get("ID"),map.get("NODE_NAME"),"40%","50%",msg);
		
	}

}
