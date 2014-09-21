package com.ben.mdm.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ib.initialise.IBTradingMain;

public class Cache {
	private  Logger log = Logger.getLogger( this.getClass() );
	public static Cache INSTANCE = new Cache();
	HashMap<String, Integer> requestIDCache = new HashMap<String, Integer>();
	private Set MDMSubScribedList = new HashSet();
	
	public void newMarketDataRequest(String ticker,int reqId)
	{
		requestIDCache.put(ticker,reqId);
	}

	public int getRequestIdFromTicker(String ticker) {
		return requestIDCache.get(ticker);
	}
	
	public  Set getMDMSubscribed() {
		return MDMSubScribedList;
	}
	
}
