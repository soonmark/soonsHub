<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>

<meta charset="utf-8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta name="description" content="">
<meta name="author" content="">

<title>>일정 입력 페이지</title>

<!-- Bootstrap core CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<%-- <link type="text/css"
	href="${pageContext.request.contextPath}/resources/vendor/bootstrap/css/bootstrap.min.css"
	rel="stylesheet"> --%>

<!-- Custom CSS -->
<link type="text/css"
	href="${pageContext.request.contextPath}/resources/vendor/bootstrap/css/style.css"
	rel="stylesheet">


<!-- Jquery -->
<script type="text/javascript"
	src="${pageContext.request.contextPath}/resources/lib/jquery-3.3.1.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>

<!-- Custom styles for this template -->
<!-- <style>
body {
	padding-top: 54px;
}

@media ( min-width : 992px) {
	body {
		padding-top: 56px;
	}
}
</style> -->


</head>
<body>

	<!-- Navigation -->
	<nav class="navbar navbar-inverse navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target="#myNavbar">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#">Schedules</a>
			</div>
			<div class="collapse navbar-collapse" id="myNavbar">
				<ul class="nav navbar-nav">
					<li class="active"><a href="#">Home</a></li>
				</ul>
			</div>
		</div>
	</nav>




	<!-- Page Content -->
	<div class="jumbotron text-center topConts">
		<h2>일정</h2>
		<ul class="list_eventDates">
			<li class="list-float-left">
					<textarea id="inputEvent" autocomplete="off" name="inputEvent"
						rows="1" tabindex="1"></textarea> <!-- class="form-control input-lg" -->
			</li>
		</ul>
		<div class="panel panel-default myPanelMargin">
			<div class="panel-heading font-bording display-right">
				<span class="glyphicon glyphicon-chevron-right"></span> <span
					class="inputText">입력한 일정 : ${message}</span>
			</div>
