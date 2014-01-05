package apidemo;

import javax.swing.SwingUtilities;

import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.TopModel.TopRow;

import com.ib.client.Contract;
import com.ib.client.ExecutionFilter;
import com.ib.controller.ApiController.ILiveOrderHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.ITradeReportHandler;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.SecType;
import com.ib.sample.main;

public class CreateOrderFromEmail {

	
	public void CreateOrder(String Symbol, int Quantity, Action Side)
	{
		double FarPrice =  0.0;
		
		GetPosition(Symbol);
		
		NewContract contract = new NewContract();
		NewOrder order = new NewOrder();
		contract.symbol(Symbol);
		order.totalQuantity(Quantity);
		order.action(Side);
		
		//FarPrice = GetFarPrice(contract,Side);
		
		order.orderType(OrderType.MKT);
		
		order.lmtPrice(FarPrice);
		
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");
		
		;
		
		main.INSTANCE.controller().placeOrModifyOrder(contract, order, new IOrderHandler() {
			@Override public void orderState(NewOrderState orderState) {
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						
					}
				});
			}
			@Override public void handle(int errorCode, final String errorMsg) {
				
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						System.out.println(errorMsg);
					}
				});
			}
		});
		
		
	}
	private double GetFarPrice(NewContract contract,Action side)
	{
		TopRow row = new TopRow( null, contract.description() );
		//m_rows.add( row);
		main.INSTANCE.controller().reqTopMktData(contract, "", false, row);
	//	fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
		
		if (row.m_ask >0.0 && row.m_bid >0.0)
		{
			if (side==Action.BUY)
			{
				return row.m_ask+0.02;
			}
			else
			{
				return row.m_bid-0.02;
			}
		}
		else
			return 0.0;
	}
	
	private void GetPosition(String Symbol)
	{
		ITradeReportHandler m_tradeReportHandler = null;
		OrdersModel m_model = new OrdersModel();
		main.INSTANCE.controller().reqExecutions( new ExecutionFilter(), m_tradeReportHandler);
		main.INSTANCE.controller().reqLiveOrders( m_model);

		
		
		for (int i=0;i< m_model.getRowCount();i++)
		{
		//	m_model.get(i).;
		}
		
		
		
		
	}
}
