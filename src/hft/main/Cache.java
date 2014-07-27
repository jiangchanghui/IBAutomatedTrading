package hft.main;

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

public class Cache {
	private  Logger log = Logger.getLogger( this.getClass() );
	public static Cache instance = new Cache();
	private HashMap<String,OrderRow> orders_map = new HashMap<String,OrderRow>();
	private ArrayList<String> Tickers_list = new ArrayList<>();
	private HashMap<String,ArrayList<Double>> AverageBarSize_Map = new HashMap<String,ArrayList<Double>>();
	private HashMap<String,MarketDataTuple> LastPx_Map = new HashMap<String,MarketDataTuple>();
	PositionModel _positions = new PositionModel();
	private volatile boolean _isloading=false;
	boolean IsApiConnected = false;
	private OrdersModel _OpenOrders = new OrdersModel();
	public Cache()
	{
		
		IBTradingMain.INSTANCE.controller().reqPositions( _positions);		//Needed to store live position updates
		Tickers_list.add("AAPL");
		IBTradingMain.INSTANCE.controller().takeFutureTwsOrders( _OpenOrders); //Pushes order updates live. 
	}
	public OrdersModel GetOpenOrders()
	{
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
	public PositionRow IsPosiitonExist(String Ticker)
	{
		for (PositionRow row : _positions.m_map.values())
		{
			if (row.m_contract.symbol().equals(Ticker))
				return row;
		}
		return null;
	}
	

public class PositionModel extends AbstractTableModel implements IPositionHandler {
	HashMap<PositionKey,PositionRow> m_map = new HashMap<PositionKey,PositionRow>();
	ArrayList<PositionRow> m_list = new ArrayList<PositionRow>();

	@Override public void position(String account, NewContract contract, int position, double avgCost) {
		PositionKey key = new PositionKey( account, contract.conid() );
		PositionRow row = m_map.get( key);
		if (row == null) {
			row = new PositionRow();
			m_map.put( key, row);
			m_list.add( row);
		}
		row.update( account, contract, position, avgCost);
		
		
	}

	@Override public void positionEnd() {
	//	m_model.fireTableDataChanged();
		
	}

	public void clear() {
		m_map.clear();
		m_list.clear();
		fireTableDataChanged();
	}

	@Override public int getRowCount() {
		return m_map.size();
	}

	@Override public int getColumnCount() {
		return 4;
	}
	
	@Override public String getColumnName(int col) {
		switch( col) {
			case 0: return "Account";
			case 1: return "Contract";
			case 2: return "Position";
			case 3: return "Avg Cost";
			default: return null;
		}
	}

	@Override public Object getValueAt(int rowIn, int col) {
		PositionRow row = m_list.get( rowIn);
		
		switch( col) {
			case 0: return row.m_account;
			case 1: return row.m_contract.description();
			case 2: return row.m_contract.symbol();
			case 3: return row.m_position;
			case 4: return Formats.fmt( row.m_avgCost);
			default: return null;
		}
	}
}

private static class PositionKey {
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

public static class PositionRow {
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
	
	public String ToString()
	{
		return m_contract.symbol()+"/"+m_position+"/@"+m_avgCost;
	}
	
 private void AttachLogHandler()
 {
	 try
		{
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Handler handler = new FileHandler("C:\\Users\\Ben\\IBLogs\\IBTrading"+sdf.format(date)+".log");
		//log.addHandler(handler);
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
 }
 private class SummaryModel extends AbstractTableModel implements IAccountSummaryHandler {
		ArrayList<SummaryRow> m_rows = new ArrayList<SummaryRow>();
		HashMap<String,SummaryRow> m_map = new HashMap<String,SummaryRow>();
		boolean m_complete;

		public void clear() {
			IBTradingMain.INSTANCE.controller().cancelAccountSummary( this);
			m_rows.clear();
			m_map.clear();
			m_complete = false;
			fireTableDataChanged();
		}

		@Override public void accountSummary(String account, AccountSummaryTag tag, String value, String currency) {
			SummaryRow row = m_map.get( account);
			if (row == null) {
				row = new SummaryRow();
				m_map.put( account, row);
				m_rows.add( row);
			}
			row.update( account, tag, value);
			
			if (m_complete) {
				fireTableDataChanged();
			}
		}
		
		@Override public void accountSummaryEnd() {
			fireTableDataChanged();
			m_complete = true;
		}

		@Override public int getRowCount() {
			return m_rows.size();
		}

		@Override public int getColumnCount() {
			return AccountSummaryTag.values().length + 1; // add one for Account column 
		}
		
		@Override public String getColumnName(int col) {
			if (col == 0) {
				return "Account";
			}
			return AccountSummaryTag.values()[col - 1].toString();
		}

		@Override public Object getValueAt(int rowIn, int col) {
			SummaryRow row = m_rows.get( rowIn);

			if (col == 0) {
				return row.m_account;
			}
			
			AccountSummaryTag tag = AccountSummaryTag.values()[col - 1];
			String val = row.m_map.get( tag);
			
			switch( tag) {
				case Cushion: return fmtPct( val);
				case LookAheadNextChange: return fmtTime( val);
				default: return AccountInfoPanel.format( val, null);
			}
		}

		public String fmtPct(String val) {
			return val == null || val.length() == 0 ? null : Formats.fmtPct( Double.parseDouble( val) );
		}

		public String fmtTime(String val) {
			return val == null || val.length() == 0 || val.equals( "0") ? null : Formats.fmtDate( Long.parseLong( val) * 1000);
		}
	}
	
	private static class SummaryRow {
		String m_account;
		HashMap<AccountSummaryTag,String> m_map = new HashMap<AccountSummaryTag,String>();
		
		public void update(String account, AccountSummaryTag tag, String value) {
			m_account = account;
			m_map.put( tag, value);
		} 
	}
}



public void MarketDataTick(MarketDataTick _message) {
	
	
		LastPx_Map.put(_message.getTicker(), new MarketDataTuple(_message.getBar().close(), System.currentTimeMillis()));
	
	
	
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
}


