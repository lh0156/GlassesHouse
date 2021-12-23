package com.team.project.test;

import java.util.Scanner;

public class test2 {
	
	private static String sel = "";
	private static Scanner scan = new Scanner(System.in);

	public static void main(String[] args) {

		mainScreen();
		
		System.out.println("종료");
	}

	private static void mainScreen() {
		
		while (!(sel.equalsIgnoreCase("q"))) {
		
			System.out.println();
			System.out.println("[메인화면]");
			System.out.println("내용");

			System.out.println("1.화면1 2.화면2");
			System.out.println("q.종료");
			System.out.print("입력 : ");
			sel = scan.nextLine();
			
			if (sel.equalsIgnoreCase("1")) {
		
				화면1();

			} else if (sel.equalsIgnoreCase("q")) {

				return;	//이 메서드를 종료하고 상위 화면으로 이동 or 메인으로 이동 or 프로그램종료
						
			} else {
					
				System.out.println("\n잘못된 입력입니다.\n");
			}
		}
	}

	private static void 화면1() {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			System.out.println();
			System.out.println("[구매]");
			System.out.println("로그인 안되어있는데 로그인 하십쇼");
			
			System.out.println("1.로그인 2.비회원주문");
			System.out.println("u.이전단계 m.메인 q.종료");
			System.out.println("input : ");
			sel = scan.nextLine();

			if (sel.equalsIgnoreCase("1")) {
		
				로그인();
				
				if (sel.equalsIgnoreCase("2")) {
					화면1_2();
				}
				
			} else if (sel.equalsIgnoreCase("2")) {

				화면1_2();

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;	//이 메서드를 종료하고 상위 화면으로 이동 or 메인으로 이동 or 프로그램종료
						
			} else {
					
				System.out.println("\n잘못된 입력입니다.\n");
			}
		}
	}
	
	private static void 화면1_2() {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println();
			System.out.println("[구매]");
			System.out.println("내용");
			
			System.out.println("1.choice 2.choice");
			System.out.println("u.이전단계 m.메인 q.종료");
			System.out.println("input : ");
			sel = scan.nextLine();

			
			if (sel.equalsIgnoreCase("1")) {
		
				System.out.println("login");
				

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;	//이 메서드를 종료하고 상위 화면으로 이동 or 메인으로 이동 or 프로그램종료
						
			} else {
					
				System.out.println("\n잘못된 입력입니다.\n");
			}
		}	
	}
	

	private static void 로그인() {

		boolean flag = false;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println();
			System.out.println("[로그인]");
			System.out.println("내용");
			
			System.out.println("1.login 2.join");
			System.out.println("u.이전단계 m.메인 q.종료");
			System.out.println("input : ");
			sel = scan.nextLine();

			
			if (sel.equalsIgnoreCase("1")) {
		
				System.out.println("login");
				flag = true;
				
				if (flag) {
					sel = "2";
					return;
				}

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;	//이 메서드를 종료하고 상위 화면으로 이동 or 메인으로 이동 or 프로그램종료
						
			} else {
					
				System.out.println("\n잘못된 입력입니다.\n");
			}
		}
		
	}


	
	
}
