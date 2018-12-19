package com.common.utils.constant;

public interface TaskAlarmStatus {

	int DEFAULT=0;
	
	int LOCK = -1;
	
	
	/**
	 * 无需告警
	 */
	int NO_NEED_ALARM=1;
	
	
	/**
	 * 邮件发送成功
	 */
	int ALARM_SUCCESS=2;
	
	/**
	 * 邮件发送失败
	 */
	int ALARAM_FAIL=3;
	
	
}
