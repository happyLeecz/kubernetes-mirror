/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import java.util.logging.Logger;

import com.github.kubesys.KubernetesClient;
import com.github.kubesys.mirror.client.KubeSqlClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class KubeStarter {
	
	/**
	 * m_logger
	 */
	protected static final Logger m_logger = Logger.getLogger(KubeStarter.class.getName());
	
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		KubernetesClient kubeClient = new KubernetesClient(
									System.getenv("kubeUrl"), 
									System.getenv("token"));
		KubeSqlClient kubeSqlClient = KubeSqlClient.createSqlClient(
									System.getenv("database"));
		KubeMirror kubeMirror = new KubeMirror(kubeClient, kubeSqlClient);
		kubeMirror.start();
	}



	
}