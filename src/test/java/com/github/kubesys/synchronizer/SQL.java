/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class SQL {
	
	public static void main(String[] args) throws Exception {
		MysqlClient client = Starter.getSqlClient();
		System.out.println(client.hasTable("kube", "abc"));
	}
	
}
