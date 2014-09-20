package com.ib.cache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.benberg.struct.MarketDataTick;
import com.ib.controller.AccountSummaryTag;
import com.ib.controller.Bar;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.ApiController.IAccountSummaryHandler;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.initialise.IBTradingMain;
import com.twitter.main.SendTweet;

import analytics.AnalyticsCache;
import analytics.HistoricalRsiCache;
import apidemo.AccountInfoPanel;
import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionModel;

public class CommonCache {
	private  Logger log = Logger.getLogger( this.getClass() );
	public static CommonCache instance = new CommonCache();
	private HashMap<String,OrderRow> orders_map = new HashMap<String,OrderRow>();
	private ArrayList<String> Tickers_list = new ArrayList<>();
	private HashMap<String,ArrayList<Double>> AverageBarSize_Map = new HashMap<String,ArrayList<Double>>();
	private HashMap<String,MarketDataTuple> LastPx_Map = new HashMap<String,MarketDataTuple>();
	PositionModel _positions = new PositionModel();
	private volatile boolean _isloading=false;
	boolean IsApiConnected = false;
	private OrdersModel _OpenOrders = new OrdersModel();
	private int _hftQty=500;
	private double _hftRatio=0.5;
	public void Setup()
	{
		
		IBTradingMain.INSTANCE.controller().reqPositions( _positions);		//Needed to store live position updates
		Tickers_list.add("AAPL");
		IBTradingMain.INSTANCE.controller().takeFutureTwsOrders( _OpenOrders);
		IBTradingMain.INSTANCE.controller().reqLiveOrders(_OpenOrders);//Pushes order updates live. 
	}
	
	public OrdersModel GetOpenOrders()
	{
		IBTradingMain.INSTANCE.controller().reqLiveOrders(_OpenOrders);
		return _OpenOrders;
	}
	
	
	public  ArrayList<String> GetTickersList()
	{
		return Tickers_list;
		
	}
	public PositionModel GetAllPositions()
	{
		return _positions;
	}
	public void CalcAverageBarSize(String Ticker,Bar bar) {
		//new bar arrived
		ArrayList<Double> tmp =AverageBarSize_Map.get(Ticker);
		double range = bar.high()-bar.low();
		if (tmp ==null)
			tmp = new ArrayList<Double>();
			
		if(tmp.size()<20)
			tmp.add(tmp.size(),range);
		else
		{
			for (int i=0;i<20;i++)
			{
			tmp.add(i,tmp.get(i+1));
			}
			tmp.add(20, range);
		}
		AverageBarSize_Map.put(Ticker, tmp);
				
			
	}
		
	public double GetAverageBarSize(String Ticker)
	{
	ArrayList<Double> tmp =AverageBarSize_Map.get(Ticker);
	
	if(tmp==null)//not really possible, did for testing
		return 1.0;
	double average=0.0;
	for(double value : tmp)
	{
		average+=value;
		
	}
	average = average/tmp.size();
	
	return average;
		
	}
	
	public OrderRow GetOrderDetails(String Ticker)
	{
		if (orders_map.keySet().contains(Ticker))
			return orders_map.get(Ticker);
		else
			return null;
		
	}

	public void OrderHandler(NewOrder order, NewOrderState orderState,NewContract contract) {
		
		OrderRow full = orders_map.get( order.permId() );
		
		if (full != null) {
			full.m_order = order;
			full.m_state = orderState;
			
		}
		else 
		{
			full = new OrderRow( contract, order, orderState);
			
			orders_map.put( contract.symbol(), full);
			
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
	public int IsPosiitonExist(String Ticker)
	{
		for (PositionRow row : _positions.m_map.values())
		{
			if (row.m_contract.symbol().equals(Ticker))
				return row.m_position;
		}
		return 0;
	}
	public void MarketDataTick(MarketDataTick _message) {

		log.info("Updating last price for "+_message.getTicker()+" , LastPx : "+_message.getBar().close());

			LastPx_Map.put(_message.getTicker(), new MarketDataTuple(_message.getBar().close(), System.currentTimeMillis()));
		
		
		
	}
	public void SetLastPx(String Ticker, double LastPx)
	{
		log.info("Updated last price for "+Ticker+" to "+LastPx);
		LastPx_Map.put(Ticker,new MarketDataTuple(LastPx,System.currentTimeMillis()));
	}
	public double GetLastPx(String Ticker)
	{
		MarketDataTuple tmp = LastPx_Map.get(Ticker);
		if (tmp == null)
			return 0.0;
		else
		{
			long LastUpdateTime = tmp.LastUpdateTime;
			long delta = System.currentTimeMillis() - LastUpdateTime;
			if (delta > 60000 );
				log.warn("STALE MARKET DATA : Last Update time for "+Ticker+"+is over "+delta/1000+" seconds old");
			return tmp.LastPx;
		}
		
		
	}

	private class MarketDataTuple
	{
		double LastPx = 0.0;
		long LastUpdateTime=0;
		
		public MarketDataTuple (double LastPx, long LastUpdateTime)
		{
			this.LastPx = LastPx;
			this.LastUpdateTime = LastUpdateTime;
		}
		
		
		
		
		
	}



	public void SetHftQty(int qty) {
		// TODO Auto-generated method stub
		_hftQty = qty;
		log.info("Invocation successfull, hftQty = "+_hftQty);
	}
	public int GetHftQty()
	{
		return _hftQty;
	}

	public void SetHftRatio(double ratio) {
		// TODO Auto-generated method stub
		_hftRatio = ratio;
		log.info("Invocation successfull, hftRatio = "+_hftRatio);
	}
	public double GetHftRatio()
	{
		return _hftRatio;
	}

public class PositionModel implements IPositionHandler {
	HashMap<PositionKey,PositionRow> m_map = new HashMap<PositionKey,PositionRow>();
	ArrayList<PositionRow> m_list = new ArrayList<PositionRow>();

	@Override public void position(String account, NewContract contract, int position, double avgCost) {
		PositionKey key = new PositionKey( account, contract.conid() );
		PositionRow row = m_map.get( key);
		if (row == null) {
			row = new PositionRow();
			m_map.put( key, row);
			m_list.add( row);
		//	System.out.println("Added new position for "+row.ToString());
		}
		row.update( account, contract, position, avgCost);
		System.out.println("Updated position for "+row.ToString());
		
	}

	public ArrayList<PositionRow> ToList()
	{
		return m_list;
	}

	@Override
	public void positionEnd() {
		// TODO Auto-generated method stub
		
	}
	
		}
	


class PositionKey {
	String m_account;
	int m_conid;

	PositionKey( String account, int conid) {
		m_account = account;
		m_conid = conid;
	}
	
	@Override public int hashCode() {
		return m_account.hashCode() + m_conid;
	}
	
	@Override public boolean equals(Object obj) {
		PositionKey other = (PositionKey)obj;
		return m_account.equals( other.m_account) && m_conid == other.m_conid;
	}
}

public class PositionRow {
	String m_account;
	NewContract m_contract;
	int m_position;
	double m_avgCost;

	void update(String account, NewContract contract, int position, double avgCost) {
		m_account = account;
		m_contract = contract;
		m_position = position;
		m_avgCost = avgCost;
	}
	
	public NewContract GetContract()
	{
		return m_contract;
		
	}
	public int GetPosition()
	{
		return m_position;
	}
	
	public double GetAvgPx()
	{
		return m_avgCost;		
	}
	
	public String ToString()
	{
		return m_contract.symbol()+"/"+m_position+"/@"+m_avgCost;
	}
	
 
}


}


