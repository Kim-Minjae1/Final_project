document.addEventListener('DOMContentLoaded', function() {
	var csrfToken = document.querySelector('input[name="_csrf"]').value;
	var createCompanyScheduleForm = document.getElementById("eventForm");
	const startInput = document.getElementById('startTime');
    const endInput = document.getElementById('endTime'); 
    
    var calendarEl = document.getElementById('calendar');
	var calendar;
	var categoryColors = {};
	var categoryNames = {};
	
	let selectedDate = null;
	var pickStartDate = null;
	var pickEndDate = null; 
  	
    var allEvents = [];  
  	   
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
            fetchSchedules();
        }
    });
    
    function fetchSchedules() {
	    $.ajax({
	        url: '/api/company/schedules',
	        method: 'GET',
	        contentType: 'application/json',
	        headers: {
	            'X-CSRF-TOKEN': csrfToken
	        }
	    })
	    .done(function(schedules) {
	        $.ajax({
	            url: '/api/repeat/schedules',
	            method: 'GET',
	            contentType: 'application/json',
	            headers: {
	                'X-CSRF-TOKEN': csrfToken
	            }
	        })
	        .done(function(repeats) {
	            $.ajax({
	                url: '/api/company/exception/schedules',
	                method: 'GET',
	                contentType: 'application/json',
	                headers: {
	                    'X-CSRF-TOKEN': csrfToken
	                }
	            })
	            .done(function(exceptions) {
	                var events = [];
	
	                schedules.forEach(function(schedule) {
	                    if (schedule.schedule_repeat === 0) { 
	                        events.push(createEvent(schedule));
	                    } else {
	                        // 반복 일정
	                        var repeatInfo = repeats.find(r => r.schedule_no === schedule.schedule_no);
	                        if (repeatInfo) {
	                            var startDate = new Date(schedule.schedule_start_date);
	                            var endDate = repeatInfo.schedule_repeat_end_date ? new Date(repeatInfo.schedule_repeat_end_date) : new Date(startDate.getFullYear() + 1, startDate.getMonth(), startDate.getDate());
	                            var currentDate = new Date(startDate);
	                            
	                            while (currentDate <= endDate) {
	                                // 예외 일정 검사
	                                var exceptionEvent = exceptions.find(e => 
	                                    e.schedule_no === schedule.schedule_no && 
	                                    e.schedule_exception_date === formatDate(currentDate)
	                                );
	
	                                if (exceptionEvent && exceptionEvent.schedule_exception_status === 0) {
	                                    events.push(createExceptionEvent(exceptionEvent, currentDate));
	                                } else if (exceptionEvent && exceptionEvent.schedule_exception_status === 1) {
	                                    
	                                } else {
	                                    events.push(createEvent(schedule, currentDate, repeatInfo));
	                                }
	                                 
	                                currentDate = calculateNextRepeatDate(currentDate, repeatInfo);
	                            }
	                        }
	                    }
	                }); 
	                allEvents.push(events);
	                initializeCalendar(events);
	            })
	        }) 
	    }) 
	}
	
	// 예외 일정
	function createExceptionEvent(exceptionEvent, currentDate) {
	    const endDate = exceptionEvent.schedule_exception_end_date ? new Date(exceptionEvent.schedule_exception_end_date) : null;
	
	    if (endDate) { 
	        endDate.setDate(endDate.getDate() + 1);
	    }
	
	    return {
	        order: 1,
	        className: 'exception-event',
	        id: exceptionEvent.schedule_exception_no,
	        title: exceptionEvent.schedule_exception_title,
	        start: exceptionEvent.schedule_exception_start_date + (exceptionEvent.schedule_exception_start_time ? 'T' + exceptionEvent.schedule_exception_start_time : ''),
	        end: endDate ? formatDate(endDate) + (exceptionEvent.schedule_exception_end_time ? 'T' + exceptionEvent.schedule_exception_end_time : '') : null,
	        allDay: !exceptionEvent.schedule_exception_start_time && !exceptionEvent.schedule_exception_end_time,
	        backgroundColor: categoryColors[exceptionEvent.schedule_category_no] || '#3788d8',
	        borderColor: categoryColors[exceptionEvent.schedule_category_no] || '#3788d8',
	        textColor: '#000000',
	        extendedProps: {
	            categoryName: categoryNames[exceptionEvent.schedule_category_no],
	            comment: exceptionEvent.schedule_exception_comment,
	            createDate: exceptionEvent.schedule_exception_create_date,
	            endDate: exceptionEvent.schedule_exception_end_date ? exceptionEvent.schedule_exception_end_date : null,
	            startTime: exceptionEvent.schedule_exception_allday === 0 ? exceptionEvent.schedule_exception_start_time : null,
	            endTime: exceptionEvent.schedule_exception_allday === 0 ? exceptionEvent.schedule_exception_end_time : null,
	            exceptionNo: exceptionEvent.schedule_exception_no,
	            isException: true
	        }
	    };
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

	// DB 일정 생성
	function createEvent(schedule, currentDate, repeatInfo) { 
	    var eventStart = currentDate || new Date(schedule.schedule_start_date);
	    var eventEnd = schedule.schedule_end_date ? 
	        new Date(new Date(schedule.schedule_end_date).getTime() + 24 * 60 * 60 * 1000) :  
	        null;
	
	    if (eventEnd) { 
	        if (currentDate) {
	            var duration = eventEnd.getTime() - new Date(schedule.schedule_start_date).getTime();
	            eventEnd = new Date(eventStart.getTime() + duration);
	        }
	    }
	
	    return { 
			order:1,
			className: 'db-event',
	        id: schedule.schedule_no,
	        title: schedule.schedule_title,
	        start: formatDate(eventStart) + (schedule.schedule_start_time ? 'T' + schedule.schedule_start_time : ''),
	        end: eventEnd ? formatDate(eventEnd) + (schedule.schedule_end_time ? 'T' + schedule.schedule_end_time : '') : null,
	        allDay: schedule.schedule_allday === 1,
	        backgroundColor: categoryColors[schedule.schedule_category_no] || '#3788d8',  
	        borderColor: categoryColors[schedule.schedule_category_no] || '#3788d8',
	        textColor : '#000000',
	        extendedProps: {
	            categoryName: categoryNames[schedule.schedule_category_no],
	            comment: schedule.schedule_comment,
	            repeatOption: schedule.schedule_repeat,
	            createDate: schedule.schedule_create_date,
	            startDate: eventStart ? schedule.schedule_start_date : null,
	            endDate: eventEnd ? schedule.schedule_end_date : null,  
	            startTime: schedule.schedule_allday === 0 ? schedule.schedule_start_time : null,
	            endTime: schedule.schedule_allday === 0 ? schedule.schedule_end_time : null,  
	            repeatType: repeatInfo ? repeatInfo.schedule_repeat_type : null,
	            repeatDay : repeatInfo ? repeatInfo.schedule_repeat_day : null,
	            repeatWeek: repeatInfo ? repeatInfo.schedule_repeat_week : null,
	            repeatDate : repeatInfo ? repeatInfo.schedule_repeat_date : null,
	            repeatMonth: repeatInfo ? repeatInfo.schedule_repeat_month : null,
	            createData: true
	        } 
	    };
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
			    } else if (info.event.extendedProps.createData) {
			        const eventId = info.event.id;
			        showEventModalById(calendar, eventId);
			    } else {
			        info.jsEvent.preventDefault();
			    }  
            },
            eventDidMount: function(info) { 
	            if (info.event.extendedProps.createData || info.event.extendedProps.isException) {
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
                    allDay:true,
                    order:-1 
                },
                { 
		            events: events 
		        }
            ],
            eventOrder: '-order,-allDay, start',
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
            dateClick: function(info) {
			    selectedDate = info.dateStr;   
			    $('#eventDate').val(selectedDate);   
			    document.getElementById('eventDate').dispatchEvent(new Event('change'));
    
			    document.getElementById('eventModal').style.display = 'block';
			    const submitButton = document.getElementById('create_modal_submit');
				submitButton.textContent = '저장';
		    }
        });

        calendar.render();
    }
    
    window.addEventListener('click', function(event) {
	    const modal = document.getElementById('eventViewModal');
	    if (event.target === modal) {
	        modal.style.display = 'none';
	    }
	});

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

    function showEventModalById(calendar, eventId) {
	    const event = calendar.getEventById(eventId);   
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
		
	    title.textContent = event.title;
	    category.textContent = `[${event.extendedProps.categoryName}]`;
	   
		const startDate = pickStartDate;
	    const endDate = pickEndDate;
	    const startTime = event.extendedProps.startTime;
	    const endTime = event.extendedProps.endTime;
	    
	    hiddenIsException.value = 0;
	    
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
			
	    hiddenEventId.value = event.extendedProps.exceptionNo; 
	    comment.textContent = event.extendedProps.comment; 
	    createdDate.textContent = event.extendedProps.createDate.substr(0,10) + ` 등록`; 
	    repeatInfo.textContent = '';
	
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
	    
    document.getElementById('closeScheduleModalBtn').addEventListener('click', function() {
        document.getElementById('eventViewModal').style.display = 'none';
    });
 	
	function formatDate(date) {
	    return date.getFullYear() + '-' + 
	           String(date.getMonth() + 1).padStart(2, '0') + '-' + 
	           String(date.getDate()).padStart(2, '0');
	}
 
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

    function resetForm(form) {
        form.reset(); 
    }
    
	// 등록 모달 열기
	document.getElementById('addEventBtn').addEventListener('click', function() {
		const submitButton = document.getElementById('create_modal_submit');
		submitButton.textContent = '저장';
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
                
                const editTitle = document.getElementById('modal-title');
				editTitle.textContent = '사내 일정 등록'; 
				resetForm(createCompanyScheduleForm); 
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
                
                const editTitle = document.getElementById('modal-title');
				editTitle.textContent = '사내 일정 등록'; 
				
				resetForm(createCompanyScheduleForm);
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
        url: '/categories', 
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
	        schedule_month: repeatMonth
	    };
	 
		if(type_form === "저장") {     
		    $.ajax({
		        type: "POST",
		        url: '/company/schedule/save',   
		        contentType: 'application/json',
		        data: JSON.stringify(eventData),
		        headers: {
		            'X-CSRF-TOKEN': csrfToken
		        },
		        success: function(response) {
		            Swal.fire({
		                text: '일정이 저장되었습니다.',
		                icon: 'success',
		                confirmButtonText: '확인',
		                confirmButtonColor: '#0056b3'
		            }).then(function() {
		                window.location.reload();  
		                document.getElementById('eventModal').style.display = 'none';
		            });
		        } 
		    }); 
	    	
		}
		// 수정
		else {
			const isRecurring = document.getElementById('isRecurring').value;    
		    if (isRecurring === "1") {   
		        openEventRepeatModal();
		    } else { 
		        submitEventUpdate(); 
		    }
		}
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
	
	// 메시지
	function showAlert(message) {
	    Swal.fire({
	        text: message,
	        icon: 'warning',
	        confirmButtonColor: '#0056b3',
	        confirmButtonText: '확인',
	    });
	}
	
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
	
	// 일정 수정
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
		
	    const editTitle = document.getElementById('modal-title');
	    editTitle.textContent = '일정 수정';
	    
		const submitButton = document.getElementById('create_modal_submit');
   		submitButton.textContent = '수정';
    
	    $.ajax({
	        url: '/schedule/edit/' + eventNo,
	        type: 'GET',
	        dataType: 'json',
	        success: function(data) { 
				
	            $('#eventId').val(data.schedule.schedule_no);  
	            const repeatValue = data.schedule.schedule_repeat;
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
	            
	            if (repeatValue === 1) { 
		            $('#repeatOption').val(data.scheduleRepeat.schedule_repeat_type);
		            document.getElementById('repeatOption').dispatchEvent(new Event('change'));
	                $('#repeatEndDate').val(data.scheduleRepeat.schedule_repeat_end_date);
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
	    
	    $.ajax({
	        url: '/schedule/exception/edit/' + eventId,
	        type: 'GET',
	        dataType: 'json',
	        success: function(data) { 
	            $('#eventId').val(eventId);  
	            $('#isRecurring').val('0');    
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
	 	
	 	if(typeEventRepeat === "반복 일정 수정") {
		    handleRecurringEventUpdate(eventId, repeatEditOption);			
		} else {
			repeatDelete(eventId, repeatEditOption);
		}
	 
	    document.getElementById('eventRepeatModal').style.display = 'none';
	}); 
	
	// 반복 일정 수정  
	function handleRecurringEventUpdate(eventId, repeatEditOption) {
	    const eventData = getEventFormData();    
	    
	    $.ajax({
	        type: "POST",
	        url: '/company/schedule/edit/recurring/' + eventId + '?editOption=' + repeatEditOption + '&pickStartDate=' + pickStartDate + '&pickEndDate=' + pickEndDate,
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
	            } else {
	                Swal.fire('반복 일정', response.res_msg, 'error');
	            }   
            },
	        error: function () {
	            Swal.fire("서버 오류", "반복 일정 수정 중 오류가 발생했습니다.", "error");
	        }
	    });
	} 

	// 일정 수정
	function submitEventUpdate() {
	    const eventData = getEventFormData();
    	const isException = $('#isRecurring').val() === '1';
		const url = isException ? '/company/schedule/exception/edit/' : '/company/schedule/edit/';
	    
	    $.ajax({
	        type: "POST",
	        url: url + document.getElementById('eventId').value,
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
	        },
	        error: function(xhr, status, error) {
	            console.error('일정 수정 오류: ', error);
	        }
	    });
	}
	
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
	
	    return {
	        title: title,
	        category: category,
	        startDate: startDate,
	        endDate: allDay ? endDate : null,
	        startTime: allDay ? null : startTime,
	        endTime: allDay ? null : endTime,
	        allDay: allDay,
	        repeat: repeatOption,
	        description: description,
	        repeatEndDate: repeatEndDate,
	        schedule_day_of_week: repeatDayOfWeek,
	        schedule_week: repeatWeek,
	        schedule_date: repeatDate,
	        schedule_month: repeatMonth
	    };
	}
	
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
                                 location.href = "/schedule/company";  
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
                                 location.href = "/schedule/company";  
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
                         location.href = "/schedule/company";  
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
    
	document.getElementById('searchstartDate').addEventListener('change', function () {
	    const startDateValue = this.value; 
	    const endDateInput = document.getElementById('searchendDate');  
	 
	    endDateInput.value = '';
	 
	    endDateInput.min = startDateValue;   
	});
 
	document.getElementById('searchButton').addEventListener('click', function () {
	    const searchTerm = document.getElementById('searchInput').value.toLowerCase().trim();
	    const searchCategory = document.getElementById('searchCategory').value;
	    const startDateValue = document.getElementById('searchstartDate').value;
	    const endDateValue = document.getElementById('searchendDate').value;
	
	    const startDate = startDateValue ? new Date(startDateValue) : null;
	    const endDate = endDateValue ? new Date(endDateValue) : null;
	
	    const events = allEvents[0];
	     
	    const holidayEvents = calendar.getEvents().filter(event => 
	        event.extendedProps.description === '공휴일'
	    );
	    
	    calendar.removeAllEvents();
	     
	    holidayEvents.forEach(event => calendar.addEvent(event));
	
	    if (searchTerm === '' && !startDate && !endDate) {
	        events.forEach(event => {
	            calendar.addEvent(event);
	        });
	        return;
	    }
	
	    const filteredEvents = events.filter(event => {
	        const titleMatch = event.title.toLowerCase().includes(searchTerm);
	        const contentMatch = event.extendedProps.comment &&
	            event.extendedProps.comment.toLowerCase().includes(searchTerm);
	        const categoryMatch = event.extendedProps.categoryName &&
	            event.extendedProps.categoryName.toLowerCase().includes(searchTerm);
	
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
	
	        switch (searchCategory) {
	            case 'all':
	                return (titleMatch || contentMatch || categoryMatch) && withinDateRange;
	            case 'content':
	                return contentMatch && withinDateRange;
	            case 'categoryName':
	                return categoryMatch && withinDateRange;
	            default:
	                return false;
	        }
	    });
	
	    filteredEvents.forEach(event => {
	        calendar.addEvent(event);
	    });
	});
	
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '일정 관리&emsp;&gt;&emsp;전사 일정';
});