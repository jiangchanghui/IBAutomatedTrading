package hft.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import apidemo.util.Util;

import com.benberg.struct.MarketDataTick;
import com.benberg.struct.NewOrderRequest;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.twitter.main.SendTweet;

public class QueueHandler {
	private  Logger log = Logger.getLogger( this.getClass() );
	public static QueueHandler INSTANCE = new QueueHandler();
//	 private final static String Q_create_new_order = "Q_create_new_order";
//	 private final static String Q_marketdata_tick = "Q_marketdata_tick";
//	 private final static String Ex_marketdata_routing="Ex_marketdata_routing";
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
		    channel.exchangeDeclare(Util.INSTANCE.exchange_marketdata_routing, "topic");
		    
		    channel_order = connection.createChannel();
		    channel.queueDeclare(Util.INSTANCE.queue_marketdata_tick, false, false, false, null);
		    channel_order.queueDeclare(Util.INSTANCE.queue_new_order, false, false, false, null);
		    }
		    catch (Exception e)
		    {
		    	log.fatal(e.toString(),e);
		    }
		    
		 //   channel.close();
		 //   connection.close();
	 }
	 public synchronized void SendToMarketDataTickQueue(MarketDataTick _message)
	 { 
		 try {
			 channel.basicPublish(Util.INSTANCE.exchange_marketdata_routing, _message.getTicker(), null, toBytes(_message));
			//	channel.basicPublish("", Q_marketdata_tick, null, toBytes(_message));
				log.info("SEND > "+Util.INSTANCE.exchange_marketdata_routing+" { Mkt Data Tick for '" + _message.getTicker() + "}");
			
			Cache.instance.MarketDataTick(_message);	
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.fatal(e.toString(),e);
			}
		  
		 
	 }
	 public synchronized void SendToNewOrderQueue(NewOrderRequest _message)
	 { 
		 try {
			   				
				log.info("SEND > "+Util.INSTANCE.queue_new_order+" {" + _message.toString() + "}");
				channel_order.basicPublish("", Util.INSTANCE.queue_new_order, null, toBytes(_message));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.fatal(e.toString(),e);
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
