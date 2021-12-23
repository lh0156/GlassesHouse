package com.team.project.member;

public class Member {

//한명의 멤버 정보를 담기 위한 클래스
	
	private long code;
	private String id;
	private String password;
	private String name;
	private String tel;
	private String birth;
	private String address;
	private Grade grade;

	public Member() {
		this(0, "", "", "", "", "", "", null);
	}
	
	public Member(long code, String id, String password, String name, String tel, String birth, String address, Grade grade) {
		super();
		this.code = code;
		this.id = id;
		this.password = password;
		this.name = name;
		this.tel = tel;
		this.birth = birth;
		this.address = address;
		this.grade = grade;
	}

	public long getCode() {
		return code;
	}

	public void setCode(long code) {
		this.code = code;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getBirth() {
		return birth;
	}

	public void setBirth(String birth) {
		this.birth = birth;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}

	@Override
	public String toString() {
		return "code=" + code
				+ "\nid=" + id
				+ "\npassword=" + password
				+ "\nname=" + name
				+ "\ntel=" + tel
				+ "\nbirth=" + birth
				+ "\naddress=" + address
				+ "\ngrade=" + grade;
	}
	
	public String memberJoinView() {
		
		/**
		 * 
		 */
		
		return "이름 : " + name
				+ "\n아이디 : " + id
				+ "\n패스워드 : " + password.replaceAll("[a-zA-Z0-9\"!@#$%\"]", "*")
				+ "\n생년월일 : " + birth
				+ "\n전화번호 : " + tel
				+ "\n주소 : " + address;
	}
}


