package com.ib.sample;

import org.apache.log4j.Logger;

import apidemo.CreateOrderFromEmail;

import com.ib.cache.MarketDataCache;
import com.ib.cache.PositionCache;
import com.ib.controller.Types.Action;

public class CentralRiskControl extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
	private CreateOrderFromEmail _CreateOrder;
	private int ThresholdLoss = -1200;
	
	public CentralRiskControl(int RiskLimit) {
		if (RiskLimit > 0)
			ThresholdLoss = -RiskLimit;
		else
			ThresholdLoss = RiskLimit;
	
	}
	public void run()
	{
		log.info("Running - Threshhold loss : $"+ThresholdLoss);
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
		log.info("Position Count : "+PositionCache.INSTANCE.GetAllPositions().m_list.size());
		for(apidemo.PositionsPanel.PositionRow _position : PositionCache.INSTANCE.GetAllPositions().m_list)
		{
			
			String Ticker = _position.m_contract.symbol();
			int Quantity = _position.m_position;
			double AvgPx = _position.m_avgCost;
			
			if (Quantity ==0)
				continue;
			log.info("Checking Position : "+Ticker+"/"+Quantity+"@"+AvgPx);
			
						
			double LastPx = MarketDataCache.INSTANCE.GetLastPx(Ticker);
			double PnL = (LastPx - AvgPx )*Quantity;
			
			log.info("PnL : $"+PnL+" using (LastPx : "+LastPx+" , AvgPx : "+AvgPx+ " , Position : "+Quantity);
			
			if (PnL < ThresholdLoss)
			{
				log.info("PnL for "+Ticker+" is "+PnL+". This is greater than threshold value ("+ThresholdLoss+") . Closing position.");
				
			//Check if order exists for the position.
				log.info("Sending close order for "+Ticker+", Quantity : "+Quantity+" , PositionAvgPx : "+AvgPx);
				if (Quantity > 0)
					 _CreateOrder.CreateOrder(Ticker,Math.abs(Quantity),Action.SELL,0.0);
				else
					 _CreateOrder.CreateOrder(Ticker,Math.abs(Quantity),Action.BUY,0.0);
						
		
				
			}
			else
			{
				log.info("PnL for "+Ticker+" is "+PnL+". Within limit ("+ThresholdLoss+"), no action to take");
				
			}
			
		}
		
		
		
		
		
	}

}
