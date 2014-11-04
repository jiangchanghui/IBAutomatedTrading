/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package apidemo.util;

import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import apidemo.MarketValueSummaryPanel;
import apidemo.TopModel.TopRow;

import com.ib.cache.CommonCache;
import com.ib.cache.LevelOneSnapshot;
import com.ib.cache.MarketDataCache;
import com.ib.controller.NewContract;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;
import com.ib.initialise.IBTradingMain;
import com.reademail.main.mailReader;
import com.web.request.HistoricResultSet;

public class Util {
		public static Util INSTANCE = new Util();
	private  Logger logger = Logger.getLogger( this.getClass() );
	private static final int BUF = 14;
	private static final int MAX = 300;
	
	

	 public String queue_new_trade;
	 public String queue_new_order;
	 public String queue_marketdata_tick;
	 public String exchange_marketdata_routing;
	 public String QUsername;
	 public String QPassword;
	 public String DBUsername;
	 public String DBPassword;
	 
	 private java.sql.Connection connect;
	 private java.sql.Statement statement;
	 private ResultSet resultSet;
	 
	 private MarketValueSummaryPanel marketSummary = new MarketValueSummaryPanel();
	
	/** Resize all columns in the table to fit widest row including header. */ 
	public static void resizeColumns( JTable table) {
		if (table.getGraphics() == null) {
			return;
		}
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		FontMetrics fm = table.getFontMetrics( renderer.getFont() );

		TableColumnModel mod = table.getColumnModel();
		for (int iCol = 0; iCol < mod.getColumnCount(); iCol++) {
			TableColumn col = mod.getColumn( iCol);
			
			int max = col.getPreferredWidth() - BUF;
			
			String header = table.getModel().getColumnName( iCol);
			if (header != null) {
				max = Math.max( max, fm.stringWidth( header) );
			}
			
			for (int iRow = 0; iRow < table.getModel().getRowCount(); iRow++) {
				Object obj = table.getModel().getValueAt(iRow, iCol);
				String str = obj == null ? "" : obj.toString();
				max = Math.max( max, fm.stringWidth( str) );
			}

			col.setPreferredWidth( max + BUF);
			col.setMaxWidth( MAX);
		}
		table.revalidate();
		table.repaint();
	}
	
