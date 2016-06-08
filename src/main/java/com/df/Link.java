package com.df;

import com.google.gson.annotations.SerializedName;

public class Link {
	@SerializedName("source")
	private Integer Source;
	@SerializedName("target")
	private Integer Target;
	@SerializedName("value")
	private Integer Value=1;
	@SerializedName("color")
	private String Color;
	@SerializedName("direction")
	private Boolean Direction=false;
	@SerializedName("flow_index")
	private Integer flowIndex;
	
	public Integer getSource() {
		return Source;
	}
	public void setSource(Integer source) {
		Source = source;
	}
	public Integer getTarget() {
		return Target;
	}
	public void setTarget(Integer target) {
		Target = target;
	}
	public Integer getValue() {
		return Value;
	}
	public void setValue(Integer value) {
		Value = value;
	}
	public String getColor() {
		return Color;
	}
	public void setColor(String color) {
		Color = color;
	}
	public Boolean getDirection() {
		return Direction;
	}
	public void setDirection(Boolean direction) {
		Direction = direction;
	}
	public Integer getFlowIndex() {
		return flowIndex;
	}
	public void setFlowIndex(Integer flowIndex) {
		this.flowIndex = flowIndex;
	}
}
