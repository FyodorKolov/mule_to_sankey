package com.df;

public class UnickAttribute {

	private String[] Name;
	private Boolean isInserted=false;
	
	public UnickAttribute(Boolean Inserted,String... name) {
		this.isInserted=Inserted;
		this.Name=name;
	}
	
	public UnickAttribute(String... name) {
		this(false,name);
	}
	
	public String[] getName() {
		return Name;
	}
	public void setName(String[] name) {
		Name = name;
	}
	public Boolean getIsInserted() {
		return isInserted;
	}
	public void setIsInserted(Boolean isInserted) {
		this.isInserted = isInserted;
	}
}
