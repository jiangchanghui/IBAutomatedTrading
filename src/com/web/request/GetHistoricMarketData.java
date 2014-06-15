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

	
	private int IsTickerInMap(String Ticker,String TimeFrame)
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
	
	
	public NewMarketDataRequest getMarketDataToJson(NewMarketDataRequest message) throws InterruptedException {
		
		int req_id = IsTickerInMap(message.GetTicker(),message.GetTimeFrame());
		if(req_id != -1)
		{
		 return ConvertToJson(MarketDataMapWeb.get(req_id),message.GetCorrelationId());	
		 
		}
		else
		{	

		
		NewContract m_contract = new NewContract();
		m_contract.symbol( message.GetTicker()); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
				
		HistoricResultSet dataSet = new HistoricResultSet(message.GetTicker(),message.GetTimeFrame());

		
		
		req_id =IBTradingMain.INSTANCE.controller().reqHistoricalData(m_contract, "20140613 21:00", GetNumberDays(message.GetTimeFrame()), DurationUnit.DAY, GetBarSize(message.GetTimeFrame()), WhatToShow.TRADES, false, dataSet);

			
		
		MarketDataMapWeb = m_controller.GetHistoricalMapWeb();
		MarketDataMap = m_controller.GetHistoricalMap();
		return (ConvertToJson(MarketDataMapWeb.get(req_id),message.GetCorrelationId()));
		
		
		
		}
		
		
		
	}
private BarSize GetBarSize(String TimeFrame) {
	if (TimeFrame.equals("50Day1D"))
		return BarSize._1_day;
	if (TimeFrame.equals("1Day1Min"))
		return BarSize._1_min;
	if (TimeFrame.equals("3Day5Min"))
		return BarSize._5_mins;
	
		return null;
	}






private int GetNumberDays(String TimeFrame) {
		if (TimeFrame.equals("50Day1D"))
			return 50;
		if (TimeFrame.equals("1Day1Min"))
			return 1;
		if (TimeFrame.equals("3Day5Min"))
			return 3;
	
			return 0;
	}






private DurationUnit GetDurationUnit(String TimeFrame) {
		
	if (TimeFrame.equals("50Day1D"))
			return DurationUnit.DAY;
	if (TimeFrame.equals("1Day1Min"))
		return DurationUnit.DAY;
	if (TimeFrame.equals("3Day5Min"))
		return DurationUnit.DAY;
	
		return null;
	}






private NewMarketDataRequest ConvertToJson(HistoricResultSet Data,String CorrelationId) throws InterruptedException 
{
	String result ="";
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	format.setTimeZone(TimeZone.getTimeZone("UTC"));

	Date date; 
	//long millis = date.getTime();
	int _LoopCount=0;
	System.out.println(Data.m_rows.size());
	while(Data.m_rows.size()==0 && _LoopCount < 100)
	{
		Thread.sleep(500);
		System.out.println(Data.m_rows.size());
		_LoopCount++;
	}
	
	System.out.println(Data.m_rows.size());
	
	//for (Bar b : Data.m_rows)
	for( int i=0;i < Data.m_rows.size();i++)
	{
	//	date = format.parse(b.formattedTime());
		
		/**
		result+= "{\"Time\":\""+b.time()+"\","+
				 "\"Symbol\":\""+Data.Ticker+"\","+
				 "\"High\":\""+b.high()+"\","+
				 "\"Low\":\""+b.low()+"\","+
				 "\"Open\":\""+b.open()+"\","+
				 "\"Close\":\""+b.close()+"\""+
				 "},";
		
		result+= "["+ConvertTime(b.time())+","+
				 b.high()+","+
				 b.low()+","+
				 b.open()+","+
				 b.close()+"],";
				 */
		
	
		result+= "["+ConvertTime(Data.m_rows.get(i).time())+","+
				Data.m_rows.get(i).high()+","+
				Data.m_rows.get(i).low()+","+
				Data.m_rows.get(i).open()+","+
				Data.m_rows.get(i).close()+"],";
		
		
		
	}
	System.out.println(Data.m_rows.size());
	result = result.substring(0, result.length() - 1);
	//result +="]";
System.out.println(result);

NewMarketDataRequest _response = new NewMarketDataRequest(Data.Ticker, CorrelationId,result,true);

	return _response;
}

private long ConvertTime(long l)
{
	return (l*1000)-14400000;
	
}

}
