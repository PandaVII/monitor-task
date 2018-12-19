package com.common.task;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class EveryMinJob implements Job{
	private Logger log = Logger.getLogger("taskLog");
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String aa= null;
		log.info(">>>>>>>>>>c.c.t.EveryMinJob>>>>>>>>>>>start");
		try {
			System.out.println(aa.equals("aaa"));
			Thread.sleep(5000);
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
		log.info(">>>>>>>>>>c.c.t.EveryMinJob>>>>>>>>>>>end");
	}

}
