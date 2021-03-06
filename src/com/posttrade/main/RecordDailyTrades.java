package com.posttrade.main;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import apidemo.AccountInfoPanel;
import apidemo.MarketValueSummaryPanel;
import apidemo.TradesPanel;
import apidemo.util.Util;
import apidemo.util.NewTabbedPanel.INewTab;

import com.ib.client.CommissionReport;
import com.ib.client.Execution;
import com.ib.client.ExecutionFilter;
import com.ib.controller.NewContract;
import com.ib.controller.ApiController.IAccountHandler;
import com.ib.controller.ApiController.ITradeReportHandler;
import com.ib.initialise.IBTradingMain;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class RecordDailyTrades extends Thread {
	private  Logger log = Logger.getLogger( this.getClass() );
	private java.sql.Connection connect;
	private java.sql.Statement statement;
	private ResultSet resultSet;

	public void run() {
		log.info("Starting record of PnL");
		// Connect to db
		try {
			
			int RPnL = Util.INSTANCE.GetCurrentPnL();
			log.info("Writing daily pnl : $"+RPnL);	 
			WriteDayPnL(RPnL);
			
			
			Set<String> _ListOfTickers = new HashSet<String>();

			log.info("Requesting executions...");	 
			TradesPanel m_tradesPanel = new TradesPanel();
			IBTradingMain.INSTANCE.controller().reqExecutions2(
					new ExecutionFilter(), m_tradesPanel);

			for (apidemo.TradesPanel.FullExec exec : m_tradesPanel.getExecutions()) {
				_ListOfTickers.add(exec.m_contract.symbol()); // get unique list
																// of tickers
			}
			log.info(_ListOfTickers.size()+" Tickers found : "+_ListOfTickers.toArray());	 
			double _BuyVal =0.0;
			double _SellVal=0.0;
			double _pnl =0.0;
			for (String ticker : _ListOfTickers) {
				//iterate over each ticker
				
				for (apidemo.TradesPanel.FullExec exec : m_tradesPanel.getExecutions()) {
					
					if (exec.m_contract.symbol().equals(ticker)){
						//found execution for ticker
						if (exec.m_trade.m_side.equals("BUY"))
							_BuyVal +=exec.m_trade.m_shares*exec.m_trade.m_avgPrice;
						else
							_SellVal +=exec.m_trade.m_shares*exec.m_trade.m_avgPrice;
					}
					
					
				}
				//pnl = buyval - sellval
				_pnl = _SellVal-_BuyVal;
				log.info("PnL for "+ticker+" : "+_pnl);
				WritePositionPnL(ticker,_pnl);
				
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.fatal(e.toString(),e);
		}
		log.info("Finished writing pnl");
		// Tweet values

	}

	private void WriteDayPnL(int rPnL) {
		
			Util.INSTANCE.WriteToDatabase("update ibtrading.daypnl set pnl="+rPnL+" where date ='"+Util.INSTANCE.GetDate()+"'");
		
	}
	private void WritePositionPnL(String Ticker,double rPnL) {
		
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			
			String s = "insert into ibtrading.positionpnl values ("+sdf.format(date)+","+Ticker+","+rPnL+")";
			Util.INSTANCE.WriteToDatabase(s);
	}
	
}
