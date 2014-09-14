package com.posttrade.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

import org.apache.log4j.Logger;


import apidemo.CreateOrderFromEmail;
import apidemo.TradesPanel;

import com.ib.cache.MarketDataCache;
import com.ib.cache.PositionCache;
import com.ib.client.ExecutionFilter;
import com.ib.controller.Types.Action;
import com.ib.initialise.IBTradingMain;
import com.ib.initialise.PositionModel;



public class EoDClosePositions extends Thread {
	private static final Logger log = Logger.getLogger( EoDClosePositions.class.getName() );
	 PositionModel m_model = new PositionModel();
	@Override
	public void run() {
		log.info("Running EOD position close process");
		IBTradingMain.INSTANCE._LiveStatus = false;
		CreateOrderFromEmail _CreateOrder = new CreateOrderFromEmail();			
		log.info("Position Count : "+PositionCache.INSTANCE.GetAllPositions().m_list.size());
		for(apidemo.PositionsPanel.PositionRow _position : PositionCache.INSTANCE.GetAllPositions().m_list)
		{
			
			String Ticker = _position.m_contract.symbol();
			int Quantity = _position.m_position;
			double AvgPx = _position.m_avgCost;
			
			if (Quantity ==0)
				continue;
			log.info("Closing Position : "+Ticker+"/"+Quantity+"@"+AvgPx);
			
			if (Quantity > 0)
				 _CreateOrder.CreateOrder(this.getClass().getName(),Ticker,Math.abs(Quantity),Action.SELL,0.0);
			else
				 _CreateOrder.CreateOrder(this.getClass().getName(),Ticker,Math.abs(Quantity),Action.BUY,0.0);
			
			double LastPx = MarketDataCache.INSTANCE.GetLastPx(Ticker);
			
			if(LastPx != 0.0)
			{
			
			double PnL = (LastPx - AvgPx )*Quantity;
			log.info("Estimated PnL for position "+Ticker+" =  $"+PnL);
			
			}
		}
		
		
		RecordDailyTrades _record = new RecordDailyTrades();
		_record.start();
	}
	
	
}
