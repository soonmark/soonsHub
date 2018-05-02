package com.soonmark.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soonmark.domain.AppConstants;
import com.soonmark.domain.DateTimeDTO;
import com.soonmark.domain.StringDateTimeDTO;
import com.soonmark.domain.DateTimeEn;
import com.soonmark.domain.Priority;

public class DateTimeEstimator {

	private Logger logger = LoggerFactory.getLogger(DateTimeEstimator.class);

	private DateTimeListManager timeList;
	private DateTimeListManager dateList;
	private EventListManager resultList;
	private int focusingRecurNum;

	boolean startDateExists;
	boolean endDateExists;

	public DateTimeEstimator(DateTimeListManager timeList, DateTimeListManager dateList) {
		this.timeList = timeList;
		this.dateList = dateList;
		resultList = new EventListManager();
		focusingRecurNum = 2;
	}

	public EventListManager fillEmptyDatas(DateTimeDTO startDate, DateTimeDTO endDate) {
		startDateExists = false;
		endDateExists = false;

		boolean isDateEmpty = false;
		boolean isTimeEmpty = false;

		// 날짜, 시간 두개의 값이 없을 때도 크로스시켜야 하므로 빈 객체 삽입.
		if (timeList.getDtMgrList().size() == 0) {
			timeList.insertDtObj(new InvalidDateTimeObj());
			isTimeEmpty = true;
		}

		if (dateList.getDtMgrList().size() == 0) {
			dateList.insertDtObj(new InvalidDateTimeObj());
			isDateEmpty = true;
		}

		if (isDateEmpty && isTimeEmpty) {
			timeList.deleteDtObj(0);
			dateList.deleteDtObj(0);
			return resultList;
		}

		// startDate, endDate 존재여부 확인
		if (startDate != null) {
			startDateExists = true;
		}

		if (endDate != null) {
			endDateExists = true;
		}

		if (isDateEmpty) {
			setTimeToCloseFutureTime(startDate, endDate);
			setPriorityForTimeWithoutDate();
		} else {
			// 월간 일수 차이에 대한 예외처리
			if (isValidDates() == true) {
				addEstimateDateAndTime(isTimeEmpty, startDate, endDate);
			}
		}

		return resultList;
	}

	private void setPriorityForTimeWithoutDate() {
		int closestIdx = 0;
		DateTimeAdjuster closest = new DateTimeAdjuster();
		// closest.setDate(resultList.getElement(closestIdx).getDate());
		// closest.setMonth(resultList.getElement(closestIdx).getMonth());
		// closest.setYear(resultList.getElement(closestIdx).getYear());
		// closest.setHour(resultList.getElement(closestIdx).getHour(), false);
		// closest.setMinute(resultList.getElement(closestIdx).getMinute());
		closest.setDate(resultList.getElement(closestIdx).getStartDate().getDate());
		closest.setMonth(resultList.getElement(closestIdx).getStartDate().getMonth());
		closest.setYear(resultList.getElement(closestIdx).getStartDate().getYear());
		closest.setHour(resultList.getElement(closestIdx).getStartDate().getHour(), false);
		closest.setMinute(resultList.getElement(closestIdx).getStartDate().getMinute());

		// for (int i = 1; i < resultList.getDtMgrList().size(); i++) {
		for (int i = 1; i < resultList.getEvMgrList().size(); i++) {
			DateTimeAdjuster cur = new DateTimeAdjuster();
			// cur.setDate(resultList.getElement(i).getDate());
			// cur.setMonth(resultList.getElement(i).getMonth());
			// cur.setYear(resultList.getElement(i).getYear());
			// cur.setHour(resultList.getElement(i).getHour(), false);
			// cur.setMinute(resultList.getElement(i).getMinute());
			cur.setDate(resultList.getElement(i).getStartDate().getDate());
			cur.setMonth(resultList.getElement(i).getStartDate().getMonth());
			cur.setYear(resultList.getElement(i).getStartDate().getYear());
			cur.setHour(resultList.getElement(i).getStartDate().getHour(), false);
			cur.setMinute(resultList.getElement(i).getStartDate().getMinute());

			if (closest.getTimePoint().isAfter(cur.getTimePoint())) {
				closest.setTimePoint(cur.getTimePoint());
				closestIdx = i;
			}
		}

		// resultList.getElement(closestIdx).setPriority(Priority.timeWithFirstEstimateDate);
		resultList.getElement(closestIdx).getStartDate().setPriority(Priority.timeWithFirstEstimateDate);
	}

