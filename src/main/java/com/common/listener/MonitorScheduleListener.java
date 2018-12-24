package com.common.listener;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorScheduleListener implements SchedulerListener{

	private final static Logger logger=LoggerFactory.getLogger(MonitorScheduleListener.class);
	
	public void jobScheduled(Trigger trigger) {
		// TODO Auto-generated method stub
		
	}

	public void jobUnscheduled(TriggerKey triggerKey) {
		// TODO Auto-generated method stub
		
	}

	public void triggerFinalized(Trigger trigger) {
		// TODO Auto-generated method stub
		
	}

	public void triggerPaused(TriggerKey triggerKey) {
		// TODO Auto-generated method stub
		
	}

	public void triggersPaused(String triggerGroup) {
		// TODO Auto-generated method stub
		
	}

	public void triggerResumed(TriggerKey triggerKey) {
		// TODO Auto-generated method stub
		
	}

	public void triggersResumed(String triggerGroup) {
		// TODO Auto-generated method stub
		
	}

	public void jobAdded(JobDetail jobDetail) {
		logger.info("添加任务（"+jobDetail.getKey().getName() +"）成功");
	}

	public void jobDeleted(JobKey jobKey) {
		logger.info("删除任务（"+jobKey.getName()+")");
	}

	public void jobPaused(JobKey jobKey) {
		
		logger.info("任务（"+jobKey.getName() +"）已暂停");
	}

	public void jobsPaused(String jobGroup) {
		// TODO Auto-generated method stub
		
	}

	public void jobResumed(JobKey jobKey) {
		logger.info("重新启动任务（"+jobKey.getName()+")");
	}

	public void jobsResumed(String jobGroup) {
		// TODO Auto-generated method stub
		
	}

	public void schedulerError(String msg, SchedulerException cause) {
		logger.error(msg, cause);
	}

	public void schedulerInStandbyMode() {
		// TODO Auto-generated method stub
		
	}

	public void schedulerStarted() {
		logger.info("scheduler启动完成");
		
	}

	public void schedulerStarting() {
		logger.info("scheduler正在启动...");
		
	}

	public void schedulerShutdown() {
		logger.info("scheduler停止结束");
	}

	public void schedulerShuttingdown() {
		logger.info("scheduler正在停止...");
		
	}

	public void schedulingDataCleared() {
		// TODO Auto-generated method stub
		
	}

}
