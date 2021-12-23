package com.team.project.item;

public enum Material {

	RIMLESS("무테/반무테"),
	METAL("메탈"),
	PLASTIC("뿔테"),
	CLEAR("투명"),
	TITANIUM("티타늄"),
	WOOD("나무");
	
	private String string;
	
	private Material(String string) {
		this.string = string;
	}
	
	public String getString() {
		return string;
	}
}
