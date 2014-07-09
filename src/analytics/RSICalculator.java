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

public class RSICalculator extends Thread{

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
		     
		     CalculateRsi(_message);
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

	public void CalculateRsi(MarketDataTick _message) {
		try{
		
			String _Ticker = _message.getTicker();
			Bar bar = _message.getBar();
			
			
			
		
		GetHistoricMarketData GetHistMarketData = new GetHistoricMarketData();
		
		double _rsi = RSIWorker(GetHistMarketData.GetHistoricalMarketData(_Ticker),bar);
		
	//	if (_rsi > _overbought)
			_QueueHandler.SendToNewOrderQueue(_Ticker);
			
	//	if (_rsi < _oversold)
	//		_QueueHandler.SendToNewOrderQueue(_Ticker);
			
		
		
	//	return RSIWorker(GetHistMarketData.GetHistoricalMarketData(_Ticker),bar);
		
			
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}

	private double RSIWorker(HistoricResultSet Data,Bar bar)
	{
		//is _time > 5 minutes from last time in Data
		
		AnalyticsCache _AnalyticsCache = AnalyticsCache.instance;
		
		int size = Data.m_rows.size();
		long _time = bar.time();
		long lastTime = Data.m_rows.get(size-1).time();
		double CurrentRSI = _AnalyticsCache.GetRSI(Data.GetTicker());
	//	System.out.println("Last : "+_close);
	//	System.out.println((_time - lastTime));
		if((_time < (lastTime+60)) && (CurrentRSI > 0))
			return CurrentRSI;
		else if (CurrentRSI > 0)
			Data.historicalData(bar, false);
		
			//more than 5 mins from last bar in Data, need new RSI.
		
		
		
		
		ArrayList<HistDataCustom> _histData = new ArrayList<HistDataCustom>();
		
		ArrayList<Double> _UpPeriods = new ArrayList<Double>();
		ArrayList<Double> _DownPeriods = new ArrayList<Double>();
		if (Data.m_rows.size()==0)
			return 0.0;
		double _PreviousClose = Data.m_rows.get(size-15).close();
		double _CurrentClose = 0.0;
		System.out.println("Running RSI calc");
		for( int i=size-14;i < size;i++)
		{
			
			_CurrentClose = Data.m_rows.get(i).close();
			System.out.println(_CurrentClose);
			if (_CurrentClose > _PreviousClose) //Up bar
				_UpPeriods.add(_CurrentClose-_PreviousClose);
			else if (_CurrentClose < _PreviousClose) //down bar
				_DownPeriods.add(_PreviousClose-_CurrentClose);
			else //no change
			{
				_UpPeriods.add(0.0);
				_DownPeriods.add(0.0);
			}
			_PreviousClose =_CurrentClose;
		}
		
		Double _MAUpPeriods = calculateAverage(_UpPeriods);
		System.out.println("MA up "+_MAUpPeriods);
		Double _MADownPeriods = calculateAverage(_DownPeriods);
		System.out.println("MA down "+_MADownPeriods);
		Double _RS = _MAUpPeriods / _MADownPeriods;
		Double _RSI = 100- (100/(1+_RS));
		System.out.println("End calc");
		_AnalyticsCache.SetRSI(Data.GetTicker(), _RSI);
		System.out.println("RSI new calc to be : "+_RSI);
		return _RSI;
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
