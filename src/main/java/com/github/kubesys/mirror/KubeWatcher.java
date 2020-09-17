/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesConstants;
import com.github.kubesys.watchers.AutoDiscoverCustomizedResourcesWacther;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class KubeWatcher extends AutoDiscoverCustomizedResourcesWacther {

	protected final KubeMirror mirror;
	
	public KubeWatcher(KubernetesClient client, KubeMirror mirror) {
		super(client);
		this.mirror = mirror;
	}

	@Override
	public void doAdded(JsonNode node) {
		super.doAdded(node);
		mirror.addSource(node.get(
				KubernetesConstants.KUBE_SPEC_NAMES_KIND).asText());
	}

	@Override
	public void doDeleted(JsonNode node) {
		super.doDeleted(node);
		mirror.deleteSource(node.get(
				KubernetesConstants.KUBE_SPEC_NAMES_KIND).asText());
	}

	@Override
	public void doClose() {
		try {
			this.kubeClient.watchResources("CustomResourceDefinition", 
					KubernetesConstants.VALUE_ALL_NAMESPACES, 
					new KubeWatcher(kubeClient, mirror));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
