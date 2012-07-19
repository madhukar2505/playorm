package com.alvazan.orm.api.spi.db;

import java.util.List;


public class Remove implements Action {
	private String colFamily;
	private byte[] rowKey;
	private RemoveEnum action;
	private List<byte[]> columns;
	
	public RemoveEnum getAction() {
		return action;
	}
	public void setAction(RemoveEnum action) {
		this.action = action;
	}
	public String getColFamily() {
		return colFamily;
	}
	public void setColFamily(String colFamily) {
		this.colFamily = colFamily;
	}
	public byte[] getRowKey() {
		return rowKey;
	}
	public void setRowKey(byte[] rowKey) {
		this.rowKey = rowKey;
	}
	public List<byte[]> getColumns() {
		return columns;
	}
	public void setColumns(List<byte[]> columnNames) {
		this.columns = columnNames;
	}
}
