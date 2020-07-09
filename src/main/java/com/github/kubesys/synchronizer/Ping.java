/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubesys.KubernetesClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class Ping implements Runnable {

	public boolean created = true;
	
	protected final KubernetesClient client;
	
	public static final String PING = "{\r\n" + 
			"	\"kind\": \"Namespace\",\r\n" + 
			"	\"apiVersion\": \"v1\",\r\n" + 
			"	\"metadata\": {\r\n" + 
			"		\"name\": \"ping\"\r\n" + 
			"	}\r\n" + 
			"}";
	
	
	public Ping(KubernetesClient client) {
		super();
		this.client = client;
	}


	@Override
	public void run() {
		while (true) {

			try {
				
				JsonNode json = client.getResource("Namespace", "", "ping");
				if (json.get("kind").asText().equals("Namespace")) {
					created = true;
				} else {
					created = false;
				}
				
				if (created) {
					client.deleteResource("Namespace", "", "ping");
				} else {
					client.createResource(new ObjectMapper().readTree(PING));
				}
				
				Thread.sleep(5000);
			} catch (Exception ex) {
				System.exit(1);
			}
			
		}
	}
	
	
}
