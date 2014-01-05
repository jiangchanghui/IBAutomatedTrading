package com.reademail.main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import apidemo.ApiDemo;
import apidemo.CreateOrderFromEmail;
import com.ib.controller.Types.Action;
import com.ib.sample.main;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
public class mailReader {

	main _mainInstance;
	
	public mailReader(main main) {
		_mainInstance = main;
		Start();
	}

		
	private void Start()
	{
		final CreateOrderFromEmail _CreateOrder = new CreateOrderFromEmail();				
		SubjectSplitter _SubjectSplitter = new SubjectSplitter();
		  
		 Properties props = new Properties();
		 String _Email="";
		 String _Password="";
		 try {
			props.load(new FileInputStream("c:\\config.properties"));
			_Email = props.getProperty("email");
	    	_Password = props.getProperty("password");
			
			
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
						 System.out.println("Message Added Event fired");
						
						 try {
							Message msg = folder.getMessage(folder.getMessageCount());
							 System.out.println("SENT DATE:" + msg.getSentDate());
					            System.out.println("SUBJECT:" + msg.getSubject());
					        
					            
					            OrderTemplate  _OrderTemplate = Split(msg.getSubject());
					             
					          	_CreateOrder.CreateOrder(_OrderTemplate.getTicker(),_OrderTemplate.getQuantity(),_OrderTemplate.getSide());
							
							
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
	
	public OrderTemplate Split(String Subject)
	{
		String[] array = Subject.split(" "); 
		String Ticker=null;;
		int Quantity=0;;
		Action Side=null;
		String regex = "[0-9]+";
		
		for(String s : array)
		{
			if (s.startsWith("$"))
			{
				Ticker = s.substring(1).toUpperCase();
				continue;
			}
			if (s.startsWith("<") || s.endsWith(">"))
			{
				if (s.equals("<b>"))
					Side = Action.BUY;
				if (s.equals("<s>"))
					Side = Action.SELL;
				continue;
			}
			if (s.contains("short") && Subject.contains("<s>"))
			{
				Side = Action.SELL;
				continue;
			}
			
			if (s.contains("short") && Subject.contains("cover") && Subject.contains("<b>"))
			{
				Side = Action.BUY;
				continue;
			}
			if (s.contains("out"))
			{
			//	close position
				
			}
			if (s.contains("in"))
			{
				Side=Action.BUY;
				continue;
			}
			if (s.matches(regex)) 
			{
			   Quantity = Integer.parseInt(s);
			   continue;
			}
		}
		
		OrderTemplate _OrderTemplate = new OrderTemplate(Quantity,Ticker,Side);
		
		
		return _OrderTemplate;
	}
}
