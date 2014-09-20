package hft.main;

import java.util.ArrayList;

import org.apache.log4j.Logger;


import hft.main.Cache.PositionModel;
import hft.main.Cache.PositionRow;
import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;

import com.benberg.struct.NewOrderRequest;
import com.ib.client.Contract;
import com.ib.controller.OrderStatus;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;
import com.ib.initialise.IBTradingMain;
import com.twitter.main.SendTweet;

public class LivePositionHandler extends Thread{
	public static LivePositionHandler INSTANCE = new LivePositionHandler();
	private  Logger log = Logger.getLogger( this.getClass() );
		
	
	//Call this every 30 seconds to handle position changes
	public void run()
	{
		while(true){
			try {
				Thread.sleep(10000);
				CheckPositions();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	
	private void CheckPositions() {
		
		for(PositionRow _position : Cache.instance.GetAllPositions().m_list)
		{
			CheckIfHasCLoseOrder(_position.m_contract.symbol(),_position.m_position,_position.m_avgCost);
		}
		
	}



	public void OnPositionChanged(Contract contract, int pos, double avgCost)
	{
	//	if(pos==0)
		//	return;
		log.info("Poisiton CHange for :"+contract.m_symbol+", Position : "+pos+", AvCost : "+avgCost);
			
		int timeout = 0;
		//this happens so that theres time for the orders to be processed and read before continuing. Prevent race condition.
		while(Cache.instance.IsLoadingOrders() && timeout < 100)
		{
			try {
				Thread.sleep(100);
				timeout++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("Completed loading orders : " + Cache.instance.IsLoadingOrders());
		if (Cache.instance.IsLoadingOrders())
			return;
		
		
		String Ticker = contract.m_symbol;
		
	}
	private void CheckIfHasCLoseOrder(String Ticker,int pos,double AvgPx)
	{
	
		log.info("Checking Position  :"+Ticker+", Position : "+pos+", AvCost : "+AvgPx);
		
		if (pos < 0)
		{
			//there should be no negative positions. If there is, its an error and close the position.
			log.warn("Position for "+Ticker+" is "+pos+". Cancelling orders and closing position");
			IBTradingMain.INSTANCE.controller().cancelAllOrders();
			QueueHandler.INSTANCE.SendToNewOrderQueue(new NewOrderRequest(Ticker, Math.abs(pos), OrderType.MKT, 0.0,Action.BUY,this.getClass().getName()));
			return;
		}
		
		
		for (OrderRow row : Cache.instance.GetOpenOrders().m_orders)
		{

			log.info("Checking order ticker :  "+ row.m_contract.symbol()+" for position  "+Ticker);	
			log.info("Order state is : "+row.m_state.status());
			OrderStatus state = row.m_state.status();
			if (state ==OrderStatus.Submitted || state==OrderStatus.PreSubmitted)
			{


			if(row.m_contract.symbol().equals(Ticker))
			{//order pending already
				//check quantity on order
				log.info("Found order for "+ row.m_contract.symbol());	
			
				if(row.m_order.totalQuantity()!=pos)
				{
					//cancel order
					log.info("Found position size discrepancy , order quantity :  "+ row.m_order.totalQuantity() + ". Position : "+pos);	
					
					IBTradingMain.INSTANCE.controller().cancelOrder( row.m_order.orderId());
					//create new order for position size.
					log.info("Replacing order for "+ Ticker + " with quantity : "+pos+". Average execution cost : "+AvgPx);	
					if(pos>0)
						CreateNewClosePositionOrder(Ticker,pos,AvgPx,Action.SELL);
					else
						CreateNewClosePositionOrder(Ticker,pos,AvgPx,Action.BUY);	
					
					return;
				}
				
				else if (row.m_order.action()==Action.BUY)
					return;
				else
					return; //order placed is equal to pending order therefore nothing to be done.
					
			}
			}
		}
		// if we get here there is no order, so need to create one
		if(pos>0)
		{
		log.info("No close order found for  "+ Ticker + ". Creating order with quantity : "+pos+". Average execution cost : "+AvgPx);	
		CreateNewClosePositionOrder(Ticker,pos,AvgPx,Action.SELL);
		}
		if(pos<0)
		{
		log.info("No close order found for  "+ Ticker + ". Creating order with quantity : "+pos+". Average execution cost : "+AvgPx);	
		CreateNewClosePositionOrder(Ticker,Math.abs(pos),AvgPx,Action.BUY);
		}
		
	}

	private void CreateNewClosePositionOrder(String Ticker, int Quantity, double avgPx, Action side) {
		//Get Average bar size - is there a better way to set sell limit price?
		double av_barsize = Cache.instance.GetAverageBarSize(Ticker);
		//Add onto current position
		double LimitPx = avgPx+(av_barsize*Cache.instance.GetHftRatio());
		log.info("Sending close order for "+Ticker+", Quantity : "+Quantity+" , PositionAvgPx : "+avgPx+" , LimitPx : "+LimitPx);
		QueueHandler.INSTANCE.SendToNewOrderQueue(new NewOrderRequest(Ticker, Quantity, OrderType.LMT, LimitPx,side,this.getClass().getName()));
		
		
		//place order
		
		
	}

	private void OnError() {
	//	IBTradingMain.INSTANCE.controller().cancelAllOrders();	
				
	}
	
	
	
	

}
