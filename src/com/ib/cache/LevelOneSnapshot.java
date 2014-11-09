package com.ib.cache;

import static com.ib.controller.Formats.fmtPct;

import com.ib.controller.NewTickType;
import com.ib.controller.ApiController.TopMktDataAdapter;
import com.ib.controller.Types.MktDataType;

public class LevelOneSnapshot extends TopMktDataAdapter{

	private String ticker;
	private int bidSize;
	private int askSize;
	private double bid;
	private double ask;
	private double last;
	private int volume;
	private long lastTime;
	private double close;
	private double change;
	private boolean halted;
	
	//constructor to create object
	public LevelOneSnapshot(String Ticker) {
		this.ticker = Ticker;
	}



	public long GetLastUpdateTime()
	{
		return lastTime;
	}
	
	public String getTicker() {
		return ticker;
	}
	
	public String GetChangePercent()
	{
		return close == 0	? "--" : fmtPct( (last - close) /close);
	}
	
	@Override public void tickPrice( NewTickType tickType, double price, int canAutoExecute) {
		switch( tickType) {
			case BID:
				bid = price;
				break;
			case ASK:
				ask = price;
				break;
			case LAST:
				last = price;
				break;
			case CLOSE:
				close = price;
				break;
		}
		
	}
	@Override public void tickSize( NewTickType tickType, int size) {
		switch( tickType) {
			case BID_SIZE:
				bidSize = size;
				break;
			case ASK_SIZE:
				askSize = size;
				break;
			case VOLUME:
				volume = size;
				break;
		}
		
	}
	
	public boolean isActive()
	{
		if (close!=0)
		{
			if (last<=0)
				last=close;
			return true;
		}
		else
			return false;
	}
	public boolean isHalted()
	{
		return halted;
	}
	
	//update last Time
	@Override public void tickString(NewTickType tickType, String value) {
		switch( tickType) {
			case LAST_TIMESTAMP:
				lastTime = Long.parseLong( value) * 1000;
				break;
		}
	}
	
	//if trading session changes , i.e halted.
	@Override public void marketDataType(MktDataType marketDataType) {
		halted = marketDataType == MktDataType.Frozen;
		
	}

	public String toString()
	{
		return ticker.toUpperCase()+": "+last+" ("+GetChangePercent()+")                            ["+bid+" : "+ask+"]";		
	}

	
}
