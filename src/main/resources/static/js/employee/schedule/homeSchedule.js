document.addEventListener('DOMContentLoaded', function() { 
	let participantMembers = [];
    var csrfToken = document.querySelector('input[name="_csrf"]').value;
    var calendarEl = document.getElementById('calendar');
    var calendar; 
    
    var categoryColors = {};
    var categoryNames = {};
   
    var memberNo = document.getElementById('schedule_memberNo').value; 
    var userDepartmentNo = document.getElementById('schedule_departmentNo').value;    
    
    var allEvents = [];  
        
    var googleHolidayDates = [];  
 	
	calendar = new FullCalendar.Calendar(calendarEl, {  
	    googleCalendarApiKey: 'AIzaSyBaQi-ZLyv7aiwEC6Ca3C19FE505Xq2Ytw',
	    eventSources: [
	        {
	            googleCalendarId: 'ko.south_korea#holiday@group.v.calendar.google.com',
	            color: 'transparent',
	            textColor: 'red',
	            className: 'google-holiday',
	            allDay: true,
	            order: -1,
	            success: function(googleEvents) { 
	                googleHolidayDates = googleEvents.map(event => {
	                    const holidayDate = new Date(event.start);
	                    return holidayDate.toISOString().split('T')[0]; 
	                }); 
	                 
	                fetchAllSchedules();
	            }
	        }
	    ]   
	});  
    
    $.ajax({
        url: '/home/categories',
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        },
        success: function(categories) {
            categories.forEach(function(category) {
                categoryColors[category.schedule_category_no] = '#' + category.schedule_category_color;
                categoryNames[category.schedule_category_no] = category.schedule_category_name;
            }); 
        }
    });
 
	// 참여자 정보 
	var parMemberNos = [];

	function searchParticipate(memberNo, scheduleNo, callback) {
	    $.ajax({
	        url: '/home/api/participate/member/schedules/' + scheduleNo + '/' + memberNo,
	        method: 'GET',
	        dataType: 'json',
	        headers: {
	            'X-CSRF-TOKEN': csrfToken
	        },
	        success: function(data) {
	            parMemberNos = data.participants.map(participant => participant.member_no); 
	            parMemberNames = data.participants.map(participant => participant.memberName + ' ' + participant.positionName); 
	            callback(parMemberNos, parMemberNames);
	        } 
	    });
	} 
	
	// 예외 일정 참여자 정보 
	var exceptionParMemberNos = [];
	var exceptionParMemberNames = [];  
	
	function searchExceptionParticipate(memberNo, scheduleExceptionNo, callback) {
	    $.ajax({
	        url: '/home/api/participate/member/schedules/exception/' + scheduleExceptionNo + '/' + memberNo,
	        method: 'GET',
	        dataType: 'json',
	        headers: {
	            'X-CSRF-TOKEN': csrfToken
	        },
	        success: function(data) { 
	            exceptionParMemberNos = data.participants.map(participant => participant.member_no); 
	            exceptionParMemberNames = data.participants.map(participant => participant.memberName + ' ' + participant.positionName); 
	            callback(exceptionParMemberNos, exceptionParMemberNames); 
	        }
	    });
	} 
	
	// 회의 일정 참여자 정보 
	var meetingParMemberNos = [];
	var meetingParMemberNames = [];
	
	function searchMeetingParticipate(memberNo, meetingNo, callback) {
	    $.ajax({
	        url: '/home/api/employee/meeting/schedules/' + meetingNo + '/' + memberNo,
	        method: 'GET',
	        dataType: 'json',
	        headers: {
	            'X-CSRF-TOKEN': csrfToken
	        },
	        success: function(data) { 
	            if (data.participants) {
	                meetingParMemberNos = data.participants.map(participant => participant.member_no);
	                meetingParMemberNames = data.participants.map(participant => participant.memberName + ' ' + participant.positionName);
	            }
	            callback(meetingParMemberNos, meetingParMemberNames);
	        }
	    });
	}
	
    function fetchAllSchedules() {
	    Promise.all([
	        fetchSchedules('/home/api/personal/schedules/' + memberNo, 'personalResult'),
	        fetchSchedules('/home/api/department/schedules', 'departmentResult'),
	        fetchSchedules('/home/api/company/schedules', 'scheduleDtos'),
	        fetchSchedules('/home/api/participate/schedules', 'participateResult'),
	        fetchSchedules('/home/api/employee/vacation/schedules', 'vacationResult'),
	        fetchSchedules('/home/api/employee/meeting/schedules', 'meetingResult'),
	        $.ajax({
	            url: '/home/api/repeat/schedules',
	            method: 'GET',
	            headers: { 'X-CSRF-TOKEN': csrfToken }
	        }),
	        $.ajax({
	            url: '/home/api/company/exception/schedules',
	            method: 'GET',
	            headers: { 'X-CSRF-TOKEN': csrfToken }
	        })
	    ]).then(function([personalSchedules, departmentSchedules, companySchedules, participateSchedules, vacationSchedules, meetingSchedules, repeats, exceptions]) {
	        const allEvents = [];
	
	        Promise.all([
	            processSchedules(personalSchedules.personalResult, 'personalResult', repeats, exceptions, allEvents),
	            processSchedules(departmentSchedules.departmentResult, 'departmentResult', repeats, exceptions, allEvents),
	            processSchedules(companySchedules.scheduleDtos, 'scheduleDtos', repeats, exceptions, allEvents),
	            processSchedules(participateSchedules.participateResult, 'participateResult', repeats, exceptions, allEvents),
	            processVacationSchedules(vacationSchedules.vacationResult, allEvents),
	            processMeetingSchedules(meetingSchedules.meetingResult, allEvents)  
	        ]).then(() => {
	            initializeCalendar(allEvents); 
	            filterEvents(); 
	        });
	    });
	}

    
    function processVacationSchedules(vacationSchedules, allEvents) {
	    vacationSchedules.vacationSchedules.forEach(function(vacation) {
	        const event = createVacationEvent(vacation);
	 
	        const startDate = new Date(event.start);
	        const endDate = new Date(event.end);
	        let currentDate = new Date(startDate);
	 
	        let lastValidStart = null;
	        let lastValidEnd = null;
	
	        while (currentDate <= endDate) {
	            const dayOfWeek = currentDate.getUTCDay();  
	            const currentDateString = currentDate.toISOString().split('T')[0];
	 
	            if (dayOfWeek === 0 || dayOfWeek === 6 || googleHolidayDates.includes(currentDateString)) {
	               
	                if (lastValidStart) {
	                    allEvents.push({
	                        ...event,
	                        start: lastValidStart.toISOString().split('T')[0],
	                        end: lastValidEnd.toISOString().split('T')[0]
	                    }); 
	                    lastValidStart = null;
	                    lastValidEnd = null;
	                }
	            } else { 
	                if (!lastValidStart) {
	                    lastValidStart = new Date(currentDate);
	                }
	                lastValidEnd = new Date(currentDate);
	                lastValidEnd.setDate(lastValidEnd.getDate() + 1);
	            }
	
	            currentDate.setDate(currentDate.getDate() + 1); 
	        }
	 
	        if (lastValidStart) {
	            allEvents.push({
	                ...event,
	                start: lastValidStart.toISOString().split('T')[0],
	                end: lastValidEnd.toISOString().split('T')[0]
	            });
	        }
	    }); 
	}
	
	function processMeetingSchedules(meetingSchedules, allEvents) { 
	    if (meetingSchedules && meetingSchedules.meetingSchedules) {
	        meetingSchedules.meetingSchedules.forEach(function(meeting) {
	            const event = createMeetingEvent(meeting);
	            allEvents.push(event);
	        });
	    }
	}
	
	function createMeetingEvent(meeting) { 
		var event = {
		    order: 1,
		    id: 'meeting' + meeting.meeting_no,
		    title: meeting.meeting_name  + ' 회의', 
		    start: meeting.meeting_reservation_date + 'T' + meeting.meeting_reservation_start_time,
		    end: meeting.meeting_reservation_date + 'T' + meeting.meeting_reservation_end_time,
		    allDay: false,
		    backgroundColor: '#fff8db',
		    borderColor: '#fff8db',
		    textColor: '#000000',
		    className: 'meeting-event',
		    extendedProps: {
		        type: 'meetingResult',
		        categoryName: '회의',  
		        positionName: meeting.position_name,
		        departmentName: meeting.department_name,
		        member_no: meeting.member_no,
		        meeting_no: meeting.meeting_no,
		        participant_no: [],
		        participant_name: [],
		        participantsLoaded: false,
		        member_name : meeting.member_name,
		        meeting_reservation_purpose : meeting.meeting_reservation_purpose,
		        meeting_name : meeting.meeting_name,
		        meeting_date : meeting.meeting_reservation_date,
		        meeting_start_time : meeting.meeting_reservation_start_time,
		        meeting_end_time : meeting.meeting_reservation_end_time, 
		    }
		}; 
	     
	    searchMeetingParticipate(event.extendedProps.member_no, event.extendedProps.meeting_no, function(participantNos, participantNames) {
	        event.extendedProps.participant_no = participantNos;
	        event.extendedProps.participant_name = participantNames;
	        event.extendedProps.participantsLoaded = true;
	         
	        const calendarEvent = calendar.getEventById(event.id);
	        if (calendarEvent) {
	            calendarEvent.setExtendedProp('participant_no', participantNos);
	            calendarEvent.setExtendedProp('participant_name', participantNames);
	            calendarEvent.setExtendedProp('participantsLoaded', true);
	        }
	        filterEvents();    
	    });
	
	    return event;
	}
	
	function createVacationEvent(vacation) {  
	    var eventEnd = new Date(vacation.vacation_approval_end_date);
	      
	    var vacation_name = vacation.vacation_type_name === "반차" ? "반차" : "휴가";
	    
	    return {
	        order: -1,
	        id: 'vacation_' + vacation.vacation_approval_no,
	        title: vacation.member_name + ' ' + vacation.position_name + ' ' + vacation_name,
	        start: vacation.vacation_approval_start_date,
	        end: eventEnd,
	        allDay: true,
	        backgroundColor: '#d9d9d9',
	        borderColor: '#d9d9d9',
	        textColor: '#000000',
	        className: 'vacation-event',
	        extendedProps: {
	            type: 'vacationResult',
	            categoryName: vacation.vacation_type_name,
	            department_no: vacation.department_no,
	            positionName: vacation.position_name,
	            departmentName: vacation.department_name,
	            categoryName : vacation.vacation_type_name === "반차" ? "반차" : "휴가"
	        }
	    };
	}
  
    function processSchedules(schedules, type, repeats, exceptions, allEvents) {
	    if (!Array.isArray(schedules)) {
	        return Promise.resolve();
	    }
	
	    const eventPromises = schedules.flatMap(function(schedule) {
	        if (schedule.schedule_repeat === 0) {
	            return createEvent(schedule, type);
	        } else {
	            const repeatInfo = repeats.find(r => r.schedule_no === schedule.schedule_no);
	            if (repeatInfo) {
	                const startDate = new Date(schedule.schedule_start_date);
	                const endDate = repeatInfo.schedule_repeat_end_date ? new Date(repeatInfo.schedule_repeat_end_date) : new Date(startDate.getFullYear() + 1, startDate.getMonth(), startDate.getDate());
	                let currentDate = new Date(startDate);
	
	                const datePromises = [];
	
	                while (currentDate <= endDate) {
	                    const exceptionEvent = exceptions.find(e =>
	                        e.schedule_no === schedule.schedule_no &&
	                        e.schedule_exception_date === formatDate(currentDate)
	                    );
	
	                    if (exceptionEvent && exceptionEvent.schedule_exception_status === 0) {
	                        datePromises.push(createExceptionEvent(exceptionEvent, currentDate, type, schedules[0].member_no));
	                    } else if (exceptionEvent && exceptionEvent.schedule_exception_status === 1) {
	                         
	                    } else {
	                        datePromises.push(createEvent(schedule, type, currentDate, repeatInfo));
	                    }
	
	                    currentDate = calculateNextRepeatDate(currentDate, repeatInfo);
	                }
	
	                return datePromises;
	            }
	        }
	        return [];
	    });
	
	    return Promise.all(eventPromises).then(events => {
	        events.flat().forEach(event => {
	            allEvents.push(event);
	            calendar.addEvent(event);
	        });
	    });
	}
	
    function createEvent(schedule, type, date, repeatInfo) {
        var eventStart = date || new Date(schedule.schedule_start_date);
        var eventEnd = schedule.schedule_end_date ? new Date(schedule.schedule_end_date) : null;
    	 
        if (date) { 
            eventEnd = new Date(date);
            eventEnd.setHours(new Date(schedule.schedule_end_date).getHours());
            eventEnd.setMinutes(new Date(schedule.schedule_end_date).getMinutes());
        }  
        
        if (schedule.schedule_allday === 1 && eventEnd) {
	        eventEnd.setDate(eventEnd.getDate() + 1);
	    }
	    
        var event = {
            order: 1,
            id: schedule.schedule_no,
            title: schedule.schedule_title,
            start: formatDate(eventStart) + (schedule.schedule_start_time ? 'T' + schedule.schedule_start_time : ''),
            end: eventEnd ? formatDate(eventEnd) + (schedule.schedule_end_time ? 'T' + schedule.schedule_end_time : '') : null,
            allDay: schedule.schedule_allday === 1,
            backgroundColor: categoryColors[schedule.schedule_category_no] || '#3788d8',
            borderColor: categoryColors[schedule.schedule_category_no] || '#3788d8',
            textColor: '#000000',
            className: type + '-event',
            extendedProps: {
                type: type,
                categoryName: categoryNames[schedule.schedule_category_no],
                comment: schedule.schedule_comment,
                repeatOption: schedule.schedule_repeat,
                createDate: schedule.schedule_create_date,
                startDate: eventStart ? formatDate(eventStart) : null,
                endDate: eventEnd ? formatDate(eventEnd) : null,
                startTime: schedule.schedule_allday === 0 ? schedule.schedule_start_time : null,
                endTime: schedule.schedule_allday === 0 ? schedule.schedule_end_time : null,
                department_no: schedule.department_no,
                member_no: schedule.member_no,
                participant_no: [],
                participantsLoaded: false,
                isException: schedule.isException || false,
                repeatType: repeatInfo ? repeatInfo.schedule_repeat_type : null,
	            repeatDay : repeatInfo ? repeatInfo.schedule_repeat_day : null,
	            repeatWeek: repeatInfo ? repeatInfo.schedule_repeat_week : null,
	            repeatDate : repeatInfo ? repeatInfo.schedule_repeat_date : null,
	            repeatMonth: repeatInfo ? repeatInfo.schedule_repeat_month : null,
	            memberName : schedule.member_name,
	            participant_name : [],
	            positionName : schedule.position_name,
	            departmentName : schedule.department_name
            }
        };
    
        if (type === 'participateResult') {
            searchParticipate(event.extendedProps.member_no, schedule.schedule_no, function(participantNos, participantNames) {
                event.extendedProps.participant_no = participantNos; 
                event.extendedProps.participant_name = participantNames; 
                event.extendedProps.participantsLoaded = true;
                calendar.getEventById(event.id).setExtendedProp('participant_no', participantNos);
                calendar.getEventById(event.id).setExtendedProp('participant_name', participantNames);
                calendar.getEventById(event.id).setExtendedProp('participantsLoaded', true);   
            	filterEvents();  
            });
        }  
          
        return event;
    }

    function createExceptionEvent(exceptionEvent, currentDate, type, ori_memberNo) { 
	    const startDate = new Date(exceptionEvent.schedule_exception_start_date);
	    let endDate = exceptionEvent.schedule_exception_end_date ? new Date(exceptionEvent.schedule_exception_end_date) : null;
	
	    if (!exceptionEvent.schedule_exception_start_time && !exceptionEvent.schedule_exception_end_time && endDate) {
	        endDate.setDate(endDate.getDate() + 1);
	    }
	
	    var event = {
	        order: 1,
	        id: exceptionEvent.schedule_exception_no,
	        title: exceptionEvent.schedule_exception_title,
	        start: exceptionEvent.schedule_exception_start_date + (exceptionEvent.schedule_exception_start_time ? 'T' + exceptionEvent.schedule_exception_start_time : ''),
	        end: endDate ? formatDate(endDate) + (exceptionEvent.schedule_exception_end_time ? 'T' + exceptionEvent.schedule_exception_end_time : '') : null,
	        allDay: !exceptionEvent.schedule_exception_start_time && !exceptionEvent.schedule_exception_end_time,
	        backgroundColor: categoryColors[exceptionEvent.schedule_category_no] || '#3788d8',
	        borderColor: categoryColors[exceptionEvent.schedule_category_no] || '#3788d8',
	        textColor: '#000000',
	        className: type + '-event',
	        extendedProps: {
	            type: type,
	            categoryName: categoryNames[exceptionEvent.schedule_category_no],
	            comment: exceptionEvent.schedule_exception_comment,
	            createDate: exceptionEvent.schedule_exception_create_date,
	            endDate: exceptionEvent.schedule_exception_end_date ? exceptionEvent.schedule_exception_end_date : null,
	            startTime: exceptionEvent.schedule_exception_allday === 0 ? exceptionEvent.schedule_exception_start_time : null,
	            endTime: exceptionEvent.schedule_exception_allday === 0 ? exceptionEvent.schedule_exception_end_time : null,
	            exceptionNo: exceptionEvent.schedule_exception_no,
	            isException: true,
	            exceptionType: exceptionEvent.schedule_exception_type,
	            originNo: exceptionEvent.schedule_no,
	            member_no: ori_memberNo,
	            department_no: exceptionEvent.department_no,
	            excepetion_participant_no: [],
	            excepetion_participant_name: [],
	            excepetion_participantsLoaded: false,
	            memberName: exceptionEvent.member_name, 
	            positionName: exceptionEvent.position_name,
	            departmentName: exceptionEvent.department_name
	        }
	    };
	
	    return new Promise((resolve) => {
	        if (type === 'participateResult') {  
	            searchExceptionParticipate(exceptionEvent.member_no,exceptionEvent.schedule_exception_no, function(ExceptionparticipantNos, ExceptionparticipantNames) {
	                event.extendedProps.excepetion_participant_no = ExceptionparticipantNos; 
	                event.extendedProps.excepetion_participant_name = ExceptionparticipantNames; 
	                event.extendedProps.excepetion_participantsLoaded = true;
	                 
	                resolve(event);
	            });
	        } else {
	            resolve(event);
	        }
	    });
	}

	function fetchSchedules(url, type) {
        return $.ajax({
            url: url,
            method: 'GET',
            headers: { 'X-CSRF-TOKEN': csrfToken }
        }).then(function(response) {
            var result = {};
            result[type] = response;
            return result;
        });
    }
    
    function calculateNextRepeatDate(currentDate, repeatInfo) {
        switch (repeatInfo.schedule_repeat_type) {
            case 1: // 매일
                return new Date(currentDate.setDate(currentDate.getDate() + 1));
            case 2: // 매주
                return new Date(currentDate.setDate(currentDate.getDate() + 7));
            case 3: // 매월 n일
                var nextMonth = new Date(currentDate.setMonth(currentDate.getMonth() + 1));
                nextMonth.setDate(repeatInfo.schedule_repeat_date);
                return nextMonth;
            case 4: // 매월 n번째 요일
                var nextMonth = new Date(currentDate.setMonth(currentDate.getMonth() + 1));
                while (nextMonth.getDay() !== repeatInfo.schedule_repeat_day || 
                       Math.floor((nextMonth.getDate() - 1) / 7) + 1 !== repeatInfo.schedule_repeat_week) {
                    nextMonth.setDate(nextMonth.getDate() + 1);
                }
                return nextMonth;
            case 5: // 매년
                return new Date(currentDate.setFullYear(currentDate.getFullYear() + 1));
            default:
                return new Date(currentDate.setDate(currentDate.getDate() + 1));
        }
    }

    function formatDate(date) {
        return date.getFullYear() + '-' + 
               String(date.getMonth() + 1).padStart(2, '0') + '-' + 
               String(date.getDate()).padStart(2, '0');
    } 
 	
	function filterEventsByDate(events, date) {
	    return events.filter(event => { 
	        if (event.start && event.display !== 'none') {  
	            const eventStart = new Date(event.start);
	            let eventEnd;
	 
	            if (event.allDay) {
	                eventEnd = new Date(event.end);
	                eventEnd.setDate(eventEnd.getDate() - 1);
	            } else {
	                eventEnd = new Date(event.start); 
	            }
	 
	            eventStart.setHours(0, 0, 0, 0); 
	            eventEnd.setHours(0, 0, 0, 0); 
	
	            const selectedDate = new Date(date);
	            selectedDate.setHours(0, 0, 0, 0); 
	
	            return selectedDate >= eventStart && selectedDate <= eventEnd;
	        }
	        return false;   
	    });
	} 
	
	function displaySelectedDateEvents(selectedDateEvents, selectedDate) {
	    const selectedDateEventsList = document.getElementById('schedule_selected_date_list');
	    selectedDateEventsList.innerHTML = '';  
	    const home_schedule_date = document.getElementById('schedule_home_date');
	  
	    home_schedule_date.innerHTML = selectedDate;  
		 
	    const filteredEvents = selectedDateEvents.filter(event => event.display !== 'none');
			  
	    if (filteredEvents.length === 0) {
	        selectedDateEventsList.innerHTML = '<li>일정이 없습니다.</li>';
	    } else {
	        filteredEvents.forEach(event => {
	            const listItem = document.createElement('li');
	            if (event.allDay) {
	                listItem.textContent = `[${event.extendedProps.categoryName}] ${event.title}`;					
	            } else if (!event.allDay && event.extendedProps.type === 'meetingResult') {
	                listItem.textContent = `[${event.extendedProps.categoryName}] ${event.title} ${event.extendedProps.meeting_start_time} ~ ${event.extendedProps.meeting_end_time}`;	
	            } else {
	                listItem.textContent = `[${event.extendedProps.categoryName}] ${event.title} ${event.extendedProps.startTime} ~ ${event.extendedProps.endTime}`;	
	            }
	            selectedDateEventsList.appendChild(listItem);
	        });
	    }
	}
 
    function initializeCalendar(events) {
    	const calendarEl = document.getElementById('calendar');
	    calendar = new FullCalendar.Calendar(calendarEl, {
	        initialView: 'dayGridMonth',
	        locale: 'ko',
	        headerToolbar: {
	            left: 'prev,next',
	            center: 'title',
	            right: 'today'
	        },
	        contentHeight: 'auto',
	        handleWindowResize: true,
	        fixedWeekCount: false,
	        eventDidMount: function(info) {
	            info.el.style.cursor = 'pointer';
	            if (info.event.extendedProps.description === '공휴일') {
	                const dateCell = info.el.closest('.fc-daygrid-day');
	                if (dateCell) {
	                    const dateCellContent = dateCell.querySelector('.fc-daygrid-day-number');
	                    if (dateCellContent) {
	                        dateCellContent.style.color = '#FF0000';
	                    } 
	                }
	            }
	        },
	        googleCalendarApiKey: 'AIzaSyBaQi-ZLyv7aiwEC6Ca3C19FE505Xq2Ytw',
	        eventSources: [
	            {
	                googleCalendarId: 'ko.south_korea#holiday@group.v.calendar.google.com',
	                color: 'transparent',
	                textColor: 'red',
	                className: 'google-holiday',
	                allDay: true,
	                order: -1,
	                type: 'holiday'
	            },
	            {
	                events: events
	            }
	        ],
	        eventOrder: '-order,-allDay,start',
	        buttonText: {
	            today: '오늘'
	        },
	        eventClick: function(info) { 
				console.log(info.event);
				const eventStart = new Date(info.event.start.getTime() - (info.event.start.getTimezoneOffset() * 60000));
			    pickStartDate = eventStart.toISOString().split('T')[0];
				 
			    let eventEnd;
			    if (info.event.end) { 
			        if (info.event.allDay) {
			            eventEnd = new Date(info.event.end.getTime() - 24 * 60 * 60 * 1000 - (info.event.end.getTimezoneOffset() * 60000));
			        } else {
			            eventEnd = new Date(info.event.end.getTime() - (info.event.end.getTimezoneOffset() * 60000));
			        }
			    } else {
			        eventEnd = eventStart;
			    }
			    pickEndDate = eventEnd.toISOString().split('T')[0];  
				 
			    if (info.event.extendedProps.isException) { 
			        showExceptionEventModal(info.event);
			    } 
			    else {
				    if (info.event.extendedProps.type === 'personalResult' || info.event.extendedProps.type === 'participateResult'
		            	|| info.event.extendedProps.type === 'scheduleDtos' || info.event.extendedProps.type === 'departmentResult') {
				        const eventId = info.event.id; 
				        showEventModalById(calendar, eventId, info.event);
				    } else if (info.event.extendedProps.type === 'vacationResult') {
						showVacationModal(info.event);
					} else if (info.event.extendedProps.type === 'meetingResult') {
						const eventId = info.event.id;
				        showMeetingModalById(calendar, eventId, info.event);
					} else {
				        info.jsEvent.preventDefault();
				    }   
				}
			    
            },
	        dayCellContent: function(info) {
	            var number = document.createElement("a");
	            number.classList.add("fc-daygrid-day-number");
	            number.innerHTML = info.dayNumberText.replace("일", '').replace("日", "");
	
	            var wrapper = document.createElement("div");
	            wrapper.classList.add("fc-daygrid-day-top");
	            wrapper.appendChild(number);
	
	            if (info.view.type === "dayGridMonth") {
	                return { domNodes: [wrapper] };
	            }
	            return { domNodes: [] };
	        },
	        moreLinkContent: function(args) {
			    return ' + ' + args.num;
			},
	        dayMaxEvents: 2,
	        dateClick: function(info) {
	            selectedDate = info.dateStr;  
	
	            const eventDateInput = document.getElementById('eventDate');
	            if (eventDateInput) {  
	                eventDateInput.value = selectedDate;
	                eventDateInput.dispatchEvent(new Event('change'));
	            }  
	
	            const selectedDateEvents = filterEventsByDate(events, selectedDate);
	            displaySelectedDateEvents(selectedDateEvents, selectedDate);
	        }, 
	        eventTimeFormat: {
			    hour: '2-digit',    
			    minute: '2-digit',  
			    meridiem: false,    
			    hour12: false       
		    }
	    });
	 
	    const today = new Date();
	    const formattedToday = `${today.getFullYear()}-${('0' + (today.getMonth() + 1)).slice(-2)}-${('0' + today.getDate()).slice(-2)}`;
	 
	    const selectedDateEvents = filterEventsByDate(events, formattedToday);
	    displaySelectedDateEvents(selectedDateEvents, formattedToday);
	
	    filterEvents(); 
	    calendar.render();
	}

	
	function filterEvents() { 
	  calendar.getEvents().forEach(function(event) {
	        const eventDepartmentNo = event.extendedProps.department_no;
	        const participantNos = event.extendedProps.participant_no || []; 
	        let shouldDisplay = false;
	
	        // 개인 일정
	        if (event.extendedProps.type === 'personalResult' && (event.extendedProps.member_no.toString() === memberNo)) {
	            shouldDisplay = true;
	        }
	        // 참여자 일정
	        else if (event.extendedProps.type === 'participateResult') {
	            if (event.extendedProps.participantsLoaded) {
	                shouldDisplay = participantNos.includes(parseInt(memberNo));
	            } else {
	                shouldDisplay = true;
	            }
	        }  
	        // 부서 일정
	        else if (event.extendedProps.type === 'departmentResult') {
	            if (eventDepartmentNo.toString() === userDepartmentNo) {
	                shouldDisplay = true;  
	            } else {
	                shouldDisplay = false;  
	            }
	        }
	        // 회의 일정
	        else if (event.extendedProps.type === 'meetingResult') {
	            if (event.extendedProps.member_no.toString() === memberNo || participantNos.includes(parseInt(memberNo))) {
	                shouldDisplay = true;  
	            }
	        }  
	        // 휴가, 전사
	        else if (event.extendedProps.type === 'vacationResult' || event.extendedProps.type === 'scheduleDtos') {
				shouldDisplay = true;  
			}
	        event.setProp('display', shouldDisplay ? 'auto' : 'none'); 
	    });
	}
	
	document.getElementById('closeScheduleModalBtn').addEventListener('click', function() {    
		console.log("close");
		document.getElementById('eventViewModal').style.display = 'none';
	});
	
	window.addEventListener('click', function(event) {
	    var modal = document.getElementById('eventViewModal');
	     
	    if (event.target === modal) {
	        modal.style.display = 'none';
	    }
	});

 	// 일정 상세
	function showEventModalById(calendar, eventId, event) {    
	    if (!event) {
	        console.error("일정을 찾을 수 없습니다.");
	        return;
	    }
	    const modal = document.getElementById('eventViewModal');
	    const title = document.getElementById('eventViewTitle');
	    const dateRange = document.getElementById('eventViewDateRange');
	    const category = document.getElementById('eventViewCategory');
	    const comment = document.getElementById('eventViewComment');
	    const createdDate = document.getElementById('eventViewCreatedDate');
	    const repeatInfo = document.getElementById('eventViewRepeatInfo'); 
	    const hiddenEventId = document.getElementById('eventId'); 
 		const hiddenIsException = document.getElementById('isException'); 
 		hiddenIsException.value = 1;
	    title.textContent = event.title;
	    category.textContent = `[` + event.extendedProps.categoryName + `]`;
	  	
	    const startDate = pickStartDate;
	    const endDate = pickEndDate;
	    const startTime = event.extendedProps.startTime;
	    const endTime = event.extendedProps.endTime;
	 	const allDay = event.allDay; 
	 	
		document.getElementById('eventViewDateRange').style.display = 'block';
		document.getElementById('eventViewComment').style.display = 'block';
	    document.getElementById('eventViewHr').style.display = 'block';
	    document.getElementById('eventViewRepeatInfo').style.display = 'block';
	    document.getElementById('eventViewCreatedDate').style.display = 'block';  
	    document.getElementById('eventViewHr').style.display = 'block';   
		
		document.getElementById('par_join').style.display = 'none'; 
		document.getElementById('par_create_name').style.display = 'none'; 
		document.getElementById('par_create_name').style.display = 'none'; 
		document.getElementById('par_join_name').style.display = 'none'; 
		document.getElementById('meeting_name').style.display = 'none'; 
	 	document.getElementById('departmentName').style.display = 'block';  
	 	 
	    if(allDay && startDate === endDate) {
	    	dateRange.textContent = `${startDate}`; 
		} else if(!allDay) {  
			if(startTime === endTime) { 
				dateRange.textContent = `${startDate} ${startTime}`;				
			} 
			else {
				dateRange.textContent = `${startDate} ${startTime} ~ ${endTime}`;	
			}
		} else {
			dateRange.textContent = `${startDate} ~ ${endDate}`; 
		}
		 
		if (event.extendedProps.type === 'departmentResult' && event.extendedProps.member_no !== Number(memberNo)) {  
		    document.getElementById('departmentName').style.display = 'block'; 
		} else if (event.extendedProps.type === 'scheduleDtos') {  
		    document.getElementById('departmentName').style.display = 'none'; 
		} else if (event.extendedProps.type === 'participateResult' && event.extendedProps.member_no !== Number(memberNo)) {  
		    document.getElementById('departmentName').style.display = 'none'; 
		}
		else {
		} 
		
		document.getElementById('event_modal_vacation').style.height = '300px'; 
		
		if (event.extendedProps.type === 'participateResult') {
			document.getElementById('event_modal_vacation').style.height = '500px'; 
			document.getElementById('par_join').style.display = 'block'; 
			document.getElementById('par_create_name').style.display = 'block'; 
			document.getElementById('par_create_name').style.display = 'block'; 
			document.getElementById('par_join_name').style.display = 'block'; 
			const createName = document.getElementById('par_create_name');
			createName.innerHTML = `[등록자]<br>${event.extendedProps.memberName} ${event.extendedProps.positionName}`;

		    const joinName = document.getElementById('par_join_name');
		 
		    if (event.extendedProps.participant_name && Array.isArray(event.extendedProps.participant_name)) {
			    joinName.innerHTML = event.extendedProps.participant_name
			        .map(name => `${name}`).join('<br>');
			} 
		    
		    document.getElementById('event_modal_vacation').style.height = '500px'; 
		}
		else if (event.extendedProps.type === 'departmentResult') {
			document.getElementById('par_join').style.display = 'none'; 
			document.getElementById('par_create_name').style.display = 'block'; 
			document.getElementById('par_join_name').style.display = 'none'; 
			document.getElementById('meeting_name').style.display = 'block'; 
			const departmentName = document.getElementById('departmentName');
			departmentName.textContent = event.extendedProps.departmentName + ' 부서 일정';
			
			const createName = document.getElementById('par_create_name');
			createName.innerHTML = `[등록자]<br>${event.extendedProps.memberName} ${event.extendedProps.positionName}`;
		}
		else {
			 document.getElementById('par_join').style.display = 'none'; 
		     document.getElementById('par_create_name').style.display = 'none'; 
			 document.getElementById('meeting_name').style.display = 'none'; 
			 document.getElementById('departmentName').style.display = 'none';
		}
 
		
		document.getElementById('isRecurring_view').value = 0;
	 	hiddenEventId.value = eventId;
	    comment.textContent = event.extendedProps.comment; 
	    createdDate.textContent = event.extendedProps.createDate.substr(0,10) + ` 등록`; 
	    repeatInfo.textContent = getRepeatInfoText(event.extendedProps.repeatType, event.extendedProps.repeatDay, event.extendedProps.repeatWeek , event.extendedProps.repeatDate , event.extendedProps.repeatMonth);
	    modal.style.display = 'block';  
	}  
	
	// 예외 상세 모달
	function showExceptionEventModal(event) {
	    const modal = document.getElementById('eventViewModal');
	    const title = document.getElementById('eventViewTitle');
	    const dateRange = document.getElementById('eventViewDateRange');
	    const category = document.getElementById('eventViewCategory');
	    const comment = document.getElementById('eventViewComment');
	    const createdDate = document.getElementById('eventViewCreatedDate');
	    const repeatInfo = document.getElementById('eventViewRepeatInfo'); 
	    const hiddenEventId = document.getElementById('eventId'); 
		const hiddenIsException = document.getElementById('isException'); 
		document.getElementById('isRecurring').value = 2;
		hiddenIsException.value = 0;
	    title.textContent = event.title;
	    category.textContent = `[${event.extendedProps.categoryName}]`;
	   
		const startDate = pickStartDate;
	    const endDate = pickEndDate;
	    const startTime = event.extendedProps.startTime;
	    const endTime = event.extendedProps.endTime;
	    document.getElementById('isRecurring_view').value = 2; 
	    
		document.getElementById('eventViewDateRange').style.display = 'block';
		document.getElementById('eventViewComment').style.display = 'block';
	    document.getElementById('eventViewHr').style.display = 'block';
	    document.getElementById('eventViewRepeatInfo').style.display = 'block';
	    document.getElementById('eventViewCreatedDate').style.display = 'block';
	    document.getElementById('event_repeat_title').style.display = 'block';  
	    document.getElementById('eventViewHr').style.display = 'block'; 
    
		document.getElementById('par_join').style.display = 'none'; 
		document.getElementById('par_create_name').style.display = 'none'; 
		document.getElementById('par_create_name').style.display = 'none'; 
		document.getElementById('par_join_name').style.display = 'none'; 
		document.getElementById('meeting_name').style.display = 'none'; 
	     
	    if(startDate === endDate) {
	    	dateRange.textContent = `${startDate}`; 
		} else if(endDate === null) {
			if(startTime === endTime) {
				dateRange.textContent = `${startDate} ${startTime}`;				
			} 
			else {
				dateRange.textContent = `${startDate} ${startTime} ~ ${endTime}`;	
			}
		} else {
			dateRange.textContent = `${startDate} ~ ${endDate}`; 
		}
		
		if (event.extendedProps.type === 'participateResult') { 
			document.getElementById('event_modal_vacation').style.height = '500px'
			const createName = document.getElementById('par_create_name');
			createName.innerHTML = `[등록자]<br>${event.extendedProps.memberName} ${event.extendedProps.positionName}`;

		    const joinName = document.getElementById('par_join_name');
		 	 
			if (event.extendedProps.excepetion_participant_name && Array.isArray(event.extendedProps.excepetion_participant_name)) {
			    joinName.innerHTML = event.extendedProps.excepetion_participant_name
			        .map(name => `${name}`).join('<br>');
			}

		} 
		else if (event.extendedProps.type === 'departmentResult') {
			document.getElementById('par_join').style.display = 'none'; 
			document.getElementById('par_create_name').style.display = 'none'; 
			document.getElementById('par_join_name').style.display = 'none'; 
			document.getElementById('meeting_name').style.display = 'block'; 
			const departmentName = document.getElementById('departmentName');
			departmentName.textContent = event.extendedProps.departmentName;
		}
		else {
			 document.getElementById('par_join').style.display = 'none'; 
		     document.getElementById('par_create_name').style.display = 'none'; 
			 document.getElementById('meeting_name').style.display = 'none'; 
			 document.getElementById('departmentName').style.display = 'none';
		}
  
	    hiddenEventId.value = event.extendedProps.exceptionNo; 
	    comment.textContent = event.extendedProps.comment; 
	    createdDate.textContent = event.extendedProps.createDate.substr(0,10) + ` 등록`; 
	    repeatInfo.textContent = '';
		
		document.getElementById('event_modal_vacation').style.height = '300px'; 
	    modal.style.display = 'block';  
	}
	
	function getRepeatInfoText(repeatType, repeatDay, repeatWeek, repeatDate, repeatMonth) { 
	    let info_type = '';
	    let info_day = ''; 
	
	    switch(repeatType) {
	        case 1:  
	            info_type = '매일 반복';
	            break;
	        case 2:  
	            info_type = '매주';
	            info_day = getDayInformation(repeatDay);  
	            break;
	        case 3:  
	            info_type = `매월 ${repeatDate}일 반복`;
	            break;
	        case 4:  
	            info_type = `매월 ${repeatWeek}번째 ${getDayInformation(repeatDay)} 반복`;
	            break;
	        case 5: 
	            info_type = `매년 ${repeatMonth}월 ${repeatDate}일 반복`;
	            break;
	        default:
	            info_type = '';
	            break;
	    }
	    
	    return info_type + (info_day ? ' ' + info_day + ' 반복' : '');
	}
	 
	function getDayInformation(day) {
	    const days = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일'];
	    return days[day];
	} 
	
	function showMeetingModalById(calendar, eventId, event) { 
	    if (!event) {
	        console.error("일정을 찾을 수 없습니다.");
	        return;
	    }
	    const modal = document.getElementById('eventViewModal');
	    const title = document.getElementById('eventViewTitle'); 
	    const category = document.getElementById('eventViewCategory');
	    const comment = document.getElementById('eventViewComment');   
	    const dateRange = document.getElementById('eventViewDateRange');  
	    title.textContent = event.title;
	    category.textContent = `[` + event.extendedProps.categoryName + `]`;
	  	comment.textContent = event.extendedProps.meeting_reservation_purpose;
	    
		dateRange.textContent = `${event.extendedProps.meeting_date} ${event.extendedProps.meeting_start_time} ~ ${event.extendedProps.meeting_end_time}`;

		document.getElementById('eventViewDateRange').style.display = 'block';
		document.getElementById('eventViewComment').style.display = 'block';
	    document.getElementById('eventViewHr').style.display = 'block';
	    document.getElementById('eventViewRepeatInfo').style.display = 'block';
	    document.getElementById('eventViewCreatedDate').style.display = 'block';    
	 	  
	    document.getElementById('event_modal_vacation').style.height = '500px'; 
		document.getElementById('par_join').style.display = 'block'; 
		document.getElementById('par_create_name').style.display = 'block'; 
		document.getElementById('par_create_name').style.display = 'block'; 
		document.getElementById('meeting_name').style.display = 'block'; 
		document.getElementById('par_join_name').style.display = 'block'; 
	    const createName = document.getElementById('par_create_name');
	    createName.innerHTML = `[예약자]<br>${event.extendedProps.member_name} ${event.extendedProps.positionName}`;
	
	    const joinName = document.getElementById('par_join_name');
	 
	    if (event.extendedProps.participant_name && Array.isArray(event.extendedProps.participant_name)) {
		    joinName.innerHTML = event.extendedProps.participant_name
		        .map(name => `${name}`).join('<br>');
		} 
		
		const meetingName = document.getElementById('meeting_name');
		meetingName.textContent = `[회의실] ${event.extendedProps.meeting_name}`;
	    
	    document.getElementById('eventViewHr').style.display = 'none'; 
	    document.getElementById('eventViewRepeatInfo').style.display = 'none';
	    document.getElementById('eventViewCreatedDate').style.display = 'none';
	    document.getElementById('event_modal_vacation').style.height = '500px'; 
   		modal.style.display = 'block';  
	}   
	
	// 휴가 모달
	function showVacationModal(event) {
	    const modal = document.getElementById('eventViewModal');
	    const title = document.getElementById('eventViewTitle'); 
	    const category = document.getElementById('eventViewCategory'); 
	    
	    const categoryname = event.extendedProps.categoryName === "반차" ? "반차" : "휴가";
	    title.textContent = event.extendedProps.departmentName + ' '+ event.title;
	    category.textContent = `[` + categoryname + `]`; 
	    
		document.getElementById('eventViewDateRange').style.display = 'none';
		document.getElementById('eventViewComment').style.display = 'none';
	    document.getElementById('eventViewHr').style.display = 'none';
	    document.getElementById('eventViewRepeatInfo').style.display = 'none';
	    document.getElementById('eventViewCreatedDate').style.display = 'none'; 
		document.getElementById('departmentName').style.display = 'none';
		document.getElementById('event_modal_vacation').style.height = '150px'; 
		
		
		document.getElementById('par_join').style.display = 'none'; 
		document.getElementById('par_join_name').style.display = 'none'; 
	    document.getElementById('par_create_name').style.display = 'none'; 
		document.getElementById('par_create_name').style.display = 'none';  
			 
	    modal.style.display = 'block';  
	}
});