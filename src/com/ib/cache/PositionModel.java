package com.ib.cache;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

import apidemo.PositionsPanel.PositionKey;
import apidemo.PositionsPanel.PositionRow;

import com.ib.controller.Formats;
import com.ib.controller.NewContract;
import com.ib.controller.ApiController.IPositionHandler;

public class PositionModel extends AbstractTableModel implements IPositionHandler {
	HashMap<PositionKey,PositionRow> m_map = new HashMap<PositionKey,PositionRow>();
	public ArrayList<PositionRow> m_list = new ArrayList<PositionRow>();

	@Override public void position(String account, NewContract contract, int position, double avgCost) {
		PositionKey key = new PositionKey( account, contract.conid() );
		PositionRow row = m_map.get( key);
		if (row == null) {
			row = new PositionRow();
			m_map.put( key, row);
			m_list.add( row);
		}
		row.update( account, contract, position, avgCost);
		
		
	}

	@Override public void positionEnd() {
	
	
	}

	public void clear() {
		m_map.clear();
		m_list.clear();
		fireTableDataChanged();
	}

	@Override public int getRowCount() {
		return m_map.size();
	}

	@Override public int getColumnCount() {
		return 4;
	}
	
	@Override public String getColumnName(int col) {
		switch( col) {
			case 0: return "Account";
			case 1: return "Contract";
			case 2: return "Position";
			case 3: return "Avg Cost";
			default: return null;
		}
	}

	@Override public Object getValueAt(int rowIn, int col) {
		PositionRow row = m_list.get( rowIn);
		
		switch( col) {
			case 0: return row.m_account;
			case 1: return row.m_contract.description();
			case 2: return row.m_position;
			case 3: return Formats.fmt( row.m_avgCost);
			default: return null;
		}
	}
}
