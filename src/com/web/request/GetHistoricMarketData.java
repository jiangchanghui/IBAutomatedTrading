package com.web.request;

import java.util.HashMap;

import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.Types.BarSize;
import com.ib.controller.Types.DurationUnit;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.ib.sample.IBTradingMain;


public class GetHistoricMarketData {

	/**
	 * @param args
	 */
	public void get() {
		// TODO Auto-generated method stub

		IBTradingMain  IBMain = IBTradingMain.INSTANCE;
		ApiController m_controller = IBMain.GetController();
		
		NewContract m_contract = new NewContract();
		m_contract.symbol( "IBM" ); 
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" ); 
		m_contract.currency( "USD" ); 
				
		HistoricResultSet dataSet = new HistoricResultSet();
		IBTradingMain.INSTANCE.controller().reqHistoricalData(m_contract, "20140611 21:00", 1, DurationUnit.DAY, BarSize._10_mins, WhatToShow.TRADES, false, dataSet);
//		m_resultsPanel.addTab( "Historical " + m_contract.symbol(), panel, true, true);
		
		
		HashMap<Integer, IHistoricalDataHandler> Test = m_controller.GetHistoricalMap();
		
		System.out.println(Test.size());
		
		
	}

}
