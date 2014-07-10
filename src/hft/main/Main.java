package hft.main;

import java.util.Date;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import analytics.AnalyticsCache;
import analytics.RSICalculator;
import apidemo.TradesPanel;
import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionModel;

import com.ib.client.ExecutionFilter;
import com.ib.controller.NewContract;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.ib.sample.IBTradingMain;
import com.web.request.HistoricResultSet;
import com.benberg.struct.NewMarketDataRequest;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.IRealTimeBarHandler;
import com.ib.controller.Bar;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.OrderType;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.BarSize;
import com.ib.controller.Types.DurationUnit;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.TimeInForce;
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
//		RequestMarketData("TSLA");
//		RequestMarketData("FSLR");
//		RequestMarketData("IBM");
//		RequestMarketData("NFLX");
//		RequestMarketData("GOOGL");

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
		
		
		TradesPanel m_tradesPanel = new TradesPanel();
		IBTradingMain.INSTANCE.controller().reqExecutions(new ExecutionFilter(), m_tradesPanel);
		
	}

	public void MarketDataTick(String Ticker,double _RSI) {
		
	
		
	//if(_RSI < _OverSold)
	//{
		//Check if an oder exists
		OrdersModel m_model = new OrdersModel();
		
		IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);
		
		
		Cache c = new Cache().instance;
		c.IsLoadingOrders(true);
		
		
		while(c.IsLoadingOrders())
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//all orders loaded
		OrderRow _order = c.GetOrderDetails(Ticker);
			
		if (_order ==null)
		{
			//order does not exist 
			//place order
			CreateOrder(Ticker,c.OrderSizeCalc(Ticker),Action.SELL,0.0);
			
			//Create corresponding scalp order
			//get exec price
			_order = c.GetOrderDetails(Ticker);
		//	PositionModel _positions = new PositionModel();
		//	IBTradingMain.INSTANCE.controller().reqPositions( _positions);
		}
		
	}
	
		
	public void CreateOrder(String Symbol, int Quantity, Action Side, Double FFLimit)
	{
		if (Symbol==null  || Quantity == 0 || Side == null)
		{
			
		//	 log.log(Level.WARNING ,"Order Create failed with Symbol : {0}, Quantity : {1}, Side : {2}, FFLimit :{3}",new Object[]{Symbol,Quantity,Side.toString(),FFLimit});
			return;
		}
		
		double FarPrice =  0.0;
		
	
		
		
		NewContract contract = new NewContract();
		NewOrder order = new NewOrder();
		contract.symbol(Symbol);
		order.totalQuantity(Quantity);
		order.action(Side);
		
			
		order.orderType(OrderType.MKT);
		order.tif(TimeInForce.FOK);//prevents blocked shorts hanging around.
		//order.lmtPrice(FarPrice);
		
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");
		
	//	if (!FatFingerViolation(contract, order,FFLimit))
		//{
		//log.log(Level.INFO ,"Order being executed for {0} {1} {2} at {3}",new Object[]{Side,Quantity,Symbol,order.orderType().toString()});
		IBTradingMain.INSTANCE.controller().placeOrModifyOrder(contract, order, new IOrderHandler() {
			@Override public void orderState(NewOrderState orderState) {
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						
					}
				});
			}
			@Override public void handle(final int errorCode, final String errorMsg) {
				
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
					//	log.log(Level.SEVERE ,"Order execution failed with ({0}:{1})",new Object[]{errorCode,errorMsg});						
					//	IBTradingMain.INSTANCE.m_errorMap.put(dateFormat.format(new Date()), errorMsg);
						
						if (errorMsg.contains("Order held"))
						{
						//	log.log(Level.SEVERE ,"Order is held, cancelling all open orders");
							IBTradingMain.INSTANCE.controller().cancelAllOrders();
						}
						if (errorMsg.contains("not be placed"))
						{
							IBTradingMain.INSTANCE.controller().cancelAllOrders();
						}
						
						
						
						
						
					}
				});
			}
		});
	//	}
		
	}
		
	

}
