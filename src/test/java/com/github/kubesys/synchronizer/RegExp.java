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
public class RegExp {
	
	/**
	 * name
	 */
//	public static final String NAME = "\"args\": [\"echo 'dfaaf0dbd2c5111fc0d48475c174933f5753edc6c6b9c2b6df1173d2f6a5d7ef  /src/test.py' \\u003e /tmp/func.sha256 \\u0026\\u0026 sha256sum -c /tmp/func.sha256 \\u0026\\u0026 cp /src/test.py /kubeless/test.py \\u0026\\u0026 cp /src/requirements.txt /kubeless\"],";
	
//	public static final String NAME = "\"cni_network_config\": \"{\\n  \\\"name\\\": \\\"k8s-pod-network\\\",\\n  \\\"cniVersion\\\": \\\"0.3.1\\\"\\n}\"";
	
//	public static final String NAME = "#!/usr/bin/env bash\\n\\nLOKI_URI=\\\"http://${LOKI_SERVICE}:${LOKI_PORT}\\\"\\n\\nfunction setup() {\\n  apk add -u curl jq\\n  until (curl -s ${LOKI_URI}/api/prom/label/app/values | jq -e '.values[] | select(. == \\\"loki\\\")'); do\\n    sleep 1\\n  done\\n}\\n\\n@test \\\"Has labels\\\" {\\n  curl -s ${LOKI_URI}/api/prom/label | \\\\\\n  jq -e '.values[] | select(. == \\\"app\\\")'\\n}\\n\\n@test \\\"Query log entry\\\" {\\n  curl -sG ${LOKI_URI}/api/prom/query?limit=10 --data-urlencode 'query={app=\\\"loki\\\"}' | \\\\\\n  jq -e '.streams[].entries | length \\u003e= 1'\\n}\\n\\n@test \\\"Push log entry legacy\\\" {\\n  local timestamp=$(date -Iseconds -u | sed 's/UTC/.000000000+00:00/')\\n  local data=$(jq -n --arg timestamp \\\"${timestamp}\\\" '{\\\"streams\\\": [{\\\"labels\\\": \\\"{app=\\\\\\\"loki-test\\\\\\\"}\\\", \\\"entries\\\": [{\\\"ts\\\": $timestamp, \\\"line\\\": \\\"foobar\\\"}]}]}')\\n\\n  curl -s -X POST -H \\\"Content-Type: application/json\\\" ${LOKI_URI}/api/prom/push -d \\\"${data}\\\"\\n\\n  curl -sG ${LOKI_URI}/api/prom/query?limit=1 --data-urlencode 'query={app=\\\"loki-test\\\"}' | \\\\\\n  jq -e '.streams[].entries[].line == \\\"foobar\\\"'\\n}\\n\\n@test \\\"Push log entry\\\" {\\n  local timestamp=$(date +%s000000000)\\n  local data=$(jq -n --arg timestamp \\\"${timestamp}\\\" '{\\\"streams\\\": [{\\\"stream\\\": {\\\"app\\\": \\\"loki-test\\\"}, \\\"values\\\": [[$timestamp, \\\"foobar\\\"]]}]}')\\n\\n  curl -s -X POST -H \\\"Content-Type: application/json\\\" ${LOKI_URI}/loki/api/v1/push -d \\\"${data}\\\"\\n\\n  curl -sG ${LOKI_URI}/api/prom/query?limit=1 --data-urlencode 'query={app=\\\"loki-test\\\"}' | \\\\\\n  jq -e '.streams[].entries[].line == \\\"foobar\\\"'\\n}\\n";
	
	public static final String NAME = "\"prometheus.yml\": \"\\'evaluation_interval\\'\"";
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		System.out.println(NAME);
		String replaceAll = NAME.replaceAll("&&", "\\\\u0026\\\\u0026")
				.replaceAll(">", "\\\\u003e")
				.replaceAll("\\'", "\\\\\\\\'")
				.replaceAll("\\\\n", "\\\\\\\\\\n")
				.replaceAll("\\\\\"", "\\\\\\\\\\\\\"");
		System.out.println(replaceAll);
		
	}
	

	
	
}
