package com.ib.cache;

import org.apache.log4j.Logger;

import com.ib.initialise.IBTradingMain;

import apidemo.OrdersPanel.OrdersModel;
import apidemo.PositionsPanel.PositionRow;


public class PositionCache {
	private  Logger log = Logger.getLogger( this.getClass() );
	public static PositionCache INSTANCE = new PositionCache();
	 PositionModel _positions = new PositionModel();

	
	public void Subscribe(){
		IBTradingMain.INSTANCE.controller().reqPositions( _positions);		//Needed to store live position updates
		log.info("Position Cache subscrption... OK");
	
	}
	public PositionModel GetAllPositions()
	{
		return _positions;
	}
	
	public PositionRow GetPosition(String Ticker)
	{
		for (PositionRow row : _positions.m_map.values())
		{
			if (row.m_contract.symbol().equals(Ticker))
				return row;
		}
		return null;
	}

}
