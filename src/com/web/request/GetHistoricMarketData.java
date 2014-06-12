package com.web.request;

import java.util.HashMap;

import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.Types.BarSize;
import com.ib.controller.Types.DurationUnit;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.ib.sample.IBTradingMain;


public class GetHistoricMarketData {
	
	private static GetHistoricMarketData instance = null;
	HashMap<Integer, IHistoricalDataHandler> MarketDataMap;
	ApiController m_controller;
	IBTradingMain  IBMain;
	public GetHistoricMarketData()
	{
		 IBMain = IBTradingMain.INSTANCE;
		m_controller = IBMain.GetController();
		
		
	}
	
	
	
	
	
	
	 public static GetHistoricMarketData getInstance() {
	      if(instance == null) {
	         instance = new GetHistoricMarketData();
	      }
	      return instance;
	   }

	
	private boolean IsTickerInMap(String Ticker)
	{
		
		MarketDataMap = m_controller.GetHistoricalMap();
		
		if(MarketDataMap.containsKey(Ticker))
			return true;
		else
			return false;
		
		
		
	}
	
	
	public String get(String Ticker) {
		
		if(IsTickerInMap(Ticker))
		{
		 MarketDataMap.get(Ticker);	
		}
		else
		{	

		
		NewContract m_contract = new NewContract();
		m_contract.symbol( Ticker); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
				
		HistoricResultSet dataSet = new HistoricResultSet();
		IBTradingMain.INSTANCE.controller().reqHistoricalData(m_contract, "20140612 21:00", 1, DurationUnit.SECOND, BarSize._10_mins, WhatToShow.TRADES, false, dataSet);
//		m_resultsPanel.addTab( "Historical " + m_contract.symbol(), panel, true, true);
		MarketDataMap = m_controller.GetHistoricalMap();
		
		
		
		
		
		
		System.out.println(Test.size());
		}
		
		
		return null;
	}

}