	private void setTimeToCloseFutureTime(DateTimeDTO startDate, DateTimeDTO endDate) {
		logger.info("날짜 정보없음");
		for (int i = 0; i < timeList.getDtMgrList().size(); i++) {
			for (int j = 0; j < dateList.getDtMgrList().size(); j++) {

				InvalidDateTimeObj startDtObj = new InvalidDateTimeObj();
				InvalidDateTimeObj endDtObj = null;
				DateTimeAdjuster tmpCal = new DateTimeAdjuster();

				tmpCal.setHour(timeList.getElement(i).getHour(), false);

				// 메소드의 객체가 now 캘린더가 아니면 true 입력
				if (timeList.getElement(i).getMinute() == AppConstants.NO_DATA) {
					tmpCal.setMinute(0);
				} else {
					tmpCal.setMinute(timeList.getElement(i).getMinute());
				}

				// 미리 선택된 날짜가 전혀 없을 때
				if (!startDateExists && !endDateExists) {
					// 날짜 정보가 없으면 가장 근접한 미래날짜로 세팅.
					 if (tmpCal.getTimePoint().toLocalTime().isBefore(LocalTime.now())) {
						 tmpCal.plusDate(1);
					 }
				}
				// 미리 선택된 날짜시간 정보가 있으면
				else {
					// 일정 시작 날짜만 있을 때
					if (startDateExists && !endDateExists) {
						tmpCal.setYear(startDate.getDate().getYear());
						tmpCal.setMonth(startDate.getDate().getMonthValue());
						tmpCal.setDate(startDate.getDate().getDayOfMonth());

						// 시간이 없을 때
						if (startDate.getTime() == null) {
							// 선택된 날짜로 세팅.

							startDtObj.setAllDate(tmpCal);
							startDtObj.setHour(tmpCal.getHour());
							startDtObj.setMinute(tmpCal.getMinute());
							startDtObj.setPriority(timeList.getElement(i).getPriority());
						}
						// 일정 시작 날짜와 시간 모두 있을 때
						else {
							startDtObj.setYear(startDate.getDate().getYear());
							startDtObj.setMonth(startDate.getDate().getMonthValue());
							startDtObj.setDate(startDate.getDate().getDayOfMonth());
							startDtObj.setHour(startDate.getTime().getHour());
							startDtObj.setMinute(startDate.getTime().getMinute());
							
							endDtObj = new InvalidDateTimeObj();
							endDtObj.setYear(startDate.getDate().getYear());
							endDtObj.setMonth(startDate.getDate().getMonthValue());
							endDtObj.setDate(startDate.getDate().getDayOfMonth());
							endDtObj.setHour(tmpCal.getHour());
							endDtObj.setMinute(tmpCal.getMinute());
							endDtObj.setPriority(timeList.getElement(i).getPriority());
						}
					} else if (!startDateExists && endDateExists) {

					}
					// 시작시간, 종료시간 존재 시,
					else if( startDateExists && endDateExists) {
						startDtObj.setYear(startDate.getDate().getYear());
						startDtObj.setMonth(startDate.getDate().getMonthValue());
						startDtObj.setDate(startDate.getDate().getDayOfMonth());
						startDtObj.setHour(tmpCal.getHour());
						startDtObj.setMinute(tmpCal.getMinute());
						
						endDtObj = new InvalidDateTimeObj();
//						endDtObj.setAllDate(tmpCal);
						endDtObj.setYear(endDate.getDate().getYear());
						endDtObj.setMonth(endDate.getDate().getMonthValue());
						endDtObj.setDate(endDate.getDate().getDayOfMonth());
						endDtObj.setHour(tmpCal.getHour());
						endDtObj.setMinute(tmpCal.getMinute());
						endDtObj.setPriority(timeList.getElement(i).getPriority());
					}
					// 둘다 존재하지 않을 
					else {
						
					}
				}

//				startDtObj.setAllDate(tmpCal);
//				startDtObj.setHour(tmpCal.getHour());
//				startDtObj.setMinute(tmpCal.getMinute());
//				startDtObj.setPriority(timeList.getElement(i).getPriority());

				// resultList.insertDtObj(dtObj);
				InvalidEventObj evObj = new InvalidEventObj(startDtObj, endDtObj);
				resultList.insertDtObj(evObj);
			}
		}
	}

