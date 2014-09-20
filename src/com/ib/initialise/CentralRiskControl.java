package com.ib.initialise;

import hft.main.QueueHandler;

import org.apache.log4j.Logger;

import apidemo.CreateOrderFromEmail;

import com.benberg.struct.NewOrderRequest;
import com.ib.cache.MarketDataCache;
import com.ib.cache.PositionCache;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

public class CentralRiskControl extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );

	private int ThresholdLoss = -50;
	
	public CentralRiskControl(int RiskLimit) {
		if (RiskLimit > 0)
			ThresholdLoss = -RiskLimit;
		else
			ThresholdLoss = RiskLimit;
	
	}
	public void run()
	{
		log.info("Running - Threshhold loss : $"+ThresholdLoss);
			
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
		int _zeroPositions=0;
		log.info("Position Count : "+PositionCache.INSTANCE.GetAllPositions().m_list.size());
		for(apidemo.PositionsPanel.PositionRow _position : PositionCache.INSTANCE.GetAllPositions().m_list)
		{
			
			String Ticker = _position.m_contract.symbol();
			int Quantity = _position.m_position;
			double AvgPx = _position.m_avgCost;
			
			if (Quantity ==0){
				_zeroPositions++;
				continue;
			}
			
						
			double LastPx = MarketDataCache.INSTANCE.GetLastPx(Ticker);
			double PnL = (LastPx - AvgPx )*Quantity;
			
			log.info("PnL : $"+PnL+" using (LastPx : "+LastPx+" , AvgPx : "+AvgPx+ " , Position : "+Quantity);
			
			if (PnL < ThresholdLoss)
			{
				log.info("PnL for "+Ticker+" is "+PnL+". This is greater than threshold value ("+ThresholdLoss+") . Closing position.");
				
			//Check if order exists for the position.
				log.info("Sending close order for "+Ticker+", Quantity : "+Quantity+" , PositionAvgPx : "+AvgPx);
				IBTradingMain.INSTANCE.controller().cancelAllOrders(); //Close any existing orders first.
				if (Quantity > 0)
					QueueHandler.INSTANCE.SendToNewOrderQueue(new NewOrderRequest(Ticker,Math.abs(Quantity), OrderType.MKT,0.0,Action.SELL,this.getClass().getName()));
				else
					QueueHandler.INSTANCE.SendToNewOrderQueue(new NewOrderRequest(Ticker,Math.abs(Quantity), OrderType.MKT,0.0,Action.BUY,this.getClass().getName()));
						
		
				
			}
			else
			{
				log.info("PnL for "+Ticker+" is "+PnL+". Within limit ("+ThresholdLoss+"), no action to take");
				
			}
			
		}
		
		log.info("There is an additional "+_zeroPositions+" flat positions.");
		
		
		
	}

}
