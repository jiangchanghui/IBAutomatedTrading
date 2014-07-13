package hft.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import com.benberg.struct.MarketDataTick;
import com.benberg.struct.NewOrderRequest;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.twitter.main.SendTweet;

public class QueueHandler {
	private  Logger log = Logger.getLogger( this.getClass() );
	public static QueueHandler instance = new QueueHandler();
	 private final static String Q_create_new_order = "Q_create_new_order";
	 private final static String Q_marketdata_tick = "Q_marketdata_tick";
	 private final static String Ex_marketdata_routing="Ex_marketdata_routing";
	 // private final static String QUEUE_OUT = "q_web_in";
	 ConnectionFactory factory;
	 Connection connection;
	 Channel channel;
	 Channel channel_order;
	 
	
	 
	 
	 
	 public void setup() 
	 {
		
		    try{
		    factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername("Admin"); 
			factory.setPassword("Admin"); 
			//for market data ticks - Topic method
		    connection = factory.newConnection();
		    channel = connection.createChannel();
		    channel.exchangeDeclare(Ex_marketdata_routing, "topic");
		    
		    channel_order = connection.createChannel();
		    channel.queueDeclare(Q_marketdata_tick, false, false, false, null);
		    channel_order.queueDeclare(Q_create_new_order, false, false, false, null);
		    }
		    catch (Exception e)
		    {
		    	log.fatal(e.getStackTrace().toString());
		    }
		    
		 //   channel.close();
		 //   connection.close();
	 }
	 public synchronized void SendToMarketDataTickQueue(MarketDataTick _message)
	 { 
		 try {
			 channel.basicPublish(Ex_marketdata_routing, _message.getTicker(), null, toBytes(_message));
			//	channel.basicPublish("", Q_marketdata_tick, null, toBytes(_message));
				log.info(" [x] Sent Tick for '" + _message.getTicker() + "' to exchange "+Ex_marketdata_routing);
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.fatal(e.getStackTrace().toString());
			}
		  
		 
	 }
	 public synchronized void SendToNewOrderQueue(NewOrderRequest newOrderRequest)
	 { 
		 try {
			   
				channel_order.basicPublish("", Q_create_new_order, null, toBytes(newOrderRequest));
				log.info(" [x] Sent Order creation request for '" + newOrderRequest.toString() + "'");
			
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.fatal(e.getStackTrace().toString());
			}
		  
		 
	 }
	 public byte[] toBytes(Object obj) {
	      byte[]bytes; 
	      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	      try{ 
	        ObjectOutputStream oos = new ObjectOutputStream(baos); 
	        oos.writeObject(obj); 
	        oos.flush();
	        oos.reset();
	        bytes = baos.toByteArray();
	        oos.close();
	        baos.close();
	      } catch(IOException e){ 
	        bytes = new byte[] {};
	    	log.fatal(e.getStackTrace().toString());
	      }         
	      return bytes; 
	    }
}
