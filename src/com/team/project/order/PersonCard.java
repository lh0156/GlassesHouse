package com.team.project.order;

public class PersonCard {

	String name;
	String tel;
	
	public PersonCard() {
		this("", "");
	}
	
	public PersonCard(String name, String tel) {
		this.name = name;
		this.tel = tel;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTel() {
		return tel;
	}
	
	public void setTel(String tel) {
		this.tel = tel;
	}

	@Override
	public String toString() {
		return "name=" + name
				+ "\ntel=" + tel;
	}
}
