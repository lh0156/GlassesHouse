package com.team.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import com.team.project.item.Item;
import com.team.project.item.ItemTag;
import com.team.project.item.Material;
import com.team.project.item.SearchCondition;
import com.team.project.item.Shape;
import com.team.project.member.Grade;
import com.team.project.member.Member;
import com.team.project.order.Order;
import com.team.project.order.OrderItemTag;
import com.team.project.order.OrderStatus;
import com.team.project.order.OrderTimeStamp;
import com.team.project.order.PersonCard;
import com.team.project.payment.PayInfo;
import com.team.project.payment.PayOption;

public class Application {

	public static ArrayList<Item> itemRepository;
	public static ArrayList<Member> memberRepository;
	public static ArrayList<Order> orderRepository;
	public static HashMap<String, ItemTag> itemTagRepository;
	public static Set<String> brandList;
	
	public static long loginMemberCode;
	public static ArrayList<ItemTag> basket;
	
	public static Scanner scan;
	public static String sel;

	public static final String VERTICAL = " ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- \n"; //167
	public static final String VERTICAL_B = " ======================================================================================================================================================================= \n";
	public static final String HORIZON = "|                                                                                                                                                                       |\n";
	public static int viewLineCount = 0;
	
	
	static {
		
		itemRepository = new ArrayList<Item>();
		memberRepository = new ArrayList<Member>();
		orderRepository = new ArrayList<Order>();
		itemTagRepository = new HashMap<String, ItemTag>();
		brandList = new HashSet<String>();
		
		loginMemberCode = -1;
		basket = new ArrayList<ItemTag>();
		scan = new Scanner(System.in);
		sel = "";
		
		try {
			
			loadItem();
			loadMember();
			loadOrder();
			loadItemTag();
			
		} catch(Exception e) {
			
			e.printStackTrace();
			System.out.println("[ERROR] 에러 꼭 찾고 넘어가세요.");
		}
	}
	
	
	public static void main(String[] args) {
		
		ItemTag itemTag = new ItemTag("8013AS C1", 1, 109000);
		basket.add(itemTag);
		itemTag = new ItemTag("OROSHI GRAPHITE", 1, 680000);
		basket.add(itemTag);
		

		while (!sel.equalsIgnoreCase("q")) {
			
			sel = "";
			
			if (loginMemberCode == -1) {
				//비회원 화면
				mainScreen();
				
			} else if (getMember(loginMemberCode).getGrade() == Grade.STANDARD) {
				//회원화면
				loginMainScreen();
				
			} else if (getMember(loginMemberCode).getGrade() == Grade.ADMIN) {
				//관리자화면
				adminMainScreen();
			}
		}
			
		quit();
	}
	
	
	//파일 불러오기
	
	/**
	 * item.dat 파일로부터 상품 정보를 불러오는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void loadItem() throws Exception {
	
		HashMap<String, String[]> itemMaterials = loadItemMaterial(Path.ITEM_MATERIAL.getPath());

		BufferedReader reader = new BufferedReader(new FileReader(Path.ITEM.getPath()));
		String line = "";
		while((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			Item item = new Item();
			item.setCode(temp[0]);
			item.setBrand(temp[1]);
			item.setName(temp[2]);
			item.setWeight(Integer.parseInt(temp[3]));
			item.setSize(temp[4]);
			item.setShape(Shape.valueOf(temp[5]));
			
			ArrayList<Material> materials = new ArrayList<Material>();
			
			String[] materialArray = itemMaterials.get(item.getCode());
			
			for (int i = 1; i < materialArray.length; ++i) {
				materials.add(Material.valueOf(materialArray[i]));
			}

			item.setMaterial(materials);
			
			itemRepository.add(item);
			brandList.add(item.getBrand());
		}
		reader.close();
	}


	/**
	 * itemMaterial.dat 파일로부터 상품 소재 정보를 불러오는 메서드입니다.
	 * 
	 * @param path 파일경로
	 * @return 상품 소재 정보
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static HashMap<String, String[]> loadItemMaterial (String path) throws Exception {
		
		HashMap<String, String[]> itemMaterials = new HashMap<String, String[]>();

		BufferedReader reader = new BufferedReader(new FileReader(Path.ITEM_MATERIAL.getPath()));
		String line = null;
		while((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			itemMaterials.put(temp[0], temp);
		}
		reader.close();

		return itemMaterials;
	}
	
	
	/**
	 * member.dat 파일로부터 회원 정보를 불러오는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void loadMember() throws Exception {
		
		BufferedReader reader = new BufferedReader(new FileReader(Path.MEMBER.getPath()));
		String line = null;
		while((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			Member member = new Member();
			member.setCode(Long.parseLong(temp[0]));
			member.setId(temp[1]);
			member.setPassword(temp[2]);
			member.setName(temp[3]);
			member.setTel(temp[4]);
			member.setBirth(temp[5]);
			member.setAddress(temp[6]);
			member.setGrade(Grade.valueOf(temp[7].toUpperCase()));
			
			memberRepository.add(member);
		}
		reader.close();
	}
	
	
	/**
	 * order.dat 파일로부터 주문 정보를 불러오는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void loadOrder() throws Exception {

		ArrayList<OrderItemTag> orderItemTags = loadOrderItemTag(Path.ORDER_ITEM_TAG.getPath());
		HashMap<Long, PayInfo> payInfos = new HashMap<Long, PayInfo>();
		payInfos.putAll(loadCardPayInfo(Path.CARD_PAY_INFO.getPath()));
		payInfos.putAll(loadBankbookPayInfo(Path.BANKBOOK_PAY_INFO.getPath()));
		HashMap<Long, OrderTimeStamp> orderTimeStamps = loadOrderTimeStamp(Path.ORDER_TIME_STAMP.getPath());
		HashMap<Long, PersonCard> orderMans = loadPersonCard(Path.ORDER_MAN.getPath());
		HashMap<Long, PersonCard> receivers = loadPersonCard(Path.RECEIVER.getPath());

		BufferedReader reader = new BufferedReader(new FileReader(Path.ORDER.getPath()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			Order order = new Order();
			order.setCode(Long.parseLong(temp[0]));
			order.setMemberCode(Long.parseLong(temp[1]));
			order.setAddress(temp[2]);
			order.setStatus(OrderStatus.valueOf(temp[3].toUpperCase()));
			
			ArrayList<OrderItemTag> itemInfo = new ArrayList<OrderItemTag>();
			
			for (OrderItemTag orderItemTag : orderItemTags) {
				if (order.getCode() == orderItemTag.getOrderCode()) {
					itemInfo.add(orderItemTag);
				}
			}
			
			order.setItemInfo(itemInfo);
			order.setPayInfo(payInfos.get(order.getCode()));
			order.setTimeStamp(orderTimeStamps.get(order.getCode()));
			order.setOrderMan(orderMans.get(order.getCode()));
			order.setReceiver(receivers.get(order.getCode()));
			
			orderRepository.add(order);
		}
		reader.close();
	}
	

	/**
	 * orderItemTag.dat 파일로부터 주문 물품 정보를 물러오는 메서드입니다.
	 * 
	 * @param path 파일경로
	 * @return 주문 물품 정보
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<OrderItemTag> loadOrderItemTag (String path) throws Exception {

		ArrayList<OrderItemTag> orderItemTags = new ArrayList<OrderItemTag>();

		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			OrderItemTag orderItemTag = new OrderItemTag();
			orderItemTag.setItemCode(temp[0]);
			orderItemTag.setCount(Integer.parseInt(temp[1]));
			orderItemTag.setPrice(Integer.parseInt(temp[2]));
			orderItemTag.setOrderCode(Long.parseLong(temp[3]));
			
			orderItemTags.add(orderItemTag);
		}
		reader.close();

		return orderItemTags;
	}
	
	
	/** 
	 * orderTimeStamp.dat 파일로부터 주문 시간 정보를 불러오는 메서드입니다.
	 * 
	 * @param path 파일경로
	 * @return 주문 시간 정보
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static HashMap<Long, OrderTimeStamp> loadOrderTimeStamp (String path) throws Exception {
		
		HashMap<Long, OrderTimeStamp> orderTimeStamps = new HashMap<Long, OrderTimeStamp>();

		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			ArrayList<Calendar> timeStamps = new ArrayList<Calendar>();
			
			String[] temp = line.split(",");
			
			for (int i = 1; i < temp.length; ++i) {
				
				if (!temp[i].equalsIgnoreCase("null")) {
					
					String[] dateNums = temp[i].split("-");
					Calendar calendar = Calendar.getInstance();
					calendar.set(Integer.parseInt(dateNums[0])
								, Integer.parseInt(dateNums[1]) - 1
								, Integer.parseInt(dateNums[2])
								, Integer.parseInt(dateNums[3])
								, Integer.parseInt(dateNums[4]));
					
					timeStamps.add(calendar);
				} else {
					timeStamps.add(null);
				}
			}
			
			OrderTimeStamp orderTimeStamp = new OrderTimeStamp();
			orderTimeStamp.setOrderTime(timeStamps.get(0));
			orderTimeStamp.setDeliveryTime(timeStamps.get(1));
			orderTimeStamp.setPurchaseConfirmTime(timeStamps.get(2));
			orderTimeStamp.setExtraRequestTime(timeStamps.get(3));
			orderTimeStamp.setExtraDoneTime(timeStamps.get(4));
			
			orderTimeStamps.put(Long.parseLong(temp[0]), orderTimeStamp);
		}
		reader.close();

		return orderTimeStamps;
	}
	
	
	/**
	 * orderMan.dat, receiver.dat 파일로부터 주문자, 수령인 정보를 불러오는 메서드입니다.
	 * 
	 * @param path 파일경로
	 * @return 주문자, 수령인 정보
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static HashMap<Long, PersonCard> loadPersonCard (String path) throws Exception {
		
		HashMap<Long, PersonCard> personCards = new HashMap<Long,PersonCard>();
		
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			PersonCard personCard = new PersonCard();
			personCard.setName(temp[1]);
			personCard.setTel(temp[2]);
			
			personCards.put(Long.parseLong(temp[0]), personCard);
		}
		reader.close();

		return personCards;
	}
	
	
	/**
	 * cardPayInfo.dat 파일로부터 카드 결제 정보를 불러오는 메서드입니다.
	 * 
	 * @param path
	 * @return 카드 결제 정보
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static HashMap<Long, PayInfo> loadCardPayInfo(String path) throws Exception {
		
		HashMap<Long, PayInfo> payInfos = new HashMap<Long, PayInfo>();
		
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			PayInfo payInfo = new PayInfo();
			payInfo.setPayOption(PayOption.valueOf(temp[1]));
			payInfo.setPrice(Integer.parseInt(temp[2]));
			payInfo.setStatus(Boolean.parseBoolean(temp[3]));
			payInfo.setNum(temp[4]);
			
			payInfos.put(Long.parseLong(temp[0]), payInfo);
		}
		reader.close();

		return payInfos;
	}

	
	/**
	 * bankbookPayInfo.dat 파일로부터 무통장 결제 정보를 불러오는 메서드입니다.
	 * 
	 * @param path
	 * @return 무통장 결제 정보
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static HashMap<Long, PayInfo> loadBankbookPayInfo(String path) throws Exception {
		
		HashMap<Long, PayInfo> payInfos = new HashMap<Long, PayInfo>();
		
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			PayInfo payInfo = new PayInfo();
			payInfo.setPayOption(PayOption.valueOf(temp[1]));
			payInfo.setPrice(Integer.parseInt(temp[2]));
			payInfo.setStatus(Boolean.parseBoolean(temp[3]));
			payInfo.setNum(temp[4]);
			
			payInfos.put(Long.parseLong(temp[0]), payInfo);
		}
		reader.close();

		return payInfos;
	}

	
	/**
	 * itemTag.dat 파일로부터 상품 재고 정보를 불러오는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void loadItemTag() throws Exception {
		
		BufferedReader reader = new BufferedReader(new FileReader(Path.ITEM_TAG.getPath()));
		String line = null;
		while((line = reader.readLine()) != null) {
			
			String[] temp = line.split(",");
			
			ItemTag itemTag = new ItemTag();
			itemTag.setItemCode(temp[0]);
			itemTag.setCount(Integer.parseInt(temp[1]));
			itemTag.setPrice(Integer.parseInt(temp[2]));
			
			itemTagRepository.put(itemTag.getItemCode(), itemTag);
		}
		reader.close();
	}
	
	

	
	//공통view
	
	/**
	 * 메인화면에 로고를 띄워주는 View 메서드입니다.
	 * 
	 * @author 조진욱
	 */
	private static void logoView() {
		
		
		String string = "      _       __          __                                            \n"
						+ "     | |     / /  ___    / /  _____  ____    ____ ___   ___            \n"
						+ "     | | /| / /  / _ \\  / /  / ___/ / __ \\  / __ `__ \\ / _ \\      \n"
						+ "     | |/ |/ /  /  __/ / /  / /__  / /_/ / / / / / / //  __/ _       \n"
						+ "     |__/|__/   \\___/ /_/   \\___/  \\____/ /_/ /_/ /_/ \\___/ ( )  ";
		String string1 = "         ______    __                                                 __  __                             \n"
				+ "        / ____/   / /  ____ _   _____   _____  ___    _____          / / / /  ____   __  __   _____  ___ \n"
				+ "       / / __    / /  / __ `/  / ___/  / ___/ / _ \\  / ___/         / /_/ /  / __ \\ / / / /  / ___/ / _ \\\n"
				+ "      / /_/ /   / /  / /_/ /  (__  )  (__  ) /  __/ (__  )         / __  /  / /_/ // /_/ /  (__  ) /  __/ _  \n"
				+ "      \\____/   /_/   \\__,_/  /____/  /____/  \\___/ /____/         /_/ /_/   \\____/ \\__,_/  /____/  \\___/ ( )";
		
	    System.out.println(string);     
	    System.out.println(string1);     
		System.out.println("\n\n\n");
		System.out.println( "                                                                                                   #@@@                                                \n"
							+ "                                                                                                     @@@&                                                 \n"                    
							+ "                                                                                                      %@@@@@@@@@@@*                   ,@@@@@@@@@@@&        \n"                   
							+ "                                                                                                   @@@@@@/    .%@@@@@#             *@@@@@%.    ,&@@@@@.    \n"
							+ "                                                                                                 @@@@(             @@@@*         .@@@@             .@@@@   \n"
							+ "                                                                                                @@@@                ,@@@/        @@@(                &@@@  \n"
							+ "                                                                                               ,@@@                  %@@@  %@@@@@@@@                  @@@# \n"
							+ "                                                                                               ,@@@                  &@@@  %@@@@@@@@                  @@@( \n"
							+ "                                                                                                @@@@                *@@@,        @@@%                @@@@  \n"
							+ "                                                                                                 %@@@@            .@@@@.          @@@@/            %@@@@   \n"
							+ "                                                                                                   &@@@@@&/,.,#@@@@@@,              @@@@@@%*,,/%@@@@@@     \n"
							+ "                                                                                                      ,@@@@@@@@@@%                     (@@@@@@@@@@*        \n"
							);
		
		
		
	    
		viewLineCount += 32;
	}
	
	
	/**
	 * 꾸며진 화면 제목을 띄워주는 메서드입니다.
	 * 
	 * @param title 화면제목
	 * 
	 * @author 조진욱
	 */
	private static void titleView(String title) {
		
		StringBuilder sb = new StringBuilder();

		sb.append("\n\n\n\n\n");
		sb.append(VERTICAL);
		sb.append("| " + fixLeftString("[" + title + "]", 100) + fixRightString("u.이전단계 m.메인 q.종료", 65) + " |\n");
		sb.append(VERTICAL_B);
		
		viewLineCount += 3;
		System.out.println(sb.toString());
	}

	
	/**
	 * 제목줄 바로 아래 메세지를 띄워주는 View 메서드입니다.
	 * 
	 * @param message 출력메세지
	 * 
	 * @author 조진욱
	 */
	private static void topMessageView(String message) {
		
		System.out.println(" " + message);
		
		viewLineCount += 4;
	}


	/**
	 * 화면 상하 간격을 맞춰주는 View 메서드입니다.
	 * 
	 * @author 조진욱
	 */
	private static void fixNewLineView() {
		
		StringBuilder sb = new StringBuilder();
		
		int fixNum = 33 - viewLineCount;
		
		for (int i = 0; i < fixNum; ++i) {
			sb.append("\n");
		}
		
		System.out.println(sb.toString());
		
		viewLineCount = 0;
	}


	/**
	 * 상하 간격을 맞춘 이후 메세지를 띄워주는 View 메서드입니다.
	 * 
	 * @param message 출력메세지
	 */
	private static void messageView(String message) {
		
		System.out.println(" " + message);
	}
	
	
	/**
	 * 선택지를 출력하는 View 메서드입니다.
	 * 
	 * @param choice 선택지
	 */
	private static void selectView(String choice) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n");
		sb.append(" " + fixLeftString(choice, 138) + "\n");
		sb.append(VERTICAL);
		
