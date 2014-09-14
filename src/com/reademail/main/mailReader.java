package com.reademail.main;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import apidemo.ApiDemo;
import apidemo.CreateOrderFromEmail;
import apidemo.PositionsPanel.PositionRow;
import apidemo.TradesPanel;

import apidemo.OrdersPanel.OrdersModel;

import apidemo.PositionsPanel.PositionModel;
import apidemo.util.Util;



import com.ib.cache.MarketDataCache;
import com.ib.cache.PositionCache;
import com.ib.client.ExecutionFilter;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.ApiController.ITradeReportHandler;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.ib.initialise.IBTradingMain;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.twitter.main.SendTweet;
import com.web.request.HistoricResultSet;


public class mailReader extends Thread{
	private static  Logger logger =Logger.getLogger(mailReader.class);

	 private static String queue_new_trade = "";
	 private static String QUsername="";
	 private static String QPassword="";
	 static Double _FFLimit=0.0;
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	
			
	public void run()
	{
		
		final CreateOrderFromEmail _CreateOrder = new CreateOrderFromEmail();				
		boolean _run = true;
		String _lastMessage="";
		try{
	//	 Properties props = new Properties();
	//	 props.load(new FileInputStream("C:\\Users\\Ben\\Config\\config.properties"));
		 QUsername = Util.INSTANCE.QUsername;
		 QPassword = Util.INSTANCE.QPassword;
		 queue_new_trade = Util.INSTANCE.queue_new_trade;		           
							
		 ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername(QUsername); 
			factory.setPassword(QPassword); 
			factory.setVirtualHost("/"); 
		    
		    Connection connection = factory.newConnection();
		    Channel channel_Recv = connection.createChannel();
		    Channel channel_Send = connection.createChannel();
		    channel_Recv.queueDeclare(queue_new_trade, false, false, false, null);
		     		    
		    QueueingConsumer consumer = new QueueingConsumer(channel_Recv);
		    channel_Recv.basicConsume(queue_new_trade, true, consumer);
		    
		    SendTweet sendtweet = new SendTweet();
		    
		    while (_run) {
		    	
		    	
		    	try{
		    		
		    	logger.info("Trading waiting for new emails on Queue "+queue_new_trade);
		  
		    //blocking call until a message enteres queue
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      String message = new String(delivery.getBody());
		      logger.info("RECV on / "+queue_new_trade+" > " + message);
		      
		      if (!IBTradingMain.INSTANCE.IsTradingLive())
		      {
		    	  //SHutdown initiated
		    	  _run=false;
		    	  continue;
		      }
		      
		      
		     if (message.equals("") || message == null || message.equals(" ") )
		     {
		    	 logger.info("Discarded message , Reason : empty> "+message);
		    	 continue;
		     }	  
		     if (_lastMessage.equals(message))
		     {
		    	 logger.info("Discarded message, Reason : Duplicate message > "+message);
		    	 continue;
		    	 
		     }
		     
		      OrderTemplate  _OrderTemplate = Split(message);
		      logger.info("Logic completed, Routing order for "+_OrderTemplate.getSide()+" "+_OrderTemplate.getTicker()+" "+_OrderTemplate.getQuantity());
	            _CreateOrder.CreateOrder(this.getClass().getName(),_OrderTemplate.getTicker(),_OrderTemplate.getQuantity(),_OrderTemplate.getSide(),_FFLimit);
	            _lastMessage=message;
	            Util.INSTANCE.SubscribeToMarketData(_OrderTemplate.getTicker());
	            
	            IBTradingMain.INSTANCE.m_ordersMap.put(message,_OrderTemplate);
	            String Tweet = message +" -> "+ _OrderTemplate.getSide()+" "+_OrderTemplate.getTicker()+" "+_OrderTemplate.getQuantity();
	         //   sendtweet.SendNewTweet(Tweet); //FOR TESTING ONLY
					   
	            PositionCache.INSTANCE.Subscribe();
					            
		    	}
		    	catch(Exception e)
		    	{
		    		 logger.fatal(e.toString(),e);
		    	}
					            
					            
					            
					            
			    }
							
		    logger.info("CONNECTION CLOSED BY CLIENT: Closed connection to queue "+queue_new_trade);
		}
		catch (Exception e)
		{
			 logger.fatal(e.toString(),e);
		}
	 
	            
	    
	}
	


	public OrderTemplate Split(String Message)
	{
		 logger.info("Deciphering  message : "+ Message );
		
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
				logger.info("Set Ticker to "+ Ticker );
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
				
				logger.info("Set Side to "+ Side );
				_location++;
				continue;
			}
			//Quantity
			if (s.matches(regex)) 
			{
				int number = Integer.parseInt(s);
				
				
				if (Quantity ==0 && number > 10) //quantity not set yet. Set quantity
				{
					Quantity = number;
					logger.info("Set quantity to "+Quantity+" becuase message contains "+s);
				}
				else if (number % 10 == 0)//Quantity already set and is divisible by 10
				{
					Quantity = number;
					 logger.info("Set quantity to "+Quantity+" becuase message contains "+s);
				}
			  
			   _location++;
			   continue;
			}
			
