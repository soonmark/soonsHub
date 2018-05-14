package com.soonmark.core;

import com.soonmark.domain.TokenType;

public class DateTimeListMgrSet {
	
	// 앞으로 추천할 날짜 리스트
	private DateTimeListManager dateList;
	
	// 앞으로 추천할 특수 날짜 리스트
	private DateTimeListManager specialDateList;

	// 앞으로 추천할 요일 리스트
	private DateTimeListManager dayList;

	// 앞으로 추천할 시간 리스트
	private DateTimeListManager timeList;

	// 최종 리스트
	private EventListManager resultList;

	public DateTimeListMgrSet() {
		dateList = new DateTimeListManager(TokenType.dates);
		specialDateList = new DateTimeListManager(TokenType.special);
		dayList = new DateTimeListManager(TokenType.days);
		timeList = new DateTimeListManager(TokenType.times);
		resultList = new EventListManager();
	}

	public DateTimeListManager getDateList() {
		return dateList;
	}
	
	public void setDateList(DateTimeListManager dateList) {
		this.dateList = dateList;
	}
	
	public DateTimeListManager getSpecialDateList() {
		return specialDateList;
	}
	
	public void setSpecialDateList(DateTimeListManager specialDateList) {
		this.specialDateList = specialDateList;
	}
	
	public DateTimeListManager getDayList() {
		return dayList;
	}
	
	public void setDayList(DateTimeListManager dayList) {
		this.dayList = dayList;
	}
	
	public DateTimeListManager getTimeList() {
		return timeList;
	}
	
	public void setTimeList(DateTimeListManager timeList) {
		this.timeList = timeList;
	}
	
	public EventListManager getResultList() {
		return resultList;
	}
	
	public void setResultList(EventListManager resultList) {
		this.resultList = resultList;
	}
	
	public void deduplicateElements(TokenType tokenType) {
		getDTListByTokType(tokenType).deduplicateElements();
	}


	public DateTimeListManager getDTListByTokType(TokenType tokenType) {
		DateTimeListManager list;
		switch (tokenType) {
		case dates:
			list = dateList;
			break;
		case days:
			list = dayList;
			break;
		case times:
			list = timeList;
			break;
		case special:
			list = specialDateList;
			break;
		default:
			list = new DateTimeListManager();
			break;
		}
		return list;
	}
	
	public void mergeList(TokenType targetType, TokenType tokenType) {
		DateTimeListManager targetList = getDTListByTokType(targetType);
		DateTimeListManager nonTargetList = getDTListByTokType(tokenType);
		
		// mergeBy 완성하자
		targetList.mergeByList(tokenType, nonTargetList);
	}

	public EventListManager mergeList(TokenType periodType, TokenType firstType, TokenType secondType) {
		DateTimeListManager periodList = getDTListByTokType(periodType);
		DateTimeListManager firstList = getDTListByTokType(firstType);
		DateTimeListManager secondList = getDTListByTokType(secondType);
		
		// mergeBy 완성하자
		return periodList.mergeWith(firstList, secondList);
	}
	
	public void adjustForAmPmTime() {
		timeList.adjustForAmPmTime();
	}

	public boolean allListEmpty() {
		if(getDateList().getDtMgrList().size() == 0 && getDayList().getDtMgrList().size() == 0
				&& getSpecialDateList().getDtMgrList().size() == 0 && getTimeList().getDtMgrList().size() == 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean relatedToDateListEmpty() {
		if(getDateList().getDtMgrList().size() == 0 && getDayList().getDtMgrList().size() == 0
				&& getSpecialDateList().getDtMgrList().size() == 0) {
			return true;
		}
		
		return false;
	}

}
