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
		
		IBTradingMain.INSTANCE.controller().reqLiveOrders( _OpenOrders);
		
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
		log.info("Completed loading orders : " + Cache.instance.IsLoadingOrders());
		if (Cache.instance.IsLoadingOrders())
			return;
		
		
		String Ticker = contract.m_symbol;
		for (OrderRow row : _OpenOrders.m_orders)
		{
			log.info("Checking if close order exists for "+ row.m_contract.symbol());	
			if(row.m_contract.symbol().equals(Ticker))
			{//order pending already
				//check quantity on order
				log.info("Found order for "+ row.m_contract.symbol());	
				if(row.m_order.totalQuantity()!=pos)
				{
					//cancel order
					log.info("Found position size discrepancy , order quantity :  "+ row.m_order.totalQuantity() + ". Position : "+pos);	
					
					IBTradingMain.INSTANCE.controller().cancelOrder( row.m_order.orderId());
					//create new order for position size.
					log.info("Replacing order for "+ Ticker + " with quantity : "+pos+". Average execution cost : "+avgCost);	
					CreateNewClosePositionOrder(Ticker,pos,avgCost);
					return;
				}
				else
					return; //order placed is equal to pending order therefore nothing to be done.
					
			}
		}
		// if we get here there is no order, so need to create one
		log.info("No close order found for  "+ Ticker + ". Creating order with quantity : "+pos+". Average execution cost : "+avgCost);	
		CreateNewClosePositionOrder(Ticker,pos,avgCost);
		
	
		
	}

	private void CreateNewClosePositionOrder(String Ticker, int Quantity, double avgPx) {
		//Get Average bar size - is there a better way to set sell limit price?
		double av_barsize = Cache.instance.GetAverageBarSize(Ticker);
		//Add onto current position
		double LimitPx = avgPx+av_barsize;
		log.info("Sending close order for "+Ticker+", Quantity : "+Quantity+" , PositionAvgPx : "+avgPx+" , LimitPx : "+LimitPx);
		QueueHandler.instance.SendToNewOrderQueue(new NewOrderRequest(Ticker, Quantity, OrderType.LMT, LimitPx,Action.SELL));
		
		
		//place order
		
		
	}

	private void OnError() {
	//	IBTradingMain.INSTANCE.controller().cancelAllOrders();	
				
	}
	
	
	
	

}
