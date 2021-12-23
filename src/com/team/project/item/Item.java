package com.team.project.item;

import java.util.ArrayList;

public class Item {

	private String code;
	private String brand;
	private String name;
	private int weight;
	private String size;
	private Shape shape;
	private ArrayList<Material> material;
	
	public Item() {
		this("", "", "", 0, "", null, null);
	}

	public Item(String code, String brand, String name, int weight, String size, Shape shape, ArrayList<Material> material) {
		this.code = code;
		this.brand = brand;
		this.name = name;
		this.weight = weight;
		this.size = size;
		this.shape = shape;
		this.material = material;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public ArrayList<Material> getMaterial() {
		return material;
	}

	public void setMaterial(ArrayList<Material> material) {
		this.material = material;
	}

	@Override
	public String toString() {
		return "code=" + code
				+ "\nbrand=" + brand
				+ "\nname=" + name
				+ "\nweight=" + weight
				+ "\nsize=" + size
				+ "\nshape=" + shape
				+ "\nmaterial=" + material;
	}
	
	
}
