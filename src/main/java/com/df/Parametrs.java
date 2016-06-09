package com.df;

import java.util.Map;

public class Parametrs {

	private String Type;
	private Map<String,UnickAttribute> listElem; 
	public Parametrs(String type_,Map<String,UnickAttribute> in) {
		this.Type=type_;
		this.listElem=in;
	}
	
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public Map<String,UnickAttribute> getListElem() {
		return listElem;
	}
	public void setListElem(Map<String,UnickAttribute> in) {
		listElem = in;
	}
}
