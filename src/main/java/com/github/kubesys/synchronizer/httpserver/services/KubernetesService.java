/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer.httpserver.services;

import java.sql.ResultSet;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.httpfrk.core.HttpBodyHandler;
import com.github.kubesys.synchronizer.Constants;
import com.github.kubesys.synchronizer.MysqlClient;
import com.github.kubesys.synchronizer.Starter;
import com.github.kubesys.tools.annotations.ServiceDefinition;
import com.github.kubesys.tools.annotations.api.CatalogDescriber;
import com.github.kubesys.tools.annotations.api.ParamDescriber;
import com.github.kubesys.tools.annotations.api.ServiceDescriber;

import io.github.kubesys.KubernetesClient;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * @since 2019.10.29
 *
 */

@ServiceDefinition
@CatalogDescriber(desc = "以Kubernetes格式进行生命周期管理")
public class KubernetesService extends HttpBodyHandler {
	
	public static final String SELECT         = "SELECT #TARGET# FROM #TABLE#";
	
	public static final String WHERE          = " WHERE ";
	
	public static final String CONDITION      = " JSON_EXTRACT(data, '$.#ITEM#') like '%#VALUE#%' AND ";
	
	public static final String LIMIT          = " LIMIT #FROM#, #TO#";
	
	public static final String TARGET_DATA    = "*";
	
	public static final String TARGET_COUNT   = "count(*) as count";
	
	/**
	 * 
	 */
	private final KubernetesClient kubeClient;
	
	private final MysqlClient sqlClient;
	
	public KubernetesService() throws Exception {
		super();
		this.kubeClient = Starter.getKubeClient();
		this.sqlClient  = Starter.getSqlClient();
		Starter.createSynchTargetsFromConfifMap(kubeClient.getResource(
				Constants.KIND_CONFIGMAP, Constants.NS_KUBESYSTEM, Starter.NAME));
		Starter.synchFromKubeToMysql(kubeClient, sqlClient);
	}

	@ServiceDescriber(shortName = "创建资源", desc = "以Template结尾Kind写到本地文件，其余的会注册到Kubernetes", prereq= "json参数符合[kubernetes CRD](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)规范")
	public JsonNode create(@ParamDescriber(required = true, desc = "必须是JSON格式，且满足[kubernetes CRD](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)规范", example = "{\"a\": \"b\"}") JsonNode json)
			throws Exception {
		
		return kubeClient.createResource(json);
	}

	@ServiceDescriber(shortName = "删除资源", desc = "以Template结尾Kind会从本地删除，其余的会从Kubernetes删除", prereq= "json参数符合[kubernetes CRD](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)规范")
	public JsonNode delete(
			@ParamDescriber(required = true, desc = "查看支持[kind](kind)", example = "Pod") String kind,
			@ParamDescriber(required = false, desc = "如果参数为null，(1)对于支持namespace的kind，取值为default；(2)否则为空", example = "default") String namespace,
			@ParamDescriber(required = true, desc = "资源名", example = "Any object") String name)
			throws Exception {
		
		return kubeClient.deleteResource(kind, namespace, name);
	}

	@ServiceDescriber(shortName = "更新资源", desc = "以Template结尾的资源会更新到本地文件，其余的会更新到Kubernetes", prereq= "资源存在，且json参数符合[kubernetes CRD](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)规范")
	public JsonNode update(
			@ParamDescriber(required = true, desc = "必须是JSON格式，且满足[kubernetes CRD](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)规范", example = "Any object") JsonNode json)
			throws Exception {
		
		return kubeClient.updateResource(json);
	}

	@ServiceDescriber(shortName = "查询资源", desc = "以Template结尾的资源会从本地进行查询，其余的会从Kubernetes查询", prereq= "json参数符合[kubernetes CRD](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)规范")
	public JsonNode query(
			@ParamDescriber(required = true, desc = "查看支持[kind](kind.md)", example = "Pod") String kind,
			@ParamDescriber(required = false, desc = "如果参数为null，(1)对于支持namespace的kind，取值为all-namespaces；(2)否则为空", example = "default") String namespace,
			@ParamDescriber(required = false, desc = "一页显示的数据条数，取值为null，表示一次取回全部数据", example = "10") int limit,
			@ParamDescriber(required = false, desc = "为null表示取前limit条数据，否则按照nextId取相关的数据", example = "test") int page,
			@ParamDescriber(required = false, desc = "根据label进行过滤，符合[Kubernetes规范](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/)", example = "java.util.Map") Map<String, String> labels)
			throws Exception {
		StringBuffer sqlBase = new StringBuffer();
		
		String kindName = kubeClient.getConfig().getName(kind);
		sqlBase.append(SELECT.replace("#TABLE#", kindName));
	
		String countSql = sqlBase.toString().replace("#TARGET#", TARGET_COUNT);
		ResultSet rsc = sqlClient.execWithResult(Constants.DB, countSql);
		rsc.next();
		int total = rsc.getInt("count");
		
		int l = (limit <= 0) ? 10 : limit;
		int p = (page <= 1) ? 1 : page;
		if (labels != null && !labels.isEmpty()) {
			sqlBase.append(WHERE);
			for (String key : labels.keySet()) {
				sqlBase.append(CONDITION
						.replace("#ITEM#", key.replace("#", "."))
						.replace("#VALUE#", labels.get(key)));
			}
			sqlBase.replace(sqlBase.length() - 4, sqlBase.length(), 
					LIMIT.replace("#FROM#", String.valueOf((p-1)*l))
							.replace("#TO#", String.valueOf(p*l)));
		} else {
			sqlBase.append( 
					LIMIT.replace("#FROM#", String.valueOf((p-1)*l))
							.replace("#TO#", String.valueOf(p*l)));
		}
		
		String dataSql = sqlBase.toString().replace("#TARGET#", TARGET_DATA);
		ResultSet rsd = sqlClient.execWithResult(Constants.DB, dataSql);
		ArrayNode items = new ObjectMapper().createArrayNode();
		while(rsd.next()) {
			items.add(rsd.getString("data"));
		}
		
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("kind", kind + "List");
		node.put("apiVersion", "v1");
		ObjectNode meta = new ObjectMapper().createObjectNode();
		{
			meta.put("totalCount", total);
			meta.put("remainingItemCount", (total - p*l < 0) ? 0 : (total - p*l));
			meta.put("totalCount", total);
			meta.put("continue", String.valueOf(p + 1));
			meta.put("selfLink", countSql);
		}
		node.set("metadata", meta );
		node.set("items", items);
		
		return node;
	}

	@ServiceDescriber(shortName = "获取资源", desc = "以Template结尾的资源会从本地进行查询，其余的会从Kubernetes查询。注意currentName参数只对Template资源有效，它记录的是模板信息，用于自定义资源进行更新操作，参见[自定义资源](crd.md)", prereq= "json参数符合[kubernetes CRD](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)规范")
	public JsonNode get(
			@ParamDescriber(required = true, desc = "查看支持[kind](kind)", example = "Pod") String kind,
			@ParamDescriber(required = false, desc = "如果参数为null，(1)对于支持namespace的kind，取值为default；(2)否则为空", example = "default") String namespace,
			@ParamDescriber(required = true, desc = "资源名字", example = "busybox") String name)
			throws Exception {
		
		return kubeClient.getResource(kind, (namespace != null) ? namespace : "", name);
	}
	
}
