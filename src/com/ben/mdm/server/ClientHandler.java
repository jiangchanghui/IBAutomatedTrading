package com.ben.mdm.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.ben.common.Message;

import apidemo.util.Util;

public class ClientHandler extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
	Socket socket;
	ObjectInputStream sInput;
	ObjectOutputStream sOutput;


	
	public void Start(Socket socket) {
		this.socket = socket;
		try
		{
			// create output first
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			sInput  = new ObjectInputStream(socket.getInputStream());
		
			new MarketDataListener(sOutput).start();
			
		
		boolean keepGoing = true;
		
			// read a String (which is an object)
				try{
					while(keepGoing) {
					Message message = (Message) sInput.readObject();
					
					switch(message.getType()) {
					
					case Message.NEW_REAL_TIME_MARKET_DATA_REQUEST:
						Util.INSTANCE.SubscribeToMarketData(message.getTicker());
						Cache.INSTANCE.getMDMSubscribed().add(message.getTicker());
						Cache.INSTANCE.newMarketDataRequest(message.getTicker(), message.getReqId());
						break;
							
					}
				}
				}
				catch(Exception e)
				{
					log.error(e.toString(),e);
				}
			}
		
		catch (Exception e) {
				
			log.error(e.toString(),e);	
		}
			 
	}
}

