package analytics;

import hft.main.QueueHandler;

import com.benberg.struct.MarketDataTick;
import com.ib.controller.Bar;
import com.web.request.GetHistoricMarketData;
import com.web.request.HistoricResultSet;

public class BackTesting {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try{
		//get data for given period, 1 min intervals
		GetHistoricMarketData mdm = new GetHistoricMarketData();
		HistoricResultSet data = mdm.GetHistoricalMarketData("AAPL");
		
		
		for(Bar bar : data.m_rows)
		{
			//dump on market data topic
			QueueHandler.instance.SendToMarketDataTickQueue(new MarketDataTick("AAPL", bar));	
			
			Thread.sleep(100);
		}
		
		
		
		}
		catch (Exception e)
		{
			
		}
		
		

	}

}
