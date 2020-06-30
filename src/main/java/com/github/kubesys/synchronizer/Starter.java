/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesConstants;
import com.github.kubesys.mqclient.AMQClient;
import com.github.kubesys.mqclient.AMQUtils;
import com.github.kubesys.sqlclient.SqlClient;
import com.github.kubesys.sqlclient.SqlUtils;
import com.rabbitmq.client.ConnectionFactory;

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
	
	public static final String AMQP_NAME = "kube-rabbitmq";
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		
		KubernetesClient kubeClient = getKubeClient();
		SqlClient sqlClient = getSqlClientBy(kubeClient, DATABASE_NAME);
		confirmSynchDataFromConfifMap(kubeClient.getResource(
					Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, SYNCH_NAME));
		
		dataFromKubeToMysqlAndPushToMQ(kubeClient, sqlClient, getAMQClientBy(kubeClient, AMQP_NAME));
	}


	
	/*****************************************************************************************
	 * 
	 * Core
	 * 
	 *****************************************************************************************/
	
	public static void dataFromKubeToMysqlAndPushToMQ(KubernetesClient kubeClient, SqlClient sqlClient, AMQClient pusher) throws Exception {
		for (String kind : synchTargets) {
			String tableName = kubeClient.getConfig().getName(kind);
			sqlClient.createTable(tableName);
			Synchronizer synchronizer = new Synchronizer(kind, kubeClient, sqlClient, pusher);
			kubeClient.watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, synchronizer);
		}
	}
	

	public static void confirmSynchDataFromConfifMap(JsonNode node) throws Exception {
		
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

	/**
	 * @return                               client
	 * @throws Exception                     exception
	 */
	public static KubernetesClient getKubeClient() throws Exception {
		String url    = System.getenv("URL");
		String token  = System.getenv("TOKEN");
		
		return (token == null) ?  new KubernetesClient(url)
								: new KubernetesClient(url, token);
	}

	/**
	 * @param kubeClient                     client            
	 * @param config                         config
	 * @return                               sqlclient
	 * @throws Exception                     exception
	 */
	public static AMQClient getAMQClientBy(KubernetesClient kubeClient, String config) throws Exception {
		
		JsonNode data = kubeClient.getResource(Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, config).get("data");
		
		ConnectionFactory factory = AMQUtils.createConnectionFactory(
											data.get("HOST").asText(), 
											data.get("USER").asText(), 
											data.get("PASSWORD").asText(), 
											data.get("PORT").asInt());

		return new AMQClient(factory.newConnection(), data.get("QUEUE").asText());
	}
	
	/**
	 * @param kubeClient                     client            
	 * @param config                         config
	 * @return                               sqlclient
	 * @throws Exception                     exception
	 */
	public static SqlClient getSqlClientBy(KubernetesClient kubeClient, String config) throws Exception {
		
		JsonNode data = kubeClient.getResource(Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, config).get("data");
		
		SqlClient sqlClient = new SqlClient(SqlUtils.createConnection(
											data.get("DRIVER").asText(), 
											data.get("JDBC").asText(), 
											data.get("USER").asText(), 
											data.get("PASSWORD").asText()),
										data.get("DATABASE").asText());

		if (sqlClient.hasDatabase()) {
			sqlClient.dropDatabase();
		}
		sqlClient.createDatabase();
		
		return sqlClient;
	}
	
}
