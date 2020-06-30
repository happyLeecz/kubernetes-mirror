/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.mysql.cj.jdbc.Driver;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class Mapper {
	
	/**
	 * name
	 */
	public static final String NAME = "druid.properties";
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		DruidDataSource dataSource = createDataSourceFromResource(NAME);
		Connection conn = dataSource.getConnection();
		conn.setCatalog("kube");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from pods");

        while (rs.next()) {
//        	System.out.print(rs.getString("name"));
//        	System.out.println("\t");
//        	System.out.print(rs.getString("namespace"));
//        	System.out.println("\t");
//        	System.out.print(rs.getObject("data"));
//        	System.out.println("\t");
        }
        
        stmt.close();
        conn.close();
	}
	

	static DruidDataSource createDataSourceFromResource(String resource) throws IOException {
        Properties props = new Properties();
        props.put("druid.driverClassName", Driver.class.getName());
        props.put("druid.url", "jdbc:mysql://kube-database.kube-system:3306");
        props.put("druid.username", "root");
        props.put("druid.password", "onceas");
        props.put("druid.initialSize", 10);
        props.put("druid.maxActive", 100);
        props.put("druid.maxWait", 3000);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.configFromPropety(props);
        return dataSource;
    }
	
}
