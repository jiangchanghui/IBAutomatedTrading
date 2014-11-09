package hft.main;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import analytics.AnalyticsCache;
import analytics.RSICalculator;
import analytics.SlowStochasticsCalculator;
import apidemo.TradesPanel;
import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionModel;
import apidemo.util.Util;

import com.ib.cache.CommonCache;
import com.ib.client.ExecutionFilter;
import com.ib.controller.NewContract;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.twitter.main.SendTweet;
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
import com.ib.initialise.IBTradingMain;


public class HftMain extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
		
	
	
	
	 public void run()
		{	
		  
		 CommonCache.instance.Setup();
		 StartQueueHandler();//Initialise the singleton for queues used for market data and new orders. 
		 
		 StartNewOrderHandler(); //New thread to continuously listen for new orders created from analytics workers
		 
		 StartStochasticsWorkers(); //starts 2 stochastic workers per ticker 
		 
		 LivePositionHandler H = new LivePositionHandler();
		 H.start();
		
		}
	 
	

	private void StartQueueHandler()
	 {
		 QueueHandler.INSTANCE.setup();
		
	 }
	 private void StartNewOrderHandler()
	 {
		 OrderHandler O = new OrderHandler();
		 O.start();
	 }
	 private void StartStochasticsWorkers()
	 {
		log.info("Initialising HFT module... ");
			
	//	int req_id = IsTickerInRTMap(message.GetTicker());
	
	//	RTMarketDataMap = m_controller.GetTRealTimeMap();
						
		WaitForTradingStart();
		
			//creates subscription for market data ticks every 2 seconds
			int i=0;
			while(!IBTradingMain.INSTANCE.IsApiConnected() && i <20)
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
				log.info("Initialising HFT module... Failed : Api not connected");
				return;
			}
		
		
		for(String ticker : CommonCache.instance.GetTickersList())
		{
		Util.INSTANCE.SubscribeToMarketData(ticker);
		SlowStochasticsCalculator s = new SlowStochasticsCalculator();
		s.SetTicker(ticker);
		s.setThreadName("Stochastics Worker for :"+ticker);
		s.start();
	//	log.info("Initialised HFT module for "+ticker);
		}
	
	log.info("Initialising HFT module... Complete");	
		

		}


		
	private void WaitForTradingStart()
	{
		Calendar calendarNow = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		Calendar calendarTradingHours = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		calendarTradingHours.set(Calendar.HOUR_OF_DAY, 9);
		calendarTradingHours.set(Calendar.MINUTE, 30);
		
		Date todaysTradingStart = calendarTradingHours.getTime();
		
		Date todaysDate = calendarNow.getTime();
		
		if (todaysDate.after(todaysTradingStart))
			return;
		else
			log.info("Current time : "+todaysDate.toString()+". Waiting for trading to start at : "+todaysTradingStart.toString());
		
		while(true)
		{
		
		Date currentDate = calendarNow.getTime();
		
		if (currentDate.after(currentDate))
			break;
		else
		{
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error(e.toString(),e);
			}
		}
		}
		
		
	}

}
