package edu.hust.model;

import java.util.List;

public class ListWrapper {

	private List<?> listRecord;

	public ListWrapper(List<?> listRecord) {
		super();
		this.listRecord = listRecord;
	}

	public ListWrapper() {
		super();
		// TODO Auto-generated constructor stub
	}

	public List<?> getListRecord() {
		return listRecord;
	}

	public void setListRecord(List<?> listRecord) {
		this.listRecord = listRecord;
	}
	
	
}
