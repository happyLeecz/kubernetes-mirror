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
import com.github.kubesys.synchronizer.clients.MysqlClient;
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
	
	protected static final Logger m_logger = Logger.getLogger(Starter.class.getName());
	
	/**
	 * 
	 */
	public static final Set<String> targets = new HashSet<>(); 
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	
	public static void main(String[] args) throws Exception {
		
		KubernetesClient kubeClient = getKubeClient();
		MysqlClient sqlClient = getMysqlClient();
		createListenTargetsByConfifMap(kubeClient);
		watchKubernetesCoreKinds(kubeClient, sqlClient);
	}

	
	/*****************************************************************************************
	 * 
	 * Core
	 * 
	 *****************************************************************************************/
	
	protected static void watchKubernetesCRDKinds(KubernetesClient kubeClient, MysqlClient sqlClient) throws Exception {
		kubeClient.watchResources(Constants.KIND_CUSTOMRESOURCEDEFINTION, 
							KubernetesConstants.VALUE_ALL_NAMESPACES, 
							new Synchronizer(Constants.KIND_CUSTOMRESOURCEDEFINTION, kubeClient, sqlClient));
	}

	protected static void watchKubernetesCoreKinds(KubernetesClient kubeClient, MysqlClient sqlClient) throws Exception {
		for (String kind : targets) {
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
										new Synchronizer(kind, kubeClient, sqlClient));
		}
	}

	public static final String NAME = "kubernetes-synchronizer";
	
	protected static void createListenTargetsByConfifMap(KubernetesClient kube) throws Exception {
		
		try {
			JsonNode node = kube.getResource(Constants.KIND_CONFIGMAP, 
					Constants.NS_KUBESYSTEM, NAME);
			Iterator<JsonNode> elements = node.get(Constants.YAML_DATA).elements();
			while (elements.hasNext()) {
				String asText = elements.next().asText();
				targets.add(asText);
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

	private static MysqlClient getMysqlClient() throws Exception {
		
		String jdbc = System.getenv("JDBC") == null ? Constants.JDBC : System.getenv("JDBC");
		String user = System.getenv("USER") == null ? Constants.USER : System.getenv("USER");
		String pwd  = System.getenv("PWD") == null ? Constants.PWD : System.getenv("PWD");

		Class.forName(Driver.class.getName());
		MysqlClient sqlClient =  new MysqlClient(
				DriverManager.getConnection(jdbc, user, pwd));
		
		if (!sqlClient.hasDatabase(Constants.DB)) {
			sqlClient.createDatabase(Constants.DB);
		}
		
		return sqlClient;
	}

}
