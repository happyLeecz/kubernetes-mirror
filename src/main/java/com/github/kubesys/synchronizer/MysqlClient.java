/*

 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 1.2.0
 * @since   2020/4/23
 *
 */
public class MysqlClient {
	
	public static final Logger m_logger = Logger.getLogger(MysqlClient.class.getName());

	public static final String LABEL_DATABASE   = "#DATBASE#";
	
	public static final String LABEL_TABLE      = "#TABLE#";
	
	public static final String LABEL_NAME       = "#NAME#";
	
	public static final String LABEL_NAMESPACE  = "#NAMESPACE#";
	
	public static final String LABEL_JSON       = "#JSON#";
	
	public static final String CHECK_DATABASE  = "SELECT * FROM information_schema.SCHEMATA where SCHEMA_NAME='#DATBASE#'";
	
	public static final String CREATE_DATABASE = "CREATE DATABASE #DATBASE#";
	
	public static final String DELETE_DATABASE = "DROP DATABASE #DATBASE#";
	
	
	public static final String CHECK_TABLE     = "SELECT DISTINCT t.table_name, n.SCHEMA_NAME FROM "
			+ "information_schema.TABLES t, information_schema.SCHEMATA n "
			+ "WHERE t.table_name = '#TABLE#' AND n.SCHEMA_NAME = '#DATBASE#'";
	
	public static final String CREATE_TABLE    = "CREATE TABLE #TABLE# (name varchar(250), namespace varchar(250), data json, primary key(name, namespace))";
	
	public static final String DELETE_TABLE    = "DROP TABLE #TABLE#";
	/**
	 * conn
	 */
	protected final Connection conn;

	public MysqlClient(Connection conn) {
		super();
		this.conn = conn;
	}
	
	/**
	 * @param name                db name
	 * @return                    true or false
	 * @throws Exception          exception
	 */
	public synchronized boolean hasDatabase(String name) throws Exception {
		return execWithResultCheck(null, CHECK_DATABASE.replace(LABEL_DATABASE, name));
	}
	
	/**
	 * create database
	 * 
	 * @throws Exception mysql exception
	 */
	public synchronized boolean createDatabase(String name) throws Exception {
		return exec(null, CREATE_DATABASE.replace(LABEL_DATABASE, name));
	}
	
	/**
	 * @return delete database
	 * @throws Exception mysql exception
	 */
	public synchronized boolean dropDatabase(String name) throws Exception {
		return exec(null, DELETE_DATABASE.replace(LABEL_DATABASE, name));
	}
	
	/**
	 * @param name  class name
	 * @return true if the table exists, otherwise return false
	 * @throws Exception mysql exception
	 */
	public synchronized boolean hasTable(String dbName, String tableName) throws Exception {
		return execWithResultCheck(dbName, CHECK_TABLE.replace(LABEL_DATABASE, dbName)
											.replace(LABEL_TABLE, tableName));
	}

	/**
	 * @param clazz class
	 * @return sql
	 * @throws Exception mysql exception
	 */
	public synchronized boolean createTable(String dbName, String tableName) throws Exception {
		return exec(dbName, CREATE_TABLE.replace(LABEL_TABLE, tableName));
	}
	
	/**
	 * @param clazz class
	 * @return sql
	 * @throws Exception mysql exception
	 */
	public synchronized boolean dropTable(String dbName, String tableName) throws Exception {
		return exec(dbName, DELETE_TABLE.replace(LABEL_TABLE, tableName));
	}
	
	/**
	 * @param dbName                          dbName
	 * @param sql                             sql
	 * @return                                true or false
	 * @throws Exception                      exception
	 */
	public boolean execWithResultCheck(String dbName, String sql) throws Exception {
		if (dbName != null) {
			conn.setCatalog(dbName);
		}
		
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (Exception ex) {
			return false;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	/**
	 * @param dbName                          dbName
	 * @param sql                             sql
	 * @return                                true or false
	 * @throws Exception                      exception
	 */
	public ResultSet execWithResult(String dbName, String sql) throws Exception {
		if (dbName != null) {
			conn.setCatalog(dbName);
		}
		
		try {
			return conn.prepareStatement(sql).executeQuery();
		} catch (Exception ex) {
			return null;
		} 
	}
	
	/**
	 * @param dbName                          dbName
	 * @param sql                             sql
	 * @return                                true or false
	 * @throws Exception                      exception
	 */
	public boolean exec(String dbName, String sql) throws Exception {
		
		if (dbName != null) {
			conn.setCatalog(dbName);
		}
		
		PreparedStatement pstmt = null;
				
		
		try {
			pstmt = conn.prepareStatement(sql);
			return pstmt.execute();
		} catch (Exception ex) {
			m_logger.severe("caused by " + sql + ":" + ex);
			System.out.println("bug:" + sql);
			return false;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	public Connection getConn() {
		return conn;
	}
	
}