	/** Configure dialog to close when Esc is pressed. */
	public static void closeOnEsc( final JDialog dlg) {
        dlg.getRootPane().getActionMap().put( "Cancel", new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		dlg.dispose();
        	}
        });

        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
	}

	public static void sleep( int ms) {
		try {
			Thread.sleep( ms);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void SubscribeToMarketData(String ticker) {
		if (MarketDataCache.INSTANCE.IsLastPxSubscriptionExists(ticker))
			return;
		
		 logger.info("New Market Data request for "+ticker);
		 NewContract m_contract = new NewContract();
			m_contract.symbol( ticker); 
			m_contract.secType( SecType.STK ); 
			m_contract.exchange( "SMART" ); 
			m_contract.currency( "USD" ); 
		 
		 
		HistoricResultSet dataSet = new HistoricResultSet(ticker);
		int req_id =IBTradingMain.INSTANCE.controller().reqRealTimeBars(m_contract, WhatToShow.TRADES, false, dataSet);
				
			
		}
	public void SubscribeToLevelOneQuote(String ticker) {
		if (MarketDataCache.INSTANCE.IsLevelOneSubscriptionExists(ticker))
		{
			return;
		//	logger.info("Desubscribing to LevelOne Market Data request for "+ticker);
		//	LevelOneSnapshot snapshot = MarketDataCache.INSTANCE.GetLevelOneSnapshot(ticker);
		//	IBTradingMain.INSTANCE.controller().cancelTopMktData( snapshot);
		}
			
		
		 logger.info("New LevelOne Market Data request for "+ticker);
		 NewContract m_contract = new NewContract();
			m_contract.symbol( ticker); 
			m_contract.secType( SecType.STK ); 
			m_contract.exchange( "SMART" ); 
			m_contract.currency( "USD" ); 
		
			LevelOneSnapshot dataSet = new LevelOneSnapshot(ticker);
		
		
		IBTradingMain.INSTANCE.controller().reqTopMktData(m_contract, "", false, dataSet);
		
		MarketDataCache.INSTANCE.AddLevelOneQuote(dataSet);
		
	}
	
	
	public void Log(String message)
	{
		 logger.info(message);
		
	}

	public void Log(String string, ParseException e) {
		// TODO Auto-generated method stub
		 logger.info(string,e);
	}
	
	 public void PrintStartup() {
		 String[] Diamond =
			   {"      /\\    ",
			    "     /  \\   ",
			    "    /    \\  ",
			    "    \\    /  ",
			    "     \\  /   ",
			    "      \\/    "};
		 for (int i = 0; i < Diamond.length; ++i) 
			{
			 logger.info(Diamond[i]);
			}
		 logger.info("Startup....Free memory : "+Runtime.getRuntime().freeMemory());
		 
	}
	 
	
	public boolean ReadPropertiesFile()
	{
		 Properties props = new Properties();
		 try {
			props.load(new FileInputStream("C:\\Users\\Ben\\Config\\config.properties"));
		
		 QUsername = props.getProperty("qusername");
		 QPassword = props.getProperty("qpassword");
		 queue_new_trade = props.getProperty("queue_new_trade");
		 DBUsername = props.getProperty("dbusername");
		 DBPassword = props.getProperty("dbpassword");
		 queue_new_order = props.getProperty("queue_new_order");
		 queue_marketdata_tick = props.getProperty("queue_marketdata_tick");
		 exchange_marketdata_routing = props.getProperty("exchange_marketdata_routing");
		 return true;
		 } catch (Exception e) {
				// TODO Auto-generated catch block
			 logger.fatal(e.toString(),e);
			 return false;
			}
		
	}
	
	public void ConnectToDatabase()
	{
		
		logger.info("Connecting to database...");
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ibtrading?"
							+ "user=" + DBUsername + "&password="
							+ DBPassword);
			
			
			logger.info("Connecting to database...Ok");	
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.toString(),e);
		}
		
		
	}
	public void WriteToDatabase(String statement)
	{
		try {
			if (connect == null)
				ConnectToDatabase();
			
			PreparedStatement preparedStatement;
			preparedStatement = connect.prepareStatement(statement);
			preparedStatement.executeUpdate();
			} 
		catch (SQLException e) {
			logger.error(e.toString(),e);
		}
	}
	public double ReadTodaysPnL(boolean high)
	{
		
		
		String column;
		if(high)
			column="High";
		else
			column="Low";
		
		try{
			statement = connect.createStatement();
			resultSet = statement
	          .executeQuery("select "+column+" from ibtrading.daypnl where date = '"+GetDate()+"'");
			
			 while (resultSet.next()) {
				 return resultSet.getDouble(column);
			 }
			
		}
		catch(Exception e)
		{
			
		}
		return 0.0;
	}
	
	public int GetCurrentPnL()
	{
		
	
		if (!marketSummary.m_model.isInitialised())
		{
			marketSummary.subscribe();
			logger.error("Market Summary not initialised");	 
			return 0;
		}
		else
		{
					
		int RPnL = Integer.parseInt( (String) marketSummary.m_model.getValueAt(0, 11));
		logger.debug("Current pnl : $"+RPnL);	 
		return RPnL;
		}
			
		
		
	}
	public String GetDate()
	{
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(date);
	}
private boolean _hasInsertedTodaysDate=false;
	public void DatabaseEntryForToday() {
		
		if(!_hasInsertedTodaysDate)
		{
			try{
				if (connect == null)
					ConnectToDatabase();
				WriteToDatabase("insert into ibtrading.daypnl values('"+GetDate()+"',0,0,0)");
		_hasInsertedTodaysDate=true;
			}
			catch(Exception e)
			{
				logger.error(e.toString(),e);
			}
		}
			
	}

	

}
