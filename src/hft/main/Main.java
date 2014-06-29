package hft.main;

import analytics.calculations.RSICalculator;
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


public class Main {
	
	RSICalculator RSICalc;
	int _OverBought = 80;
	int _OverSold = 20;
	public void Main()
	{
		RSICalc = new RSICalculator();
	}
	
	
	
	public static void main(String[] args) {
		
		
		String _Ticker = "AAPL";
		
	//	int req_id = IsTickerInRTMap(message.GetTicker());
	
	//	RTMarketDataMap = m_controller.GetTRealTimeMap();
		NewContract m_contract = new NewContract();
		m_contract.symbol( _Ticker); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
		
		
	
		
		HistoricResultSet dataSet = new HistoricResultSet(_Ticker);
		//creates subscription for market data ticks every 2 seconds
		int req_id =IBTradingMain.INSTANCE.controller().reqRealTimeBars(m_contract, WhatToShow.TRADES, false, dataSet);
		
		
		
		
		// return ConvertToJson(RTMarketDataMap.get(req_id),message.GetCorrelationId());	
			
		
		
		
	}

	public void MarketDataTick(String _Ticker, long _time, double _close) {
		
	double _RSI = RSICalc.CalculateRsi(_Ticker, _time,_close);
		
	if(_RSI > _OverBought)
		
		OrdersModel m_model = new OrdersModel();
		IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);
		
		
		
		
		//Market Data has ticked
		//is there an order working
		
		//no - Calc RSI and send orders is relevent

		//yes - do nothing
		
		
		
		
		// TODO Auto-generated method stub
		
	}

}
