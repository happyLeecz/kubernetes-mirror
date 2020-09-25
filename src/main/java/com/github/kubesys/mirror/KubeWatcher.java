/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;


import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesWatcher;
import com.github.kubesys.mirror.client.KubeSqlClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class KubeWatcher extends KubernetesWatcher {

	
	protected final KubeMirror kubeMirror;
	
	protected final KubeSqlClient sqlClient;
	
	public KubeWatcher(KubernetesClient client, KubeMirror kubeMirror, KubeSqlClient sqlClient) {
		super(client);
		this.kubeMirror = kubeMirror;
		this.sqlClient = sqlClient;
	}

	@Override
	public void doAdded(JsonNode node) {
		try {
			for (String kind : kubeMirror.createSources()) {
				startWatcher(kind);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void doDeleted(JsonNode node) {
		// ignore here
		try {
			for (String kind : kubeMirror.getSources()) {
				stopWatcher(kind); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void doModified(JsonNode node) {
		
		try {
			Set<String> current = kubeMirror.createSources();
			
			for (String kind : current) {
				startWatcher(kind);
			}
			
			for (String kind : kubeMirror.getSources()) {
				if (current.contains(kind)) {
					continue;
				}
				
				stopWatcher(kind);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void startWatcher(String kind) throws Exception {
		if (kubeMirror.beenWatched(kind)) {
			return;
		}
		
		kubeMirror.addSource(kind);
	}
	
	protected void stopWatcher(String kind) throws Exception {
		
		if (!kubeMirror.beenWatched(kind)) {
			return;
		}
		
		kubeMirror.deleteSource(kind);
	}

	@Override
	public void doClose() {
		try {
			this.kubeClient.watchResource("ConfigMap", "kube-system", "kube-mirror",
					new KubeWatcher(kubeClient, kubeMirror, sqlClient));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
