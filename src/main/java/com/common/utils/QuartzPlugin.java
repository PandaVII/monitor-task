package com.common.utils;

import java.util.Map;
import java.util.Properties;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.listener.MonitorJobListener;
import com.common.listener.MonitorScheduleListener;
import com.common.utils.constant.TaskStatus;

public class QuartzPlugin {
	
	private static Logger log = LoggerFactory.getLogger("taskLog");
	
	public static Scheduler scheduler = null;
	//初始化配置
	private static Properties prop = PropertiesUtil.getInstance();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean init(String className,String cronExp,Integer status,String jobGroup){
		try {
			Class clazz;
			
			clazz = Class.forName(className);
			
			JobDetail job = JobBuilder.newJob(clazz).withIdentity(className, jobGroup).build();
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(className, jobGroup)
									.withSchedule(CronScheduleBuilder.cronSchedule(cronExp)).build();
			
			scheduler.scheduleJob(job, trigger);
			if(status == TaskStatus.STOP_BY_PERSON || status == TaskStatus.STOP_BY_AUTO){
				// 暂停触发
				scheduler.pauseTrigger(trigger.getKey());
			}
		}catch(ClassNotFoundException e){
			log.error("定时任务类"+className+"不存在！", e);
			insertErrorLog(className, TaskStatus.START,"定时任务类"+className+"不存在！" );
			return false;
		} catch (SchedulerException e) {
			log.error("获取Scheduler失败！", e);
			return false;
		}
		return true;
	}

	/**
	 * 启动任务
	 * @param clazz
	 * @param jobGroup
	 */
	public static boolean start(String clazz,String jobGroup){
		try {
			JobKey jobKey = JobKey.jobKey(clazz, jobGroup);
			scheduler.resumeJob(jobKey);
			JDBCUtil.update(" update TSM_TASK_INFO set task_status = ? where task_clazz = ?",TaskStatus.START,clazz);
		} catch (SchedulerException e) {
			log.error("启动任务"+clazz+"失败",e);
			insertErrorLog(clazz,TaskStatus.START,"尝试加载任务失败");
			return false;
		}
		return true;
	}
	
	/**
	 * 暂停任务
	 * @param clazz
	 * @param jobGroup
	 */
	public static boolean stop(String clazz,String jobGroup){
		try {
			JobKey jobKey = JobKey.jobKey(clazz, jobGroup);
			scheduler.pauseJob(jobKey);
			JDBCUtil.update(" update TSM_TASK_INFO set task_status = ? where task_clazz = ?",TaskStatus.STOP_BY_PERSON,clazz);
		} catch (SchedulerException e) {
			log.error("主动暂停任务"+clazz+"失败",e); 
			insertErrorLog(clazz,TaskStatus.STOP_BY_AUTO,"主动暂停任务"+clazz+"失败");
			return false;
		}
		return true;
	}
	
	/**
	 * 开启Quartz
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean start(){
		/**
    	 * 首次运行
    	 * 1.检查是否是第一次运行 任务之前状态是否为正常停止
    	 * 2.启动流程
    	 */
    	String jobClassName = prop.getProperty("QuartzMain.clazz");
    	String jobCronExp = prop.getProperty("QuartzMain.exp");
        //启动主任务
    	StdSchedulerFactory sf = new StdSchedulerFactory();
//    	TriggerListener myListener = new MonitorTriggerListener(); //trigger监听
    	SchedulerListener mcl = new MonitorScheduleListener(); // scheduler监听
    	JobListener mjl = new MonitorJobListener();  //job监听
    	try {
			Scheduler scheduler = sf.getScheduler();
			ListenerManager lm = scheduler.getListenerManager();
//			lm.addTriggerListener(myListener);   
			lm.addSchedulerListener(mcl);
			lm.addJobListener(mjl);
			Class clazz;
			try {
				clazz = Class.forName(jobClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			JobDetail job = JobBuilder.newJob(clazz).withIdentity(jobClassName, "main").build();
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobClassName, "main")
								      .withSchedule(CronScheduleBuilder.cronSchedule(jobCronExp)).build();

			scheduler.scheduleJob(job, trigger);
			scheduler.start();
			QuartzPlugin.scheduler = scheduler;
		} catch (SchedulerException e) {
			log.error("启动主业务流程失败",e);
			return false;
		}
    	return true;
	}
	
	
	
	/**
	 * 停止Quartz
	 */
	public static boolean stop() {
		try {
			scheduler.shutdown();
			QuartzPlugin.scheduler = null;
		} catch (SchedulerException e) {
			log.error("停止Scheduler失败！", e);
			return false;
		}
		return true;
	}
	
	
	/**
	 * 获取任务状态
	 * 
	 * @param scheduler
	 * @param clazz
	 * @param jobGroup
	 * @return
	 * @throws SchedulerException
	 */
	@SuppressWarnings("static-access")
	public static int getStatus(String clazz,String jobGroup,int retryCount) throws SchedulerException {
		TriggerKey triggerKey = TriggerKey.triggerKey(clazz, jobGroup);
		TriggerState triggerState = scheduler.getTriggerState(triggerKey);
		if(triggerState.equals(triggerState.NORMAL)){
			return TaskStatus.START;
		}else if(triggerState.equals(triggerState.PAUSED)){
			if(retryCount == 3)
				return TaskStatus.STOP_BY_AUTO;
			else
				return TaskStatus.STOP_BY_PERSON;
		}else if(triggerState.equals(triggerState.NONE)){
			return -1;
		}else {
			log.info(String.format(">>>>>>>>>>> job trigger state error, clazz:{%s},state:{%s}",clazz,triggerState));
		}
		return -1;
	}
	
	/********************************
	 * 
	 * 抽取类方法
	 * 
	 */
	
	/**
	 * 新增错误日志
	 * @param clazz  类名
	 * @param state  状态区分
	 * @param errorMsg  错误信息
	 */
	public static void insertErrorLog(String clazz,int state ,String errorMsg){
		Map<String, Object> map = JDBCUtil.query(" SELECT * FROM TSM_TASK_INFO WHERE TASK_CLAZZ= ?", clazz).get(0);
		Integer retryCount = Integer.valueOf(map.get("TASK_RETRY_COUNT").toString());
		
		if(state == TaskStatus.START && retryCount < 3){
			JDBCUtil.update(" update TSM_TASK_INFO set TASK_RETRY_COUNT = ? WHERE TASK_CLAZZ = ? ",retryCount+1,clazz);
		}else if(state == TaskStatus.STOP_BY_PERSON || retryCount == 3){
			JDBCUtil.update(" update TSM_TASK_INFO set TASK_STATUS = ? WHERE TASK_CLAZZ = ? ",TaskStatus.STOP_BY_AUTO,clazz);
			JDBCUtil.update(" insert into tsm_task_log (TASK_ID,TASK_NAME,STOP_TIME,UPDATE_TIME,TASK_ERROR_MSG)"
					+ " values (?,?,now(),now(),?)", map.get("ID"),map.get("TASK_NAME"),errorMsg);
		}
	}
	
	
}
