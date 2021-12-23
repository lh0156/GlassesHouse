package com.team.project.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.team.project.Path;
import com.team.project.item.Item;
import com.team.project.order.Order;
import com.team.project.order.OrderTimeStamp;

public class Test {

	public static Scanner scan = new Scanner(System.in);
	public static String sel = "";

	private static ArrayList<Item> itemRepository = new ArrayList<Item>();
	private static ArrayList<Order> orderRepository = new ArrayList<Order>();
	
	public static void main(String[] args) throws Exception {

//		m1();
		
//		mainScreen();
		
//		searchScreen();
		
		m2();
	}

	private static void m2() {
		
		String input = "조진욱";
		
		System.out.println(Pattern.matches("^[가-힣]{2,8}$", input));
	}
	
	private static boolean orderCodeValid(String input) {
		
		return Pattern.matches("^\\#[0-9]+$", input);
	}
	
	private static String fixString(String s, int limit) {
	
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < s.length(); ++i) {
			
			if (limit <= 1) {
				break;
			} else {
				
				if (s.charAt(i) >= '가' && s.charAt(i) <= '힇') {
					limit -= 2;
					sb.append(s.charAt(i));
				} else {
					limit -= 1;
					sb.append(s.charAt(i));
				}
			}
		}
		
		if (limit > 0) {
			for (int i = 0; i < limit; ++i) {
				sb.append(" ");
			}
		}
		
		
		return sb.toString();
	}

	private static void searchScreen() {

		ArrayList<String> jogan = new ArrayList<String>();
		String searchWord = "";
		
		
		while (true) {
			System.out.println();
			System.out.println();
			System.out.println("조건 : " + jogan.toString());
			System.out.println("검색어 : " + searchWord);
			
			System.out.println("1.검색조건 추가 2.검색어 입력 3.검색실행");
			System.out.print("입력 : ");
			sel = scan.nextLine();
			
			if (sel.equalsIgnoreCase("1")) {
				
				jogan = addJogan(jogan);
				
			} else if (sel.equalsIgnoreCase("2")) {

				searchWord = addSearchWord(searchWord);
				
			} else if (sel.equalsIgnoreCase("3")) {
				
			}
			
		}
		
	}
	
	
	private static ArrayList<String> addJogan(ArrayList<String> jogan) {
		
		ArrayList<String> result = jogan;
		
		System.out.println("현재 조건 목록 : " + jogan.toString());
		System.out.println("조건을 입력하세요.");
		System.out.print("입력 : ");
		String input = scan.nextLine();
		
		result.add(input);
		
		return result;
	}
	
	
	
	private static String addSearchWord(String searchWord) {
		
		System.out.println("이전 검색어 : " + searchWord);
		System.out.println("검색어를 입력하세요.");
		System.out.print("입력 : ");
		String input = scan.nextLine();
		
		return input;
	}
	

	private static void mainScreen() {


		while (!sel.equals("q")) {

			System.out.println("[메인화면]");
			System.out.println("1.전체목록 q.종료");
			System.out.print("입력 : ");
			sel = scan.nextLine();
			
			if (sel.equals("1")) {
				itemListScreen();
			}
		}
		
		
		quit();
	}

	private static void quit() {
		
		System.out.println("종료합니다.");
	}

	private static void itemListScreen() {

		while(!(sel.equals("q") || sel.equals("m"))) {
			System.out.println("\nsel = " + sel);
			
			System.out.println("[전체상품목록]");
			System.out.println("1.상품검색 u.이전단계 m.메인 q.종료");
			System.out.print("입력 : ");
			sel =scan.nextLine();

			if (sel.equals("1")) {
				
				totalSearchScreen();
				
			} else if (sel.equals("u")) {
				return;
			}
		}
		
		
	}

	private static void totalSearchScreen() {
		
		while(!(sel.equals("q") || sel.equals("m"))) {
			System.out.println("\nsel = " + sel);
			
			System.out.println("[상품검색]");
			System.out.println("u.이전단계 m.메인 q.종료");
			System.out.print("입력 : ");
			sel =scan.nextLine();

			if (sel.equals("1")) {
				
			} else if (sel.equals("u")) {
				return; //OOOOO
			}
		}
		
	}

	private static void m1() throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Path.ORDER_TIME_STAMP.getPath()));
		
		ArrayList<OrderTimeStamp> otStamps = new ArrayList<OrderTimeStamp>();
		
		String line = "";
		while ((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			String orderCode = temp[0];
			String orderTime = temp[1];
			String deliveryTime = temp[2];
			String purchaseConfirmTime = temp[3];
			String extraRequestTime = temp[4];
			String extraDoneTime = temp[5];
			
			
			ArrayList<Calendar> timeStamps = new ArrayList<Calendar>();
			
			for (int i = 1; i < 6; ++i) {
				
				if (!temp[i].equals("null")) {

					String[] t = temp[i].split("-");

					ArrayList<Integer> list= new ArrayList<Integer>();
					
					Calendar c = Calendar.getInstance();
					c.set(Integer.parseInt(t[0])
							, Integer.parseInt(t[1])
							, Integer.parseInt(t[2])
							, Integer.parseInt(t[3])
							, Integer.parseInt(t[4])
							);
					timeStamps.add(c);
				} else {
					timeStamps.add(null);
				}
			}
			
			OrderTimeStamp timeStamp = new OrderTimeStamp(
										timeStamps.get(0)
										,timeStamps.get(1)
										,timeStamps.get(2)
										,timeStamps.get(3)
										,timeStamps.get(4)
										);
			otStamps.add(timeStamp);
		}
		
		
		for (int i = 0; i < otStamps.size(); ++i) {

			OrderTimeStamp stamp = otStamps.get(i);
			
			System.out.print((i+1) + " : ");
			if (stamp.getExtraDoneTime() != null) { //취소완료 존재
				
				if (stamp.getExtraRequestTime() != null) { //취소신청 존재
					
					System.out.print((stamp.getExtraRequestTime().compareTo(stamp.getExtraDoneTime()) <= 0) + ", ");
					
					if (stamp.getDeliveryTime() != null) { //배송완료 존재

						System.out.print((stamp.getDeliveryTime().compareTo(stamp.getExtraRequestTime()) <= 0) + ", ");
						
						if (stamp.getOrderTime() != null) { //주문 존재
							System.out.print((stamp.getOrderTime().compareTo(stamp.getDeliveryTime()) <= 0) + ", ");
							System.out.print("교환/반품완료");
							
						} else { //주문 존재 x 불가
							System.out.print("error1");
						}
					} else { //배송완료 존재 x
						
						if (stamp.getOrderTime() != null) { //주문 존재
							System.out.print((stamp.getOrderTime().compareTo(stamp.getExtraRequestTime()) <= 0) + ", ");
							System.out.print("취소완료");
						} else { //주문 존재 x 불가
							System.out.print("error2");
						}
					}
				} else { //취소신청 존재 x 불가
					System.out.print("error3");
				}
					
			} else { //취소완료 존재 x
				
				if (stamp.getExtraRequestTime() != null) { //취소신청 존재

					if (stamp.getDeliveryTime() != null) { //배송완료 존재

						System.out.print((stamp.getDeliveryTime().compareTo(stamp.getExtraRequestTime()) <= 0) + ", ");
						
						if (stamp.getOrderTime() != null) { //주문 존재
							
							System.out.print((stamp.getOrderTime().compareTo(stamp.getDeliveryTime()) <= 0) + ", ");
							System.out.print("교환/반품신청");

						} else { //주문 존재 x 불가
							System.out.print("error4");
						}
					} else { //배송완료 존재 x
						
						if (stamp.getOrderTime() != null) { //주문 존재							
							System.out.print("취소신청");
						} else { //주문 존재 x 불가
							System.out.print("error5");
						}
					}
				} else { //취소신청 존재 x
					
					if (stamp.getPurchaseConfirmTime() != null) { //구매확정 존재
						
						if (stamp.getDeliveryTime() != null) { //배송완료 존재

							System.out.print((stamp.getDeliveryTime().compareTo(stamp.getPurchaseConfirmTime()) <= 0) + ", ");
							
							if (stamp.getOrderTime() != null) { //주문 존재
								
								System.out.print((stamp.getOrderTime().compareTo(stamp.getDeliveryTime()) <= 0) + ", ");
								System.out.print("구매확정");
								
							} else { //주문 존재 x 불가
								System.out.print("error6");
							}
						} else { //배송완료 존재 x 불가
							System.out.print("error7");
						}
					} else { //구매확정 존재 x
						if (stamp.getDeliveryTime() != null) { //배송완료 존재
							
							if (stamp.getOrderTime() != null) { //주문 존재

								System.out.print((stamp.getOrderTime().compareTo(stamp.getDeliveryTime()) <= 0) + ", ");
								System.out.print("배송완료");

							} else { //주문 존재 x 불가
								System.out.print("error8");
							}
						} else { //배송완료 존재 x
							
							if (stamp.getOrderTime() != null) { //주문 존재
								
								System.out.print("배송대기");
								
							} else { //주문 존재 x 불가
								System.out.print("error9");
							}
						}
					}
				}
			}
			
			System.out.println();
		}
	}
}
