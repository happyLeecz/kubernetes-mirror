/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesConstants;
import com.github.kubesys.KubernetesException;
import com.github.kubesys.KubernetesWatcher;
import com.github.kubesys.mqclient.AMQClient;
import com.github.kubesys.sqlclient.SqlClient;

/**
 * @author wuheng
 * @since 2019.4.20
 */
public class Synchronizer extends KubernetesWatcher {

	public static final Logger m_logger = Logger.getLogger(Synchronizer.class.getName());
	
	/**
	 * client
	 */
	protected final KubernetesClient kubeClient;
	
	protected final SqlClient sqlClient;

	protected AMQClient ampClient;
	
	protected final String kind;
	
	protected final String tableName;
	
	public Synchronizer(String kind, KubernetesClient kubeClient, SqlClient sqlClient, AMQClient ampClient) {
		super();
		this.kind = kind;
		this.kubeClient = kubeClient;
		this.sqlClient = sqlClient;
		this.ampClient = ampClient;
		this.tableName = kubeClient.getConfig().getName(kind);
	}


	/******************************************************
	 * 
	 * Lifecycle
	 * 
	 ******************************************************/
	public void doAdded(JsonNode json) {

//		if (Constants.KIND_CUSTOMRESOURCEDEFINTION.equals(kind)) {
//			
//			String newKind = json.get(Constants.YAML_SPEC).get(Constants.YAML_SPEC_NAMES)
//										.get(Constants.YAML_SPEC_NAMES_KIND).asText();
//			
//			if (!Starter.synchTargets.contains(newKind)) {
//				return;
//			}
//			
//			kubeClient.watchResources(newKind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
//									new Synchronizer(newKind, kubeClient, sqlClient, ampClient));
//		} 

		// 
		try {
			sqlClient.insertObject(tableName, Utils.getName(json), 
					Utils.getNamespace(json, kubeClient.getConfig().isNamespaced(kind)), 
					Utils.getJsonWithoutAnotation(json));
			m_logger.info("insert object  " + json + " successfully.");
			send(getJSON("ADDED", json));
		} catch (Exception e) {
			m_logger.severe("fail to insert object because of missing table " + tableName + ":" + e);
		}

	}

	public void doModified(JsonNode json) {
		
		try {
			sqlClient.updateObject(kubeClient.getConfig().getName(kind), Utils.getName(json), 
					Utils.getNamespace(json, kubeClient.getConfig().isNamespaced(kind)), 
					Utils.getJsonWithoutAnotation(json));
			send(getJSON("MODIFIED", json));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void doDeleted(JsonNode json) {
		
		try {
			sqlClient.deleteObject(kubeClient.getConfig().getName(kind), Utils.getName(json), 
					Utils.getNamespace(json, kubeClient.getConfig().isNamespaced(kind)), 
					Utils.getJsonWithoutAnotation(json));
			send(getJSON("DELETED", json));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected JsonNode getJSON(String operator, JsonNode json) {
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("operator", operator);
		node.put("kind", json.get("kind").asText());
		node.put("name", Utils.getName(json));
		node.put("namespace", Utils.getNamespace(json, kubeClient.getConfig().isNamespaced(kind)));
		return node;
	}

	@Override
	public void doOnClose(KubernetesException exception) {
		
		m_logger.severe("caused by" + exception);
		if (Starter.synchTargets.contains(kind)) {
			kubeClient.watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
								new Synchronizer(kind, kubeClient, sqlClient, ampClient));
		}
		m_logger.info("start synchronizer '" + kind + "'.");
	}

	
	protected void send(JsonNode json) throws Exception {
		if (this.ampClient == null) {
			this.ampClient = Starter.getAMQClientBy(
					kubeClient, Starter.AMQP_NAME);
		}
		this.ampClient.send(json);
	}

}
