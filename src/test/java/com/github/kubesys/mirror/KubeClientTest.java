/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import com.github.kubesys.KubernetesClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class KubeClientTest {
	
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient(
								System.getenv("kubeUrl"), 
								System.getenv("token"));
		System.out.println(client.listResources("Pod"));
	}
	
}
