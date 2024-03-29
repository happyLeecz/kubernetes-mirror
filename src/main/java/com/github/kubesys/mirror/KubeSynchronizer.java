/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesWatcher;
import com.github.kubesys.mirror.client.KubeSqlClient;

/**
 * @author wuheng
 * @since 2019.4.20
 */
public class KubeSynchronizer extends KubernetesWatcher {

	public static final Logger m_logger = Logger.getLogger(KubeSynchronizer.class.getName());

	/**
	 * kind
	 */
	protected final String kind;

	/**
	 * table
	 */
	protected final String table;

	/**
	 * sqlClient
	 */
	protected final KubeSqlClient kubeSqlClient;

	public KubeSynchronizer(String kind, String table, KubernetesClient kubeClient, KubeSqlClient kubeSqlClient) {
		super(kubeClient);
		this.kind = kind;
		this.table = table;
		this.kubeSqlClient = kubeSqlClient;
	}

	@Override
	public void doAdded(JsonNode json) {
		try {
			kubeSqlClient.insertObject(table, KubeUtils.getName(json),
					KubeUtils.getNamespace(json),
					KubeUtils.getJsonWithoutAnotation(json));
			m_logger.info("insert object  " + json + " successfully.");
		} catch (Exception e) {
			m_logger.severe("fail to insert object :" + e);
		}
	}

	@Override
	public void doModified(JsonNode json) {
		try {
			kubeSqlClient.updateObject(table, KubeUtils.getName(json),
					KubeUtils.getNamespace(json),
					KubeUtils.getJsonWithoutAnotation(json));
			m_logger.info("update object  " + json + " successfully.");
		} catch (Exception e) {
			m_logger.severe("fail to update object :" + e);
		}
	}

	@Override
	public void doDeleted(JsonNode json) {
		try {
			kubeSqlClient.deleteObject(table, KubeUtils.getName(json),
					KubeUtils.getNamespace(json),
					KubeUtils.getJsonWithoutAnotation(json));
			m_logger.info("delete object  " + json + " successfully.");
		} catch (Exception e) {
			m_logger.severe("fail to delete object :" + e);
		}
	}

	@Override
	public void doClose() {
		try {
			kubeClient.watchResources(kind, new KubeSynchronizer(
					kind, table, kubeClient, kubeSqlClient));
		} catch (Exception e) {
			System.exit(1);
		}
	}
}
