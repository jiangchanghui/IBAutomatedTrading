package com.web.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.benberg.struct.NewMarketDataRequest;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ListenForWebRequests extends Thread{

	 private final static String QUEUE_IN = "rpc_queue";
	// private final static String QUEUE_OUT = "q_web_in";
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

		//    channel.queueDeclare(QUEUE_OUT, false, false, false, null);
		    channel.queueDeclare(QUEUE_IN, false, false, false, null);
		    channel.basicQos(1);
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
		      
		      BasicProperties props = delivery.getProperties();
		      BasicProperties replyProps = new BasicProperties
		                                       .Builder()
		                                       .correlationId(props.getCorrelationId())
		                                       .build();
		      
		      String message = new String(delivery.getBody());
		      
		      System.out.println(" [x] Received '" + message + "'");
		      
		      NewMarketDataRequest _message = fromBytes( delivery.getBody());
		      
		      
		      GetHistoricMarketData MDM = new  GetHistoricMarketData();
		      MDM = GetHistoricMarketData.getInstance();
		      if(_message.getType()==NewMarketDataRequest.MARKETDATA)
		      {
			      if(_message.IsRealTime())
			    	  SendReplyMessage (MDM.GetNewRealTimeDataRequest(_message),props, replyProps);
			      else
			    	  SendReplyMessage (MDM.GetMarketDataToJson(_message),props, replyProps);
		      }
		      else //request for RSI data
		      {
		    	  
		      }
		      } 
		  }
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
	

	}

	
	private  void SendReplyMessage(NewMarketDataRequest _message, BasicProperties props, BasicProperties replyProps) throws IOException
	{
		
	    if(_message != null)
	    {
	 //   channel.basicPublish("", QUEUE_OUT, null, _message.toBytes());
	    	 System.out.println("sending : "+_message.GetMarketDataJson());
	    channel.basicPublish( "", props.getReplyTo(), replyProps, _message.toBytes());
	    System.out.println(" [x] Sent reply on topic  '" + QUEUE_IN + "'");
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