		System.out.println(sb.toString());
	}
	
	
	
	
	//범용 리스트 view
	
	/**
	 * 상품 컬렉션과 페이지를 입력하면 출력하는 View 메서드입니다.
	 * 마지막 페이지가 아니면 true 마지막 페이지면 false를 반환합니다.
	 * 
	 * @param items 상품 컬렉션
	 * @param page 페이지 번호
	 * @return 마지막 페이지 여부
	 * 
	 * @author 조진욱
	 */
	private static boolean itemListView(ArrayList<Item> items, int page) {
		
		StringBuilder sb = new StringBuilder();
		int startIndex = (page - 1) * 20;
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s | %s | %s |\n"
								, "번호"
								, fixCenterString("상품코드", 20)
								, fixCenterString("브랜드", 16)
								, fixCenterString("상품명", 89)
								, fixCenterString("가격", 10)
								, fixCenterString("규격", 11)));
		sb.append(VERTICAL);
		
		for (int i = startIndex; i < startIndex + 20; ++i) {
			
			try {

				sb.append(String.format("| %s | %s | %-16s | %s | %,10d | %s |\n"
						, fixCenterString("" + (i + 1), 4)
						, fixLeftString(items.get(i).getCode(), 20)
						, fixLeftString(items.get(i).getBrand(), 16)
						, fixLeftString(items.get(i).getName(), 89)
						, itemTagRepository.get(items.get(i).getCode()).getPrice()
						, fixCenterString(items.get(i).getSize(), 11)));
				viewLineCount++;
				
			} catch (IndexOutOfBoundsException e) {
				sb.append(VERTICAL);
				viewLineCount++;
				System.out.println(sb.toString());
				pagingView(items.size(), page);
				return false;
			}
		}
		sb.append(VERTICAL);
		
		viewLineCount += 6;
		System.out.println(sb.toString());
		pagingView(items.size(), page);

		if (startIndex + 20 == items.size()) {
			return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * 회원 컬렉션과 페이지를 입력하면 출력하는 View 메서드입니다.
	 * 마지막 페이지가 아니면 true 마지막 페이지면 false를 반환합니다.
	 * 
	 * @param member 상품 컬렉션
	 * @param page 페이지 번호
	 * @return 마지막 페이지 여부
	 * 
	 * @author 조진욱
	 */
	private static boolean memberListView(ArrayList<Member> member, int page) {
		
		StringBuilder sb = new StringBuilder();
		int startIndex = (page - 1) * 20;
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s | %s | %s |\n"
								, "번호"
								, fixCenterString("회원코드", 20)
								, fixCenterString("아이디", 20)
								, fixCenterString("이름", 66)
								, fixCenterString("전화번호", 20)
								, fixCenterString("주소", 20)));
		sb.append(VERTICAL);
		
		for (int i = startIndex; i < startIndex + 20; ++i) {
			
			try {

				sb.append(String.format("| %s | 16%s | %-16s | %s | %,10d | %s |\n"
						, fixCenterString("" + (i + 1), 4)
						, member.get(i).getCode()
						, fixLeftString(member.get(i).getId(), 16)
						, fixLeftString(member.get(i).getName(), 89)
						, fixLeftString(member.get(i).getTel(), 89)
						, fixCenterString(member.get(i).getAddress(), 11)));
				viewLineCount++;
				
			} catch (IndexOutOfBoundsException e) {
				sb.append(VERTICAL);
				viewLineCount++;
				System.out.println(sb.toString());
				pagingView(member.size(), page);
				return false;
			}
		}
		sb.append(VERTICAL);
		
		viewLineCount += 6;
		System.out.println(sb.toString());
		pagingView(member.size(), page);

		if (startIndex + 20 == member.size()) {
			return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * 주문 컬렉션과 페이지를 입력하면 출력하는 View 메서드입니다.
	 * 마지막 페이지가 아니면 true 마지막 페이지면 false를 반환합니다.
	 * 
	 * @param items 상품 컬렉션
	 * @param page 페이지 번호
	 * @return 마지막 페이지 여부
	 * 
 	 * @author 조진욱
	 */
	private static boolean orderListView(ArrayList<Order> orders, int page) {
		
		StringBuilder sb = new StringBuilder();
		int startIndex = (page - 1) * 20;
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s | %s | %s | %s | %s | %s |\n"
								, "번호"
								, "주문번호"
								, fixCenterString("상품코드", 20)
								, fixCenterString("브랜드", 16)
								, fixCenterString("상품명", 59)
								, "수량"
								, fixCenterString("가격", 10)
								, fixCenterString("주문일자", 10)
								, fixCenterString(("진행상태"), 10)));
		
		sb.append(VERTICAL_B);
		
		for (int i = startIndex; i < startIndex + 20; ++i) {

			ArrayList<Item> items = new ArrayList<Item>();
			
			try {
				
				for (int j = 0; j < orders.get(i).getItemInfo().size(); ++j) {
					
					items.add(getItem(orders.get(i).getItemInfo().get(j).getItemCode()));
				}
			
				sb.append(String.format("| %s | %08d | %s | %-16s | %s | %4d | %,10d | %tF | %s |\n"
						, fixCenterString("" + (i + 1), 4)
						, orders.get(i).getCode()
						, fixLeftString(items.get(0).getCode(), 20)
						, fixLeftString(items.get(0).getBrand(), 16)
						, fixLeftString(items.get(0).getName(), 59)
						, orders.get(i).getItemInfo().get(0).getCount()
						, orders.get(i).getItemInfo().get(0).getPrice()
						, orders.get(i).getTimeStamp().getOrderTime()
						, fixCenterString(orders.get(i).getStatus().getString(), 10)));
				viewLineCount++;
				
			} catch (IndexOutOfBoundsException e) {
				sb.append("\n" + fixCenterString("마지막 페이지입니다.", 120) + "\n");
				sb.append(VERTICAL);
				viewLineCount += 2;
				System.out.println(sb.toString());
				pagingView(orders.size(), page);
				return false;
			}			
		}
		sb.append(VERTICAL);
		
		viewLineCount += 4;
		System.out.println(sb.toString());
		pagingView(orders.size(), page);
		
		if (startIndex + 20 == orders.size()) {
			return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * 브랜드 목록을 출력하는 View 메서드입니다.
	 * 
	 * @param brands 브랜드리스트
	 * 
	 * @author 조진욱
	 */
	private static void brandListView(ArrayList<String> brands) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s |\n"
				, fixLeftString(" ", 39)
				, fixLeftString(" ", 39)
				, fixLeftString(" ", 39)
				, fixLeftString(" ", 39)));
		for (int i = 0; i < brands.size(); ++i) {
			
			sb.append("| " + fixCenterString("  "+ (i + 1), 4) + " : " + fixLeftString(brands.get(i), 33));
			
			if (i + 1 == brands.size()) {
				sb.append("| " + fixLeftString("", 39) + " | \n");
			}
			
			if (i % 4 == 3) {
				sb.append("|\n");
			}
		}
		sb.append(String.format("| %s | %s | %s | %s |\n"
				, fixLeftString(" ", 39)
				, fixLeftString(" ", 39)
				, fixLeftString(" ", 39)
				, fixLeftString(" ", 39)));
		sb.append(VERTICAL); 
		
		viewLineCount += 21;
		System.out.println(sb.toString());
	}
	
	
	/**
	 * 리스트 View 아래 페이지를 출력하는 View 메서드입니다.
	 * 
	 * @param count 컬렉션 사이즈
	 * @param page 출력 페이지
	 * 
	 * @author 조진욱
	 */
	private static void pagingView(int count, int page) {
		
		StringBuilder sb = new StringBuilder();

		int lastPage = (int)Math.ceil(count / 20.0);

		sb.append(String.format("%s%s%s%s%s[%s]%s%s%s%s%s"
									, page > 3 ? " << " : "  "
									, page + 1 > lastPage ? "  " + (page - 4) + "  " : ""
									, page + 2 > lastPage ? "  " + (page - 3) + "  " : ""
									, page - 2 <= 0 ? "" : "  " + (page - 2) + "  "
									, page - 1 <= 0 ? "" : "  " + (page - 1) + "  "
									, " " + page + " "
									, page + 1 > lastPage ? "" : "  " + (page + 1) + "  "
									, page + 2 > lastPage ? "" : "  " + (page + 2) + "  "
									, page - 2 <= 0 ? "  " + (page + 3) + "  " : ""
									, page - 1 <= 0 ? "  " + (page + 4) + "  " : ""
									, page != lastPage ? " >> " : "  "));

		System.out.println(fixCenterString(sb.toString(), 167));
	}
	
	
	
	
	//범용 상세 View
	
	
	/**
	 * 
	 * 상품의 상세 정보를 출력해주는 View 메서드입니다.
	 * 
	 * @param item
	 * 
	 * @author 엄윤섭
	 */
	private static void itemDetailView(Item item) {
		String mate = "";
		for(int i = 0; i < item.getMaterial().size(); i ++) {
			if(i == 0) {
				mate += item.getMaterial().get(i).toString();				
			}else {
				mate += ", " + item.getMaterial().get(i).toString();
			}
		}
		
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(VERTICAL);
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append("| " + fixLeftString("   상품코드", 20) + ":    " + fixLeftString(item.getCode(), 141) + "|\n");
		sb.append("| " + fixLeftString("   브랜드", 20) + ":    " + fixLeftString(item.getBrand(), 141) + "|\n");
		sb.append("| " + fixLeftString("   상품명", 20) + ":    " + fixLeftString(item.getName(), 141) + "|\n");
		sb.append("| " + fixLeftString("   무게", 20) + ":    " + fixLeftString(item.getWeight()+"g", 141) + "|\n");
		sb.append("| " + fixLeftString("   소재", 20) + ":    " + fixLeftString(mate, 141) + "|\n");
		sb.append("| " + fixLeftString("   안경규격", 20) + ":    " + fixLeftString(item.getSize(), 141) + "|\n");
		sb.append("| " + fixLeftString("   가격", 20) + ":    " + fixLeftString(String.format("%,d",itemTagRepository.get(item.getCode()).getPrice())+"원", 141) + "|\n");
		sb.append("| " + fixLeftString("   수량", 20) + ":    " + fixLeftString(itemTagRepository.get(item.getCode()).getCount()+"개", 141) + "|\n");
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append(VERTICAL);
		
		viewLineCount += 13;
		System.out.println(sb.toString());
		
	}

	
	/**
	 * 입력된 회원의 상세 정보를 출력하는 View 메서드입니다.
	 * 
	 * @param member 회원객체
	 * 
	 * @author 조진욱
	 */
	private static void memberDetailView (Member member) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n\n");
		
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                              정보수정                              |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           아 이 디   :   " + fixLeftString(member.getId(), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           비밀번호   :   " + fixLeftString(member.getPassword().replaceAll("[a-zA-Z0-9!@#$%]", "*"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           이    름   :   " + fixLeftString(member.getName(), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           생년월일   :   " + fixLeftString(member.getBirth(), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           전화번호   :   " + fixLeftString(member.getTel(), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           주    소   :   " + fixLeftString(member.getAddress(), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		
		viewLineCount += 23;
		System.out.println(sb.toString());
	}
	

	/**
	 * 입력된 주문의 상세 정보를 출력하는 View 메서드입니다.
	 * 
	 * @param order 주문 객체
	 * 
	 * @author 조진욱
	 */
	private static void orderDetailView(Order order) {
		
		StringBuilder sb = new StringBuilder();
		int sum = 0;
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s | %s | %s |\n"
								, "번호"
								, fixCenterString("상품코드", 20)
								, fixCenterString("브랜드", 16)
								, fixCenterString("상품명", 89)
								, fixCenterString("수량", 6)
								, fixCenterString("가격", 15)));
		sb.append(VERTICAL);
		
		for (int i = 0; i < order.getItemInfo().size(); ++i) {
			
			sum += order.getItemInfo().get(i).getPrice();
			
			sb.append(String.format("| %s | %s | %s | %s | %,6d | %,15d |\n"
								, fixCenterString(i + 1 + "", 4)
								, fixLeftString(order.getItemInfo().get(i).getItemCode(), 20)
								, fixLeftString(getItem(order.getItemInfo().get(i).getItemCode()).getBrand(), 16)
								, fixLeftString(getItem(order.getItemInfo().get(i).getItemCode()).getName(), 89)
								, order.getItemInfo().get(i).getCount()
								, order.getItemInfo().get(i).getPrice()));
			viewLineCount++;
		}
		
		String address = order.getAddress();
		String[] temp = address.split(" ");
		String elseAddress = (temp.length > 2) ? order.getAddress().substring(address.indexOf(temp[2]) + 1) : "";
		
		sb.append(VERTICAL);
		sb.append(String.format("%s : %,13d 원 \n\n", fixRightString("총 가격", 149), sum));
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s |\n"
								, fixLeftString("주문자", 46)
								, fixLeftString("수령인", 46)
								, fixLeftString("배송주소", 67)));
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s |\n"
								, fixLeftString("이름       :   " + order.getOrderMan().getName(), 46)
								, fixLeftString("이름       :   " + order.getReceiver().getName(), 46)
								, temp.length > 1 ? fixLeftString(temp[0] + " " + temp[1], 67) : fixLeftString("",67)));
		
		sb.append(String.format("| %s | %s | %s |\n"
								, fixLeftString("연락처     :   " + order.getOrderMan().getTel(), 46)
								, fixLeftString("연락처     :   " + order.getReceiver().getTel(), 46)
								, fixLeftString(elseAddress, 67)));
		sb.append(VERTICAL);
		sb.append("\n");
		
		
		PayInfo payInfo = order.getPayInfo();
		String num = "";
		if(order.getPayInfo() == null) {
			num = "결제정보 입력 대기중";
		}else {
			num = payInfo.getPayOption() == PayOption.CARD ? payInfo.getNum().substring(0, payInfo.getNum().lastIndexOf("-") + 1) + "****" : payInfo.getNum();
		}
		
		
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s |\n"
								, fixLeftString("결제정보   :   " + order.getPayInfo() == null ? order.getPayInfo().getPayOption().getString() : ""  + " (" + num + ")", 95)
								, fixLeftString("진행상태   :   " + order.getStatus().getString(), 67)));
		sb.append(VERTICAL);
		
		viewLineCount += 17;
		System.out.println(sb.toString());
	}
	
	


	//정렬
	
	private static String selectItemCategory(String input) {
		String category = "";
		switch(input){
		case "1":
			category =  "name";
		case "2":
			category = "code";
		case "3":
			category =  "brand";
		case "4":
			category =  "weight";
		case "5":
			category =  "size";
		}
		return category;
	}
	
	
	private static String selectOrderCategory(String input) {
		String category = "";
		switch(input){
		case "1":
			category =  "code";
		case "2":
			category = "memberCode";
		case "3":
			category =  "address";
		case "4":
			category =  "status";
		}
		return category;
	}


	/**
	 * 리스트와 카테고리를 받아 리스트를 카테고리별로 저장시킨뒤 리턴해주는 메서드입니다.
	 * 
	 * @param order 정렬 대상
	 * @param category 정렬 기준
	 * @param send 오름차순, 내림차순(1,-1)
	 * @return 정렬이 끝난 리스트
	 * @author 이찬우
	 */
	private static ArrayList<Order> orderSort(ArrayList<Order> order, String category, int send) {
		
		ArrayList<Order> orders = new ArrayList<Order>();
		
		if(send == 1) {
			switch(category) {
				case "code":
					order.stream().sorted((o1, o2) -> (int)(o1.getCode() - o2.getCode())).forEach((o) -> orders.add(o)); 
					return orders;
				case "memberCode":
					order.stream().sorted((o1,o2) -> (int)(o1.getMemberCode() - o2.getMemberCode())).forEach((o) -> orders.add(o));
					return orders;
				case "address":
					order.stream().sorted((o1,o2)->(o1.getAddress().compareTo(o2.getAddress()))).forEach((o)-> orders.add(o));
					return orders;
				case "status":
					order.stream().sorted((o1,o2)->(o1.getStatus().toString().compareTo(o2.getStatus().toString()))).forEach((o)->orders.add(o));
					return orders;
			}
		}else {
			switch(category) {
				case "code":
					order.stream().sorted((o1, o2) -> (int)(o2.getCode() - o1.getCode())).forEach((o) -> orders.add(o)); 
					return orders;
				case "memberCode":
					order.stream().sorted((o1,o2) -> (int)(o2.getMemberCode() - o1.getMemberCode())).forEach((o) -> orders.add(o));
					return orders;
				case "address":
					order.stream().sorted((o1,o2)->(o2.getAddress().compareTo(o1.getAddress()))).forEach((o)-> orders.add(o));
					return orders;
				case "status":
					order.stream().sorted((o1,o2)->(o2.getStatus().toString().compareTo(o1.getStatus().toString()))).forEach((o)->orders.add(o));
					return orders;
			}
		}
		return orders;
	}
	

	/**
	 * 리스트와 카테고리를 받아 리스트를 카테고리별로 저장시킨뒤 리턴해주는 메서드입니다.
	 * 
	 * @param item 정렬 대상
	 * @param category 정렬 기준
	 * @param send 오름차순, 내림차순(1,-1)
	 * @return 정렬이 끝난 리스트
	 * @author 이찬우
	 */
	private static ArrayList<Item> itemSort(ArrayList<Item> item, String category, int send) {
		ArrayList<Item> items = new ArrayList<Item>();
		if(send == 1) {
			switch(category) {
				case "name":
					item.stream().sorted((o1,o2)->(o1.getName().compareTo(o2.getName()))).forEach((o) -> items.add(o)); 
					return items;
				case "code":
					item.stream().sorted((o1,o2)->(o1.getCode().compareTo(o2.getCode()))).forEach((o)->items.add(o));
					return items;
				case "brand":
					item.stream().sorted((o1,o2)->(o1.getBrand().compareTo(o2.getBrand()))).forEach((o)->items.add(o));
					return items;
				case "weight":
					item.stream().sorted((o1,o2)->(o1.getWeight() - o2.getWeight())).forEach((o)->items.add(o));
					return items;
				case "size":
					item.stream().sorted((o1,o2)->(o1.getSize().compareTo(o2.getSize()))).forEach((o)->items.add(o));	
					return items;
			}
		}else {
			switch(category) {
				case "name":
					item.stream().sorted((o1,o2)->(o2.getName().compareTo(o1.getName()))).forEach((o)->items.add(o)); 
					return items;
				case "code":
					item.stream().sorted((o1,o2)->(o2.getCode().compareTo(o1.getCode()))).forEach((o)->items.add(o));
					return items;
				case "brand":
					item.stream().sorted((o1,o2)->(o2.getBrand().compareTo(o1.getBrand()))).forEach((o)->items.add(o));
					return items;
				case "weight":
					item.stream().sorted((o1,o2)->(o2.getWeight() - o1.getWeight())).forEach((o)->items.add(o));
					return items;
				case "size":
					item.stream().sorted((o1,o2)->(o2.getSize().compareTo(o1.getSize()))).forEach((o)->items.add(o));
					return items;
			}
			
		}
		return items;
	}
	
	
	
	
	//상품객체관련
	
	/**
	 * 상품코드를 입력하면 상품객체를 반환해주는 메서드입니다. 없으면 null을 반환합니다.
	 * 
	 * @param itemCode
	 * @return
	 */
	private static Item getItem(String itemCode) {
		
		for (Item item : itemRepository) {
			if (item.getCode().equals(itemCode)) {
				return item;
			}
		}
		
		return null;
	}


	/**
	 * 입력된 쉐입의 상품 리스트를 반환하는 메서드입니다.
	 * 
	 * @param shape 상품 쉐입
	 * @return 해당 상품 리스트
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<Item> getCategoryItems(Shape shape) {
		
		ArrayList<Item> items = new ArrayList<Item>();
		
		for (Item item : itemRepository) {
			if (item.getShape() == shape) {
				items.add(item);
			}
		}
		
		return items;
	}

	
	/**
	 * 입력된 소재의 상품 리스트를 반환하는 메서드입니다.
	 * 
	 * @param material 상품 소재
	 * @return 해당 상품 리스트
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<Item> getCategoryItems(Material material) {
		
		ArrayList<Item> items = new ArrayList<Item>();
		
		for (Item item : itemRepository) {
			for (Material m : item.getMaterial()) {
				if (m == material) {
					items.add(item);
				}
			}
		}
		
		return items;
	}

	
	/**
	 * 입력된 브랜드의 상품 리스트를 반환하는 메서드입니다.
	 * 
	 * @param brand 상품 브랜드
	 * @return 해당 상품 리스트
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<Item> getCategoryItems(String brand) {
		
		ArrayList<Item> items = new ArrayList<Item>();
		
		for (Item item : itemRepository) {
			if (item.getBrand().equalsIgnoreCase(brand)) {
				items.add(item);
			}
		}
		
		return items;
	}

	
	

	//회원객체관련
	
	/**
	 * 회원코드를 입력하면 회원객체를 반환해주는 메서드입니다. 없으면 null을 반환합니다.
	 * 
	 * @param memberCode 회원코드
	 * @return 회원객체
	 * 
	 * @author 조진욱
	 */
	private static Member getMember(long memberCode) {
		
		for (Member member : memberRepository) {
			if (member.getCode() == memberCode) {
				return member;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 아이디를 입력하면 회원객체를 반환해주는 메서드입니다. 없으면 null을 반환합니다.
	 * 
	 * @param memberId 아이디
	 * @return 회원객체
	 * 
	 * @author 조진욱
	 */
	private static Member getMember(String memberId) {
		
		for (Member member : memberRepository) {
			if (member.getId().equalsIgnoreCase(memberId)) {
				return member;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 전화번호를 입력하면 회원객체를 반환해주는 메서드입니다. 없으면 null을 반환합니다.
	 * 없으면 
	 * 
	 * @param Tel 전화번호
	 * @return 회원객체
	 * 
	 * @author 조진욱
	 */
	private static Member getMemberByTel(String Tel) {
		
		for (Member member : memberRepository) {
			if (member.getTel().equalsIgnoreCase(Tel)) {
				return member;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 이름을 입력하면 해당하는 회원객체 컬렉션을 반환해주는 메서드입니다. 없으면 null을 반환합니다.
	 * 
	 * @param name 이름
	 * @return 회원객체
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<Member> getMembersByName(String name) {
		
		ArrayList<Member> members = new ArrayList<Member>();
		
		for (Member member : memberRepository) {
			if (member.getName().equalsIgnoreCase(name)) {
				members.add(member);
			}
		}
		
		return members.isEmpty() ? null : members;
	}
	
	
	/**
	 * memberRepository 내의 회원객체를 수정된 회원로 대체하는 메서드입니다. 
	 * 
	 * @param member
	 */
	private static void setMember (Member member) {
		
		int count = 0;
		for (Member m : memberRepository) {
			
			if (m.getCode() == member.getCode()) {
				memberRepository.set(count, member);
				count++;
			}
		}
	}
	
	
	
	
	//주문객체관련
	
	/**
	 * 입력된 주문코드의 주문 객체를 반환하는 메서드입니다. 없으면 null을 반환합니다.
	 * 
	 * @param orderCode 주문코드
	 * @return 주문 객체
	 * 
	 * @author 조진욱
	 */
	private static Order getOrder(long orderCode) {
		
		for (Order order : orderRepository) {
			if (order.getCode() == orderCode) {
				return order;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 입력된 회원코드의 주문 리스트를 반환하는 메서드입니다.
	 * 
	 * @param memberCode
	 * @return
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<Order> getOrders(long memberCode) {
		
		ArrayList<Order> orders = new ArrayList<Order>();
		
		for (Order order : orderRepository) {
			
			if (order.getMemberCode() == memberCode) {
				orders.add(order);
			}
		}
		
		return orders;
	}

	
	/**
	 * 로그인 된 회원의 주문 리스트를 반환하는 메서드입니다. 없으면 null을 반환합니다.
	 * 
	 * @return 로그인 회원 주문 리스트
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<Order> getMyOrder() {
		
		ArrayList<Order> orders = new ArrayList<Order>();
		
		for (Order order : orderRepository) {
			if (order.getCode() == loginMemberCode) {
				orders.add(order);
			}
		}
		
		return orders;
	}

	
	/**
	 * orderRepository 내의 주문객체 수정된 주문객체로 대체하는 메서드입니다. 
	 * 
	 * @param order 수정된 주문객체
	 * 
	 * @author 조진욱
	 */
	private static void setOrder(Order order) {
		
		int count = 0;
		
		for (Order o : orderRepository) {
			if (o.getCode() == order.getCode()) {
				orderRepository.set(count, order);
			}
			count++;
		}
	}
	

	
	
	//유효성검사
	
	/**
	 * 숫자인지 검사하는 메서드입니다.
	 * 
	 * @param input 입력값
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean numValid(String input) {
		return Pattern.matches("^[0-9]+$", input);
	}


	/**
	 * #이 붙은 숫자인지 검사하는 메서드입니다.
	 * 
	 * @param input 입력값
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean sharpNumValid(String input) {
		
		return Pattern.matches("^#[0-9]+$", input);
	}

	
	/**
	 * 장바구니에 있는지 검사하는 메서드입니다.
	 * 
	 * @param input 입력값
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean isExistBasket(String input) {
		
		return Integer.parseInt(input.replace("#", "")) <= basket.size() ? true : false;
	}

	
	/**
	 * 회원 이름 양식에 맞는지 검사하는 메서드입니다.
	 * 
	 * @param input 입력값
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean nameValid(String input) {
		
		input = input.trim();
		return Pattern.matches("^[가-힣]{2,8}$", input);
	}
		

	/**
	 * 회원 아이디 양식에 맞는지 검사하는 메서드입니다.
	 * 
	 * @param input
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean idValid(String input) {
		
		input = input.trim();
		return Pattern.matches("^[a-zA-Z0-9]{4,16}$", input);
	}
	
	
	/**
	 * 중복 아이디인지 검사하는 메서드입니다.
	 * 
	 * @param input
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean isExistId(String input) {
		
		for (Member member : memberRepository) {
			if (member.getId().equalsIgnoreCase(input)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * 회원 비밀번호 양식에 맞는지 검사하는 메서드입니다.
	 * 
	 * @param input
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean passwordValid(String input) {
		
		input = input.trim();
		
		if (input.length() >= 8
			&& input.length() <= 16
			&& input.replaceAll("[a-zA-Z0-9\"!@#$%\"]", "").length() == 0
			&& input.replaceAll("[a-zA-Z0-9]", "").length() >= 1) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * 생일 양식에 맞는지 검사하는 메서드입니다.
	 * 
	 * @param input
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean birthValid(String input) {
		
		input = input.trim();
		return Pattern.matches("^[12][0-9]{3}-?[01]?[1-9]-?[0-3]?[0-9]$", input);
	}


	/**
	 * 전화번호 양식에 맞는지 검사하는 메서드입니다.
	 * 
	 * @param input
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean telValid(String input) {
		
		input = input.trim();
		return Pattern.matches("^01[017]-?[0-9]{3,4}-?[0-9]{4}$", input);
	}
	
	
	/**
	 * 주소 양식에 맞는지 검사하는 메서드입니다.
	 * 
	 * @param input
	 * @return 검사결과
	 * 
	 * @author 조진욱
	 */
	private static boolean addressValid(String input) {
		
		input = input.trim();
		return Pattern.matches("^[가-힣]{2,5}[시도] ?[가-힣]{2,5}[시군구] ?[가-힣 ]*$", input);
	}
	

	/**
	 * 카드로 주문을 할 때 카드 번호가 유효한지 유효성 검사를 해주는 메서드입니다.
	 * 
	 * @param input
	 * @return boolean
	 * @author 엄윤섭
	 * 
	 */
	private static boolean cardNumValid(String input) {
		
		return Pattern.matches("^[0-9]{4}-?[0-9]{4}-?[0-9]{4}-?[0-9]{4}$", input);
	}




	//유틸리티
	
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
		
//		space -= s.replaceAll("[^\\uAC00-\\uD7A3]", s).length();
//		
//		for (int i = 0; i < space; ++i) {
//			sb.append(" ");
//		}
//		
//		for (int i = 0; i < s.length(); ++i) {
//			sb.setCharAt(i, s.charAt(i));
//		}
		
		for (int i = 0; i < s.length(); ++i) {
			
			if (space - count > 3) {
				
				if ((s.charAt(i) >= '가' && s.charAt(i) <= '힣')
					|| (s.charAt(i) >= 'ㄱ' && s.charAt(i) <= 'ㅣ')) {
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
				
				if ((s.charAt(i) >= '가' && s.charAt(i) <= '힣')
					|| (s.charAt(i) >= 'ㄱ' && s.charAt(i) <= 'ㅣ')) {
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
			sb.insert(0, " ");
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
				
				if ((s.charAt(i) >= '가' && s.charAt(i) <= '힣')
					|| (s.charAt(i) >= 'ㄱ' && s.charAt(i) <= 'ㅣ')) {
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
	

	/**
	 * 입력창을 띄워주고 입력값을 반환하는 View 메서드입니다.
	 * 
	 * @return 사용자입력
	 */
	private static String getInput() {
		
		System.out.print(" 입력 : ");
		return scan.nextLine();
	}

	
	/**
	 * 사용자의 입력이 있을 때까지 잠시 멈추는 메서드입니다.
	 * 
	 * @author 조진욱
	 */
	private static void pause() {
		pause("");
	}
	
	
	/**
	 * 추가 메세지를 출력한 뒤 사용자의 입력이 있을 때까지 잠시 멈추는 메서드입니다.
	 * 
	 * @param message 추가 메세지
	 * 
	 * @author 조진욱
	 */
	private static void pause(String message) {
		
		System.out.println();
		if (!message.equalsIgnoreCase("")) {
			System.out.println(" " + message + "\n");
		}
		System.out.println(" 계속하시려면 엔터를 누르세요.");
		scan.nextLine();
	}

	
	/**
	 * 현재 진행중이던 행동을 멈추고 나갈 것인지 사용자의 의사를 물어봅니다.
	 * 현재 입력창이 이동셀렉터가 아닌 경우에 u, m, q를 입력했을 시 호출하는 메서드입니다.
	 * 
	 * @param input 입력값
	 * 
	 * @author 조진욱
	 */
	private static void exitWarning(String input) {
		exitWarning(input, "");
	}
	
	
	/**
	 * 추가 메세지를 출력한 뒤 현재 진행중이던 행동을 멈추고 나갈 것인지 사용자의 의사를 물어봅니다.
	 * 현재 입력창이 이동셀렉터가 아닌 경우에 u, m, q를 입력했을 시 호출하는 메서드입니다.
	 * 
	 * @param input 입력값
	 * 
	 * @author 조진욱
	 */
	private static void exitWarning(String input, String message) {
		
		String yn = "";
		
		while(!(yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("n"))) {
			
			if (!message.equalsIgnoreCase("")) {
				message += " ";
			}
			
			System.out.println("\n " + message + "정말 나가시겠습니까? (y/n)\n");
			yn = getInput();
			
			if (yn.equalsIgnoreCase("y")) {
				sel = input;
			} else if (yn.equalsIgnoreCase("n")){
				return;
			} else {
				pause("잘못된 입력입니다.");
			}
		}
	}
	

	
	
	//메인화면
	
	private static void mainScreen() {
		
		/*
		요구사항 RQ-01-00-00 ~
		 */
		
		while (!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("메인화면");
			topMessageView("");
			logoView();
			fixNewLineView();
			selectView("1.상품목록 2.장바구니 3.로그인 4.회원가입 5.주문조회");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				itemListScreen();
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				basketScreen();
				
			} else if (sel.equalsIgnoreCase("3")) {
				
				loginScreen();
				
			} else if (sel.equalsIgnoreCase("4")) {
				
				joinScreen();
				
				if (sel.equalsIgnoreCase("3")) {
					loginScreen();
				}
				
			} else if (sel.equalsIgnoreCase("5")) {
 
				orderLookupScreen();
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") ||sel.equalsIgnoreCase("q")) {
				
				return;
				
			} else {
				pause("잘못된 입력입니다.");
			}
		}
	}

	
	private static void loginMainScreen() {

		/*
		요구사항 RQ-01-10-00 ~
		 */
		
		while (!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("메인화면");
			topMessageView("");
			logoView();
			fixNewLineView();
			selectView("1.상품목록 2.장바구니 3.내정보");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				itemListScreen();
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				basketScreen();
				
			} else if (sel.equalsIgnoreCase("3")) {
				
				myInfoScreen();
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") ||sel.equalsIgnoreCase("q")) {
				
				return;
				
			} else {
				pause("잘못된 입력입니다.");
			}
		}
	}

	
	private static void adminMainScreen() {

		/*
		요구사항 RQ-01-20-00 ~
		 */
		
		while (!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("메인화면");
			topMessageView("");
			logoView();
			fixNewLineView();
			selectView("1.매출관리 2.주문관리 3.재고관리");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				salesManagementScreen();
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				orderManagementScreen();
				
			} else if (sel.equalsIgnoreCase("3")) {
				
				invenManagementScreen();
				
			} else if (sel.equalsIgnoreCase("4")) {
				
				memberManagementScreen();
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") ||sel.equalsIgnoreCase("q")) {
				
				return;
				
			} else {
				pause("잘못된 입력입니다.");
			}
		}
	}
	
	

	
	//상품목록
	
	private static void itemListScreen() {

		/*
		요구사항 RQ-02-00-00 ~
		 */
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("상품목록");
			topMessageView("");
			logoView();
			fixNewLineView();
			selectView("1.전체상품보기 2.카테고리선택 3.통합검색");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				totalListScreen();
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				categorySelectScreen();
				
			} else if (sel.equalsIgnoreCase("3")) {
				
				itemSearchScreen();
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;

			} else {
				pause("잘못된 입력입니다.");
			}
		}
	}

	
	
	
	//전체보기
	
	private static void totalListScreen() {

		/*
		요구사항 RQ-02-10-00 ~
		 */

		ArrayList<Item> items = itemRepository;
		
		int page = 1;
		boolean isNotLast = true;
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("전체상품보기");
			topMessageView("#과 숫자를 입력하면 상세페이지로 이동합니다. (예시 : #1)");
			isNotLast = itemListView(items, page);
			fixNewLineView();
			messageView("");

			if (page == 1 && !isNotLast) {
				
			} else if (page == 1) {
				selectView("1.검색 2.다음페이지");
			} else if (isNotLast) {
				selectView("1.검색 2.다음페이지 3.이전페이지");
			} else {
				selectView("1.검색 2.이전페이지");
			}
			
			sel = getInput();

			if (sharpNumValid(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				itemDetailScreen(items.get(index));

			} else if (sel.equalsIgnoreCase("1")) {
			
				itemSearchScreen();
				
			} else if (sel.equalsIgnoreCase("2") && !(page == 1 && !isNotLast)) {
		
				page += isNotLast ? 1 : -1;

			} else if (sel.equalsIgnoreCase("3") && page != 1 && !(page == 1 && !isNotLast)) {

				page --;

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}

	}

	
	private static void itemDetailScreen(Item item) {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("상품상세 : " + item.getCode());
			topMessageView("");
			itemDetailView(item);
			fixNewLineView();
			messageView("");
			selectView("1.장바구니추가");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
			
				basketAddScreen(item);
				
			}else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}
	
	
	private static void basketAddScreen(Item item) {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("상품상세 : " + item.getCode());
			itemDetailView(item);
			fixNewLineView();
			messageView("장바구니에 추가할 수량을 입력해주세요.");
			selectView("");
			sel = getInput();

			if (numValid(sel)) {
			
				ItemTag itemTag = itemTagRepository.get(item.getCode());
				itemTag.setCount(Integer.parseInt(sel));
				
				basket.add(itemTag);
				
				pause("장바구니 추가완료");
				
			} else if (sel.equalsIgnoreCase("2")) {
		
				

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}


	private static void itemSearchScreen() {

		SearchCondition condition = new SearchCondition();
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("통합검색");
			topMessageView("");
			searchConditionView(condition);
			fixNewLineView();
			messageView("");
			selectView("1.검색조건추가 2.검색어입력 3.검색실행");
			sel = getInput();
			
			if (sel.equalsIgnoreCase("1")) {
				
				condition = searchConditionInput(condition);
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				condition = searchWordInput(condition);
				
			} else if (sel.equalsIgnoreCase("3")) {
				
				searchListScreen(condition);
				
			} else if (sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u")) {
				
				return;
			
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}


	
	
	//카테고리보기

	private static void categorySelectScreen() {

		/*
		요구사항 RQ-02-20-00 ~
		 */

		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("카테고리선택");
			topMessageView("");
			logoView();
			fixNewLineView();
			selectView("1.쉐입 2.소재 3.브랜드");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				categoryShapeScreen();
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				categoryMaterialScreen();

			} else if (sel.equalsIgnoreCase("3")) {

				categoryBrandScreen();
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}


	private static void categoryShapeScreen() {
		
		Shape shape = null;
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("카테고리선택 : 쉐입");
			topMessageView("");
			fixNewLineView();
			messageView("");
			selectView("1.라운드 2.스퀘어 3.하금테 4.믹스 5.보잉 6.캣아이 7.기타");
			sel = getInput();

			if (Pattern.matches("^[1-7]$", sel)) {
				
				switch (sel) {
				case "1" : shape = Shape.ROUND; break;
				case "2" : shape = Shape.SQUARE; break;
				case "3" : shape = Shape.HALF_FRAME; break;
				case "4" : shape = Shape.MIX; break;
				case "5" : shape = Shape.BOEING; break;
				case "6" : shape = Shape.CATEYE; break;
				case "7" : shape = Shape.ELSE; break;
				}
				
				categoryListScreen(shape);
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}


	private static void categoryMaterialScreen() {
		
		Material material = null;
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("카테고리선택 : 소재");
			topMessageView("");
			fixNewLineView();
			messageView("");
			selectView("1.무테/반무테 2.메탈 3.뿔테 4.투명 5.티타늄 6.나무");
			sel = getInput();

			if (Pattern.matches("^[1-7]$", sel)) {
				
				switch (sel) {
				case "1" : material = Material.RIMLESS; break;
				case "2" : material = Material.METAL; break;
				case "3" : material = Material.PLASTIC; break;
				case "4" : material = Material.CLEAR; break;
				case "5" : material = Material.TITANIUM; break;
				case "6" : material = Material.WOOD; break;
				}
				
				categoryListScreen(material);
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}

	
	private static void categoryBrandScreen() {
		
		ArrayList<String> brands= new ArrayList<String>();
		brandList.stream().sorted((s1, s2) -> s1.compareTo(s2)).forEach(s -> brands.add(s));
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("카테고리선택 : 브랜드");
			topMessageView("");
			brandListView(brands);
			fixNewLineView();
			messageView("");
			selectView("");
			sel = getInput();

			if (numValid(sel) && Integer.parseInt(sel) <= brands.size()) {
				
				int index = Integer.parseInt(sel) - 1;
				categoryListScreen(brands.get(index));
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}
	

	private static void categoryListScreen(Shape shape) {
		
		ArrayList<Item> items = getCategoryItems(shape);
		
		int page = 1;
		boolean isNotLast = true;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("카테고리보기 : " + shape.getString());
			topMessageView("#과 숫자를 입력하면 상세페이지로 이동합니다. (예시 : #1)");
			isNotLast = itemListView(items, page);
			fixNewLineView();
			messageView("");
			
			if (page == 1 && !isNotLast) {
				
			} else if (page == 1) {
				selectView("1.검색 2.다음페이지");
			} else if (isNotLast) {
				selectView("1.검색 2.다음페이지 3.이전페이지");
			} else {
				selectView("1.검색 2.이전페이지");
			}
			
			sel = getInput();

			if (sharpNumValid(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				itemDetailScreen(items.get(index));

			} else if (sel.equalsIgnoreCase("1")) {
			
				itemSearchScreen();
				
			} else if (sel.equalsIgnoreCase("2") && !(page == 1 && !isNotLast)) {
		
				page += isNotLast ? 1 : -1;

			} else if (sel.equalsIgnoreCase("3") && page != 1 && !(page == 1 && !isNotLast)) {

				page --;

				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}
	
	
	private static void categoryListScreen(Material material) {
		
		ArrayList<Item> items = getCategoryItems(material);
		
		int page = 1;
		boolean isNotLast = true;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("카테고리보기 : " + material.getString());
			topMessageView("#과 숫자를 입력하면 상세페이지로 이동합니다. (예시 : #1)");
			isNotLast = itemListView(items, page);
			fixNewLineView();
			messageView("");
			
			if (page == 1 && !isNotLast) {
				
			} else if (page == 1) {
				selectView("1.검색 2.다음페이지");
			} else if (isNotLast) {
				selectView("1.검색 2.다음페이지 3.이전페이지");
			} else {
				selectView("1.검색 2.이전페이지");
			}
			
			sel = getInput();

			if (sharpNumValid(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				itemDetailScreen(items.get(index));

			} else if (sel.equalsIgnoreCase("1")) {
			
				itemSearchScreen();
				
			} else if (sel.equalsIgnoreCase("2") && !(page == 1 && !isNotLast)) {
		
				page += isNotLast ? 1 : -1;

			} else if (sel.equalsIgnoreCase("3") && page != 1 && !(page == 1 && !isNotLast)) {

				page --;

				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
	}
	
	
	private static void categoryListScreen(String brand) {
		
		ArrayList<Item> items = getCategoryItems(brand);
		
		int page = 1;
		boolean isNotLast = true;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("카테고리보기 : " + brand);
			topMessageView("#과 숫자를 입력하면 상세페이지로 이동합니다. (예시 : #1)");
			isNotLast = itemListView(items, page);
			fixNewLineView();
			messageView("");

			if (page == 1 && !isNotLast) {
				
			} else if (page == 1) {
				selectView("1.검색 2.다음페이지");
			} else if (isNotLast) {
				selectView("1.검색 2.다음페이지 3.이전페이지");
			} else {
				selectView("1.검색 2.이전페이지");
			}
			
			sel = getInput();

			if (sharpNumValid(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				itemDetailScreen(items.get(index));

			} else if (sel.equalsIgnoreCase("1")) {
			
				itemSearchScreen();
				
			} else if (sel.equalsIgnoreCase("2") && !(page == 1 && !isNotLast)) {
		
				page += isNotLast ? 1 : -1;

			} else if (sel.equalsIgnoreCase("3") && page != 1 && !(page == 1 && !isNotLast)) {

				page --;

				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}
	

	

	//검색
	
	/**
	 * 지금까지 입력된 검색조건을 출력하는 View 메서드입니다.
	 * 
	 * @param condition 검색조건
	 * 
	 * @author 조진욱
	 */
	private static void searchConditionView(SearchCondition condition) {
		
		StringBuilder sb = new StringBuilder();
		
		int[] prices = condition.getPrices();
		String[] sizes = condition.getSizes();
		
		sb.append(VERTICAL);
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append("| " + fixLeftString("   쉐입", 20) + ":    " + fixLeftString(condition.getShapes(), 140) + " |\n");
		sb.append("| " + fixLeftString("   소재", 20) + ":    " + fixLeftString(condition.getMaterials(), 140) + " |\n");
		sb.append("| " + fixLeftString("   브랜드", 20) + ":    " + fixLeftString(condition.getBrands(), 140) + " |\n");
		sb.append("| " + fixLeftString("   가격(최소)", 20) + ":    " + String.format("%,-140d", prices[0]) + " |\n");
		sb.append("| " + fixLeftString("   가격(최대)", 20) + ":    " + String.format("%,-140d", prices[1]) + " |\n");
		sb.append("| " + fixLeftString("   규격(최소)", 20) + ":    " + fixLeftString(sizes[0], 140) + " |\n");
		sb.append("| " + fixLeftString("   규격(최대)", 20) + ":    " + fixLeftString(sizes[1], 140) + " |\n");
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append(VERTICAL);
		sb.append("\n");
		sb.append(VERTICAL);
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append("| " + fixLeftString("   검색어", 20) + ":    " + fixLeftString(condition.getWord(), 141) + "|\n");
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append(VERTICAL);
		
		viewLineCount += 18;
		System.out.println(sb.toString());
	}
	
	
	/**
	 * 입력된 검색조건에 부합하는 상품객체들을 컬렉션으로 반환하는 메서드입니다.
	 * 
	 * @param condition 검색조건
	 * @return 검색된 상품객체 컬렉션
	 * 
	 * @author 조진욱
	 */
	private static ArrayList<Item> getSearchResult(SearchCondition condition) {

		ArrayList<Item> items = new ArrayList<Item>();
		
		ArrayList<String> shapes = condition.getShapes().length() == 0 ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(condition.getShapes().split(", ")));
		ArrayList<String> materials = condition.getMaterials().length() == 0 ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(condition.getMaterials().split(", ")));
		ArrayList<String> brands = condition.getBrands().length() == 0 ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(condition.getBrands().split(", ")));
		int[] prices = condition.getPrices();
		int[][] sizes = new int[2][3];
		String word = condition.getWord().trim();
		String[] words = word.length() == 0 ? null : word.split(" ");
		
		for (int i = 0; i < 2; ++i) {
			
			String[] temp = condition.getSizes()[i].split("-");
			sizes[i] = Arrays.stream(temp).mapToInt(s -> Integer.parseInt(s)).toArray();
		}

		ArrayList<Item> temp = new ArrayList<Item>();
		itemRepository.stream().filter(item -> shapes.isEmpty() ? true : shapes.contains(item.getShape().getString()))
								.filter(item -> brands.isEmpty() ? true : brands.contains(item.getBrand()))
								.filter(item -> itemTagRepository.get(item.getCode()).getPrice() >= prices[0]
										&& itemTagRepository.get(item.getCode()).getPrice() <= prices[1])
								.forEach(item -> temp.add(item));
		
		loopOut:
		for (Item item : temp) {
			
			if (itemTagRepository.get(item.getCode()).getPrice() < prices[0]
				&& itemTagRepository.get(item.getCode()).getPrice() > prices[1]) {
			
				continue;
			}
				
			
			int[] itemSize = Arrays.stream(item.getSize().split("-")).mapToInt(s -> Integer.parseInt(s)).toArray();
			
			if (itemSize[0] < sizes[0][0] && itemSize[0] > sizes[1][0]
				&& itemSize[1] < sizes[0][1] && itemSize[1] > sizes[1][1]
				&& itemSize[2] < sizes[0][2] && itemSize[2] > sizes[1][2]) {
				continue;
			}
					
			for (Material material : item.getMaterial()) {
				
				if (!(materials.isEmpty()
					)) {
					continue;
					
				} else if (!(materials.contains(material.getString())
						)) {
					continue;
				}
			}
			
			if (words != null) {
				
				int count = 0;
				for (String s : words) {
					
					if (!item.getName().contains(s)
						&& !s.contains(item.getShape().getString())
						&& !s.contains(item.getBrand())) {
						continue;
					}
					
					count++;

				}
				
				if (count != words.length) {
					continue loopOut;
				}
			}
			
			items.add(item);
		}
		
		return items;
	}


	private static SearchCondition searchConditionInput(SearchCondition condition) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			titleView("검색조건추가");
			topMessageView("");
			searchConditionView(condition);
			fixNewLineView();
			messageView("");
			selectView("1.쉐입 2.소재 3.브랜드 4.가격 5.규격 r.이전");
			sel = getInput();
			
			if (sel.equalsIgnoreCase("1")) {
				
				return searchShapeInput(condition);
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				return searchMaterialInput(condition);
				
			} else if (sel.equalsIgnoreCase("3")) {
				
				return searchBrandInput(condition);
				
			} else if (sel.equalsIgnoreCase("4")) {
				
				return searchPriceInput(condition);
				
			} else if (sel.equalsIgnoreCase("5")) {
				
				return searchSizeInput(condition);
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("r")) {
				
				return condition;
			
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
		
		return null;
	}


	private static SearchCondition searchShapeInput(SearchCondition condition) {
		
		SearchCondition result = condition;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			titleView("검색조건추가 : 쉐입");
			topMessageView("");
			searchConditionView(result);
			fixNewLineView();
			messageView("검색하고 싶은 카테고리를 입력해주세요. 쉼표로 구분하여 다중입력합니다. (예 : 1, 3, 4)");
			selectView("1.라운드 2.스퀘어 3.하금테 4.믹스 5.보잉 6.캣아이 7.기타 r.이전");
			sel = getInput();
			
			if (Pattern.matches("^[1-7, ]+$", sel)) {
				
				String temp[] = sel.split(",");
				Shape shape = null;
				String shapeCondition = "";
				
				for (int i = 0; i < temp.length; ++i) {
					
					temp[i] = temp[i].trim();
					
					switch (temp[i]) {
					case "1" : shape = Shape.ROUND; break;
					case "2" : shape = Shape.SQUARE; break;
					case "3" : shape = Shape.HALF_FRAME; break;
					case "4" : shape = Shape.MIX; break;
					case "5" : shape = Shape.BOEING; break;
					case "6" : shape = Shape.CATEYE; break;
					case "7" : shape = Shape.ELSE; break;
					}
					
					if (i == 0) {
						shapeCondition = shape.getString();
					} else {
						shapeCondition += ", " + shape.getString();
					}
				}
				
				
				result.setShapes(shapeCondition);
				return result;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("r")) {
				
				return condition;
			
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
		return condition;
	}
	
	
	private static SearchCondition searchMaterialInput(SearchCondition condition) {
		
		SearchCondition result = condition;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			titleView("검색조건추가 : 소재");
			topMessageView("");
			searchConditionView(result);
			fixNewLineView();
			messageView("검색하고 싶은 카테고리를 입력해주세요. 쉼표로 구분하여 다중입력합니다. (예 : 1, 3, 4)");
			selectView("1.무테/반무테 2.메탈 3.뿔테 4.투명 5.티타늄 6.나무 r.이전");
			sel = getInput();
			
			if (Pattern.matches("^[1-6, ]+$", sel)) {
				
				String temp[] = sel.split(",");
				Material material = null;
				String materialCondition = "";
				
				for (int i = 0; i < temp.length; ++i) {
					
					temp[i] = temp[i].trim();
					
					switch (temp[i]) {
					case "1" : material = Material.RIMLESS; break;
					case "2" : material = Material.METAL; break;
					case "3" : material = Material.PLASTIC; break;
					case "4" : material = Material.CLEAR; break;
					case "5" : material = Material.TITANIUM; break;
					case "6" : material = Material.WOOD; break;
					}
					
					if (i == 0) {
						materialCondition = material.getString();
					} else {
						materialCondition += ", " + material.getString();
					}
				}
				
				result.setMaterials(materialCondition);
				return result;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("r")) {
				
				return condition;
			
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
		return condition;
	}
	
	
	private static SearchCondition searchBrandInput(SearchCondition condition) {
		
		SearchCondition result = condition;
		ArrayList<String> brands = new ArrayList<String>();
		brandList.stream().sorted((s1, s2) -> s1.compareTo(s2)).forEach(s -> brands.add(s));
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			titleView("검색조건추가 : 브랜드");
			topMessageView("");
			searchConditionView(result);
			for (int i = 0; i < brands.size(); ++i) {
				System.out.println((i+1) + brands.get(i));
			}
			fixNewLineView();
			messageView("검색하고 싶은 카테고리를 입력해주세요. 쉼표로 구분하여 다중입력합니다. (예 : 1, 3, 4)");
			selectView("r.이전");
			sel = getInput();
			
			if (Pattern.matches("^[0-9, ]+$", sel)) {
				
				String[] temp = sel.split(",");
				String brandCondition = "";
				
				for (int i = 0; i < temp.length; ++i) {
					
					int index = Integer.parseInt(temp[i].trim()) - 1;
					
					if (i == 0) {
						brandCondition = brands.get(index);
					} else {
						brandCondition += ", " + brands.get(index);
					}
				}
				
				result.setBrands(brandCondition);
				return result;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("r")) {
				
				return condition;
			
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
		return condition;
	}
	
	
	private static SearchCondition searchPriceInput(SearchCondition condition) {
		
		SearchCondition result = condition;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			titleView("검색조건추가 : 가격");
			topMessageView("");
			searchConditionView(result);
			fixNewLineView();
			messageView("검색하고 싶은 가격 범위를 입력해주세요. 쉼표로 구분하여 입력합니다. (예 : 100000, 400000 = 10만원에서 40만원 사이)");
			selectView("r.이전");
			sel = getInput();
			
			if (Pattern.matches("^[0-9]{1,7}, ?[0-9]{1,7}$", sel)) {
				
				String[] temp = sel.split(",");
				int num1 = Integer.parseInt(temp[0].trim());
				int num2 = Integer.parseInt(temp[1].trim());
				
				int[] priceCondition = condition.getPrices();

				priceCondition[0] = num1 < num2 ? num1 : num2;
				priceCondition[1] = num1 < num2 ? num2 : num1;
				
				result.setPrices(priceCondition);
				return result;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("r")) {
				
				return condition;
			
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
		return condition;
	}
	

	private static SearchCondition searchSizeInput(SearchCondition condition) {
		
		SearchCondition result = condition;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			titleView("검색조건추가 : 규격");
			topMessageView("");
			searchConditionView(result);
			fixNewLineView();
			messageView("검색하고 싶은 규격 범위를 입력해주세요. 쉼표로 구분하여 입력합니다. (형식 : 렌즈직경-브릿지거리-다리길이, 예 : 30-10-130, 50-23-14)");
			selectView("r.이전");
			sel = getInput();
			
			if (Pattern.matches("^[3-5][0-9]-[1-2][0-9]-1[3-4][0-9], ?[3-5][0-9]-[1-2][0-9]-1[3-4][0-9]$", sel)) {
				
				String[] temp = sel.split(",");
				String[] sizeCondition = condition.getSizes();

				sizeCondition[0] = temp[0].trim();
				sizeCondition[1] = temp[1].trim();
				
				result.setSizes(sizeCondition);
				return result;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("r")) {
				
				return condition;
			
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
		return condition;
	}
	

	private static SearchCondition searchWordInput(SearchCondition condition) {
		
		titleView("통합검색");
		topMessageView("");
		searchConditionView(condition);
		fixNewLineView();
		messageView("검색어를 입력해주세요.");
		selectView("");
		sel = getInput();
		
		if (sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u")) {
			
			return condition;
		
		} else {
			
			condition.setWord(sel);
			return condition;
		}
	}

	
	/**
	 * 검색결과 화면을 보여주는 Screen 메서드입니다.
	 * 
	 * @param condition 검색조건
	 * 
	 * @author 조진욱
	 */
	private static void searchListScreen(SearchCondition condition) {
		
		ArrayList<Item> items = getSearchResult(condition);
		
		int page = 1;
		boolean isNotLast = true;
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("검색결과");
			topMessageView("#과 숫자를 입력하면 상세페이지로 이동합니다. (예시 : #1)");
			isNotLast = itemListView(items, page);
			fixNewLineView();
			messageView("");

			if (page == 1 && !isNotLast) {
				
			} else if (page == 1) {
				selectView("1.검색 2.다음페이지");
			} else if (isNotLast) {
				selectView("1.검색 2.다음페이지 3.이전페이지");
			} else {
				selectView("1.검색 2.이전페이지");
			}
			
			sel = getInput();

			if (sharpNumValid(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				itemDetailScreen(items.get(index));

			} else if (sel.equalsIgnoreCase("1")) {
			
				itemSearchScreen();
				
			} else if (sel.equalsIgnoreCase("2") && !(page == 1 && !isNotLast)) {
		
				page += isNotLast ? 1 : -1;

			} else if (sel.equalsIgnoreCase("3") && page != 1 && !(page == 1 && !isNotLast)) {

				page --;

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
		
	}


	
	
	//장바구니
	
	/**
	 * 장바구니를 출력하는 View 메서드입니다.
	 * 
	 * @author 조진욱
	 */
	private static void basketView() {
		
		StringBuilder sb = new StringBuilder();
		int sum = 0;
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s | %s | %s |\n"
								, "번호"
								, fixCenterString("상품코드", 25)
								, fixCenterString("브랜드", 20)
								, fixCenterString("상품명", 80)
								, fixCenterString("수량", 6)
								, fixCenterString("가격", 15)));
		
		sb.append(VERTICAL);
		for (int i = 0; i < basket.size(); ++i) {
			
			sb.append(String.format("| %s | %s | %s | %s | %,6d | %,15d |\n"
									, fixCenterString(i + 1 + "", 4)
									, fixCenterString(basket.get(i).getItemCode(), 25)
									, fixCenterString(getItem(basket.get(i).getItemCode()).getBrand(), 20)
									, fixLeftString(getItem(basket.get(i).getItemCode()).getName(), 80)
									, basket.get(i).getCount()
									, basket.get(i).getPrice() * basket.get(i).getCount()));
			viewLineCount ++;
			
			sum += basket.get(i).getPrice() * basket.get(i).getCount();
		}
		sb.append(VERTICAL);
		sb.append(String.format("%s : %,13d 원 \n\n", fixRightString("총 가격", 149), sum));
		
		viewLineCount += 7;
		System.out.println(sb.toString());
	}

	
	/**
	 * 장바구니의 기본 화면입니다.
	 * 
	@author 엄윤섭
	 **/
	private static void basketScreen() {
	
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
		
			titleView("장바구니");
			topMessageView("");
			basketView();
			fixNewLineView();
			messageView("");
			selectView("1.수량수정 2.상품삭제 3.주문하기");
			sel = getInput();
			
			if (sel.equalsIgnoreCase("1")) {
			
				basketCountChange();
			
			} else if (sel.equalsIgnoreCase("2")) {
			
				basketRemove();
			
			} else if (sel.equalsIgnoreCase("3")) {
			
				loginChecker();
			
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
			
				return;
				
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", sel));
			}		
		}
	}
	
	
	/**
	 * 
	 * 바스켓 내의 수량을 수정하는 메서드입니다.
	 * 
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static void basketCountChange() {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
		
			titleView("상품수정");
			topMessageView("");
			basketView();
			viewLineCount -= 3;
			fixNewLineView();
			messageView("수정할 상품의 번호를 입력해주세요. (형식 : #숫자, 예시 : #1)");
			selectView("r.뒤로");
			sel = getInput();
			
			if (sharpNumValid(sel) && isExistBasket(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				basketCountInput(index);
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {

				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}

	
	/**
	 * 
	 * 바스켓 내 상품의 원하는 수정 갯수를 입력하는 메서드입니다.
	 * 
	 * @param index(수량)
	 * 
	 * @author 엄윤섭
	 */
	private static void basketCountInput(int index) {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("상품수정");
			topMessageView("");
			basketView();
			viewLineCount += 3;
			fixNewLineView();
			messageView("변경하실 수량을 입력해주세요.");
			selectView("r.뒤로");
			sel = getInput();
			
			if (numValid(sel)) {
				
				int count = Integer.parseInt(sel);
				ItemTag result = basket.get(index);
				result.setCount(count);
				basket.set(index, result);
				sel = "u";
				return;
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {

				return;
				
			} else {
				pause("잘못된 입력입니다.");
			}
		}
	}

	
	/**
	 * 바스켓에 들어있는 상품을 지우는 메서드입니다.
	 * 
	 * @author 엄윤섭
	 */
	private static void basketRemove() {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
		
			titleView("상품 삭제");
			topMessageView("");
			basketView();
			viewLineCount -= 3;
			fixNewLineView();
			messageView("삭제할 상품의 번호를 입력해주세요. (형식 : #숫자, 예시 : #1)");
			selectView("r.뒤로");
			sel = getInput();
			
			if (sharpNumValid(sel) && isExistBasket(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				basketRemoveInput(index);
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {

				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}

	
	/**
	 * 바스켓에 들어있는 상품을 지울 때 일련의 과정을 체크하는 메서드입니다.
	 * 
	 * @author 엄윤섭
	 */
	private static void basketRemoveInput(int index) {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("상품 삭제");
			topMessageView("");
			basketView();
			viewLineCount -= 3;
			fixNewLineView();
			messageView("정말 삭제하시겠습니까?");
			selectView("y.예 r.뒤로");
			sel = getInput();
			
			if (sel.equalsIgnoreCase("y")) {
				
				basket.remove(index);
				sel = "u";
				return;
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {

				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}
	
	
	
	
	//상품주문
	
	/**
	 * 로그인 상태인지 확인하는 메서드입니다.
	 * 
	 * @author User엄윤섭
	 * 
	 */
	private static void loginChecker() {

		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			Order order = new Order();
			order.setItemInfo(OrderItemTag.toList(basket));
			order.setMemberCode(loginMemberCode);
			
			if (loginMemberCode != -1) {
				
				Member member = getMember(loginMemberCode);
				PersonCard orderMan = new PersonCard(member.getName(), member.getTel());
				order.setOrderMan(orderMan);
				orderCheckDuplicate(order);
				
			} else if (loginMemberCode == -1 && sel == "pass") {
				
				orderOrderManNameinput(order);
				
			} else {
				
				sel = "order";
				loginScreen();
			}
		}
	}
	
	
	/**
	 * 주문자와 수령인의 이름 동일 여부를 확인하고 동일하지 않을 경우 수령인의 이름 입력받는 메서드입니다.
	 * 
	 * @param order 객체
	 * @return order 객체(주문자 정보와 수령인 정보가 동일한지를 판단한 후 반환하는 주문 객체)
	 * 
	 * @author User
	 * 
	 */
	private static Order orderOrderManNameinput(Order order) {

		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("주문자 이름을 입력해주세요.");
			selectView("r.이전");
			sel = getInput();
			
			if (nameValid(sel)) {

				PersonCard orderMan = order.getOrderMan();
				orderMan.setName(sel);
				order.setOrderMan(orderMan);
				order = orderOrderManTelInput(order);
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
	
		return order;
	}
	
	
	/**
	 * 주문자와 수령인의 연락처 동일 여부를 확인하고 동일하지 않을 경우 수령인의 연락처를 입력받는 메서드입니다.
	 * 
	 * @param order 객체
	 * @return order 객체(주문자 정보와 수령인 정보가 동일한지를 판단한 후 반환하는 주문 객체)
	 * 
	 * @author User
	 * 
	 */
	private static Order orderOrderManTelInput(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("주문자 연락처를 입력해주세요.");
			selectView("r.이전");
			sel = getInput();

			if (telValid(sel)) {

				PersonCard orderMan = order.getOrderMan();
				orderMan.setTel(sel);
				order.setOrderMan(orderMan);
				order = orderCheckDuplicate(order);
				
			} else if (sel.equalsIgnoreCase("r")) {

				PersonCard orderMan = order.getOrderMan();
				orderMan.setName("");
				order.setOrderMan(orderMan);
				return order;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}
	
	
	/**
	 * 
	 * 상품을 주문하는 주문자와 수령받는 수령자의 동일 여부를 확인하는 메서드입니다.
	 * 
	 * @param 주문 객체
	 * @return 주문 객체
	 * 
	 * @author 엄윤섭
	 */
	private static Order orderCheckDuplicate(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("주문자와 수령인이 같습니까?");
			selectView("y.예 n.아니오");
			sel = getInput();

			if (sel.equalsIgnoreCase("y")) {
				
				order.setReceiver(order.getOrderMan());
				
				if (loginMemberCode != -1) {

					order = orderDefaultAddressCheck(order);
					
				} else {
					
					order = orderAddressInput(order);
				}
				
				
			} else if (sel.equalsIgnoreCase("n")) {
				
				orderReceiverNameInput(order);
				
			} else if (sel.equalsIgnoreCase("r")) {
				
				PersonCard orderMan = order.getOrderMan();
				orderMan.setTel("");
				order.setOrderMan(orderMan);
				return order;

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}

	
	/**
	 * 주문을 할 때, 수령인의 이름을 입력받는 메서드입니다.
	 * 
	 * @param order
	 * @return
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static Order orderReceiverNameInput(Order order) {

		order.setItemInfo(OrderItemTag.toList(basket));
		order.setMemberCode(loginMemberCode);
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("수령인 이름을 입력해주세요.");
			selectView("r.이전");
			sel = getInput();
			
			if (nameValid(sel)) {
				
				PersonCard receiver = order.getReceiver();
				receiver.setName(sel);
				order.setReceiver(receiver);
				order = orderReceiverTelInput(order);
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
	
		return order;
	}
	
	
	/**
	 * 주문을 할 때, 수령인의 전화번호를 입력받는 메서드입니다.
	 * 
	 * @param order
	 * @return
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static Order orderReceiverTelInput(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("수령인 연락처를 입력해주세요.");
			selectView("r.이전");
			sel = getInput();

			if (telValid(sel)) {

				PersonCard orderMan = order.getOrderMan();
				orderMan.setTel(sel);
				order.setOrderMan(orderMan);
				
				if (loginMemberCode != -1) {
					order = orderDefaultAddressCheck(order);
				} else {
					order = orderAddressInput(order);
				}
				
			} else if (sel.equalsIgnoreCase("r")) {

				PersonCard receiver = order.getReceiver();
				receiver.setName("");
				order.setReceiver(receiver);
				return order;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}

	
	/**
	 * 주문을 할 때, 수령인의 주소을 입력받는 메서드입니다.(yes, no 구분)
	 * 
	 * @param order
	 * @return
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static Order orderDefaultAddressCheck(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("기존 주소를 사용하시겠습니까?");
			selectView("y.예 n.아니오 r.이전");
			sel = getInput();

			if (sel.equalsIgnoreCase("y")) {

				order.setAddress(getMember(loginMemberCode).getAddress());
				order = orderFinalCheck(order);
				
			} else if(sel.equalsIgnoreCase("n")){
				
				orderAddressInput(order);
				
				
			} else if (sel.equalsIgnoreCase("r")) {

				PersonCard receiver = order.getReceiver();
				receiver.setTel("");
				order.setReceiver(receiver);
				return order;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}

	
	/**
	 * 주문을 할 때, 수령인의 주소을 입력받는 메서드입니다.(yes, no 구분)
	 * 
	 * @param order
	 * @return
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static Order orderAddressInput(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("배송주소를 입력해주세요.");
			selectView("r.이전");
			sel = getInput();

			if (addressValid(sel)) {

				order.setAddress(sel);
				order = orderFinalCheck(order);
				
			} else if (sel.equalsIgnoreCase("r")) {

				PersonCard receiver = order.getReceiver();
				receiver.setTel("");
				order.setReceiver(receiver);
				return order;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}

	
	/**
	 * 주문을 할 때 최종적으로 체크를 하는 메서드입니다.
	 * 
	 * @param order
	 * @return
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static Order orderFinalCheck(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("상품주문");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("위와 같이 주문하시겠습니까?");
			selectView("y.예 r.이전");
			sel = getInput();

			if (sel.equalsIgnoreCase("y")) {
				
				payOrderScreen(order);
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}

	
	/**
	 * 주문을 할 때, 결제 방식을 선택하는 메서드입니다.
	 * 
	 * @param order
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static void payOrderScreen(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("주문결제");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("결제 방식을 선택해주세요.");
			selectView("1.카드결제 2.무통장입금 r.이전");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				cardPayInput(order);
				
			} else if (sel.equalsIgnoreCase("2")) {

				bankbookPayConfirm(order);
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
	}
	
	
	/**
	 * 주문을 할 때, 카드로 결제를 하는 메서드입니다.
	 * 
	 * @param order
	 * 
	 * @author 엄윤섭
	 * 
	 */
	private static void cardPayInput(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("주문결제");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("카드 번호를 입력해주세요.");
			selectView("r.이전");
			sel = getInput();

			if (cardNumValid(sel)) {
				
				PayInfo payInfo = new PayInfo();
				payInfo.setPayOption(PayOption.CARD);
				payInfo.setPrice(order.getTotalPrice());
				payInfo.setStatus(true);
				payInfo.setNum(sel);
				order.setPayInfo(payInfo);
				
				cardPayConfirm(order);
				
			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;

			} else {

				pause("잘못된 입력입니다.");
			}
		}		
	}


	private static void cardPayConfirm(Order order) {
		
		OrderTimeStamp stamp = new OrderTimeStamp();
		stamp.setOrderTime(Calendar.getInstance());
		
		order.setTimeStamp(stamp);
		
		System.out.println(order);
		
		orderRepository.add(order);
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("결제완료");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView(fixCenterString("결제가 완료되었습니다.", 167));
			selectView("엔터를 누르면 메인 화면으로 이동합니다.");
			sel = getInput();

			sel = "m";
		}
	}
	

	private static void bankbookPayConfirm(Order order) {

		PayInfo payInfo = new PayInfo();
		payInfo.setPayOption(PayOption.BANKBOOK);
		payInfo.setPrice(order.getTotalPrice());
		payInfo.setStatus(false);
		payInfo.setNum("123456-12-123456");
		order.setPayInfo(payInfo);
		
		OrderTimeStamp stamp = new OrderTimeStamp();
		stamp.setOrderTime(Calendar.getInstance());
		order.setTimeStamp(stamp);
		
		System.out.println(order);
		
		orderRepository.add(order);
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("결제완료");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView(fixCenterString("24시간 이내에 해당 계좌로 입금해주세요.", 167));
			selectView("엔터를 누르면 메인 화면으로 이동합니다.");
			sel = getInput();

			sel = "m";
		}
	}

	
	/**
	 * 로그인 상태에서 주문 정보들을 총합하는 메서드입니다.
	 * 
	 * @param basket 장바구니
	 * 
	 * @author 엄윤섭
	 * 
	 * 
	 */
	private static void memberOrderScreen(ArrayList<ItemTag> basket) {
	
		String input = "";
		
		Order order = new Order();
		Member member = getMember(loginMemberCode);
		
		PersonCard orderMan = new PersonCard();
		PersonCard receiver = new PersonCard();

		ArrayList<OrderItemTag> orderItemTag = new ArrayList<OrderItemTag>();

		
		String message1 = "";
		String message2 = "";
		String message3 = "주문자와 수령인이 같습니까?";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("상품주문");
			topMessageView(message1);
			fixNewLineView();

			System.out.println();
			orderMan.setName(member.getName());
			System.out.printf("주문자 이름  : %s\r\n",orderMan.getName());
			orderMan.setTel(member.getTel());
			System.out.printf("주문자 전화번호 : %s\r\n",orderMan.getTel());
			
			messageView(message2);
			selectView(message3);
			input = getInput();
			
			if(input.equalsIgnoreCase("y")) {
				
				sel = "y";
				receiver.setName(orderMan.getName());
				receiver.setTel(orderMan.getTel());
				
			}else if(input.equalsIgnoreCase("n")){

				sel = "n";
				message3 = "수령인 이름을 입력하세요.";
				receiver.setName(input);
				
			}else if(sel.equalsIgnoreCase("n")){
				
				message3 = "수령인 전화번호를 입력하세요.";
				receiver.setTel(input);
				
			}else if(input.equalsIgnoreCase("q") || input.equalsIgnoreCase("m") || input.equalsIgnoreCase("u")){
				
				sel = input;
				return;
				
			}else {
				
				message2 = "잘못된 입력입니다.";
				break;
				
			}
			
			
			System.out.println();
			System.out.print("기존 주소를 사용하시겠습니까?y/n");
			System.out.println("[m:메인][u:이전][q:종료]");
			System.out.print("입력 : ");
			input = scan.nextLine();
			
			if(input.equalsIgnoreCase("y")) {
				order.setAddress(member.getAddress());
			}else if(input.equalsIgnoreCase("n")){
				System.out.print("수령인 주소 입력 : ");
				order.setAddress(scan.nextLine());
			}else if(input.equalsIgnoreCase("q")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("u")){
				sel = input;
			}else {
				System.out.println("y/n만 입력가능합니다.");
				break;
			}
			
			order.setReceiver(receiver);
			order.setOrderMan(orderMan);
			
			long orderCode = orderRepository.size() + 1;
			
			OrderAdd(basket, orderCode, orderItemTag);
			
			order.setCode(orderCode); // 주문코드
			order.setMemberCode(-1); // 주문 회원 코드
			order.setItemInfo(orderItemTag); //주문 상품 정보

			System.out.println();

			payMent(order, basket);
			
		}
	}
	

	/**
	 * 비로그인 상태에서 주문 정보들을 총합하는 메서드입니다.
	 * @param basket 장바구니
	 * 
	 * @author 엄윤섭
	 */
	private static void nonMemberOrderScreen(ArrayList<ItemTag> basket) {
		String input = "";
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("로그인 하시겠습니까? [y. 예] [n. 아니오]");
			System.out.println("[m:메인][u:이전][q:종료]");
			System.out.print("입력: ");
			input = scan.nextLine();
			if(input.equalsIgnoreCase("u")||input.equalsIgnoreCase("m")||input.equalsIgnoreCase("q")) {
				sel = input;
				return;
			}
			
			if (input.equalsIgnoreCase("y")) {
				loginScreen();
				return;
			} else if (input.equalsIgnoreCase("n")) {
				PersonCard orderNameAndTel = new PersonCard();
				PersonCard receiverNameAndTel = new PersonCard();
				Order order = new Order();
				ArrayList<OrderItemTag> orderItemTag = new ArrayList<OrderItemTag>();
				
				System.out.print("주문자 이름 입력 : ");
				orderNameAndTel.setName(scan.nextLine());
				System.out.print("주문자 전화번호 입력 : ");
				orderNameAndTel.setTel(scan.nextLine());
				
				System.out.print("주문자와 수령인이 같습니까? : ");
				input = scan.nextLine();
				String receiverName = "";
				String receiverTel = "";
				
				if(input.equalsIgnoreCase("y")) {
					receiverNameAndTel.setName(orderNameAndTel.getName());
					receiverNameAndTel.setTel(orderNameAndTel.getTel());

				}else if(input.equalsIgnoreCase("n")){
					System.out.print("수령인 이름 입력 : ");
					receiverNameAndTel.setName(scan.nextLine());
					System.out.print("수령인 전화번호 입력 : ");
					receiverNameAndTel.setTel(scan.nextLine());
				}else if(input.equalsIgnoreCase("q")||
						input.equalsIgnoreCase("m")||
						input.equalsIgnoreCase("u")){
					sel = input;
					return;
				}else {
					System.out.println("y/n만 입력가능합니다.");
					break;
				}
				System.out.print("수령인 주소 입력 : ");
				order.setAddress(scan.nextLine());
				order.setReceiver(receiverNameAndTel);
				order.setOrderMan(orderNameAndTel);
				
				long orderCode = orderRepository.size() + 1;
				
				OrderAdd(basket, orderCode, orderItemTag);
				
				order.setCode(orderCode); // 주문코드
				order.setMemberCode(-1); // 주문 회원 코드
				order.setItemInfo(orderItemTag); //주문 상품 정보

				System.out.println();

				payMent(order, basket);

			} else if (input.equalsIgnoreCase("u") || (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m")))) {
				sel = input;
				return;
			}
		}

	}
	
	   
	/**
	 * 상품정보를 생성하는 메서드입니다.
	 * @param itemCode 상품코드
	 * @return itemTag 상품정보
	 * @author 엄윤섭
	 */
	private static ItemTag createItemTag(String itemCode) {
		ItemTag it = new ItemTag();
		
		while (!(sel.equalsIgnoreCase("m") || (sel.equalsIgnoreCase("q")))) {
   		 
	       	 try {
		        	System.out.print("주문 수량 입력 : ");
		        	String co = scan.nextLine();
		        	
		        	int price = 0;
		        	int count = 0;
		        	
		        	if(itemTagRepository.containsKey(itemCode)) {
		        		price = itemTagRepository.get(itemCode).getPrice();
		        		count = itemTagRepository.get(itemCode).getCount();
		        	}
		        	
		        	if(count >= Integer.parseInt(co)) {
			        	
			        	it.setPrice(price);
			        	it.setCount(Integer.parseInt(co));
			        	it.setItemCode(itemCode);
			        	
			        	return it;
			        	
		        	}else {
		        		continue;
		        	}
	       	}catch(NumberFormatException e){
	       		System.out.println("숫자를 입력하세요.");
	       	}
		}
		return it;
	}


	/**
	 * 주문정보 및 결제를 진행하는 메서드입니다.
	 * @param order 주문정보
	 * @param basket 장바구니
	 * @author 엄윤섭
	 */
	private static void payMent(Order order, ArrayList<ItemTag> basket) {

		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			int price = 0;
			String input = "";
			String itemCode = "";

			System.out.println("1. 카드 결제 2. 무통장 입금");
			System.out.println("[u. 이전 단계] [m. 메인] [q. 종료]");
			System.out.print("입력: ");
			sel = scan.nextLine();

			if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
				return;
			}
			
			price = totalprice(basket);
			
			if (sel.equalsIgnoreCase("1")) {
				
				System.out.println("카드 번호를 입력해주세요");
				System.out.print("카드 번호: ");
				input = scan.nextLine();

				if (input.length() - input.replace("-", "").length() == 3) {
					System.out.println("잘못입력하셨습니다.");
					return;
				}

				PayInfo cardpayinfo = new PayInfo();
				
				cardpayinfo.setPrice(price);
				cardpayinfo.setNum(input);
				cardpayinfo.setPayOption(PayOption.CARD);
				cardpayinfo.setStatus(true);
				order.setPayInfo(cardpayinfo);
				
				orderAdd(order);
				
				pause("결제가 완료되었습니다.");

			} else if (sel.equalsIgnoreCase("2")) {// 무통장 거래
				
				System.out.println("24시간 이내에 해당 계좌로 입금해주세요");
				System.out.printf("총 금액 : %d\r\n", price);
				System.out.println("임금 계죄 : 123456-12-123456");
				
				PayInfo bankBookPayInfo = new PayInfo();
				
				bankBookPayInfo.setPrice(price);
				bankBookPayInfo.setNum("123456-12-123456");
				bankBookPayInfo.setPayOption(PayOption.BANKBOOK);
				bankBookPayInfo.setStatus(false);
				order.setPayInfo(bankBookPayInfo);
				
				orderAdd(order);
				
				pause("결제가 완료되었습니다.");
				
			}
		}
	}
	
	
   /**
    * 장바구니 물건들의 총 가격을 구하는 메서드입니다.
    * @param basket 장바구니
    * @return price 총가격
    */
	private static int totalprice(ArrayList<ItemTag> basket) {
		int price = 0;
		for (int b = 0; b < basket.size(); b++) {
			String itemCode = basket.get(b).getItemCode();
			if (itemTagRepository.containsKey(itemCode)) {
				price += itemTagRepository.get(itemCode).getPrice();
			}
		}
		return price;
	}

	
	/**
	 * 주문 정보를 추가하는 메서드 입니다.
	 * @param order 주문정보
	 * @author 엄윤섭
	 */
	private static void orderAdd(Order order) {
		OrderTimeStamp timeStamp = new OrderTimeStamp();
		Calendar thisTime = Calendar.getInstance();

		timeStamp.setOrderTime(thisTime);

		order.setStatus(OrderStatus.DELIVERY);
		
		order.setTimeStamp(timeStamp);

		orderRepository.add(order);

		for (int i = 0; i < basket.size(); i++) {
			String itemCode = basket.get(i).getItemCode();
			if (itemTagRepository.containsKey(itemCode)) {
				int co = itemTagRepository.get(itemCode).getCount() - basket.get(i).getCount();
				itemTagRepository.get(itemCode).setCount(co);
			}
		}
		
	}

	
	/**
	 * 주문상품정보를 리스트에 추가하는 메서드입니다.
	 * @param basket 장바구니
	 * @param orderCode 주문코드
	 * @param orderItemTag 주문 상품 정보
	 * @author 엄윤섭
	 */
	private static void OrderAdd(ArrayList<ItemTag> basket, long orderCode, ArrayList<OrderItemTag> orderItemTag) {
		for (int i = 0; i < basket.size(); i++) {

			OrderItemTag orderItemTagArray = new OrderItemTag();

			orderItemTagArray.setItemCode(basket.get(i).getItemCode());// 주문코드
			orderItemTagArray.setCount(basket.get(i).getCount());
			orderItemTagArray.setPrice(basket.get(i).getPrice());
			orderItemTagArray.setOrderCode(orderCode);// 코드

			orderItemTag.add(orderItemTagArray);
		}
	}
	
	
	
	
	//로그인
	
	/**
	 * 로그인 화면을 출력하는 View 메서드입니다.
	 * 
	 * @param map 입력정보
	 * @param message1 출력 시스템 메세지 1
	 * @param message2 출력 시스템 메세지 2
	 * 
	 * @author 조진욱
	 */
	private static void loginView (HashMap<String, String> map, String message1, String message2) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n\n");
		
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                               로그인                               |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message1.equalsIgnoreCase("") ? "" : "* ") + message1 + (message1.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message2.equalsIgnoreCase("") ? "" : "* ") + message2 + (message2.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           아이디     :   " + fixLeftString(map.get("id") == null ? "" : map.get("id"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           비밀번호   :   " + fixLeftString(map.get("password") == null ? "" : map.get("password").replaceAll("[a-zA-Z0-9!@#$%]", "*"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		
		viewLineCount += 23;
		System.out.println(sb.toString());
	}
	
	
	/**
	 * 로그인스크린
	 * @author 서민종
	 * 로그인과 관련된 로그인, 회원가입, 아이디 찾기, 비밀번호 찾기를 제공합니다.
	 */
	private static void loginScreen() {

		/*
		 * 요구사항 RQ-04-00-00 ~
		 */
		
		String input = "";
		
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> result = new HashMap<String, String>();
		String message1 = "";
		String message2 = "";
		String message3 = "1.로그인 2.회원가입 3.아이디찾기 4.비밀번호찾기";
		
		if (sel.equalsIgnoreCase("order")) {
			message3 = "1.로그인 2.비회원주문";
		}
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			if (result.get("id") == null
				|| (result.get("newPassword") != null && result.get("newPassword2") != null)) {
				
				titleView("로그인");
				topMessageView("");
				loginView(map, message1, message2);
				fixNewLineView();
				selectView(message3);
				input = getInput();
			}
			
			if (input.equalsIgnoreCase("1")) {
				
				loginIdInput();
				
			} else if (input.equalsIgnoreCase("2") && !sel.equalsIgnoreCase("order")) {
				
				joinScreen();
				
			} else if (input.equalsIgnoreCase("2") && sel.equalsIgnoreCase("order")) {
				
				sel = "pass";
				return;
				
			} else if (input.equalsIgnoreCase("3") && !sel.equalsIgnoreCase("order")) {
				
				result = idSearchNameInput(map);
				
			} else if (input.equalsIgnoreCase("4") && !sel.equalsIgnoreCase("order")) {
				
				result = passwordSearchIdInput(map);
				
			} else if (input.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
				
				sel = input;
				return;
				
			} else {
				
				message1 = "잘못된 입력입니다.";
				message2 = "입력값 : " + input;
			}
		}
	}
	
	
	/**
	 * 로그인
	 * 회원정보를 입력받아 회원일치여부를 확인하고 loginMemberCode를 설정함으로써 회원자격으로 다양한페이지를 이용할 수 있도록 합니다.
	 * 
	 * @author 서민종
	 */
	private static void loginIdInput() {

		HashMap<String, String> map = new HashMap<String, String>();
		String message1 = "";
		String message2 = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("로그인");
			topMessageView("");
			loginView(map, message1, message2);
			viewLineCount++;
			fixNewLineView();
			messageView("");
			selectView("아이디를 입력해주세요.");
			sel = getInput();

			
			if (idValid(sel)) {

				map.put("id", sel);

				message1 = "";
				
				map = loginPasswordInput(map);
			
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
			
				return;
						
			} else if (!idValid(sel)) {
				
				
			} else {
					
				map.put("id", sel);
				message1 = "형식이 올바르지 않습니다.";
				message2 = "(영문, 숫자 4 ~ 16자)";
			}
		}
	}
	
	
	private static HashMap<String, String> loginPasswordInput(HashMap<String, String> map) {

		Member member = getMember(map.get("id"));
		String message1 = "";
		String message2 = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("로그인");
			topMessageView("");
			loginView(map, message1, message2);
			viewLineCount++;
			fixNewLineView();
			messageView("비밀번호를 입력해주세요.");
			selectView("r.이전");
			sel = getInput();

			map.put("password", sel);
			
			if (member != null && member.getPassword().equalsIgnoreCase(sel)) {
				
				message1 = "";
				
				loginMemberCode = member.getCode();
				sel = "m";
				return map;
				
			} else if (sel.equalsIgnoreCase("r")) {

				map.put("id", null);
				return map;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return map;
				
			} else if (member == null) {
				
				message1 = "가입되지 않은 아이디입니다.";
				
			} else {

				message1 = "아이디와 비밀번호가 일치하지 않습니다.";
			}
		}
		
		return map;
	}


	
	
	//아이디찾기
	
	/**
	 * 아이디 찾기 화면을 출력하는 View 메서드입니다.
	 * 
	 * @param map 입력정보
	 * @param message1 출력 시스템 메세지 1
	 * @param message2 출력 시스템 메세지 2
	 * 
	 * @author 조진욱
	 */
	private static void idSearchView (HashMap<String, String> map, String message1, String message2) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n\n");
		
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                             아이디찾기                             |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message1.equalsIgnoreCase("") ? "" : "* ") + message1 + (message1.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message2.equalsIgnoreCase("") ? "" : "* ") + message2 + (message2.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           이름       :   " + fixLeftString(map.get("name") == null ? "" : map.get("name"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           전화번호   :   " + fixLeftString(map.get("tel") == null ? "" : map.get("tel"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		
		viewLineCount += 23;
		System.out.println(sb.toString());
	}
	

	private static HashMap<String, String> idSearchNameInput(HashMap<String, String> map) {

	      HashMap<String, String> result = map;
	      String message1 = "";
	      String message2 = "";
	      String message3 = "이름을 입력해주세요.";
	      
	      while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

	         titleView("아이디찾기");
	         topMessageView("");
	         idSearchView(result, message1, message2);
	         fixNewLineView();
	         messageView("");
	         selectView(message3);
	         sel = getInput();

	         if (map.get("id") == null) {
	            
	            result.put("name", sel);
	            
	            if (nameValid(sel)) {
	   
	               result = idSearchTelInput(result);
	               
	               if (result.get("id") != null) {
	                  message1 = "아이디 찾기 성공";
	                  message2 = map.get("id");
	                  message3 = "1.비밀번호찾기 2.로그인";
	               }
	               
	            } else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
	   
	               return map;
	   
	            } else {
	   
	               message1 = "형식이 올바르지 않습니다.";
	               message2 = "(한글 2 ~ 8자)";
	            }
	         
	         } else {
	            
	            if (Pattern.matches("^[12]$", sel)) {
	               
	               sel = sel.equalsIgnoreCase("1") ? "4" : "1";
	               HashMap<String, String> reMap = new HashMap<String, String>();
	               reMap.put("id", result.get("id"));
	               return reMap;
	               
	            } else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
	               
	               return map;

	            } else {
	               
	               message3 = "잘못된 입력입니다.";
	            }
	            
	         } 
	      }
	      
	      return map;
	   }


	private static HashMap<String, String> idSearchTelInput(HashMap<String, String> map) {

		HashMap<String, String> result = map;
		
		String message1 = "";
		String message2 = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("title");
			topMessageView("");
			idSearchView(result, message1, message2);
			fixNewLineView();
			messageView("");
			selectView("전화번호를 입력해주세요.");
			sel = getInput();

			result.put("tel", sel.trim());
			
			if (telValid(sel)) {

				ArrayList<Member> members = getMembersByName(map.get("name"));
				
				if (members != null) {
					
					for (Member member : members) {
						
						if (member.getTel().replace("-", "").equalsIgnoreCase(result.get("tel").replace("-", ""))) {
							
							result.put("id", member.getId());
							return result;
							
						} else {
							
							message1 = "일치하는 회원정보가 없습니다.";
						}
					}
				}

			} else if (sel.equalsIgnoreCase("2")) {

				//logic

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return map;

			} else {

				message1 = "형식이 올바르지 않습니다.";
				message1 = "(010-1234-5678, '-'생략가능)";
			}
		}
		
		return map;
	}


	

	//비밀번호찾기
	
	/**
	 * 비밀번호 찾기 화면을 출력하는 View 메서드입니다.
	 * 
	 * @param map 입력정보
	 * @param message1 출력 시스템 메세지 1
	 * @param message2 출력 시스템 메세지 2
	 * 
	 * @author 조진욱
	 */
	private static void passwordSearchView (HashMap<String, String> map, String message1, String message2) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n\n");
		
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                            비밀번호찾기                            |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message1.equalsIgnoreCase("") ? "" : "* ") + message1 + (message1.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message2.equalsIgnoreCase("") ? "" : "* ") + message2 + (message2.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           아이디     :   " + fixLeftString(map.get("id") == null ? "" : map.get("id"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           전화번호   :   " + fixLeftString(map.get("tel") == null ? "" : map.get("tel"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		
		viewLineCount += 23;
		System.out.println(sb.toString());
	}
	
	
	private static HashMap<String, String> passwordSearchIdInput(HashMap<String, String> map) {

		HashMap<String, String> result = new HashMap<String, String>();
		
		String message1 = "";
		String message2 = "";
		String message3 = "아이디를 입력해주세요.";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))
				|| (result.get("newPassword") != null && result.get("newPassword2") != null)) {

			titleView("비밀번호찾기");
			topMessageView("");
			passwordSearchView(result, message1, message2);
			fixNewLineView();
			messageView("");
			selectView(message3);
			sel = getInput();

			if (result.get("newPassword") == null) {
				
				if (idValid(sel)) {
	
					result.put("id", sel);
					result = passwordSearchTelInput(result);
					
				} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
	
					return map;
	
				} else {
	
					map.put("id", sel);
					message1 = "형식이 올바르지 않습니다.";
					message2 = "(영문, 숫자 4 ~ 16자)";
				}
		
			}
		}
		return map;
	}
	
	
	private static HashMap<String, String> passwordSearchTelInput(HashMap<String, String> map) {

		HashMap<String, String> result = map;
		
		String message1 = "";
		String message2 = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("title");
			topMessageView("");
			passwordSearchView(result, message1, message2);
			fixNewLineView();
			messageView("");
			selectView("전화번호를 입력해주세요.");
			sel = getInput();

			result.put("tel", sel.trim());
			
			if (telValid(sel)) {

				Member member = getMember(map.get("id"));
				
				if (member != null
					&& member.getTel().replace("-", "").equalsIgnoreCase(result.get("tel").replace("-", ""))) {
					
					sel = "1";
					return setNewPassword(result);
							
				} else if (member == null) {
						
					message1 = "가입되지 않은 아이디입니다.";
					
				} else {
					
					message1 = "등록된 정보와 전화번호가 일치하지 않습니다.";
					
				}

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return map;

			} else {

				message1 = "형식이 올바르지 않습니다.";
				message1 = "(010-1234-5678, '-'생략가능)";
			}
		}
		
		return map;
	}

	
	/**
	 * 비밀번호 재설정 화면을 출력하는 View 메서드입니다.
	 * 
	 * @param map 입력정보
	 * @param message1 출력 시스템 메세지 1
	 * @param message2 출력 시스템 메세지 2
	 * 
	 * @author 조진욱
	 */
	private static void passwordSetView (HashMap<String, String> map, String message1, String message2) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n\n");
		
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                          비밀번호 재설정                           |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message1.equalsIgnoreCase("") ? "" : "* ") + message1 + (message1.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|" + fixCenterString((message2.equalsIgnoreCase("") ? "" : "* ") + message2 + (message2.equalsIgnoreCase("") ? "" : " *"), 68) +"|", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|           비밀번호   :   " + fixLeftString(map.get("newPassword") == null ? "" : map.get("newPassword").replaceAll("[a-zA-Z0-9!@#$%]", "*"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|         비밀번호확인 :   " + fixLeftString(map.get("newPassword2") == null ? "" : map.get("newPassword").replaceAll("[a-zA-Z0-9!@#$%]", "*"), 20) + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|      ---------------------------------------------------------     |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		
		viewLineCount += 23;
		System.out.println(sb.toString());
	}
	
	
	private static HashMap<String, String> setNewPassword(HashMap<String, String> map) {
		
		HashMap<String, String> result = map;
		
		String message1 = "";
		String message2 = "";
		String message3 = "새로운 비밀번호를 입력해주세요.";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("비밀번호 재설정");
			topMessageView("");
			passwordSetView(result, message1, message2);
			fixNewLineView();
			messageView("");
			selectView(message3);
			sel = getInput();

			if (result.get("newPassword") != null && result.get("newPassword2") != null) {
				sel = "1";
				return result;
			}
			
			if (passwordValid(sel)) {
				
				message1 = "";
				message2 = "";
				
				if (result.get("newPassword") == null) {
					
					result.put("newPassword", sel);
					message3 = "비밀번호를 다시 입력해주세요.";
					
				} else {
					
					result.put("newPassword2", sel);
					
					if (!sel.equalsIgnoreCase(result.get("newPassword"))) {
						
						message1 = "비밀번호가 일치하지 않습니다.";
						message3 = "새로운 비밀번호를 입력해주세요.";
						result.put("newPassword", null);
						
						
					} else if (sel.equals(getMember(result.get("id")))) {
						
						message1 = "기존 비밀번호와 동일합니다.";
						message3 = "새로운 비밀번호를 입력해주세요.";
						result.put("newPassword", null);
						
					} else {
						
						setPassword(result.get("id"), result.get("newPassword"));
						message1 = "비밀번호 재설정 완료";
						message2 = "로그인 화면으로 이동합니다.";
						message3 = "엔터를 입력하여 진행";
						sel = "1";
					}
				}
				

			} else if (sel.equalsIgnoreCase("2")) {

				//logic

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return map;

			} else {

				message1 = "형식이 올바르지 않습니다.";
				message2 = "영문, 숫자, !@#$%^ 중 1자 이상 포함 8 ~ 16자";
				message3 = "새로운 비밀번호를 입력해주세요.";
				result.put("newPassword", null);
			}
		}
		
		return map;
	}


	private static void setPassword(String memberId, String password) {
		
		int count = 0;
		for (Member member : memberRepository) {
			
			if (member.getId().equalsIgnoreCase(memberId)) {
				member.setPassword(password);
				memberRepository.set(count, member);
			}
			count++;
		}
	}


	/**
	 * 아이디 찾기
	 * @author 서민종
	 * @return sel값을 갖고 return하여 회원이 원하는 페이지로 바로 이동할 수 있는 기능을 제공합니다.
	 * 회원정보를 입력받아 회원일치여부를 확인하고 가입된 아이디를 출력해주는 기능을 합니다.
	 */
	private static String idSearch() {

		/*
		 * 요구사항 RQ-04-30-00 ~
		 */
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			String name = "";
			String tel = "";
			boolean flag = true;
			boolean idPass = false;
			boolean telPass = false;
			int count = 0;

			while (flag) {
				System.out.print("이름 입력: ");
				name = scan.nextLine();
				count = 0;
				if (name.length() >= 2 && name.length() <= 8) {
					for (int i = 0; i < name.length(); i++) {
						char c = name.charAt(i);
						if (c > '가' && c < '힣') {
							count++;
						}
					}
					if (name.length() == count) {
						idPass = true;
						break;
					}
				}
				System.out.println("형식이 올바르지 않습니다(한글 2~8자)");
				System.out.println("이름 입력을 계속 진행하시겠습니까?(Y/N)");
				System.out.print("입력: ");
				String input = scan.nextLine();
				if (input.equalsIgnoreCase("y")) {
				} else if (input.equalsIgnoreCase("n")) {
					sel = "";
					return sel;
				} else {
					System.out.println("\n잘못된 입력입니다.\n");
				}
			}

			while (flag) {
				System.out.print("전화번호 입력: ");
				tel = scan.nextLine();
				tel = tel.replace("-", "");
				count = 0;
				if (tel.length() == 11) {
					for (int i = 0; i < tel.length(); i++) {
						char c = tel.charAt(i);
						if (c >= '0' && c <= '9') {
							count++;
						}
					}
				}
				if (tel.length() == count) {
					telPass = true;
					break;
				}
				System.out.println("전화번호의 형식이 올바르지 않습니다.");
				System.out.println("(000-0000-0000, '-'생략가능, 숫자만 입력)");
				System.out.println("전화번호 입력을 계속 진행하시겠습니까?(Y/N)");
				System.out.print("입력: ");
				String input = scan.nextLine();
				if (input.equalsIgnoreCase("y")) {
				} else if (input.equalsIgnoreCase("n")) {
					sel = "";
					return sel;
				} else {
					System.out.println("\n잘못된 입력입니다.\n");
				}
			}
			pause("검색중입니다...");

			if (idPass && telPass) {
				for (int i = 0; i < memberRepository.size(); i++) {
					if (name.equalsIgnoreCase(memberRepository.get(i).getName()) && tel.equalsIgnoreCase(memberRepository.get(i).getTel().replace("-", ""))) {

						System.out.println("아이디 찾기 성공");
						System.out.printf("회원님의 Id는 %s입니다.\r\n", memberRepository.get(i).getId());

						while (flag) {
							System.out.println("1.비밀번호 찾기 2.로그인");
							System.out.println("u.이전 단계 m.메인 q.종료");
							System.out.print("입력: ");
							sel = scan.nextLine();

							if (sel.equalsIgnoreCase("1")) {
								sel = "4";
								return sel;
							} else if (sel.equalsIgnoreCase("2")) {
								sel = "1";
								return sel;
							} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
								return sel;
							} else {
								System.out.println("\n잘못된 입력입니다.\n");
							}
						}
					}
				}

				while (flag) {
					System.out.println("일치하는 회원정보가 없습니다.");
					System.out.println("1.아이디 찾기 2.로그인 m.메인 q.종료");
					System.out.println("u.이전 단계 m.메인 q.종료");
					System.out.print("입력: ");
					sel = scan.nextLine();

					if (sel.equalsIgnoreCase("1")) {
						sel = "3";
						return sel;
					} else if (sel.equalsIgnoreCase("2")) {
						sel = "1";
						return sel;
					} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
						return sel;
					} else {
						System.out.println("\n잘못된 입력입니다.\n");
					}
				}
			}
		}
		return sel;
	}
	
	
	/**
	 * 비밀번호 찾기
	 * @author 서민종
	 * @return sel값을 갖고 return하여 회원이 원하는 페이지로 바로 이동할 수 있는 기능을 제공합니다.
	 * 회원정보를 입력받아 회원일치여부를 확인하고 가입된 비밀번호를 출력해주는 기능을 합니다.
	 */
	private static String passwordSearch() {

		/*
		 * 요구사항 RQ-04-40-00 ~
		 */
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			String id = "";
			String tel = "";
			boolean flag = true;
			boolean idPass = false;
			boolean telPass = false;
			boolean memberPass = false;
			int memberGetIndex = 0;

			HashMap<String, String> memberCheck = new HashMap<String, String>();

			for (int i = 0; i < memberRepository.size(); i++) {
				memberCheck.put(memberRepository.get(i).getId(), memberRepository.get(i).getTel());
			}

			while (flag) {
				System.out.print("\nId: ");
				id = scan.nextLine();

				if (memberCheck.containsKey(id)) {
					idPass = true;
					
					System.out.print("\nTel: ");
					tel = scan.nextLine();
					tel = tel.replace("-", "");
					
					if (tel.equalsIgnoreCase(memberCheck.get(id).replace("-", ""))) {
						telPass = true;
						memberPass = true;
						pause("회원여부 검색중...");
						break;
					} else {
						while (flag) {
							System.out.println("\n등록된 정보와 전화번호가 일치하지 않습니다.");
							System.out.println("1.비밀번호 찾기 2.로그인");
							System.out.println("u.이전 단계 m.메인 q.종료");
							System.out.print("입력: ");
							sel = scan.nextLine();
							if (sel.equalsIgnoreCase("1")) {
								sel = "4";
								return sel;
							} else if (sel.equalsIgnoreCase("2")) {
								sel = "1";
								return sel;
							} else if (sel.equalsIgnoreCase("u")||
									sel.equalsIgnoreCase("m") ||
									sel.equalsIgnoreCase("q")) {
								return sel;
							} else {
								System.out.println("\n잘못된 입력입니다.\n");
							}
						}
					}
					
					
				} else {
					while (flag) {
						System.out.println("\n일치하는 Id가 없습니다.");
						System.out.println("1.회원가입 2.비밀번호 찾기");
						System.out.println("u.이전 단계 m.메인 q.종료");
						System.out.print("입력: ");
						sel = scan.nextLine();
						if (sel.equalsIgnoreCase("1")) {
							sel = "2";
							return sel;
						} else if (sel.equalsIgnoreCase("2")) {
							sel = "4";
							return sel;
						} else if (sel.equalsIgnoreCase("u") ||
								sel.equalsIgnoreCase("m") ||
								sel.equalsIgnoreCase("q")) {
							return sel;
						} else {
							System.out.println("\n잘못된 입력입니다.\n");
						}
					}
				}
			}
			
			
			
			if (idPass && telPass && memberPass) {
				
				String newPassword = "";
				int count1 = 0;
				int count2 = 0;
				char c = 0;
				boolean stop = false;
				
				for (int i = 0; i<memberRepository.size(); i++) {
					if (id.equalsIgnoreCase(memberRepository.get(i).getId())) {
						memberGetIndex = i;
					}
				}
				while (flag) {
					System.out.print("\n새로운 비밀번호 입력(u.이전 단계 m.메인 q.종료): ");
					newPassword = scan.nextLine();
					
					if(newPassword.equalsIgnoreCase("u")
							|| newPassword.equalsIgnoreCase("m")
							|| newPassword.equalsIgnoreCase("q")) {
						sel = newPassword;
						return sel;
					} else {
						
						if (!(memberRepository.get(memberGetIndex).getPassword().equalsIgnoreCase(newPassword))) {
							
							if (newPassword.length() >= 8 && newPassword.length() <= 16) {
								for (int i = 0; i < newPassword.length(); i++) {
									c = newPassword.charAt(i);
									if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%') {
										count1++;
									}
								}
								for (int i = 0; i < newPassword.length(); i++) {
									c = newPassword.charAt(i);
									if (count1 != 0) {
										if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' 
												|| c >= '0' && c <= '9' || c == '!' || c == '@' 
												|| c == '#' || c == '$' || c == '%') {
											count2++;
											stop = true;
										}
									} else {
										System.out.println("특수문자 !@#$%중 1자 이상 포함");
										break;
									}
									
								}
								if (stop) {
									if (count2 == newPassword.length()) {
										while (memberPass) {
											System.out.print("\n새로운 비밀번호 확인 입력(u.이전 m.메인 q.종료): ");
											String newPasswordPass = scan.nextLine();
											
											if(newPasswordPass.equalsIgnoreCase("u")
													|| newPasswordPass.equalsIgnoreCase("m")
													|| newPasswordPass.equalsIgnoreCase("q")) {
												sel = newPasswordPass;
												return sel;
											} else {
												if(newPasswordPass.equalsIgnoreCase(newPassword)) {
													memberPass = false;
													memberRepository.get(memberGetIndex).setPassword(newPasswordPass);
													pause("비밀번호 재설정을 완료하였습니다. 로그인화면으로 이동합니다.");
													sel = "";
													return sel;
												} else {
													System.out.println("일치하지 않습니다. 다시 입력해주시오.");
												}
											}
										}//while
									} else {
										System.out.println("영문, 숫자, 특수문자 외 문자가 포함되었습니다.");
									}
								}
							} else {
								System.out.println("영문, 숫자, 특수문자 8자 ~ 16자 입력");
							}
						} else {
							System.out.println("기존비밀번호와 동일합니다.");
						}
					}
				}
			}
		}
		return sel;
	}
	
	
	
	
	//회원가입
	
	/**
	 * 회원가입 화면을 출력하는 View 메서드입니다.
	 * 
	 * @param member 회원객체
	 * 
	 * @author 조진욱
	 */
	private static void memberJoinView(Member member) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(VERTICAL);
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append("| " + fixLeftString("   이름", 20) + ":    " + fixLeftString(member.getName(), 141) + " |\n");
		sb.append("| " + fixLeftString("   아이디", 20) + ":    " + fixLeftString(member.getId(), 141) + " |\n");
		sb.append("| " + fixLeftString("   패스워드", 20) + ":    " + fixLeftString(member.getPassword().replaceAll("[a-zA-Z0-9\"!@#$%\"]", "*"), 141) + " |\n");
		sb.append("| " + fixLeftString("   생년월일", 20) + ":    " + fixLeftString(member.getBirth(), 141) + " |\n");
		sb.append("| " + fixLeftString("   전화번호", 20) + ":    " + fixLeftString(member.getTel(), 141) + " |\n");
		sb.append("| " + fixLeftString("   주소", 20) + ":    " + fixLeftString(member.getAddress(), 141) + " |\n");
		sb.append("| " + fixLeftString("", 165) + " |\n");
		sb.append(VERTICAL);
		
		viewLineCount += 10;
		System.out.println(sb.toString());
	}
	
	
	/**
	 * 회원가입 화면을 출력하는 Screen 메서드입니다.
	 * 
	 * @author 조진욱
	 */
	private static void joinScreen() {

		/*
		요구사항 RQ-04-20-00 ~
		 */
		
		Member member = new Member();
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			Member result = joinInputName(member);

			if (sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u")) {
				
				return;
			
			} else if (!result.getAddress().equalsIgnoreCase("")) {

				long lastCode = 0;
				for (Member m : memberRepository) {
					
					lastCode = lastCode < m.getCode() ? m.getCode() : lastCode;
				}
				
				result.setCode(lastCode + 1);
				result.setGrade(Grade.STANDARD);

				memberRepository.add(result);
				
				pause("회원가입 완료");
				
				sel = "3";
				return;
				
			} else {
				
				pause("잘못된 입력입니다.");
			}
		}
	}
	
	
	private static Member joinInputName(Member member) {

		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) && member.getAddress().equalsIgnoreCase("")) {
			
			titleView("회원가입");
			topMessageView("");
			memberJoinView(member);
			fixNewLineView();
			messageView("이름을 입력해주세요. (한글 2 ~ 8자)");
			selectView("r.이전");
			input = getInput();
			
			if (nameValid(input)) {
				
				result.setName(input);
				result = joinInputId(result);
				
			} else if (input.equalsIgnoreCase("r")) {
				
				sel = "u";
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", input));
			}
		}
		
		return result;
	}
	
	
	private static Member joinInputId(Member member) {
		
		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) && member.getAddress().equalsIgnoreCase("")) {
			
			titleView("회원가입");
			topMessageView("");
			memberJoinView(member);
			fixNewLineView();
			messageView("아이디를 입력해주세요. (영문, 숫자 4 ~ 16자)");
			selectView("r.이전");
			input = getInput();
			
			if (idValid(input) && !isExistId(input)) {

				result.setId(input.toLowerCase());
				result = joinInputPassword(result);
				
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setName("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else if (isExistId(input)) {
				
				pause(String.format("이미 가입된 아이디입니다. 다른 아이디를 입력해주세요.", input));
				
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", input));
			}
		}
		
		return result;
	}
	

	private static Member joinInputPassword(Member member) {
		
		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) && member.getAddress().equalsIgnoreCase("")) {
			
			titleView("회원가입");
			topMessageView("");
			memberJoinView(member);
			fixNewLineView();
			messageView("비밀번호를 입력해주세요. (영문, 숫자, 특수문자 !@#$% 중 2개를 포함하여 8 ~ 16자)");
			selectView("r.이전");
			input = getInput();
			
			if (passwordValid(input)) {

				result.setPassword(input);
				result = joinInputBirth(result);
				
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setId("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", input));
			}
		}
		
		return result;
	}
	

	private static Member joinInputBirth(Member member) {
		
		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) && member.getAddress().equalsIgnoreCase("")) {
			
			titleView("회원가입");
			memberJoinView(member);
			topMessageView("");
			fixNewLineView();
			messageView("생년월일을 입력해주세요. (형식 : YYYY-MM-DD, 띄어쓰기 없음)");
			selectView("r.이전");
			input = getInput();
			
			if (birthValid(input)) {

				result.setBirth(input);
				result = joinInputTel(result);
				
					
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setPassword("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", input));
			}
		}
		
		return result;
	}
	
	
	private static Member joinInputTel(Member member) {
		
		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) && member.getAddress().equalsIgnoreCase("")) {
			
			titleView("회원가입");
			topMessageView("");
			memberJoinView(member);
			fixNewLineView();
			messageView("전화번호를 입력해주세요. (형식 : 010-1234-5678, 띄어쓰기 없음)");
			selectView("r.이전");
			input = getInput();
			
			if (telValid(input)) {

				result.setTel(input);
				result = joinInputAddress(result);
					
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setBirth("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", input));
			}
		}
		
		return result;
	}
	
	
	private static Member joinInputAddress(Member member) {

		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) && member.getAddress().equalsIgnoreCase("")) {
			
			titleView("회원가입");
			topMessageView("");
			memberJoinView(member);
			fixNewLineView();
			messageView("주소를 입력해주세요. (예 : 서울특별시 동작구 서초동, 경기도 광주시 태전동)");
			selectView("r.이전");
			input = getInput();
			
			if (addressValid(input)) {

				result.setAddress(input);
				result = joinInputCheck(result);
				
					
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setTel("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				System.out.println();
				System.out.printf("잘못된 입력입니다. 입력값 : %s", input);
			}
		}
		
		return result;
	}
	
	
	private static Member joinInputCheck(Member member) {
		
		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("회원가입");
			topMessageView("");
			memberJoinView(member);
			fixNewLineView();
			messageView("위 정보로 가입하시겠습니까?");
			selectView("y.예 r.이전");
			input = getInput();
			
			if (input.equalsIgnoreCase("y")) {
				
				break;
					
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setAddress("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				System.out.println();
				System.out.printf("잘못된 입력입니다. 입력값 : %s", input);
			}
		}
		
		return result;
	}
	

	
	
	//내정보

	/**
	 * 내정보
	 * @author 서민종
	 * 내정보를 출력하여 사용자 확인을 돕고 주문내역 및 회원정보 수정으로 이동할 수 있는 기능을 합니다.
	 */
	private static void myInfoScreen() {

		/*
		 * 요구사항 RQ-06-00-00 ~
		 */

		ArrayList<Order> orders = getOrderByMemberCode();
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("내정보");
			topMessageView("");
			memberJoinView(getMember(loginMemberCode));
			fixNewLineView();
			messageView("");
			selectView("1.주문내역 2.정보수정");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {

				myOrderScreen();
				
			} else if (sel.equalsIgnoreCase("2")) {

				myInfoChangeScreen();
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			
			boolean flag = true;
			
			System.out.println("-----------------------");
			System.out.println("\t내정보");
			System.out.println("-----------------------");
			
			System.out.printf("아이디: %s\r\n", memberRepository.get((int)loginMemberCode-1).getId());
			System.out.printf("이름: %s\r\n", memberRepository.get((int)loginMemberCode-1).getName());
			System.out.printf("생년월일: %s\r\n", memberRepository.get((int)loginMemberCode-1).getBirth());
			System.out.printf("전화번호: %s\r\n", memberRepository.get((int)loginMemberCode-1).getTel());
			System.out.printf("주소: %s\r\n", memberRepository.get((int)loginMemberCode-1).getAddress());
			
			
			System.out.println("1.주문내역 2.회원정보 수정 m.메인 q.종료");
			System.out.print("입력: ");
			sel = scan.nextLine();
			
			if (sel.equalsIgnoreCase("1")) {
				myOrderScreen();
			} else if (sel.equalsIgnoreCase("2")) {
				myInfoChangeScreen();
			} else if (sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
				return;
			} else {
				System.out.println("\n잘못된 입력입니다.\n");
			}
		}
	}
	
	
	private static ArrayList<Order> getOrderByMemberCode() {
		
		ArrayList<Order> orders = new ArrayList<Order>();
		
		for (Order order : orderRepository) {
			
			if (order.getMemberCode() == loginMemberCode) {
				orders.add(order);
			}
		}
		
		return orders;
	}


	private static void myOrderScreen() {
		
		/*
		요구사항 RQ-08-00-00 ~
		*/
		
		ArrayList<Order> orders = getMyOrder();
		
		int page = 1;
		boolean isNotLast = true;
		
		while(!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("주문관리");
			topMessageView("");
			isNotLast = orderListView(orders, 1);
			fixNewLineView();
			messageView("");
			selectView("");
			sel = getInput();
			
			if (page == 1 && !isNotLast) {
				selectView("");
			} else if (page == 1) {
				selectView("1.다음페이지");
			} else if (isNotLast) {
				selectView("1.다음페이지 2.이전페이지");
			} else {
				selectView("1.이전페이지");
			}
			
			sel = getInput();
			
			if (sharpNumValid(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				adminOrderDetailScreen(orders.get(index));
			
			} else if (sel.equalsIgnoreCase("1") && !(page == 1 && !isNotLast)) {
				
				page += isNotLast ? 1 : -1;

			} else if (sel.equalsIgnoreCase("2") && page != 1 && !(page == 1 && !isNotLast)) {

				page --;

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;
						
			} else {
					
				System.out.println();
				pause("잘못된 입력입니다.");
			}			
		}
	}

	
	/**
	 * 회원주문내역
	 * @author 서민종
	 * 회원의 주문내역을 확인하고 주문상세검색을 지원합니다.
	 */
	private static void orderHistoryScreen() {

		/*
		 * 요구사항 RQ-06-10-00 ~
		 */
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			boolean flag = true;
			ArrayList<Order> orders = new ArrayList<Order>();
			
			for(int i = 0; i < orderRepository.size(); i++) {
				if(orderRepository.get(i).getMemberCode() == 375) {
					orders.add(orderRepository.get(i));
				}
			}
			
			
			for(int i = 0; i < orders.size(); i++) {
				
				for(int j = 0; j < orders.get(i).getItemInfo().size(); j++) {
					
					for (int k = 0; k < itemRepository.size(); k++) {
						
						if(orders.get(i).getItemInfo().get(j).getItemCode().equalsIgnoreCase(itemRepository.get(k).getCode())) {
							//주문 시점, 주문코드, 상품코드, 브랜드명, 상품명, 수량, 가격, 진행상태 이전r 메인m 종료q
							StringBuilder sb = new StringBuilder();
							int startIndex = 1;
							sb.append(String.format("[%s][%s][%s][%s][%s][%s][%,d][%s]\n"
									, fixCenterString(startIndex+"", 4)
									, fixCenterString(orders.get(i).getCode()+"", 4)
									, fixCenterString(orders.get(i).getItemInfo().get(j).getItemCode(), 30)
									, fixCenterString(itemRepository.get(k).getBrand(), 16)
									, fixCenterString(itemRepository.get(k).getName(), 50)
									, fixCenterString(orders.get(i).getItemInfo().get(j).getCount()+"", 10)
									, Integer.parseInt(orders.get(i).getItemInfo().get(j).getPrice()+"", 10)
									, fixCenterString(orders.get(i).getStatus().getString(), 10)));
							startIndex++;
							System.out.println(sb.toString());
						}
					}
				}
			}
		
			while(flag) {
				System.out.println("1.주문 상세 r.이전 m.메인 q.종료");
				System.out.print("입력: ");
				sel = scan.nextLine();
				if(sel.equalsIgnoreCase("1")) {
					System.out.print("주문코드 입력: ");
					String input = (scan.nextLine());
					orderDetailSelecter(getOrder(Long.parseLong(input)));
				} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
					return;
				}
			}
		}
	}
	

	/**
	 * 회원정보수정
	 * @author 서민종
	 * 회원이 자신의 정보를 확인하고 항목별로 선택하여 수정할 수 있는 기능을 제공합니다.
	 */
	private static void myInfoChangeScreen2() {

		/*
		 * 요구사항 RQ-06-20-00 ~
		 */
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			boolean flag1 = true;
			boolean flag2 = true;
			String passwordOutput = "";
			String password = memberRepository.get((int)loginMemberCode-1).getPassword();
			String tel = memberRepository.get((int)loginMemberCode-1).getTel();
			String address = memberRepository.get((int)loginMemberCode-1).getAddress();
			
			for (int i = 0; i<password.length(); i++) {
				passwordOutput += "*";
			}
			
			while(flag1) {
				
				System.out.printf("-----------------------------------");
				System.out.printf("\n아이디: %s\n", memberRepository.get((int)loginMemberCode-1).getId());
				System.out.printf("이름: %s\n", memberRepository.get((int)loginMemberCode-1).getName());
				System.out.printf("생년월일: %s\n", memberRepository.get((int)loginMemberCode-1).getBirth());
				System.out.printf("1.비밀번호: %s\n", passwordOutput);
				System.out.printf("2.전화번호: %s\n", tel);
				System.out.printf("3.주소: %s\n", address);
				System.out.println("----------------------------------");
				System.out.println("s.현재 정보 저장 u.이전 단계 m.메인 q.종료");
				System.out.print("변경할 정보의 번호 입력: ");
				sel = scan.nextLine();
				
				if (sel.equalsIgnoreCase("1") || sel.equalsIgnoreCase("2") 
						|| sel.equalsIgnoreCase("3") || sel.equalsIgnoreCase("s") 
						|| sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
						|| sel.equalsIgnoreCase("q")) {
					break;
				} else {
					System.out.println("\n잘못된 입력입니다.\n");
				}
			}
			
			while(flag2) {
				
				if(sel.equalsIgnoreCase("1")) {
					password = passwordChange(password);
					if(sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
						password = memberRepository.get((int)loginMemberCode-1).getPassword();
						} else { myInfoOutput(password, tel, address);}
				} else if(sel.equalsIgnoreCase("2")) {
					tel = telChange();
					if(sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
						tel = memberRepository.get((int)loginMemberCode-1).getTel();
						} else { myInfoOutput(password, tel, address);}
				} else if(sel.equalsIgnoreCase("3")) {
					address = addressChange();
					if(sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
						address = memberRepository.get((int)loginMemberCode-1).getAddress();
						} else { myInfoOutput(password, tel, address);}
				} else if(sel.equalsIgnoreCase("s")) {
					myInfoSet(password, tel, address);
				} else if(sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
					return;
				} else {
					System.out.println("\n잘못된 입력입니다.\n");
				}
			}
		}
	}
	
	
	private static void myInfoChangeScreen() {

		Member member = getMember(loginMemberCode);
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
		
			
			titleView("내정보");
			topMessageView("");
			memberDetailView(member);
			fixNewLineView();
			messageView("");
			selectView("1.비밀번호 2.전화번호 3.주소 s.저장");
			
			sel = getInput();
			
			if (sel.equalsIgnoreCase("1")) {
		
				member = myInfoInputPassword(member);
				
			} else if (sel.equalsIgnoreCase("2")) {

				member = myInfoInputTel(member);
				
			} else if (sel.equalsIgnoreCase("3")) {

				member = myInfoInputAddress(member);

			} else if (sel.equalsIgnoreCase("s")) {
				
				member = myInfoSaveCheck(member);
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;
						
			} else {
					
				System.out.println();
				pause("잘못된 입력입니다.");
			}
		}
	}
	
	
	private static Member myInfoSaveCheck(Member member) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
		
			
			titleView("내정보");
			topMessageView("");
			memberDetailView(member);
			fixNewLineView();
			messageView("변경사항을 저장하시겠습니까?");
			selectView("y.예 n.아니오 r.뒤로");
			
			sel = getInput();
			
			if (sel.equalsIgnoreCase("y")) {

				setMember(member);
				return member;
				
			} else if (sel.equalsIgnoreCase("n")) {

				sel = "u";
				return member;

			} else if (sel.equalsIgnoreCase("r")) {
				
				return member;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return member;
						
			} else {
					
				System.out.println();
				pause("잘못된 입력입니다.");
			}
		}
		
		return member;
	}
	

	private static Member myInfoInputPassword(Member member) {
		
		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("내정보");
			topMessageView("");
			memberDetailView(member);
			fixNewLineView();
			messageView("비밀번호를 입력해주세요. (영문, 숫자, 특수문자 !@#$% 중 2개를 포함하여 8 ~ 16자)");
			selectView("r.이전");
			input = getInput();
			
			if (passwordValid(input)) {

				result.setPassword(input);
				return result;
				
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setId("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", input));
			}
		}
		
		return result;
	}
	
	
	private static Member myInfoInputTel(Member member) {
		
		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("내정보");
			topMessageView("");
			memberDetailView(member);
			fixNewLineView();
			messageView("전화번호를 입력해주세요. (형식 : 010-1234-5678, 띄어쓰기 없음)");
			selectView("r.이전");
			input = getInput();
			
			if (telValid(input)) {

				result.setTel(input);
				return result;
					
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setBirth("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", input));
			}
		}
		
		return result;
	}
	
	
	private static Member myInfoInputAddress(Member member) {

		Member result = member;
		String input = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("내정보");
			topMessageView("");
			memberDetailView(member);
			fixNewLineView();
			messageView("주소를 입력해주세요. (예 : 서울특별시 동작구 서초동, 경기도 광주시 태전동)");
			selectView("r.이전");
			input = getInput();
			
			if (addressValid(input)) {

				result.setAddress(input);
				return result;
				
					
			} else if (input.equalsIgnoreCase("r")) {
				
				member.setTel("");
				return member;
			
			} else if (input.equalsIgnoreCase("q") || (input.equalsIgnoreCase("m"))) {

				exitWarning(input, "입력값은 저장되지 않습니다.");
			
			} else {
				
				System.out.println();
				System.out.printf("잘못된 입력입니다. 입력값 : %s", input);
			}
		}
		
		return result;
	}
	
	/**
	 * 내정보저장
	 * @author 서민종
	 * 수정된 회원정보를 최종적으로 memberRepository에 기능을 합니다.
	 * @param password 수정된 password를 넘겨받습니다.
	 * @param tel 수정된 tel을 넘겨받습니다.
	 * @param address 수정된 address를 넘겨받습니다.
	 * @return sel s를 반환하여 저장이 될 수 있게 돕습니다.
	 */
	private static String myInfoSet(String password, String tel, String address) {
		
		memberRepository.get((int)loginMemberCode-1).setPassword(password);
		memberRepository.get((int)loginMemberCode-1).setTel(tel);
		memberRepository.get((int)loginMemberCode-1).setAddress(address);
		System.out.println("저장 완료");
		pause("내정보화면으로 이동합니다.");
		sel = "u";
		return sel;
	}
	
	
	/**
	 * 회원정보출력
	 * @author 서민종
	 * 수정이 적용된 회원정보를출력하여 사용자 확인을 돕습니다.
	 * @param password 수정된 password를 가지고 옵니다.
	 * @param tel 수정된 tel을 가지고 옵니다.
	 * @param address 수정된 address를 가지고 옵니다.
	 * @return 추가수정 및 이전단계, 메인, 종료의 선택이 가능하며 사용자가 입력한 sel값을 반환합니다.
	 */
	private static String myInfoOutput(String password, String tel, String address) {
		
		boolean flag1 = true;
		String passwordOutput = "";

		for (int i = 0; i<password.length(); i++) {
			passwordOutput += "*";
		}
		
		while(flag1) {
			
			System.out.printf("-----------------------------------");
			System.out.printf("\n아이디: %s\n", memberRepository.get((int)loginMemberCode-1).getId());
			System.out.printf("이름: %s\n", memberRepository.get((int)loginMemberCode-1).getName());
			System.out.printf("생년월일: %s\n", memberRepository.get((int)loginMemberCode-1).getBirth());
			System.out.printf("1.비밀번호: %s\n", passwordOutput);
			System.out.printf("2.전화번호: %s\n", tel);
			System.out.printf("3.주소: %s\n", address);
			System.out.println("----------------------------------");
			System.out.println("s.현재 정보 저장 u.이전 단계 m.메인 q.종료");
			System.out.print("변경할 정보의 번호 입력: ");
			sel = scan.nextLine();
			
			if (sel.equalsIgnoreCase("1") || sel.equalsIgnoreCase("2") 
					|| sel.equalsIgnoreCase("3") || sel.equalsIgnoreCase("s") 
					|| sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m")
					|| sel.equalsIgnoreCase("q")) {
				return sel;
			} else {
				System.out.println("\n잘못된 입력입니다.\n");
			}
		}
		
		return sel;
	}
	
	
	/**
	 * 회원주소수정
	 * @author 서민종
	 * 입력값을 스페이스 기준으로 분리하여 끝자리가 시/도, 시/군/구로 끝나는지 유효성검사 기능을 합니다.
	 * @return 사용자가 입력한 새주소를 반환하거나 종료를 원할 시 u. m. q.의 sel값을 반환합니다.
	 */
	private static String addressChange() {
		
		boolean flag = true;
		String newAddress = "";
		while(flag) {
			System.out.print("새로운 주소 입력(u.이전 단계 m.메인 q.종료): ");
			newAddress = scan.nextLine();
			
			if(newAddress.equalsIgnoreCase("u")
					|| newAddress.equalsIgnoreCase("m")
					|| newAddress.equalsIgnoreCase("q")) {
				sel = newAddress;
				return sel;
			} else { 
				String[] temp = newAddress.split(" ");
				
				if(temp[0].substring(temp[0].length()-1,temp[0].length()).equalsIgnoreCase("시")
						|| temp[0].substring(temp[0].length()-1,temp[0].length()).equalsIgnoreCase("도")) {
					if(temp[1].substring(temp[1].length()-1,temp[1].length()).equalsIgnoreCase("시")
							|| temp[1].substring(temp[1].length()-1,temp[1].length()).equalsIgnoreCase("군")
							|| temp[1].substring(temp[1].length()-1,temp[1].length()).equalsIgnoreCase("구")) {
						
						return newAddress;
						
					} else {
						System.out.println("\n잘못된 입력입니다.\n");
					}
				} else {
					System.out.println("\n잘못된 입력입니다.\n");
				}
			}
		}
		return sel;
	}
	
	
	/**
	 * 전화번호수정
	 * @author 서민종
	 * 사용자가 입력한 전화번호의 "-"의 위치나 숫자로 이루어졌는지 유효성검사 기능을 합니다.
	 * @return 사용자가 입력한 새로운 전화번호를 반환하거나 종료를 원할 시 u. m. q.의 sel값을 반환합니다.
	 */
	private static String telChange() {
		
		boolean flag = true;
		String newTel = "";
		while (flag) {
			
			System.out.print("새로운 전화번호 입력(u.이전 단계 m.메인 q.종료): ");
			newTel = scan.nextLine();
			
			if(newTel.equalsIgnoreCase("u")
					|| newTel.equalsIgnoreCase("m")
					|| newTel.equalsIgnoreCase("q")) {
				sel = newTel;
				return sel;
				
			} else {
				if(newTel.length() == 13 
						&& newTel.substring(3, 4).equalsIgnoreCase("-") 
						&& newTel.substring(8, 9).equalsIgnoreCase("-")) {
					String newTelChange = newTel.replace("-", "");
					
					for(int i=0; i<newTelChange.length(); i++) {
						char c = newTelChange.charAt(i);
						if(c >= '0' && c <= '9') {
							return newTel;
						} else {
							System.out.println("전화번호는 숫자만 기입할 수 있습니다.");
						}
					}
				} else {
					System.out.println("\n전화번호는 '000-0000-0000'의 형식만 입력할 수 있으며"
							+ " '-'을 생략할 수 없습니다.");
				}
			}
		}
		return sel;
	}
	
	
	/**
	 * 비밀번호수정
	 * @author 서민종
	 * 사용자가 입력한 비밀번호의 길이, 특수문자 1자이상 포함, 기존비밀번호와의 동일성등의 유효성검사 기능을 합니다.
	 * @return 사용자가 입력한 새로운 비밀번호를 반환하거나 종료를 원할 시 u. m. q.의 sel값을 반환합니다.
	 */
	private static String passwordChange(String password) {
		
		boolean flag = true;
		
		while (flag) {
			
			System.out.print("기존 비밀번호 입력(u.이전 단계 m.메인 q.종료): ");
			String oldPasswordInput = scan.nextLine();
			if (oldPasswordInput.equalsIgnoreCase(password)) {
				
				while (flag) {
					String newPassword = newPasswordCheck(password);
					if (newPassword.equalsIgnoreCase("u") || newPassword.equalsIgnoreCase("m") || newPassword.equalsIgnoreCase("q")) {
						sel = newPassword;
						return sel;
					} else {
						return newPassword;
					}
				}
			} else if (oldPasswordInput.equalsIgnoreCase("u") 
					|| oldPasswordInput.equalsIgnoreCase("m") 
					|| oldPasswordInput.equalsIgnoreCase("q")){
				sel = oldPasswordInput;
				return sel;
			} else {
				System.out.println("일치하지 않습니다.");
			}
		}
		return sel;
	}

	
	private static String newPasswordCheck(String password) {
		
		
		boolean flag = true;
		while(flag) {
			
			System.out.print("새로운 비밀번호 입력(u.이전 단계 m.메인 q.종료): ");
			String newPassword = scan.nextLine();
			
			if(newPassword.equalsIgnoreCase("u") 
					|| newPassword.equalsIgnoreCase("m")
					|| newPassword.equalsIgnoreCase("q")) {
				
					return newPassword;
				
			} else if(!password.equalsIgnoreCase(newPassword)) {
				
				if(newPassword.length() >= 8 && newPassword.length() <= 16) {
					int count1 = 0;
					int count2 = 0;
					for(int i=0; i<newPassword.length(); i++) {
						char c = newPassword.charAt(i);
						if(c == '!' || c == '@' || c == '#'
								||c == '$' || c == '%') {count1++;}
						if (c >= '0' && c <= '9' || c >= 'a' 
								&& c <= 'z' || c >= 'A' && c <= 'Z') {count2++;}}
					
					if(count1 != 0 && (count1+count2) == newPassword.length()) {
						
						System.out.print("새로운 비밀번호 확인 입력(u.이전 단계 m.메인 q.종료): ");
						String newPasswordCheck = scan.nextLine();
						if (newPasswordCheck.equalsIgnoreCase("u") 
								|| newPasswordCheck.equalsIgnoreCase("m") 
								|| newPasswordCheck.equalsIgnoreCase("q")) {
							
							return newPasswordCheck;
							
						} else if (newPassword.equalsIgnoreCase(newPasswordCheck)) {
							
							return newPassword;
							
						} else { System.out.println("잘못입력하셨습니다.");}
					} else { System.out.println("영문, 숫자, 특수문자(특수문자는 !@#$%중 1자 이상 포함)");}
				} else { System.out.println("8자~16자 입력");}
			} else { System.out.println("기존비밀번호와 동일합니다."); }
		}//while
		return sel;
	}
	
	
	
	
	//비회원 주문조회
	
	/**
	 * 비회원 주문조회 화면을 출력하는 Screen 메서드입니다.
	 */
	private static void orderLookupScreen() {

		/*
		요구사항 RQ-05-00-00 ~
		 */
		String input = "";
		Order order;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("비회원 주문 조회");
			topMessageView("");
			fixNewLineView();
			messageView("주문 정보를 조회합니다. 주문코드를 입력해주세요");
			selectView("");
			
			sel = getInput();
			
			if (numValid(sel)) {
				
				Long orderCode = Long.parseLong(sel);
				
				if ((order = getOrder(orderCode)) != null) {
					
					orderDetailSelecter(order);
					
				} else {
					
					System.out.println("주문 정보가 없습니다. 주문코드를 확인해주세요.");
				}
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || (sel.equalsIgnoreCase("q"))) {

				return;
			
			} else {
				
				pause(String.format("잘못된 입력입니다. 입력값 : %s", sel));
			}
		}
	}
	
	
	
	
	//주문상세
	
	/**
	 * 주문객체의 상태에 따라 해당하는 주문상세 화면으로 연결해주는 메서드입니다.
	 * 
	 * @param order 주문객체
	 * 
	 * @author 조진욱
	 */
	private static void orderDetailSelecter (Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {
			
			sel = "";
			
			if (order.getStatus() == OrderStatus.DELIVERY || order.getStatus() == OrderStatus.DEPOSIT_STAY) {
				order = orderDetailScreen1(order);
			} else if (order.getStatus() == OrderStatus.DELIVERY_COMPLITE) {
				order = orderDetailScreen2(order);
			} else {
				order = orderDetailScreen3(order);
			}
		}
	}
	

	private static Order orderDetailScreen1(Order order) {
		
		/*
		요구사항 RQ-06-10-00 ~
		*/
		
		Order result = order;
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("p"))) {

			titleView("주문상세");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("");
			selectView("1.주문취소");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				order = orderCancelInput(order);

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
				
				return order; 
				
			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}
	
	
	private static Order orderCancelInput(Order order) {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("주문취소");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("정말 주문을 취소하시겠습니까?"); 
			selectView("y.예 r.뒤로");
			sel = getInput();
	        
	        if(sel.equalsIgnoreCase("y")) {
	        	
	        	order.setStatus(OrderStatus.CANCEL_ORDER);
	        	setOrder(order);
	        	
	        	sel = "p";
	        	return order;
	        	
	        } else if(sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
	        		|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
	        	
	           return order;
	           
	        } else {
	           System.out.println();
	           pause("잘못된 입력입니다.");
	        }
		}
		
		return order;
	}

	
	private static Order orderDetailScreen2(Order order) {
		
		String s = "";
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("p"))) {

			titleView("주문상세");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView(s);
			selectView("1.교환/반품 2.구매확정");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				order = orderRequestInput(order);

			} else if (sel.equalsIgnoreCase("2")) {

				order = orderConfirmInput(order);

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				s = "잘못된 입력입니다.";
			}
		}

		return order;
	}
	
	
	private static Order orderConfirmInput(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("구매확정");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("구매확정 하시겠습니까?");
			selectView("y.예 r.뒤로");
			sel = getInput();

			if (sel.equalsIgnoreCase("y")) {
				
				order.setStatus(OrderStatus.PURCHASE_CONFIRMED);
				setOrder(order);
				
				sel = "p";
				return order;

			} else if (sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
					|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}


	private static Order orderRequestInput(Order order) {
		
		String s = "";

		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("u"))) {

			titleView("교환/반품");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView(s);
			selectView("1.교환 2.반품");
			sel = getInput();

			if (sel.equalsIgnoreCase("1")) {
				
				order = orderChangeInput(order);
				
			} else if (sel.equalsIgnoreCase("2")) {

				order = orderReturnInput(order);
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		
		return order;
	}


	private static Order orderChangeInput(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("교환신청");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("정말 교환신청 하시겠습니까?");
			selectView("y.예 r.뒤로");
			sel = getInput();

	        if(sel.equalsIgnoreCase("y")) {
	        	
	        	order.setStatus(OrderStatus.EXCHANGE_REQUEST);
	        	setOrder(order);
	        	
	        	sel = "p";
	        	return order;
	        	
	        } else if(sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
	        		|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}


	private static Order orderReturnInput(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {

			titleView("반품신청");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("정말 반품신청 하시겠습니까?");
			selectView("y.예 r.뒤로");
			sel = getInput();

	        if(sel.equalsIgnoreCase("y")) {
	        	
	        	order.setStatus(OrderStatus.RETURN_REQUEST);
	        	setOrder(order);
	        	
	        	sel = "p";
	        	return order;
	        	
	        } else if(sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
	        		|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}


	private static Order orderDetailScreen3(Order order) {
		
		while (!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("p"))) {

			titleView("주문상세");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("");
			selectView("");
			sel = getInput();

			if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return order;

			} else {

				pause("잘못된 입력입니다.");
			}
		}
		
		return order;
	}


	
	
	//관리자 화면
	
	
	/**
	 * 입력받은 orders 를 시작 인덱스로 부터 20줄씩 출력하는 View메서드입니다.
	 * @param orders 출력할 order 목록
	 * @param page 시작 인덱스
	 * 
	 * @author 이찬우
	 */
	private static boolean salesView(ArrayList<Order> orders, int page) {
		
		StringBuilder sb = new StringBuilder();
		int startIndex = (page - 1) * 20;
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s | %s | %s |\n"
				, "번호"
				, fixCenterString("상품코드", 20)
				, fixCenterString("브랜드명", 16)
				, fixCenterString("상품명", 89)
				, fixCenterString("총가격", 6)
				, fixCenterString("총수량", 15)));
		
		sb.append(VERTICAL_B);
		
		HashMap<String, ItemTag> totalsale = new HashMap<String, ItemTag>();
		
		for(Order ord : orders) {
			for(int i = 0; i < ord.getItemInfo().size(); i++) {
				
				ItemTag itemtag = new ItemTag();
				if(totalsale.containsKey(ord.getItemInfo().get(i).getItemCode())) {
					itemtag.setCount(totalsale.get(itemtag.getItemCode()).getCount() + ord.getItemInfo().get(i).getCount());
					itemtag.setPrice(totalsale.get(itemtag.getItemCode()).getPrice() + ord.getItemInfo().get(i).getPrice());
				}else {
					itemtag.setCount(ord.getItemInfo().get(i).getCount());
					itemtag.setPrice(ord.getItemInfo().get(i).getPrice());
					
				}
				totalsale.put(ord.getItemInfo().get(i).getItemCode(),itemtag);
			}
		}
		
		
		
		for (int i = startIndex; i < startIndex + 20; ++i) {
			ArrayList<Item> items = new ArrayList<Item>();
			try {
			
				for (int j = 0; j < orders.get(i).getItemInfo().size(); ++j) {
					if(items.contains(orders.get(i).getItemInfo().get(j).getItemCode())) {
						continue;
					}else {
						items.add(getItem(orders.get(i).getItemInfo().get(j).getItemCode()));
					}
				}
			
				for(int j = 0; j <orders.get(i).getItemInfo().size(); j++) {
					sb.append(String.format("| %s | %s | %s | %s | %,6d | %,15d |\r\n"
											, fixCenterString("" + i+1, 4)
											, fixLeftString(items.get(j).getCode(),30)
											, fixCenterString(items.get(j).getBrand(), 16)
											, fixLeftString(items.get(j).getName(), 50)
											, totalsale.get(items.get(j).getCode()).getPrice()
											, totalsale.get(items.get(j).getCode()).getCount()));
					viewLineCount++;
				}
			} catch (IndexOutOfBoundsException e) {
				sb.append("\n" + fixCenterString("마지막 페이지입니다.", 120) + "\n");
				sb.append(VERTICAL);
				viewLineCount += 2;
				System.out.println(sb.toString());
				pagingView(orders.size(), page);
				
				return false;
			}			
			
		}	
		sb.append(VERTICAL);
		
		viewLineCount += 4;
		System.out.println(sb.toString());
		pagingView(orders.size(), page);
		
		if (startIndex + 20 == orders.size()) {
			return false;
		} else {
			return true;
		}
	
	}
	
	
	/**
	 * 매출관리 화면으로 관리자 메인화면에서 1번입력시 호출 되는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void salesManagementScreen() {
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			/*
			요구사항 RQ-07-00-00 ~
			*/
			//실제 = 당일 매출
			//지금은 데이터가 없으므로 10월 25일 매출
			int Daysales = 0;
			//당월 매출
			int monthsales = 0;
			//전체 매출
			int totalsales = 0;
			
			
			for(Order ord : orderRepository) {
				if(ord.getStatus() == OrderStatus.PURCHASE_CONFIRMED) {
					totalsales += ord.getPayInfo().getPrice();
					//현재는 10월 9일이라는 날짜가 들어가지만 10에는 프로그램 실행날짜의 9월을 에는 프로그램 실행날짜의 일을 입력 
					if(ord.getTimeStamp().getOrderTime().get(Calendar.MONTH)+1 == 10) {
						monthsales += ord.getPayInfo().getPrice();
						if(ord.getTimeStamp().getOrderTime().get(Calendar.DATE) == 9) {
								Daysales += ord.getPayInfo().getPrice();
						}
					}
				}
			}
			
			String title = "매출관리";
			String choice = "1.월매출상세보기 2.일매출상세보기";
			titleView("매출관리");
			topMessageView("");
			saleDetailView(Daysales ,monthsales,totalsales);
			fixNewLineView();
			
			
			System.out.print("입력 : ");
			sel = scan.nextLine();
			System.out.println();
			
			if(sel.equalsIgnoreCase("1")) {
				monthSelesDatail();
			}else if(sel.equalsIgnoreCase("2")){
				daySelesDatail();	
			}else if(sel.equalsIgnoreCase("u")) {
				return;
			}else if(sel.equalsIgnoreCase("m")
					||sel.equalsIgnoreCase("q")) {
				return;
			}else {
				System.out.println("잘못된 값을 입력하셨습니다.");
			}
		}
	}
	
	
	private static void saleDetailView (int Daysales, int monthsales, int totalsales) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n\n");
		
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                            매출 관리                               |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");		
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|          일   매 출   :  " + (String.format("%,-20d", Daysales))  + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|          -------------------------------------------------         |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|          월   매 출   :  " +(String.format("%,-20d", monthsales))  + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|          -------------------------------------------------         |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|          총  매 출    :  " +(String.format("%,-20d", totalsales))  + String.format("%23s", "|"), 167) + "\n");
		sb.append(fixCenterString("|          -------------------------------------------------         |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("|                                                                    |", 167) + "\n");
		sb.append(fixCenterString("--------------------------------------------------------------------", 167) + "\n");
		
		viewLineCount += 23;
		System.out.println(sb.toString());
	}
	
	
	/**
	 * 일별상세매출화면으로 년월일을 입력받아 일치하는 order객체를 불러와 출력하는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void daySelesDatail() {
		
		int start = 1;
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
			titleView("일별매출상세보기(YYYY-MM-DD)");
			
			
			selectView("년월일입력(YYYY-MM-DD)");
			String selMonth = scan.nextLine();
			
			
			if(selMonth.contains("-")) {
				String[] check = selMonth.split("-");
				int year = Integer.parseInt(check[0]);
				int month = Integer.parseInt(check[1]);
				int day = Integer.parseInt(check[2]);
				ArrayList<Order> order = new ArrayList<Order>();
				for(Order ord : orderRepository) {
					if(ord.getStatus() == OrderStatus.PURCHASE_CONFIRMED) {
						if(ord.getTimeStamp().getOrderTime().get(Calendar.YEAR) == year) {
							if(ord.getTimeStamp().getOrderTime().get(Calendar.MONTH)+1 == month) {
								if(ord.getTimeStamp().getOrderTime().get(Calendar.DATE) == day) {
									order.add(ord);
								}
							}
						}
					}
				}
				
				
				salesView(order, start);
				pause("엔터키 입력시 일별매출상세보기 화면으로 돌아갑니다.");
				
				
			}else if(selMonth.equalsIgnoreCase("u")) {
				return;
			}else if(selMonth.equalsIgnoreCase("m")||
				selMonth.equalsIgnoreCase("q")){
				sel = selMonth;
				return;
				
			}else{
				pause("해당 월의 매출 내역이 없거나 잘못된 입력입니다.");
			}
			
		}
	}
	
	
	/**
	 * 월별상세매출화면으로 년월을 입력받아 일치하는 order객체를 불러와 출력하는 메서드입니다..
	 * 
	 * @author 이찬우
	 */
	private static void monthSelesDatail() {
		int start = 1;
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {			
			System.out.println("----------------------------");
			System.out.println("월별매출상세보기(YYYY-MM)");
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("년월입력 : ");
			String selMonth = scan.nextLine();
			System.out.println("----------------------------");
			
			if(selMonth.contains("-")) {
				String[] check = selMonth.split("-");
				int year = Integer.parseInt(check[0]);
				int month = Integer.parseInt(check[1]);
				ArrayList<Order> order = new ArrayList<Order>();
				
				for(Order ord : orderRepository) {
					if(ord.getStatus() == OrderStatus.PURCHASE_CONFIRMED) {
						if(ord.getTimeStamp().getOrderTime().get(Calendar.YEAR) == year) {
							if(ord.getTimeStamp().getOrderTime().get(Calendar.MONTH)+1 == month) {
								order.add(ord);
							}
						}
					}
				}
				salesView(order, start);
				pause("엔터키 입력시 월별매출상세보기 화면으로 돌아갑니다.");
				
			}else if(selMonth.equalsIgnoreCase("u")) {
				return;
				
			}else if(selMonth.equalsIgnoreCase("m")||
				selMonth.equalsIgnoreCase("q")){
				sel = selMonth;
				return;
				
			}else{
				pause("해당 월의 매출 내역이 없거나 잘못된 입력입니다.");
			}
			
		}
	}
	
	
	/**
	 * 재고관리화면으로 재고목록을 출력하며 관리자 메인화면에서 3번을 입력시 호출되는 메서드입니다. 
	 * 
	 * @author 이찬우
	 */
	private static void invenManagementScreen() {
		
		/*
		요구사항 RQ-09-00-00 ~
		*/
		
		int page = 1;
		boolean isNotLast = true;
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {			
			
			titleView("재고관리");
			topMessageView("");
			invenView(itemRepository, page);
			fixNewLineView();
			messageView("");
			
			if(page == 1 && !isNotLast) {

			} else if (page == 1) {
				selectView("1.상품등록 2.상품수정 3.발주 4.다음페이지");
			} else if (isNotLast) {
				selectView("1.상품등록 2.상품수정 3.발주 4.다음페이지 5.이전페이지");
			} else {
				selectView("1.상품등록 2.상품수정 3.발주 4.이전페이지");
			}
			
			sel = getInput();
			
			if (sel.equalsIgnoreCase("1")) {
				
				newItemScreen();
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				itemChangeScreen();
				
			} else if (sel.equalsIgnoreCase("3")) {
				
				adminOrder();
				
			} else if (sel.equalsIgnoreCase("4") && !(page == 1 && !isNotLast)) {
				
				page += isNotLast ? 1 : -1;
				
			} else if (sel.equalsIgnoreCase("5") && page != 1 && !(page == 1 && !isNotLast)){
				
				page --;
				
			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m")) {
				
				return;
				
			}else {
				
				pause("잘못된 입력입니다.");
			}
		
		}
		
	}
	
	
	/**
	 * ArrayList<Order> 컬렉션을 넣으면 시작 인덱스로부터 20개씩 출력해주는 View 메서드입니다.
	 * 
	 * @param orders 출력할 order 목록
	 * @param page 시작 인덱스
	 */
	private static boolean invenView(ArrayList<Item> items, int page) {
		
		StringBuilder sb = new StringBuilder();
		int startIndex = (page - 1) * 20;
		
		sb.append(VERTICAL);
		sb.append(String.format("| %s | %s | %s | %s | %s | %s |\n"
								, "번호"
								, fixCenterString("상품코드", 20)
								, fixCenterString("브랜드", 16)
								, fixCenterString("상품명", 90)
								, fixCenterString("가격", 10)
								, fixCenterString("수량", 10)));
		sb.append(VERTICAL);
		
		for (int i = startIndex; i < startIndex + 20; ++i) {
			
			try {

				sb.append(String.format("| %s | %s | %-16s | %s | %,10d | %10d |\n"
						, fixCenterString("" + (i + 1), 4)
						, fixLeftString(items.get(i).getCode(), 20)
						, fixLeftString(items.get(i).getBrand(), 16)
						, fixLeftString(items.get(i).getName(), 90)
						, itemTagRepository.get(items.get(i).getCode()).getPrice()
						, itemTagRepository.get(items.get(i).getCode()).getCount()));
				viewLineCount++;
				
			} catch (IndexOutOfBoundsException e) {
				sb.append(VERTICAL);
				viewLineCount++;
				System.out.println(sb.toString());
				pagingView(items.size(), page);
				return false;
			}
		}
		sb.append(VERTICAL);
		
		viewLineCount += 5;
		System.out.println(sb.toString());
		pagingView(items.size(), page);

		if (startIndex + 20 == items.size()) {
			return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * 재고관리 메서드 중 1번 입력시 호출되는 메서드로 상품을 등록 하기위한 메서드를 호출하는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void newItemScreen() {
		
		/*
		요구사항 RQ-09-10-00 ~
		*/
		
		Item item = new Item();
		ItemTag itemTag = new ItemTag();
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {			
			
			
			
			//r 키 입력시
			if(sel.equalsIgnoreCase("r")) {
				if(!item.getSize().equalsIgnoreCase("")) {
					item.setSize("");
					sel = "";
				}else if(itemTag.getPrice() != 0){
					itemTag.setPrice(0);
					sel = "";
				}else if(item.getWeight() != 0) {
					item.setWeight(0);
					sel = "";
				}else if(!item.getBrand().equalsIgnoreCase("")) {
					item.setBrand("");
					sel = "";
				}else if(item.getMaterial() != null) {
					item.setMaterial(null);
					sel = "";
				}else if(item.getShape() != null) {
					item.setShape(null);
					sel = "";
				}else if(!item.getName().equalsIgnoreCase("")) {
					item.setName("");
					sel = "";
				}else if(!item.getCode().equalsIgnoreCase("")) {
					item.setCode("");
					sel = "";
				}else if(item.getCode().equalsIgnoreCase("")) {
					System.out.println("입력된 값이 없습니다.");
					sel = "";
				}
			}
			
			
			viewItem(item,itemTag);
			
			//필요한 값들 순서대로 입력
			if(item.getCode().equalsIgnoreCase("")) {
				item.setCode(insertCode());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(item.getName().equalsIgnoreCase("")) {
				item.setName(insertName());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(item.getShape() == null) {
				item.setShape(insertShape());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(item.getMaterial() == null) {
				item.setMaterial(insertMaterial());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(item.getBrand().equalsIgnoreCase("")){
				item.setBrand(insertBrand());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(item.getWeight() == 0) {
				item.setWeight(insertWeight());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(itemTag.getPrice() == 0) {
				itemTag.setPrice(insertPrice());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(item.getSize().equalsIgnoreCase("")) {
				item.setSize(insertSize());
				if(sel.equalsIgnoreCase("u")
						||sel.equalsIgnoreCase("m")
						||sel.equalsIgnoreCase("q")) {
					return;
				}else if(sel.equalsIgnoreCase("r")) {
					
				}else {
					checkIntputItem(item,itemTag);	
					if(sel.equalsIgnoreCase("u")
							||sel.equalsIgnoreCase("m")
							||sel.equalsIgnoreCase("q")) {
						return;
					}
				}
			
			}
		}
			
	}
	
	
	/**
	 * 규격입력이 끝났을 경우 해당 객체들의 저장 여부를 확인하고 객체들을 저장하는 메서드 
	 * @param item 저장하고자하는 상품객체
	 * @param itemTag 저장하고자하는 상품태그객체
	 *
	 * @author 이찬우
	 */
	private static void checkIntputItem(Item item, ItemTag itemTag) {
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {			
			
			viewItem(item,itemTag);
			String input = "";
			System.out.println("정말로 저장하시겠습니까(Y/N)?");
			System.out.println("----------------------------");
			System.out.println("r.이전입력 u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			
			if(input.equalsIgnoreCase("Y")) {
				itemRepository.add(item);
				itemTagRepository.put(item.getCode(), itemTag);
				brandList.add(item.getBrand());
				pause("상품 등록을 완료되었습니다.");
				sel = "u";
				return;
			}else if(input.equalsIgnoreCase("r")||
					input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")){
				sel = input;
				return;
			}else if(input.equalsIgnoreCase("n")){
				System.out.println("저장하지 않았습니다.");
				sel = "u";
				return;
			}else {
				System.out.println("잘못된 값을 입력하셨습니다.");
			}
		}
	}


	/**
	 * 입력할 값과 입력된 상품 정보들을 출력해주는 메서드
	 * 
	 * @param item 출력할 item 정보
	 * @param itemtag 출력할 itemtag 정보
	 * @author 이찬우
	 */
	private static void viewItem(Item item, ItemTag itemtag) {
		
		System.out.println("----------------------------");
		System.out.printf("1.상품코드 : %s\r\n",item.getCode()); 
		System.out.printf("2.상품명 : %s\r\n",item.getName());
		if(item.getShape() == null) {
			System.out.printf("3.쉐입 : %s\r\n",""); 				
		}else {
			System.out.printf("3.쉐입 : %s\r\n",item.getShape()); 	
		}
		if(item.getMaterial() == null) {				
			System.out.printf("4.소재 : %s\r\n",""); 				
		}else {
			System.out.print("4.소재 : ");
			for(int i = 0; i < item.getMaterial().size();i++) {
				if(i == item.getMaterial().size()-1) {
					System.out.printf("%s\r\n",item.getMaterial().get(i));
				}else {
					System.out.printf("%s,",item.getMaterial().get(i));						
				}
			}
		}
		System.out.printf("5.브랜드 : %s\r\n",item.getBrand()); 
		if(item.getWeight() == 0) {
			System.out.printf("6.무게 : %s\r\n", "");  	
		}else {
			System.out.printf("6.무게 : %dg\r\n",item.getWeight());  	
		}
		if(itemtag.getPrice() == 0) {
			System.out.printf("7.가격 : %s\r\n", "");  	
		}else {
			System.out.printf("7.가격 : %,d원\r\n",itemtag.getPrice());  	
		}
		System.out.printf("8.규격 : %s\r\n",item.getSize()); 
		System.out.println("----------------------------");
	}

	
	/**
	 * 정해진 규칙에 따라 안경규격을 입력받아 리턴해주는 메서드
	 * 
	 * @return size 안경 규격
	 * @author 이찬우
	 */
	private static String insertSize() {
		String size = "";
		String input = "";
		while(!sel.equalsIgnoreCase("q") || !sel.equalsIgnoreCase("m") ||!sel.equalsIgnoreCase("u")||!sel.equalsIgnoreCase("r")) {
			
			System.out.println("----------------------------");
			System.out.println("규격 입력(최소 30-10-130)");				
			System.out.println("----------------------------");
			System.out.println("r.이전입력 u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("----------------------------");
			
			if(input.equalsIgnoreCase("r")||
					input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")){
				sel = input;
				return "";
				
			}else if(input.length() - input.replace("-","").length() == 2){
				
				try {
					String[] temp = input.split("-");
					
					if(Integer.parseInt(temp[0]) >= 30 && Integer.parseInt(temp[1]) >= 10 && Integer.parseInt(temp[2]) >= 130) {
						size = input;
						return size;
					}
				}catch(NumberFormatException e) {
					System.out.println("숫자와 하이푼의 조합(30-10-130)과 최소규격을 맞춰주세요");
				}
				
			}else {
				System.out.println("잘못된 값을 입력하셨습니다.");
			}
		}
		return size;
	}
	

	/**
	 * 정해진 규칙에 따라 안경가격을 입력받아 리턴해주는 메서드
	 * 
	 * @return price 안경 가격
	 * @author 이찬우
	 */
	private static int insertPrice() {
		int price = 0;
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("가격 입력");				
			System.out.println("----------------------------");
			System.out.println("r.이전입력 u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");			
			input = scan.nextLine();
			System.out.println("----------------------------");
			System.out.println();
			
			try {
				
				if(input.equalsIgnoreCase("r")||
						input.equalsIgnoreCase("u")||
						input.equalsIgnoreCase("m")||
						input.equalsIgnoreCase("q")){
					sel = input;
					return price;
				
					
				}else if(Integer.parseInt(input) >= 0){
					price = Integer.parseInt(input);
					return price;
					
				}else {
					System.out.println();
					System.out.println("----------------------------");
					System.out.println("잘못된 값을 입력하셨습니다.");
				}
				
			}catch (NumberFormatException e) {
				System.out.println("숫자를 입력해주세요");
			}
		}
		return price;
		
	}

	
	/**
	 * 정해진 규첵에 따라 안경무게를 입력받아 리턴해주는 메서드
	 * 
	 * @return weight 안경 무게
	 * @author 이찬우
	 */
	private static int insertWeight() {
		int weight = 0;
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("무게 입력");				
			System.out.println("----------------------------");
			System.out.println("r.이전입력 u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");			
			input = scan.nextLine();
			System.out.println("----------------------------");
			System.out.println();
			
			try {
				if(input.equalsIgnoreCase("r")||
						input.equalsIgnoreCase("u")||
						input.equalsIgnoreCase("m")||
						input.equalsIgnoreCase("q")){
					sel = input;	
					return weight;
					
				}else if(Integer.parseInt(input) > 0){
					weight = Integer.parseInt(input);
					return weight;
					
				}else {
					System.out.println();
					System.out.println("----------------------------");
					System.out.println("잘못된 값을 입력하셨습니다.");
					
				}
			}catch(NumberFormatException e){
				System.out.println("숫자를 입력해주세요");
			}
			
		}
		return weight;
	}
	
	
	/**
	 * 정해진 규칙에 따라 안경브랜드를 입력받아 리턴해주는 메서드
	 * 
	 * @return brand 안경 브랜드
	 * @author 이찬우
	 */
	private static String insertBrand() {
		String brand = "";
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("브랜드 입력");				
			System.out.println("----------------------------");
			System.out.println("r.이전입력 u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");			
			input = scan.nextLine();
			System.out.println("----------------------------");
			if(input.equalsIgnoreCase("r")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")){
				sel = input;	
				return brand;
			}else if(input.equalsIgnoreCase("u")) {
				return brand;
			}else {
				brand = input;
				return brand;
			}
		}
		return brand;
	}


	/**
	 * 정해진 규칙에 따라 안경소재들을 입력받아 리스트로 리턴해주는 메서드
	 * 
	 * @return ArrayList<Material> 안경 소재 리스트
	 * @author 이찬우
	 */
	private static ArrayList<Material> insertMaterial() {
		ArrayList<Material> material = new ArrayList<Material>();
		Set<Material> materials = new HashSet<Material>();
		
		Material mate = null;
		int count = 1;
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			
				System.out.println("----------------------------");
				System.out.printf("%d번 소재 선택\r\n",count);
				System.out.println("1.무테/반무테 2.메탈 3.뿔테 4.투명 5.티타늄 6.나무");
				System.out.println("----------------------------");
				System.out.println("e.입력종료 r.이전입력 u.이전 m.메인 q.종료");
				System.out.println("----------------------------");
				System.out.print("입력 : ");
				input = scan.nextLine();
				System.out.println("----------------------------");
				
				if(input.equalsIgnoreCase("1")) {
					mate = Material.RIMLESS;
				}else if(input.equalsIgnoreCase("2")) {
					mate = Material.METAL;
				}else if(input.equalsIgnoreCase("3")) {
					mate = Material.PLASTIC;
				}else if(input.equalsIgnoreCase("4")) {
					mate = Material.CLEAR;
				}else if(input.equalsIgnoreCase("5")) {
					mate = Material.TITANIUM;
				}else if(input.equalsIgnoreCase("6")) {
					mate = Material.WOOD;
				}else if(input.equalsIgnoreCase("u")) {
					return null;
					
				}else if(input.equalsIgnoreCase("m")||
						input.equalsIgnoreCase("q")||
						input.equalsIgnoreCase("r")){
					sel = input;
					return null;
					
				}else if(input.equalsIgnoreCase("e")) {
					if(materials.isEmpty()) {
						System.out.println("소재는 최소 1개 이상이여야합니다.");
					}else {
						return material;
					}
				}else {
					System.out.println();
					System.out.println("----------------------------");
					System.out.printf("잘못된 값(%s)을 입력하셨습니다.\r\n",input);
					
				}
				
				if(mate != null) {
					if(materials.contains(mate)) {
						System.out.println("같은 값을 입력하실수는 없습니다.");
					}else {
						materials.add(mate);
						material.add(mate);
						count++;
					}		
				}
		}
		return material;
	}

	/**
	 * 정해진 규칙에 따라 안경모양을 입력받아 리턴해주는 메서드
	 * 
	 * @return shape
	 * @author 이찬우
	 */
	private static Shape insertShape() {
		Shape shape = null;
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("shape 선택");
			System.out.println("1.라운드 2.스퀘어 3.하금테 4.믹스 5.보잉 6.캣아이 7.기타");
			System.out.println("----------------------------");
			System.out.println("r.이전입력 u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("----------------------------");
			System.out.println();
			if(input.equalsIgnoreCase("1")) {
				shape = Shape.ROUND;
				return shape;
			}else if(input.equalsIgnoreCase("2")) {
				shape = Shape.SQUARE;
				return shape;
			}else if(input.equalsIgnoreCase("3")) {
				shape = Shape.HALF_FRAME;
				return shape;
			}else if(input.equalsIgnoreCase("4")) {
				shape = Shape.MIX;
				return shape;
			}else if(input.equalsIgnoreCase("5")) {
				shape = Shape.BOEING;
				return shape;
			}else if(input.equalsIgnoreCase("6")) {
				shape = Shape.CATEYE;
				return shape;
			}else if(input.equalsIgnoreCase("7")) {
				shape = Shape.ELSE;
				return shape;
			}else if(input.equalsIgnoreCase("r")||
					input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")){
				sel = input;
				return shape = null;
			}else {
				System.out.println("잘못된 값을 입력하셨습니다.");
				System.out.println("----------------------------");
				System.out.println();
			}
		}
		return shape;
	}

	
	/**
	 * 정해진 규칙에 따라 안경 상품명을 입력받아 리턴해주는 메서드
	 * 
	 * @return name 안경 상품명
	 * @author 이찬우
	 */
	private static String insertName() {
		String name = "";
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("상품명 입력");				
			System.out.println("----------------------------");
			System.out.println("r.이전입력 u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");			
			input = scan.nextLine();
			System.out.println("----------------------------");
			if(input.equalsIgnoreCase("r")||
					input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")){
				sel = input;
				return name;
			}else {
				name = input;
				return name;
			}
		}
		return name;
	}


	/**
	 * 정해진 규칙에 따라 안경 상품코드를 입력받아 리턴해주는 메서드
	 * 
	 * @return code
	 * @author 이찬우
	 */
	private static String insertCode() {
		String code = "";
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("상품코드 입력");				
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");			
			input = scan.nextLine();
			System.out.println("----------------------------");
			if(input.equalsIgnoreCase("r")||
					input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")){
				sel = input;
				return code;
			}else {
				code = input;
				return code;
			}
		}
		return code;
	}


	
	
	//재고관리 - 상품수정
	
	/**
	 * 재고관리 메서드에서 2번 입력시 호출되는 메서드로 상품목록의 가격 또는 수량을 수정하는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void itemChangeScreen() {
		String input = "";
		/*
		요구사항 RQ-09-20-00 ~
		*/
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("상품수정");				
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("상품번호 입력 : ");			
			input = scan.nextLine();
			System.out.println("----------------------------");
			
			if(input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")) {
				sel = input;
				return;
			}
			Item item = new Item();
			ItemTag itemtag = new ItemTag();
			
			for(int i = 0; i < itemRepository.size();i++) {
				if(Integer.parseInt(input)-1 == i) {
					item = itemRepository.get(i);
				}
				if(itemTagRepository.containsKey(item.getCode())) {
					itemtag = itemTagRepository.get(item.getCode());
				}
			}
			itemChangeDetail(item, itemtag);	
		}
		
	}

	
	/**
	 * 입력받은 상품의 가격 또는 수량 중 수정될 항목을 선택하는 메서드입니다.
	 * 
	 * @param item 수정될 상품 정보
	 * @param itemtag 수정될 상품태그 정보
	 * @author 이찬우
	 */
	private static void itemChangeDetail(Item item, ItemTag itemtag) {
		String input = "";
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			String code = item.getCode();
			System.out.println("----------------------------");
			System.out.printf("상품수정 - %s\r\n",code);				
			System.out.println("----------------------------");
			
			itemDetailInfo(item,itemtag);
			
			System.out.println("----------------------------");
			System.out.println("1.가격 2.개수");
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("입력 : ");			
			input = scan.nextLine();
			System.out.println("----------------------------");
			if(input.equalsIgnoreCase("1")) {
				priceChange(code);
				if(sel.equalsIgnoreCase("u")||
						sel.equalsIgnoreCase("m")||
						sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(input.equalsIgnoreCase("2")) {
				countChange(code);
				if(sel.equalsIgnoreCase("u")||
						sel.equalsIgnoreCase("m")||
						sel.equalsIgnoreCase("q")) {
					return;
				}
			}else if(input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")){
				sel = input;
				return;
			}else {
				System.out.println("잘못된 값을 입력하셨습니다.");
			}
		}
	}
	

	/**
	 * 수정할 상품의 item과 itemtag를 받아 상품의 상세정보를 출력하는 메서드입니다.
	 * 
	 * @param item 상품 정보
	 * @param itemtag 상품 가격,수량 정보
	 * @author 이찬우
	 */
	private static void itemDetailInfo(Item item, ItemTag itemtag) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("[%s][%s][%s][%s][%s]\n"
								, fixCenterString("상품코드", 15)
								, fixCenterString("브랜드명", 16)
								, fixCenterString("상품명", 50)
								, fixCenterString("가격", 10)
								, fixCenterString("수량", 10)));
		
		sb.append(String.format("[%s][%s][%s][%,10d][%,10d]\r\n"
				, fixLeftString(item.getCode(),15)
				, fixCenterString(item.getBrand(), 16)
				, fixLeftString(item.getName(),50)
				, itemtag.getPrice()
				, itemtag.getCount()));
		
		System.out.println(sb.toString());
	}


	/**
	 * item목록 중 입력받은 code와 일치하는 목록의 상품 가격을 출력하고 변동가격을 입력받는 메서드입니다. 
	 * 
	 * @param code 수정할 상품 코드
	 * @author user 이찬우
	 */
	private static void priceChange(String code) {
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.printf("가격변경 - %s\r\n",code);				
			System.out.println("----------------------------");
			System.out.printf("현재가격 : %d\r\n",itemTagRepository.get(code).getPrice());	
			System.out.println("----------------------------");			
			System.out.println("u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("변경가격 : ");	
			input = scan.nextLine();
			try {
				if(input.equalsIgnoreCase("u")||
						input.equalsIgnoreCase("m")||
						input.equalsIgnoreCase("q")){
					sel = input;
					return;
				}else if(Integer.parseInt(input) > 0) {
					
					priceChangeCheck(input,code);
					return;
				
				}else {
					System.out.println("잘못된 값을 입력하셨습니다.");
				}
				
			}catch(NumberFormatException e) {
				System.out.println("숫자를 입력해주세요.");
			}
		}
	}


	/**
	 * 입력된 변동가격을 적용할지를 확인하고 가격을 수정하는 메서드입니다.
	 * 
	 * @param input 변동가격
	 * @param code 수정할 상품코드
	 * @author 이찬우
	 */
	private static void priceChangeCheck(String input, String code) {
		while(!input.equalsIgnoreCase("n")) {
			int price = Integer.parseInt(input);
			System.out.println("----------------------------");
			System.out.printf("현재가격 : %d\r\n",itemTagRepository.get(code).getPrice());					
			System.out.printf("변경가격 : %d\r\n",price);	
			System.out.println("----------------------------");	
			System.out.println("변경하시겠습니까?(Y/N)");
			System.out.println("----------------------------");
			System.out.print("입력 : ");	
			input = scan.nextLine();
			System.out.println("----------------------------");
			if(input.equalsIgnoreCase("y")) {
				itemTagRepository.get(code).setPrice(price);
				pause("가격이 변경되었습니다. ");
				System.out.println("----------------------------");
				return;
			}else if(input.equalsIgnoreCase("n")) {
				pause("가격변경을 취소하셨습니다.");
				System.out.println("----------------------------");
				return;
			}else {
				System.out.println("잘못입력하셨습니다.");
			}
		}
	}


	/**
	 * item목록 중 입력받은 code와 일치하는 목록의 상품 수량을 출력하고 변동수량을 입력받는 메서드입니다.
	 * 
	 * @param code 수정할 상품 코드
	 * @author 이찬우
	 */
	private static void countChange(String code) {
		String input = "";
		while(!(sel.equalsIgnoreCase("q")|| sel.equalsIgnoreCase("m"))) {
			
			System.out.println("----------------------------");
			System.out.printf("수량변경 - %s\r\n",code);				
			System.out.println("----------------------------");
			System.out.printf("현재수량 : %d\r\n",itemTagRepository.get(code).getCount());	
			System.out.println("----------------------------");			
			System.out.println("u.이전 m.메인 q.종료");
			System.out.println("----------------------------");
			System.out.print("변경수량 : ");	
			input = scan.nextLine();
			try {
				if(input.equalsIgnoreCase("u")||
						input.equalsIgnoreCase("m")||
						input.equalsIgnoreCase("q")){
					sel = input;
					return;
				}else if(Integer.parseInt(input) > 0) {
					countChangeCheck(input, code);	
					return;
					
				}else {
					System.out.println("잘못된 값을 입력하셨습니다.");
				}
			}catch (NumberFormatException e) {
				System.out.println("숫자를 입력해주세요..");
			}
		}
	}

	
	/**
	 * 입력된 변동수량을 적용할지를 확인하고 수량을 수정하는 메서드입니다.
	 * 
	 * @param input 변동수량
	 * @param code 수정할 상품 코드
	 * @author 이찬우
	 */
	private static void countChangeCheck(String input, String code) {
		while(!input.equalsIgnoreCase("n")) {
			int count = Integer.parseInt(input);
			System.out.println("----------------------------");
			System.out.printf("현재수량 : %d\r\n",itemTagRepository.get(code).getCount());					
			System.out.printf("변경수량 : %d\r\n",count);	
			System.out.println("----------------------------");	
			System.out.println("변경하시겠습니까?(Y/N)");
			System.out.println("----------------------------");
			System.out.print("입력 : ");	
			input = scan.nextLine();
			System.out.println("----------------------------");
			if(input.equalsIgnoreCase("y")) {
				itemTagRepository.get(code).setCount(count);
				pause("수량이 변경되었습니다.");
				System.out.println("----------------------------");
				return;
			}else if(input.equalsIgnoreCase("n")) {
				pause("수량변경을 취소하셨습니다.");
				System.out.println("----------------------------");
				return;
			}else {
				System.out.println("잘못입력하셨습니다.");
			}
		}
	}

	
	
	
	//재고관리 - 발주
	
	/**
	 * 상품의 번호와 발주할 수량을 입력받아 발주를 확인하고 재고에 수량을 추가하는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void adminOrder() {
		String input = "";
		
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {	
			System.out.println("----------------------------");
			System.out.println("번호 입력"); 
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");			
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("----------------------------");
			
			if(input.equalsIgnoreCase("u")||			
					input.equalsIgnoreCase("m")||			
					input.equalsIgnoreCase("q")) {
				sel = input;
				return;
			}
			try {
				Item item = new Item();
				ItemTag itemtag = new ItemTag();
				
				for(int i = 0; i < itemRepository.size();i++) {
					if(Integer.parseInt(input)-1 == i) {
						item = itemRepository.get(i);
					}
					if(itemTagRepository.containsKey(item.getCode())) {
						itemtag = itemTagRepository.get(item.getCode());
					}
				}
				
				adminOrderScreen(item,itemtag);
				
				
			}catch (NumberFormatException e) {
				System.out.println("숫자를 입력해주세요;");
			}
		}
			
		
	}

	
	/**
	 * 발주 수량을 입력받고 발주 여부를 확인하여 저장하는 메서드입니다.
	 * 
	 * @param item 발주할 상품 정보
	 * @param itemtag 발주할 상품태그 정보
	 * @author 이찬우
	 */
	private static void adminOrderScreen(Item item, ItemTag itemtag) {
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {	
			itemDetailInfo(item,itemtag);
			
			System.out.println("----------------------------");
			System.out.println("발주수량 입력"); 
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");			
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("----------------------------");
			if(input.equalsIgnoreCase("u")||			
					input.equalsIgnoreCase("m")||			
					input.equalsIgnoreCase("q")) {
				sel = input;
				return;
			}
			try {
				int amount = Integer.parseInt(input);
				System.out.printf("%s를 %d개 발주하시겠습니까?(Y/N)",item.getCode(),amount);
				input = scan.nextLine();
				System.out.println("----------------------------");
				if(input.equalsIgnoreCase("y")) {
					itemTagRepository.get(item.getCode()).setCount(itemTagRepository.get(item.getCode()).getCount() + amount);
					pause("발주되었습니다.");
					System.out.println("----------------------------");
					return;
				}else if(input.equalsIgnoreCase("n")) {
					pause("발주를 취소하셨습니다.");
					System.out.println("----------------------------");
					return;
				}else {
					System.out.println("잘못입력하셨습니다.");
				}
				
			}catch (NumberFormatException e) {
				
				System.out.println("숫자를 입력해주세요.");	
			}
		}
		
	}
	

	/**
	 * ArrayList<Item> 컬렉션을 넣으면 시작 인덱스로부터 20개씩 출력해주는 View 메서드입니다.
	 * 
	 * @param items 출력할 item 목록
	 * @param page 시작 인덱스
	 * @author 이찬우
	 */
	private static boolean extraView(ArrayList<Item> items, int page) {
		
		StringBuilder sb = new StringBuilder();
		int startIndex = (page - 1) * 20;
		
		
		sb.append(String.format("[%s][%s][%s][%s][%s][%s]\n"
								, "번호"
								, fixCenterString("상품코드", 15)
								, fixCenterString("브랜드명", 16)
								, fixCenterString("상품명", 50)
								, fixCenterString("가격", 10)
								, fixCenterString("수량", 10)));
		
		
		
		
		for (int i = startIndex; i < startIndex + 20; ++i) {
			try {
				sb.append(String.format("[%04d][%s][%s][%s][%,10d][%,10d]\r\n"
										, i+1
										, fixLeftString(items.get(i).getCode(),15)
										, fixCenterString(items.get(i).getBrand(), 16)
										, fixLeftString(items.get(i).getName(),50)
										, itemTagRepository.get(items.get(i).getCode()).getPrice()
										, itemTagRepository.get(items.get(i).getCode()).getCount()));
			} catch (IndexOutOfBoundsException e) {
				sb.append("\n마지막 페이지입니다.\n");
				System.out.println(sb.toString());
				return false;
			}	
		}
		
		System.out.println(sb.toString());
		pagingView(items.size(), page);
		
		if (startIndex + 20 == items.size()) {
			return false;
		} else {
			return true;
		}
	}
	
	
	
	
	//관리자 - 회원관리
	
	/**
	 * 회원관리화면으로 관리자 메인화면에서 4번 입력시 호출되며 회원목록을 출력하는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void memberManagementScreen() {
		int start = 1;
		int page = 1;
		String input = "";
		/*
		요구사항 RQ-10-00-00 ~
		*/
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {			
			
			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("회원 조회"); 
			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			memberListView(memberRepository, start, page);
			
			if(start == 1) {
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				System.out.println("1.회원 상세 검색 "); 
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				System.out.println("2.다음페이지 u.이전 m.메인 q.종료");			
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			}else {
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				System.out.println("1.회원 상세 검색"); 
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				System.out.println("2.다음페이지 3.이전페이지 u.이전 m.메인 q.종료");			
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			}
			
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			if(input.equalsIgnoreCase("1")) {
				memberSearchScreen();
			}else if(input.equalsIgnoreCase("2")) {
				start += 20;	
				page++;
			}else if(input.equalsIgnoreCase("3")){
				if(start != 1) {	
					start -= 20;
					page--;
				}else {
					System.out.println("첫페이지입니다.");
					System.out.println();
				}
			}else if(input.equalsIgnoreCase("u")) {
				return;
			}else if(input.equalsIgnoreCase("m")
					||input.equalsIgnoreCase("q")) {
				sel = input;
				return;
			}else {
				System.out.println("잘못입력하셨습니다.");
			}
		}
	}
	

	/**
	 * ArrayList<Member> 컬렉션을 넣으면 시작 인덱스로부터 20개씩 출력해주는 View 메서드입니다.
	 * 
	 * @param member 출력할 회원 목록
	 * @param start 시작 인덱스
	 * @param page 페이지수
	 * 
	 * @author 이찬우
	 */
	private static void memberListView(ArrayList<Member> member, int start,int page) {
		
		StringBuilder sb = new StringBuilder();
		int startIndex = start - 1;
		
		sb.append(String.format("[%s][%s][%s][%s][%s]\n"
								, fixCenterString("번호", 4)
								, fixCenterString("회원코드", 10)
								, fixCenterString("회원아이디", 10)
								, fixCenterString("회원이름", 10)
								, fixCenterString("전화번호", 15)));
		
		
		
		for (int i = startIndex; i < startIndex + 20; ++i) {
			
			sb.append(String.format("[%s][%s][%s][%s][%s]\r\n"
									, fixCenterString("" + (i + 1), 4)
									, fixCenterString(""+member.get(i).getCode(),10)
									, fixCenterString(member.get(i).getId(), 10)
									, fixCenterString(member.get(i).getName(),10)
									, fixCenterString(member.get(i).getTel(), 15)));
		}
		
		System.out.println(sb.toString());
		System.out.println(String.format("%d페이지", page));
	}
	
	
	/**
	 * 검색에 필요한 입력값 인 회원코드와 전화번호 중 하나를 선택하는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void memberSearchScreen() {
		String input = "";
		while(!(sel.equalsIgnoreCase("q") || sel.equalsIgnoreCase("m"))) {			
			
			System.out.println("----------------------------");
			System.out.println("회원 상세 검색 "); 
			System.out.println("----------------------------");
			System.out.println("1.회원코드 2.전화번호");			
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");			
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("----------------------------");
			
			
			if(input.equalsIgnoreCase("1")) {
				memberCodeSearchScreen();
			}else if(input.equalsIgnoreCase("2")) {
				memberPhoneSearchScreen();
			}else if(input.equalsIgnoreCase("u")) {
				return;
			}else if(input.equalsIgnoreCase("m")
					||input.equalsIgnoreCase("q")){
				sel = input;
				return;
			}else {
				System.out.println("잘못된 값을 입력하셨습니다");
			}
		}

	}
	
	
	/**
	 * 전화번호를 입력받아 입력받은 전화번호와 일치하는 회원의 정보를 찾는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void memberPhoneSearchScreen() {
		String input = "";
		while(!(sel.equalsIgnoreCase("q")||sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("회원 전화번호 검색 "); 	
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");			
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("----------------------------");
			
			if(input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")) {
				sel = input;
				return;
			}else {
				Member member = new Member();
				String tel = input; 
				for(Member mem : memberRepository) {
					if(mem.getTel().equalsIgnoreCase(tel)) {
						member = mem;
					}
				}
				ArrayList<Order> order = new ArrayList<Order>();
				for(Order ord : orderRepository) {
					if(ord.getMemberCode() == member.getCode()) {
						order.add(ord);
					}	
				}
				ArrayList<Order> ord = new ArrayList<Order>();
				for(int i = 0 ; i < order.size() ; i++) {
					for(int j = 0 ; j < order.get(i).getItemInfo().size() ; j++) {
						ArrayList<OrderItemTag> oit = new ArrayList<OrderItemTag>();
						Order ordere = new Order();
						oit.add(order.get(i).getItemInfo().get(j));
						ordere.setItemInfo(oit);
						ordere.setCode(order.get(i).getCode());
						ord.add(ordere);
					}
				}
				String str = "전화번호";
				printmemberListView(str,ord,member);
				
			}
		}
	}

		
	/**
	 * 회원 코드를 입력받아 입력받은 회원코드와 일치하는 회원의 정보를 찾는 메서드입니다.
	 * 
	 * @author 이찬우
	 */
	private static void memberCodeSearchScreen() {
		String input = "";
		while(!(sel.equalsIgnoreCase("q")||sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.println("회원 코드 검색 "); 	
			System.out.println("----------------------------");
			System.out.println("u.이전 m.메인 q.종료");			
			System.out.println("----------------------------");
			System.out.print("입력 : ");
			input = scan.nextLine();
			System.out.println("----------------------------");
			
			if(input.equalsIgnoreCase("u")||
					input.equalsIgnoreCase("m")||
					input.equalsIgnoreCase("q")) {
				sel = input;
				return;
			}else {
				Member member = new Member();
				long code = Long.parseLong(input); 
				for(Member mem : memberRepository) {
					if(mem.getCode() == code) {
						member = mem;
					}
				}
				
				ArrayList<Order> order = new ArrayList<Order>();
				for(Order ord : orderRepository) {
					if(ord.getMemberCode() == code) {
						order.add(ord);
					}
				}
				
				ArrayList<Order> ord = new ArrayList<Order>();
				for(int i = 0 ; i < order.size() ; i++) {
					for(int j = 0 ; j < order.get(i).getItemInfo().size() ; j++) {
						ArrayList<OrderItemTag> oit = new ArrayList<OrderItemTag>();
						Order ordere = new Order();
						oit.add(order.get(i).getItemInfo().get(j));
						ordere.setItemInfo(oit);
						ordere.setCode(order.get(i).getCode());
						ordere.setMemberCode(order.get(i).getMemberCode());
						ord.add(ordere);
					}
				}
				String str = "회원코드";
				printmemberListView(str,ord,member);
				
				}	
			}
	}

	
	/**
	 * 회원의 정보 및 주문정보를 받아 출력해주는 메서드입니다.
	 * 
	 * @param str  전화번호, 회원코드 구분
	 * @param ord  주문정보
	 * @param member 회원정보
	 * @author 이찬우
	 */
	private static void printmemberListView(String str, ArrayList<Order> ord, Member member) {
		
		String input = "";
		int amount = 1;
		while(!(sel.equalsIgnoreCase("q")||sel.equalsIgnoreCase("m"))) {
			System.out.println("----------------------------");
			System.out.printf("입력된 %s  : %s\r\n",str,str.equalsIgnoreCase("회원코드") ? member.getCode() : member.getTel()); 	
			System.out.println("----------------------------");
			if(ord.isEmpty() && member.getCode() > 0) {
				
				adminMemberDetailView(member);
				System.out.println("해당 회원의 주문내역이없습니다.");
				break;
				
			}else if(ord.isEmpty() && member.getCode() == 0) {
				
				System.out.println("해당 회원의 회원정보 및 주문내역이없습니다.");
				break;
				
			}else {
				
				adminMemberDetailView(member);
				adminOrderDetailView(ord, amount);
				
				if(ord.size() > 5 && amount == 1) {
					System.out.println("----------------------------");
					System.out.println("1.다음페이지 u.이전 m.메인 q.종료");			
					System.out.println("----------------------------");
				}else if(ord.size() > 5 && amount > 1 && ord.size() >= amount + 5) {
					System.out.println("----------------------------");
					System.out.println("1.다음페이지 2.이전페이지 u.이전 m.메인 q.종료");			
					System.out.println("----------------------------");
				}else if(ord.size() < amount + 5 && amount > 1){
					System.out.println("----------------------------");
					System.out.println("2.이전페이지 u.이전 m.메인 q.종료");			
					System.out.println("----------------------------");
				}else {
					System.out.println("----------------------------");
					System.out.println("u.이전 m.메인 q.종료");			
					System.out.println("----------------------------");
				}
				System.out.print("입력 : ");
				input = scan.nextLine();
				if(input.equalsIgnoreCase("u")||
						input.equalsIgnoreCase("m")||
						input.equalsIgnoreCase("q")) {
					sel = input;
					return;
				}else if(input.equalsIgnoreCase("1")) {
					amount+=5;
				}else if(input.equalsIgnoreCase("2")) {
					if(amount < 5) {
						System.out.println("첫페이지입니다.");
					}else{
						amount-=5;
					}
				}
			}
		}
	}
	

	/**
	 * 넘겨받은 member 객체와 order리스트를 통해 회원코드와 주문 정보를 출력하는 메서드입니다.
	 * 
	 * @param member 출력할 회원 정보
	 * @param order 출력할 주문 정보
	 * @param amount 페이지 수
	 * @author 이찬우
	 */
	private static void adminOrderDetailView(ArrayList<Order> ord, int amount) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("[%s][%s][%s][%s][%s][%s]\r\n"
				, fixCenterString("번호", 5)
				, fixCenterString("주문코드", 10)
				, fixCenterString("회원코드", 10)
				, fixCenterString("상품코드", 10)
				, fixCenterString("수량", 10)
				, fixCenterString("가격", 10)));
		
		
		for(int i = amount ; i < amount+5 ;i++) {
			if(i != ord.size()-1) {
				sb.append(String.format("[%s][%s][%s][%s][%,10d][%,10d]\r\n"
						, fixCenterString(""+(i+1),5)
						, fixCenterString(""+ord.get(i).getCode(),10)
						, fixCenterString(""+ord.get(i).getMemberCode(),10)
						, fixCenterString(""+ord.get(i).getItemInfo().get(0).getItemCode(),10)
						, ord.get(i).getItemInfo().get(0).getCount()
						, ord.get(i).getItemInfo().get(0).getPrice()));
			}else {
				break;
			}
		}
		System.out.println(sb.toString());
	}


	/**
	 * 넘겨받은 member 객체를 통해 회원의 상세정보 정보를 출력하는 메서드입니다.
	 * 
	 * @param member 출력할 회원 정보
	 * @param order 출력할 주문 정보
	 */
	private static void adminMemberDetailView(Member member) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("[%s][%s][%s][%s][%s]\n"
				, fixCenterString("회원코드", 10)
				, fixCenterString("회원아이디", 10)
				, fixCenterString("회원이름", 10)
				, fixCenterString("전화번호", 15)
				, fixCenterString("주소", 20)));
		
		sb.append(String.format("[%s][%s][%s][%s][%s]\r\n"
				, fixCenterString(""+member.getCode(),10)
				, fixCenterString(member.getId(), 10)
				, fixCenterString(member.getName(),10)
				, fixCenterString(member.getTel(), 15)
				, fixLeftString(member.getAddress(), 20)));
		
		
		System.out.println(sb.toString());
		//pause("엔터 입력시 회원 상세 검색으로 돌아갑니다.");
	
	}
	
	
	
	
	//관리자 주문관리
	
	/**
	 * (관리자) 주문 관리 화면을 출력하는 Screen 메서드입니다.
	 * 전체 주문 목록을 제목줄에 맞추어 출력합니다.
	 * 현재 페이지와 존재하는 페이지를 출력합니다.
	 *
	 * @author 한수연
	 */
	private static void orderManagementScreen() {
		
		/*
		요구사항 RQ-08-00-00 ~
		*/
		
		ArrayList<Order> orders = orderRepository;
		
		int page = 1;
		boolean isNotLast = true;
		
		while(!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("주문관리");
			topMessageView("#숫자 입력시 주문 상세 내역으로 이동합니다.");
			isNotLast = orderListView(orders, page);
			fixNewLineView();
			messageView("");
			
			if (page == 1 && !isNotLast) {
				selectView("1.검색");
			} else if (page == 1) {
				selectView("1.다음페이지");
			} else if (isNotLast) {
				selectView("1.다음페이지 2.이전페이지");
			} else {
				selectView("1.검색 2.이전페이지");
			}
			
			sel = getInput();
			
			if (sharpNumValid(sel)) {
				
				int index = Integer.parseInt(sel.substring(1)) - 1;
				adminOrderDetailScreen(orders.get(index));
			
			} else if (sel.equalsIgnoreCase("1") && !(page == 1 && !isNotLast)) {
				
				page += isNotLast ? 1 : -1;

			} else if (sel.equalsIgnoreCase("2") && page != 1 && !(page == 1 && !isNotLast)) {

				page --;

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;
						
			} else {
					
				System.out.println();
				pause("잘못된 입력입니다.");
			}			
		}
	}


	/**
	 * (관리자) 입력 된 주문의 상세 주문 화면을 출력하는 Screen 메서드입니다.
	 * 
	 * @param order 주문 객체
	 * 
	 * @author 조진욱
	 */
	private static void adminOrderDetailScreen(Order order) {
		
		while(!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("주문상세");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("");
			selectView("1.진행상태변경 2.주문취소");
			sel = getInput();
			
			
			if (sel.equalsIgnoreCase("1")) {
			
				order = adminOrderStatusInput(order);
				
			} else if (sel.equalsIgnoreCase("2")) {
				
				order = orderCancelInput(order);

			} else if (sel.equalsIgnoreCase("u") || sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {

				return;
						
			} else {

				pause("잘못된 입력입니다.");
			}			
		}
	}
	

	/**
	 * (관리자) 주문상태변경 화면을 출력하는 Screen 메서드입니다.
	 * 
	 * @param order 주문 객체
	 * @return 상태 변경된 주문 객체
	 * 
	 * @author 한수연
	 */
	private static Order adminOrderStatusInput(Order order) {
		
		Order result = order;
		
		while(!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("진행상태변경");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("변경할 진행상태를 선택해주세요"); 
			selectView("1.교환신청 2.교환완료 3.반품신청 4.반품완료 r.뒤로");
			sel = getInput();
	        
	        if(Pattern.matches("^[1-4]$", sel)) {
	        	
	        	switch (sel) {
	        	case "1" : result.setStatus(OrderStatus.EXCHANGE_REQUEST); break;
	        	case "2" : result.setStatus(OrderStatus.EXCHANGE_DONE); break;
	        	case "3" : result.setStatus(OrderStatus.RETURN_REQUEST); break;
	        	case "4" : result.setStatus(OrderStatus.RETURN_DONE); break;
	        	}
	        	setOrder(result);

	        	sel = "u";
	        	return result;
	        	
	        } else if(sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
	        		|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
	        	
	           return order;
	           
	        } else {

	        	pause("잘못된 입력입니다.");
	        }
		}
		
		return order;
	}

	
	/**
	 * (관리자) 주문취소 화면을 출력하는 Screen 메서드입니다.
	 * 
	 * @param order 주문 객체
	 * @return 상태 변경된 주문 객체
	 * 
	 * @author 조진욱
	 */
	private static Order adminOrderCancelInput(Order order) {
		
		String s = "정말 주문을 취소하시겠습니까?";
		
		while(!(sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q"))) {
			
			titleView("주문취소");
			topMessageView("");
			orderDetailView(order);
			fixNewLineView();
			messageView("s"); 
			selectView("y.예 r.뒤로");
			sel = getInput();
	        
	        if(sel.equalsIgnoreCase("y")) {
	        	
	        	order.setStatus(OrderStatus.CANCEL_ORDER);
	        	setOrder(order);
	        	
	        	sel = "u";
	        	return order;
	        	
	        } else if(sel.equalsIgnoreCase("r") || sel.equalsIgnoreCase("u")
	        		|| sel.equalsIgnoreCase("m") || sel.equalsIgnoreCase("q")) {
	        	
	           return order;
	           
	        } else {
	        	
	           pause("잘못된 입력입니다.");
	        }
		}
		
		return order;
	}

	

	
	//프로그램 종료

	/**
	 * 프로그램 종료시 저장 메서드를 호출하고 저장이 완료되면 종료 메세지를 출력하는 메서드입니다.
	 * 
	 * @author 조진욱
	 */
	private static void quit() {
		
		System.out.println("저장중입니다.");
		
		try {

			saveItem();
			saveMember();
			saveOrder();
			saveItemTag();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("[ERROR] 에러 꼭 찾고 넘어가세요.");
		}
		
		System.out.println("저장이 완료되었습니다.");
		System.out.println("종료되었습니다.");
	}


	/**
	 * itemRepository 내의 모든 상품객체를 데이터 파일로 저장하는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void saveItem() throws Exception {
		
		BufferedWriter itemWriter = new BufferedWriter(new FileWriter(Path.ITEM.getPath()));
		BufferedWriter itemMaterialWriter = new BufferedWriter(new FileWriter(Path.ITEM_MATERIAL.getPath()));
		
		for (Item item : itemRepository) {
			String tempItem = String.format("%s,%s,%s,%s,%s,%s", item.getCode()
															,item.getBrand()
															,item.getName()
															,item.getWeight()
															,item.getSize()
															,item.getShape());
			
			itemWriter.append(tempItem);
			itemWriter.newLine();
			
			String tempItemMaterial = item.getCode();
			for (Material material : item.getMaterial()) {
				
				tempItemMaterial += String.format("," + material.toString());
			}
			
			itemMaterialWriter.append(tempItemMaterial);
			itemMaterialWriter.newLine();
		}
		itemWriter.close();
		itemMaterialWriter.close();
	}
	

	/**
	 * memberRepository 내의 모든 회원객체를 데이터 파일로 저장하는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void saveMember() throws Exception {
	
		BufferedWriter writer = new BufferedWriter(new FileWriter(Path.MEMBER.getPath()));
		
		for (Member member : memberRepository) {
			
			String tempMember = String.format("%s,%s,%s,%s,%s,%s,%s,%s", member.getCode()
																	, member.getId()
																	, member.getPassword()
																	, member.getName()
																	, member.getTel()
																	, member.getBirth()
																	, member.getAddress()
																	, member.getGrade().toString());
			
			writer.append(tempMember);
			writer.newLine();
		}
		writer.close();
	}


	/**
	 * orderRepository 내의 모든 주문객체를 데이터 파일로 저장하는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void saveOrder() throws Exception {
		
		BufferedWriter orderWriter = new BufferedWriter(new FileWriter(Path.ORDER.getPath()));
		BufferedWriter orderItemTagWriter = new BufferedWriter(new FileWriter(Path.ORDER_ITEM_TAG.getPath()));
		BufferedWriter orderTimeStampWriter = new BufferedWriter(new FileWriter(Path.ORDER_TIME_STAMP.getPath()));
		BufferedWriter orderManWriter = new BufferedWriter(new FileWriter(Path.ORDER_MAN.getPath()));
		BufferedWriter receiverWriter = new BufferedWriter(new FileWriter(Path.RECEIVER.getPath()));
		BufferedWriter cardPayInfoWriter = new BufferedWriter(new FileWriter(Path.CARD_PAY_INFO.getPath()));
		BufferedWriter bankbookPayInfoWriter = new BufferedWriter(new FileWriter(Path.BANKBOOK_PAY_INFO.getPath()));
		
		for (Order order : orderRepository) {
			
			String tempOrder = String.format("%s,%s,%s,%s", order.getCode()
															, order.getMemberCode()
															, order.getAddress()
															, order.getStatus().toString());
			
			orderWriter.append(tempOrder);
			orderWriter.newLine();
			
			ArrayList<OrderItemTag> orderItemTags = order.getItemInfo();
			
			for (OrderItemTag orderItemTag : orderItemTags) {
				
				String tempOderItemTag = String.format("%s,%s,%s,%s", orderItemTag.getItemCode()
																	, orderItemTag.getCount()
																	, orderItemTag.getPrice()
																	, orderItemTag.getOrderCode());
				
				orderItemTagWriter.append(tempOderItemTag);
				orderItemTagWriter.newLine();
			}
			
			OrderTimeStamp orderTimeStamp = order.getTimeStamp();
			
			Calendar orderTime = orderTimeStamp.getOrderTime();
			Calendar deliveryTime = orderTimeStamp.getDeliveryTime();
			Calendar purchaseConfirmTime = orderTimeStamp.getPurchaseConfirmTime();
			Calendar extraRequestTime = orderTimeStamp.getExtraRequestTime();
			Calendar extraDoneTime = orderTimeStamp.getExtraDoneTime();
			
			
			String tempOrderTime = orderTime == null ? null : String.format("%tF-%tH-%tM", orderTime, orderTime, orderTime);
			String tempDeliveryTime = deliveryTime == null ? null : String.format("%tF-%tH-%tM", deliveryTime, deliveryTime, deliveryTime);
			String tempPurchaseConfirmTime = purchaseConfirmTime == null ? null : String.format("%tF-%tH-%tM", purchaseConfirmTime, purchaseConfirmTime, purchaseConfirmTime);
			String tempExtraRequestTime = extraRequestTime == null ? null : String.format("%tF-%tH-%tM", extraRequestTime, extraRequestTime, extraRequestTime);
			String tempExtraDoneTime = extraDoneTime == null ? null : String.format("%tF-%tH-%tM", extraDoneTime, extraDoneTime, extraDoneTime);
			
			String tempOrderTimeStamp = String.format("%s,%s,%s,%s,%s,%s", order.getCode()
																			, tempOrderTime
																			, tempDeliveryTime
																			, tempPurchaseConfirmTime
																			, tempExtraRequestTime
																			, tempExtraDoneTime);
			
			orderTimeStampWriter.append(tempOrderTimeStamp);
			orderTimeStampWriter.newLine();
			
			PersonCard orderMan = order.getOrderMan();
			String tempOrderMan = String.format("%s,%s,%s", order.getCode()
															, orderMan.getName()
															, orderMan.getTel());
			
			orderManWriter.append(tempOrderMan);
			orderManWriter.newLine();
			
			PersonCard receiver = order.getReceiver();
			String tempreceiver = String.format("%s,%s,%s", order.getCode()
															, receiver.getName()
															, receiver.getTel());
			
			receiverWriter.append(tempreceiver);
			receiverWriter.newLine();
			
			
			if (order.getPayInfo().getPayOption() == PayOption.CARD) {
				
				PayInfo cardPayInfo = order.getPayInfo();
				
				String tempCardPayInfo = String.format("%s,%s,%s,%s,%s", order.getCode() 
																		, cardPayInfo.getPayOption().toString()
																		, cardPayInfo.getPrice()
																		, cardPayInfo.isStatus()
																		, cardPayInfo.getNum());
				
				cardPayInfoWriter.append(tempCardPayInfo);
				cardPayInfoWriter.newLine();
						
			} else if (order.getPayInfo().getPayOption() == PayOption.BANKBOOK) {
			
				
				
				PayInfo bankbookPayInfo = order.getPayInfo();
				
				String tempBankbookPayInfo = String.format("%s,%s,%s,%s,%s", order.getCode() 
																			, bankbookPayInfo.getPayOption().toString()
																			, bankbookPayInfo.getPrice()
																			, bankbookPayInfo.isStatus()
																			, bankbookPayInfo.getNum());
				
				bankbookPayInfoWriter.append(tempBankbookPayInfo);
				bankbookPayInfoWriter.newLine();
			}
		}
		orderWriter.close();
		orderItemTagWriter.close();
		orderTimeStampWriter.close();
		orderManWriter.close();
		receiverWriter.close();
		cardPayInfoWriter.close();
		bankbookPayInfoWriter.close();
	}


	/**
	 * itemTagRepository 내의 모든 상품 수량, 가격 정보를 데이터 파일로 저장하는 메서드입니다.
	 * 
	 * @throws Exception
	 * 
	 * @author 조진욱
	 */
	private static void saveItemTag() throws Exception {
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(Path.ITEM_TAG.getPath()));
		
		Set<String> keys = itemTagRepository.keySet();
		for (String key : keys) {
			
			ItemTag itemTag = itemTagRepository.get(key);
			
			String tempItemTag = String.format("%s,%s,%s", itemTag.getItemCode()
														, itemTag.getCount()
														, itemTag.getPrice());
			
			writer.append(tempItemTag);
			writer.newLine();
		}
		writer.close();
	}


}

