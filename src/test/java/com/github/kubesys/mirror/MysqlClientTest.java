/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;

import java.sql.ResultSet;

import com.github.kubesys.mirror.client.KubeSqlClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class MysqlClientTest {
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		KubeSqlClient conn = KubeSqlClient.createSqlClient("mysql");
        ResultSet rs = conn.execWithResult("select * from user");
        while (rs.next()) {
        	System.out.print(rs.getString("Host"));
        	System.out.print("\t");
        	System.out.print(rs.getString("User"));
        	System.out.print("\t");
        	System.out.print(rs.getObject("Password"));
        	System.out.println("\t");
        }
	}
	
}
