package com.common.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveryMinJob implements Job{
	private static Logger log = LoggerFactory.getLogger(EveryMinJob.class);
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info(">>>>>>>>>>c.c.t.EveryMinJob>>>>>>>>>>>start");
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
		log.info(">>>>>>>>>>c.c.t.EveryMinJob>>>>>>>>>>>end");
	}

}
