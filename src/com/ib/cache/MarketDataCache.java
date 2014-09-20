package com.ib.cache;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import apidemo.util.Util;

public class MarketDataCache {

	private  Logger log = Logger.getLogger( this.getClass() );
	
	public static MarketDataCache INSTANCE = new MarketDataCache();
	private HashMap<String,MarketDataTuple> LastPx_Map = new HashMap<String,MarketDataTuple>();
	
	
	public MarketDataCache(){
	//	PropertyConfigurator.configure("c:\\log4j.properties"); 
	}
	
	public void SetLastPx(String Ticker, double LastPx)
	{
	//	log.info("Updated last price for "+Ticker+" to "+LastPx);
		LastPx_Map.put(Ticker,new MarketDataTuple(LastPx,System.currentTimeMillis()));
	}
	
	public boolean SubscriptionExists(String Ticker)
	{
		MarketDataTuple tmp = LastPx_Map.get(Ticker);
		if (tmp == null)
			return false;
		else
			return true;
		
	}
	
	
	public double GetLastPx(String Ticker)
	{
		MarketDataTuple tmp = LastPx_Map.get(Ticker);
		if (tmp == null)
		{
			Util.INSTANCE.SubscribeToMarketData(Ticker);
			return 0.0;
		}
			
		else
		{
			long LastUpdateTime = tmp.LastUpdateTime;
			long delta = System.currentTimeMillis() - LastUpdateTime;
			if (delta > 60000 )
				log.warn("STALE MARKET DATA : Last Update time for "+Ticker+"+is over "+delta/1000+" seconds old");
			if (delta > 300000)
			{
				log.warn("Resubscribing to live market data for :"+Ticker);
				Util.INSTANCE.SubscribeToMarketData(Ticker);
			}
			return tmp.LastPx;
		}
		
		
	}
	
	private class MarketDataTuple
	{
		double LastPx = 0.0;
		long LastUpdateTime=0;
		
		public MarketDataTuple (double LastPx, long LastUpdateTime)
		{
			this.LastPx = LastPx;
			this.LastUpdateTime = LastUpdateTime;
		}
		
		
		
		
		
	}
	
	
}