			//SHORT			
			if (s.contains("SHORT") && Subject.contains("<S>"))
			{
				Side = Action.SELL;
				logger.info("Set Ticker to SELL becuase message continas SHORT and <S>");
				_location++;
				continue;
			}
			
			
			
			
			if (s.contains("OUT") && Quantity==0)
			{
				PositionRow PositionRow = PositionCache.INSTANCE.GetPosition(Ticker);
				int Position = PositionRow.m_position;
				Quantity = Math.abs(Position);
				
				if (Position < 0)
				{
					Side=Action.BUY;
				}
				else if (Position > 0)
				{
					Side=Action.SELL;
				}
				logger.info("Set quantity to "+Quantity+" becuase message contains OUT");
				logger.info("Set Side to "+Side+" becuase Position is "+Position+" and this is a cover/sell long");
				_location++;
				continue;
			}
			if (s.contains("OUT") )
			{
				PositionRow PositionRow = PositionCache.INSTANCE.GetPosition(Ticker);
				int Position = PositionRow.m_position;
				
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
				logger.info("Set Side to "+Side+" becuase Position is "+Position+" and this is a cover/sell long");
				
				 				
				
				
				_location++;
				
			}
			
			if (s.contains("1/"))
			{
				int number = Integer.parseInt(s.substring(s.indexOf("/")+1,s.indexOf("/")+2));
				PositionRow PositionRow = PositionCache.INSTANCE.GetPosition(Ticker);
				int Position = PositionRow.m_position;
				if(Position!=0)
				{
					Quantity = (Math.abs(Position)/number);
				logger.info("Set quantity to "+Quantity+" becuase message contains "+s+" and position is "+Position+"");
				}
				else 
				{
				Quantity=0;
				logger.info("Set quantity to "+Quantity+" becuase message contains "+s+" and position is "+Position+"");
				}
				_location++;
				
			}
		
			if (s.contains("K") && s.length()==2)
			{
				int number = Integer.parseInt(s.substring(0,1));
				
				Quantity = number*1000;
				logger.info("Set quantity to "+Quantity+" becuase message contains "+s+"");
				_location++;
			}
			
			
			if (s.contains("1K"))
			{
			Quantity = 1000;
			logger.info("Set quantity to "+Quantity+" becuase message contains "+s+"");
			_location++;
			continue;
			}	
			if (s.contains("1.5K"))
			{
			Quantity = 1500;
			logger.info("Set quantity to "+Quantity+" becuase message contains "+s+"");
			_location++;
			continue;
			}	
			if (s.contains("2K"))
			{
			Quantity = 2000;
			logger.info("Set quantity to "+Quantity+" becuase message contains "+s+"");
			_location++;
			continue;
			}	
			if (s.contains("2.5K"))
			{
			Quantity = 2500;
			logger.info("Set quantity to "+Quantity+" becuase message contains "+s+"");
			_location++;
			continue;
			}	
			if (s.contains("3K"))
			{
			Quantity = 3000;
			logger.info("Set quantity to "+Quantity+" becuase message contains "+s+"");
			_location++;
			continue;
			}	
			
			_location++;
			
		}
		logger.info("End of main logic, checking entire message");
		
		//if (Quantity==0)
	//	{
			if (Subject.contains("COV") || Subject.contains("FLAT") || Subject.contains("LAST"))
			{
				PositionRow PositionRow = PositionCache.INSTANCE.GetPosition(Ticker);
				int Position = PositionRow.m_position;
				logger.info("Position "+Position+"");
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
						logger.info("Quantity = "+Quantity+" and Position = "+Position+" so setting Quantity to Position");
						logger.info("Set Quantity to "+Quantity+"");
					}
				
				logger.info("Set SIDE to "+Side.toString()+" becuase subject contains COVER/FLAT/LAST");
			}
				
				
				
				_location++;
				
			

		
		if (Subject.contains("SWING") || Subject.contains("<SW>"))
		{
			PositionRow PositionRow = PositionCache.INSTANCE.GetPosition(Ticker);
			int Position = PositionRow.m_position;
			if (Position ==0)
			{
			Ticker = null;
			logger.info("Message contains SWING and so Ticker is "+Ticker+"");
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
	private ReturnObj TypoQuantity(String Ticker)
	{
		logger.info("Checking for typo..");
		
		boolean typo=false;
		int _PositionQuantity=0;
		
		com.ib.cache.PositionModel _AllPositions = PositionCache.INSTANCE.GetAllPositions();
		
		for (PositionRow _position : _AllPositions.m_list)	
		{
			String _PostiionSymbol = _position.m_contract.symbol();
			typo = CheckTypo(_PostiionSymbol,Ticker);
			if (typo)
			{
				
				
				_PositionQuantity = _position.m_position;
				return new ReturnObj(_PostiionSymbol,_PositionQuantity);
				
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
	
	
	public OrderTemplate getSplit(String message)
	{
	
		return Split(message);
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
	
	
	
}