	private boolean isValidDates() {
		boolean result = true;
		for (int j = 0; j < dateList.getDtMgrList().size(); j++) {
			int m = dateList.getElement(j).getMonth();
			int dt = dateList.getElement(j).getDate();

			if ((m == 2 && dt > 29) || (m < 8 && m % 2 == 0 && dt > 30) || (m > 7 && m % 2 == 1 && dt > 30)) {
				result = false;
			}
		}
		return result;
	}

	private void addEstimateDateAndTime(boolean isTimeEmpty, DateTimeDTO startDate, DateTimeDTO endDate) {
		// request 로 들어온 startDate가 존재할 때,
		if (startDateExists) {
			for (int i = 0; i < timeList.getDtMgrList().size(); i++) {
				for (int j = 0; j < dateList.getDtMgrList().size(); j++) {
					for (int k = 0; k < focusingRecurNum; k++) {
						InvalidEventObj eventObj = new InvalidEventObj(new InvalidDateTimeObj(), new InvalidDateTimeObj());
//						InvalidDateTimeObj startDtObj = new InvalidDateTimeObj();
//						InvalidDateTimeObj endDtObj = new InvalidDateTimeObj();
						eventObj.getStartDate().copyAllExceptForDayFrom(dateList.getElement(j));
						eventObj.getEndDate().copyAllExceptForDayFrom(dateList.getElement(j));

						// 시작시간과 입력시간정보 없을 땐 종일 로 나타내기
						estimateTime(isTimeEmpty, eventObj.getEndDate(), timeList.getElement(i));
						
						// 시작 시간있으면 종료시간을 그 한 시간뒤로 변경해야함.
						if (startDate.getTime() != null) {
							if(eventObj.getEndDate().isAllDayEvent() == true) {
								eventObj.getEndDate().setHour(startDate.getTime().getHour() + 1);
								eventObj.getEndDate().setMinute(startDate.getTime().getMinute());
							}
								eventObj.getStartDate().setHour(startDate.getTime().getHour());
								eventObj.getStartDate().setMinute(startDate.getTime().getMinute());
						}/* else {
							// 시작시간과 입력시간정보 없을 땐 종일 로 나타내기
							estimateTime(isTimeEmpty, eventObj.getEndDate(), timeList.getElement(i));
						}*/

						eventObj.getStartDate().setYear(startDate.getDate().getYear());
						eventObj.getStartDate().setMonth(startDate.getDate().getMonthValue());
						eventObj.getStartDate().setDate(startDate.getDate().getDayOfMonth());
						// 년월일 요일 추정
						estimateDates(eventObj, true, k, dateList.getElement(j), startDate.getDate());
					}
				}
			}
		}
		// request 로 들어온 startDate는 없지만, endDate는 존재할 때
		else if (endDateExists) {

		}
		// request 로 들어온 startDate, endDate 존재하지 않을 때
		else {

			for (int i = 0; i < timeList.getDtMgrList().size(); i++) {
				for (int j = 0; j < dateList.getDtMgrList().size(); j++) {
					for (int k = 0; k < focusingRecurNum; k++) {
						InvalidEventObj eventObj = new InvalidEventObj();
//						InvalidDateTimeObj startDtObj = new InvalidDateTimeObj();
						eventObj.setStartDate(new InvalidDateTimeObj());
						eventObj.getStartDate().copyAllExceptForDayFrom(dateList.getElement(j));

						// 시간정보 없을 땐 종일 로 나타내기

						estimateTime(isTimeEmpty, eventObj.getStartDate(), timeList.getElement(i));

						// 년월일 요일 추정
						estimateDates(eventObj, false, k, dateList.getElement(j));
					}
				}
			}
		}
	}

