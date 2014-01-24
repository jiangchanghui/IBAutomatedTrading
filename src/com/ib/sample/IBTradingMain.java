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


import com.ib.controller.ApiController;
import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController.IBulletinHandler;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.ITimeHandler;
import com.ib.controller.Types.NewsType;
import com.ib.controller.Types.SecType;
import com.posttrade.main.CloseAllPositions;
import com.reademail.main.OrderTemplate;
import com.reademail.main.mailReader;
import com.twitter.main.SendTweet;
import com.web.server.WebServer;


import apidemo.AccountInfoPanel;
import apidemo.MarketDataPanel;
import apidemo.TicketDlg;
import apidemo.TradingPanel;
import apidemo.util.HtmlButton;
import apidemo.util.NewTabbedPanel;
import apidemo.util.VerticalPanel;



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
	
	public Map<String,OrderTemplate> m_ordersMap = new HashMap<String,OrderTemplate>();
	public Map<String,String> m_errorMap = new HashMap<String,String>();	
	
	public static void main(String[] args) throws UnknownHostException {
		// TODO Auto-generated method stub
		INSTANCE.run();
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

	 	
	private void run() throws UnknownHostException {
		
		new mailReader().start();
		new SendTweet().start();
		WebServer _webServer = new WebServer();
		EoDClosePositions e = new	EoDClosePositions();
		//Timer for task to close all positions
	//	e.run();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 21);
		calendar.set(Calendar.MINUTE, 35);
		calendar.set(Calendar.SECOND, 0);
		Date time = calendar.getTime();

		Timer timer = new Timer();
		timer.schedule(new EoDClosePositions(), time);
	

		
		
		
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
		m_controller.connect( "127.0.0.1", 7496, 0);
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


@Override public void connected() {
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


