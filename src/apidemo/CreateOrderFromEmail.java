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

	
	public void CreateOrder(String Symbol, int Quantity, Action Side, Double FFLimit)
	{
		if (Symbol==null  || Quantity == 0 || Side == null || FFLimit == 0.0)
		{
			return;
		}
		
		double FarPrice =  0.0;
		
		GetPosition(Symbol);
		
		NewContract contract = new NewContract();
		NewOrder order = new NewOrder();
		contract.symbol(Symbol);
		order.totalQuantity(Quantity);
		order.action(Side);
		
			
		order.orderType(OrderType.MKT);
		
		order.lmtPrice(FarPrice);
		
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");
		
		if (!FatFingerViolation(contract, order,FFLimit))
		{
		
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
		TradesPanel m_tradesPanel = new TradesPanel();
		main.INSTANCE.controller().reqExecutions( new ExecutionFilter(), m_tradesPanel);
		main.INSTANCE.controller().reqLiveOrders( m_model);

		
		
	//	m_tradesPanel.ac
		
		for (int i=0;i< m_model.getRowCount();i++)
		{
		
		//	m_model.get(i).;
		}
		
	}
	public String GetPositions()
	{
		OrdersModel m_model = new OrdersModel();
		main.INSTANCE.controller().reqLiveOrders( m_model);
		
		return m_model.get(1).m_contract.symbol();
	}
	
	private boolean FatFingerViolation(NewContract contract, NewOrder order,Double FFLimit)
	{
		Double FarPrice = GetFarPrice(contract,order.action());
	
		Double Notional = FarPrice * order.totalQuantity();
		
		if (Notional > FFLimit)
			return true;
		else
			return false;
		
		
		
	}
	
	
}
