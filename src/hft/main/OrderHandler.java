package hft.main;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import analytics.SDM;
import apidemo.util.Util;

import com.benberg.struct.MarketDataTick;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.OrderType;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.TimeInForce;
import com.ib.initialise.IBTradingMain;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.twitter.main.SendTweet;
import com.web.request.GetHistoricMarketData;
import com.benberg.struct.NewOrderRequest;

public class OrderHandler extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );

	 
	// private final static String Q_create_new_order = "Q_create_new_order";
		// private final static String QUEUE_OUT = "q_web_in";
		 ConnectionFactory factory;
		 Connection connection;
		 Channel channel;
		 GetHistoricMarketData GetHistMarketData;
		 QueueHandler _QueueHandler;
	private void Setup()
	{
		try{
			   factory = new ConnectionFactory();
			    factory.setHost("localhost");
			    factory.setUsername("Admin"); 
				factory.setPassword("Admin"); 
			    connection = factory.newConnection();
			    channel = connection.createChannel();

			    channel.queueDeclare(Util.INSTANCE.queue_new_order, false, false, false, null);
			    log.info("Initiliased Order listener for "+Util.INSTANCE.queue_new_order);
			
			
			}
			catch(Exception e)
			{
				log.fatal(e.getStackTrace().toString());
			}
	}
	
	
	public void run()
	{
		try{
			Setup();
			QueueingConsumer consumer = new QueueingConsumer(channel);
		    channel.basicConsume(Util.INSTANCE.queue_new_order, true, consumer);

			    while (true) {
			      log.info("Order listener waiting for data on queue :"+Util.INSTANCE.queue_new_order );
			      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			      
			    NewOrderRequest _message =  fromBytes(delivery.getBody());
			      
			     log.info("RECV < " +Util.INSTANCE.queue_new_order+" {"+  _message.toString() + "}");
			   
			     try{
			     NewOrderRequest(_message);
			     }
			     catch (Exception e)
			     {
			    	 log.fatal(e.getStackTrace().toString()); 
			     }
			   }
			}
			catch(Exception e)
			{
				log.fatal(e.getStackTrace().toString());
			}
	}
	
	public  NewOrderRequest fromBytes(byte[] body) {
		
		NewOrderRequest obj = null;
		    try {
		        ByteArrayInputStream bis = new ByteArrayInputStream (body);
		        ObjectInputStream ois = new ObjectInputStream (bis);
		        obj = (NewOrderRequest)ois.readObject();
		        ois.close();
		        bis.close();
		    }
		    catch (Exception e) {
		    	log.fatal(e.toString());
		    }
		  
		   
		    return obj;     
		}
	
	private void NewOrderRequest(NewOrderRequest _message)
	{
			String Ticker = _message.Ticker();
			Action side = _message.Side();
			int Quantity = _message.Quantity();
			OrderType OrdType = _message.OrderType();
			double LimitPx = RountToTick(_message.LimitPx());
			
			if (Ticker==null || side ==null || Quantity <=0.0 || OrdType==null)
			{
				log.error("Ticker = "+Ticker+". Order creation Failed. "+ Ticker+"/"+side+"/"+Quantity+"/"+OrdType);
				 return;
			}
			
			NewContract contract = new NewContract();
			NewOrder order = new NewOrder();
			contract.symbol(Ticker);
			order.totalQuantity(Quantity);
			order.action(side);
			contract.secType(SecType.STK);
			contract.exchange("SMART");
			contract.currency("USD");
			
			if (OrdType.equals(OrderType.MKT))
			{
			order.orderType(OrdType);
			order.tif(TimeInForce.FOK);//prevents blocked shorts hanging around.
			}
			else
			{
				order.orderType(OrderType.LMT);
				order.lmtPrice(LimitPx);	
			}
						
			
		if(!IBTradingMain.INSTANCE._LiveStatus)
		{
			log.error("Order execution failed, Trading live status = "+IBTradingMain.INSTANCE._LiveStatus);
			return;
		}
			
			
		log.info("SEND > API {new order creation for "+order.action()+"/"+order.totalQuantity()+"/"+order.orderType()+"/"+order.lmtPrice()+"/"+order.tif()+"}");
			//log.log(Level.INFO ,"Order being executed for {0} {1} {2} at {3}",new Object[]{Side,Quantity,Symbol,order.orderType().toString()});
			IBTradingMain.INSTANCE.controller().placeOrModifyOrder(contract, order, new IOrderHandler() {
				@Override public void orderState(NewOrderState orderState) {
					SwingUtilities.invokeLater( new Runnable() {
						@Override public void run() {
							//Order Placed
							
							
							
						}
					});
				}
				@Override public void handle(final int errorCode, final String errorMsg) {
					
					SwingUtilities.invokeLater( new Runnable() {
						@Override public void run() {
						log.error("Order execution failed with ("+errorCode+","+errorMsg+")");						
						//	IBTradingMain.INSTANCE.m_errorMap.put(dateFormat.format(new Date()), errorMsg);
						
								IBTradingMain.INSTANCE.controller().cancelAllOrders();
					
							
						}
					});
				}
			});
		
			
		}


	private double RountToTick(double limitPx) {
		
		double roundedlimitPx = Math.round(limitPx*100)/100.0d;
		log.info("LimitPx rounded from "+limitPx+" to "+roundedlimitPx);
		return roundedlimitPx;
	}
		
		
	
}




	
