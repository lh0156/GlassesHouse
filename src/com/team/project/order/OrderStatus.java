package com.team.project.order;

public enum OrderStatus {

	DEPOSIT_STAY("결제대기"),
	DELIVERY("배송대기"),
	BEING_DELIVERY("배송중"),
	DELIVERY_COMPLITE("배송완료"),
	PURCHASE_CONFIRMED("구매확정"),
	RETURN_REQUEST("반품신청"),
	RETURN_DONE("반품완료"),
	EXCHANGE_REQUEST("교환신청"),
	EXCHANGE_DONE("교환완료"),
	CANCEL_ORDER("주문취소");
	
	private String string;
	
	private OrderStatus (String string) {
		this.string = string;
	}
	
	public String getString() {
		return string;
	}
}
