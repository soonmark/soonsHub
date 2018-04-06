package com.soonmark.myapp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.regex.Matcher;

public enum TokenType {
	dates(0){
		void setVoInfo(DateTimeVO vo, Matcher matcher) {
			try {
				vo.setYear(Integer.parseInt(matcher.group("year")));
				int year = vo.getYear();
//				if(year.length() == 2) {
//					// 받은 년도가 2자리수면 처리해줘야함.
//					// 일단 90년 전까지는 19로, 그 전은 20으로 해놓자.
//					if(LocalDate.now().getYear()-(yearInt + 2000) < 90
//							&& LocalDate.now().getYear()-(yearInt + 2000) > -10) {
//						yearInt += 2000;
//					}else {
//						yearInt += 1900;
//					}
//					vo.setYear("" + yearInt);
//				}
				if(year >= 0 && year < 100) {
					// 받은 년도가 2자리수면 처리해줘야함.
					// 일단 90년 전까지는 19로, 그 전은 20으로 해놓자.
					if(LocalDate.now().getYear()-(year + 2000) < 90
							&& LocalDate.now().getYear()-(year + 2000) > -10) {
						year += 2000;
					}else {
						year += 1900;
					}
					vo.setYear(year);
				}
				vo.setHasInfo(DateTimeEn.year.ordinal(), true);
			} catch (IllegalArgumentException e) {
				vo.setYear(-1);
			}
			try {
				vo.setMonth(Integer.parseInt(matcher.group("month")));
				vo.setHasInfo(DateTimeEn.month.ordinal(), true);
			} catch (IllegalArgumentException e) {
				vo.setMonth(-1);
			}
			try {
				vo.setDate(Integer.parseInt(matcher.group("date")));
				vo.setHasInfo(DateTimeEn.date.ordinal(), true);
			} catch (IllegalArgumentException e) {
				vo.setDate(-1);
			}
		}
	}, days(1){
		void setVoInfo(DateTimeVO vo, Matcher matcher) {
			// month와 date 에 해당하는 group 만 따로 읽어 저장
			try {
				// getDayOfWeekByLocale
				String engWeekday = vo.getDayOfWeekByLocale(matcher.group("day"));
				vo.setDay(DayOfWeek.valueOf(engWeekday));
				vo.setHasInfo(DateTimeEn.day.ordinal(), true);
			} catch (IllegalArgumentException e) {
				vo.setDay(null);
			}
		}
	}, times(2){
		void setVoInfo(DateTimeVO vo, Matcher matcher) {
			vo.setHour(Integer.parseInt(matcher.group("hour")));
			vo.setHasInfo(DateTimeEn.hour.ordinal(), true);
			try {
				// 시간 중에 group 명이 minute 이 없는 경우 0으로 세팅
				vo.setMinute(Integer.parseInt(matcher.group("minute")));
				vo.setHasInfo(DateTimeEn.minute.ordinal(), true);
			} catch (IllegalArgumentException e) {
				vo.setMinute(0);
				vo.setHasInfo(DateTimeEn.minute.ordinal(), true);
			}
		}
	}, special(3){
		void setVoInfo(DateTimeVO vo, Matcher matcher) {
			//enum 
			for(specialDateTypeNeedsDay sdt : specialDateTypeNeedsDay.values()) {
				try {
					vo.setSpecialDate(matcher.group(sdt.name()));
					
					// date 로 해두면 이상해질 것 같지만 일단...
					vo.setHasInfo(DateTimeEn.date.ordinal(), true);
					vo.isFocusOnDay = false;
					break;
				} catch (IllegalArgumentException e) {
					//enum 
					for(specialDateTypeWithoutDay sdtwd : specialDateTypeWithoutDay.values()) {
						try {
							vo.setSpecialDate(matcher.group(sdtwd.name()));
							
							// date 로 해두면 이상해질 것 같지만 일단...
							vo.setHasInfo(DateTimeEn.date.ordinal(), true);
							vo.isFocusOnDay = false;
							break;
						} catch (IllegalArgumentException event) {
							vo.setSpecialDate("-1");
						}
					}
				}
			}
//			try {
//				vo.setSpecialDate(matcher.group("dateWithoutDays"));
//				vo.setHasInfo(DateTimeEn.date.ordinal(), true);
//				vo.isFocusOnDay = false;
//			} catch (IllegalArgumentException e) {
//				try {
//					vo.setSpecialDate(matcher.group("dateWithDays"));
//					vo.setHasInfo(DateTimeEn.date.ordinal(), true);
//					vo.isFocusOnDay = true;
//				} catch (IllegalArgumentException ex) {
//					vo.setSpecialDate("-1");
//				}
//			}
		}
	};
	
	int tokenType;
	TokenType(int type){
		tokenType = type;
	}
	
	int getInteger() {
		return tokenType;
	}
	
	
	// 추상 메소드
	abstract void setVoInfo(DateTimeVO vo, Matcher matcher);
}
