/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesConstants;
import com.github.kubesys.sqlclient.SqlClient;
import com.github.kubesys.sqlclient.SqlUtils;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class Starter {
	
	/**
	 * m_logger
	 */
	protected static final Logger m_logger = Logger.getLogger(Starter.class.getName());
	
	/**
	 * targets
	 */
	protected static final Set<String> synchTargets = new HashSet<>(); 
	
	/**
	 * name
	 */
	public static final String SYNCH_NAME = "kube-synchronizer";
	
	public static final String DATABASE_NAME = "kube-database";
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		
		KubernetesClient kubeClient = getKubeClient();
		SqlClient sqlClient = getSqlClientBy(kubeClient, DATABASE_NAME);
		createSynchTargetsFromConfifMap(kubeClient.getResource(
					Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, SYNCH_NAME));
		
		Pusher pusher = new Pusher();		
		synchFromKubeToMysql(kubeClient, sqlClient, pusher);
	}


	
	/*****************************************************************************************
	 * 
	 * Core
	 * 
	 *****************************************************************************************/
	
	public static void synchFromKubeToMysql(KubernetesClient kubeClient, SqlClient sqlClient, Pusher pusher) throws Exception {
		for (String kind : synchTargets) {
			String tableName = kubeClient.getConfig().getName(kind);
			sqlClient.createTable(SqlClient.DEFAULT_DB, tableName);
			Synchronizer synchronizer = new Synchronizer(kind, kubeClient, sqlClient, pusher);
			kubeClient.watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, synchronizer);
		}
	}
	

	public static void createSynchTargetsFromConfifMap(JsonNode node) throws Exception {
		
		try {
			Iterator<JsonNode> elements = node.get(Constants.YAML_DATA).elements();
			while (elements.hasNext()) {
				String asText = elements.next().asText();
				synchTargets.add(asText);
			}
		} catch (Exception ex) {
			m_logger.severe("ConfigMap 'kubernetes-synchronizer' is not "
					+ "				ready in namespace 'kube-system'.");
			System.exit(1);
		}
	}
	
	/*****************************************************************************************
	 * 
	 * Create
	 * 
	 *****************************************************************************************/

	public static KubernetesClient getKubeClient() throws Exception {
		String url    = System.getenv("URL");
		String token  = System.getenv("TOKEN");
		
		return (token == null) ?  new KubernetesClient(url)
								: new KubernetesClient(url, token);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public static SqlClient getSqlClientBy(KubernetesClient kubeClient, String config) throws Exception {
		
		JsonNode data = kubeClient.getResource(Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, config).get("data");
		
		SqlClient sqlClient = new SqlClient(SqlUtils.createConnection(
											data.get("DRIVER").asText(), 
											data.get("JDBC").asText(), 
											data.get("USER").asText(), 
											data.get("PASSWORD").asText()));

		if (sqlClient.hasDatabase(SqlClient.DEFAULT_DB)) {
			sqlClient.dropDatabase(SqlClient.DEFAULT_DB);
		}
		sqlClient.createDatabase(SqlClient.DEFAULT_DB);
		return sqlClient;
	}
	
}
