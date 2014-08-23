package com.web.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.swing.table.AbstractTableModel;

import apidemo.AccountInfoPanel;
import apidemo.CreateOrderFromEmail;
import apidemo.OrdersPanel;
import apidemo.AccountSummaryPanel.SummaryModel;
import apidemo.AccountSummaryPanel.SummaryRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionModel;
import apidemo.TopModel.TopRow;
import apidemo.TradesPanel;


import com.google.gson.Gson;

import com.google.gson.annotations.SerializedName;

import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;

import java.util.ArrayList;

import java.util.List;

import com.ib.client.ExecutionFilter;
import com.ib.controller.AccountSummaryTag;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.OrderStatus;
import com.ib.controller.OrderType;
import com.ib.controller.ApiController.IAccountSummaryHandler;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.SecType;
import com.ib.sample.IBTradingMain;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ShutdownSignalException;
import com.reademail.main.OrderTemplate;
import com.reademail.main.mailReader;
import apidemo.TopModel.TopRow;



@WebService 
public class Index extends Thread{
	private static final Logger log = Logger.getLogger( Index.class.getName() );
	 private static String queue_web_request = "";
	 private static String queue_web_response = "";
	 private static String RPC_QUEUE_NAME = "rpc_queue";
	 private static String QUsername="";
	 private  static String QPassword="";
	 ConnectionFactory factory;
	 Connection connection;
	 private Channel channel;
	 private String requestQueueName = "rpc_queue";
	 private String replyQueueName;
	 private QueueingConsumer consumer;
	  final CreateOrderFromEmail _CreateOrder = new CreateOrderFromEmail();		
	 public void run()
	{
		try{
			AttachLogHandler();
			Properties props = new Properties();
			log.log(Level.INFO ,"Processing config entries");
			props.load(new FileInputStream("c:\\config.properties"));
			QUsername = props.getProperty("qusername");
	    	QPassword = props.getProperty("qpassword");
	    	queue_web_response = props.getProperty("queue_web_response");
	    	queue_web_request = props.getProperty("queue_web_request");
	    	log.log(Level.INFO ,"Processing config entries complete");
	    	
	    	
	    	
	    	factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername(QUsername); 
			factory.setPassword(QPassword); 
			factory.setVirtualHost("/");   
		    
			 connection = factory.newConnection();
			 
			
		        Channel channel = connection.createChannel();

		        channel.exchangeDeclare("Exchange.Web", "topic");
		        String queueName = channel.queueDeclare().getQueue();

		        

		    
		         channel.queueBind(queueName, "Exchange.Web", "web.request.*");
		      

		        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		        QueueingConsumer consumer = new QueueingConsumer(channel);
		        channel.basicConsume(queueName, true, consumer);

			 
			 
			 
         /*
			 Channel channel_Recv = connection.createChannel();
             Channel channel_Send = connection.createChannel();
             channel_Recv.queueDeclare(queue_web_request, false, false, false, null);
             channel_Send.queueDeclare(queue_web_response, false, false, false, null);
             
            
             
             QueueingConsumer consumer = new QueueingConsumer(channel_Recv);
             channel_Recv.basicConsume(queue_web_request, true, consumer);
             
             log.log(Level.INFO,"Initialised Receive Queue: {0} and Send Queue : {1} for web requests",new Object[]{queue_web_request,queue_web_response});
           */
             
             
             JSONParser parser = new JSONParser();
             JsonKeyFinder finder = new JsonKeyFinder();
             
             String CorrelationID = "";
             String message = "";
             while (true) {
               try{
                     
                     
              // log.log(Level.INFO,"Trading waiting for web querys on Queue : {0}",queue_web_request);
               QueueingConsumer.Delivery delivery = consumer.nextDelivery();
               String received = new String(delivery.getBody());
               String routingKey = delivery.getEnvelope().getRoutingKey();
               System.out.println(" [x] Received '" + routingKey + "':'" + message + "'");
              
               
               JSONObject json = (JSONObject)new JSONParser().parse(received);
               System.out.println("Message=" + json.get("Message"));
               System.out.println("CorrelationID=" + json.get("CorrelationID"));
                            
               CorrelationID =json.get("CorrelationID").toString();     
               message = json.get("Message").toString();
               
               log.log(Level.INFO,"Received new message on Topic {0} : {1}",new Object[]{queue_web_request,message});
               String Response="";
		      
		      
		    
		      if (message.equals("GETORDERS"))
		      {
		    	Response = GetOrders();
		      }
		      if (message.equals("GETPOSITIONS"))
		      {
		    	Response = GetOpenPositions();
		      }
		      if (message.equals("GETHISTORY"))
		      {
		    	Response = GetHistory();
		      }
		      if (message.equals("CANCELALL"))
		      {
		    	Response = CancelAllOrders();
		      }
		      if (message.equals("ISCONNECTED"))
		      {
		    	Response = IsConnected();
		      }
		      if (message.equals("RUN_EMAIL_LISTENER"))
		      {
		    	  Runtime.getRuntime().exec("cmd /k start cmd /c java -jar C:\\EmailListener.jar ");
		    	Response = "Executed Successfully";
		      }
		      if (message.startsWith("NEW_ORDER"))
		      {
		    	  String[] parts = message.split(";");
		    	  
		    	  String _Symbol = parts[1];
		    	  int _Quantity = Integer.parseInt(parts[2]);
		    	  Action _Side = Action.valueOf(parts[3].toUpperCase());
		    	  
		    	  log.log(Level.INFO,"Routing order for creation from Email request {0}", message);
		    	  _CreateOrder.CreateOrder(_Symbol,_Quantity,_Side,0.0);
		    	  Response = "Routed order for "+_Side.toString()+" "+_Quantity+" "+_Symbol+" at MKT";
		      }
		      if (message.equals("GET_ERRORS"))
		      {
		    	  Response = GetErrors();
		    	  
		      }
		      if (message.equals("CHECK_EMAIL_LISTENER"))
		      {
		    	  Response = CheckEmailListener();
		    	  
		      }
		      if (message.equals("GET_EXECUTIONS"))
		      {
		    	  Response = GetExecutions();
		    	  
		      }
		      
		 
		      
		      String routingKeyResponse = "web.response."+CorrelationID;
		      
		      
		      
		      channel.basicPublish("Exchange.Web", routingKeyResponse, null, Response.getBytes());
		      
		      System.out.println(" Published on  : '" + routingKeyResponse + "':'" + Response + "'");
		      
		      
            //  log.log(Level.INFO,"Sent WebReply message on Topic {0} : {1}",new Object[]{queue_web_response,Response});
		      }
		      catch(Exception e)
		      {
		    	  log.log(Level.SEVERE,"Web Request listener error", e.toString());
		      }
		    
		    
		    
		    }
		}
		catch (Exception e)
		{
			 log.log(Level.SEVERE,"Web Request listener has failed with error {0}",e.toString());
		}
		
		
	}
	 
