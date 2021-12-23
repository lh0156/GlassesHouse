package com.team.project.payment;

public class PayInfo {

	private PayOption payOption;
	private int price;
	private boolean status;
	private String num;
	
	public PayInfo() {
	}
	
	public PayInfo(PayOption payOption, int price, boolean status, String num) {
		this.payOption = payOption;
		this.price = price;
		this.status = status;
		this.num = num;
	}

	public PayOption getPayOption() {
		return payOption;
	}

	public void setPayOption(PayOption payOption) {
		this.payOption = payOption;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	@Override
	public String toString() {
		return "PayInfo [payOption=" + payOption + ", price=" + price + ", status=" + status + ", num=" + num + "]";
	}
}
