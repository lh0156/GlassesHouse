package com.team.project;

public enum Path {

	//mac
	MEMBER("data/member/member.dat"),
	
	ITEM("data/item/item.dat"),
	ITEM_MATERIAL("data/item/itemMaterial.dat"),
	ITEM_TAG("data/item/itemTag.dat"),
	
	ORDER("data/order/order.dat"),
	ORDER_ITEM_TAG("data/order/orderItemTag.dat"),
	ORDER_TIME_STAMP("data/order/orderTimeStamp.dat"),
	ORDER_MAN("data/order/orderMan.dat"),
	RECEIVER("data/order/receiver.dat"),

	CARD_PAY_INFO("data/payment/cardPayInfo.dat"),
	BANKBOOK_PAY_INFO("data/payment/bankbookPayInfo.dat");
	
	private String path;
	
	private Path(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
