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
	public static final String NAME = "\"kubeconfig\": \"apiVersion: v1\\nclusters:\\n- cluster:\\n    certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUN5RENDQWJDZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJd01EWXhOakUwTXpJek1sb1hEVE13TURZeE5ERTBNekl6TWxvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBTU9LCnR2eDhwdUw2bDc3Y3RpUmg2U3JHUkxjWGt6Q2p0dGJ4cEtMRUwwUG5ONkd3SE5zVU9yRUk2WjdsUkw5Vlk1VVEKRlJTckwwWWNqTUFGNTNVYktiVTNQOEdYMWJpekkxaVpBTS80NDBoQ08xOVpVTW4xUkVTUS9OMjZqMDRwYzJadgp2N01yVFFGdVZ2R093cTRyQmNwWUZIRGxWSXBLdVpHZWRBQnlPWnl2NTRDWkZLSFBXQXhaZ1NwWlYwM0d4WFhFCnhnbkpBR3g0ajh6U1pMZmludjlENFZjMFpEYVJ3c0hKV0lNeFMrY2gzRzlrOE5FYU82YW9CZ0hHRmxhelMwZkEKZkhyTy9OWjZYLzRZUXNqaWVCZUhSYTBhYmtteldEMHV1aXdQeDZlWFJqM2FJTzBEVkRiYVVER0lSLzJOL0FhSwpleHo3cEc1cTVPOUM5OGRCdHhFQ0F3RUFBYU1qTUNFd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFHS3ZkWXBZRGtrS3hpR1phQWdJcE1oN256bGsKOFRVbi93eE9WcG9pdndzTlUzcFpTZnkwS01uS0VQdTRWRG00Ti9WUFlLY0tmeEp1bVU0U1MxbUcrS2NZNkFGbQpiRTgyV2UzWkVsdHV0Ni9lQW9WdGZaSE9oWGMvNzJWZmVLN1ZqUXdMQlBaSms2c25mVW0vMHNIb3IxYUJuWTRBCmFrN3FYeFh3TnQ0RWpyZmdTMHZ3aVMrTTV4OUhTNG03S28yTW5hTjhuODR1MFlMTTFLeTJFUDIyV2VIa0Q0QlkKTXhJamYyUDl0clVsbnB6K3pLeUJyOXhKVEhMZWY3d3lGUUVOTkh0Um9aU0tHb1lFU2ViMWt2Z3NLVElIcGh5eAo4Rlc1U21mU2lXTTBWZHFQakdXbFFjQ0NxelJYalhaaW5iUUc2Vk4valZPRG1mb0JtTmwyV2RsM1hocz0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=\\n    server: https://172.17.104.190:6443\\n  name: \\\"\\\"\\ncontexts: null\\ncurrent-context: \\\"\\\"\\nkind: Config\\npreferences: {}\\nusers: null\\n\"";
	
	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		System.out.println(NAME.replaceAll("&&", "\\\\u0026\\\\u0026")
				.replaceAll(">", "\\\\u003e")
				.replaceAll("\\'", "\\\\'")
				.replaceAll("\\\\n", "\\\\\\\\n"));
	}
	

	
	
}
