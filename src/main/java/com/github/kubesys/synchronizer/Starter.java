/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
				if (!sqlClient.hasTable("kube", tableName)) {
					sqlClient.createTable("kube", tableName);
				}
				System.out.println("create table " + tableName + " successfully.");
			} catch (Exception e) {
				System.out.println("fail to create table " + tableName + ":" + e);
			}
			
			kubeClient.watchResources(kind, "allNS", new Synchronizer(kind, kubeClient, sqlClient));
		}
	}

	protected static void createListenTargetsByConfifMap(KubernetesClient kube) throws Exception {
		JsonNode node = null;
		try {
			node = kube.getResource("ConfigMap", "kube-system", "kubeext-mysql-exporter");
		} catch (Exception ex) {
			System.err.println("ConfigMap 'kubeext-mysql-exporter' is not ready in namespace 'kube-system'.");
			ex.printStackTrace();
			System.exit(1);
		}
		Iterator<JsonNode> elements = node.get("data").elements();
		while (elements.hasNext()) {
			String asText = elements.next().asText();
			System.out.println("# " + asText);
			targets.add(asText);
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
