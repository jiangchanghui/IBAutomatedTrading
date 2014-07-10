package analytics;

import hft.main.QueueHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


import com.benberg.struct.MarketDataTick;
import com.benberg.struct.NewMarketDataRequest;
import com.ib.controller.Bar;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.web.request.GetHistoricMarketData;
import com.web.request.HistoricResultSet;

public class SlowStochasticsCalculator extends Thread{

	 private final static String Q_marketdata_tick = "Q_marketdata_tick";
	// private final static String QUEUE_OUT = "q_web_in";
	 ConnectionFactory factory;
	 Connection connection;
	 Channel channel;
	 QueueHandler _QueueHandler;
	 double _overbought =0.0;
	 double _oversold = 0.0;
	private void setup()
	{
		try{
		   factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername("Admin"); 
			factory.setPassword("Admin"); 
		    connection = factory.newConnection();
		    channel = connection.createChannel();

		    channel.queueDeclare(Q_marketdata_tick, false, false, false, null);
		    System.out.println("Initiliased lisener thread "+ this.getId());
		_overbought = SDM.overbought;
		_oversold = SDM.oversold;
		_QueueHandler = new QueueHandler().instance;
		}
		catch(Exception e)
		{
		e.printStackTrace();	
		}
		
		
	}

	public void run()
	{
		try{
			setup();
		QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(Q_marketdata_tick, true, consumer);

		    while (true) {
		      System.out.println("RSI worker "+ this.getId()+"Waiting for data");
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      
		      MarketDataTick _message = fromBytes( delivery.getBody());
		      
		     System.out.println(" [x] Received '" + _message + "', calculating RSI");
		     
		     CalculatSlowSto(_message);
		    }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
public  MarketDataTick fromBytes(byte[] body) {
		
	MarketDataTick obj = null;
	    try {
	        ByteArrayInputStream bis = new ByteArrayInputStream (body);
	        ObjectInputStream ois = new ObjectInputStream (bis);
	        obj = (MarketDataTick)ois.readObject();
	        ois.close();
	        bis.close();
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	    }
	    catch (ClassNotFoundException ex) {
	        ex.printStackTrace();
	    }
	   
	    return obj;     
	}

	public void CalculatSlowSto(MarketDataTick _message) {
		try{
		
			String _Ticker = _message.getTicker();
			Bar bar = _message.getBar();
		
		GetHistoricMarketData GetHistMarketData = new GetHistoricMarketData();
		
		SlowStoWorker(GetHistMarketData.GetHistoricalMarketData(_Ticker),bar);
		
		
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}

	private void SlowStoWorker(HistoricResultSet Data,Bar bar)
	{
		//is _time > 5 minutes from last time in Data
		
		AnalyticsCache _AnalyticsCache = AnalyticsCache.instance;
		String _Ticker = Data.GetTicker();
		int size = Data.m_rows.size();
		long _time = bar.time();
		long lastTime = Data.m_rows.get(size-1).time();
	
	//	if((_time < (lastTime+60)))
	//		return;
	//	else 
			Data.historicalData(bar, false);
		
			//more than 5 mins from last bar in Data, need new RSI.
		
		double _CurrentClose = bar.close();
		double _lowestLowOfPreviousPeriods = CalcLowestLow(Data);
		double _highestHighOfPreviousPeriods = CalcHighestHigh(Data);
		double A = _CurrentClose - _lowestLowOfPreviousPeriods;		
				//A current close - lowest low of previous 14 periods
		double B = _highestHighOfPreviousPeriods - _lowestLowOfPreviousPeriods;
		double K = (A/B)*100;
				//B highest high - lowest low
				//K A/B*100
		
		
		
		HistoricalStochasticsCache _cache= _AnalyticsCache.GetHistoricalStochasticMap(_Ticker);
		
		int _size = _cache.GetHistMap().size();
		if (_size < 3)
		{
			// not enough data
		//	_cache.GetHistMap().add(new StochasticsStruct(0,0,A,B));
			_AnalyticsCache.SetStochastics(_Ticker,0, 0,A,B);
			return;
		}
		double A1=_cache.GetHistMap().get(_size-1).getA();
		double A2 = _cache.GetHistMap().get(_size-2).getA();
		double A3 = _cache.GetHistMap().get(_size-3).getA();
		
		double B1=_cache.GetHistMap().get(_size-1).getB();
		double B2 = _cache.GetHistMap().get(_size-2).getB();
		double B3 = _cache.GetHistMap().get(_size-3).getB();
		
		
		//enough data, calculate slowsto
		double SlowSto= ((A1+A2+A3)/(B1+B2+B3))*100;
		if (_size < 6)
		{
			
			//not enough data still
			_AnalyticsCache.SetStochastics(_Ticker,0, SlowSto,A,B);
			return;
		}
		
		double S1 = _cache.GetHistMap().get(_size-1).getS();
		double S2 = _cache.GetHistMap().get(_size-2).getS();
		double S3 = _cache.GetHistMap().get(_size-3).getS();
		
		double SignalLine = (S1+S2+S3)/3;
		_AnalyticsCache.SetStochastics(_Ticker,SignalLine, SlowSto,A,B);
				
				
			
		System.out.println("End calc");
		
		System.out.println("Stochastics Calc to be: Slo Stow : "+SlowSto+" , Signal Line : "+SignalLine);
		
		
		//if signal line has moved above Slow sto and slow sto is below 20.
		if (SlowSto < 20 && SignalLine > SlowSto)
		{
			_QueueHandler.SendToNewOrderQueue(_Ticker);
		}
		
		
		
		
		
		
		}
		
		
	private double CalcHighestHigh(HistoricResultSet data) {
		int size = data.m_rows.size();
		double high = 0.0;
		double value = 0.0;
		for( int i=size-14;i < size;i++)
		{
			value = data.m_rows.get(i).high(); 
		if (value > high)
			high = value;
		
		}
		return high;
	}

	private double CalcLowestLow(HistoricResultSet data) {
		int size = data.m_rows.size();
		double low = 9999.0;
		double value = 0.0;
		for( int i=size-14;i < size;i++)
		{
			value = data.m_rows.get(i).low(); 
		if (value < low)
			low = value;
		
		}
		return low;
	}

	private double calculateAverage(ArrayList<Double> marks) {
		  Double sum = 0.0;
		  if(!marks.isEmpty()) {
		    for (Double mark : marks) {
		        sum += mark;
		    }
		    return sum.doubleValue() / marks.size();
		  }
		  return sum;
		}
	
	private  class HistDataCustom {
		private long time;
		private double close;
		
		public HistDataCustom(long time, double close) {
			this.time = time;
			this.close = close;
		}

	public long getTime()
	{
		return time;
		
	}
	
}
	}
