document.addEventListener("DOMContentLoaded", function() {
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '근태 조회';
    // work_date와 근태를 담을 배열
    var attendanceDates = [];
    
    // 모든 attendanceList의 work_date의 값을 가져옴
    var attendanceList = document.querySelectorAll(".hidden_table .hidden_date");
    
    // 휴가 문서의 시작날짜와 끝나는 날짜 받아와서 배열 저장 
    var vacationDates = [];
    document.querySelectorAll(".hidden_table .vacation_start, .hidden_table .vacation_end").forEach(function(cell, index, array) {
        // 짝수 인덱스는 시작 날짜, 홀수 인덱스는 종료 날짜
        if (index % 2 === 0) {
            var startDate = cell.textContent.trim();
	        var endDate = array[index + 1] ? array[index + 1].textContent.trim() : null;

	        if (startDate != null && endDate != null) {
	            vacationDates.push({
	                start: startDate,
	                end: endDate
	            });
	        }
	    }
	});
    
    // 날짜와 근태를 배열에 추가합니다.
    attendanceList.forEach(function(cell) {
        var workDate = cell.textContent.trim();
        var checkInTime = cell.nextElementSibling ? cell.nextElementSibling.textContent.trim() : '';
        
        // 근태 값을 넣어줄 변수
        var status = '';

        // 출근 시간 존재 여부 및 출근 지각 결근 판단
        // 출근 시간 없음 = 결근 
        if (checkInTime === '') {
			// 휴가 날짜 범위에 workDate가 포함되는지 확인
            var isVacation = vacationDates.some(function(vacation) {
                return workDate >= vacation.start && workDate <= vacation.end;
            });
            status = isVacation ? 'bar_vacation' : 'bar_absent'; 
        } else {
            // 출근 시간이 존재하면 시간과 분으로 나눔 
            var checkInHour = parseInt(checkInTime.split(':')[0]);
            // 출근 시간이 9시보다 늦으면 지각 
            if (checkInHour >= 9) {
                status = 'bar_late'; 
            // 출근 시간 존재&9시 이전이면 출근 
            } else {
                status = 'bar_normal'; 
            }
        }
        
        // 배열에 workDate와 근태를 담아줌 
        if (workDate) {
            attendanceDates.push({ date: workDate, statusClass: status });
        }
    });
    
    // 오늘 날짜 
    var today = new Date();
    // 공휴일 데이터와 함께 달력을 업데이트하는 함수 호출
    fetchAndRenderHolidays(today, attendanceDates);
    
    // 이전 달
    function prevMonth() {
        // 올해, 이번 달-1, 1일 
        today = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        fetchAndRenderHolidays(today, attendanceDates);
    }
    
    // 다음 달 
    function nextMonth() {
        // 올해, 이번 달+1, 1일 
        today = new Date(today.getFullYear(), today.getMonth() + 1, 1);
        fetchAndRenderHolidays(today, attendanceDates);
    }
    
    // 오늘 버튼 클릭 이벤트 추가
    document.querySelector(".today_button").addEventListener("click", function() {
        today = new Date(); // 오늘 날짜로 설정
        fetchAndRenderHolidays(today, attendanceDates);
    });
    
    // 이전 달, 다음 달 보내기 
    window.prevMonth = prevMonth; 
    window.nextMonth = nextMonth; 
});

// 공휴일 
function fetchAndRenderHolidays(today, attendanceDates) {
    // 연도와 월 추출
    var year = today.getFullYear();
    var month = String(today.getMonth() + 1).padStart(2, '0'); 
    
    // API 호출
    fetch(`/holidays?year=${year}&month=${month}`)
        .then(response => response.json())
        .then(data => {
            var items = data.response.body.items;
            var holidays = [];
            
            if (items) {
                if (Array.isArray(items.item)) {
                    holidays = items.item;
                } else {
                    holidays = [items.item];
                }
            }
            
            // 공휴일 데이터를 달력 function의 매개변수로 보내기 
            attendanceCalendar(today, attendanceDates, holidays);
        })
}

