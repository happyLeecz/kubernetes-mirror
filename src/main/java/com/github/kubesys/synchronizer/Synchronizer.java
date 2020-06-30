/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.KubernetesClient;
import io.github.kubesys.KubernetesConstants;
import io.github.kubesys.KubernetesException;
import io.github.kubesys.KubernetesWatcher;

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
	
	protected final MysqlClient sqlClient;

	protected final Pusher pusher;
	
	protected final String kind;
	
	protected final String tableName;
	
	public Synchronizer(String kind, KubernetesClient kubeClient, MysqlClient sqlClient, Pusher pusher) {
		super();
		this.kind = kind;
		this.kubeClient = kubeClient;
		this.sqlClient = sqlClient;
		this.pusher = pusher;
		this.tableName = kubeClient.getConfig().getName(kind);
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
			
			kubeClient.watchResources(newKind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
									new Synchronizer(newKind, kubeClient, sqlClient, pusher));
		} 

		// 
		try {
			sqlClient.insertObject(tableName, Utils.getName(json), 
					Utils.getNamespace(json, kubeClient.getConfig().isNamespaced(kind)), 
					Utils.getJsonWithoutAnotation(json));
			m_logger.info("insert object  " + json + " successfully.");
			pusher.push(getJSON("ADDED", json));
		} catch (Exception e) {
			m_logger.severe("fail to insert object because of missing table " + tableName + ":" + e);
		}

	}

	public void doModified(JsonNode json) {
		
		try {
			sqlClient.updateObject(kubeClient.getConfig().getName(kind), Utils.getName(json), 
					Utils.getNamespace(json, kubeClient.getConfig().isNamespaced(kind)), 
					Utils.getJsonWithoutAnotation(json));
			pusher.push(getJSON("MODIFIED", json));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void doDeleted(JsonNode json) {
		
		try {
			sqlClient.deleteObject(kubeClient.getConfig().getName(kind), Utils.getName(json), 
					Utils.getNamespace(json, kubeClient.getConfig().isNamespaced(kind)), 
					Utils.getJsonWithoutAnotation(json));
			pusher.push(getJSON("DELETED", json));
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
								new Synchronizer(kind, kubeClient, sqlClient, pusher));
		}
		m_logger.info("start synchronizer '" + kind + "'.");
	}


}
