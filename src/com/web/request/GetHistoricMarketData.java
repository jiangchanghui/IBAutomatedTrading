package com.web.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ib.controller.ApiController;
import com.ib.controller.Bar;
import com.ib.controller.NewContract;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.Types.BarSize;
import com.ib.controller.Types.DurationUnit;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.ib.sample.IBTradingMain;


public class GetHistoricMarketData {
	
	private static GetHistoricMarketData instance = null;
	HashMap<Integer, HistoricResultSet> MarketDataMapWeb;
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

	
	private int IsTickerInMap(String Ticker)
	{
		
		MarketDataMapWeb = m_controller.GetHistoricalMapWeb();
		System.out.println("Searching cache for : "+Ticker);
		for (Entry<Integer, HistoricResultSet> m : MarketDataMapWeb.entrySet()) {
			String Symbol = m.getValue().GetTicker();	
			if (Symbol.equals(Ticker))
			{
				System.out.println("Data in cache. Req ID : "+m.getKey());
				return m.getKey();
			}
				
				
			
			
	}
			System.out.println("Data not in cache. New Request");
			return -1;
			
	}
	
	
	public String getMarketDataToJson(String Ticker) {
		
		int req_id = IsTickerInMap(Ticker);
		if(req_id != -1)
		{
		 return ConvertToJson(MarketDataMapWeb.get(req_id));	
		 
		}
		else
		{	

		
		NewContract m_contract = new NewContract();
		m_contract.symbol( Ticker); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
				
		HistoricResultSet dataSet = new HistoricResultSet(Ticker);
	//	HistoricResultSet dataSet = new HistoricResultSet();
		req_id =IBTradingMain.INSTANCE.controller().reqHistoricalData(m_contract, "20140613 21:00", 1, DurationUnit.DAY, BarSize._10_mins, WhatToShow.TRADES, false, dataSet);
//		m_resultsPanel.addTab( "Historical " + m_contract.symbol(), panel, true, true);
		MarketDataMapWeb = m_controller.GetHistoricalMapWeb();
		MarketDataMap = m_controller.GetHistoricalMap();
		return (ConvertToJson(MarketDataMapWeb.get(req_id)));
		
		
		
		}
		
		
		
	}
private String ConvertToJson(HistoricResultSet Data)
{
	String result ="[";
	for (Bar b : Data.m_rows)
	{
		result+= "{\"Time\":\""+b.formattedTime()+"\","+
				 "\"Symbol\":\""+Data.Ticker+"\","+
				 "\"High\":\""+b.high()+"\","+
				 "\"Low\":\""+b.low()+"\","+
				 "\"Open\":\""+b.open()+"\","+
				 "\"Close\":\""+b.close()+"\""+
				 "},";
	}
	result = result.substring(0, result.length() - 1);
	result +="]";
System.out.println(result);
	return result;
}



}
