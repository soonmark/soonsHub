package com.soonmark.myapp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		return "home";
	}

	// Ajax request 받아서 json 형태로 response.
	// 시간, 날짜 추천 리스트 보냄
	@RequestMapping(value = "refresh", method = RequestMethod.POST, produces = "application/json; charset=utf8")
	public @ResponseBody String inputProcess(HttpServletRequest httpServletRequest) {

		int recomNum = 10; // 추천할 개수를 10개로 한정

		// 현재 시스템 날짜 // 여기서 수정하자.
		MyCalendar now = new MyCalendar();
		// now.plusHour(3);

		// 숫자만 저장, 날짜 전체 저장
		logger.info("시간값 : " + now);

		// 입력값 불러오기
		String inputEvent = httpServletRequest.getParameter("inputEventsss");

		logger.info("현재 시간값 : " + now);

		// 앞으로 추천할 날짜 리스트
		DateListVO dateVos = new DateListVO();

		// 앞으로 추천할 예외 날짜 리스트
		DateListVO specialDateVos = new DateListVO();

		// 앞으로 추천할 요일 리스트
		DateListVO dayVos = new DateListVO();

		// 앞으로 추천할 시간 리스트
		DateListVO timeVos = new DateListVO();

		// 최종 리스트
		DateListVO vos = new DateListVO();

		// 한글, 숫자, 영어, 공백만 입력 가능
		if (!(Pattern.matches("^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9\\s\\:\\-\\.\\/]*$", inputEvent))) {

			logger.error("Error : 입력 허용 패턴이 아님");

			DateVO vo = new DateVO();
			// -2는 잘못된 기호나 문자 입력 시 에러 코드
			vo.setYear("-2");
			dateVos.insertVOs(vo);

			logger.info("JSON 값  : " + dateVos.toJsonString());
			return dateVos.toJsonString();
		}

		logger.info("입력받은 일정 : " + inputEvent);

		// 년월일 패턴
		List<String> datePatterns = new ArrayList<String>();
		// 요일 패턴
		List<String> daysPatterns = new ArrayList<String>();
		// 그 외 특이 패턴
		List<String> specialDatePatterns = new ArrayList<String>();
		// 시간 패턴
		List<String> timePatterns = new ArrayList<String>();
		
		
		// 패턴 초기 세팅
		initPatterns(datePatterns, daysPatterns, specialDatePatterns, timePatterns);
		
		// 날짜 매칭	 / 요일 매칭 / 시간 매칭
		matchingProcess(inputEvent, datePatterns, TokenType.dates, dateVos);
		matchingProcess(inputEvent, specialDatePatterns, TokenType.special, specialDateVos);
		matchingProcess(inputEvent, daysPatterns, TokenType.days, dayVos);
		matchingProcess(inputEvent, timePatterns, TokenType.times, timeVos);
		
		
		merge(dateVos);

		
		// 날짜, 시간 두개의 값이 없을 때도 크로스시켜야 하므로 빈 객체 삽입.
		timeVos.insertVOs(new DateVO());
		dateVos.insertVOs(new DateVO());
		// 요일도 날짜와 크로스시켜야하므로 빈 객체 삽입.
		dayVos.insertVOs(new DateVO());

		// 우선, 요일과 날짜 크로스
		// 날짜가 있고 요일이 없는 경우나 - clear
		// 요일이 있는데 날짜가 없는 경우
		// 요일과 날짜가 있지만 서로 안 맞는 경우
		// 요일과 날짜가 있고 둘이 맞는 경우
		// 위 4가지 경우에 대해 코딩.
		for (int i = 0; i < dateVos.getVos().size(); i++) {
			for (int j = 0; j < dayVos.getVos().size(); j++) {
				// 요일 정보 없으면 그냥 나가기
				// 날짜 정보가 있을 때는 날짜 빈 객체 스킵
				// 이 2가지 경우에는 dateVos의 마지막 element 삭제.
				if (dayVos.getVos().size() == 1
						|| (dateVos.getVos().size() > 1 && i == dateVos.getVos().size() - 1)) {
					dateVos.deleteVOs(dateVos.getVos().size() - 1);;
					continue;
				}
				// 요일 정보가 있을 때는 요일 빈 객체 스킵
				if(dayVos.getVos().size() > 1 && j == dayVos.getVos().size() - 1) {
					continue;
				}

				// 날짜 없고 요일있는건 처리해야하니까 if문 처리 안 함.
				MyCalendar tmpCal = new MyCalendar();
				DateVO vo = new DateVO();

				// 날짜 없고 요일만 있을 때는
				if (dateVos.getVos().size() == 1) {
					// 가까운 미래시 날짜 찾아 tmpCal에 세팅.
					tmpCal.setCloseDateOfTheDay(dayVos.getElement(j).getDay());
					vo.setFocusOnDay(true);

				} else { // 요일 정보와 날짜 정보가 있을 때는 요일 정보를 무시
					if(dateVos.getElement(i).hasInfo(DateTimeEn.year.ordinal())) {
						tmpCal.setYear(Integer.parseInt(dateVos.getElement(i).getYear()));
					}
					if(dateVos.getElement(i).hasInfo(DateTimeEn.month.ordinal())) {
						tmpCal.setMonth(Integer.parseInt(dateVos.getElement(i).getMonth()));
					}
					if(dateVos.getElement(i).hasInfo(DateTimeEn.date.ordinal())) {
						tmpCal.setDate(Integer.parseInt(dateVos.getElement(i).getDate()));
					}
					vo.setFocusOnDay(false);
				}

				vo.setAllDate(tmpCal);

				
				dateVos.getElement(i).setAllDate(vo);
				
				dateVos.getElement(i).setFocusOnDay(vo.isFocusOnDay());
				dateVos.getElement(i).setHasInfo(DateTimeEn.day.ordinal(), true);
			}
		}

		// dateVos에 추가했던 element를 삭제했으므로 다시 하나 만들어줌.
		dateVos.insertVOs(new DateVO());

		for (int i = 0; i < timeVos.getVos().size(); i++) {
			for (int j = 0; j < dateVos.getVos().size(); j++) {

				// 둘다 정보가 들어왔으면 빈값 매칭 안 해줘도 됨.
				// 시간만 있을 때는 -> 날짜 빈거랑 매칭하고 시간 여분 빼기
				// 날짜만 있을 때는 -> 시간 빈거랑 매칭하고 날짜 여분 빼기
				// 둘 다 비어있을 때도 안 해줘도 됨.
				if ((timeVos.getVos().size() > 1 && dateVos.getVos().size() > 1
						&& (i == timeVos.getVos().size() - 1 || j == dateVos.getVos().size() - 1))
						|| ((timeVos.getVos().size() > 1 && dateVos.getVos().size() == 1)
								&& (i == timeVos.getVos().size() - 1))
						|| ((timeVos.getVos().size() == 1 && dateVos.getVos().size() > 1)
								&& (j == dateVos.getVos().size() - 1))
						|| (timeVos.getVos().size() == 1 && dateVos.getVos().size() == 1)) {
					continue;
				}

				logger.info("여1");
				
				String y = dateVos.getElement(j).getYear();
				String m = dateVos.getElement(j).getMonth();
				String dt = dateVos.getElement(j).getDate();
				String day = dateVos.getElement(j).getDay();
				boolean isFocusOnDay = dateVos.getElement(j).isFocusOnDay();

				String h = timeVos.getElement(i).getHour();
				String min = timeVos.getElement(i).getMinute();

				// 월간 일수 차이에 대한 예외처리
				if ((Integer.parseInt(m) == 2 && Integer.parseInt(dt) > 28)
						|| (Integer.parseInt(m) < 8 && Integer.parseInt(m) % 2 == 0 && Integer.parseInt(dt) > 30)
						|| (Integer.parseInt(m) > 7 && Integer.parseInt(m) % 2 == 1 && Integer.parseInt(dt) > 30)) {

					break;
				}

				// 날짜 정보가 없으면 매일 일정 or 가장 근접한 미래날짜로 세팅.
				if (dateVos.getVos().size() == 1) {
					logger.info("여2");

					// 현재 시각과 비교해서 이미 지난 시간일 경우 + 12;
					MyCalendar tmpCal = new MyCalendar();

					// 메소드의 객체가 now 캘린더가 아니면 true 입력
					tmpCal.setHour(Integer.parseInt(h), true);
					if (min == "-1") {
						tmpCal.setMinute(0);
					} else {
						tmpCal.setMinute(Integer.parseInt(min));
					}

					// for 문 돌면서 비교할 기준 시 설정
					MyCalendar comparedCal = new MyCalendar();
					comparedCal.setTimePoint(now.getTimePoint());

					for (int k = 0; k < recomNum; k++) {
						DateVO vo = new DateVO();

						if(k == 0) {
							vo.setDate("매일");
							vo.setHour(tmpCal.getHour());
							vo.setMinute(tmpCal.getMinute());
						}
						else {
							tmpCal.setCloseDateOfTime(comparedCal);
							comparedCal.setTimePoint(tmpCal.getTimePoint());

							// 현재 시스템 날짜
							vo.setAllDate(tmpCal);
							vo.setHour(tmpCal.getHour());
							vo.setMinute(tmpCal.getMinute());
						}

						vos.insertVOs(vo);
					}
				}

				else { // 날짜 정보 있으면 (시간은 있든 말든 상관없음.)
					for (int k = 0; k < recomNum; k++) {
						DateVO vo = new DateVO();
						DateVO secVo = new DateVO();
						
						logger.info("여3");
						vo.setAllDate(dateVos.getElement(j));
						vo.setFocusOnDay(isFocusOnDay);
						
						// 무슨 정보가 있는지 담겨있음
						vo.setHasInfo(DateTimeEn.year.ordinal(), dateVos.getElement(j).hasInfo(DateTimeEn.year.ordinal()));
						vo.setHasInfo(DateTimeEn.month.ordinal(), dateVos.getElement(j).hasInfo(DateTimeEn.month.ordinal()));
						vo.setHasInfo(DateTimeEn.date.ordinal(), dateVos.getElement(j).hasInfo(DateTimeEn.date.ordinal()));
						vo.setHasInfo(DateTimeEn.day.ordinal(), dateVos.getElement(j).hasInfo(DateTimeEn.day.ordinal()));

						if (y.equals("-1")) {
							vo.setYear(now.getYear());
						}
						if (m.equals("-1")) {
							vo.setMonth(now.getMonth());
						}
						if (dt.equals("-1")) {
							vo.setDate(now.getDate());
						}
						if (day.equals("-1")) {
							// 날짜에 맞는 요일 구하는 메소드
							vo.setProperDay();
						}

						
						// 시간정보 없을 땐, 종일 로 나타내기
						if (timeVos.getVos().size() == 1) {
							vo.setHour("종일");

						} else {	// 날짜와 시간 정보 있을 때
							vo.setHour(h);
						}
						vo.setMinute(min);

						// 이전에는 요일 정보를 안 받았기 때문에 이렇게 짰는데 다시 짜자.
						if (vo.isFocusOnDay == true) {
							if (k == 0) {
								vo.setDate("매주");
								vo.setYear("-1");
								vo.setMonth("-1");
							} else {
								// 요일에 맞는 날짜만 뽑도록 구하는 로직
								LocalDate tmpDate = LocalDate.of(Integer.parseInt(vo.getYear()),
																Integer.parseInt(vo.getMonth()),
																Integer.parseInt(vo.getDate()));
								
								tmpDate = tmpDate.plusWeeks(k - 1);
								vo.setDate(tmpDate.getDayOfMonth() + "");
								vo.setYear(tmpDate.getYear() + "");
								vo.setMonth(tmpDate.getMonthValue() + "");
							}
						} else {
							if (k == 0) {
								vo.setYear("매년");
								vo.setDay("-1");
							} else {
								// 빈 값 중에 가장 큰 위치값의 인덱스 년 < 월 < 일 < 요일
								int emptyInfoIdx = DateTimeEn.day.ordinal();
								// 월과 요일 정보가 있으면 일 정보가 될 것이고...
								// 년과 일 정보가 있으면 월 정보가 될 것이고...
								// 년과 요일 정보가 있으면 일 정보가 될...
								for(int a = DateTimeEn.day.ordinal() ; a > DateTimeEn.year.ordinal() ; a--) {
									if(vo.hasInfo(a) && !vo.hasInfo(a-1)) {
										emptyInfoIdx = a-1;
										break;
									}
								}
								
								MyCalendar tmpCal2 = new MyCalendar();
								tmpCal2.setYear(Integer.parseInt(vo.getYear()));
								tmpCal2.setMonth(Integer.parseInt(vo.getMonth()));
								tmpCal2.setDate(Integer.parseInt(vo.getDate()));
								// 해당 정보를 기준으로 더해주기.
								tmpCal2.setCloseDate(now, emptyInfoIdx);
								
								vo.setYear((Integer.parseInt(tmpCal2.getYear()) + k - 1) + "");

								// 날짜에 맞는 요일 구하는 로직
								vo.setProperDay();
							}
						}

						vos.insertVOs(vo);
						
						// 시간정보와 날짜 모두 있을 땐, halfTime 일 경우, 오후 시간도 저장
						if (timeVos.getVos().size() > 1 && Integer.parseInt(vo.getHour()) <= 12) {
							secVo.setHour(((Integer.parseInt(vo.getHour()) + 12)%24) + "");
							secVo.setMinute(vo.getMinute());
							secVo.setAllDate(vo);
							vos.insertVOs(secVo);
						}
					}
				}

			}
		}

		return vos.toJsonString();
	}

	void initPatterns(List<String> datePatterns, List<String> daysPatterns, List<String> specialDatePatterns,
			List<String> timePatterns) {

		// datePatterns.add("^(.*)([0-9]{4})-(0?[1-9]|1[0-2])-([0-9]{1,2})((.*))$"); //
		// 2018-3-19
		// datePatterns.add("^(.*)([0-9]{4})/(0?[1-9]|1[0-2])/([0-9]{1,2})((.*))$"); //
		// 2018/3/19
		// datePatterns.add("^(.*)([0-9]{4})\\.(0?[1-9]|1[0-2])\\.([0-9]{1,2})((.*))$");
		// // 2018.3.19
		// datePatterns.add("^(.*)([0-9]{4})년 (0?[1-9]|1[0-2])월 ([0-9]{1,2})일((.*))$");
		// // 2018년 3월 19일
		datePatterns.add("^(.*)(?<month>1[0-2])월 (?<date>[1-9]|[1-2][0-9]|3[0-1])일(.*)$"); // 11월 19일
		datePatterns.add("^(|.*[^1])(?<month>[1-9])월 (?<date>[1-9]|[1-2][0-9]|3[0-1])일(.*)$"); // 3월 19일
		datePatterns.add("^(.*)(?<month>1[0-2])-(?<date>[1-9]|[1-2][0-9]|3[0-1])(|[^0-9].*)$"); // 11-19
		datePatterns.add("^(|.*[^1])(?<month>[1-9])-(?<date>[1-9]|[1-2][0-9]|3[0-1])(|[^0-9].*)$"); // 3-19
		datePatterns.add("^(.*)(?<month>1[0-2])\\.(?<date>[1-9]|[1-2][0-9]|3[0-1])(|[^0-9].*)$"); // 11.19
		datePatterns.add("^(|.*[^1])(?<month>[1-9])\\.(?<date>[1-9]|[1-2][0-9]|3[0-1])(|[^0-9].*)$"); // 3.19
		datePatterns.add("^(.*)(?<month>1[0-2])/(?<date>[1-9]|[1-2][0-9]|3[0-1])(|[^0-9].*)$"); // 11/19
		datePatterns.add("^(|.*[^1])(?<month>[1-9])/(?<date>[1-9]|[1-2][0-9]|3[0-1])(|[^0-9].*)$"); // 3/19
		// 일만 입력받기
//		datePatterns.add("^([^월]*)(?<date>[1-2][0-9]|3[0-1])일(.*)$"); // 19일
//		datePatterns.add("^(.*)(?<date>[1-2][0-9]|3[0-1])일(.*)$"); // 19일
//		datePatterns.add("^(|[^월]*[^1-3])(?<date>[1-9])일(.*)$");
		// 1일
//		datePatterns.add("^(|.*[^1-3])(?<date>[1-9])일(.*)$"); // 1일
		// 월만 입력받기
//		datePatterns.add("^(.*)(?<month>1[0-2])월([^일]*)$"); // 12월
//		datePatterns.add("^(.*)(?<month>1[0-2])월(.*)$"); // 12월
//		datePatterns.add("^(|.*[^1])(?<month>[1-9])월([^일]*)$"); // 1월
//		datePatterns.add("^(|.*[^1])(?<month>[1-9])월(.*)$"); // 1월
		// 20180319

		// 요일 패턴
		daysPatterns.add("^(.*)(?<day>월|화|수|목|금|토|일)요일(.*)$"); // 월요일

		// 그 외 특이 패턴
		specialDatePatterns.add("^(.*)(?<dateWithoutDays>내일)(.*)$"); // 내일
		specialDatePatterns.add("^(.*)(?<dateWithoutDays>오늘)(.*)$"); // 오늘
		specialDatePatterns.add("^(.*)(?<dateWithoutDays>모레)(.*)$"); // 모레
		specialDatePatterns.add("^(.*)(?<dateWithoutDays>매일)(.*)$"); // 매일

		specialDatePatterns.add("^(.*)(?<dateWithDays>이번주)(.*)$"); // 이번주
		specialDatePatterns.add("^(.*)(?<dateWithDays>다음주)(.*)$"); // 다음주
		specialDatePatterns.add("^(.*)(?<dateWithDays>다다음주)(.*)$"); // 다다음주
		specialDatePatterns.add("^(.*)(?<dateWithDays>매주)(.*)$"); // 매주


		
		// 시간 패턴
		timePatterns.add("^(.*)(?<hour>1[0-9]|2[0-3]):(?<minute>[0-5][0-9])(.*)$"); // 12:01 // 12:1은 안 됨
		timePatterns.add("^(|.*[^1-2])(?<hour>[1-9]):(?<minute>[0-5][0-9])(.*)$"); // 2:01 // 2:1은 안 됨
		timePatterns.add("^(.*)(?<hour>1[0-9]|2[0-3])시 (?<minute>[0-5][0-9])분(.*)$"); // 12시 30분
		timePatterns.add("^(.*)(?<hour>1[0-9]|2[0-3])시 (?<minute>[0-9])분(.*)$"); // 12시 3분
		timePatterns.add("^(|.*[^1-2])(?<hour>[1-9])시 (?<minute>[0-5][0-9])분(.*)$"); // 7시 30분
		timePatterns.add("^(|.*[^1-2])(?<hour>[1-9])시 (?<minute>[0-9])분(.*)$"); // 7시 3분
		// 분 정보 없는 시간
		timePatterns.add("^(.*)(?<hour>1[0-9]|2[0-3])시([^분]*)$"); // 12시
		timePatterns.add("^(|.*[^1-2])(?<hour>[1-9])시([^분]*)$"); // 7시
	}
		
	void matchingProcess(String inputEv, List<String> patterns, TokenType tokenType, DateListVO targetVos) {
		// 요일 매칭
		Iterator<String> iter = patterns.iterator();
		while (iter.hasNext()) {
			String pattern = iter.next();
			Pattern inputPattern = Pattern.compile(pattern);
			Matcher matcher = inputPattern.matcher(inputEv);
			
			if (matcher.matches()) {
				logger.info("패턴 : " + pattern);
				logger.info("패턴 만족 : " + matcher.group(0));
				
				DateVO vo = new DateVO();
				
				// enum의 추상메소드로 바로 감.
				tokenType.setVoInfo(vo, matcher);
				
				targetVos.insertVOs(vo);
			}
		}
	}
	
	void merge(DateListVO targetVos) {
		
	}
}
