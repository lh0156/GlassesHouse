package com.team.project.order;

import java.util.Calendar;

public class OrderTimeStamp {

	private Calendar orderTime;
	private Calendar deliveryTime;
	private Calendar purchaseConfirmTime;
	private Calendar extraRequestTime;
	private Calendar extraDoneTime;
	
	public OrderTimeStamp() {
		this(null, null, null, null, null);
	}

	public OrderTimeStamp(Calendar orderTime, Calendar deliveryTime, Calendar purchaseConfirmTime, Calendar extraRequestTime, Calendar extraDoneTime) {
		this.orderTime = orderTime;
		this.deliveryTime = deliveryTime;
		this.purchaseConfirmTime = purchaseConfirmTime;
		this.extraRequestTime = extraRequestTime;
		this.extraDoneTime = extraDoneTime;
	}

	public Calendar getOrderTime() {
		return orderTime;
	}
	
	public void setOrderTime(Calendar orderTime) {
		this.orderTime = orderTime;
	}
	
	public Calendar getDeliveryTime() {
		return deliveryTime;
	}
	
	public void setDeliveryTime(Calendar deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	
	public Calendar getPurchaseConfirmTime() {
		return purchaseConfirmTime;
	}
	
	public void setPurchaseConfirmTime(Calendar purchaseConfirmTime) {
		this.purchaseConfirmTime = purchaseConfirmTime;
	}
	
	public Calendar getExtraRequestTime() {
		return extraRequestTime;
	}
	
	public void setExtraRequestTime(Calendar extraRequestTime) {
		this.extraRequestTime = extraRequestTime;
	}
	
	public Calendar getExtraDoneTime() {
		return extraDoneTime;
	}
	
	public void setExtraDoneTime(Calendar extraDoneTime) {
		this.extraDoneTime = extraDoneTime;
	}

	@Override
	public String toString() {
		return "orderTime=" + orderTime
				+ "\ndeliveryTime=" + deliveryTime
				+ "\npurchaseConfirmTime=" + purchaseConfirmTime
				+ "\nextraRequestTime=" + extraRequestTime
				+ "\nextraDoneTime=" + extraDoneTime;
	}
}