<!-- 			<div class="panel-body font-bording display-right">
				<span class="glyphicon glyphicon-chevron-right"></span> <span
					class="changedText">선택한 날짜 : </span>
			</div> -->
		</div>

		<button type="button"
			class="btn btn-success wider-width btn-lg disabled">추가</button>
	</div>
	<div class="container"></div>




	<p></p>


	<!-- jquery -->
	<script type="text/javascript">
		$(function() {
			$
					.ajaxSetup({
						error : function(jqXHR, exception) {
							if (jqXHR.status === 0) {
								alert('Not connect.\n Verify Network.');
							} else if (jqXHR.status == 400) {
								alert('Server understood the request, but request content was invalid. [400]');
							} else if (jqXHR.status == 401) {
								alert('Unauthorized access. [401]');
							} else if (jqXHR.status == 403) {
								alert('Forbidden resource can not be accessed. [403]');
							} else if (jqXHR.status == 404) {
								alert('Requested page not found. [404]');
							} else if (jqXHR.status == 500) {
								alert('Internal server error. [500]');
							} else if (jqXHR.status == 503) {
								alert('Service unavailable. [503]');
							} else if (exception === 'parsererror') {
								alert('Requested JSON parse failed. [Failed]');
							} else if (exception === 'timeout') {
								alert('Time out error. [Timeout]');
							} else if (exception === 'abort') {
								alert('Ajax request aborted. [Aborted]');
							} else {
								alert('Uncaught Error.n' + jqXHR.responseText);
							}
						}
					});

			var inputEvent = $('#inputEvent');

			var tmpStr = "default";
			
			var startDate = "startDate";
			var endDate = "endDate";
			
			function createJsonObj(strDate){
				var oriStr = strDate;
				var idVal = "#" + strDate;
				
				strDate = new Object();
				strDate.year = $(idVal).find('#year').text();
				strDate.month = $(idVal).find('#month').text();
				strDate.date = $(idVal).find('#date').text();
				strDate.day = $(idVal).find('#day').text();
				strDate.hour = $(idVal).find('#hour').text();
				strDate.minute = $(idVal).find('#minute').text();
				strDate.isAllDayEvent = $(idVal).find('#isAllDayEvent').text();
				strDate = JSON.stringify(strDate);
				console.log(strDate);
				
				if(oriStr == "startDate"){
					startDate = strDate;
				}
				if(oriStr == "endDate"){
					endDate = strDate;
				}
			}
			

			// 입력창에 입력이 될 때마다
			$(inputEvent).keyup(function() {
				var prev = tmpStr;
				tmpStr = inputEvent.val();
				
				if($('#startDate').length){
					if(startDate == "startDate"){
						createJsonObj(startDate);
					}
				}
				if($('#endDate').length){
					if(endDate == "endDate"){
						createJsonObj(endDate);
					}
				}
				console.log("startDate : " + startDate);
				console.log("endDate : " + endDate);
				

				// but 이전 입력값과 같으면 얼럿 안 나오게 
				if (tmpStr != prev)
					checkInput();
			}); // end keyup

			function zeroFill(number, width) {
				width -= number.toString().length;
				if (width > 0) {
					return new Array(width + (/\./.test(number) ? 2 : 1))
							.join('0')
							+ number;
				}
				return number + "";
			}

			const INVALID_INPUT_CHARACTER = -2;
			const NO_DATA = -1;
			const NO_DATA_FOR_DAY = "";

			function checkInput() {
				console.log("tmpStr 내용 : " + tmpStr);

				// 뷰 올리기...
				var settings = {
					url : 'getEvent',
					type : 'post',
					dataType : 'json',
					data : {
						"inputText" : tmpStr,
 						"startDate" : startDate,
						"endDate" : endDate
					},
					success : function(data) {
						var str = "";
						$(data)
								.each(
										function(idx, dataEach) {
											console.log("data " + idx + ": "
													+ dataEach.year);

											if (dataEach.year == INVALID_INPUT_CHARACTER) {
												alert("., /, -, : 외의 기호는 입력이 불가능합니다.");
											} else {
												var infoStr = "";
												str += "<div class=\"list-group-item\">";
												
												if (dataEach.year != NO_DATA) {
													str += zeroFill(
															dataEach.year, 4);
													infoStr += "<div class=\"info\" id=\"year\">" + zeroFill(dataEach.year, 4) + "</div>";
												}
												if (dataEach.month != NO_DATA) {
													str += "/"
															+ zeroFill(
																	dataEach.month,
																	2) + "/";
													infoStr += "<div class=\"info\" id=\"month\">" + zeroFill(dataEach.month, 2) + "</div>";
												}
												if (dataEach.date != NO_DATA) {
													str += zeroFill(
															dataEach.date, 2)
															+ " ";
													infoStr += "<div class=\"info\" id=\"date\">" + zeroFill(dataEach.date, 2) + "</div>";
												}
												if (dataEach.displayDay != NO_DATA_FOR_DAY) {
													str += dataEach.displayDay
															+ " ";
													infoStr += "<div class=\"info\" id=\"day\">" + dataEach.displayDay + "</div>";
												}
												if (dataEach.allDayEvent == true) {
													str += "종일";
													infoStr += "<div class=\"info\" id=\"isAllDayEvent\">" + dataEach.allDayEvent + "</div>";
												} else {
													if (dataEach.hour != NO_DATA) {
														str += zeroFill(
																dataEach.hour,
																2);
														infoStr += "<div class=\"info\" id=\"hour\">" + dataEach.hour + "</div>";
													}
													if (dataEach.minute != NO_DATA) {
														str += ":"
																+ zeroFill(
																		dataEach.minute,
																		2);
														infoStr += "<div class=\"info\" id=\"minute\">" + dataEach.minute + "</div>";
													}
												}
												str += infoStr + "</div>";
											}
										});

						$(".list-group").html(str);
						$('.inputText').text("입력한 일정 : " + tmpStr);
						console.log("성공");

  						$(document).on(
								{
									mouseenter : function() {
										$(this).addClass("active");
									},
									mouseleave : function() {
										$(this).removeClass("active");
									}
								}, '.list-group-item');

					}
				}

				$.ajax(settings);
			}
			;
		});
		
		// 리스트 중에서 하나 클릭했을 때 이벤트
		$(document).on({
			click : function() {
/* 				$('.changedText').text("선택한 날짜 : " + $(this).text()); */
				
				if ( $('.newly-added').length == 0) {
					console.log("text: " + $(this).clone().children().remove().end().text());
					var newEvent = "<li class=\"list-float-left newly-added\" id=\"startDate\"><span>"
									+ $(this).clone().children().remove().end().text() + "</span></li>";

					$('.list_eventDates').prepend(newEvent);
					
					$(this).find(".info").each(
							function(){
								$('#startDate').append($(this).clone());
					});
				}
				else if ( $('.newly-added').length == 1) {
					console.log("text: " + $(this).clone().children().remove().end().text());
					var newEvent = "<li class=\"list-float-left newly-added\" id=\"endDate\"><span>"
									+ $(this).clone().children().remove().end().text() + "</span></li>";

					$('.list_eventDates').prepend(newEvent);
					
					$(this).find(".info").each(
							function(){
								$('#endDate').append($(this).clone());
					});
				}
				
				
/* 				checkInput(); */
			}
		}, '.list-group-item');
	</script>


	<div class="panel panel-default myPanelMargin">
		<div class="panel-heading font-bording">날짜/시간 자동 완성</div>
		<div class="list-group panel-body"></div>
	</div>




	<!-- Bootstrap core JavaScript -->
	<%-- 
	<script
		src="<c:url value="/resources/vendor/bootstrap/js/bootstrap.bundle.min.js" />"></script>
 --%>
</body>
</html>
