package analytics;




import hft.main.QueueHandler;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.benberg.struct.MarketDataTick;
import com.benberg.struct.NewOrderRequest;
import com.ib.cache.CommonCache;
import com.ib.client.Contract;
import com.ib.controller.NewContract;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

import apidemo.ContractPanel;

import apidemo.util.HtmlButton;
import apidemo.util.NewTabbedPanel;
import apidemo.util.UpperField;
import apidemo.util.VerticalPanel;
import apidemo.util.NewTabbedPanel.NewTabPanel;
import apidemo.util.VerticalPanel.StackPanel;

public class AdminPanel  extends NewTabPanel{
    private final NewTabbedPanel m_requestPanel = new NewTabbedPanel();
    private final NewContract m_contract = new NewContract();
    public AdminPanel() {
        
     
        setLayout( new BorderLayout() );
        add( new Panel(), BorderLayout.NORTH);
        
        
    }
    private class Panel extends JPanel {
        
     
         UpperField m_symbol = new UpperField();
         UpperField m_Qty = new UpperField();
         UpperField m_Ratio = new UpperField();
        Panel() {
            HtmlButton SetQty = new HtmlButton( "Set Qty") {
                @Override protected void actionPerformed() {
                	if (m_Qty.getInt() != 0)
    					com.ib.cache.CommonCache.instance.SetHftQty(m_Qty.getInt());
                    
                    
                }

            };
            HtmlButton SetRatio = new HtmlButton( "Set Ratio") {
                @Override protected void actionPerformed() {
               //     onsetPos();
                	if (m_Ratio.getDouble() != 0)
    					com.ib.cache.CommonCache.instance.SetHftRatio(m_Ratio.getDouble());
                    
                }
            };
            HtmlButton setMarketable = new HtmlButton( "Analytics Marketable") {
                @Override protected void actionPerformed() {
              //      onSetMarketable();
                    
                    
                }
            };
            VerticalPanel butPanel = new VerticalPanel();
            butPanel.add( SetQty);
            butPanel.add( SetRatio);
                       
            VerticalPanel paramPanel = new VerticalPanel();
            paramPanel.add("Hft Parameters");
            paramPanel.add( "Set hft Qty", m_Qty );
            paramPanel.add( "SetCloseOrderRatioToAvgBarSize", m_Ratio );          
            JPanel rightPanel = new StackPanel();
            rightPanel.add( paramPanel);
            
            setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
          
            
            add (paramPanel);
            add( butPanel);
        }

       
    }
	@Override
	public void activated() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void closed() {
		// TODO Auto-generated method stub
		
	}
}
