package com.web.request;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ListenForWebRequests extends Thread{

	 private final static String QUEUE_NAME = "q_web_receive";
	public void run() {
		
		GetHistoricMarketData MDM = new  GetHistoricMarketData();
	      MDM = GetHistoricMarketData.getInstance();
	     MDM.getMarketDataToJson("IBM");		
	    
	     MDM.getMarketDataToJson("AAPL");
	     MDM.getMarketDataToJson("IBM");
		/*
		try{
		    ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    Connection connection = factory.newConnection();
		    Channel channel = connection.createChannel();

		    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		    System.out.println(" [*] Waiting for messages on queue "+QUEUE_NAME);
		    
		    QueueingConsumer consumer = new QueueingConsumer(channel);
		    channel.basicConsume(QUEUE_NAME, true, consumer);
		    
		    while (true) {
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      String message = new String(delivery.getBody());
		      
		      System.out.println(" [x] Received '" + message + "'");
		      
		      GetHistoricMarketData MDM = new  GetHistoricMarketData();
		      MDM = GetHistoricMarketData.getInstance();
		      SendReplyMessage (MDM.get(message));
		      
		    }
		  }
		catch (Exception e)
		{
			
			
		}
	
*/
	}

	
	private static void SendReplyMessage(String message) throws IOException
	{
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    
	    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
	    System.out.println(" [x] Sent '" + message + "'");
	    
	    channel.close();
	    connection.close();
	  
		
		
	
	}
	
}
