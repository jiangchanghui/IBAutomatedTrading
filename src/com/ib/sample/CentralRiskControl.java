package com.ib.sample;

import org.apache.log4j.Logger;

import com.ib.cache.PositionCache;

public class CentralRiskControl extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
	
	private double ThresholdLoss = -45;
	public void run()
	{
				
		while(true)
		{
			
			try 
			{
				Thread.sleep(10000);
				CheckPositions();
				
			} catch (InterruptedException e) 
			{
				
				e.printStackTrace();
			}
		}
						
	}
	private void CheckPositions()
	{

		for(apidemo.PositionsPanel.PositionRow _position : PositionCache.INSTANCE.GetAllPositions().m_list)
		{
		
			String Ticker = _position.m_contract.symbol();
			int Quantity = _position.m_position;
			double AvgPx = _position.m_avgCost;
			log.info("Checking Position : "+Ticker+"/"+Quantity+"@"+AvgPx);
			
						
			double LastPx = Cache.instance.GetLastPx(Ticker);
			log.info("LastPx for "+Ticker+" : "+LastPx);
			double PnL = (LastPx - AvgPx )*Quantity;
			
			if (PnL < ThresholdLoss)
			{
				log.info("PnL for "+Ticker+" is "+PnL+". This is greater than "+ThresholdLoss+" . Closing position.");
				
			
				log.info("Sending close order for "+Ticker+", Quantity : "+Quantity+" , PositionAvgPx : "+AvgPx);
				QueueHandler.instance.SendToNewOrderQueue(new NewOrderRequest(Ticker, Quantity, OrderType.MKT,0.0,Action.SELL));
				
				//close position
				
			}
			else
			{
				log.info("PnL for "+Ticker+" is "+PnL+". Within limit ("+ThresholdLoss+"), no action to take");
				
			}
			
		}
		
		
		
		
		
	}

}
