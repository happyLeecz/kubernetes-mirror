/*

 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.sql.Connection;
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

	public static final String INSERT_OBJECT   = "INSERT INTO #TABLE# VALUES ('#NAME#', '#NAMESPACE#', '#JSON#')";
	
	public static final String UPDATE_OBJECT   = "UPDATE #TABLE# SET data = '#JSON#' WHERE name = '#NAME#' and namespace = '#NAMESPACE#'";
	
	public static final String DELETE_OBJECT   = "DELETE FROM #TABLE# WHERE name = '#NAME#' and namespace = '#NAMESPACE#'";
	
	
	
	protected final MysqlClient dbHelper;

	public Synchronizer(MysqlClient dbHelper) {
		super();
		this.dbHelper = dbHelper;
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
		if(!dbHelper.exec(Constants.DB, INSERT_OBJECT
					.replace(MysqlClient.LABEL_TABLE, table)
					.replace(MysqlClient.LABEL_NAME, name)
					.replace(MysqlClient.LABEL_NAMESPACE, namespace)
					.replace(MysqlClient.LABEL_JSON, getNormalJSON(json)))) {
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
		return dbHelper.exec(Constants.DB, UPDATE_OBJECT
					.replace(MysqlClient.LABEL_TABLE, table)
					.replace(MysqlClient.LABEL_NAME, name)
					.replace(MysqlClient.LABEL_NAMESPACE, namespace)
					.replace(MysqlClient.LABEL_JSON, getNormalJSON(json)));		
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
		return dbHelper.exec(Constants.DB, DELETE_OBJECT
					.replace(MysqlClient.LABEL_TABLE, table)
					.replace(MysqlClient.LABEL_NAME, name)
					.replace(MysqlClient.LABEL_NAMESPACE, namespace)
					.replace(MysqlClient.LABEL_JSON, getNormalJSON(json)));		
	}
	
	public String getNormalJSON(String json) {
		String replaceAll = json.replaceAll("&&", "\\\\u0026\\\\u0026")
				.replaceAll(">", "\\\\u003e")
				.replaceAll("\\'", "\\\\'")
//				.replaceAll("\\'", "\\\\\\\\'")
				.replaceAll("\\\\n", "\\\\\\\\\\n")
				.replaceAll("\\\\\"", "\\\\\\\\\\\\\"");
		
		// plase see loki-loki-stack-test
		while (true) {
			if (replaceAll.contains("\\\\\\\\\\")) {
				replaceAll = replaceAll.replace("\\\\\\\\\\", "\\\\\\");
			} else {
				break;
			}
		}
		
//		while(true) {
//			if (replaceAll.contains("\\'")) {
//				replaceAll = replaceAll.replace("\\'", "'");
//			} else {
//				break;
//			}
//		}
		return replaceAll;
	}
	
	

	public Connection getConn() {
		return dbHelper.getConn();
	}
	
}
