package com.reademail.main;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
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
import com.ib.sample.main;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;



public class mailReader {
	private static final Logger log = Logger.getLogger( mailReader.class.getName() );
	main _mainInstance;
	 Double _FFLimit=0.0;
	public mailReader(main main) {
		_mainInstance = main;
		Start();
	}

		
	private void Start()
	{
	//	TestLogic();
		final CreateOrderFromEmail _CreateOrder = new CreateOrderFromEmail();				
		SubjectSplitter _SubjectSplitter = new SubjectSplitter();
		  
		 Properties props = new Properties();
		 String _Email="";
		 String _Password="";
		 String _TradeAlertEmail="";
		 final boolean FilterEmails=true;
		 try {
			 log.log(Level.INFO ,"Processing config entries");
			props.load(new FileInputStream("c:\\config.properties"));
			_Email = props.getProperty("email");
	    	_Password = props.getProperty("password");
	    	_TradeAlertEmail = props.getProperty("alertemail");
			_FFLimit = Double.valueOf(props.getProperty("fflimit"));
			 log.log(Level.INFO ,"Processing config entries -Done");
		} catch (Exception e1) {
		}
		 
		 
		 
	        props.setProperty("mail.store.protocol", "imaps");
	        try {
	            Session session = Session.getInstance(props, null);
	            Store store = session.getStore();
	            store.connect("imap.gmail.com", _Email, _Password);
	           
	            
	           /* 
	            Folder inbox = store.getFolder("INBOX");
	            inbox.open(Folder.READ_ONLY);
	            Message msg = inbox.getMessage(inbox.getMessageCount());
	            Address[] in = msg.getFrom();
	            for (Address address : in) {
	                System.out.println("FROM:" + address.toString());
	            }
	            Multipart mp = (Multipart) msg.getContent();
	            BodyPart bp = mp.getBodyPart(0);
	            System.out.println("SENT DATE:" + msg.getSentDate());
	            System.out.println("SUBJECT:" + msg.getSubject());
	            System.out.println("CONTENT:" + bp.getContent());
	            */
	            
	         //   IMAPStore imapStore = (IMAPStore) session.getStore("imaps");
	         //   imapStore.connect();
	            
	           final Folder folder = store.getFolder("INBOX");
	            
	         //   final IMAPFolder folder = (IMAPFolder) imapStore.getFolder("Inbox");
	            folder.open(Folder.READ_WRITE);
	            
	           
	            
	            folder.addMessageCountListener(new MessageCountListener() {

	               
					@Override
					public void messagesAdded(MessageCountEvent arg0) {
					
						 log.log(Level.INFO ,"New EMail");
						 try {
							Message msg = folder.getMessage(folder.getMessageCount());
							
							String from = InternetAddress.toString(msg.getFrom());
							 log.log(Level.INFO ,"SENT DATE : {0}",msg.getSentDate());
							 log.log(Level.INFO ,"FROM : {0}",from);
							 log.log(Level.INFO ,"SUBJECT : {0}",msg.getSubject());
					           
							
					        
					        //   if (FilterEmails && from == "gold@bullsonwallstreet.com")
					      //     {					            
					            OrderTemplate  _OrderTemplate = Split(msg.getSubject());
					            log.log(Level.INFO ,"Routing order for {0}",_OrderTemplate.getSide()+" "+_OrderTemplate.getTicker()+" "+_OrderTemplate.getQuantity());
					            _CreateOrder.CreateOrder(_OrderTemplate.getTicker(),_OrderTemplate.getQuantity(),_OrderTemplate.getSide(),_FFLimit);
					    
					            //     }
							
						} catch (MessagingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						 
						 
					}

					@Override
					public void messagesRemoved(MessageCountEvent arg0) {
						
						
					}
	            });

	    folder.addMessageChangedListener(new MessageChangedListener() {

	                public void messageChanged(MessageChangedEvent e) {
	                
	                }
	            });
	            
	    Thread t = new Thread(new Runnable() {

		            public void run() {
		            	 log.log(Level.INFO ,"Listening for new emails" );
		                try {
		                    while (true) {
		                    	
		                        ((IMAPFolder) folder).idle();
		                    }
		                } catch (MessagingException ex) {
		                    //Handling exception goes here
		                }
		            }
		        });

        t.start();
	            
	            
	            
	        } catch (Exception mex) {
	            mex.printStackTrace();
	        }
	}
	
	public OrderTemplate Split(String Message)
	{
		 log.log(Level.INFO ,"Received message : {0}", Message );
		
		String Subject = Message.toUpperCase();
		String[] array = Subject.split(" "); 
		String Ticker=null;;
		int Quantity=0;;
		Action Side=null;
		String regex = "[0-9]+";
		int _location=0;
		for(String s : array)
		{
			//TICKER
			
			if (s.startsWith("$"))
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
			if (s.matches(regex) && _location<6) 
			{
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
			
			
			
			
			if (s.contains("OUT") && Quantity==0)
			{
				int Position = GetPosition(Ticker);
				Quantity = Position;
				
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
				Quantity = (Position/number);
				log.log(Level.INFO ,"Set quantity to {0} becuase message contains {1} and position is{2}", new Object[]{Quantity,s,Position});
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
		
		if (Subject.contains("COVER"))
		{
			int Position = GetPosition(Ticker);
			Quantity = Position;
			if (Position < 0)
			{
				Side = Action.BUY;
			}
			log.log(Level.INFO ,"Set SIDE to BUY becuase subject contains COVER");
			log.log(Level.INFO ,"Set Quantity to {0}",Quantity);
			_location++;
			
		}
		if (Subject.contains("SWING") || Subject.contains("<SW>"))
		{
			Ticker = null;
			log.log(Level.INFO ,"Message contains SWING and so Ticker is {0}",Ticker);
		}
		
		
			OrderTemplate _OrderTemplate = new OrderTemplate(Quantity,Ticker,Side);
		
		
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

	main.INSTANCE.controller().reqPositions( m_model);
	
		//ArrayList<apidemo.TradesPanel.FullExec> _Execs = new ArrayList<apidemo.TradesPanel.FullExec>();
		
//		while (m_model.getRowCount()==0)
		
		
	//	_Execs = m_tradesPanel.getExecutions();
		
	
	//	System.out.println(m_model.getValueAt(0, 1));
	//	System.out.println(_Execs.toString());
	//	System.out.println(_Execs.isEmpty());
	
	
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
		
		if (_PositionQuantity==0 && count!=count2)
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
	
	
	private void TestLogic()
	{
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("C:\\Users\\Ben\\SampleSubjects.txt"));
			PrintWriter writer = new PrintWriter("C:\\Users\\Ben\\SampleSubjects_processed.txt", "UTF-8");
		String line;
		while ((line = br.readLine()) != null) {
		   
			if (!(line.contains("http")) && line.contains("$"))
			{
				 OrderTemplate  _OrderTemplate = Split(line);
			     writer.println(line+","+_OrderTemplate.getSide()+","+_OrderTemplate.getTicker()+","+_OrderTemplate.getQuantity());
			}
		}
		br.close();
		writer.close();
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
	}
	
}