	private void estimateDates(InvalidEventObj eventObj, boolean fillEnd, int k, InvalidDateTimeObj origin, LocalDate... sDate) {

		estimateYear(eventObj, fillEnd, sDate);

		if(fillEnd) {
			if (eventObj.getEndDate().getFocusToRepeat() == null) {
				// 반복없이 해당 값만 insert
				estimateOneDate(eventObj, fillEnd, sDate);
			} else {
				// focus할 게 있으면 그 정보를 기준으로 for문 돌며 여러값 insert
				estimateMultipleDates(eventObj, fillEnd, k, origin, sDate);
			}
		}
		else {
			if (eventObj.getStartDate().getFocusToRepeat() == null) {
				// 반복없이 해당 값만 insert
				estimateOneDate(eventObj, fillEnd, sDate);
			} else {
				// focus할 게 있으면 그 정보를 기준으로 for문 돌며 여러값 insert
				estimateMultipleDates(eventObj, fillEnd, k, origin, sDate);
			}
		}
	}

	private void estimateMultipleDates(InvalidEventObj eventObj, boolean fillEnd, int k, InvalidDateTimeObj origin, LocalDate... sDate) {
		if(fillEnd) {
			if (eventObj.getEndDate().getMonth() == AppConstants.NO_DATA) {
				eventObj.getEndDate().setMonth(1);
			}
			if (eventObj.getEndDate().getDate() == AppConstants.NO_DATA) {
				eventObj.getEndDate().setDate(1);
			}
			if (eventObj.getEndDate().getDay() == AppConstants.NO_DATA_FOR_DAY) {
				// 날짜에 맞는 요일 구하는 메소드
				eventObj.getEndDate().setProperDay();
			}

			if (k == 0 && eventObj.getEndDate().isAllDayEvent() != true) {
				eventObj.getEndDate().setPriority(Priority.timeWithFirstEstimateDate);
			}
			if (eventObj.getEndDate().isFocusOnDay() == true) {
				// 매주 해당 요일에 맞는 날짜만 뽑도록 구하는 로직
				setDatesByEveryWeek(eventObj.getEndDate(), k, origin);
			} else {
				setDatesByToken(eventObj.getEndDate(), k, sDate);
			}
		}
		else {
			if (eventObj.getStartDate().getMonth() == AppConstants.NO_DATA) {
				eventObj.getStartDate().setMonth(1);
			}
			if (eventObj.getStartDate().getDate() == AppConstants.NO_DATA) {
				eventObj.getStartDate().setDate(1);
			}
			if (eventObj.getStartDate().getDay() == AppConstants.NO_DATA_FOR_DAY) {
				// 날짜에 맞는 요일 구하는 메소드
				eventObj.getStartDate().setProperDay();
			}

			if (k == 0 && eventObj.getStartDate().isAllDayEvent() != true) {
				eventObj.getStartDate().setPriority(Priority.timeWithFirstEstimateDate);
			}
			if (eventObj.getStartDate().isFocusOnDay() == true) {
				// 매주 해당 요일에 맞는 날짜만 뽑도록 구하는 로직
				setDatesByEveryWeek(eventObj.getStartDate(), k, origin);
			} else {
				setDatesByToken(eventObj.getStartDate(), k);
			}
		}
		// resultList.insertDtObj(dtObj);
		InvalidEventObj evObj = new InvalidEventObj(eventObj.getStartDate(), eventObj.getEndDate());
		resultList.insertDtObj(evObj);
	}

	private void setDatesByToken(InvalidDateTimeObj dtObj, int k, LocalDate ... sDate) {
		DateTimeAdjuster tmpCal2 = new DateTimeAdjuster();
		tmpCal2.setYear(dtObj.getYear());
		tmpCal2.setMonth(dtObj.getMonth());
		tmpCal2.setDate(dtObj.getDate());
		// focus 할 해당 정보를 기준으로 더해주기.
		if(sDate.length > 0) {
			tmpCal2.setCloseDate(tmpCal2, dtObj.getFocusToRepeat(), k, sDate[0]);
		}
		else {
			tmpCal2.setCloseDate(tmpCal2, dtObj.getFocusToRepeat(), k);
		}

		dtObj.setDate(tmpCal2.getDate());
		dtObj.setYear(tmpCal2.getYear());
		dtObj.setMonth(tmpCal2.getMonth());

		// 날짜에 맞는 요일 구하는 로직
		dtObj.setProperDay();
	}

