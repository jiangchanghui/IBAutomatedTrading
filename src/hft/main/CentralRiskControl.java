package hft.main;

import hft.main.Cache.PositionRow;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;


import com.benberg.struct.NewOrderRequest;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.OrderType;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.Types.Action;
import com.ib.initialise.IBTradingMain;

public class CentralRiskControl extends Thread{
	

private  Logger log = Logger.getLogger( this.getClass() );

private double ThresholdLoss = -45;
	public void run()
	{
				
		while(true)
		{
			
			try 
			{
				Thread.sleep(10000);
				CheckPositions();
				
			} catch (InterruptedException e) 
			{
				
				e.printStackTrace();
			}
		}
						
	}
	
	private void CheckPositions()
	{
		
<<<<<<< HEAD
	
				
=======
		int timeout = 0;
		/*
		while(m_model.IsLoadingPositions() && timeout < 100)
		{
			try {
				Thread.sleep(100);
				timeout++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	//	log.info("");
		if (m_model.IsLoadingPositions())
			return;
		*/
>>>>>>> 323a762996a36211c9570c6fe7b7fbe7a5f6b876
		//positions loaded, check each one.
		for(hft.main.Cache.PositionRow _position : Cache.instance.GetAllPositions().m_list)
		{
		
			String Ticker = _position.m_contract.symbol();
			int Quantity = _position.m_position;
			double AvgPx = _position.m_avgCost;
			log.info("Checking Position : "+Ticker+"/"+Quantity+"@"+AvgPx);
			
						
			double LastPx = Cache.instance.GetLastPx(Ticker);
			log.info("LastPx for "+Ticker+" : "+LastPx);
			double PnL = (LastPx - AvgPx )*Quantity;
			
			if (PnL < ThresholdLoss)
			{
				log.info("PnL for "+Ticker+" is "+PnL+". This is greater than "+ThresholdLoss+" . Closing position.");
				
			
				log.info("Sending close order for "+Ticker+", Quantity : "+Quantity+" , PositionAvgPx : "+AvgPx);
				QueueHandler.instance.SendToNewOrderQueue(new NewOrderRequest(Ticker, Quantity, OrderType.MKT,0.0,Action.SELL));
				
				//close position
				
			}
			else
			{
				log.info("PnL for "+Ticker+" is "+PnL+". Within limit ("+ThresholdLoss+"), no action to take");
				
			}
			
		}
		
		
		
		
		
	}
	

	
	
	public class PositionModel extends AbstractTableModel implements IPositionHandler {
		HashMap<PositionKey,PositionRow> m_map = new HashMap<PositionKey,PositionRow>();
		ArrayList<PositionRow> m_list = new ArrayList<PositionRow>();
		boolean m_complete=false;
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
				m_complete = true;
		}
		public boolean IsLoadingPositions()
		{
			return m_complete;
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
				case 2: return row.m_position;
				case 3: return Formats.fmt( row.m_avgCost);
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
	}
	
}
