package com.ib.sample;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.ib.cache.PositionCache;
import com.reademail.main.mailReader;

public class ServiceHandler extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );
	private CommandLine cmd;
	public ServiceHandler(CommandLine cmd) {
		this.cmd=cmd;
		
	}
	public void run(){
	
		int i=0;
		log.info("Runing Startup services..");
		log.info("Awaiting connection to trading ....");
		while(!IBTradingMain.INSTANCE.IsApiConnected() && i <20)
		{
			try {
				Thread.sleep(1000);
				i++;
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (i >=20)
		{
			log.info("Initialising services Failed : Trading Api not connected");
			return;
		}
		log.info("SUCCESS : Connected to Trading");
		if (cmd.hasOption("CentralRisk"))
		{
			try {
				new CentralRiskControl(((Number)cmd.getParsedOptionValue("RiskLimit")).intValue()).start();
				log.info("Central Risk Control... OK");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.fatal(e.toString(),e);
				System.exit(-1);
			}
		}
		new mailReader().start();
		log.info("New Trade Reader... OK");
		PositionCache.INSTANCE.Subscribe();
	
		
		
	}

}
