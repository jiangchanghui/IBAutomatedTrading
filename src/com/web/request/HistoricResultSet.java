package com.web.request;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import analytics.RSICalculator;

import com.ib.controller.Bar;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.IRealTimeBarHandler;

public class HistoricResultSet  implements IHistoricalDataHandler, IRealTimeBarHandler {

	public final ArrayList<Bar> m_rows = new ArrayList<Bar>();
	final BarModel m_model = new BarModel();
	private volatile boolean complete =false;
	String Ticker ="";
	String TimeFrame;
	hft.main.Main hft_Class;
	RSICalculator _RSICalc;
	public int GetCount()
	{
		return m_rows.size();
	}
	
	public HistoricResultSet(String ticker)
	{
		this.Ticker = ticker;
		hft_Class = new hft.main.Main();
		_RSICalc = new RSICalculator();
	}
	
	
	public HistoricResultSet(String ticker,String TimeFrame)
	{
		this.Ticker = ticker;
		this.TimeFrame=TimeFrame;
	
	}
	
	
	public String GetTicker()
	{
		return Ticker;
	}
	public String GetTimeFrame()
	{
		return TimeFrame;
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
	}
	
	@Override public void historicalDataEnd() {
		fire();
	}

	@Override public void realtimeBar(Bar bar) {
		m_rows.add( bar); 
		fire(bar);
	}
	//historical fire
	private void fire() {
		
		SwingUtilities.invokeLater( new Runnable() {
			@Override public void run() {
				m_model.fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
				//	m_chart.repaint();
			}
		});
	}
	
	private void fire(Bar bar) {
		final double close = bar.close();
		final long time = bar.time();
		final Bar _bar = bar;
		SwingUtilities.invokeLater( new Runnable() {
			@Override public void run() {
				m_model.fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
				
				//This is when new amrket data arrives. Should call Rsi Function.
				System.out.println(GetTicker()+"  " +time+"   "+close+ "  "+hft_Class);
				hft_Class.MarketDataTick(GetTicker(),_RSICalc.CalculateRsi(GetTicker(), _bar));
			//	_RSICalc.CalculateRsi(GetTicker(), time,close);
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
}
