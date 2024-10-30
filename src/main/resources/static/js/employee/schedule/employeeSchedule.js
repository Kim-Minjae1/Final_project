document.addEventListener('DOMContentLoaded', function() { 
	let participantMembers = [];
    var csrfToken = document.querySelector('input[name="_csrf"]').value;
    var calendarEl = document.getElementById('calendar');
    var calendar;
    
	const startInput = document.getElementById('startTime');
    const endInput = document.getElementById('endTime'); 
    
    var categoryColors = {};
    var categoryNames = {};
   
    var memberNo = document.getElementById('memberNo').value; 
    var userDepartmentNo = document.getElementById('departmentNo').value; 
    var createEmployeeScheduleForm = document.getElementById("eventForm");
    fetchDepartmentInfo();   
    
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
       
    function fetchDepartmentInfo() {
        $.ajax({
            url: '/api/schedule/department/list',
            method: 'GET',
            contentType: 'application/json',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .done(function(departments) {
            const departmentCheckboxesDiv = $('#departmentCheckboxes');
            departmentCheckboxesDiv.empty();
            
            departments.forEach(function(department) {
                const checkbox = $(`
                	<div class="department_check_row">
                    <input type="checkbox" class="department-checkbox" id="dept_${department.department_no}" data-department-no="${department.department_no}">
                    <label for="dept_${department.department_no}">${department.department_name}</label></div>
                `);
                
                departmentCheckboxesDiv.append(checkbox); 
                
                $(`#dept_${department.department_no}`).on('change', filterEvents);
            });
            
            updateCheckboxes(memberNo);  
        });
    }

    function updateCheckboxes(memberNo) {
        $.ajax({
            url: '/api/schedule/checks/' + memberNo,
            method: 'GET',
            dataType: 'json',
            success: function (data) {
                const UserIncheckbox = $(`[data-department-no="${userDepartmentNo}"]`);  
                UserIncheckbox.prop('checked', true); 
                
                data.forEach(function (scheduleChecks) {
                    const departmentNo = scheduleChecks.department_no; 
                    const checkbox = $(`[data-department-no="${departmentNo}"]`); 
                      
                    if (checkbox.length) {   
                        if (scheduleChecks.schedule_check_status === 0) {
                            checkbox.prop('checked', true); 
                        }
                    } 
                });
            }
        });
    }  
   	 
    $('#all_company_schedule').on('change', function () {
       updateScheduleCheck(memberNo, 1, $(this).prop('checked'));
    });

    $('#departmentCheckboxes').on('change', '.department-checkbox', function () {
        const departmentNo = $(this).data('department-no'); 
        updateScheduleCheck(memberNo, departmentNo, $(this).prop('checked'));
    });
   
    function updateScheduleCheck(memberNo, departmentNo, isChecked) {
       const scheduleCheckStatus = isChecked ? 0 : 1; 
       filterEvents();
       $.ajax({
         url: '/api/schedule/checks/save',
         method: 'POST',
         contentType: 'application/json',
         data: JSON.stringify({
           member_no: memberNo,
           department_no: departmentNo,
           schedule_check_status: scheduleCheckStatus
         }),
         headers: {
            'X-CSRF-TOKEN': csrfToken
        },
         dataType: 'json'
       });
    }

    $.ajax({
        url: '/categories',
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
	        url: '/api/participate/member/schedules/' + scheduleNo + '/' + memberNo,
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
	        url: '/api/participate/member/schedules/exception/' + scheduleExceptionNo + '/' + memberNo,
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
	        url: '/api/employee/meeting/schedules/' + meetingNo + '/' + memberNo,
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
	    fetchSchedules('/api/personal/schedules/' + memberNo, 'personalResult'),
	    fetchSchedules('/api/department/schedules', 'departmentResult'),
	    fetchSchedules('/api/company/schedules', 'scheduleDtos'),
	    fetchSchedules('/api/participate/schedules', 'participateResult'),
	    fetchSchedules('/api/employee/vacation/schedules', 'vacationResult'),
	    fetchSchedules('/api/employee/meeting/schedules', 'meetingResult'),
	    $.ajax({
	      url: '/api/repeat/schedules',
	      method: 'GET',
	      headers: { 'X-CSRF-TOKEN': csrfToken }
	    }),
	    $.ajax({
	      url: '/api/company/exception/schedules',
	      method: 'GET',
	      headers: { 'X-CSRF-TOKEN': csrfToken }
	    })
	  ]).then(function([personalSchedules, departmentSchedules, companySchedules, 
	                    participateSchedules, vacationSchedules, meetingSchedules, 
	                    repeats, exceptions]) {
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
	 
	function loadAllParticipants(meetings) {
	  return Promise.all(meetings.map(meeting => {
	    return new Promise((resolve) => {
	      searchMeetingParticipate(meeting.member_no, meeting.meeting_reservation_no, 
	        (participantNos, participantNames) => {
	          resolve({
	            meetingNo: meeting.meeting_reservation_no,
	            participantNos,
	            participantNames
	          });
	        });
	    });
	  }));
	}

	function processMeetingSchedules(meetingSchedules, allEvents) {
	  if (meetingSchedules && meetingSchedules.meetingSchedules) {
	    const meetings = meetingSchedules.meetingSchedules;
	     
	    return loadAllParticipants(meetings).then(participantsData => { 
	      const participantsMap = participantsData.reduce((acc, data) => {
	        acc[data.meetingNo] = {
	          nos: data.participantNos,
	          names: data.participantNames
	        };
	        return acc;
	      }, {});
	 
	      meetings.forEach(meeting => {
	        const participants = participantsMap[meeting.meeting_reservation_no];
	        const event = createMeetingEvent(meeting, participants);
	        allEvents.push(event);
	      });
	    });
	  }
	  return Promise.resolve(); 
	}

	
	function createMeetingEvent(meeting, participants) {
  var event = {
    order: 1,
    id: 'meeting' + meeting.meeting_no,
    title: meeting.meeting_name + ' 회의',
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
      participant_no: participants ? participants.nos : [],
      participant_name: participants ? participants.names : [],
      participantsLoaded: true,
      member_name: meeting.member_name,
      meeting_reservation_purpose: meeting.meeting_reservation_purpose,
      meeting_name: meeting.meeting_name,
      meeting_date: meeting.meeting_reservation_date,
      meeting_start_time: meeting.meeting_reservation_start_time,
      meeting_end_time: meeting.meeting_reservation_end_time,
      meeting_reservation_no: meeting.meeting_reservation_no
    }
  };
  
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
	    var eventEnd;
	
	    if (date) { 
	        const originalStart = new Date(schedule.schedule_start_date);
	        const originalEnd = new Date(schedule.schedule_end_date);
	        const duration = originalEnd - originalStart;
	
	        eventEnd = new Date(date.getTime() + duration);
	    } else {
	        eventEnd = schedule.schedule_end_date ? new Date(schedule.schedule_end_date) : null;
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
	            repeatDay: repeatInfo ? repeatInfo.schedule_repeat_day : null,
	            repeatWeek: repeatInfo ? repeatInfo.schedule_repeat_week : null,
	            repeatDate: repeatInfo ? repeatInfo.schedule_repeat_date : null,
	            repeatMonth: repeatInfo ? repeatInfo.schedule_repeat_month : null,
	            memberName: schedule.member_name,
	            participant_name: [],
	            positionName: schedule.position_name,
	            departmentName: schedule.department_name
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
 	
    function initializeCalendar(events) {
        calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            headerToolbar: {
		      left: 'dayGridMonth,timeGridWeek,timeGridDay,listMonth,prevYear, prev',
		      center: 'title',
		      right: 'next, nextYear, today, customYearPicker, customMonthPicker, customDayPicker'
		    },
		    customButtons: {
		      customYearPicker: {
		        text: '년도',
		        click: function(e) {
		          togglePicker('yearPicker', e.currentTarget);
		        }
		      },
		      customMonthPicker: {
		        text: '월',
		        click: function(e) {
		          togglePicker('monthPicker', e.currentTarget);
		        }
		      },
		      customDayPicker: {  
		        text: '일',
		        click: function(e) {
		          togglePicker('dayPicker', e.currentTarget); 
		        }
		      }
		    },
            contentHeight: 'auto',
            handleWindowResize: true,
            fixedWeekCount: false,
            eventClick: function(info) { 
				 
				const eventStart = new Date(info.event.start.getTime() - (info.event.start.getTimezoneOffset() * 60000));
			    pickStartDate = eventStart.toISOString().split('T')[0];
				
				info.jsEvent.preventDefault();   
	            let  modal = document.getElementById('eventViewModal');
	            let  X = info.jsEvent.clientX;
	            let  Y = info.jsEvent.clientY;
	            
	            if(X > 1900) {
					X = X - 900;
					Y = Y - 320;
				}
				else if(Y > 800) {
					X = X - 700;
					Y = Y - 490;
				}
				else {
					X = X - 700;
					Y = Y - 320;
				}
	            modal.style.top = `${Y}px`;
	            modal.style.left = `${X}px`;   
            
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
            eventDidMount: function(info) { 
	            if (info.event.extendedProps.type === 'personalResult' || info.event.extendedProps.type === 'participateResult' || info.event.extendedProps.type === 'meetingResult'
	            	|| info.event.extendedProps.type === 'scheduleDtos' || info.event.extendedProps.type === 'departmentResult' || info.event.extendedProps.type === 'vacationResult') {
	                info.el.style.cursor = 'pointer';  
	            } else {
	                info.el.style.cursor = 'default'; 
	            }  
	            
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
                    type:'holiday'
                },  
                {
                    events: events
                }
            ],
            eventOrder: '-order,-allDay,start',
            buttonText: {
                today: '오늘',
                month: '월간',
                week: '주간',
                day: '일간',
                listMonth : '목록'
            },
            allDayText: '종일',
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
            dayMaxEvents: 3,
            moreLinkContent: function(args) {
			    return ' + ' + args.num;
			},	
            dateClick: function(info) {
			    selectedDate = info.dateStr;   
			    $('#eventDate').val(selectedDate);   
			    document.getElementById('eventDate').dispatchEvent(new Event('change'));
    
			    document.getElementById('eventModal').style.display = 'block';
			    const submitButton = document.getElementById('create_modal_submit');
				submitButton.textContent = '저장';
		    },
		     eventTimeFormat: {
			    hour: '2-digit',    
			    minute: '2-digit',  
			    meridiem: false,    
			    hour12: false       
		     }
        });
        filterEvents();
        calendar.render();
    }
 
    function filterEvents() {
	    const companyChecked = document.getElementById('all_company_schedule').checked;
	    const selectedDepartments = $('.department-checkbox:checked').map(function() {
	        return $(this).data('department-no').toString();
	    }).get();
	
	    const searchTerm = document.getElementById('searchInput').value.toLowerCase();  
	    const searchCategory = document.getElementById('searchCategory').value; 
 	
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
	                if (event.extendedProps.member_no.toString() === memberNo) {
			            shouldDisplay = true;
			        } else {
			            shouldDisplay = participantNos.includes(parseInt(memberNo));
			        }
	            } else {
	                shouldDisplay = true;
	            }
	        } 
	        // 전사 일정
	        else if (event.extendedProps.type === 'scheduleDtos') {
	            shouldDisplay = companyChecked;  
	        }
	        // 휴가 
	        else if (event.extendedProps.type === 'vacationResult') {
	            if (companyChecked || selectedDepartments.includes(eventDepartmentNo.toString())) {
	                shouldDisplay = true;  
	            }
	        }
	        // 부서 일정
	        else if (event.extendedProps.type === 'departmentResult') {
	            if (selectedDepartments.includes(eventDepartmentNo.toString())) {
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
	        
	        // 검색	 
		    const startDateValue = document.getElementById('searchstartDate').value;
			const endDateValue = document.getElementById('searchendDate').value;
			const startDate = startDateValue ? new Date(startDateValue) : null;
			const endDate = endDateValue ? new Date(endDateValue) : null;
			
			if (startDate) {
			    startDate.setHours(0, 0, 0, 0);  
			}
			if (endDate) {
			    endDate.setHours(23, 59, 59, 999); 
			}
			
			let eventStartDate;
			let eventEndDate;
			
			if (event.allDay === true) {
			    eventStartDate = new Date(event.start);
			    eventEndDate = new Date(event.end);
			    eventEndDate.setDate(eventEndDate.getDate() - 1); 
			} else {
			    eventStartDate = new Date(event.start);
			    eventEndDate = eventStartDate;
			 
			    eventStartDate.setHours(0, 0, 0, 0);
			    eventEndDate.setHours(0, 0, 0, 0);
			}
			
			const withinDateRange =
			    (!startDate || eventEndDate >= startDate) &&  
			    (!endDate || eventStartDate <= endDate);      
			
			if (shouldDisplay && (startDate || endDate)) {
			    shouldDisplay = withinDateRange;
			} 
	
	        if (shouldDisplay && searchTerm) {
	            const eventTitle = event.title.toLowerCase();
	            const eventContent = event.extendedProps.meeting_reservation_purpose ? event.extendedProps.meeting_reservation_purpose.toLowerCase() : '';
	            const categoryName = event.extendedProps.categoryName ? event.extendedProps.categoryName.toLowerCase() : '';
	 
	            if (searchCategory === 'all') { 
	                if (!eventTitle.includes(searchTerm) && !eventContent.includes(searchTerm) && !categoryName.includes(searchTerm)) {
	                    shouldDisplay = false;
	                }
	            } else if (searchCategory === 'content') { 
	                if (!eventContent.includes(searchTerm)) {
	                    shouldDisplay = false;
	                }
	            } else if (searchCategory === 'categoryName') { 
	                if (!categoryName.includes(searchTerm)) {
	                    shouldDisplay = false;
	                }
	            }
	        }
	  		if (event.extendedProps.description === '공휴일') {
	            shouldDisplay = true;
	        } 
	        event.setProp('display', shouldDisplay ? 'auto' : 'none');
	    });
	}
  
    function createPicker(id, options) {
	    const picker = document.createElement('select');
	    picker.id = id;
	    picker.className = 'custom-picker';
	    picker.size = 7; 
	    picker.style.display = 'none';
	    options.forEach(option => {
	      const optionEl = document.createElement('option');
	      optionEl.value = option.value;
	      optionEl.text = option.text;
	      picker.appendChild(optionEl);
	    });
	    document.body.appendChild(picker);
	    return picker;
   }
	
   function togglePicker(pickerId, button) {
        const picker = document.getElementById(pickerId);
        const otherPickers = ['yearPicker', 'monthPicker', 'dayPicker'].filter(id => id !== pickerId);
        
        otherPickers.forEach(id => {
            const otherPickerElement = document.getElementById(id);
            if (otherPickerElement) {
                otherPickerElement.style.display = 'none';
            }
        });

        if (picker) {
            if (picker.style.display === 'none' || picker.style.display === '') {
                const rect = button.getBoundingClientRect();
                picker.style.display = 'block';
                picker.style.top = `${rect.bottom}px`;
                picker.style.left = `${rect.left}px`;
                picker.focus();
            } else {
                picker.style.display = 'none';
            }
        }
    }
	
   const currentYear = new Date().getFullYear();
   const yearOptions = Array.from({length: 11}, (_, i) => ({
     value: currentYear - 5 + i,
     text: `${currentYear - 5 + i}년`
   }));
	
   const monthOptions = Array.from({length: 12}, (_, i) => ({
     value: i,
     text: `${i + 1}월`
   })); 
	
   const yearPicker = createPicker('yearPicker', yearOptions);
   const monthPicker = createPicker('monthPicker', monthOptions); 

   yearPicker.onchange = function() {
     const selectedYear = parseInt(this.value);
     const currentDate = calendar.getDate();
     calendar.gotoDate(new Date(selectedYear, currentDate.getMonth(), 1));
     this.style.display = 'none';
   };

   monthPicker.onchange = function() {
        const selectedMonth = parseInt(this.value);
        const currentDate = calendar.getDate();
        calendar.gotoDate(new Date(currentDate.getFullYear(), selectedMonth, 1));
        updateDayPicker(currentDate.getFullYear(), selectedMonth);  
        this.style.display = 'none';
    }; 
  
   const dayPicker = createPicker('dayPicker', []);
    updateDayPicker(currentYear, new Date().getMonth());  

    dayPicker.onchange = function() {
        const selectedDay = parseInt(this.value);
        const currentDate = calendar.getDate();
        calendar.gotoDate(new Date(currentDate.getFullYear(), currentDate.getMonth(), selectedDay));
        this.style.display = 'none';
    }; 
    
    function updateDayPicker(year, month) {
        const lastDay = new Date(year, month + 1, 0).getDate(); 
        const dayOptions = Array.from({ length: lastDay }, (_, i) => ({
            value: i + 1,
            text: `${i + 1}일`
        }));

        dayPicker.innerHTML = '';  
        dayOptions.forEach(option => {
            const opt = document.createElement('option');
            opt.value = option.value;
            opt.textContent = option.text;
            dayPicker.appendChild(opt);
        });
 
        dayPicker.value = lastDay;
    }
    
   document.addEventListener('click', function (e) {
     if (!e.target.closest('.fc-customYearPicker-button') &&
       !e.target.closest('.fc-customMonthPicker-button') &&
       !e.target.closest('.fc-customDayPicker-button') &&
       !e.target.closest('.custom-picker')) {
       yearPicker.style.display = 'none';
       monthPicker.style.display = 'none';
       if (dayPicker) {
         dayPicker.style.display = 'none';
       }
     }
   });
		 
	
    // 일정 등록
    function roundToNearest30Minutes(timeStr) {
        const [hours, minutes] = timeStr.split(':').map(Number);
        const roundedMinutes = Math.round(minutes / 30) * 30;
        const adjustedHours = hours + Math.floor(roundedMinutes / 60);
        const adjustedMinutes = roundedMinutes % 60;
        return `${String(adjustedHours).padStart(2, '0')}:${String(adjustedMinutes).padStart(2, '0')}`;
    }

    function handleTimeChange(event) {
        const input = event.target;
        const originalValue = input.value;
        if (originalValue) {
            input.value = roundToNearest30Minutes(originalValue);
        }
    }
    
    startInput.addEventListener('change', handleTimeChange);
    endInput.addEventListener('change', handleTimeChange); 
    
	// 등록 모달 열기
	document.getElementById('addEventBtn').addEventListener('click', function() {    
		document.getElementById('eventModal').style.display = 'block';
	});

	// 등록 모달 닫기 
	document.getElementById('closeModalBtn').addEventListener('click', function() {
		Swal.fire({
			text: '작성한 내용이 저장되지 않습니다.',
			icon: 'warning',
			showCancelButton: true,
			confirmButtonColor: '#0056b3',
			cancelButtonColor: '#f8f9fa',
			confirmButtonText: '확인',
			cancelButtonText: '취소',
		}).then((result) => {
			if (result.isConfirmed) {
				document.getElementById('eventModal').style.display = 'none';
			    document.getElementById('allDay').checked = false;
                document.getElementById('allDay').dispatchEvent(new Event('change'));
				resetForm(createEmployeeScheduleForm);
				$('#organization-chart').jstree("uncheck_all");
				
				const editTitle = document.getElementById('modal-title');
				editTitle.textContent = '일정 등록'; 
	 
			    const reservationArea = $('.selected-participants-container');  
			    reservationArea.find('.selected-participants').remove(); 
			    
			    selectedMembers = [];
			    localStorage.removeItem('selectedMembers');  
			    $('#selectedMembers').val('');
				document.getElementById('eventRepeatModal').style.display = 'none';
			}
		});
	});
	
	document.getElementById('event_repeat_close_btn').addEventListener('click', function() { 
		document.getElementById('eventRepeatModal').style.display = 'none'; 
	}); 
	
	document.getElementById('closeModalBtn2').addEventListener('click', function() {
		Swal.fire({
			text: '작성한 내용이 저장되지 않습니다.',
			icon: 'warning',
			showCancelButton: true,
			confirmButtonColor: '#0056b3',
			cancelButtonColor: '#f8f9fa',
			confirmButtonText: '확인',
			cancelButtonText: '취소',
		}).then((result) => {
			if (result.isConfirmed) {
				document.getElementById('eventModal').style.display = 'none';
			    document.getElementById('allDay').checked = false;
                document.getElementById('allDay').dispatchEvent(new Event('change'));
				resetForm(createEmployeeScheduleForm);
				$('#organization-chart').jstree("uncheck_all");
	 	
				const editTitle = document.getElementById('modal-title');
				editTitle.textContent = '일정 등록'; 
				
			    const reservationArea = $('.selected-participants-container');  
			    reservationArea.find('.selected-participants').remove(); 
			    
			    selectedMembers = [];
			    localStorage.removeItem('selectedMembers');  
			    $('#selectedMembers').val('');
				document.getElementById('eventRepeatModal').style.display = 'none';
			}
		});
	});
	
	document.getElementById('eventDate').addEventListener('change', function() {
        const selectedDate = this.value;
        const endDateInput = document.getElementById('endDate');
        endDateInput.value = '';
        endDateInput.min = selectedDate; 
    });
    
	// 반복 옵션
	document.getElementById('eventDate').addEventListener('change', function() {
	    const selectedDate = new Date(this.value);
	    const repeatOption = document.getElementById('repeatOption');
	     
	    const dayOfWeek = selectedDate.toLocaleString('ko-KR', { weekday: 'long' });
	    const weekOfMonth = Math.ceil(selectedDate.getDate() / 7);
	 
	    repeatOption.innerHTML = `
	        <option value="0">반복 없음</option>
	        <option value="1">매일</option>
	        <option value="2">매주 ${dayOfWeek}</option>
	        <option value="3">매월 ${selectedDate.getDate()}일</option>
	        <option value="4">매월 ${weekOfMonth}번째 ${dayOfWeek}</option>
	        <option value="5">매년 ${selectedDate.getMonth() + 1}월 ${selectedDate.getDate()}일</option>
	    `;
	});
    
	// 종일 체크박스 선택  
	document.getElementById('allDay').addEventListener('change', function() {
	    const isChecked = this.checked;
	
	    const timeGroup = document.getElementById('timeGroup');
	    const startTimeInput = timeGroup.querySelector('input');
	
	    const endTimeGroup = document.getElementById('endTimeGroup');
	    const endTimeInput = endTimeGroup.querySelector('input');
	
	    const endDateGroup = document.getElementById('endDateGroup');
	    const endDateInput = endDateGroup.querySelector('input');
	
	    timeGroup.style.display = isChecked ? 'none' : 'block';
	    endTimeGroup.style.display = isChecked ? 'none' : 'block';
	    endDateGroup.style.display = isChecked ? 'block' : 'none';
	 
	    if (timeGroup.style.display === 'block') {
	        startTimeInput.classList.add('plus_detail_option');
	    } else {
	        startTimeInput.classList.remove('plus_detail_option');  
	    }
	
	    if (endTimeGroup.style.display === 'block') {
	        endTimeInput.classList.add('plus_detail_option');
	    } else {
	        endTimeInput.classList.remove('plus_detail_option');  
	    }
	
	    if (endDateGroup.style.display === 'block') {
	        endDateInput.classList.add('plus_detail_option');
	    } else {
	        endDateInput.classList.remove('plus_detail_option');  
	    }
	}); 

	 
	// 카테고리 옵션
	$.ajax({
        url: '/employee/categories', 
        method: 'GET',
        success: function(data) {
			const categorySelect = $('#category');
            data.forEach(category => {
                categorySelect.append(`<option value="${category.schedule_category_no}">${category.schedule_category_name}</option>`);
            });
        } 
    });
    
    // 반복 종료  
	document.getElementById('eventDate').addEventListener('change', function() {
	    const eventDate = this.value;   
	    const repeatEndDate = document.getElementById('repeatEndDate');
	     
	    if (eventDate) {
	        repeatEndDate.min = eventDate;
	        repeatEndDate.value = '';   
	    }
	});
	
	document.getElementById('endDate').addEventListener('change', function() {
	    const endDate = this.value;   
	    const repeatEndDate = document.getElementById('repeatEndDate');
	     
	    if (endDate) {
	        repeatEndDate.min = endDate;
	        repeatEndDate.value = '';   
	    }
	});
	
	document.getElementById('allDay').addEventListener('change', function() {
	    const repeatEndDate = document.getElementById('repeatEndDate');
	    
	    repeatEndDate.value = ''; 
	});

	document.getElementById('repeatOption').addEventListener('change', function() {
	    const repeatEndGroup = document.getElementById('repeatEndGroup'); 
	    const repeatEndDate = document.getElementById('repeatEndDate');
	
	    if (this.value != 0) {
	        repeatEndGroup.style.display = 'block';
	        repeatEndDate.classList.add('plus_detail_option_2');
	    } else {
	        repeatEndGroup.style.display = 'none';  
	        repeatEndDate.classList.remove('plus_detail_option_2');  
	        repeatEndDate.value = '';   
	    } 
	});
	
	
	// 부서 체크박스, 참여자 버튼
	const departmentScheduleCheckbox = document.getElementById('department_schedule');
    const openOrganizationChartButton = document.getElementById('openOrganizationChartButton');
 
    departmentScheduleCheckbox.addEventListener('change', function () { 
        if (this.checked) {
            openOrganizationChartButton.disabled = true;
        } else {
            openOrganizationChartButton.disabled = false;
        }
    });
   
	$('#openOrganizationChartButton').on('click', function() {
        openOrganizationChartModal();
        updateCheckboxState();
    });
    
	function updateCheckboxState() {
	    if (document.getElementById('selectedMembers').value.trim() === '') {
	        departmentScheduleCheckbox.disabled = false; 
	    } else {
	        departmentScheduleCheckbox.disabled = true; 
	    }
	}
	
	function openOrganizationChartModal() {
	    selectedMembers = [];   
	
	    $('#organizationChartModal').modal('show');
	
	    loadOrganizationChart();
	}
	 
	document.getElementById('chart_close').addEventListener('click', function() {  
		$('#organization-chart').jstree("uncheck_all");
		$('#organizationChartModal').modal('hide');	 
	});
	
    // 조직도 로딩
    function loadOrganizationChart() {
        $.ajax({
            url: '/schedule/chart',
            method: 'GET',
            success: function(data) {
                $('#organization-chart').jstree({ 
                    'core': {
                        'data': data,
                        'themes': { 
                            'icons': true,
                            'dots': false,
                            
                        }
                    },
                    'plugins': ['checkbox', 'types', 'search'],
                    'types': {
                        'default': {
                            'icon': 'fa fa-users'
                        },
                        'department': {
                            'icon': 'fa fa-users'
                        }, 
                        'member': {
			            	'icon': 'fa fa-user'  
			        	}
                    }
                }).on('ready.jstree', function (e, data) { 
                    setSelectedParticipants(participantMembers); 
                });

                // 체크박스 변경 시 선택된 사원 업데이트
                $('#organization-chart').on('changed.jstree', function (e, data) {
                    updateSelectedMembers(data.selected, data.instance);
                });
                
                // 검색  
                $('#organization_search').on('keyup', function() { 
                	const searchString = $(this).val();
	                $('#organization-chart').jstree(true).search(searchString);
	            });
            },
            error: function(xhr, status, error) {
                console.error('조직도 로딩 오류:', error);
            }
        });
    }

    // 선택된 사원 업데이트
    function updateSelectedMembers(selectedIds, instance) {
        const selectedMembersContainer = $('#selected-members');
        const permissionPickList = $('.permission_pick_list');
        selectedMembersContainer.empty();
        permissionPickList.empty();

        const selectedNodes = instance.get_selected(true);
        let selectedMembers = [];

        selectedNodes.forEach(function(node) {
            if (node.original.type === 'member') {
                const memberId = node.id;
                const memberNumber = memberId.replace('member_', ''); // 사원 번호
                const memberElement = $('<div class="selected-member"></div>');
                const memberName = $('<span></span>').text(node.text);
                const removeButton = $('<button class="remove-member">&times;</button>');

                memberElement.append(memberName).append(removeButton);
                selectedMembersContainer.append(memberElement);

                selectedMembers.push(memberNumber);

                removeButton.click(function() {
                    instance.uncheck_node(node);
                    memberElement.remove();
                    const index = selectedMembers.indexOf(memberNumber);
                    if (index !== -1) {
                        selectedMembers.splice(index, 1);
                    }

                    localStorage.setItem('selectedMembers', JSON.stringify(selectedMembers));

                    permissionPickList.find(`.permission-item[data-name="${node.text}"]`).remove();
                });

                const permissionItem = $(`<div class="permission-item" data-name="${node.text}"></div>`);
                permissionItem.text(node.text);
                permissionPickList.append(permissionItem);
            }
        });

        $('#selectedMembers').val(selectedMembers.join(','));

        localStorage.setItem('selectedMembers', JSON.stringify(selectedMembers));
    }  
    
    // 조직도 확인 -> 예약 모달 사원 출력 
	$('#participate_confirmButton').click(function()  {
	    const reservationArea = $('.selected-participants-container');  
	    const selectedMembersContainer = $('#selected-members');  
	    const selectedMembersList = selectedMembersContainer.find('.selected-member');
	    
	    reservationArea.find('.selected-participants').remove();  
	    
	    selectedMembersList.each(function() {
	        const memberName = $(this).find('span').text();
	        const participantItem = $('<span class="selected-participants"></span>');
	        participantItem.text(memberName);
	        reservationArea.append(participantItem);
	    }); 
	    $('#organizationChartModal').modal('hide');
	    updateCheckboxState();
	 });
	 
	// 요일 
	function getDayOfWeek() {
	    const selectedDate = new Date(document.getElementById('eventDate').value);
	    return selectedDate.getDay();  
	}
	
	// 주차 
	function getWeekNumber() {
	    const selectedDate = new Date(document.getElementById('eventDate').value);
	    return Math.ceil(selectedDate.getDate() / 7);  
	} 
	
	function showAlert(message) {
	    Swal.fire({
	        text: message,
	        icon: 'warning',
	        confirmButtonColor: '#0056b3',
	        confirmButtonText: '확인',
	    });
	}
	
	function resetForm(form) {
        form.reset(); 
    } 
	
	// 일정 등록 
	document.getElementById('eventForm').addEventListener('submit', function(event) {
		const type_form = document.getElementById('create_modal_submit').innerText;
	    event.preventDefault();
   		 
	    const title = document.getElementById('eventTitle').value.trim();
	    const category = document.getElementById('category').value;
	    const startDate = document.getElementById('eventDate').value;
	    const description = document.getElementById('description').value.trim();
	    const allDay = document.getElementById('allDay').checked; 
	    const endDate = document.getElementById('endDate').value;
	    const startTime = document.getElementById('startTime').value;
	    const endTime = document.getElementById('endTime').value;
	    const repeatOption = document.getElementById('repeatOption').value;
	    const repeatEndDate = document.getElementById('repeatEndDate').value;
	
	    if (!title) {
	        showAlert('일정 제목을 입력해 주세요.');
	        return;
	    }
	
	    if (!category) {
	        showAlert('카테고리를 선택해 주세요.');
	        return;
	    }
	
	    if (!startDate) {
	        showAlert('날짜를 선택해 주세요.');
	        return;
	    }
	 
	    if (!allDay) {
	        if (!startTime) {
	            showAlert('시작 시간을 입력해 주세요.');
	            return;
	        }
	        
	        if (!endTime) {
	            showAlert('종료 시간을 입력해 주세요.');
	            return;
	        }
	
	        if (endTime < startTime) {
	            showAlert('종료 시간은 시작 시간 이후로 설정해 주세요.');
	            return;
	        }
	    }
	    
	    if(allDay) {
			if (!startDate) {
	            showAlert('시작 날짜를 입력해 주세요.');
	            return;
	        }
	        
			if (!endDate) {
	            showAlert('종료 날짜를 입력해 주세요.');
	            return;
	        }
		}
	     
		if (repeatOption != 0 && !repeatEndDate && !$('#repeatOption').is(':disabled')) {
		    showAlert('반복 종료일을 입력해 주세요.');
		    return;
		}  
	
	    // 반복 옵션 값 
	    const repeat_insert_date = document.getElementById('eventDate').value;
	    const repeat = parseInt(document.getElementById('repeatOption').value);
	    const repeatDayOfWeek = repeat === 2 ||  repeat ===  4 ? getDayOfWeek() : null; // 요일
	    const repeatWeek = repeat === 4 ? getWeekNumber() : null; // 주차
	    const repeatDate = repeat === 3 ||  repeat ===  5 ? new Date(repeat_insert_date).getDate() : null; // 특정 일
	    const repeatMonth = repeat === 5 ? new Date(repeat_insert_date).getMonth() + 1 : null; // 특정 월
	 
	 	const department_schedule = document.getElementById('department_schedule').checked; 
	 	const selectedMembers = document.getElementById('selectedMembers').value;
	 	var schedule_type = 0;
	 	var departmentNo; 
	 	
	 	if (selectedMembers) { 
		    schedule_type = 2;
		} else if (department_schedule) { 
		    schedule_type = 1;
		    departmentNo = userDepartmentNo; 
		} else { 
		    schedule_type = 0;
		}
	 	
	    const eventData = {
	        title: title,
	        category: category,
	        startDate: startDate,
	        endDate : allDay ? endDate : null,
	        repeatEndDate : repeatEndDate,
	        startTime: allDay ? null : startTime,  
	        endTime: allDay ? null : endTime,
	        allDay: allDay,
	        repeat: repeat,
	        description: description,
	        repeatEndDate: repeatEndDate,
	        schedule_day_of_week: repeatDayOfWeek,
	        schedule_week: repeatWeek,
	        schedule_date: repeatDate,
	        schedule_month: repeatMonth,
	        schedule_type : schedule_type,
	        department_no : departmentNo,
	        selectedMembers: selectedMembers
	    };
	  
		if(type_form === "저장") {     
		    $.ajax({
		        type: "POST",
		        url: '/employee/schedule/save/' + memberNo,   
		        contentType: 'application/json',
		        data: JSON.stringify(eventData),
		        headers: {
		            'X-CSRF-TOKEN': csrfToken
		        },
		        success: function(response) {
					if (response.res_code === "200") {
		                Swal.fire({
		                    text: response.res_msg,
		                    icon: 'success',
		                    confirmButtonColor: '#0056b3',
		                    confirmButtonText: '확인'
		                }).then(() => {
		                  	window.location.reload();  
		                	document.getElementById('eventModal').style.display = 'none';
		                });
		                
		                // 부서 일정 알림 
		                if(schedule_type === 1) {
							console.log("departmentAlarm");
			                alarmSocket.send(JSON.stringify({
					           	type: 'noficationDepartmentSchedule',
					           	deptNo: departmentNo, 
					           	memberNo: memberNo 
					        })); 
						}
						// 참여자 일정 알림
						if(schedule_type === 2) {
							console.log("participantAlarm"); 
			                alarmSocket.send(JSON.stringify({
					           	type: 'noficationParticipantSchedule', 
					           	memberNo: memberNo,
					           	participants : selectedMembers
					        })); 
						}
		            } else {
		                Swal.fire({
		                    text: response.res_msg,
		                    icon: 'error',
		                    confirmButtonColor: '#0056b3',
		                    confirmButtonText: '확인'
		                });
		            } 
		        } 
		    }); 
		} 
		// 수정
		else {
			const isRecurring = document.getElementById('isRecurring').value;    
			const schedule_edit_type = eventData.schedule_type; 
			 
			// 예외 일정
		    if (isRecurring === "1") {   
		        openEventRepeatModal();
		    } else {  
		        submitEventUpdate(schedule_edit_type, isRecurring); 
		    }
		}
	});
	
	window.addEventListener('click', function(event) {
	    const modal = document.getElementById('eventViewModal');
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
	    document.getElementById('event_repeat_title').style.display = 'block';  
	    document.getElementById('editEventBtn').style.display = 'block';
	    document.getElementById('deleteEventBtn').style.display = 'block';  
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
		    document.getElementById('editEventBtn').style.display = 'none';
		    document.getElementById('deleteEventBtn').style.display = 'none';
		    document.getElementById('departmentName').style.display = 'block'; 
		} else if (event.extendedProps.type === 'scheduleDtos') {  
		    document.getElementById('editEventBtn').style.display = 'none';
		    document.getElementById('deleteEventBtn').style.display = 'none';
		    document.getElementById('departmentName').style.display = 'none'; 
		} else if (event.extendedProps.type === 'participateResult' && event.extendedProps.member_no !== Number(memberNo)) {  
		    document.getElementById('editEventBtn').style.display = 'none';
		    document.getElementById('deleteEventBtn').style.display = 'none';
		    document.getElementById('departmentName').style.display = 'none'; 
		}
		else {
		    document.getElementById('editEventBtn').style.display = 'block';
		    document.getElementById('deleteEventBtn').style.display = 'block'; 
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
	    document.getElementById('editEventBtn').style.display = 'block';
	    document.getElementById('deleteEventBtn').style.display = 'block'; 
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
	
	
	// 수정
	document.getElementById('editEventBtn').addEventListener('click', function() { 
		const isException = document.getElementById('isException').value;
	    const eventId = document.getElementById('eventId').value;
	    const event = calendar.getEventById(eventId); 
	    if (isException === "0") {
	        openExceptionScheduleEditModal(eventId);
	    } else {
	        openScheduleEditModal(eventId);
	    }
	    
	});
	
	// 수정 모달
	function openScheduleEditModal(eventNo) {
	    var modal = document.getElementById('eventModal');
	    modal.style.display = 'block';
	    document.getElementById('eventViewModal').style.display = 'none';
		
		const submitButton = document.getElementById('create_modal_submit');
   		submitButton.textContent = '수정';
    	
    	const editTitle = document.getElementById('modal-title');
	    editTitle.textContent = '일정 수정'; 
	    
	    $.ajax({
	        url: '/schedule/edit/' + eventNo,
	        type: 'GET',
	        dataType: 'json',
	        success: function(data) { 
 				 
	            $('#eventId').val(data.schedule.schedule_no);  
	            $('#isRecurring').val(data.schedule.schedule_repeat);  
	            $('#category').val(data.schedule.schedule_category_no);
	            $('#eventTitle').val(data.schedule.schedule_title);
	            $('#eventDate').val(pickStartDate);
	            document.getElementById('eventDate').dispatchEvent(new Event('change'));
	            
	            if (data.schedule.schedule_allday === 1) {
	                $('#allDay').prop('checked', true);
	                document.getElementById('allDay').dispatchEvent(new Event('change'));
	
	                $('#endDate').val(pickEndDate);
	                document.getElementById('endDate').dispatchEvent(new Event('change'));
	            } else {
	                $('#allDay').prop('checked', false);
	                document.getElementById('allDay').dispatchEvent(new Event('change')); 
	                $('#startTime').val(data.schedule.schedule_start_time);
	                $('#endTime').val(data.schedule.schedule_end_time);
	            }
	
	            $('#description').val(data.schedule.schedule_comment);
	             
	            if (data.schedule.schedule_repeat === 1) {
		            $('#repeatOption').val(data.scheduleRepeat.schedule_repeat_type);
		            document.getElementById('repeatOption').dispatchEvent(new Event('change'));
	                $('#repeatEndDate').val(data.scheduleRepeat.schedule_repeat_end_date);
	            } 		  
	            
	            if (data.schedule.schedule_type === 1) {
		            $('#department_schedule').prop('checked', true); 
		             document.getElementById('department_schedule').dispatchEvent(new Event('change'));
	            }  
	            
	            if (data.schedule.schedule_type === 2) { 
		            $('#department_schedule').prop('disabled', true); 
		            findParticipants(eventNo);            
	            }       
	        }
	    });
	} 
	
	// 예외 수정 모달
	function openExceptionScheduleEditModal(eventId) {
	    var modal = document.getElementById('eventModal');
	    modal.style.display = 'block';
	    document.getElementById('eventViewModal').style.display = 'none';
		
	    const editTitle = document.getElementById('modal-title');
	    editTitle.textContent = '일정 수정';
	    
	    const submitButton = document.getElementById('create_modal_submit');
	    submitButton.textContent = '수정';
	     
	    document.getElementById('isRecurring').value = 2;
	    
	    $.ajax({
	        url: '/schedule/exception/edit/' + eventId,
	        type: 'GET',
	        dataType: 'json',
	        success: function(data) {;
        		 
	            $('#eventId').val(eventId);    
	            $('#category').val(data.schedule.schedule_category_no);
	            $('#eventTitle').val(data.schedule.schedule_exception_title);
	            $('#eventDate').val(data.schedule.schedule_exception_start_date);
	            document.getElementById('eventDate').dispatchEvent(new Event('change'));
	            
	            if (!data.schedule.schedule_exception_start_time && !data.schedule.schedule_exception_end_time) {
	                $('#allDay').prop('checked', true);
	                document.getElementById('allDay').dispatchEvent(new Event('change'));
	                $('#endDate').val(data.schedule.schedule_exception_end_date);
	            } else {
	                $('#allDay').prop('checked', false);
	                document.getElementById('allDay').dispatchEvent(new Event('change')); 
	                $('#startTime').val(data.schedule.schedule_exception_start_time);
	                $('#endTime').val(data.schedule.schedule_exception_end_time);
	            }
	
	            $('#description').val(data.schedule.schedule_exception_comment);
	            
    	        $('#repeatOption').val('0');
	            $('#repeatOption').prop('disabled', true).hide();
	            $('label[for="repeatOption"]').hide();
	            $('#repeatEndDate').val('');
	            $('#repeatEndDate').prop('disabled', true).hide();
	            
	            if (data.schedule.schedule_exception_type === 1) {
		            $('#department_schedule').prop('checked', true); 
		             document.getElementById('department_schedule').dispatchEvent(new Event('change'));
	            }  
	            
	            if (data.schedule.schedule_exception_type === 2) {  
		            $('#department_schedule').prop('disabled', true); 
		            findExceptionParticipants(eventId);            
	            }  
	             
	        } 
	    });
	}
	
	// 반복 일정 수정 모달 
	function openEventRepeatModal() { 
	    var repeatModal = document.getElementById('eventRepeatModal');
	    repeatModal.style.display = 'block';
	}
	
	document.getElementById('event_repeat_modal_btn').addEventListener('click', function() {
	    const eventId = document.getElementById('eventId').value; 
	    const repeatEditOption = document.querySelector('input[name="repeatEditOption"]:checked').value;
	 	const typeEventRepeat = document.getElementById('event_repeat_title').innerText; 
 
	 	const department_schedule = document.getElementById('department_schedule').checked; 
	 	const selectedMembers = document.getElementById('selectedMembers').value;
	 	 
	 	var schedule_edit_type = 0; 
	 	
	 	if (selectedMembers) { 
		    schedule_edit_type = 2;
		} else if (department_schedule) { 
		    schedule_edit_type = 1; 
		} else { 
		    schedule_edit_type = 0;
		}
		
	 	if(typeEventRepeat === "반복 일정 수정") {
		    handleRecurringEventUpdate(eventId, repeatEditOption, schedule_edit_type);			
		} else {
			repeatDelete(eventId, repeatEditOption);
		}
	 
	    document.getElementById('eventRepeatModal').style.display = 'none';
	}); 
	
	// 반복 일정 수정  
	function handleRecurringEventUpdate(eventId, repeatEditOption, schedule_edit_type) {
	    const eventData = getEventFormData();   
	    
	    $.ajax({
	        type: "POST",
	        url: '/employee/schedule/edit/recurring/' + eventId + '/' + schedule_edit_type + '?editOption=' + repeatEditOption + '&pickStartDate=' + pickStartDate + '&pickEndDate=' + pickEndDate,
	        contentType: 'application/json',
	        data: JSON.stringify(eventData),
	        headers: {
	            'X-CSRF-TOKEN': csrfToken,
	        },
	        success: function(response) {
				 if (response.res_code === '200') {
	                Swal.fire({ 
		                text: response.res_msg,
		                icon: 'success',
		                confirmButtonText: '확인',
		                confirmButtonColor: '#0056b3',
		            }).then(function() {
		                window.location.reload();
		            });
		            
		            // 부서 일정 알림  
	                if(schedule_edit_type === 1) {   
		                alarmSocket.send(JSON.stringify({
				           	type: 'noficationDepartmentSchedule',
				           	deptNo: eventData.department_no, 
				           	memberNo: eventData.memberNo 
				        })); 
					}
					// 참여자 일정 알림
					if(schedule_edit_type === 2) {  
		                alarmSocket.send(JSON.stringify({
				           	type: 'noficationParticipantSchedule', 
				           	memberNo: eventData.memberNo,
				           	participants : eventData.selectedMembers
				        })); 
					}
	            } else {
	                Swal.fire('반복 일정', response.res_msg, 'error');
	            }   
            },
	        error: function () {
	            Swal.fire("서버 오류", "반복 일정 수정 중 오류가 발생했습니다.", "error");
	        }
	    });
	} 

	// 일정 수정 확인
	function submitEventUpdate(schedule_edit_type, isRecurring) {
	    const eventData = getEventFormData();
    	const isException = isRecurring === '0'; 
		const url = isException ? `/employee/schedule/edit/${document.getElementById('eventId').value}/${schedule_edit_type}`
					: `/employee/schedule/exception/edit/${document.getElementById('eventId').value}/${schedule_edit_type}`;
	     
	    $.ajax({
	        type: "POST",
	        url: url,
	        contentType: 'application/json',
	        data: JSON.stringify(eventData),
	        headers: {
	            'X-CSRF-TOKEN': csrfToken,
	        },
	        success: function(response) {
	            Swal.fire({
	                text: '일정이 수정되었습니다.',
	                icon: 'success',
	                confirmButtonText: '확인',
	                confirmButtonColor: '#0056b3',
	            }).then(function() {
	                window.location.reload();
	            });
	            // 부서 일정 알림  
                if(schedule_edit_type === 1) {   
	                alarmSocket.send(JSON.stringify({
			           	type: 'noficationDepartmentSchedule',
			           	deptNo: eventData.department_no, 
			           	memberNo: eventData.memberNo 
			        })); 
				}
				// 참여자 일정 알림
				if(schedule_edit_type === 2) {  
	                alarmSocket.send(JSON.stringify({
			           	type: 'noficationParticipantSchedule', 
			           	memberNo: eventData.memberNo,
			           	participants : eventData.selectedMembers
			        })); 
				}
	        },
	        error: function(xhr, status, error) {
	            console.error('일정 수정 오류: ', error);
	        }
	    });
	}
	
	// 수정용
	function getEventFormData() {
	    const title = document.getElementById('eventTitle').value.trim();
	    const category = document.getElementById('category').value;
	    const startDate = document.getElementById('eventDate').value;
	    const endDate = document.getElementById('endDate').value;
	    const description = document.getElementById('description').value.trim();
	    const allDay = document.getElementById('allDay').checked;
	    const startTime = document.getElementById('startTime').value;
	    const endTime = document.getElementById('endTime').value;
	    const repeatOption = document.getElementById('repeatOption').value;
	    const repeatEndDate = document.getElementById('repeatEndDate').value;
	
		const repeat_insert_date = document.getElementById('eventDate').value;
	    const repeatDayOfWeek = repeatOption === "2" || repeatOption === "4" ? getDayOfWeek() : null; // 요일
	    const repeatWeek = repeatOption === "4" ? getWeekNumber() : null; // 주차
	    const repeatDate = repeatOption === "3" || repeatOption === "5" ? new Date(repeat_insert_date).getDate() : null; // 특정 일
	    const repeatMonth = repeatOption === "5" ? new Date(repeat_insert_date).getMonth() + 1 : null; // 특정 월
		
		const department_schedule = document.getElementById('department_schedule').checked; 
	 	const selectedMembers = document.getElementById('selectedMembers').value;
	 	var schedule_type = 0;
	 	var departmentNo; 
	 	
	 	if (selectedMembers) { 
		    schedule_type = 2;
		} else if (department_schedule) { 
		    schedule_type = 1;
		    departmentNo = userDepartmentNo; 
		} else { 
		    schedule_type = 0;
		}
		
	    return {
	        title: title,
	        category: category,
	        startDate: startDate,
	        endDate : allDay ? endDate : null, 
	        startTime: allDay ? null : startTime,  
	        endTime: allDay ? null : endTime,
	        allDay: allDay,
	        repeat: repeatOption,
	        description: description,
	        repeatEndDate: repeatEndDate,
	        schedule_day_of_week: repeatDayOfWeek,
	        schedule_week: repeatWeek,
	        schedule_date: repeatDate,
	        schedule_month: repeatMonth,
	        schedule_type : schedule_type,
	        department_no : departmentNo,
	        selectedMembers: selectedMembers,
	        memberNo : memberNo
	    };
	}
	
	// 참여자 정보 가져오기
	function findParticipants(scheduleNo) {
		$.ajax({
	        url: '/employee/schedule/participate/' + scheduleNo,
	        type: 'GET',
	        dataType: 'json',
	        success: function(data) {   
	       		let filteredParticipants = data.participants.filter(p => (p.member_no).toString() !== memberNo);
	       		
	       		let selectedParticipants = filteredParticipants
	                .map(p => `<span class="selected-participants">${p.memberName} ${p.positionName}</span>`)
	                .join(' ');
	            $('.selected-participants-container').html(selectedParticipants);
	        	participantMembers = filteredParticipants.map(p => p.member_no); 
	            $('#selectedMembers').val(participantMembers.join(',')); 
	    		 
	            setSelectedParticipants(data.participants);  
	            var jstree = $('#organization-chart').jstree(true);
			    if (jstree) {
			        participants.forEach(function(memberNo) {
			            var nodeId = 'member_' + memberNo;
			            if (jstree.get_node(nodeId)) { 
			                jstree.check_node(nodeId);
			            }
			        });
			    } 
	        }
	    });  
	}
	
	// 예외 참여자 정보 가져오기
	function findExceptionParticipants(scheduleNo) { 
		$.ajax({
	        url: '/employee/schedule/participate/exception/' + scheduleNo,
	        type: 'GET',
	        dataType: 'json',
	        success: function(data) {   
	       		let filteredParticipants = data.participants.filter(p => (p.member_no).toString() !== memberNo);
	       		
	       		let selectedParticipants = filteredParticipants
	                .map(p => `<span class="selected-participants">${p.memberName} ${p.positionName}</span>`)
	                .join(' ');
	            $('.selected-participants-container').html(selectedParticipants);
	        	participantMembers = filteredParticipants.map(p => p.member_no); 
	            $('#selectedMembers').val(participantMembers.join(',')); 
	    		 
	            setSelectedParticipants(data.participants);  

	            var jstree = $('#organization-chart').jstree(true);
			    if (jstree) {
			        participants.forEach(function(memberNo) {
			            var nodeId = 'member_' + memberNo;
			            if (jstree.get_node(nodeId)) { 
			                jstree.check_node(nodeId);
			            }
			        });
			    } 
	        }
	    });  
	}
	
    // 조직도에서 체크박스 선택 설정
    function setSelectedParticipants(participants) {
	    var jstree = $('#organization-chart').jstree(true);
	    if (jstree) {
	        participants.forEach(function(memberNo) {
	            var nodeId = 'member_' + memberNo;
	            if (jstree.get_node(nodeId)) { 
	                jstree.check_node(nodeId);
	            }
	        });
	    } 
	}
	
	// 삭제
	// 일정 삭제 버튼
	document.getElementById('deleteEventBtn').addEventListener('click', function() { 
		const isException = document.getElementById('isException').value;
		const isOne = document.getElementById('eventViewRepeatInfo').innerText.trim(); 
	    const eventId = document.getElementById('eventId').value;  
	    const isOneBoolean = !!isOne;  
	    
 		// 반복 예외
	    if (isException === "0" && !isOneBoolean) { 
	        exceptionDelete(eventId);
	    } 
	    // 기본 일정 (반복X)
	    else if(!isOneBoolean){  
	        basicDelete(eventId);
	    }
	    else {  
			document.getElementById('event_repeat_title').innerText ='반복 일정 삭제';
			openEventRepeatModal(); 
		}
	    
	});
 
 	// 기본 삭제
 	function basicDelete(eventId) {
		Swal.fire({
            text: '일정을 삭제하시겠습니까?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#f8f9fa',
            confirmButtonText: '삭제',
            cancelButtonText: '취소'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    type: 'POST',  
                    url: '/company/schedule/delete',   
                    contentType: 'application/json',
                    data: JSON.stringify({ eventId: eventId }),  
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    success: function(response) {
                        if (response.res_code === '200') {
                            Swal.fire({
                                text: response.res_msg,
                                icon: 'success',
                                confirmButtonColor: '#0056b3',
                                confirmButtonText: '확인'
                            }).then(() => {
                                 location.href = "/employee/schedule";  
                            });
                        } else {
                            Swal.fire({
                                text: response.res_msg,
                                icon: 'error',
                                confirmButtonColor: '#0056b3',
                                confirmButtonText: '확인'
                            });
                        }
                    },
                    error: function () {
                        Swal.fire('서버 오류', response.res_msg, 'error');
                    }
                });
            }
        }); 
	}
	
	// 예외 삭제
 	function exceptionDelete(eventId) {
		Swal.fire({
            text: '일정을 삭제하시겠습니까?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#f8f9fa',
            confirmButtonText: '삭제',
            cancelButtonText: '취소'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    type: 'POST',  
                    url: '/company/schedule/exception/delete',   
                    contentType: 'application/json',
                    data: JSON.stringify({ eventId: eventId }),  
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    success: function(response) {
                        if (response.res_code === '200') {
                            Swal.fire({
                                text: response.res_msg,
                                icon: 'success',
                                confirmButtonColor: '#0056b3',
                                confirmButtonText: '확인'
                            }).then(() => {
                                 location.href = "/employee/schedule";  
                            });
                        } else {
                            Swal.fire({
                                text: response.res_msg,
                                icon: 'error',
                                confirmButtonColor: '#0056b3',
                                confirmButtonText: '확인'
                            });
                        }
                    },
                    error: function () {
                        Swal.fire('서버 오류', response.res_msg, 'error');
                    }
                });
            }
        }); 
	}
	
	// 반복 삭제  
 	function repeatDelete(eventId, repeatEditOption) {   
        $.ajax({
            type: 'POST',   
            url: '/company/schedule/repeat/delete/' + eventId + '?editOption=' + repeatEditOption + '&pickStartDate=' + pickStartDate + '&pickEndDate=' + pickEndDate,   
            contentType: 'application/json', 
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            success: function(response) {
                if (response.res_code === '200') {
                    Swal.fire({
                        text: response.res_msg,
                        icon: 'success',
                        confirmButtonColor: '#0056b3',
                        confirmButtonText: '확인'
                    }).then(() => {
                         location.href = "/employee/schedule";  
                    });
                } else {
                    Swal.fire({
                        text: response.res_msg,
                        icon: 'error',
                        confirmButtonColor: '#0056b3',
                        confirmButtonText: '확인'
                    });
                }
            },
            error: function () {
                Swal.fire('서버 오류', response.res_msg, 'error');
            }
        });
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
	    document.getElementById('event_repeat_title').style.display = 'none';  
	    document.getElementById('editEventBtn').style.display = 'none';
	    document.getElementById('deleteEventBtn').style.display = 'none'; 
		document.getElementById('departmentName').style.display = 'none';
		document.getElementById('event_modal_vacation').style.height = '150px'; 
		
		
		document.getElementById('par_join').style.display = 'none'; 
		document.getElementById('par_join_name').style.display = 'none'; 
	    document.getElementById('par_create_name').style.display = 'none'; 
		document.getElementById('par_create_name').style.display = 'none';  
			 
	    modal.style.display = 'block';  
	}
    
	document.getElementById('closeScheduleModalBtn').addEventListener('click', function() {    
		document.getElementById('eventViewModal').style.display = 'none';
	});
	
	// 회의실 상세 모달
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
	    document.getElementById('event_repeat_title').style.display = 'block';  
	    document.getElementById('editEventBtn').style.display = 'block';
	    document.getElementById('deleteEventBtn').style.display = 'block';   
	 	  
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
	    document.getElementById('editEventBtn').style.display = 'none';
	    document.getElementById('deleteEventBtn').style.display = 'none';
	    document.getElementById('eventViewRepeatInfo').style.display = 'none';
	    document.getElementById('eventViewCreatedDate').style.display = 'none';
	    document.getElementById('event_modal_vacation').style.height = '500px'; 
   		modal.style.display = 'block';  
	}  
	 
	document.getElementById('searchButton').addEventListener('click', filterEvents);
	document.getElementById('searchstartDate').addEventListener('change', function () {
	    const startDateValue = this.value; 
	    const endDateInput = document.getElementById('searchendDate');  
	 
	    endDateInput.value = '';
	 
	    endDateInput.min = startDateValue;   
	});
	
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '일정&emsp;';
	
	// 알림
	window.functionTypes = [11,12,13];  
    console.log("현재 기능 타입: " +window.functionTypes);

    if (window.functionTypes.includes(11) || window.functionTypes.includes(12) || window.functionTypes.includes(13)) {
	     markNotificationsAsRead(11); 
	     markNotificationsAsRead(12);  
	     markNotificationsAsRead(13);  
	 }
});