	 private String CheckEmailListener() throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		
		 String message = "PING";
		 channel = connection.createChannel();
		 String response = "";
		    replyQueueName = channel.queueDeclare().getQueue(); 
		    consumer = new QueueingConsumer(channel);
		    channel.basicConsume(replyQueueName, true, consumer);
		 
		    String corrId = java.util.UUID.randomUUID().toString();

		    BasicProperties props = new BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();

channel.basicPublish("", requestQueueName, props, message.getBytes());

			while (true) {
				
			QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000);
			if (delivery !=null)
			{
				if (delivery.getProperties().getCorrelationId().equals(corrId)) 
				{
						response = new String(delivery.getBody());
							break;
				}
			}
			else
			{
				response = "UNREACHABLE";
				break;
			}
			}

return response; 
		
		 
		
	}

	private String IsConnected()
	 {
		if ( IBTradingMain.INSTANCE.controller().IsConnected()==true)
		{
			return "CONNECTED";	
		}
		else
		{
			return "DISCONNECTED";
		}
		 
	 }
	 
	 
	 private String GetErrors()
	 {
		 Map<String,String> m_errorsMap = new HashMap<String,String>();
			
			m_errorsMap = IBTradingMain.INSTANCE.m_errorMap;

			 LinkedList l_cols = new LinkedList();
		        LinkedList l_final = new LinkedList();
		        JSONObject obj1 = new JSONObject();
		        JSONObject obj_cols_1 = new JSONObject();
		        JSONObject obj_cols_2 = new JSONObject();
			
		        obj_cols_1.put("id", "");
		        obj_cols_1.put("label", "Time");
		        obj_cols_1.put("type", "string");

		        obj_cols_2.put("id", "");
		        obj_cols_2.put("label", "Error Message");
		        obj_cols_2.put("type", "string");
		        
		        l_cols.add(obj_cols_1);
		        l_cols.add(obj_cols_2);
		
		        obj1.put("cols", l_cols);
		        log.log(Level.INFO,"Found {0} in error histroy ",m_errorsMap.size());
		        for (Entry<String, String> entry : m_errorsMap.entrySet())
		        {
		          
		           
		           
		            
		    	LinkedList l1_rows = new LinkedList();
	 			JSONObject obj_row1 = new JSONObject();
		        JSONObject obj_row2 = new JSONObject();
		        
		  	  
		        obj_row1.put("v", entry.getKey());
		        obj_row1.put("f", null);
		        obj_row2.put("v", entry.getValue());
		        obj_row2.put("f", null);
		        
		        l1_rows.add(obj_row1);
		        l1_rows.add(obj_row2);
		       
		        LinkedHashMap m1 = new LinkedHashMap();
		        m1.put("c", l1_rows);
	            l_final.add(m1);
		        }
		        obj1.put("rows", l_final);
			return obj1.toJSONString();
		 
	 }
	 
	private String GetHistory() {

	Map<String,OrderTemplate> m_ordersMap = new HashMap<String,OrderTemplate>();
		
		m_ordersMap = IBTradingMain.INSTANCE.m_ordersMap;

		 LinkedList l_cols = new LinkedList();
	        LinkedList l_final = new LinkedList();
	        JSONObject obj1 = new JSONObject();
	        JSONObject obj_cols_1 = new JSONObject();
	        JSONObject obj_cols_2 = new JSONObject();
		
	        obj_cols_1.put("id", "");
	        obj_cols_1.put("label", "Time");
	        obj_cols_1.put("type", "string");

	        obj_cols_2.put("id", "");
	        obj_cols_2.put("label", "Created Order");
	        obj_cols_2.put("type", "string");
	        
	        l_cols.add(obj_cols_1);
	        l_cols.add(obj_cols_2);
	
	        obj1.put("cols", l_cols);
	        log.log(Level.INFO,"Found {0} in message histroy ",m_ordersMap.size());
	        for (Map.Entry<String, OrderTemplate> entry : m_ordersMap.entrySet())
	        {
	          
	           
	           
	            
	    	LinkedList l1_rows = new LinkedList();
 			JSONObject obj_row1 = new JSONObject();
	        JSONObject obj_row2 = new JSONObject();
	        
	  	  
	        obj_row1.put("v", entry.getKey());
	        obj_row1.put("f", null);
	        obj_row2.put("v", entry.getValue().getSide().toString()+" "+entry.getValue().getTicker()+" "+entry.getValue().getQuantity());
	        obj_row2.put("f", null);
	        
	        l1_rows.add(obj_row1);
	        l1_rows.add(obj_row2);
	       
	        LinkedHashMap m1 = new LinkedHashMap();
	        m1.put("c", l1_rows);
            l_final.add(m1);
	        }
	        obj1.put("rows", l_final);
		return obj1.toJSONString();
	}

	
	
	private String CancelAllOrders()
	{
		try
		{
			IBTradingMain.INSTANCE.controller().cancelAllOrders();
			return "Cancel sent for all orders";
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE,e.toString());
			return e.toString();
		}
	}
	  PositionModel m_model = new PositionModel();
	private String GetOpenPositions()
	{
		 LinkedList l_cols = new LinkedList();
	        LinkedList l_final = new LinkedList();
	        JSONObject obj1 = new JSONObject();
	        JSONObject obj_cols_1 = new JSONObject();
	        JSONObject obj_cols_2 = new JSONObject();
	        JSONObject obj_cols_3 = new JSONObject();
	        JSONObject obj_cols_4 = new JSONObject();
		
		 obj_cols_1.put("id", "");
	        obj_cols_1.put("label", "Ticker");
	        obj_cols_1.put("type", "string");

	        obj_cols_2.put("id", "");
	        obj_cols_2.put("label", "Quantity");
	        obj_cols_2.put("type", "number");

	        obj_cols_3.put("id", "");
	        obj_cols_3.put("label", "AvgPx");
	        obj_cols_3.put("type", "string");

	        obj_cols_4.put("id", "");
	        obj_cols_4.put("label", "LastPrice");
	        obj_cols_4.put("type", "string");

			
			
	        l_cols.add(obj_cols_1);
	        l_cols.add(obj_cols_2);
	        l_cols.add(obj_cols_3);
	        l_cols.add(obj_cols_4);
	        obj1.put("cols", l_cols);
		
		
		log.log(Level.INFO ,"Getting Position data for all Symbols");
		
		IBTradingMain.INSTANCE.controller().reqPositions( m_model);
		Object obj_response=null;
		int _iterator=0;
		while (m_model.getRowCount()==0 && _iterator<10)
		{
		
				
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			_iterator++;
			
		}
		log.log(Level.INFO ,"{0} Executions found",m_model.getRowCount());
		for (int i=0;i<m_model.getRowCount();i++)
		{
		//	log.log(Level.INFO ,"{0} {1} avg price of {2}",new Object[]{m_model.getValueAt(i, 1),m_model.getValueAt(i, 3),m_model.getValueAt(i, 4)});
						
			 obj_response = m_model.getValueAt(i, 1);
			 String _Symbol = (String) (obj_response == null ? "" :  obj_response);
			
			 obj_response = m_model.getValueAt(i, 3);
			 int Quantity = (Integer) (obj_response == null ? "" :  obj_response);
			
			 obj_response = m_model.getValueAt(i, 4);
			String AvgPrice = (String) (obj_response == null ? "" :  obj_response);
			
			 obj_response = m_model.getValueAt(i, 2);
				String Symbol = (String) (obj_response == null ? "" :  obj_response);
				
			double LastPx =0.0;
			
			LinkedList l1_rows = new LinkedList();
 			JSONObject obj_row1 = new JSONObject();
	        JSONObject obj_row2 = new JSONObject();
	        JSONObject obj_row3 = new JSONObject();
	        JSONObject obj_row4 = new JSONObject();
	  	  
	        obj_row1.put("v", _Symbol);
	        obj_row1.put("f", null);
	        obj_row2.put("v", Quantity);
	        obj_row2.put("f", null);
	        obj_row3.put("v", AvgPrice);
	        obj_row3.put("f", null);
	        obj_row4.put("v", LastPx);
	        obj_row4.put("f", null);
	        
	        l1_rows.add(obj_row1);
	        l1_rows.add(obj_row2);
	        l1_rows.add(obj_row3);
	        l1_rows.add(obj_row4);
	        LinkedHashMap m1 = new LinkedHashMap();
	        m1.put("c", l1_rows);
            l_final.add(m1);
				
			
		}
	    obj1.put("rows", l_final);
		
		return obj1.toJSONString();
	}
	 
	private String GetOrders()
	{
		Object obj_response=null;
		 LinkedList l_cols = new LinkedList();
	        LinkedList l_final = new LinkedList();
	        JSONObject obj1 = new JSONObject();
	        JSONObject obj_cols_1 = new JSONObject();
	        JSONObject obj_cols_2 = new JSONObject();
	        JSONObject obj_cols_3 = new JSONObject();
	        JSONObject obj_cols_4 = new JSONObject();
		OrdersModel m_model = new OrdersModel();
		IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);
		
		  long _timeout = 5000;
		  long _delta=0;
		  int _iterator=0;
		while (m_model.getRowCount()==0 && _iterator<10)
		{
			
				
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			_iterator++;
			
		}
		log.log(Level.FINEST ,"{0} Orders found",m_model.getRowCount());
		
		
		
		List<Order> _OrdersList = new ArrayList<Order>();
		

		   // Columns
       
        obj_cols_1.put("id", "");
        obj_cols_1.put("label", "Ticker");
        obj_cols_1.put("type", "string");

        obj_cols_2.put("id", "");
        obj_cols_2.put("label", "Side");
        obj_cols_2.put("type", "string");

        obj_cols_3.put("id", "");
        obj_cols_3.put("label", "Quantity");
        obj_cols_3.put("type", "number");

        obj_cols_4.put("id", "");
        obj_cols_4.put("label", "Status");
        obj_cols_4.put("type", "string");
		
		
        l_cols.add(obj_cols_1);
        l_cols.add(obj_cols_2);
        l_cols.add(obj_cols_3);
        l_cols.add(obj_cols_4);
        obj1.put("cols", l_cols);
		
        
        for(int i=0;i<m_model.getRowCount();i++)
		{
			
			 obj_response = m_model.getValueAt(i, 6);
			 String _Symbol = (String) (obj_response == null ? "" :  obj_response);
			
			 obj_response = m_model.getValueAt(i, 5);
			 int Quantity = (Integer) (obj_response == null ? "" :  obj_response);
			
			 obj_response = m_model.getValueAt(i, 4);
			 Action Side = (Action) (obj_response == null ? "" :  obj_response);
			
			 obj_response = m_model.getValueAt(i, 7);
			 OrderStatus Status = (OrderStatus) (obj_response == null ? "" :  obj_response);
			 
			 
			 
			 
			 
			 	LinkedList l1_rows = new LinkedList();
	 			JSONObject obj_row1 = new JSONObject();
		        JSONObject obj_row2 = new JSONObject();
		        JSONObject obj_row3 = new JSONObject();
		        JSONObject obj_row4 = new JSONObject();
		        obj_row1.put("v", _Symbol);
		        obj_row1.put("f", null);
		        obj_row2.put("v", Quantity);
		        obj_row2.put("f", null);
		        obj_row3.put("v", Side.toString());
		        obj_row3.put("f", null);
		        obj_row4.put("v", Status.toString());
		        obj_row4.put("f", null);
		        
		        l1_rows.add(obj_row1);
		        l1_rows.add(obj_row2);
		        l1_rows.add(obj_row3);
		        l1_rows.add(obj_row4);
		        LinkedHashMap m1 = new LinkedHashMap();
		        m1.put("c", l1_rows);
                l_final.add(m1);
			 
		//	Order order = new Order(_Symbol,Side,Quantity,Status);
			
		//	_OrdersList.add(order);
			
		}
        obj1.put("rows", l_final);
    	      
        
       return obj1.toJSONString();
        
        

	}
	private SummaryModel m_summary = new SummaryModel();
	private double GetFarPrice(String Symbol)
	{
		try{
			
		NewContract contract = new NewContract();
		NewOrder order = new NewOrder();
		contract.symbol(Symbol);
				
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");
		
			
		
		TopRow row = new TopRow( null, contract.description() );
		//m_rows.add( row);
		AccountInfoPanel A = new AccountInfoPanel();
	//	IBTradingMain.INSTANCE.controller().reqAccountUpdates(true, "DU172464", A);
		
	//	IBTradingMain.INSTANCE.controller().reqAccountSummary( "All", AccountSummaryTag.values(), m_summary);
		
IBTradingMain.INSTANCE.controller().reqTopMktData(contract, "", false, row);
	//	fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
		
		return row.m_last;
		}
		catch (Exception e)
		{
			log.log(Level.WARNING,e.toString());
			return 0.0;
			
		}
	}
	
	
	private String GetExecutions()
	{
		try{
	 TradesPanel m_tradesPanel = new TradesPanel();
				IBTradingMain.INSTANCE.controller().reqExecutions2( new ExecutionFilter(), m_tradesPanel);
				
			
				
	ArrayList<apidemo.TradesPanel.FullExec> _Execs = new ArrayList<apidemo.TradesPanel.FullExec>();
			_Execs = m_tradesPanel.getExecutions();
	
			 LinkedList l_cols = new LinkedList();
		        LinkedList l_final = new LinkedList();
		        JSONObject obj1 = new JSONObject();
		        JSONObject obj_cols_1 = new JSONObject();
		        JSONObject obj_cols_2 = new JSONObject();
		        JSONObject obj_cols_3 = new JSONObject();
		        JSONObject obj_cols_4 = new JSONObject();
		        JSONObject obj_cols_5 = new JSONObject();
			 obj_cols_1.put("id", "");
		        obj_cols_1.put("label", "Side");
		        obj_cols_1.put("type", "string");

		        obj_cols_2.put("id", "");
		        obj_cols_2.put("label", "Ticker");
		        obj_cols_2.put("type", "string");

		        obj_cols_3.put("id", "");
		        obj_cols_3.put("label", "Quantity");
		        obj_cols_3.put("type", "number");

		        obj_cols_4.put("id", "");
		        obj_cols_4.put("label", "T.Px");
		        obj_cols_4.put("type", "number");
		       
		        obj_cols_5.put("id", "");
		        obj_cols_5.put("label", "C.Px");
		        obj_cols_5.put("type", "number");
				
				
		        l_cols.add(obj_cols_1);
		        l_cols.add(obj_cols_2);
		        l_cols.add(obj_cols_3);
		        l_cols.add(obj_cols_4);
		        l_cols.add(obj_cols_5);
		        obj1.put("cols", l_cols);	
			
		        for(apidemo.TradesPanel.FullExec exec : _Execs)
				{
					
					String Symbol = exec.m_contract.symbol();
					int Quantity=exec.m_trade.m_shares;
					double TPx = exec.m_trade.m_avgPrice;
					double CPx = exec.m_trade.m_price;
					String Side = exec.m_trade.m_side;
					 
					 
					 
					 
					 	LinkedList l1_rows = new LinkedList();
			 			JSONObject obj_row1 = new JSONObject();
				        JSONObject obj_row2 = new JSONObject();
				        JSONObject obj_row3 = new JSONObject();
				        JSONObject obj_row4 = new JSONObject();
				        JSONObject obj_row5 = new JSONObject();
				        obj_row1.put("v", Side);
				        obj_row1.put("f", null);
				        obj_row2.put("v", Symbol);
				        obj_row2.put("f", null);
				        obj_row3.put("v", Quantity);
				        obj_row3.put("f", null);
				        obj_row4.put("v", TPx);
				        obj_row4.put("f", null);
				        obj_row5.put("v", CPx);
				        obj_row5.put("f", null);
				        
				        l1_rows.add(obj_row1);
				        l1_rows.add(obj_row2);
				        l1_rows.add(obj_row3);
				        l1_rows.add(obj_row4);
				        l1_rows.add(obj_row5);
				        LinkedHashMap m1 = new LinkedHashMap();
				        m1.put("c", l1_rows);
		                l_final.add(m1);
					 
				//	Order order = new Order(_Symbol,Side,Quantity,Status);
					
				//	_OrdersList.add(order);
					
				}
		        obj1.put("rows", l_final);
		        return obj1.toJSONString();
		}
		catch (Exception e)
		{
			return "";
		}
		
		        
		    
		        
			
	}
	
	
	
	class Order {
		String Symbol;
		Action Side;
		int Quantity;
		OrderStatus Status;
		
		Order(String Symbol, Action Side, int Quantity,OrderStatus Status)
		{
			this.Symbol = Symbol;
			this.Side = Side;
			this.Quantity = Quantity;
			this.Status =Status;
		}
		
		String getSymbol()
		{
			return Symbol;
		}
		Action getSide()
		{
			return Side;
		}
		int getQuantity()
		{
			return Quantity;
		}
		OrderStatus getStatus()
		{
			return Status;
		}
	}
	public class PositionModel extends AbstractTableModel implements IPositionHandler {
		HashMap<PositionKey,PositionRow> m_map = new HashMap<PositionKey,PositionRow>();
		ArrayList<PositionRow> m_list = new ArrayList<PositionRow>();

		@Override public void position(String account, NewContract contract, int position, double avgCost) {
			PositionKey key = new PositionKey( account, contract.conid() );
			PositionRow row = m_map.get( key);
			if (row == null) {
				row = new PositionRow();
				m_map.put( key, row);
				m_list.add( row);
			}
			row.update( account, contract, position, avgCost);
			
			
		}

		@Override public void positionEnd() {
			m_model.fireTableDataChanged();
			
		}

		public void clear() {
			m_map.clear();
			m_list.clear();
			fireTableDataChanged();
		}

		@Override public int getRowCount() {
			return m_map.size();
		}

		@Override public int getColumnCount() {
			return 4;
		}
		
		@Override public String getColumnName(int col) {
			switch( col) {
				case 0: return "Account";
				case 1: return "Contract";
				case 2: return "Position";
				case 3: return "Avg Cost";
				default: return null;
			}
		}

		@Override public Object getValueAt(int rowIn, int col) {
			PositionRow row = m_list.get( rowIn);
			
			switch( col) {
				case 0: return row.m_account;
				case 1: return row.m_contract.description();
				case 2: return row.m_contract.symbol();
				case 3: return row.m_position;
				case 4: return Formats.fmt( row.m_avgCost);
				default: return null;
			}
		}
	}
	
	private static class PositionKey {
		String m_account;
		int m_conid;

		PositionKey( String account, int conid) {
			m_account = account;
			m_conid = conid;
		}
		
		@Override public int hashCode() {
			return m_account.hashCode() + m_conid;
		}
		
		@Override public boolean equals(Object obj) {
			PositionKey other = (PositionKey)obj;
			return m_account.equals( other.m_account) && m_conid == other.m_conid;
		}
	}

	private static class PositionRow {
		String m_account;
		NewContract m_contract;
		int m_position;
		double m_avgCost;

		void update(String account, NewContract contract, int position, double avgCost) {
			m_account = account;
			m_contract = contract;
			m_position = position;
			m_avgCost = avgCost;
		}
	}
	 private void AttachLogHandler()
	 {
		 try
			{
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Handler handler = new FileHandler("C:\\Users\\Ben\\IBLogs\\IBTrading"+sdf.format(date)+".log");
			log.addHandler(handler);
			}
			catch (Exception e)
			{
				System.out.println(e.toString());
			}
	 }
	 private class SummaryModel extends AbstractTableModel implements IAccountSummaryHandler {
			ArrayList<SummaryRow> m_rows = new ArrayList<SummaryRow>();
			HashMap<String,SummaryRow> m_map = new HashMap<String,SummaryRow>();
			boolean m_complete;

			public void clear() {
				IBTradingMain.INSTANCE.controller().cancelAccountSummary( this);
				m_rows.clear();
				m_map.clear();
				m_complete = false;
				fireTableDataChanged();
			}

			@Override public void accountSummary(String account, AccountSummaryTag tag, String value, String currency) {
				SummaryRow row = m_map.get( account);
				if (row == null) {
					row = new SummaryRow();
					m_map.put( account, row);
					m_rows.add( row);
				}
				row.update( account, tag, value);
				
				if (m_complete) {
					fireTableDataChanged();
				}
			}
			
			@Override public void accountSummaryEnd() {
				fireTableDataChanged();
				m_complete = true;
			}

			@Override public int getRowCount() {
				return m_rows.size();
			}

			@Override public int getColumnCount() {
				return AccountSummaryTag.values().length + 1; // add one for Account column 
			}
			
			@Override public String getColumnName(int col) {
				if (col == 0) {
					return "Account";
				}
				return AccountSummaryTag.values()[col - 1].toString();
			}

			@Override public Object getValueAt(int rowIn, int col) {
				SummaryRow row = m_rows.get( rowIn);

				if (col == 0) {
					return row.m_account;
				}
				
				AccountSummaryTag tag = AccountSummaryTag.values()[col - 1];
				String val = row.m_map.get( tag);
				
				switch( tag) {
					case Cushion: return fmtPct( val);
					case LookAheadNextChange: return fmtTime( val);
					default: return AccountInfoPanel.format( val, null);
				}
			}

			public String fmtPct(String val) {
				return val == null || val.length() == 0 ? null : Formats.fmtPct( Double.parseDouble( val) );
			}

			public String fmtTime(String val) {
				return val == null || val.length() == 0 || val.equals( "0") ? null : Formats.fmtDate( Long.parseLong( val) * 1000);
			}
		}
		
		private static class SummaryRow {
			String m_account;
			HashMap<AccountSummaryTag,String> m_map = new HashMap<AccountSummaryTag,String>();
			
			public void update(String account, AccountSummaryTag tag, String value) {
				m_account = account;
				m_map.put( tag, value);
			} 
		}
	}



