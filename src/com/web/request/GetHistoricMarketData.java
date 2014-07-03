package com.web.request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;


import com.benberg.struct.NewMarketDataRequest;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IRealTimeBarHandler;
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
	HashMap<Integer, HistoricResultSet> RTMarketDataMap;
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
		//	System.out.println("Searching cache for : "+Ticker);
			for (Entry<Integer, HistoricResultSet> m : MarketDataMapWeb.entrySet()) {
				String _Ticker = m.getValue().GetTicker();
				
				if (_Ticker.equals(Ticker))
				{
			//		System.out.println("Data in cache. Req ID : "+m.getKey());
					return m.getKey();
				}
					
					
				
				
		}
				System.out.println("Data not in cache. New Request");
				return -1;
				
		}
	
	private int IsTickerInMapWeb(String Ticker,String TimeFrame)
	{
		
		MarketDataMapWeb = m_controller.GetHistoricalMapWeb();
		System.out.println("Searching cache for : "+Ticker);
		for (Entry<Integer, HistoricResultSet> m : MarketDataMapWeb.entrySet()) {
			String _Ticker = m.getValue().GetTicker();
			String _TimeFrame = m.getValue().GetTimeFrame();
			if (_Ticker.equals(Ticker) && _TimeFrame.equals(TimeFrame) && m.getValue().m_rows.size()>0)
			{
				System.out.println("Data in cache. Req ID : "+m.getKey());
				return m.getKey();
			}
				
				
			
			
	}
			System.out.println("Data not in cache. New Request");
			return -1;
			
	}
	
	
	private int IsTickerInRTMap(String Ticker)
	{
		
		RTMarketDataMap = m_controller.GetTRealTimeMap();
		System.out.println("Searching cache for : "+Ticker);
		for (Entry<Integer, HistoricResultSet> m : RTMarketDataMap.entrySet()) {
			String _Ticker = m.getValue().GetTicker();
			if (_Ticker.equals(Ticker))
			{
				System.out.println("Data in cache. Req ID : "+m.getKey());
				return m.getKey();
			}
				
				
			
			
	}
			System.out.println("Data not in cache. New Request");
			return -1;
			
	}
	
	
	
	
	public NewMarketDataRequest GetNewRealTimeDataRequest(NewMarketDataRequest message) throws InterruptedException
	{
		int req_id = IsTickerInRTMap(message.GetTicker());
		if(req_id != -1)
			 return ConvertToJson(RTMarketDataMap.get(req_id),message.GetCorrelationId());	
		RTMarketDataMap = m_controller.GetTRealTimeMap();
		NewContract m_contract = new NewContract();
		m_contract.symbol( message.GetTicker()); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
		
		
	//	BarResultsPanel panel = new BarResultsPanel( false);
		
		HistoricResultSet dataSet = new HistoricResultSet(message.GetTicker(),message.GetTimeFrame());

		req_id =IBTradingMain.INSTANCE.controller().reqRealTimeBars(m_contract, WhatToShow.TRADES, false, dataSet);
		//m_resultsPanel.addTab( "Real-time " + m_contract.symbol(), panel, true, true);
		
		 return ConvertToJson(RTMarketDataMap.get(req_id),message.GetCorrelationId());	
			
		
		
		
	
		
		
	}
