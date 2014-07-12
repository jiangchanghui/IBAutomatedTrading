package hft.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionModel;

import com.ib.controller.Types.Action;
import com.ib.initialise.IBTradingMain;

public class MarketDataTickHandler extends Thread{
	
	
		 
	
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
			//	CreateOrder(Ticker,c.OrderSizeCalc(Ticker),Action.SELL,0.0);
				
				//Create corresponding scalp order
				//get exec price
				_order = c.GetOrderDetails(Ticker);
		//		PositionModel _positions = new PositionModel();
		//		IBTradingMain.INSTANCE.controller().reqPositions( _positions);
			}
			
		}
	
}
