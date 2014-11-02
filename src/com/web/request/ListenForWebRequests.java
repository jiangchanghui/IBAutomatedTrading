package com.web.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;

import com.benberg.struct.NewMarketDataRequest;
import com.benberg.struct.RequestType;
import com.ib.cache.LevelOneSnapshot;
import com.ib.cache.MarketDataCache;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ListenForWebRequests extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
	 private final static String QUEUE_IN = "rpc_queue";
	// private final static String QUEUE_OUT = "q_web_in";
	 ConnectionFactory factory;
	 Connection connection;
	 Channel channel;
	 private void setup() throws IOException
	 {
		 log.info("Initialising Chart Server..." );
		 factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername("Admin"); 
			factory.setPassword("Admin"); 
		    connection = factory.newConnection();
		    channel = connection.createChannel();

		//    channel.queueDeclare(QUEUE_OUT, false, false, false, null);
		    channel.queueDeclare(QUEUE_IN, false, false, false, null);
		    channel.basicQos(1);
		    
		 log.info("Initialising Chart Server...OK" );
	 }
	 
	 
	 
	public void run() {
		try {
			setup();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			log.info(e1.toString(),e1);
			return;
		}
	//	GetHistoricMarketData MDM = new  GetHistoricMarketData();
	  //    MDM = GetHistoricMarketData.getInstance();
	    // MDM.getMarketDataToJson("IBM");		
	    
	     //MDM.getMarketDataToJson("AAPL");
	     //MDM.getMarketDataToJson("IBM");
		
		try{
		  		    
		    log.info(" [*] Waiting for chart requests on queue "+QUEUE_IN);
		    
		    QueueingConsumer consumer = new QueueingConsumer(channel);
		    channel.basicConsume(QUEUE_IN, true, consumer);
		    
		    while (true) {
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      
		      BasicProperties props = delivery.getProperties();
		      BasicProperties replyProps = new BasicProperties
		                                       .Builder()
		                                       .correlationId(props.getCorrelationId())
		                                       .build();
		      
		      NewMarketDataRequest _message = fromBytes( delivery.getBody());
		      
		      log.info(" RECV on \\"+QUEUE_IN+"> Received new chart request '" + _message.toString() + "'");
		      
		     
		      
		      
		      GetHistoricMarketData HistoricMarketDataHandler = new  GetHistoricMarketData();
		      HistoricMarketDataHandler = GetHistoricMarketData.getInstance();
		      
		      
		      if(_message.getType().equals(RequestType.QUOTE))
		      {
		    	  String ticker = _message.GetTicker();
		    	  String quote;
		    	  LevelOneSnapshot snapshot = MarketDataCache.INSTANCE.GetLevelOneSnapshot(ticker);
		    	  if(snapshot!=null)
		    		  quote = snapshot.toString();
		    	  else
		    		  quote = ticker;
		    	 SendReplyMessage (new NewMarketDataRequest(ticker,_message.GetCorrelationId(),RequestType.QUOTE,quote),props,replyProps);
		      	 
		      }
		      if(_message.IsRealTime())
		    	  SendReplyMessage (HistoricMarketDataHandler.GetNewRealTimeDataRequest(_message),props, replyProps);
		      
		      if (_message.GetRequestType().equals(RequestType.CHART_DAY)) //for PhoneCharts - Show trading on the day so far.
		    	  SendReplyMessage (HistoricMarketDataHandler.GetHistoricalMarketData(_message),props, replyProps);
		      
		      
		    
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
	    	 System.out.println("sending : "+_message.GetMessage());
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
