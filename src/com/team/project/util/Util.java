package com.team.project.util;

public class Util {

	/**
	 * 콘솔창에 프린트할 때 지정된 영역 내에 왼쪽 정렬로 프린트 할 수 있는 메서드입니다.
	 * 
	 * @param s 문자열
	 * @param space 출력공간
	 * @return 출력공간 길이의 가운데 정렬된 문자열
	 * 
	 * @author 조진욱
	 */
	public static String fixLeftString(String s, int space) {
		
		StringBuilder sb = new StringBuilder();
		int count = 0;
		
		for (int i = 0; i < s.length(); ++i) {
			
			if (space - count > 3) {
				
				if (s.charAt(i) >= '가' && s.charAt(i) <= '힇') {
					count += 2;
					sb.append(s.charAt(i));
				} else {
					count += 1;
					sb.append(s.charAt(i));
				}

			} else {
				for (int j = 0; j <= space - count + 1; ++j) {
					
					sb.append(".");
					count++;
				}
				break;
			}
		}
		
		if (space - count > 0) {
			for (int i = 0; i < space - count; ++i) {
				sb.append(" ");
			}
		}
		
		return sb.toString();
	}
	

	/**
	 * 콘솔창에 프린트할 때 지정된 영역 내에 가운데 정렬로 프린트 할 수 있는 메서드입니다.
	 * 
	 * @param s 문자열
	 * @param space 출력공간
	 * @return 출력공간 길이의 가운데 정렬된 문자열
	 * 
	 * @author 조진욱
	 */
	public static String fixCenterString(String s, int space) {
		
		StringBuilder sb = new StringBuilder();
		s = s.trim();
		int count = 0;
		
		for (int i = 0; i < s.length(); ++i) {
			
			if (space - count > 1) {
				
				if (s.charAt(i) >= '가' && s.charAt(i) <= '힇') {
					count += 2;
					sb.append(s.charAt(i));
				} else {
					count += 1;
					sb.append(s.charAt(i));
				}

			} else {
				
				break;
			}
		}
			
		for (int i = 0; i < (space - count) / 2; ++i) {
			
			sb.insert(0, " ");
			sb.append(" ");
		}
		
		if ((space - count) % 2 == 1) {
			sb.append(" ");
		}
		
		return sb.toString();
	}

	
	/**
	 * 콘솔창에 프린트할 때 지정된 영역 내에 오른쪽 정렬로 프린트 할 수 있는 메서드입니다.
	 * 
	 * @param s 문자열
	 * @param space 출력공간
	 * @return 출력공간 길이의 문자열
	 * 
	 * @author 조진욱
	 */
	public static String fixRightString(String s, int space) {
		
		StringBuilder sb = new StringBuilder();
		int count = 0;
		
		for (int i = 0; i < s.length(); ++i) {
			
			if (space - count > 3) {
				
				if (s.charAt(i) >= '가' && s.charAt(i) <= '힇') {
					count += 2;
					sb.append(s.charAt(i));
				} else {
					count += 1;
					sb.append(s.charAt(i));
				}

			} else {
				for (int j = 0; j <= space - count + 1; ++j) {
					
					sb.append(".");
					count++;
				}
				break;
			}
		}
		
		if (space - count > 0) {
			for (int i = 0; i < space - count; ++i) {
				sb.insert(0, " ");
			}
		}
		
		return sb.toString();
	}
}
