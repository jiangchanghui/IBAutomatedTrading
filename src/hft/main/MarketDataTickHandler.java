package hft.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;


import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionModel;

import com.ib.cache.CommonCache;
import com.ib.controller.Types.Action;
import com.ib.initialise.IBTradingMain;
import com.twitter.main.SendTweet;

public class MarketDataTickHandler extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
		 
	//deprecated
	public void MarketDataTick(String Ticker,double _RSI) {
		
		
		
		//if(_RSI < _OverSold)
		//{
			//Check if an oder exists
			OrdersModel m_model = new OrdersModel();
			
			IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);
			
			
			CommonCache c = new CommonCache().instance;
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
			//	CreateOrder(Ticker,c.OrderSizeCalc(Ticker),Action.SELL,0.0);
				
				//Create corresponding scalp order
				//get exec price
				_order = c.GetOrderDetails(Ticker);
		//		PositionModel _positions = new PositionModel();
		//		IBTradingMain.INSTANCE.controller().reqPositions( _positions);
			}
			
		}
	
}
