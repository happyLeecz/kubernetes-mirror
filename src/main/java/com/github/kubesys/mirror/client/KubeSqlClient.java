/*

 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror.client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Logger;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 1.2.0
 * @since   2020/4/23
 *
 */
public class KubeSqlClient {
	
	public static final Logger m_logger = Logger.getLogger(KubeSqlClient.class.getName());

	public static final String DEFAULT_DB       = "kube";
	
	public static final String LABEL_DATABASE   = "#DATBASE#";
	
	public static final String LABEL_TABLE      = "#TABLE#";
	
	public static final String LABEL_NAME       = "#NAME#";
	
	public static final String LABEL_NAMESPACE  = "#NAMESPACE#";
	
	public static final String LABEL_JSON       = "#JSON#";
	
	
	
	
	public static final String CHECK_DATABASE  = "SELECT * FROM information_schema.SCHEMATA where SCHEMA_NAME='#DATBASE#'";
	
	public static final String CREATE_DATABASE = "CREATE DATABASE #DATBASE# CHARACTER SET utf8 COLLATE utf8_general_ci";
	
	public static final String DELETE_DATABASE = "DROP DATABASE #DATBASE#";
	
	
	public static final String CHECK_TABLE     = "SELECT DISTINCT t.table_name, n.SCHEMA_NAME FROM "
			+ "information_schema.TABLES t, information_schema.SCHEMATA n "
			+ "WHERE t.table_name = '#TABLE#' AND n.SCHEMA_NAME = '#DATBASE#'";
	
	public static final String CREATE_TABLE    = "CREATE TABLE #TABLE# (name varchar(512), namespace varchar(128), data json, primary key(name, namespace)) DEFAULT CHARSET=utf8";
	
	public static final String DELETE_TABLE    = "DROP TABLE #TABLE#";
	
	public static final String INSERT_OBJECT   = "INSERT INTO #TABLE# VALUES ('#NAME#', '#NAMESPACE#', '#JSON#')";
	
	public static final String UPDATE_OBJECT   = "UPDATE #TABLE# SET data = '#JSON#' WHERE name = '#NAME#' and namespace = '#NAMESPACE#'";
	
	public static final String DELETE_OBJECT   = "DELETE FROM #TABLE# WHERE name = '#NAME#' and namespace = '#NAMESPACE#'";
	
	/****************************************************************************
	 * 
	 * 
	 *                         Basic
	 * 
	 * 
	 *****************************************************************************/
	
	/**
	 * conn
	 */
	protected final DruidPooledConnection conn;
	
	protected final String database;

	public KubeSqlClient(DruidPooledConnection conn) throws Exception {
		this(conn, DEFAULT_DB);
	}
	
	
	public KubeSqlClient(DruidPooledConnection conn, String database) throws Exception {
		super();
		this.conn = conn;
		this.database = database;
		if (!hasDatabase()) {
			createDatabase();
		}
	}



	/**
	 * @return                conn
	 */
	public DruidPooledConnection getConn() {
		return conn;
	}
	
	
	/****************************************************************************
	 * 
	 * 
	 *                         Database, Table
	 * 
	 * 
	 *****************************************************************************/
	/**
	 * @param name                db name
	 * @return                    true or false
	 * @throws Exception          exception
	 */
	@Deprecated
	public synchronized boolean hasDatabase(String name) throws Exception {
		return execWithResultCheck(null, CHECK_DATABASE.replace(LABEL_DATABASE, name));
	}
	
	
	/**
	 * @return                    true or false
	 * @throws Exception          exception
	 */
	public synchronized boolean hasDatabase() throws Exception {
		return execWithResultCheck(null, CHECK_DATABASE.replace(LABEL_DATABASE, database));
	}
	
	/**
	 * create database
	 * 
	 * @throws Exception mysql exception
	 */
	public synchronized boolean createDatabase() throws Exception {
		return exec(null, CREATE_DATABASE.replace(LABEL_DATABASE, database));
	}
	
	/**
	 * @return delete database
	 * @throws Exception mysql exception
	 */
	public synchronized boolean dropDatabase() throws Exception {
		return exec(null, DELETE_DATABASE.replace(LABEL_DATABASE, database));
	}
	
