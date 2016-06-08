package com.df;

public class Parametrs {

	private String UnickAttribute;
	private String Type;
	private Boolean In=false;
	public Parametrs(String unickAttribute,String type_,Boolean in) {
		this.UnickAttribute=unickAttribute;
		this.Type=type_;
		this.In=in;
	}
	
	public Parametrs(String unickAttribute,String type_){
		this(unickAttribute,type_,false);
	}
	
	public String getUnickAttribute() {
		return UnickAttribute;
	}
	public void setUnickAttribute(String unickAttribute) {
		UnickAttribute = unickAttribute;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public Boolean getIn() {
		return In;
	}
	public void setIn(Boolean in) {
		In = in;
	}
}
