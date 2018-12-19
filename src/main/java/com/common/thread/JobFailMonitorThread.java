package com.common.thread;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.common.utils.JDBCUtil;
import com.common.utils.Mail;
import com.common.utils.PropertiesUtil;
import com.common.utils.constant.TaskStatus;
import com.common.utils.constant.TaskAlarmStatus;

/**
 * job monitor instance
 *
 * @author
 */
public class JobFailMonitorThread {
	private static Logger logger = Logger.getLogger(JobFailMonitorThread.class);
	
	
	private static String QUERY_TSM_TASK_LOG_FAIL = " SELECT * FROM TSM_TASK_LOG WHERE ALARM_STATUS in(?,?) order by id desc limit 100 ";
	private static String UPDATE_TSM_TASK_LOG_LOCK = "UPDATE TSM_TASK_LOG SET ALARM_STATUS = ? WHERE ID = ?";
	
	private Mail mail = new Mail();
	private static PropertiesUtil prop = PropertiesUtil.getInstance();
	private static JobFailMonitorThread instance = new JobFailMonitorThread();
	public static JobFailMonitorThread getInstance(){
		return instance;
	}

	// ---------------------- monitor ----------------------

	private Thread monitorThread;
	private volatile boolean toStop = false;
	public void start(){
		monitorThread = new Thread(new Runnable() {

			@Override
			public void run() {

				// monitor
				while (!toStop) {
					try {
							//查询任务日志列表
							List<Map<String,Object>> list = JDBCUtil.query(QUERY_TSM_TASK_LOG_FAIL,TaskAlarmStatus.DEFAULT,TaskAlarmStatus.ALARAM_FAIL);//查询自动暂停的任务、异常、失败
							System.out.println("异常日志数||||||>>>>"+list.size());
							for (Map<String, Object> map : list) {
								String taskLogID = map.get("ID").toString();
								List<Map<String, Object>> taskList = JDBCUtil.query(" SELECT * FROM TSM_TASK_INFO WHERE ID = ? ",map.get("TASK_ID"));
								Map<String, Object> taskMap = taskList.get(0);//获取对应任务
								String alarmEmail = map.get("TASK_ALARM_EMAIL") == null ? prop.getProperty("JobFailMonitor.DEFAULT_AlARM_EMAIL"):String.valueOf(map.get("TASK_ALARM_EMAIL"));
//								int alarmStatus = Integer.valueOf(String.valueOf(map.get("ALARM_STATUS")));
								int retryCount = Integer.valueOf(String.valueOf(taskMap.get("TASK_RETRY_COUNT")));
								
								
								//告警状态锁定
								int ret = JDBCUtil.update(UPDATE_TSM_TASK_LOG_LOCK, TaskAlarmStatus.LOCK ,taskLogID);
								if(ret < 1){
									continue;
								}
								
								Integer countConfig = Integer.valueOf(prop.getProperty("JobFailMonitor.RETRY_COUNT"));
								int newAlarmStatus = 0;
								if("1".equals(taskMap.get("TASK_MON").toString()) && alarmEmail != null && retryCount == countConfig ){
									boolean failAlarm = failAlarm(taskMap,map,alarmEmail);
									newAlarmStatus = failAlarm?TaskAlarmStatus.ALARM_SUCCESS:TaskAlarmStatus.ALARAM_FAIL;
								}else{
									newAlarmStatus = 1;
								}
								
								//更新改日志邮件告警状态
								JDBCUtil.update(" UPDATE TSM_TASK_LOG SET ALARM_STATUS = ? WHERE ID= ? ", newAlarmStatus,taskLogID);
							}

//						TimeUnit.SECONDS.sleep(1);
						TimeUnit.MINUTES.sleep(1); //1分钟运行一次
					}catch (InterruptedException e) {
						logger.info(MessageFormat.format("job monitor {0},{1}", JobFailMonitorThread.class,"已中断"));
					}catch (Exception e) {
						logger.error("job monitor error:{}", e);
					}
				}

			}
		});
		monitorThread.setDaemon(true);
		monitorThread.start();
	}

	public void toStop(){
		toStop = true;
		// interrupt and wait
		monitorThread.interrupt();
		try {
			monitorThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}


	// ---------------------- alarm ----------------------

	// email alarm template
	private static final String mailBodyTemplate = "<h5>" + prop.getProperty("JobFailMonitor.ALARM_EMAIL_TITLE") + "：</span>" + //monitor_detail
			"<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
			"   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
			"      <tr>\n" +
			"         <td width=\"15%\" >"+ "任务ID" +"</td>\n" +	//field_jobgroup
			"         <td width=\"20%\" >"+ "任务名称" +"</td>\n" +	//field_id
			"         <td width=\"20%\" >"+ "任务类名" +"</td>\n" +	//field_jobdesc
			"         <td width=\"40%\" >"+ "任务异常信息" +"</td>\n" +	//alarm_title
			"      </tr>\n" +
			"   </thead>\n" +
			"	<tbody>\n" +
			"      <tr>\n" +
			"         <td>{0}</td>\n" +
			"         <td>{1}</td>\n" +
			"         <td>{2}</td>\n" +
			"         <td>{3}</td>\n" +
			"      </tr>\n" +
			"   </tbody>\n" +
			"</table>";
	
	/**
	 * 发送任务失败告警邮件
	 * @param map
	 * @param alarmEmail
	 * @return
	 */
	private boolean failAlarm(Map<String,Object> map, Map<String,Object> logmap,String alarmEmail){
		String content = MessageFormat.format(mailBodyTemplate,
				map.get("ID"),
				map.get("TASK_NAME"),
				map.get("TASK_CLAZZ"),
				logmap.get("TASK_ERROR_MSG"));
		mail.setMail(prop.getProperty("JobFailMonitor.ALARM_EMAIL_TITLE"),"pm_dev@yihuacomputer.com", 
				alarmEmail, content);
		return mail.sendout();
	}
			
			
}
