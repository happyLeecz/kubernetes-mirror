/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.mysql.cj.jdbc.Driver;

import io.github.kubesys.KubernetesClient;
import io.github.kubesys.KubernetesConstants;

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
	public static final Set<String> synchTargets = new HashSet<>(); 
	
	/**
	 * name
	 */
	public static final String NAME = "kubernetes-synchronizer";
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		
		KubernetesClient kubeClient = getKubeClient();
		Synchronizer sqlClient = getSynchronizer();
		createSynchTargetsFromConfifMap(kubeClient.getResource(
					Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, NAME));
		SynchFromKubeToMysql(kubeClient, sqlClient);
	}

	
	/*****************************************************************************************
	 * 
	 * Core
	 * 
	 *****************************************************************************************/
	
	protected static void watchKubernetesCRDKinds(KubernetesClient kubeClient, Synchronizer sqlClient) throws Exception {
		kubeClient.watchResources(Constants.KIND_CUSTOMRESOURCEDEFINTION, 
							KubernetesConstants.VALUE_ALL_NAMESPACES, 
							new Listener(Constants.KIND_CUSTOMRESOURCEDEFINTION, kubeClient, sqlClient));
	}

	protected static void SynchFromKubeToMysql(KubernetesClient kubeClient, Synchronizer sqlClient) throws Exception {
		
		for (String kind : synchTargets) {
			String tableName = kubeClient.getConfig().getName(kind);
			
			try {
				if (!sqlClient.hasTable(Constants.DB, tableName)) {
					sqlClient.createTable(Constants.DB, tableName);
				}
				m_logger.info("create table " + tableName + " successfully.");
			} catch (Exception e) {
				m_logger.severe("fail to create table " + tableName + ":" + e);
			}
			
			kubeClient.watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
										new Listener(kind, kubeClient, sqlClient));
		}
	}

	protected static void createSynchTargetsFromConfifMap(JsonNode node) throws Exception {
		
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

	private static KubernetesClient getKubeClient() throws Exception {
		String url    = System.getenv("URL");
		String token  = System.getenv("TOKEN");
		
		return (token == null) ?  new KubernetesClient(url)
								: new KubernetesClient(url, token);
	}

	private static Synchronizer getSynchronizer() throws Exception {
		
		String jdbc = System.getenv("JDBC") == null ? Constants.JDBC : System.getenv("JDBC");
		String user = System.getenv("USER") == null ? Constants.USER : System.getenv("USER");
		String pwd  = System.getenv("PWD") == null ? Constants.PWD : System.getenv("PWD");

		Class.forName(Driver.class.getName());
		Synchronizer sqlClient =  new Synchronizer(
				DriverManager.getConnection(jdbc, user, pwd));
		
		if (!sqlClient.hasDatabase(Constants.DB)) {
			sqlClient.createDatabase(Constants.DB);
		}
		
		return sqlClient;
	}

}
