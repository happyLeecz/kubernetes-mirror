/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author wuheng
 * @since 2019.4.20
 */
public class Pusher {

	public static final Logger m_logger = Logger.getLogger(Pusher.class.getName());

	public static String queueName = "kube-events";
	
	protected final Channel channel;
	
	public Pusher() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("kube-rabbitmq.kube-system");
		factory.setUsername("root");
		factory.setPassword("onceas");
		factory.setPort(5672);
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();
        channel.queueDeclare(queueName, false, false, false, null);
	}

	public void push(JsonNode json) {
		try {
			channel.basicPublish("", queueName, null, json.toPrettyString().getBytes("UTF-8"));
			System.out.println("Producer Send +'" +json + "'");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		Pusher pusher = new Pusher();
		ObjectNode json = new ObjectMapper().createObjectNode();
		while(true) {
		json.put("hello", "world");
		pusher.push(json);
		Thread.sleep(10000);
		}
	}

}
