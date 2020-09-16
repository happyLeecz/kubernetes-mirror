/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
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
	
	public static final String YAML_DATA                            = "data";

	public static final String KIND_CONFIGMAP                       = "ConfigMap";

	public static final String NS_KUBESYSTEM                        = "kube-system";

	public static final String NAME_MIRROR                          = "kube-mirror";

	/**
	 * targets
	 */
	public Set<String> sources = new HashSet<>();

	protected final KubernetesClient kubeClient;

	protected final KubeSqlClient kubeSqlClient;

	public KubeMirror(KubernetesClient kubeClient, KubeSqlClient kubeSqlClient) {
		super();
		this.kubeClient = kubeClient;
		this.kubeSqlClient = kubeSqlClient;
	}

	public void start() throws Exception {
		fromSources(kubeClient.getResource(KIND_CONFIGMAP, 
							NS_KUBESYSTEM, NAME_MIRROR))
		.toTargets();
	}

	protected KubeMirror toTargets() throws Exception {
		for (String kind : sources) {
			
			String table = kubeClient.getConfig().getName(kind);
			
			if (!kubeSqlClient.hasTable(table)) {
				kubeSqlClient.createTable(table);
			}
			
			kubeClient.watchResources(kind, new KubeSynchronizer(
							kind, table, kubeClient, kubeSqlClient));
		}
		
		return this;
	}

	public KubeMirror fromSources(JsonNode node) {
		try {
			Iterator<JsonNode> elements = node.get(YAML_DATA).elements();
			while (elements.hasNext()) {
				String asText = elements.next().asText();
				sources.add(asText);
			}
		} catch (Exception ex) {
			m_logger.severe("ConfigMap 'kubernetes-mirror' is not " + "				ready in namespace 'kube-system'.");
			System.exit(1);
		}
		
		return this;
	}
}