	private void setDatesByEveryWeek(InvalidDateTimeObj dtObj, int k, InvalidDateTimeObj origin) {
		// 날짜 정보 없이 요일만 있을 때
		if (!dtObj.hasInfo(DateTimeEn.year.ordinal()) && !dtObj.hasInfo(DateTimeEn.month.ordinal())
				&& !dtObj.hasInfo(DateTimeEn.date.ordinal())) {
			LocalDate tmpDate = LocalDate.now();
			tmpDate = tmpDate.with(TemporalAdjusters.nextOrSame(origin.getDay()));
			tmpDate = tmpDate.plusWeeks(k);
			dtObj.setDate(tmpDate.getDayOfMonth());
			dtObj.setYear(tmpDate.getYear());
			dtObj.setMonth(tmpDate.getMonthValue());
			dtObj.setDay(tmpDate.getDayOfWeek());
			if (k == 0) {
				dtObj.setPriority(Priority.dayOrigin);
			} else {
				dtObj.setPriority(Priority.dayClones);
			}
		} else { // 날짜도 있는데 요일에 맞춰야할 때
			LocalDate tmpDate = LocalDate.of(dtObj.getYear(), dtObj.getMonth(), dtObj.getDate());
			tmpDate = tmpDate.with(TemporalAdjusters.nextOrSame(origin.getDay()));
			tmpDate = tmpDate.plusWeeks(k);
			dtObj.setDate(tmpDate.getDayOfMonth());
			dtObj.setYear(tmpDate.getYear());
			dtObj.setMonth(tmpDate.getMonthValue());
			dtObj.setDay(tmpDate.getDayOfWeek());
			if (k == 0) {
				dtObj.setPriority(Priority.dayOrigin);
			} else {
				dtObj.setPriority(Priority.dayClones);
			}
		}
	}

	private void estimateOneDate(InvalidEventObj eventObj, boolean fillEnd, LocalDate... sDate) {
		focusingRecurNum = 1;
		if(fillEnd) {
			if (eventObj.getEndDate().getMonth() == AppConstants.NO_DATA) {
				eventObj.getEndDate().setMonth(LocalDate.now().getMonthValue());
			}
			if (eventObj.getEndDate().getDate() == AppConstants.NO_DATA) {
				eventObj.getEndDate().setDate(LocalDate.now().getDayOfMonth());
			}
			if (eventObj.getEndDate().getDay() == AppConstants.NO_DATA_FOR_DAY) {
				// 날짜에 맞는 요일 구하는 메소드
				eventObj.getEndDate().setProperDay();
			}
		}
		else {
			if (eventObj.getStartDate().getMonth() == AppConstants.NO_DATA) {
				eventObj.getStartDate().setMonth(LocalDate.now().getMonthValue());
			}
			if (eventObj.getStartDate().getDate() == AppConstants.NO_DATA) {
				eventObj.getStartDate().setDate(LocalDate.now().getDayOfMonth());
			}
			if (eventObj.getStartDate().getDay() == AppConstants.NO_DATA_FOR_DAY) {
				// 날짜에 맞는 요일 구하는 메소드
				eventObj.getStartDate().setProperDay();
			}
		}

		// resultList.insertDtObj(dtObj);
		InvalidEventObj evObj = new InvalidEventObj(eventObj.getStartDate(), eventObj.getEndDate());
		resultList.insertDtObj(evObj);
	}

	private void estimateYear(InvalidEventObj eventObj, boolean fillEnd, LocalDate ... sDate) {
		if(fillEnd) {
			if (eventObj.getEndDate().getYear() == AppConstants.NO_DATA) {
				if (sDate.length > 0 && sDate[0] != null) {
					eventObj.getEndDate().setYear(sDate[0].getYear());
				} else {
					eventObj.getEndDate().setYear(LocalDate.now().getYear());
				}
			}
		}
		else {
			if (eventObj.getStartDate().getYear() == AppConstants.NO_DATA) {
				if (sDate.length > 0 && sDate[0] != null) {
					eventObj.getStartDate().setYear(sDate[0].getYear());
				} else {
					eventObj.getStartDate().setYear(LocalDate.now().getYear());
				}
			}
		}
	}

	private void estimateTime(boolean isTimeEmpty, InvalidDateTimeObj dtObj, InvalidDateTimeObj timeObj) {
		if (isTimeEmpty == true) {
			dtObj.setAllDayEvent(true);
		} else { // 날짜와 시간 정보 있을 때
			dtObj.setPriority(timeObj.getPriority());
			dtObj.setHour(timeObj.getHour());
			dtObj.setMinute(timeObj.getMinute());
		}
	}
}
