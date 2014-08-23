package com.reademail.main;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.swing.table.AbstractTableModel;

import apidemo.ApiDemo;
import apidemo.CreateOrderFromEmail;
import apidemo.TradesPanel;

import apidemo.OrdersPanel.OrdersModel;

import apidemo.PositionsPanel.PositionModel;


import com.ib.client.ExecutionFilter;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.ApiController.ITradeReportHandler;
import com.ib.controller.Types.Action;
import com.ib.sample.IBTradingMain;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.twitter.main.SendTweet;


public class mailReader extends Thread{
	private static final Logger log = Logger.getLogger( mailReader.class.getName() );
	 private static String queue_new_email = "";
	 private static String QUsername="";
	 private static String QPassword="";
	 static Double _FFLimit=0.0;
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	
			
	public void run()
	{
		AttachLogHandler();
		final CreateOrderFromEmail _CreateOrder = new CreateOrderFromEmail();				
		SubjectSplitter _SubjectSplitter = new SubjectSplitter();
		
		try{
		 Properties props = new Properties();
		 props.load(new FileInputStream("c:\\config.properties"));
		 _FFLimit = Double.valueOf(props.getProperty("fflimit"));
		 QUsername = props.getProperty("qusername");
		 QPassword = props.getProperty("qpassword");
		 queue_new_email = props.getProperty("queue_new_email");		           
							
		 ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername(QUsername); 
			factory.setPassword(QPassword); 
			factory.setVirtualHost("/"); 
		    
		    Connection connection = factory.newConnection();
		    Channel channel_Recv = connection.createChannel();
		    Channel channel_Send = connection.createChannel();
		    channel_Recv.queueDeclare(queue_new_email, false, false, false, null);
		     		    
		    QueueingConsumer consumer = new QueueingConsumer(channel_Recv);
		    channel_Recv.basicConsume(queue_new_email, true, consumer);
		    
		    SendTweet sendtweet = new SendTweet();
		    
		    while (true) {
		    	
		    	
		    	try{
		    		
		    	
		      log.log(Level.INFO,"Trading waiting for new emails on Queue : {0}",queue_new_email);
		    //blocking call until a message enteres queue
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      String message = new String(delivery.getBody());
		      log.log(Level.INFO,"Trading received new message on queue {0} : {1}",new Object[]{queue_new_email,message});
		     
					  
		      OrderTemplate  _OrderTemplate = Split(message);
					            log.log(Level.INFO ,"*****Logic completed, Routing order for {0}",_OrderTemplate.getSide()+" "+_OrderTemplate.getTicker()+" "+_OrderTemplate.getQuantity());
					            _CreateOrder.CreateOrder(_OrderTemplate.getTicker(),_OrderTemplate.getQuantity(),_OrderTemplate.getSide(),_FFLimit);
					           
					            
					            IBTradingMain.INSTANCE.m_ordersMap.put(message,_OrderTemplate);
					          String Tweet = message +" -> "+ _OrderTemplate.getSide()+" "+_OrderTemplate.getTicker()+" "+_OrderTemplate.getQuantity();
					            sendtweet.SendNewTweet(Tweet);
					            
					            
		    	}
		    	catch(Exception e)
		    	{
		    		 log.log(Level.SEVERE ,"Error occured with Trading Email listener : {0}",e.toString());
		    	}
					            
					            
					            
					            
			    }
							
					
		}
		catch (Exception e)
		{
			 log.log(Level.SEVERE ,"Error occured with Trading Email listene : {0}",e.toString());
		}
	 
	            
	    
	}
	
