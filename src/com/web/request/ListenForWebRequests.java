package com.web.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.benberg.struct.NewMarketDataRequest;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ListenForWebRequests extends Thread{

	 private final static String QUEUE_IN = "q_mdm_in";
	 private final static String QUEUE_OUT = "q_web_in";
	 ConnectionFactory factory;
	 Connection connection;
	 Channel channel;
	 private void setup() throws IOException
	 {
		 factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername("Admin"); 
			factory.setPassword("Typhoon1"); 
		    connection = factory.newConnection();
		    channel = connection.createChannel();

		    channel.queueDeclare(QUEUE_OUT, false, false, false, null);
		    channel.queueDeclare(QUEUE_IN, false, false, false, null);
		 
	 }
	 
	 
	 
	public void run() {
		try {
			setup();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	//	GetHistoricMarketData MDM = new  GetHistoricMarketData();
	  //    MDM = GetHistoricMarketData.getInstance();
	    // MDM.getMarketDataToJson("IBM");		
	    
	     //MDM.getMarketDataToJson("AAPL");
	     //MDM.getMarketDataToJson("IBM");
		
		try{
		  		    
		    System.out.println(" [*] Waiting for messages on queue "+QUEUE_IN);
		    
		    QueueingConsumer consumer = new QueueingConsumer(channel);
		    channel.basicConsume(QUEUE_IN, true, consumer);
		    
		    while (true) {
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      String message = new String(delivery.getBody());
		      
		      System.out.println(" [x] Received '" + message + "'");
		      
		      NewMarketDataRequest _message = fromBytes( delivery.getBody());
		      
		      
		      GetHistoricMarketData MDM = new  GetHistoricMarketData();
		      MDM = GetHistoricMarketData.getInstance();
		      SendReplyMessage (MDM.getMarketDataToJson(_message));
		      
		    }
		  }
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
	

	}

	
	private  void SendReplyMessage(NewMarketDataRequest _message) throws IOException
	{
		
	    if(_message != null)
	    {
	    channel.basicPublish("", QUEUE_OUT, null, _message.toBytes());
	    System.out.println(" [x] Sent reply on topic  '" + QUEUE_OUT + "'");
	    }
	 //   channel.close();
	  //  connection.close();
	  
		
		
	
	}
	public  NewMarketDataRequest fromBytes(byte[] body) {
		
		NewMarketDataRequest obj = null;
	    try {
	        ByteArrayInputStream bis = new ByteArrayInputStream (body);
	        ObjectInputStream ois = new ObjectInputStream (bis);
	        obj = (NewMarketDataRequest)ois.readObject();
	        ois.close();
	        bis.close();
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	    }
	    catch (ClassNotFoundException ex) {
	        ex.printStackTrace();
	    }
	   
	    return obj;     
	}
}
