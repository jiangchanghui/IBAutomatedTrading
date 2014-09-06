package com.ib.sample;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;




import com.ib.cache.PositionCache;
import com.ib.controller.ApiController;
import com.ib.controller.Bar;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController.IBulletinHandler;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.IRealTimeBarHandler;
import com.ib.controller.ApiController.ITimeHandler;
import com.ib.controller.Types.BarSize;
import com.ib.controller.Types.DurationUnit;
import com.ib.controller.Types.NewsType;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.WhatToShow;

import com.reademail.main.OrderTemplate;
import com.reademail.main.mailReader;
import com.twitter.main.SendTweet;
import com.web.request.GetHistoricMarketData;
import com.web.request.ListenForWebRequests;
import com.web.server.Index;
import com.web.server.WebServer;


import apidemo.AccountInfoPanel;
import apidemo.Chart;
import apidemo.MarketDataPanel;
import apidemo.TicketDlg;
import apidemo.TradingPanel;


import apidemo.util.HtmlButton;
import apidemo.util.NewTabbedPanel;
import apidemo.util.Util;
import apidemo.util.VerticalPanel;
import apidemo.util.NewTabbedPanel.NewTabPanel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class IBTradingMain implements IConnectionHandler{
	
	public static IBTradingMain INSTANCE = new IBTradingMain();
	private final JFrame m_frame = new JFrame();
	private final NewTabbedPanel m_tabbedPanel = new NewTabbedPanel(true);
	private final ConnectionPanel m_connectionPanel = new ConnectionPanel();
	private final MarketDataPanel m_mktDataPanel = new MarketDataPanel();
	
	private final JTextArea m_inLog = new JTextArea();
	private final JTextArea m_outLog = new JTextArea();
	private final Logger m_inLogger = new Logger( m_inLog);
	private final Logger m_outLogger = new Logger( m_outLog);
	private final ApiController m_controller = new ApiController( this, m_inLogger, m_outLogger);
	private final JTextArea m_msg = new JTextArea();
	private final TradingPanel m_tradingPanel = new TradingPanel();
	private final ArrayList<String> m_acctList = new ArrayList<String>();
	public ArrayList<String> accountList() 	{ return m_acctList; }
	public ApiController controller() 		{ return m_controller; }
	public JFrame frame() 					{ return m_frame; }
	private final AccountInfoPanel m_acctInfoPanel = new AccountInfoPanel();
	
	private CommandLine cmd;
	public Map<String,OrderTemplate> m_ordersMap = new HashMap<String,OrderTemplate>();
	public Map<String,String> m_errorMap = new HashMap<String,String>();	

	public volatile boolean _LiveStatus = true;
	
	
	public static void main(String[] args) throws UnknownHostException {
		// TODO Auto-generated method stub
		INSTANCE.run(args);
	}
	private static class Logger implements ILogger {
		
		final private JTextArea m_area;

		Logger( JTextArea area) {
			m_area = area;
		}

		@Override public void log(final String str) {
			SwingUtilities.invokeLater( new Runnable() {
				@Override public void run() {
//					m_area.append(str);
//					
//					Dimension d = m_area.getSize();
//					m_area.scrollRectToVisible( new Rectangle( 0, d.height, 1, 1) );
				}
			});
		}
	}

	 	
	private void run(String[] args) throws UnknownHostException {
		PropertyConfigurator.configure("c:\\Users\\Ben\\Config\\log4j.properties"); 
		Util.INSTANCE.PrintStartup();
		
		
		readCommandLineArguements(args);
		
		new ServiceHandler(cmd).start();
		
		if(!cmd.hasOption("CmdLine"))
		{
	
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 21);
		calendar.set(Calendar.MINUTE, 35);
		calendar.set(Calendar.SECOND, 0);
		Date time = calendar.getTime();

			
		m_tabbedPanel.addTab( "Connection", m_connectionPanel);
		m_tabbedPanel.addTab( "Market Data", m_mktDataPanel);
		m_tabbedPanel.addTab( "Trading", m_tradingPanel);
		m_tabbedPanel.addTab( "Account Info", m_acctInfoPanel);
		// m_tabbedPanel.addTab( "Strategy", m_stratPanel); in progress
			
		m_msg.setEditable( false);
		m_msg.setLineWrap( true);
		JScrollPane msgScroll = new JScrollPane( m_msg);
		msgScroll.setPreferredSize( new Dimension( 10000, 120) );

		JScrollPane outLogScroll = new JScrollPane( m_outLog);
		outLogScroll.setPreferredSize( new Dimension( 10000, 120) );

		JScrollPane inLogScroll = new JScrollPane( m_inLog);
		inLogScroll.setPreferredSize( new Dimension( 10000, 120) );

		NewTabbedPanel bot = new NewTabbedPanel();
		bot.addTab( "Messages", msgScroll);
		bot.addTab( "Log (out)", outLogScroll);
		bot.addTab( "Log (in)", inLogScroll);
		
        m_frame.add( m_tabbedPanel);
        m_frame.add( bot, BorderLayout.SOUTH);
        m_frame.setSize( 1024, 768);
        m_frame.setVisible( true);
        m_frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
        
        // make initial connection to local host, port 7496, client id 0
		}
		m_controller.connect( "127.0.0.1", 7496, 0);
		
    }
	public boolean IsTradingLive()
	{
		return _LiveStatus;
	}
	private void readCommandLineArguements(String[] args) {
		Options options = new Options();

		// add t option
		options.addOption("CentralRisk", false, "If specified, the central risk process will be run.");
		options.addOption(OptionBuilder.withLongOpt("RiskLimit")
                .withDescription("Max loss per position before auto close of position.")
                .withType(Number.class)
                .hasArg()
                .withArgName("argname")
                .create());
		options.addOption(OptionBuilder.withLongOpt("help").create('h'));
	//	options.addOption("RiskLimit", true, "Max loss per position before auto close of position.");
		options.addOption("CmdLine", false, "If specified, applicatio will run command line only.");
		
		
		HelpFormatter formatter = new HelpFormatter();
		
		
		CommandLineParser parser = new GnuParser();
		try {
			cmd = parser.parse( options, args);
			
			if(cmd.hasOption("-h")){
				formatter.printHelp( "program", options);
				System.exit(-1);
			}
			if(cmd.hasOption("CentralRisk") && !cmd.hasOption("RiskLimit")){
				Util.INSTANCE.Log("Must speeify a risk limit if central risk is running");
				System.exit(-1);
			}
			if(cmd.hasOption("CentralRisk")) 
				Util.INSTANCE.Log("Central Risk control enabled");
			if(cmd.hasOption("RiskLimit")) 
				Util.INSTANCE.Log("Risk limit :"+cmd.getOptionValue("RiskLimit"));
			
			
			
		} catch (ParseException e) {
			
			Util.INSTANCE.Log(e.toString(),e);
		}
		
	}
	public ApiController GetController()
	{
	return m_controller;	
	}