	public OrderTemplate Split(String Message)
	{
		 log.log(Level.INFO ,"Deciphering  message : {0}", Message );
		
		String Subject = Message.toUpperCase();
		String[] array = Subject.split(" "); 
		String Ticker=null;;
		int Quantity=0;
		Action Side=null;
		String regex = "[0-9]+";
		int _location=0;
		for(String s : array)
		{
			//TICKER
			
			if (s.startsWith("$") && Ticker==null)
			{
				Ticker = s.substring(1).toUpperCase();
				log.log(Level.INFO ,"Set Ticker to {0}", Ticker );
				_location++;
				continue;
			}
			
			//SIDE
			
			if (s.contains("<") || s.contains(">"))
			{
				if (s.contains("<B>"))
					Side = Action.BUY;
				if (s.contains("<S>"))
					Side = Action.SELL;
				if (s.contains("<SW>"))
					Side = null;
				
				log.log(Level.INFO ,"Set Side to {0}", Side );
				_location++;
				continue;
			}
			//Quantity
			if (s.matches(regex)) 
			{
				int Position = GetPosition(Ticker);
				
			   Quantity = Integer.parseInt(s);
			
			   
			   log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1}",new Object[]{Quantity,s});
			   _location++;
			   continue;
			}
			//SHORT			
			
			if (s.contains("SHORT") && Subject.contains("<S>"))
			{
				Side = Action.SELL;
				log.log(Level.INFO ,"Set Ticker to SELL becuase message continas SHORT and <S>");
				_location++;
				continue;
			}
			if (s.contains("OUT") )
			{
				int Position = GetPosition(Ticker);
			
				
				if (Position < 0)
				{
					Side=Action.BUY;
				}
				else if (Position > 0)
				{
					Side=Action.SELL;
				}
				if (Position == 0)
				{
					Side=null;
				}
				log.log(Level.INFO ,"Set Side to {0} becuase Position is {1} and this is a cover/sell long",new Object[]{Side,Position});
				
				 				
				
				
				_location++;
				
			}
			
			
			
			if (s.contains("OUT") && Quantity==0)
			{
				int Position = GetPosition(Ticker);
				Quantity = Math.abs(Position);
				
				if (Position < 0)
				{
					Side=Action.BUY;
				}
				else if (Position > 0)
				{
					Side=Action.SELL;
				}
				log.log(Level.INFO ,"Set quantity to {0} becuase message contains OUT", Quantity);
				log.log(Level.INFO ,"Set Side to {0} becuase Position is {1} and this is a cover/sell long",new Object[]{Side,Position});
				_location++;
				continue;
			}
			
			
			if (s.contains("1/"))
			{
				int number = Integer.parseInt(s.substring(s.indexOf("/")+1,s.indexOf("/")+2));
				int Position = Math.abs(GetPosition(Ticker));
				if(Position>0)
				{
					Quantity = (Position/number);
				log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1} and position is{2}", new Object[]{Quantity,s,Position});
				}
				else 
				{
				Quantity=0;
				log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1} and position is{2}", new Object[]{Quantity,s,Position});
				}
				_location++;
				
			}
		
			if (s.contains("K") && s.length()==2)
			{
				int number = Integer.parseInt(s.substring(0,1));
				
				Quantity = number*1000;
				log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1}", new Object[]{Quantity,s});
				_location++;
			}
			
			
			if (s.contains("1K"))
			{
			Quantity = 1000;
			log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1}",new Object[]{Quantity,s});
			_location++;
			continue;
			}	
			if (s.contains("1.5K"))
			{
			Quantity = 1500;
			log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1}",new Object[]{Quantity,s});
			_location++;
			continue;
			}	
			if (s.contains("2K"))
			{
			Quantity = 2000;
			log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1}",new Object[]{Quantity,s});
			_location++;
			continue;
			}	
			if (s.contains("2.5K"))
			{
			Quantity = 2500;
			log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1}",new Object[]{Quantity,s});
			_location++;
			continue;
			}	
			if (s.contains("3K"))
			{
			Quantity = 3000;
			log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1}",new Object[]{Quantity,s});
			_location++;
			continue;
			}	
			
			_location++;
			
		}
		log.log(Level.INFO ,"End of main logic, checking entire message");
		
		//if (Quantity==0)
	//	{
			if (Subject.contains("COVER") || Subject.contains("FLAT") || Subject.contains("LAST"))
			{
				int Position = GetPosition(Ticker);
				log.log(Level.INFO ,"Position {0}",Position);
				if (Position < 0)
				{
					Side = Action.BUY;
				}
				if (Position > 0)
				{
					Side = Action.SELL;
				}
				if (Position ==0)
				{
					Map <String, Integer> hm = new HashMap<String, Integer>();					
					
					ReturnObj R = new ReturnObj();
					R = TypoQuantity(Ticker);
					Ticker = R.getSymbol();
					Quantity = R.getQuantity();		
					
						if (Quantity < 0)
						{
							Side = Action.BUY;
						}
						if (Quantity > 0)
						{
							Side = Action.SELL;
						}
					
				}
				if (Quantity ==0 || Quantity > Math.abs(Position))
					{
						Quantity = Position;
						log.log(Level.INFO ,"Quantity = {0} and Position = {1} so setting Quantity to Position",new Object[]{Quantity,Position});
						log.log(Level.INFO ,"Set Quantity to {0}",Quantity);
					}
			}
				
				log.log(Level.INFO ,"Set SIDE to {0} becuase subject contains COVER/FLAT",Side.toString());
				
				_location++;
				
			

		
		if (Subject.contains("SWING") || Subject.contains("<SW>"))
		{
			int Position = GetPosition(Ticker);
			if (Position ==0)
			{
			Ticker = null;
			log.log(Level.INFO ,"Message contains SWING and so Ticker is {0}",Ticker);
			}
			else
			{
				Quantity = Position;
					if (Position < 0)
					{
						Side = Action.BUY;
					}
					else
					{
						Side=Action.SELL;
					}
			}
		}
		if (!Subject.substring(0, 10).contains("<S>") && !Subject.substring(0, 10).contains("<B>"))
		{
			Ticker="none";
			
		}
		//Check for quotations at the start and end.
		Ticker.trim();
		if (Ticker.endsWith("\""))
		{
			Ticker = Ticker.substring(0,Ticker.length()-2);
		}
		if (Ticker.startsWith("\""))
		{
			Ticker = Ticker.substring(1);
		}
		if (Side ==null)
		{
			Side=Action.BUY;
			Quantity =0;
			
		}
		OrderTemplate _OrderTemplate = new OrderTemplate(Math.abs(Quantity),Ticker,Side);
		
		
		return _OrderTemplate;
	}
	 PositionModel m_model = new PositionModel();
	private int GetPosition(String Symbol)
	{
		
		log.log(Level.INFO ,"Getting Position data for {0}",Symbol);
		
	//	ITradeReportHandler m_tradeReportHandler = null;
	//	OrdersModel m_model1 = new OrdersModel();
	//	TradesPanel m_tradesPanel = new TradesPanel();
	
	//boolean test=main.INSTANCE.controller().reqExecutions2( new ExecutionFilter(), m_tradesPanel);
	//	main.INSTANCE.controller().reqLiveOrders( m_model);

	IBTradingMain.INSTANCE.controller().reqPositions( m_model);
	
	
		int _PositionQuantity = 0;
		int count2=0;
		int count=0;
		log.log(Level.INFO ,"{0} Executions found",m_model.getRowCount());
		
	
		  long lDateTime = new Date().getTime();
		  
		  long _timeout = 5000;
		  long _delta=0;
		  int _iterator=0;
		while (m_model.getRowCount()==0 && _iterator<5)
		{
			log.log(Level.FINEST ,"{0} Executions found retrying",m_model.getRowCount());
				
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			_iterator++;
			
			
		//	 long lDateTimeNow = new Date().getTime();
			
		//	_delta = lDateTimeNow-lDateTime;
			
		}
		
		count = m_model.getRowCount();
		
		if( count ==0)
			{
				return 0;
			}
		
		
		log.log(Level.INFO ,"It took {0} ms to find the position data",_delta);
		
		
		
		log.log(Level.INFO ,"{0} Executions found",m_model.getRowCount());
		
		
		
		for (int i=0;i< count;i++)
		{
			log.log(Level.INFO ,"{0} {1} avg price of {2}",new Object[]{m_model.getValueAt(i, 1),m_model.getValueAt(i, 3),m_model.getValueAt(i, 4)});
						
			if (m_model.getValueAt(i, 2).equals(Symbol))
			{
				
				
				Object obj = m_model.getValueAt(i, 3);
				_PositionQuantity = (Integer) (obj == null ? "" :  obj);
				
			}
		}
		if (_PositionQuantity==0)
		{
			
			
			//try again
			count2 = m_model.getRowCount();
			for (int i=0;i< count2;i++)
			{
				log.log(Level.INFO ,"{0} {1} avg price of {2}",new Object[]{m_model.getValueAt(i, 1),m_model.getValueAt(i, 3),m_model.getValueAt(i, 4)});
							
				if (m_model.getValueAt(i, 2).equals(Symbol))
				{
					Object obj = m_model.getValueAt(i, 3);
					_PositionQuantity = (Integer) (obj == null ? "" :  obj);
					
				}
			}
		}
	
		//check if its still zero
		
		if (_PositionQuantity==0)
		{
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			for (int i=0;i< m_model.getRowCount();i++)
			{
				log.log(Level.INFO ,"{0} {1} avg price of {2}",new Object[]{m_model.getValueAt(i, 1),m_model.getValueAt(i, 3),m_model.getValueAt(i, 4)});
							
				if (m_model.getValueAt(i, 2).equals(Symbol))
				{
					Object obj = m_model.getValueAt(i, 3);
					_PositionQuantity = (Integer) (obj == null ? "" :  obj);
					
				}
			}
			
		}
		
	
		
		
		
		log.log(Level.INFO ,"Returning position {0} for Ticker {1}",new Object[]{_PositionQuantity,Symbol});
		return _PositionQuantity;
		
		
	}
	static class Exec {
		String Symbol;
		String Side;
		int Quantity;
		
		Exec(String Symbol, String Side, int Quantity)
		{
			this.Symbol = Symbol;
			this.Side = Side;
			this.Quantity = Quantity;
		}
		
		String getSymbol()
		{
			return Symbol;
		}
		String getSide()
		{
			return Side;
		}
		int getQuantity()
		{
			return Quantity;
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
	public class ReturnObj {
		   public int Quantity; 
		   public String Symbol; 
		   
		   ReturnObj()
		   {}
		   
		   ReturnObj(String Symbol, int Quantity)
		   {
			   this.Quantity = Quantity;
			   this.Symbol = Symbol;
		   }
		   
		   public String getSymbol()
		   {
			   return Symbol;
		   }
		   public int getQuantity()
		   {
			   return Quantity;
		   }
		   
		}
	private ReturnObj TypoQuantity(String Ticker)
	{
		log.log(Level.INFO ,"Checking for typo..");
		
		boolean typo=false;
		int _PositionQuantity=0;
		
		
		
		for (int i=0;i< m_model.getRowCount();i++)
		{
			log.log(Level.INFO ,"{0} {1} avg price of {2}",new Object[]{m_model.getValueAt(i, 1),m_model.getValueAt(i, 3),m_model.getValueAt(i, 4)});
			
			
			Object obj = m_model.getValueAt(i, 2);
			String Symbol = (String) (obj == null ? "" :  obj);
			
			
			
			typo = CheckTypo(Symbol,Ticker);
				
			if (typo)
			{
				
				Object obj1 = m_model.getValueAt(i, 3);
				_PositionQuantity = (Integer) (obj1 == null ? "" :  obj1);
				return new ReturnObj(Symbol,_PositionQuantity);
				
			}
		}
		
		return new ReturnObj("none",_PositionQuantity);
		
		
	}
	
	
	
	private boolean CheckTypo(String symbol, String ticker) {
		
		char[] first = symbol.toCharArray();
		  char[] second = ticker.toCharArray();
		  Arrays.sort(first);
		  Arrays.sort(second);
		  return Arrays.equals(first, second);
		
		
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
	
	
	public OrderTemplate getSplit(String message)
	{
	
		return Split(message);
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
	
}
