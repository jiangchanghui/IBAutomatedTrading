package hft.main;

import java.util.HashMap;

import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;

import analytics.AnalyticsCache;
import analytics.HistoricalRsiCache;
import apidemo.OrdersPanel.OrderRow;

public class Cache {
	public static Cache instance = new Cache();
	private HashMap<String,OrderRow> m_map = new HashMap<String,OrderRow>();
	private volatile boolean _isloading=false;
	boolean IsApiConnected = false;
	
	public Cache()
	{
				
	}

	public OrderRow GetOrderDetails(String Ticker)
	{
		if (m_map.keySet().contains(Ticker))
			return m_map.get(Ticker);
		else
			return null;
		
	}

	public void OrderHandler(NewOrder order, NewOrderState orderState,NewContract contract) {
		
		OrderRow full = m_map.get( order.permId() );
		
		if (full != null) {
			full.m_order = order;
			full.m_state = orderState;
			
		}
		else 
		{
			full = new OrderRow( contract, order, orderState);
			
			m_map.put( contract.symbol(), full);
			
		}
		
		
		
	}



	public void IsLoadingOrders(boolean b) {
		_isloading=b;
		
	}
	public boolean IsLoadingOrders()
	{
		return _isloading;
	}

	public int OrderSizeCalc(String ticker) {
		
		if(ticker.equals("AAPL"))
			return 10;
		if(ticker.equals("TSLA"))
			return 10;
		if(ticker.equals("NFLX"))
			return 10;
		else
			System.out.println("Ticker not setup in Order size mapping");
			
		return 0;
	}
}