private class ConnectionPanel extends JPanel {
	private final JTextField m_host = new JTextField(7);
	private final JTextField m_port = new JTextField( "7496", 7);
	private final JTextField m_clientId = new JTextField("0", 7);
	private final JLabel m_status = new JLabel("Disconnected");
	
	public ConnectionPanel() {
		HtmlButton connect = new HtmlButton("Connect") {
			@Override public void actionPerformed() {
				onConnect();
			}
		};
		
		HtmlButton test = new HtmlButton("Test") {
			@Override public void actionPerformed() {
				
			}
		};
		
		

		HtmlButton disconnect = new HtmlButton("Disconnect") {
			@Override public void actionPerformed() {
				m_controller.disconnect();
			}
		};
		
		JPanel p1 = new VerticalPanel();
		p1.add( "Host", m_host);
		p1.add( "Port", m_port);
		p1.add( "Client ID", m_clientId);
		
		JPanel p2 = new VerticalPanel();
		p2.add( connect);
		p2.add( disconnect);
		p2.add(test);
		p2.add( Box.createVerticalStrut(20));
		
		JPanel p3 = new VerticalPanel();
		p3.setBorder( new EmptyBorder( 20, 0, 0, 0));
		p3.add( "Connection status: ", m_status);
		
		JPanel p4 = new JPanel( new BorderLayout() );
		p4.add( p1, BorderLayout.WEST);
		p4.add( p2);
		p4.add( p3, BorderLayout.SOUTH);

		setLayout( new BorderLayout() );
		add( p4, BorderLayout.NORTH);
	}

	protected void onConnect() {
		int port = Integer.parseInt( m_port.getText() );
		int clientId = Integer.parseInt( m_clientId.getText() );
		m_controller.connect( m_host.getText(), port, clientId);
	}
	
	
	
	
	
}

boolean _connected = false;

public boolean IsApiConnected()
{
	return _connected;
}
@Override public void connected() {
	
	
	
	_connected = true;
	show( "connected");
	m_connectionPanel.m_status.setText( "connected");
	
	m_controller.reqCurrentTime( new ITimeHandler() {
		@Override public void currentTime(long time) {
			show( "Server date/time is " + Formats.fmtDate(time * 1000) );
		}
	});
	
	m_controller.reqBulletins( true, new IBulletinHandler() {
		@Override public void bulletin(int msgId, NewsType newsType, String message, String exchange) {
			String str = String.format( "Received bulletin:  type=%s  exchange=%s", newsType, exchange);
			show( str);
			show( message);
			
		}
	});
}

@Override public void disconnected() {
	_connected = false;
	show( "disconnected");
	m_connectionPanel.m_status.setText( "disconnected");
}

@Override public void accountList(ArrayList<String> list) {
	show( "Received account list");
	m_acctList.clear();
	m_acctList.addAll( list);
}

@Override 
public void error(Exception e) {
	show( e.toString() );
}

@Override 
public void message(int id, int errorCode, String errorMsg) {
	show( id + " " + errorCode + " " + errorMsg);
}


@Override public void show( final String str) {
	SwingUtilities.invokeLater( new Runnable() {
		@Override public void run() {
			m_msg.append(str);
			m_msg.append( "\n\n");
			
			Dimension d = m_msg.getSize();
			m_msg.scrollRectToVisible( new Rectangle( 0, d.height, 1, 1) );
		}
	});
}
}




