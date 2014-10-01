package com.ib.initialise;

import org.apache.log4j.Logger;

import apidemo.util.Util;

public class RecordIntradayPnL extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
		
	public void run()
	{
		log.info("Running intraday PnL service");
		
		while(true)
		{
			
			try 
			{
				Thread.sleep(6000);
				RecordIntradayPnL();
				
			} catch (InterruptedException e) 
			{
				log.fatal(e.toString(),e);
			
			}
		}
		
		
		
	}
	private void RecordIntradayPnL()
	{
				
			double CurrentHigh= Util.INSTANCE.ReadTodaysPnL(true);
			double CurrentLow= Util.INSTANCE.ReadTodaysPnL(false);
			double LastPnL =Util.INSTANCE.GetCurrentPnL();
						
			//Check entry exists for todays date
			Util.INSTANCE.DatabaseEntryForToday();
					
			//check high
			if (LastPnL>CurrentHigh){
				Util.INSTANCE.WriteToDatabase("update ibtrading.daypnl set High="+LastPnL+" where date ='"+Util.INSTANCE.GetDate()+"'");
				log.info("Recorded new intraday PnL high of "+LastPnL);
				return;
			}
			else if (LastPnL < CurrentLow){
				Util.INSTANCE.WriteToDatabase("update ibtrading.daypnl set Low="+LastPnL+" where date ='"+Util.INSTANCE.GetDate()+"'");
				log.info("Recorded new intraday PnL low of "+LastPnL);
				return;
			}
			return;
			
		}
	}