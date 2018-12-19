package com.common.listener;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorTriggerListener implements TriggerListener{

	private final static Logger logger=LoggerFactory.getLogger(MonitorTriggerListener.class);
	
	public String getName() {
		return "MonitorTriggerListener";
	}

	public void triggerFired(Trigger trigger, JobExecutionContext context) {
		// TODO Auto-generated method stub
		
	}

	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	public void triggerMisfired(Trigger trigger) {
		// TODO Auto-generated method stub
		
	}

	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			CompletedExecutionInstruction triggerInstructionCode) {
		String name = trigger.getJobKey().getName();
		
		
		long time = context.getJobRunTime();
		Date start = context.getFireTime();
		logger.info("TriggerListener类   >>类名:{};开始时间：{};运行时间:{}", name,start,time);
		
		
	}
	
	

}
