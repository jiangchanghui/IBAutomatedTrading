package hft.main;

import java.util.ArrayList;

import org.apache.log4j.Logger;


import hft.main.Cache.PositionModel;
import hft.main.Cache.PositionRow;
import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;

import com.benberg.struct.NewOrderRequest;
import com.ib.client.Contract;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;
import com.ib.initialise.IBTradingMain;
import com.twitter.main.SendTweet;

public class LivePositionHandler extends Thread{
	public static LivePositionHandler instance = new LivePositionHandler();
	private  Logger log = Logger.getLogger( this.getClass() );
		
	
	//somehow call this each time a position changes.
	public void OnPositionChanged(Contract contract, int pos, double avgCost)
	{
		Cache.instance.IsLoadingOrders(true); //set to true so that we know when positions are finished loading. Order End sets to false
		OrdersModel _OpenOrders = new OrdersModel();
		/*
		PositionModel _positions = Cache.instance.GetAllPositions();
		int timeout = 0;
		while(Cache.instance.IsLoadingOrders() && timeout < 100)
		{
			try {
				Thread.sleep(100);
				timeout++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (timeout == 99)
		{
		//could not get live positions in under 10 seconds.
		//assume something wrong. Close all positions
			OnError();
			return;
		}
		//all orders loaded 
	
		//Check all open positions have associated orders.
		
				
		
		
		for(PositionRow _position :_positions.m_list)
		{
			//iterate over each position and see if there is a corresponding close order. Create one if not.
			String Ticker = _position.m_contract.symbol();
			int Quantity = _position.m_position;
			double AvgPx = _position.m_avgCost;
			OrderRow tmp = _OpenOrders.getOrderByTicker(Ticker);
			if(tmp!=null)
			{
				//an order exists for the ticker. Check its the same size as the position, and is limit
				//Central risk process will manage the limit price.
				if(tmp.m_order.totalQuantity()!=Quantity)
					OnError();
				if(tmp.m_order.orderType()!=OrderType.LMT)
					OnError();
				
			}
			else
			{
			//new position need to create a limit sell.
			CreateNewClosePositionOrder(Ticker,Quantity,AvgPx);	
				
			}
			
		}
			*/
		//check order exists for contract and size.
		
		
				
		
		
	}

	private void CreateNewClosePositionOrder(String Ticker, int Quantity, double avgPx) {
		//Get Average bar size - is there a better way to set sell limit price?
		double av_barsize = _Cache.GetAverageBarSize(Ticker);
		//Add onto current position
		double LimitPx = avgPx+av_barsize;
		
		QueueHandler.instance.SendToNewOrderQueue(new NewOrderRequest(Ticker, Quantity, OrderType.LMT, LimitPx,Action.SELL));
		
		
		//place order
		
		
	}

	private void OnError() {
		IBTradingMain.INSTANCE.controller().cancelAllOrders();	
				
	}
	
	
	
	

}
