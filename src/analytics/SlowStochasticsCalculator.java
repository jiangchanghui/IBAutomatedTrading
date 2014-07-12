package analytics;

import hft.main.Cache;
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
	 private final static String Ex_marketdata_routing = "Ex_marketdata_routing";
	 private static String QueueName;
	 ConnectionFactory factory;
	 Connection connection;
	 Channel channel;
	 GetHistoricMarketData GetHistMarketData;
	 QueueHandler _QueueHandler;
	 String Ticker;
	 Cache _HftCache;
	 Bar _TempIntraMinuteBar;
	 double _overbought =0.0;
	 double _oversold = 0.0;
	private void setup()
	{
		try{
			_TempIntraMinuteBar.SetOpen(0);//clears temp bar
			_TempIntraMinuteBar.SetHigh(0);
			_TempIntraMinuteBar.SetLow(999);		
			
			
			
		   factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername("Admin"); 
			factory.setPassword("Admin"); 
		    connection = factory.newConnection();
		    channel = connection.createChannel();
		    channel.exchangeDeclare(Ex_marketdata_routing, "direct");
	        String QueueName = channel.queueDeclare().getQueue();
	        
	        channel.queueBind(QueueName, Ex_marketdata_routing, Ticker);
	        
	        
		//    channel.queueDeclare(Q_marketdata_tick, false, false, false, null);
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
			GetHistMarketData = new GetHistoricMarketData();
			SlowStoWorkerHistorical(GetHistMarketData.GetHistoricalMarketData("AAPL"));
		QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(QueueName, true, consumer);

		    while (true) {
		      System.out.println("SlowSto worker "+ this.getId()+"Waiting for data");
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      
		      MarketDataTick _message = fromBytes( delivery.getBody());
		      String routingKey = delivery.getEnvelope().getRoutingKey();
		      
		     System.out.println(" [x] Received '" + _message + "', calculating SlowSto");
		     if(routingKey.equals(Ticker))
		    	 CalculatSlowSto(_message);
		     else
		    	 System.out.println("Received routing key :"+routingKey+", Expected : "+Ticker);
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
		
		
		
		SlowStoWorker(GetHistMarketData.GetHistoricalMarketData(_Ticker),bar);
		
		
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}

	private void SlowStoWorkerHistorical(HistoricResultSet Data)
	{
		
		
		String res = "";
	for(int i =15;i<Data.m_rows.size()-14;i++)
	{
		Bar bar = Data.m_rows.get(i);
		CalcSto(bar,Data,i);
		
	}
	
	}
	private double[] CalcSto(Bar bar, HistoricResultSet Data, int counter)
	{
	//	AnalyticsTests a= new AnalyticsTests();//test
		
		
		
		String _Ticker = Data.GetTicker();
		AnalyticsCache _AnalyticsCache = AnalyticsCache.instance;
	
		double _CurrentClose = bar.close();
		double _lowestLowOfPreviousPeriods = CalcLowestLow(Data,counter);
		double _highestHighOfPreviousPeriods = CalcHighestHigh(Data,counter);
		double A = _CurrentClose - _lowestLowOfPreviousPeriods;		
				//A current close - lowest low of previous 14 periods
		double B = _highestHighOfPreviousPeriods - _lowestLowOfPreviousPeriods;
		double K = (A/B)*100;
		
		HistoricalStochasticsCache _cache= _AnalyticsCache.GetHistoricalStochasticMap(_Ticker);
		
		int _size = _cache.GetHistMap().size();
		if (_size < 3)
		{
			// not enough data
		//	_cache.GetHistMap().add(new StochasticsStruct(0,0,A,B));
			System.out.println("Array too small - skipping calc");
			_AnalyticsCache.SetStochastics(_Ticker,0, 0,A,B);
			return null;
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
			System.out.println("Not enough data yet (size)- skipping calc");
			//not enough data still
			_AnalyticsCache.SetStochastics(_Ticker,0, SlowSto,A,B);
			return null;
		}
		
		double S1 = _cache.GetHistMap().get(_size-1).getS();
		double S2 = _cache.GetHistMap().get(_size-2).getS();
		double S3 = _cache.GetHistMap().get(_size-3).getS();
		
		double SignalLine = (S1+S2+S3)/3;
		_AnalyticsCache.SetStochastics(_Ticker,SignalLine, SlowSto,A,B);
		
		System.out.println("End calc");
		System.out.println("Stochastics Calc to be:Time : "+bar.formattedTime()+" Slo Stow : "+SlowSto+" , Signal Line : "+SignalLine);
	
	//	a.FileWriter(bar.formattedTime()+","+bar.high()+","+bar.low()+","+bar.open()+","+bar.close()+","+A+","+B+","+SlowSto+","+SignalLine);
		
		
		return new double[] {SlowSto, SignalLine};
			
		
		
	}
		
	
	
	private void SlowStoWorker(HistoricResultSet Data,Bar bar)
	{
		//is _time > 5 minutes from last time in Data
		
		AnalyticsCache _AnalyticsCache = AnalyticsCache.instance;
		String _Ticker = Data.GetTicker();
		int size = Data.m_rows.size();
		long _time = bar.time();
		long lastTime = Data.m_rows.get(size-1).time();
	
		_TempIntraMinuteBar.SetClose(bar.close());
		if(_TempIntraMinuteBar.open()==0)
			_TempIntraMinuteBar.SetOpen(bar.open());
		if(_TempIntraMinuteBar.high()<bar.high())
			_TempIntraMinuteBar.SetHigh(bar.high());
		if(_TempIntraMinuteBar.low()>bar.low())
			_TempIntraMinuteBar.SetLow(bar.low());
		
		if((_time < (lastTime+60)))
		{
			System.out.println(lastTime - _time);
			return;
		}
			
		else 
			Data.historicalData(_TempIntraMinuteBar, false);
		

		
		double[] result = CalcSto(bar,Data,Data.m_rows.size());
		
		double SlowSto = result[0];
		double SignalLine = result[1];
			//more than 5 mins from last bar in Data, need new RSI.
			
		System.out.println("Stochastics Calc to be: Slo Stow : "+SlowSto+" , Signal Line : "+SignalLine);
		
		
		//if signal line has moved above Slow sto and slow sto is below 20.
		if (SlowSto < 20 && SignalLine > SlowSto)
		{
			_QueueHandler.SendToNewOrderQueue(_Ticker);
		}
		
		//Add bar into average bar size calc
		
		_HftCache.CalcAverageBarSize(_Ticker, _TempIntraMinuteBar);
		
		_TempIntraMinuteBar.SetOpen(0);//clears temp bar
		_TempIntraMinuteBar.SetHigh(0);
		_TempIntraMinuteBar.SetLow(999);
		
		
		}
		
		
	private double CalcHighestHigh(HistoricResultSet data,int endIndex) {
		//int size = data.m_rows.size();
		double high = 0.0;
		double value = 0.0;
		for( int i=endIndex-14;i < endIndex;i++)
		{
			value = data.m_rows.get(i).high(); 
		if (value > high)
			high = value;
		
		}
		return high;
	}

	private double CalcLowestLow(HistoricResultSet data,int endIndex) {
		//int size = data.m_rows.size();
		double low = 9999.0;
		double value = 0.0;
		for( int i=endIndex-14;i < endIndex;i++)
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

	public void SetTicker(String ticker) {
		this.Ticker = ticker;
		// TODO Auto-generated method stub
		
	}
	}
