package com.common.listener;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.utils.JDBCUtil;
import com.common.utils.QuartzPlugin;
import com.common.utils.constant.TaskStatus;

public class MonitorJobListener implements JobListener{

	private static Logger logger = LoggerFactory.getLogger(MonitorJobListener.class);
	
	@Override
	public String getName() {
		return "MonitorJobListener";
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context,
			JobExecutionException jobException) {
		
		if(jobException != null){
			String jobKey = context.getJobDetail().getKey().getName();
			List<Map<String,Object>> list = JDBCUtil.query(" select l.* from tsm_task_log l LEFT JOIN tsm_task_info t "+ 
					" on t.id = l.task_id where l.alarm_status = 0 "+
					" and l.UPDATE_TIME > date_sub(now(), interval 20 MINUTE) "+
					" and t.TASK_CLAZZ = ? ",jobKey);
			if(list.size() == 0){  //防止记录多次重复错误日志
				QuartzPlugin.insertErrorLog(jobKey, TaskStatus.START, jobException.getMessage());
				logger.error(jobKey+"-任务异常  ",jobException);
			}
		}
		
	}

}
