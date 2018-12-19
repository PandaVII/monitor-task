package com.common.utils.constant;

public interface TaskStatus {
	
	
	/**
	 * 任务新增
	 */
	int FAIL = 0;
	
	
	/**
	 * 任务启动
	 */
	int START = 1;
	
	
	/**
	 * 任务主动停止
	 */
	int STOP_BY_PERSON = 2;
	
	/**
	 * 任务非主动停止
	 */
	int STOP_BY_AUTO = 3;
	
	/**
	 * 任务异常
	 */
	int EXCEPTION = 4;
	
}
