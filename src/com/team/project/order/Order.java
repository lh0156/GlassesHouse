package com.team.project.order;

import java.util.ArrayList;

import com.team.project.payment.PayInfo;

public class Order {

	private long code;
	private long memberCode;
	private String address;
	private OrderStatus status;
	private ArrayList<OrderItemTag> itemInfo;
	private PayInfo payInfo;
	private OrderTimeStamp timeStamp;
	private PersonCard orderMan;
	private PersonCard receiver;
	
	public Order() {
		
		this(0, 0, "", OrderStatus.DEPOSIT_STAY, new ArrayList<OrderItemTag>(), null, new OrderTimeStamp(), new PersonCard(), new PersonCard());
	}

	public Order(long code, long memberCode, String address, OrderStatus status, ArrayList<OrderItemTag> itemInfo, PayInfo payInfo, OrderTimeStamp timeStamp, PersonCard orderMan, PersonCard receiver) {
		this.code = code;
		this.memberCode = memberCode;
		this.address = address;
		this.status = status;
		this.itemInfo = itemInfo;
		this.payInfo = payInfo;
		this.timeStamp = timeStamp;
		this.orderMan = orderMan;
		this.receiver = receiver;
	}

	public long getCode() {
		return code;
	}
	
	public void setCode(long code) {
		this.code = code;
	}
	
	public long getMemberCode() {
		return memberCode;
	}
	
	public void setMemberCode(long memberCode) {
		this.memberCode = memberCode;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public OrderStatus getStatus() {
		return status;
	}
	
	public void setStatus(OrderStatus status) {
		this.status = status;
	}
	
	public ArrayList<OrderItemTag> getItemInfo() {
		return itemInfo;
	}
	
	public void setItemInfo(ArrayList<OrderItemTag> itemInfo) {
		this.itemInfo = itemInfo;
	}
	
	public PayInfo getPayInfo() {
		return payInfo;
	}
	
	public void setPayInfo(PayInfo payInfo) {
		this.payInfo = payInfo;
	}
	
	public OrderTimeStamp getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(OrderTimeStamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public PersonCard getOrderMan() {
		return orderMan;
	}
	
	public void setOrderMan(PersonCard orderMan) {
		this.orderMan = orderMan;
	}
	
	public PersonCard getReceiver() {
		return receiver;
	}
	
	public void setReceiver(PersonCard receiver) {
		this.receiver = receiver;
	}
	
	public int getTotalPrice() {
		
		int sum = 0;
		
		for (OrderItemTag tag : itemInfo) {
			sum += tag.getPrice() * tag.getCount();
		}
		
		return sum;
	}
	

	@Override
	public String toString() {
		return "code=" + code
				+ "\nmemberCode=" + memberCode
				+ "\naddress=" + address
				+ "\nstatus=" + status
				+ "\nitemInfo=" + itemInfo
				+ "\npayInfo=" + payInfo
				+ "\ntimeStamp=" + timeStamp
				+ "\norderMan="+ orderMan
				+ "\nreceiver=" + receiver;
	}
}
