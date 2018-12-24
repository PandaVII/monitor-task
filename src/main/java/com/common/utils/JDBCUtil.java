package com.common.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 
 * 获取数据库连接
 * 
 * @author yangfan
 *
 */
public class JDBCUtil {

	private static Logger log = LoggerFactory.getLogger(JDBCUtil.class);
	
	private static JDBCpool pool = new JDBCpool();
    
    /**
    * @Method: getConnection
    * @Description: 从数据库连接池中获取数据库连接对象
    * @return Connection数据库连接对象
    * @throws SQLException
    */ 
    public static Connection getConnection() throws SQLException{
        return pool.getConnection();
    }
	
	/**
    * @Method: release
    * @Description: 释放资源，
    * 释放的资源包括Connection数据库连接对象，负责执行SQL命令的Statement对象，存储查询结果的ResultSet对象
    * @param conn
    * @param st
    * @param rs
    */ 
    public static void release(Connection conn,Statement st,ResultSet rs){
        if(rs!=null){
            try{
                //关闭存储查询结果的ResultSet对象
                rs.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
            rs = null;
        }
        if(st!=null){
            try{
                //关闭负责执行SQL命令的Statement对象
                st.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if(conn!=null){
            try{
                //关闭Connection数据库连接对象
                conn.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	
    
	public static List<Map<String, Object>> query(String sql,Object... o){
		Connection conn = null;
	    PreparedStatement st = null;
	    ResultSet rs = null;
	    List<Map<String, Object>> list = new ArrayList<>();
		try {
			conn = JDBCUtil.getConnection();
			st = conn.prepareStatement(sql);
			handleSql(st, o);
			rs = st.executeQuery();
			ResultSetMetaData md = rs.getMetaData();//获得结果集结构信息,元数据
			int columnCount = md.getColumnCount();   //获得列数 
			while(rs.next()){
				Map<String,Object> rowData = new HashMap<String,Object>();
				for (int i = 1; i <= columnCount; i++) {
					rowData.put(md.getColumnName(i), rs.getObject(i));
				}
				list.add(rowData);
			}
		} catch (SQLException e) {
			log.error("执行查询语句出错!"+sql,e);
		}finally{
			JDBCUtil.release(conn, st, rs);
		}
		return list;
	}
	
	public static int update(String sql,Object... o){
		Connection conn = null;
	    PreparedStatement st = null;
	    ResultSet rs = null;
	    int executeUpdate = 0;
		try {
			conn = JDBCUtil.getConnection();
			st = conn.prepareStatement(sql);
			handleSql(st, o);
			executeUpdate = st.executeUpdate();
		} catch (SQLException e) {
			log.error("执行更新语句出错!"+sql,e);
		}finally{
			JDBCUtil.release(conn, st, rs);
		}
		return executeUpdate;
	}
	
	
	
	/**
	 * 预处理SQL填入参数
	 * @param pst
	 * @param paras
	 * @throws SQLException
	 */
	public static void handleSql(PreparedStatement pst,Object... paras) throws SQLException{
		for (int i=0; i<paras.length; i++) {
			Object value = paras[i];
			if (value instanceof java.util.Date) {
				if (value instanceof java.sql.Date) {
					pst.setDate(i + 1, (java.sql.Date)value);
				} else if (value instanceof java.sql.Timestamp) {
					pst.setTimestamp(i + 1, (java.sql.Timestamp)value);
				} else {
					java.util.Date d = (java.util.Date)value;
					pst.setTimestamp(i + 1, new java.sql.Timestamp(d.getTime()));
				}
			} else {
				pst.setObject(i + 1, value);
			}
		}
	}
	
}
