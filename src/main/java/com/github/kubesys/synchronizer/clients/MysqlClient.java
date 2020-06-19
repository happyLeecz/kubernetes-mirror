/*

 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer.clients;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 1.2.0
 * @since   2020/4/23
 *
 */
public class MysqlClient {

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
	public boolean hasDatabase(String name) throws Exception {
		String sql = "SELECT * FROM information_schema.SCHEMATA where SCHEMA_NAME='" + name + "'";
		return query(null, sql);
	}
	
	/**
	 * create database
	 * 
	 * @throws Exception mysql exception
	 */
	public boolean createDatabase(String name) throws Exception {
		String sql = "CREATE DATABASE " + name;
		return exec(null, sql);
	}
	
	/**
	 * @return delete database
	 * @throws Exception mysql exception
	 */
	public boolean dropDatabase(String name) throws Exception {
		String sql = "DROP DATABASE " + name;
		return exec(null, sql);
	}
	
	/**
	 * @param name  class name
	 * @return true if the table exists, otherwise return false
	 * @throws Exception mysql exception
	 */
	public boolean hasTable(String dbName, String tableName) throws Exception {
		String sql = "SELECT DISTINCT t.table_name, n.SCHEMA_NAME FROM "
				+ "information_schema.TABLES t, information_schema.SCHEMATA n "
				+ "WHERE t.table_name = '" + tableName + "' AND n.SCHEMA_NAME = '" + dbName + "'";
		return query(dbName, sql);
	}

	/**
	 * @param clazz class
	 * @return sql
	 * @throws Exception mysql exception
	 */
	public  boolean createTable(String dbName, String tableName) throws Exception {
		String sql = "CREATE TABLE " + tableName + " (name varchar(250), namespace varchar(250), data json, valid boolean DEFAULT TRUE, primary key(name))";
		return exec(dbName, sql);
	}
	
	/**
	 * @param clazz class
	 * @return sql
	 * @throws Exception mysql exception
	 */
	public  boolean dropTable(String dbName, String tableName) throws Exception {
		String sql = "DROP TABLE " + tableName;
		return exec(dbName, sql);
	}
	
	public synchronized  boolean query(String dbName, String sql) throws Exception {
		if (dbName != null) {
			conn.setCatalog(dbName);
		}
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		boolean success = rs.next();
		rs.close();
		pstmt.close();
		return success;
	}
	
	public synchronized boolean exec(String dbName, String sql) throws Exception {
		try {
			if (dbName != null) {
				conn.setCatalog(dbName);
			}
			PreparedStatement pstmt = conn.prepareStatement(sql); 
			boolean success = pstmt.execute();
			pstmt.close();
			return success;
		} catch (Exception ex) {
			throw new Exception(sql + ";" + ex);
		}
	}
}
