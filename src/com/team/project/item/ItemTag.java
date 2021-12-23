package com.team.project.item;

public class ItemTag {

	private String itemCode;
	private int count;
	private int price;
	
	public ItemTag() {
		this("", 0, 0);
	}
	
	public ItemTag(String itemCode, int count, int price) {
		this.itemCode = itemCode;
		this.count = count;
		this.price = price;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "itemCode=" + itemCode
				+ "count=" + count
				+ "price=" + price + "\n";
	}
}
