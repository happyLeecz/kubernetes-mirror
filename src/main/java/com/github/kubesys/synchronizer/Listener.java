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
	 * client
	 */
	protected final KubernetesClient kubeClient;
	
	protected final MysqlClient sqlClient;

	/**
	 * client
	 */
	protected final Synchronizer synchronizer;
	
	/**
	 * kind
	 */
	protected final String kind;
	
	protected final String tableName;
	
	public Listener(String kind, KubernetesClient kubeClient, MysqlClient sqlClient) {
		super();
		this.kind = kind;
		this.kubeClient = kubeClient;
		this.sqlClient = sqlClient;
		this.synchronizer = new Synchronizer(sqlClient);
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
									new Listener(newKind, kubeClient, sqlClient));
		} 

		// 
		try {
			synchronizer.insertObject(tableName, 
					getName(json), getNamespace(json), getJsonWithoutAnotation(json));
			m_logger.info("insert object  " + json + " successfully.");
		} catch (Exception e) {
			m_logger.severe("fail to insert object because of missing table " + tableName + ":" + e);
		}

	}

	public void doModified(JsonNode json) {
		
		try {
			synchronizer.updateObject(kubeClient.getConfig().getName(kind), 
					getName(json), getNamespace(json), getJsonWithoutAnotation(json));
		} catch (Exception ex) {
			
		}

	}

	public void doDeleted(JsonNode json) {
		
		try {
			synchronizer.deleteObject(kubeClient.getConfig().getName(kind), 
					getName(json), getNamespace(json), getJsonWithoutAnotation(json));
		} catch (Exception ex) {
			
		}

	}


	/**
	 * @param json                            json
	 * @return                                node
	 */
	protected String getJsonWithoutAnotation(JsonNode json) {
		ObjectNode yaml = json.deepCopy();
		ObjectNode meta = yaml.get(Constants.YAML_METADATA).deepCopy();
		if (meta.has(Constants.YAML_METADATA_ANNOTATIONS)) {
			meta.remove(Constants.YAML_METADATA_ANNOTATIONS);
		}
		
		// > 1.18
		if (meta.has(Constants.YAML_METADATA_MANAGEDFIELDS)) {
			meta.remove(Constants.YAML_METADATA_MANAGEDFIELDS);
		}
		
		yaml.remove(Constants.YAML_METADATA);
		yaml.set(Constants.YAML_METADATA, meta);
		
		String value = yaml.toPrettyString();
		
		value = toMysqlJSON(value, "&", "\\u0026");
		value = toMysqlJSON(value, ">", "\\u003e");
		value = toMysqlJSON(value, "<", "\\u003c");
		value = toMysqlJSON(value, "\'", " \\'");
		value = toMysqlJSON(value, "\\\"", "\\\\\"");
		value = toMysqlJSON(value, "\\n", "\\\\n");
		
		return value;
	}


	public static String toMysqlJSON(String value, String src, String dst) {
		StringBuffer sb = new StringBuffer();
		int i = value.indexOf(src);
		if (i == -1) {
			sb.append(value);
		} else {
			sb.append(value.substring(0, i)).append(dst).append(
					toMysqlJSON(value.substring(i + src.length()), src, dst));
		}
		return sb.toString();
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
		
		m_logger.severe("caused by" + exception);
		if (Starter.synchTargets.contains(kind)) {
			kubeClient.watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, 
								new Listener(kind, kubeClient, sqlClient));
		}
		m_logger.info("start synchronizer '" + kind + "'.");
	}


}
