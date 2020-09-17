/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesWatcher;
import com.github.kubesys.mirror.client.KubeSqlClient;

/**
 * @author wuheng
 * @since 2019.4.20
 */
public class KubeMirror {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubeMirror.class.getName());

	/**
	 * sources
	 */
	public Set<String> sources = new HashSet<>();

	/**
	 * kube client
	 */
	protected final KubernetesClient kubeClient;

	/**
	 * sql client
	 */
	protected final KubeSqlClient kubeSqlClient;
	
	
	/**
	 * threads
	 */
	protected final Map<String, KubernetesWatcher> watchers = new HashMap<>();
	
	public KubeMirror(KubernetesClient kubeClient, KubeSqlClient kubeSqlClient) {
		super();
		this.kubeClient = kubeClient;
		this.kubeSqlClient = kubeSqlClient;
	}

	/**
	 * Kind
	 */
	public static final String KIND_CONFIGMAP = "ConfigMap";

	/**
	 * Namespace
	 */
	public static final String NS_KUBESYSTEM = "kube-system";

	/**
	 * name
	 */
	public static final String NAME_MIRROR = "kube-mirror";
	
	/**
	 * @throws Exception                    exception
	 */
	public void start() throws Exception {
		fromSources(kubeClient.getResource(KIND_CONFIGMAP, NS_KUBESYSTEM, NAME_MIRROR))
		.toTargets();
	}

	/**
	 * @return                              mirror
	 * @throws Exception                    exception
	 */
	protected KubeMirror toTargets() throws Exception {
		for (String kind : sources) {
			doWatcher(kind);
		}
		return this;
	}

	/**
	 * @param kind                          kind
	 * @throws Exception                    exception
	 */
	protected void doWatcher(String kind) throws Exception {
		String table = kubeClient.getConfig().getName(kind);

		if (kubeSqlClient.hasTable(table)) {
			kubeSqlClient.dropTable(table);
		} 
		
		kubeSqlClient.createTable(table);

		KubeSynchronizer watcher = new KubeSynchronizer(kind, table, kubeClient, kubeSqlClient);
		watchers.put(table, watcher);
		kubeClient.watchResources(kind, watcher);
	}
	
	/**
	 * @param kind                          kind
	 * @throws Exception                    exception
	 */
	@SuppressWarnings("deprecation")
	protected void stopWatcher(String kind) throws Exception {
		String table = kubeClient.getConfig().getName(kind);
		watchers.get(table).stop();
	}

	public static final String YAML_DATA = "data";
	
	
	/**
	 * @param node                           node
	 * @return                               mirror
	 */
	public KubeMirror fromSources(JsonNode node) {
		try {
			Iterator<JsonNode> elements = node.get(YAML_DATA).elements();
			while (elements.hasNext()) {
				String asText = elements.next().asText();
				sources.add(asText);
			}
		} catch (Exception ex) {
			m_logger.severe("ConfigMap 'kubernetes-mirror' is not ready in namespace 'kube-system'.");
			System.exit(1);
		}

		return this;
	}
	
	public void addSource(String kind) {
		if (!sources.contains(kind)) {
			sources.add(kind);
			try {
				doWatcher(kind);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deleteSource(String kind) {
		if (sources.contains(kind)) {
			sources.remove(kind);
			try {
				stopWatcher(kind);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
