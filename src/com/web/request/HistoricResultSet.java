package com.web.request;

import hft.main.QueueHandler;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.benberg.struct.MarketDataTick;
import com.benberg.struct.RequestType;
import com.ib.cache.MarketDataCache;
import com.ib.controller.Bar;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.IRealTimeBarHandler;

public class HistoricResultSet  implements IHistoricalDataHandler, IRealTimeBarHandler {

	public final ArrayList<Bar> m_rows = new ArrayList<Bar>();
	final BarModel m_model = new BarModel();
	private volatile boolean complete =false;
	String Ticker ="";
	String TimeFrame;
	long LastUpdateTime;
	RequestType RequestType;
	
	
	public int GetCount()
	{
		return m_rows.size();
	}
	
	public HistoricResultSet(String ticker)
	{
		this.Ticker = ticker;
	
	}
	
	
	public HistoricResultSet(String ticker,String TimeFrame)
	{
		this.Ticker = ticker;
		this.TimeFrame=TimeFrame;
	
	}
	
	
	public HistoricResultSet(String ticker, RequestType requestType) {
		this.Ticker = ticker;
		this.RequestType=requestType;;
	}

	public String GetTicker()
	{
		return Ticker;
	}
	public String GetTimeFrame()
	{
		return TimeFrame;
	}
	public RequestType getRequestType() {
		return RequestType;
	}		
	public void SetLoadComplete()
	{
		complete = true;		
	}
	public boolean IsLoadComplete()
	{
		return complete;	
	}
	
	@Override public void historicalData(Bar bar, boolean hasGaps) {
		m_rows.add( bar);
		
		this.LastUpdateTime = System.currentTimeMillis();
		
	}
	
	@Override public void historicalDataEnd() {
		fire();
	
		
	}

	@Override public void realtimeBar(Bar bar) {
		m_rows.add( bar); 
		fire(bar);
	
	}
	
	private void fire(Bar bar) {
		final double close = bar.close();
		final long time = bar.time();
		final Bar _bar = bar;
		SwingUtilities.invokeLater( new Runnable() {
			@Override public void run() {
				m_model.fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
				
				MarketDataCache.INSTANCE.SetLastPx(GetTicker(), close);
				
				QueueHandler.INSTANCE.SendToMarketDataTickQueue(new MarketDataTick(GetTicker(), _bar));
			}
		});
	}
	
	
	
	
	private void fire() {
		SwingUtilities.invokeLater( new Runnable() {
			@Override public void run() {
				m_model.fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
			//	m_chart.repaint();
			}
		});
	}
	class BarModel extends AbstractTableModel {
		@Override public int getRowCount() {
			return m_rows.size();
		}

		@Override public int getColumnCount() {
			return 7;
		}
		
		@Override public String getColumnName(int col) {
			switch( col) {
				case 0: return "Date/time";
				case 1: return "Open";
				case 2: return "High";
				case 3: return "Low";
				case 4: return "Close";
				case 5: return "Volume";
				case 6: return "WAP";
				default: return null;
			}
		}

		@Override public Object getValueAt(int rowIn, int col) {
			Bar row = m_rows.get( rowIn);
			switch( col) {
				case 0: return row.formattedTime();
				case 1: return row.open();
				case 2: return row.high();
				case 3: return row.low();
				case 4: return row.close();
				case 5: return row.volume();
				case 6: return row.wap();
				default: return null;
			}
		}
	}
	public long getLastUpdateTime() {
		return LastUpdateTime;
	}
	
}
