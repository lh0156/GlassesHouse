package com.team.project.payment;

public enum PayOption {

	CARD("카드결제"),
	BANKBOOK("무통장입금");
	
	private String string;
	
	private PayOption(String string) {
		this.string = string;
	}
	
	public String getString() {
		return string;
	}
}
