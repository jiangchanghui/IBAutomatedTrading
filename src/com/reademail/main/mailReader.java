package com.reademail.main;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;

import apidemo.ApiDemo;
import apidemo.CreateOrderFromEmail;
import apidemo.TradesPanel;

import apidemo.OrdersPanel.OrdersModel;

import com.ib.client.ExecutionFilter;
import com.ib.controller.ApiController.ITradeReportHandler;
import com.ib.controller.Types.Action;
import com.ib.sample.main;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;



public class mailReader {

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
		
		 try {
			props.load(new FileInputStream("c:\\config.properties"));
			_Email = props.getProperty("email");
	    	_Password = props.getProperty("password");
			_FFLimit = Double.valueOf(props.getProperty("fflimit"));
			
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
						 System.out.println("New Email");
						
						 try {
							Message msg = folder.getMessage(folder.getMessageCount());
							
							String from = InternetAddress.toString(msg.getFrom());
							System.out.println("SENT DATE:" + msg.getSentDate());
							System.out.println("FROM:" + from); 
							
					            System.out.println("SUBJECT:" + msg.getSubject());
					        
					       //     if (from.contains("gold@bullsonwallstreet.com"))
					        //    {					            
					            OrderTemplate  _OrderTemplate = Split(msg.getSubject());
					            _CreateOrder.CreateOrder(_OrderTemplate.getTicker(),_OrderTemplate.getQuantity(),_OrderTemplate.getSide(),_FFLimit);
					         //   }
							
						} catch (MessagingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						 
						 
					}

					@Override
					public void messagesRemoved(MessageCountEvent arg0) {
						 System.out.println("Message Removed Event fired");
						
					}
	            });

	    folder.addMessageChangedListener(new MessageChangedListener() {

	                public void messageChanged(MessageChangedEvent e) {
	                    System.out.println("Message Changed Event fired");
	                }
	            });
	            
	    Thread t = new Thread(new Runnable() {

		            public void run() {
		            	 System.out.println("Starting listening");
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
		String Subject = Message.toUpperCase();
		String[] array = Subject.split(" "); 
		String Ticker=null;;
		int Quantity=0;;
		Action Side=null;
		String regex = "[0-9]+";
		
		for(String s : array)
		{
			//TICKER
			
			if (s.startsWith("$"))
			{
				Ticker = s.substring(1).toUpperCase();
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
				continue;
			}
			
			//SHORT			
			
			if (s.contains("SHORT") && Subject.contains("<S>"))
			{
				Side = Action.SELL;
				continue;
			}
			
			if (s.contains("SHORT") && Subject.contains("COVER"))
			{
				int Position = GetPosition(Ticker);
				Quantity = Position;
				Side = Action.BUY;
				continue;
			}
			
			
			if (s.contains("OUT"))
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
			}
			
			
			if (s.contains("1/2"))
			{
				int Position = GetPosition(Ticker);
				Quantity = (Position/2);
			}
			if (s.contains("1/4"))
			{
				int Position = GetPosition(Ticker);
				Quantity = (Position/4);
			}
			if (s.contains("1K"))
			{
			Quantity = 1000;
			continue;
			}	
			if (s.contains("1.5K"))
			{
			Quantity = 1500;
			continue;
			}	
			if (s.contains("2K"))
			{
			Quantity = 2000;
			continue;
			}	
			if (s.contains("2.5K"))
			{
			Quantity = 2500;
			continue;
			}	
			if (s.contains("3K"))
			{
			Quantity = 3000;
			continue;
			}	
			
			if (s.matches(regex) && Quantity ==0) 
			{
			   Quantity = Integer.parseInt(s);
			   continue;
			}
		}
		if (Subject.contains("SWING"))
		{
			Ticker = null;
		}
			OrderTemplate _OrderTemplate = new OrderTemplate(Quantity,Ticker,Side);
		
		
		return _OrderTemplate;
	}
	private int GetPosition(String Symbol)
	{
		ITradeReportHandler m_tradeReportHandler = null;
		OrdersModel m_model = new OrdersModel();
		TradesPanel m_tradesPanel = new TradesPanel();
		main.INSTANCE.controller().reqExecutions( new ExecutionFilter(), m_tradesPanel);
		main.INSTANCE.controller().reqLiveOrders( m_model);

						
		ArrayList<apidemo.TradesPanel.FullExec> _Execs = new ArrayList<apidemo.TradesPanel.FullExec>();
		
		_Execs = m_tradesPanel.getExecutions();
		
		
		int _PositionQuantity = 0;
		
		
		for (int i=0;i< _Execs.size();i++)
		{
			System.out.println(_Execs.get(i).m_contract.symbol());
			System.out.println(_Execs.get(i).m_trade.m_shares);
			System.out.println(_Execs.get(i).m_trade.m_avgPrice);
			System.out.println(_Execs.get(i).m_trade.m_side);
			
			if (_Execs.get(i).m_contract.symbol().equals(Symbol))
			{
				if (_Execs.get(i).m_trade.m_side.equals("BOT"))
				{
					_PositionQuantity +=_Execs.get(i).m_trade.m_shares;
					
				}
				else
				{
					_PositionQuantity -=_Execs.get(i).m_trade.m_shares;
				}
			}
	
		
		}
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
