/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesConstants;
import com.github.kubesys.mirror.client.KubeSqlClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class KubeStarter {
	
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	/**
	 * @param args                               args
	 * @throws Exception                         exception
	 */
	public static void main(String[] args) throws Exception {
		KubernetesClient kubeClient = new KubernetesClient(
									System.getenv("kubeUrl"), 
									System.getenv("token"));
		KubeSqlClient kubeSqlClient = KubeSqlClient.createSqlClient(
									System.getenv("database") == null ? "kube" : System.getenv("database"));
		KubeMirror kubeMirror = new KubeMirror(kubeClient, kubeSqlClient);
		kubeMirror.start();
		new Thread(new Daemon(kubeClient)).start();
		
		kubeClient.watchResources("CustomResourceDefinition", 
				KubernetesConstants.VALUE_ALL_NAMESPACES, 
				new KubeWatcher(kubeClient, kubeMirror));
	}
	
	public static class Daemon implements Runnable {

		protected final KubernetesClient client;
		
		public Daemon(KubernetesClient client) {
			super();
			this.client = client;
		}

		@Override
		public void run() {
			while(true) {
				try {
					client.listResources("Namespace");
					Thread.sleep(1000*60*30);
				} catch (Exception e) {
				}
			}
		}
		
	}

}
