package hft.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

import javax.swing.table.AbstractTableModel;

import com.ib.controller.AccountSummaryTag;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.ApiController.IAccountSummaryHandler;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.sample.IBTradingMain;
import analytics.AnalyticsCache;
import analytics.HistoricalRsiCache;
import apidemo.AccountInfoPanel;
import apidemo.OrdersPanel.OrderRow;
import apidemo.PositionsPanel.PositionModel;

public class Cache {
	public static Cache instance = new Cache();
	private HashMap<String,OrderRow> orders_map = new HashMap<String,OrderRow>();
	PositionModel _positions;
	private volatile boolean _isloading=false;
	boolean IsApiConnected = false;
	
	public Cache()
	{
		_positions = new PositionModel();
		IBTradingMain.INSTANCE.controller().reqPositions( _positions);		
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

private static class PositionRow {
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
}


