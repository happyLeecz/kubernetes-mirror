/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.KubernetesClient;
import io.github.kubesys.KubernetesConstants;
import io.github.kubesys.KubernetesException;
import io.github.kubesys.KubernetesWatcher;

/**
 * @author wuheng
 * @since 2019.4.20
 */
public class Listener extends KubernetesWatcher {

	public static final Logger m_logger = Logger.getLogger(Listener.class.getName());
	
	/**
	 * kind
	 */
	protected final String kind;

	/**
	 * client
	 */
	protected final KubernetesClient kubeClient;

	/**
	 * client
	 */
	protected final Synchronizer sqlClient;

	public Listener(String kind, KubernetesClient kubeClient, Synchronizer sqlClient) {
		super();
		this.kind = kind;
		this.kubeClient = kubeClient;
		this.sqlClient = sqlClient;
	}


	/******************************************************
	 * 
	 * Lifecycle
	 * 
	 ******************************************************/
	public void doAdded(JsonNode json) {

		if (Constants.KIND_CUSTOMRESOURCEDEFINTION.equals(kind)) {
			
			String newKind = json.get(Constants.YAML_SPEC).get(Constants.YAML_SPEC_NAMES)
										.get(Constants.YAML_SPEC_NAMES_KIND).asText();
			
			if (!Starter.synchTargets.contains(newKind)) {
				return;
			}
			
			String tableName = kubeClient.getConfig().getName(newKind);
			try {
				if (!sqlClient.hasTable(Constants.DB, tableName)) {
					sqlClient.createTable(Constants.DB, tableName);
				}
				m_logger.info("create table " + tableName + " successfully.");
			} catch (Exception e) {
				m_logger.severe("fail to create table " + tableName + ":" + e);
			}
			
			kubeClient.watchResources(newKind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
									new Listener(newKind, kubeClient, sqlClient));
		} 

		
		ObjectNode yaml = getJsonWithoutAnotation(json);
		String itemName = getName(json);
		String itemNS = getNamespace(json);

		String sql = "INSERT INTO " + kubeClient.getConfig().getName(kind) + " VALUES ('" 
							+ itemName + "', '"
							+ itemNS + "', '"
							+ yaml.toString() + "', " 
							+ true + ")";
		
		if (!doSql(sql, itemName, itemNS, Constants.SQL_INSERT)) {
			doModified(yaml);
		}

	}

	public void doModified(JsonNode json) {
		
		ObjectNode node = getJsonWithoutAnotation(json);

		String itemName = getName(json);
		String itemNS = getNamespace(json);
		
		String sql = "UPDATE " + kubeClient.getConfig().getName(kind) 
										+ " SET data = '" + node  + "' WHERE"
										+ " name = '" + itemName + "' and "
										+ " namespace = '" + itemNS + "'";
		
		doSql(sql, itemName, itemNS, Constants.SQL_UPDATE);

	}

	public void doDeleted(JsonNode json) {

		String itemName = getName(json);
		String itemNS = getNamespace(json);
		
		String sql = "DELETE FROM " + kubeClient.getConfig().getName(kind) 
				+ " WHERE name = '" + itemName + "' and namespace = '" + itemNS + "'";
		
		doSql(sql, itemName, itemNS, Constants.SQL_DELETE);
	}

	/******************************************************
	 * 
	 * Utils
	 * 
	 ******************************************************/
	protected boolean doSql(String sql, String name, String ns, String operation) {
		
		String rsql = sql.replaceAll("\\\"", "\\\\\\\"");

		try {
			sqlClient.exec(Constants.DB, rsql);
			m_logger.info("\t" + operation + " object '<" + name + "," + ns 
					+ ">' in table '" + kind.toLowerCase() + "' successfully.");
			return true;
		} catch (Exception ex) {
			m_logger.severe(rsql + "," + ex);
			return false;
		}
	}

	/**
	 * @param json                            json
	 * @return                                node
	 */
	@SuppressWarnings("deprecation")
	protected ObjectNode getJsonWithoutAnotation(JsonNode json) {
		ObjectNode yaml = json.deepCopy();
		ObjectNode meta = yaml.get(Constants.YAML_METADATA).deepCopy();
		if (meta.has(Constants.YAML_METADATA_ANNOTATIONS)) {
			meta.remove(Constants.YAML_METADATA_ANNOTATIONS);
		}
		yaml.put(Constants.YAML_METADATA, meta);
		return yaml;
	}
	
	/**
	 * @param json                           json
	 * @return                               name
	 */
	protected String getName(JsonNode json) {
		return json.get(Constants.YAML_METADATA)
					.get(Constants.YAML_METADATA_NAME).asText();
	}
	
	/**
	 * @param json                           json
	 * @return                               namespace
	 */
	protected String getNamespace(JsonNode json) {
		JsonNode meta = json.get(Constants.YAML_METADATA);
		return (meta.has(Constants.YAML_METADATA_NAMESPACE)) 
				? meta.get(Constants.YAML_METADATA_NAMESPACE).asText() : "default";
	}

	@Override
	public void doOnClose(KubernetesException exception) {
		
		m_logger.severe(exception.toString());
		if (Starter.synchTargets.contains(kind)) {
			kubeClient.watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
								new Listener(kind, kubeClient, sqlClient));
		}
		m_logger.info("start synchronizer '" + kind + "'.");
	}

}