// 달력 
function attendanceCalendar(today, attendanceDates, holidays) {
    var row = null;
    var cnt = 0;
    var calendarTable = document.getElementById("calendar");
    var calendarTableTitle = document.getElementById("year_and_month");

    var currentYear = today.getFullYear();
    var currentMonth = today.getMonth();
    var currentMonthDisplay = currentMonth + 1; 
    calendarTableTitle.innerHTML = `${currentYear}년 ${currentMonthDisplay < 10 ? '0' + currentMonthDisplay : currentMonthDisplay}월`;

    var firstDate = new Date(currentYear, currentMonth, 1);
    var lastDate = new Date(currentYear, currentMonth + 1, 0);
    var prevMonthLastDate = new Date(currentYear, currentMonth, 0);

	// 오늘 날짜
    var todayDate = new Date();

    // 기존 행 삭제
    while (calendarTable.rows.length > 1) {
        calendarTable.deleteRow(calendarTable.rows.length - 1);
    }

    // 첫 주에 전 달의 마지막 주 날짜 추가
    row = calendarTable.insertRow();
    var daysFromPrevMonth = firstDate.getDay(); 
    var prevMonthStartDate = prevMonthLastDate.getDate() - daysFromPrevMonth + 1;

    // 전 달의 날짜를 추가
    for (var i = prevMonthStartDate; i <= prevMonthLastDate.getDate(); i++) {
        var cell = row.insertCell();
        var dateToCheck = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
        var formattedDateToCheck = `${currentYear}${String(currentMonth + 1).padStart(2, '0')}${String(i).padStart(2, '0')}`;

        // 전 달 날짜는 회색
        var textColor = '#d3d3d3';

        cell.innerHTML = `<div class="date_container">
                            <span style="color: ${textColor};">${i}</span>
                          </div>`;
        cell.align = "center"; 
        cnt += 1;
    }

    // 이번 달 날짜 추가
    for (var i = 1; i <= lastDate.getDate(); i++) {
        if (cnt % 7 == 0) { 
            row = calendarTable.insertRow();
        }

        var cell = row.insertCell();
        cnt += 1;

        var dateToCheck = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
        var dateBar = '';

        // attendanceDates 배열에서 해당 날짜가 있는지 확인
        var dateEntry = attendanceDates.find(entry => entry.date === dateToCheck);
        if (dateEntry) {
            dateBar = `<div class="date_bar ${dateEntry.statusClass}"></div>`; 
        }

        var formattedDateToCheck = `${currentYear}${String(currentMonth + 1).padStart(2, '0')}${String(i).padStart(2, '0')}`;
        
        // 공휴일 확인
        var holidayItem = holidays.find(item => item.locdate.toString() === formattedDateToCheck);
        var holidayName = holidayItem ? holidayItem.dateName : '';
        var isHoliday = holidayItem ? true : false;

	    if (holidayName.includes('(')) {
	        holidayName = holidayName.replace('(', '<br>(');
	    }
    
        // 주말 여부 확인
        var isWeekend = (new Date(currentYear, currentMonth, i).getDay() === 0 || new Date(currentYear, currentMonth, i).getDay() === 6);
        
        // 날짜 색상 설정
        var textColor = isHoliday ? 'red' : (isWeekend ? (new Date(currentYear, currentMonth, i).getDay() === 0 ? 'red' : 'blue') : 'black');
		
		if (todayDate.getFullYear() === currentYear &&
		    todayDate.getMonth() === currentMonth &&
		    todayDate.getDate() === i) {
		    cell.style.backgroundColor = 'rgb(240, 243, 248)';
		}
        cell.innerHTML = `<div class="date_container">
                    <span style="color: ${textColor};">${i}</span>
                    ${holidayName ? `<div class="holiday_name">${holidayName}</div>` : ''}
                    ${dateBar}
                  </div>`;
        cell.align = "center";

        cell.setAttribute('id', i);
    }

    // 마지막 주에 다음 달의 날짜 추가
    if (cnt % 7 != 0) {
        var nextMonthDays = 7 - (cnt % 7);
        for (var i = 1; i <= nextMonthDays; i++) {
            var cell = row.insertCell();
            cell.innerHTML = `<div class="date_container">
                                <span style="color: #d3d3d3;">${i}</span>
                              </div>`; 
            cell.align = "center"; 
        }
    }
}

