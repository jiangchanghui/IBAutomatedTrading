package com.ben.mdm.server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import apidemo.util.Util;


import com.ben.common.Message;
import com.benberg.struct.MarketDataTick;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class MarketDataListener extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
	ObjectOutputStream sOutput;
	
	public MarketDataListener(ObjectOutputStream sOutput)
	{
		this.sOutput = sOutput;
	}

	public void run(){
		try{
    ConnectionFactory factory = new ConnectionFactory();
    
    factory.setHost("localhost");
    factory.setUsername("Admin"); 
	factory.setPassword("Admin");    
    
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(Util.INSTANCE.exchange_marketdata_routing, "topic");
    String queueName = channel.queueDeclare().getQueue();

    channel.queueBind(queueName, Util.INSTANCE.exchange_marketdata_routing, "#");
   

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(queueName, true, consumer);

    while (true) {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        
        MarketDataTick _message = fromBytes( delivery.getBody());
	    String routingKey = delivery.getEnvelope().getRoutingKey();
        
        if(Cache.INSTANCE.getMDMSubscribed().contains(routingKey))
        {
        	
        	sOutput.writeObject(new Message(Message.REAL_TIME_BAR,routingKey,_message.getBar(),Cache.INSTANCE.getRequestIdFromTicker(routingKey)));
        }
	
    	}	
		}
		catch(Exception e)
		{
			log.error(e.toString(),e);
		}
	}
	private MarketDataTick fromBytes(byte[] body) {
		
		MarketDataTick obj = null;
		    try {
		        ByteArrayInputStream bis = new ByteArrayInputStream (body);
		        ObjectInputStream ois = new ObjectInputStream (bis);
		        obj = (MarketDataTick)ois.readObject();
		        ois.close();
		        bis.close();
		    }
		    catch (Exception e) {
		    	log.error(e.toString(),e);
		    }
		  
		   
		    return obj;     
		}
}
