/**
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.synchronizer;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 2.3.0
 * @since 2020.2.15
 * 
 **/
public class Puller {

	/*****************************************************************************************
	 * 
	 * Main
	 * 
	 *****************************************************************************************/

	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("kube-rabbitmq.kube-system");
		factory.setUsername("root");
		factory.setPassword("onceas");
		factory.setPort(30304);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(Pusher.queueName, false, false, false, null);
		// 创建队列消费者
		Consumer consumer = new DefaultConsumer(channel) {
			
			@Override
            public void handleDelivery(String consumerTag, Envelope envelope, 
            		AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
                System.out.println("receive:" + message);
            }
		};
		// 消息确认机制
		channel.basicConsume(Pusher.queueName, true, consumer);

	}

}
