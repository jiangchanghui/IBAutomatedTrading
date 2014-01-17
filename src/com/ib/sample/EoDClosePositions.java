package com.ib.sample;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import apidemo.TradesPanel;

import com.ib.client.ExecutionFilter;



public class EoDClosePositions extends TimerTask {
	private static final Logger log = Logger.getLogger( EoDClosePositions.class.getName() );
	 PositionModel m_model = new PositionModel();
	@Override
	public void run() {
	//	 TradesPanel m_tradesPanel = new TradesPanel();
	//		IBTradingMain.INSTANCE.controller().reqExecutions2( new ExecutionFilter(), m_tradesPanel);
			
		
			
	//		ArrayList<apidemo.TradesPanel.FullExec> _Execs = new ArrayList<apidemo.TradesPanel.FullExec>();
		//_Execs = m_tradesPanel.getExecutions();
			
			
		IBTradingMain.INSTANCE.controller().reqPositions( m_model);
	
			
		int _PositionQuantity = 0;
		int count2=0;
		int count=0;
		log.log(Level.INFO ,"{0} Executions found",m_model.getRowCount());
		
	
		  long lDateTime = new Date().getTime();
		  
		  long _timeout = 5000;
		  long _delta=0;
		  int _iterator=0;
		while (m_model.getRowCount()==0 && _iterator<5)
		{
			log.log(Level.FINEST ,"{0} Executions found retrying",m_model.getRowCount());
				
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			_iterator++;
			
			
		//	 long lDateTimeNow = new Date().getTime();
			
		//	_delta = lDateTimeNow-lDateTime;
			
		}
		

	}

}
