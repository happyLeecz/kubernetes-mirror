/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.KubernetesClient;


/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class KeepAlive implements Runnable {
	
	/**
	 * m_logger
	 */
	protected static final Logger m_logger = Logger.getLogger(KeepAlive.class.getName());

	protected final KubernetesClient client;
	
	
	public KeepAlive(KubernetesClient client) {
		super();
		this.client = client;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000*60*3);
				JsonNode json = client.getResource("ConfigMap", "kube-system", "transformer-cm");
				client.updateResource(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
