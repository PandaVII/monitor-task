package com.common.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.utils.JDBCUtil;
import com.common.utils.QuartzPlugin;
import com.common.utils.constant.TaskStatus;

public class MainJob implements Job{
	private static Logger log = LoggerFactory.getLogger(MainJob.class);
	
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		Scheduler scheduler = context.getScheduler();
		
		//任务控制
		taskControl(scheduler);
		
		
	}
	
	
	
	/**
	 * 任务控制主流程
	 * 1.轮询任务状态
	 * 2.只执行变化到对应的状态
	 * @param scheduler
	 */
	private void taskControl(Scheduler scheduler){
		Long   jobId	= null;
		String taskName =null;
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		//主业务流程控制
		try {
			String sql = "select * from TSM_TASK_INFO where TASK_STATUS != -1 ";
			conn = JDBCUtil.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while(rs.next()){
				jobId	= rs.getLong("ID");
				taskName = rs.getString("TASK_NAME");
				String clazz = rs.getString("TASK_CLAZZ");
				String exp = rs.getString("TASK_EXP");
				String jobGroup = rs.getString("TASK_GROUP");
				Integer status = rs.getInt("TASK_STATUS"); //获取数据库状态
				Integer retryCount = rs.getInt("TASK_RETRY_COUNT");
				int triggerStatus = QuartzPlugin.getStatus(clazz, jobGroup,retryCount); //获取任务实际执行状态
				if (status == triggerStatus) continue;
				if(triggerStatus == -1){ //未加载情况
					QuartzPlugin.init(clazz, exp, status , jobGroup);
					continue;
				}
				switch (status) {
				case TaskStatus.START:
						log.info(String.format(">>>>>>>>>>> 任务开始, jobId:{%s},taskName:{%s}", jobId,taskName));
						QuartzPlugin.start(clazz, jobGroup);
					break;
				case TaskStatus.STOP_BY_AUTO :
						log.info(String.format(">>>>>>>>>>> 任务自动暂停, jobId:{%s},taskName:{%s}", jobId,taskName));
				case TaskStatus.STOP_BY_PERSON :
						log.info(String.format(">>>>>>>>>>> 任务结束, jobId:{%s},taskName:{%s}", jobId,taskName));
						QuartzPlugin.stop( clazz, jobGroup);
					break;
				default:
					break;
				}
			}
			
		} catch (SQLException e) {
			log.error("执行主业务流程-获取任务出错！",e);
		} catch (SchedulerException e) {
			log.error("执行主业务流程  获取任务运行状态出错！",e);
		}finally{
			JDBCUtil.release(conn, st, rs);
		}
	}
	
	
	
}