// 근태 조회 리스트 
document.addEventListener('DOMContentLoaded', function() {
	// 시작 날, 끝나는 날, 근태, 정렬 값과 리스트를 추가할 테이블, 페이징을 추가할 div 
    const startDateInput = document.getElementById('start_date');
    const endDateInput = document.getElementById('end_date');
    const attendanceStateSelect = document.getElementById('attendance_state');
    const sortSelect = document.getElementById('sort_select');
    const attendanceTable = document.getElementById('attendance_table_body');
    const paginationDiv = document.getElementById('pagination');
	
    // 날짜를 'yyyy-MM-dd' 형식의 String으로 변환하는 함수
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
    
    // 시작 날짜를 끝나는 날보다 나중 날짜로 설정 못하게 하는 함수 
    function startDateLimit() {
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);
        if (endDate < startDate) {
            startDateInput.value = formatDate(endDate);
        }
        startDateInput.max = formatDate(endDate);
    }
	// 오늘 날짜와 이번 달의 1일을 포맷 
    const today = new Date();
    const todayStr = formatDate(today);
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const firstDayStr = formatDate(firstDayOfMonth);
	
	// startDate와 endDate의 기본 값을 이번 달의 1일과 오늘로 설정 
    startDateInput.value = firstDayStr;
    endDateInput.value = todayStr;
	
	// startDate와 endDate를 오늘 이후의 날짜를 설정할 수 없게 설정 
    startDateInput.max = todayStr;
    endDateInput.max = todayStr;

    startDateInput.addEventListener('input', function() {
        if (!this.value) {
            this.value = firstDayStr; // 삭제 시 기본값으로 설정
        }
    });

    endDateInput.addEventListener('input', function() {
        if (!this.value) {
            this.value = todayStr; // 삭제 시 기본값으로 설정
        }
    });
    	
	// memberNo의 값 
    const memberNo = document.getElementById('mem_no').textContent;
    // 전체 데이터를 저장할 배열 
    let allAttendanceData = []; 
    // 총 페이지 수
    let totalPages = 1; 
    // 현재 페이지
    let currentPage = 0; 
    // 휴가 리스트 저장할 배열
    let vacationList = [];
    
    // 휴가 리스트 가져오기 
    function getVacationList(memberNo) {
        return fetch(`/employee/attendance/vacationList?member_no=${memberNo}`)
            .then(response => {
                return response.json();
            })
            .then(data => {
                vacationList = data;
                return data;
            })
    }
    
	// 출석 리스트 가져오기 
    function loadAttendanceList(page = 0) {
        currentPage = page; 
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);
        const attendanceState = attendanceStateSelect.value;
        const sortOrder = sortSelect.value === '최신순' ? 'DESC' : 'ASC';

        fetch(`/employee/attendance/myAttendance?member_no=${memberNo}&start_date=${formatDate(startDate)}&end_date=${formatDate(endDate)}`)
            .then(response => response.json())
            .then(data => {
                allAttendanceData = data;
                filterAndPaginateData(page, attendanceState, sortOrder);
            })
    }

    function filterAndPaginateData(page, attendanceState, sortOrder) {
        // 필터링
        const filteredData = allAttendanceData.filter(attendance => {
            const checkInTime = attendance.check_in_time || '';
            let checkInHour = 0;
			
			// checkInTime의 시간만 가져오기 
            if (checkInTime) {
                const timeParts = checkInTime.split(':');
                checkInHour = parseInt(timeParts[0], 10);
            }
            
           // workDate를 Date로 변환
            const workDate = new Date(attendance.work_date);

            // 휴가인지 아닌지 확인 
            const isVacation = vacationList.some(vacation => {
                const startDate = new Date(vacation.vacation_approval_start_date);
                const endDate = new Date(vacation.vacation_approval_end_date);
                return !checkInTime && workDate >= startDate && workDate <= endDate;
            });

			// 근태 판단해서 select option에 반영 
	        let status;
	        if (checkInTime) {
	            status = parseInt(checkInTime.split(':')[0], 10) >= 9 ? '지각' : '출근';
	        } else {
	            status = isVacation ? '휴가' : '결근';
	        }
	        
			// 근태 선택 조회 
            switch (attendanceState) {
                case '출근':
                    return checkInTime && checkInHour < 9;
                case '지각':
                    return checkInTime && checkInHour >= 9;
                case '결근':
                    return !checkInTime && !isVacation;
                case '휴가':
					return isVacation;
                case '전체':
                default:
                    return true;
            }
        }).sort((first, end) => {
            return sortOrder === 'ASC' 
                ? new Date(first.work_date) - new Date(end.work_date) 
                : new Date(end.work_date) - new Date(first.work_date);
        });

        // 페이지네이션
        // 한 페이지에 10줄
        const pageSize = 10;
        totalPages = Math.ceil(filteredData.length / pageSize); 
        const paginatedData = filteredData.slice(page * pageSize, (page + 1) * pageSize);

        // 테이블에 리스트 추가 
        attendanceTable.innerHTML = '';
        if(paginatedData.length > 0){
	        paginatedData.forEach(attendance => {
	            const row = document.createElement('tr');
	            const checkInTime = attendance.check_in_time || '';
	            const checkOutTime = attendance.check_out_time || '';
	                
	            // 휴가 상태 
		        const isVacation = vacationList.some(vacation => {
		            const startDate = new Date(vacation.vacation_approval_start_date);
		            const endDate = new Date(vacation.vacation_approval_end_date);
		            return !checkInTime && new Date(attendance.work_date) >= startDate && new Date(attendance.work_date) <= endDate;
		        });
	
		        const attendanceStatus = checkInTime
	            ? (parseInt(checkInTime.split(':')[0], 10) >= 9 ? '지각' : '출근')
	            : (isVacation ? '휴가' : '결근');
	
	            row.innerHTML = `
	                <td>${attendance.work_date}</td>
	                <td>${checkInTime}</td>
	                <td>${checkOutTime}</td>
	                <td>${attendanceStatus}</td>
	            `;
	            attendanceTable.appendChild(row);
	        });			
		} else{
			const row = document.createElement('tr');
			row.innerHTML = `
	                <td colspan="4">조회된 목록이 없습니다.</td>`;
	            attendanceTable.appendChild(row);
		}

		// 페이지 개수 업데이트 
        updatePagination(page);
    }

	function updatePagination(currentPage) {
	    paginationDiv.innerHTML = '';
	
	    // 총 페이지 수가 1일 때 페이지 번호 버튼만 표시
	    if (totalPages === 1) {
	        const pageButton = document.createElement('span');
	        pageButton.className = 'pagination_button active';
	        pageButton.textContent = '1';
	        paginationDiv.appendChild(pageButton);
	        return;
	    }
		
		// 페이지 버튼 범위 계산
    	let startPage = Math.max(0, currentPage - 1); 
    	let endPage = Math.min(totalPages - 1, currentPage + 1); 
    
    	// 페이지가 3페이지가 안 될 경우
	    if (endPage - startPage < 2) {
	        if (currentPage < totalPages - 2) {
	            endPage = Math.min(totalPages - 1, endPage + (2 - (endPage - startPage)));
	        }
	        if (startPage > 0) {
	            startPage = Math.max(0, startPage - (2 - (endPage - startPage)));
	        }
	    }
	    // 처음 페이지 버튼 (<<)
	    if (currentPage > 0) {
	        const firstButton = document.createElement('span');
	        firstButton.className = 'go_first_page_button';
	        firstButton.textContent = '<<';
	        firstButton.onclick = () => loadAttendanceList(0);
	        paginationDiv.appendChild(firstButton);
	    }
	
	    // 이전 페이지 버튼 (<)
	    if (currentPage > 0) {
	        const prevButton = document.createElement('span');
	        prevButton.className = 'pagination_button';
	        prevButton.textContent = '<';
	        prevButton.onclick = () => loadAttendanceList(currentPage - 1);
	        paginationDiv.appendChild(prevButton);
	    }
	
	    // 페이지 번호 버튼
	    for (let page = startPage; page <= endPage; page++) {
	        const pageButton = document.createElement('span');
	        pageButton.className = `pagination_button ${page === currentPage ? 'active' : ''}`;
	        pageButton.textContent = page + 1;
	        pageButton.onclick = () => loadAttendanceList(page);
	        paginationDiv.appendChild(pageButton);
	    }
	
	    // 다음 페이지 버튼 (>)
	    if (currentPage < totalPages - 1) {
	        const nextButton = document.createElement('span');
	        nextButton.className = 'pagination_button';
	        nextButton.textContent = '>';
	        nextButton.onclick = () => loadAttendanceList(currentPage + 1);
	        paginationDiv.appendChild(nextButton);
	    }
	
	    // 마지막 페이지 버튼 (>>)
	    if (currentPage < totalPages - 1) {
	        const lastButton = document.createElement('span');
	        lastButton.className = 'go_last_page_button';
	        lastButton.textContent = '>>';
	        lastButton.onclick = () => loadAttendanceList(totalPages - 1);
	        paginationDiv.appendChild(lastButton);
	    }
	}

	// 날짜 변경 이벤트  
    startDateInput.addEventListener('change', startDateLimit);
    endDateInput.addEventListener('change', startDateLimit);

    // 날짜, 정렬이 변경될 때 이벤트  
    startDateInput.addEventListener('change', () => loadAttendanceList(currentPage));
    endDateInput.addEventListener('change', () => loadAttendanceList(currentPage));
    attendanceStateSelect.addEventListener('change', () => loadAttendanceList(currentPage));
    sortSelect.addEventListener('change', () => loadAttendanceList(currentPage));
	
    // 휴가 리스트와 출석 리스트를 불러오기 
    getVacationList(memberNo).then(() => loadAttendanceList(currentPage));
});
