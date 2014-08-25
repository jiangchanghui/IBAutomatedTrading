package com.ib.sample;

import org.apache.log4j.Logger;

import apidemo.CreateOrderFromEmail;

import com.ib.cache.MarketDataCache;
import com.ib.cache.PositionCache;
import com.ib.controller.Types.Action;

public class CentralRiskControl extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
	private CreateOrderFromEmail _CreateOrder;
	private double ThresholdLoss = -450;
	public void run()
	{
		_CreateOrder = new CreateOrderFromEmail();			
		while(true)
		{
			
			try 
			{
				Thread.sleep(10000);
				CheckPositions();
				
			} catch (InterruptedException e) 
			{
				log.fatal(e.toString(),e);
			
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
			
						
			double LastPx = MarketDataCache.INSTANCE.GetLastPx(Ticker);
			double PnL = (LastPx - AvgPx )*Quantity;
			
			if (PnL < ThresholdLoss)
			{
				log.info("PnL for "+Ticker+" is "+PnL+". This is greater than threshold value ("+ThresholdLoss+") . Closing position.");
				
			
				log.info("Sending close order for "+Ticker+", Quantity : "+Quantity+" , PositionAvgPx : "+AvgPx);
			
				  _CreateOrder.CreateOrder(Ticker,Quantity,Action.SELL,0.0);
				//close position
				
			}
			else
			{
				log.info("PnL for "+Ticker+" is "+PnL+". Within limit ("+ThresholdLoss+"), no action to take");
				
			}
			
		}
		
		
		
		
		
	}

}
