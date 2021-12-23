package com.team.project.item;

public class SearchCondition {

	private String shapes;
	private String materials;
	private String brands;
	private int[] prices;
	private String[] sizes;
	private String word;
	
	public SearchCondition() {
		this.shapes = "";
		this.materials = "";
		this.brands = "";
		this.prices = new int[] {0, 3000000};
		this.sizes = new String[] {"30-10-130", "50-23-148"};
		this.word = "";
	}

	public String getShapes() {
		return shapes;
	}

	public void setShapes(String shapes) {
		this.shapes = shapes;
	}

	public String getMaterials() {
		return materials;
	}

	public void setMaterials(String materials) {
		this.materials = materials;
	}

	public String getBrands() {
		return brands;
	}

	public void setBrands(String brands) {
		this.brands = brands;
	}

	public int[] getPrices() {
		return prices;
	}

	public void setPrices(int[] prices) {
		this.prices = prices;
	}

	public String[] getSizes() {
		return sizes;
	}

	public void setSizes(String[] sizes) {
		this.sizes = sizes;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	@Override
	public String toString() {
		return "SearchCondition [shapes=" + shapes + ", materials=" + materials + ", brands=" + brands + ", prices="
				+ prices + ", sizes=" + sizes + ", word=" + word + "]";
	}
}
