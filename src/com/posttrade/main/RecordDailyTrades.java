package com.posttrade.main;

import java.util.TimerTask;

import apidemo.TradesPanel;

import com.ib.client.ExecutionFilter;
import com.ib.initialise.IBTradingMain;

public class RecordDailyTrades extends TimerTask {
    public void run() {
        System.out.format("Time's up!%n");
       
        TradesPanel m_tradesPanel = new TradesPanel();
		IBTradingMain.INSTANCE.controller().reqExecutions2( new ExecutionFilter(), m_tradesPanel);
		
        
       
    }
}
		
		
		
		
		
	