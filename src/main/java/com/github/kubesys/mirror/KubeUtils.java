/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.mirror;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author wuheng
 * @since 2019.4.20
 */
public class KubeUtils {

	
	public static final String YAML_METADATA                        = "metadata";
	
	public static final String YAML_METADATA_NAME                   = "name";
	
	public static final String YAML_METADATA_NAMESPACE              = "namespace";
	
	public static final String YAML_METADATA_ANNOTATIONS            = "annotations";
	
	public static final String YAML_METADATA_MANAGEDFIELDS          = "managedFields";
	
	
	/**
	 * @param json                            json
	 * @return                                node
	 */
	public static String getJsonWithoutAnotation(JsonNode json) {
		ObjectNode yaml = json.deepCopy();
		ObjectNode meta = yaml.get(YAML_METADATA).deepCopy();
		if (meta.has(YAML_METADATA_ANNOTATIONS)) {
			meta.remove(YAML_METADATA_ANNOTATIONS);
		}
		
		// > 1.18
		if (meta.has(YAML_METADATA_MANAGEDFIELDS)) {
			meta.remove(YAML_METADATA_MANAGEDFIELDS);
		}
		
		yaml.remove(YAML_METADATA);
		yaml.set(YAML_METADATA, meta);
		
		String value = yaml.toPrettyString();
		
		value = toMysqlJSON(value, "&", "\\u0026");
		value = toMysqlJSON(value, ">", "\\u003e");
		value = toMysqlJSON(value, "<", "\\u003c");
		value = toMysqlJSON(value, "\'", " \\'");
		value = toMysqlJSON(value, "\\\"", "\\\\\\\"");
		value = toMysqlJSON(value, "\\n", "\\\\n");
		value = toMysqlJSON(value, "\\\\\\\\\\\"", "\\\\\\\"");
		value = toMysqlJSON(value, "\\\\d", "\\\\\\\\d");
		
		return value;
	}


	public static String toMysqlJSON(String value, String src, String dst) {
		StringBuilder sb = new StringBuilder();
		int i = value.indexOf(src);
		if (i == -1) {
			sb.append(value);
		} else {
			sb.append(value.substring(0, i)).append(dst).append(
					toMysqlJSON(value.substring(i + src.length()), src, dst));
		}
		return sb.toString();
	}
	
	/**
	 * @param json                           json
	 * @return                               name
	 */
	public static String getName(JsonNode json) {
		return json.get(YAML_METADATA)
					.get(YAML_METADATA_NAME).asText();
	}
	
	/**
	 * @param json                           json
	 * @param namespaced                     true or false
	 * @return                               namespace
	 */
	public static String getNamespace(JsonNode json, boolean namespaced) {
		JsonNode meta = json.get(YAML_METADATA);
		return (meta.has(YAML_METADATA_NAMESPACE)) 
				? meta.get(YAML_METADATA_NAMESPACE).asText() : (namespaced ? "default" : "");
	}

	/**
	 * @param json                           json
	 * @return                               namespace
	 */
	public static String getNamespace(JsonNode json) {
		JsonNode meta = json.get(YAML_METADATA);
		return (meta.has(YAML_METADATA_NAMESPACE)) 
				? meta.get(YAML_METADATA_NAMESPACE).asText() : "default";
	}
}
