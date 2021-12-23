package com.team.project.item;

public enum Shape {

	ROUND("라운드"),
	SQUARE("스퀘어"),
	HALF_FRAME("하금테"),
	MIX("믹스"),
	BOEING("보잉"),
	CATEYE("캣아이"),
	ELSE("기타");
	
	private String string;
	
	private Shape(String string) {
		this.string = string;
	}
	
	public String getString() {
		return string;
	}
}
