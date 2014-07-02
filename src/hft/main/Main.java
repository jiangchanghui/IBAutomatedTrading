package hft.main;

import analytics.AnalyticsCache;
import analytics.RSICalculator;
import apidemo.OrdersPanel.OrdersModel;

import com.ib.controller.NewContract;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.ib.sample.IBTradingMain;
import com.web.request.HistoricResultSet;
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


public class Main extends Thread{
	
	//RSICalculator RSICalc;
	int _OverBought = 80;
	int _OverSold = 20;
	public void Main()
	{
		
	}
	
	
	
	 public void run()
		{	
		System.out.println("Initialising HFT module... ");
		String _Ticker = "AAPL";
		
	//	int req_id = IsTickerInRTMap(message.GetTicker());
	
	//	RTMarketDataMap = m_controller.GetTRealTimeMap();
		
		
		 AnalyticsCache _AnalyticsCache = new AnalyticsCache().instance;
			//creates subscription for market data ticks every 2 seconds
			int i=0;
			while(!_AnalyticsCache.IsConnected() && i <20)
			{
				try {
					Thread.sleep(1000);
					i++;
					} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (i >=20)
			{
				System.out.println("Initialising HFT module... Failed : Api not connected");
				return;
			}
			
		RequestMarketData("AAPL");
		RequestMarketData("TSLA");
		RequestMarketData("FSLR");
		RequestMarketData("IBM");
		RequestMarketData("NFLX");
		RequestMarketData("GOOGL");

		System.out.println("Initialising HFT module... Complete");
		
		}
	 private void RequestMarketData(String _Ticker)
	 {
		 NewContract m_contract = new NewContract();
			m_contract.symbol( _Ticker); 
			m_contract.secType( SecType.STK ); 
			m_contract.exchange( "SMART" ); 
			m_contract.currency( "USD" ); 
		 
		 
		 HistoricResultSet dataSet = new HistoricResultSet(_Ticker);
		int req_id =IBTradingMain.INSTANCE.controller().reqRealTimeBars(m_contract, WhatToShow.TRADES, false, dataSet);
		
		
		// return ConvertToJson(RTMarketDataMap.get(req_id),message.GetCorrelationId());	

		System.out.println("Initialising HFT module... "+_Ticker+" Loaded");
		
		
		
		
	}

	public void MarketDataTick(double _RSI) {
		
	
		
	if(_RSI > _OverBought)
	{
		//Check if an order exists
		OrdersModel m_model = new OrdersModel();
	
		//IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);
		
	}
	//	OrdersModel m_model = new OrdersModel();
	//	IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);
		
		
		
		
		//Market Data has ticked
		//is there an order working
		
		//no - Calc RSI and send orders is relevent

		//yes - do nothing
		
		
		
		
		// TODO Auto-generated method stub
		
	}

}
