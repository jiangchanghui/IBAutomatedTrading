package com.web.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.swing.table.AbstractTableModel;

import apidemo.CreateOrderFromEmail;
import apidemo.OrdersPanel;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionModel;
import apidemo.TopModel.TopRow;


import com.google.gson.Gson;

import com.google.gson.annotations.SerializedName;

import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;

import java.util.ArrayList;

import java.util.List;

import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.OrderStatus;
import com.ib.controller.OrderType;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.SecType;
import com.ib.sample.main;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.reademail.main.OrderTemplate;
import com.reademail.main.mailReader;
import apidemo.TopModel.TopRow;



@WebService 
public class Index extends Thread{
	private static final Logger log = Logger.getLogger( mailReader.class.getName() );
	 private final static String QUEUE_WEBQUERY = "WEBREQUEST";
	 private final static String QUEUE_WEBRESPONSE = "WEBRESPONSE";
	
	 public void run()
	{
		try{
		 ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    Connection connection = factory.newConnection();
		    Channel channel_Recv = connection.createChannel();
		    Channel channel_Send = connection.createChannel();
		    channel_Recv.queueDeclare(QUEUE_WEBQUERY, false, false, false, null);
		    channel_Send.queueDeclare(QUEUE_WEBRESPONSE, false, false, false, null);
		    
		   
		    
		    QueueingConsumer consumer = new QueueingConsumer(channel_Recv);
		    channel_Recv.basicConsume(QUEUE_WEBQUERY, true, consumer);
		    
		    log.log(Level.INFO,"Initialised Receive Queue: {0} and Send Queue : {1} for web requests",new Object[]{QUEUE_WEBQUERY,QUEUE_WEBRESPONSE});
		    
		    while (true) {
		      log.log(Level.INFO,"Trading waiting for web querys on Queue : {0}",QUEUE_WEBQUERY);
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      String message = new String(delivery.getBody());
		      log.log(Level.INFO,"Received new message on Topic {0} : {1}",new Object[]{QUEUE_WEBQUERY,message});
		      String Response="";
		      if (message.equals("GETORDERS"))
		      {
		    	Response = GetOrders();
		      }
		      if (message.equals("GETPOSITIONS"))
		      {
		    	Response = GetOpenPositions();
		      }
		      channel_Send.basicPublish("", QUEUE_WEBRESPONSE, null, Response.getBytes());
		      log.log(Level.INFO,"Sent WebReply message on Topic {0} : {1}",new Object[]{QUEUE_WEBRESPONSE,Response});
		    }
		}
		catch (Exception e)
		{
			 log.log(Level.SEVERE,"Web Request listener has failed with error {0}",e.toString());
		}
		
		
	}
	private void CancelAllOrders()
	{
		
		
		
		
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
		
		main.INSTANCE.controller().reqPositions( m_model);
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
		log.log(Level.FINEST ,"{0} Executions found",m_model.getRowCount());
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
				
			double LastPx = GetFarPrice(Symbol);
			
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
		main.INSTANCE.controller().reqLiveOrders( m_model);
		
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
	
	private double GetFarPrice(String Symbol)
	{
		NewContract contract = new NewContract();
		NewOrder order = new NewOrder();
		contract.symbol(Symbol);
				
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");
		
			
		
		TopRow row = new TopRow( null, contract.description() );
		//m_rows.add( row);
		main.INSTANCE.controller().reqTopMktData(contract, "", false, row);
	//	fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
		
		return row.m_last;
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
	
	
}