	/**
	 * @param tableName               name
	 * @return                        true if the table exists, otherwise return false
	 * @throws Exception              mysql exception
	 */
	public synchronized boolean hasTable(String tableName) throws Exception {
		return execWithResultCheck(database, CHECK_TABLE.replace(LABEL_DATABASE, database)
											.replace(LABEL_TABLE, tableName));
	}

	/**
	 * @param tableName               name
	 * @return                        true if the table exists, otherwise return false
	 * @throws Exception              mysql exception
	 */
	public synchronized boolean createTable(String tableName) throws Exception {
		return exec(database, CREATE_TABLE.replace(LABEL_TABLE, tableName));
	}
	
	/**
	 * @param tableName               name
	 * @return                        true if the table exists, otherwise return false
	 * @throws Exception              mysql exception
	 */
	public synchronized boolean dropTable(String tableName) throws Exception {
		return exec(database, DELETE_TABLE.replace(LABEL_TABLE, tableName));
	}
	
	
	
	/****************************************************************************
	 * 
	 * 
	 *                         Insert, Update, Delete objects
	 * 
	 * 
	 *****************************************************************************/
	/**
	 * @param table                                  table
	 * @param name                                   name
	 * @param namespace                              namespace
	 * @param json                                   json
	 * @return                                       true or false
	 * @throws Exception                             exception
	 */
	public boolean insertObject(String table, String name, String namespace, String json) throws Exception {
		if(!exec(database, INSERT_OBJECT
					.replace(KubeSqlClient.LABEL_TABLE, table)
					.replace(KubeSqlClient.LABEL_NAME, name)
					.replace(KubeSqlClient.LABEL_NAMESPACE, namespace)
					.replace(KubeSqlClient.LABEL_JSON, json))) {
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
		return exec(database, UPDATE_OBJECT
					.replace(KubeSqlClient.LABEL_TABLE, table)
					.replace(KubeSqlClient.LABEL_NAME, name)
					.replace(KubeSqlClient.LABEL_NAMESPACE, namespace)
					.replace(KubeSqlClient.LABEL_JSON, json));		
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
		return exec(database, DELETE_OBJECT
					.replace(KubeSqlClient.LABEL_TABLE, table)
					.replace(KubeSqlClient.LABEL_NAME, name)
					.replace(KubeSqlClient.LABEL_NAMESPACE, namespace)
					.replace(KubeSqlClient.LABEL_JSON, json));		
	}
	
	
	/****************************************************************************
	 * 
	 * 
	 *                         Common
	 * 
	 * 
	 *****************************************************************************/
	
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
	 * @param sql                             sql
	 * @return                                true or false
	 * @throws Exception                      exception
	 */
	public ResultSet execWithResult(String sql) throws Exception {
		return execWithResult(database, sql); 
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
	
	public static final String DEF_DRV  = "com.mysql.cj.jdbc.Driver";
	/**
	 * URL
	 */
	public static final String DEF_URL  = "jdbc:mysql://kube-database.kube-system:3306?useUnicode=true&characterEncoding=UTF8&connectTimeout=2000&socketTimeout=6000&autoReconnect=true&&serverTimezone=Asia/Shanghai";
	
	/**
	 * USER
	 */
	public static final String DEF_USER = "root";
	
	/**
	 * PWD
	 */
	public static final String DEF_PWD  = "onceas";
	
	/**
	 * @return                               datasource
	 */
	static DruidDataSource createDataSourceFromResource() {
        Properties props = new Properties();
        props.put("druid.driverClassName", System.getProperty("driver", DEF_DRV));
        props.put("druid.url", System.getenv("mysqlUrl") == null ? DEF_URL : System.getenv("mysqlUrl"));
        props.put("druid.username", System.getenv("user") == null ? DEF_USER : System.getenv("user"));
        props.put("druid.password", System.getenv("pwd") == null ? DEF_PWD : System.getenv("pwd"));
        props.put("druid.initialSize", 10);
        props.put("druid.maxActive", 100);
        props.put("druid.maxWait", 0);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.configFromPropety(props);
        return dataSource;
    }

	/**
	 * @param database                       database
	 * @return                               SqlClient
	 * @throws Exception                     exception
	 */
	public static KubeSqlClient createSqlClient(String database) throws Exception {
		return new KubeSqlClient(createDataSourceFromResource().getConnection(), database);
	}
}
