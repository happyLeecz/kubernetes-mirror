/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.alibaba.druid.pool.DruidDataSource;
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
	protected static final Set<String> synchTargets = new HashSet<>(); 
	
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
		MysqlClient sqlClient = getSqlClient();
		createSynchTargetsFromConfifMap(kubeClient.getResource(
					Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, NAME));
		synchFromKubeToMysql(kubeClient, sqlClient);
	}


	/**
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public static MysqlClient getSqlClient() throws SQLException, Exception {
		MysqlClient sqlClient = new MysqlClient(createDataSource().getConnection());

		if (sqlClient.hasDatabase(Constants.DB)) {
			sqlClient.dropDatabase(Constants.DB);
		}
		sqlClient.createDatabase(Constants.DB);
		return sqlClient;
	}

	
	/*****************************************************************************************
	 * 
	 * Core
	 * 
	 *****************************************************************************************/
	
	protected static void watchKubernetesCRDKinds(KubernetesClient kubeClient, MysqlClient sqlClient) throws Exception {
		kubeClient.watchResources(Constants.KIND_CUSTOMRESOURCEDEFINTION, 
							KubernetesConstants.VALUE_ALL_NAMESPACES, 
							new Listener(Constants.KIND_CUSTOMRESOURCEDEFINTION, kubeClient, sqlClient));
	}

	public static void synchFromKubeToMysql(KubernetesClient kubeClient, MysqlClient sqlClient) throws Exception {
		for (String kind : synchTargets) {
			String tableName = kubeClient.getConfig().getName(kind);
			sqlClient.createTable(Constants.DB, tableName);
			kubeClient.watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
										new Listener(kind, kubeClient, sqlClient));
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

	
	private static DruidDataSource createDataSource() throws Exception {
        Properties props = new Properties();
        props.put("druid.driverClassName", Driver.class.getName());
        props.put("druid.url", Constants.JDBC);
        props.put("druid.username", Constants.USER);
        props.put("druid.password", Constants.PWD);
        props.put("druid.initialSize", 10);
        props.put("druid.maxActive", 100);
        props.put("druid.maxWait", 3000);
        props.put("druid.keepAlive", true);
        props.put("druid.validationQuery", MysqlClient.CHECK_DATABASE.replace("#DATBASE#", "kube"));
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.configFromPropety(props);
        return dataSource;
    }
	
}
