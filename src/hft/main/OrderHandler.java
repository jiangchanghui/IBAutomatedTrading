package hft.main;

import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import analytics.SDM;

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

public class OrderHandler extends Thread{
	private  Logger log = Logger.getLogger( this.getClass() );

	 
	 private final static String Q_create_new_order = "Q_create_new_order";
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

			    channel.queueDeclare(Q_create_new_order, false, false, false, null);
			    log.info("Initiliased Order listener for Q_create_new_order");
			
			
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
		    channel.basicConsume(Q_create_new_order, true, consumer);

			    while (true) {
			      log.info("Order listener waiting for data");
			      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			      
			    String _message =  new String(delivery.getBody());
			      
			     log.info(" [x] Received '" + _message + "', for new order creation");
			     NewOrderRequest(_message);
			  
			    }
			}
			catch(Exception e)
			{
				log.fatal(e.getStackTrace().toString());
			}
	}
	
	private void NewOrderRequest(String Ticker)
	{
		
			if (Ticker==null)
			{
				log.error("Ticker = "+Ticker+". Order creation Failed. ");
			//	 log.log(Level.WARNING ,"Order Create failed with Symbol : {0}, Quantity : {1}, Side : {2}, FFLimit :{3}",new Object[]{Symbol,Quantity,Side.toString(),FFLimit});
				return;
			}
			
			NewContract contract = new NewContract();
			NewOrder order = new NewOrder();
			contract.symbol(Ticker);
			order.totalQuantity(100);
			order.action(Action.BUY);
			
				
			order.orderType(OrderType.MKT);
			order.tif(TimeInForce.FOK);//prevents blocked shorts hanging around.
			//order.lmtPrice(FarPrice);
			
			contract.secType(SecType.STK);
			contract.exchange("SMART");
			contract.currency("USD");
			
		log.info("SEND new order creation for "+order.action()+" "+order.totalQuantity()+" "+order.orderType()+" "+order.tif()+" "+contract.toString());
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
							
							if (errorMsg.contains("Order held"))
							{
							//	log.log(Level.SEVERE ,"Order is held, cancelling all open orders");
								IBTradingMain.INSTANCE.controller().cancelAllOrders();
							}
							if (errorMsg.contains("not be placed"))
							{
								IBTradingMain.INSTANCE.controller().cancelAllOrders();
							}
							
							
							
							
							
						}
					});
				}
			});
		
			
		}
		
		
	
}




	