public HistoricResultSet GetHistoricalMarketData(String Ticker) throws InterruptedException {
		
		int req_id = IsTickerInMap(Ticker);
		if(req_id != -1)
		{
		 return MarketDataMapWeb.get(req_id);	
		 
		}
		else
		{	

		
		NewContract m_contract = new NewContract();
		m_contract.symbol( Ticker); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
				
		HistoricResultSet dataSet = new HistoricResultSet(Ticker,null);

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
	//	System.out.println(sdf.format(date));
		
	
		req_id =IBTradingMain.INSTANCE.controller().reqHistoricalData(m_contract, sdf.format(date), 2, DurationUnit.DAY, BarSize._1_min, WhatToShow.TRADES,true, dataSet);
		
		
		MarketDataMapWeb = m_controller.GetHistoricalMapWeb();
		MarketDataMap = m_controller.GetHistoricalMap();
		return MarketDataMapWeb.get(req_id);
		}
		
		
	
		
		
		
	}
	
	
	
	
	
	public NewMarketDataRequest GetMarketDataToJson(NewMarketDataRequest message) throws InterruptedException {
		
		int req_id = IsTickerInMapWeb(message.GetTicker(),message.GetTimeFrame());
//		if(req_id != -1)
//		{
//		 return ConvertToJson(MarketDataMapWeb.get(req_id),message.GetCorrelationId());	
		 
	//	}
//		else
	//	{	

		
		NewContract m_contract = new NewContract();
		m_contract.symbol( message.GetTicker()); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
				
		HistoricResultSet dataSet = new HistoricResultSet(message.GetTicker(),message.GetTimeFrame());

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		System.out.println(sdf.format(date));
		
		if (nullcheck(message))
			req_id =IBTradingMain.INSTANCE.controller().reqHistoricalData(m_contract, sdf.format(date), GetNumberDays(message.GetTimeFrame()), GetDurationUnit(message.GetTimeFrame()), GetBarSize(message.GetTimeFrame()), WhatToShow.TRADES,GetRTH(message.GetTimeFrame()), dataSet);
		else
			return null;
			
		
		MarketDataMapWeb = m_controller.GetHistoricalMapWeb();
		MarketDataMap = m_controller.GetHistoricalMap();
		return (ConvertToJson(MarketDataMapWeb.get(req_id),message.GetCorrelationId()));
		
		
		
	//	}
		
		
		
	}
private boolean GetRTH(String TimeFrame) {
	if (TimeFrame.equals("1Yr1D"))
		return true;
	else
		return false;
	
	}






private DurationUnit GetDurationUnit(String TimeFrame) {
	if (TimeFrame.equals("1Yr1D"))
		return DurationUnit.YEAR;
	if (TimeFrame.equals("3Day1Min"))
		return DurationUnit.DAY;
	if (TimeFrame.equals("3Day5Min"))
		return DurationUnit.DAY;
		return null;
	}






private boolean nullcheck(NewMarketDataRequest message) {

	if (message.GetCorrelationId() != null && message.GetTicker().length()>1 && GetNumberDays(message.GetTimeFrame())>0 && GetBarSize(message.GetTimeFrame())!=null &&message.GetTicker()!="undefined")
		return true;
	else
		return false;
	}






private BarSize GetBarSize(String TimeFrame) {
	if (TimeFrame.equals("1Yr1D"))
		return BarSize._1_day;
	if (TimeFrame.equals("3Day1Min"))
		return BarSize._1_min;
	if (TimeFrame.equals("3Day5Min"))
		return BarSize._5_mins;
	
		return null;
	}






private int GetNumberDays(String TimeFrame) {
		if (TimeFrame.equals("1Yr1D"))
			return 1;
		if (TimeFrame.equals("3Day1Min"))
			return 3;
		if (TimeFrame.equals("3Day5Min"))
			return 3;
	
			return 0;
	}









private NewMarketDataRequest ConvertToJson(HistoricResultSet Data,String CorrelationId) throws InterruptedException 
{
	String price_result ="";
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	format.setTimeZone(TimeZone.getTimeZone("UTC"));

	if (Data.m_rows.size()==0)
		return null;
	for( int i=0;i < Data.m_rows.size();i++)
	{
			
	
		price_result+= "["+ConvertTime(Data.m_rows.get(i).time())+","+
				Data.m_rows.get(i).open()+","+
				Data.m_rows.get(i).high()+","+
				Data.m_rows.get(i).low()+","+
				Data.m_rows.get(i).close()+"],";
		
		
		
	}
	System.out.println(Data.m_rows.size());
	price_result = price_result.substring(0, price_result.length() - 1);
	System.out.println(price_result);
	
		
	
	NewMarketDataRequest _response = new NewMarketDataRequest(Data.Ticker, CorrelationId,price_result,true);

	return _response;
}

private long ConvertTime(long l)
{
	return (l*1000)-14400000;
	
}

}
