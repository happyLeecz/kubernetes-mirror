/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 0.1
 * 
 * 
 **/
public final class Constants {
	
	private Constants() {
		super();
	}

	/**
	 * CPU demand
	 */
	public static final String KIND_CUSTOMRESOURCEDEFINTION         = "CustomResourceDefinition";
	
	public static final String KIND_CONFIGMAP                       = "ConfigMap";
	
	
	public static final String NS_KUBESYSTEM                        = "kube-system";
	
	
	public static final String YAML_KIND                            = "kind";
	
	public static final String YAML_DATA                            = "data";
	
	public static final String YAML_SPEC                            = "spec";
	
	public static final String YAML_SPEC_NAMES                      = "names";
	
	public static final String YAML_SPEC_NAMES_KIND                 = "kind";
	
	public static final String YAML_METADATA                        = "metadata";
	
	public static final String YAML_METADATA_NAME                   = "name";
	
	public static final String YAML_METADATA_NAMESPACE              = "namespace";
	
	public static final String YAML_METADATA_ANNOTATIONS            = "annotations";
	
	public static final String YAML_METADATA_MANAGEDFIELDS          = "managedFields";
	
	public static final String SQL_INSERT                           = "insert";
	
	public static final String SQL_UPDATE            				= "update";
	
	public static final String SQL_DELETE            				= "delete";
	
//	protected static final String JDBC     = "jdbc:mysql://kube-database.kube-system:3306?useUnicode=true&characterEncoding=UTF8&connectTimeout=2000&socketTimeout=6000&autoReconnect=true&&serverTimezone=Asia/Shanghai";
	
	public static final String JDBC     = "jdbc:mysql://kube-database.kube-system:3306";
	
	public static final String USER     = "root";
	
	public static final String PWD      = "onceas";
	
	public static final String DB       = "kube";

}
