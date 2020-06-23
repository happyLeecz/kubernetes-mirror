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
public class Synchronizer {
	
	public static final Logger m_logger = Logger.getLogger(Synchronizer.class.getName());

	public static final String LABEL_DATABASE   = "#DATBASE#";
	
	public static final String LABEL_TABLE      = "#TABLE#";
	
	public static final String LABEL_NAME       = "#NAME#";
	
	public static final String LABEL_NAMESPACE  = "#NAMESPACE#";
	
	public static final String LABEL_JSON       = "#JSON#";
	
	public static final String CHECK_DATABASE  = "SELECT * FROM information_schema.SCHEMATA where SCHEMA_NAME='#DATBASE#'";
	
	public static final String CREATE_DATABASE = "CREATE DATABASE #DATBASE#";
	
	public static final String DELETE_DATABASE = "DROP DATABASE #DATBASE#";
	
	
	public static final String INSERT_OBJECT   = "INSERT INTO #TABLE# VALUES ('#NAME#', '#NAMESPACE#', '#JSON#')";
	
	public static final String UPDATE_OBJECT   = "UPDATE #TABLE# SET data = '#JSON#' WHERE name = '#NAME#' and namespace = '#NAMESPACE#'";
	
	public static final String DELETE_OBJECT   = "DELETE FROM #TABLE# WHERE name = '#NAME#' and namespace = '#NAMESPACE#'";
	
	
	public static final String CHECK_TABLE     = "SELECT DISTINCT t.table_name, n.SCHEMA_NAME FROM "
			+ "information_schema.TABLES t, information_schema.SCHEMATA n "
			+ "WHERE t.table_name = '#TABLE#' AND n.SCHEMA_NAME = '#DATBASE#'";
	
	public static final String CREATE_TABLE    = "CREATE TABLE #TABLE# (name varchar(250), namespace varchar(250), data json, primary key(name, namespace))";
	
	public static final String DELETE_TABLE    = "DROP TABLE #TABLE#";
	/**
	 * conn
	 */
	protected final Connection conn;

	public Synchronizer(Connection conn) {
		super();
		this.conn = conn;
	}
	
	/**
	 * @param name                db name
	 * @return                    true or false
	 * @throws Exception          exception
	 */
	public synchronized boolean hasDatabase(String name) throws Exception {
		return execWithResult(null, CHECK_DATABASE.replace(LABEL_DATABASE, name));
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
		if (!hasDatabase(dbName)) {
			return false;
		}
		return execWithResult(dbName, CHECK_TABLE.replace(LABEL_DATABASE, dbName)
											.replace(LABEL_TABLE, tableName));
	}

	/**
	 * @param clazz class
	 * @return sql
	 * @throws Exception mysql exception
	 */
	public synchronized boolean createTable(String dbName, String tableName) throws Exception {
		if (!hasDatabase(dbName)) {
			createDatabase(dbName);
		}
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
	 * @param table                                  table
	 * @param name                                   name
	 * @param namespace                              namespace
	 * @param json                                   json
	 * @return                                       true or false
	 * @throws Exception                             exception
	 */
	public boolean insertObject(String table, String name, String namespace, String json) throws Exception {
		if(!exec(Constants.DB, INSERT_OBJECT
					.replace(LABEL_TABLE, table)
					.replace(LABEL_NAME, name)
					.replace(LABEL_NAMESPACE, namespace)
					.replace(LABEL_JSON, getNormalJSON(json)))) {
			return updateObject(table, name, namespace, json);
		}
		return true;
	}
	
	/**
	 * @param table                                  table
	 * @param name                                   name
	 * @param namespace                              namespace
	 * @param json                                   json
	 * @return                                       true or false
	 * @throws Exception                             exception
	 */
	public boolean updateObject(String table, String name, String namespace, String json) throws Exception {
		return exec(Constants.DB, UPDATE_OBJECT
					.replace(LABEL_TABLE, table)
					.replace(LABEL_NAME, name)
					.replace(LABEL_NAMESPACE, namespace)
					.replace(LABEL_JSON, getNormalJSON(json)));		
	}
	
	/**
	 * @param table                                  table
	 * @param name                                   name
	 * @param namespace                              namespace
	 * @param json                                   json
	 * @return                                       true or false
	 * @throws Exception                             exception
	 */
	public boolean deleteObject(String table, String name, String namespace, String json) throws Exception {
		return exec(Constants.DB, DELETE_OBJECT
					.replace(LABEL_TABLE, table)
					.replace(LABEL_NAME, name)
					.replace(LABEL_NAMESPACE, namespace)
					.replace(LABEL_JSON, getNormalJSON(json)));		
	}
	
	public String getNormalJSON(String json) {
		return json.replaceAll("&&", "\\\\u0026\\\\u0026")
				.replaceAll(">", "\\\\u003e")
				.replaceAll("\\'", "\\\\'")
//				.replaceAll("\\'", "\\\\\\\\'")
				.replaceAll("\\\\n", "\\\\\\\\\\n")
				.replaceAll("\\\\\"", "\\\\\\\\\\\\\"");
	}
	
	/**
	 * @param dbName                          dbName
	 * @param sql                             sql
	 * @return                                true or false
	 * @throws Exception                      exception
	 */
	public boolean execWithResult(String dbName, String sql) throws Exception {
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
