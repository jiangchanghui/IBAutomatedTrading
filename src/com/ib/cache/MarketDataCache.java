package com.ib.cache;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import apidemo.util.Util;

public class MarketDataCache {

	private  Logger log = Logger.getLogger( this.getClass() );
	
	public static MarketDataCache INSTANCE = new MarketDataCache();
	private HashMap<String,LastPxTuple> LastPx_Map = new HashMap<String,LastPxTuple>();
	private HashMap<String,LevelOneSnapshot> LevelOneQuote_Map = new HashMap<String,LevelOneSnapshot>();
	
	public MarketDataCache(){
	//	PropertyConfigurator.configure("c:\\log4j.properties"); 
	}
	
	public void SetLastPx(String Ticker, double LastPx)
	{
		log.info("Updated last price for "+Ticker+" to "+LastPx);
		LastPx_Map.put(Ticker,new LastPxTuple(LastPx,System.currentTimeMillis()));
	}
	
	public boolean IsLastPxSubscriptionExists(String Ticker)
	{
	
		LastPxTuple tmp = LastPx_Map.get(Ticker);
		if (tmp == null)
			return false;
		else
			return true;
		
	}
	public boolean IsLevelOneSubscriptionExists(String Ticker)
	{
	
		LevelOneSnapshot tmp = LevelOneQuote_Map.get(Ticker);
		if (tmp == null)
			return false;
		else
			return true;
		
	}
	
//once subscription is created, the Util class uses this method to populate the hashmap. 
//this is different to how lastPx's are managed. Last Px has a seperate Tuple which is updated when the HistoricResultSet is updated via the SetLastPx method.
//LevelOneSnapshots use the same object to receive the update from IB, and in the hashmap.
	public void AddLevelOneQuote(LevelOneSnapshot dataSet) {
		LevelOneQuote_Map.put(dataSet.getTicker(),dataSet);
		
	}
	
	
	
	public double GetLastPx(String Ticker)
	{
		LastPxTuple tmp = LastPx_Map.get(Ticker);
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
	
	public LevelOneSnapshot GetLevelOneSnapshot(String Ticker)
	{
		
		
		if (!IsLevelOneSubscriptionExists(Ticker))
		{	
			
			Util.INSTANCE.SubscribeToLevelOneQuote(Ticker);
		
		//try waiting for subscription to come through, shouldnt take long
		
		int timeout=0;
			while(timeout<20 && !IsLevelOneSubscriptionExists(Ticker))
			{
				try {
					Thread.sleep(100);
					timeout++;
				} catch (InterruptedException e) {
					log.warn(e.toString(),e);
				}
						
			}
		
		}
		
		LevelOneSnapshot snapshot = LevelOneQuote_Map.get(Ticker);	
		if(snapshot==null)		
			return null;
		else
		{
			int timeout = 0;
			while (!snapshot.isActive() && timeout <30)
			{
				log.info("snapshot not ready");
				try {
					Thread.sleep(100);
					timeout++;
				} catch (InterruptedException e) {
					log.warn(e.toString(),e);
				}
			}
			
			log.info("Snapshot for "+Ticker+" :{"+snapshot.toString()+"}");
			long LastUpdateTime = snapshot.GetLastUpdateTime();
			long delta = System.currentTimeMillis() - LastUpdateTime;
			if (delta > 60000 )
				log.warn("STALE LEVELONE QUOTE : Last Update time for "+Ticker+"+is over "+delta/1000+" seconds old");
		//	if (delta > 300000)
		//		{
		//			log.warn("Resubscribing to level one snapshot for :"+Ticker);
		//			Util.INSTANCE.SubscribeToLevelOneQuote(Ticker);
		//		}
			return snapshot;
		}
		
		
	}
	
	
	private class LastPxTuple
	{
		double LastPx = 0.0;
		long LastUpdateTime=0;
		
		public LastPxTuple (double LastPx, long LastUpdateTime)
		{
			this.LastPx = LastPx;
			this.LastUpdateTime = LastUpdateTime;
		}
		
		
		
		
		
	}


}
