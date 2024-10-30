$(document).ready(function() { 
	
	// 참여 등록 사원 번호
	let participantMembers = [];
	let reservationOwnerNo = null;
	let reservationOwnerName = null;
	let reservationOwnerPosition = null;
	var csrfToken = document.querySelector('input[name="_csrf"]').value;
	
    const date = new Date();
    const year = date.getFullYear();
    const month = ('0' + (date.getMonth() + 1)).slice(-2);
    const day = ('0' + date.getDate()).slice(-2);
    const today = `${year}-${month}-${day}`;
	
    // 현재 시간
    function getCurrentTime() {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes}`;
    }
    
    $('#editReservationButton').on('click', function() { 
	    const reservationNo = $(this).data('reservation-no'); 
	    openReservationModal(reservationNo); 
	});
	
	// 수정 모달
	function openReservationModal(reservationNo) {
        var modal = document.getElementById('reservationModal');
        modal.style.display = 'block';
        currentReservationNo = reservationNo;
        
        $.ajax({
            url: '/employee/meeting/reservation/detail/modal/' + reservationNo,
            type: 'GET',
            dataType: 'json',
            success: function(data) {   
                
                $('#reservationId').val(data.reservation.meeting_reservation_no);
                $('#reservation_date_input').val(data.reservation.meeting_reservation_date);
                $('#reservation_purpose').val(data.reservation.meeting_reservation_purpose);
           
                let selectedParticipants = data.participants.map(p => `<span class="selected-participants">${p.memberName} ${p.positionName}`).join(' </span>');
                $('.selected-participants-container').html(selectedParticipants);
                 
            	participantMembers = data.participants.map(p => p.member_no); 
            	reservationOwnerNo = data.reservation.member_no; 
            	reservationOwnerName = data.reservation.member_name;
            	reservationOwnerPosition = data.reservation.position_name;
            	
                fetchMeetings(function() {
                    $('#reservation_room').val(data.reservation.meeting_no);
                    
                    const selectedRoom = $('#reservation_room').find(':selected');
                    const startTime = selectedRoom.data('start');
                    const endTime = selectedRoom.data('end');
                    
                    if (startTime && endTime) {
                        populateTimeSelect(startTime, endTime, 'start');
                        
                        $('#reservation_start_time').val(data.reservation.meeting_reservation_start_time);
                        
                        fetchRoomReservations(data.reservation.meeting_reservation_date, data.reservation.meeting_no, function(filteredReservations) {
                            currentReservations = filteredReservations;
                            disableReservedTimes(filteredReservations);
                             
                            updateEndTimeOptions(data.reservation.meeting_reservation_start_time);
                            $('#reservation_end_time').val(data.reservation.meeting_reservation_end_time);
                        });
                    }
                });
            	// 조직도에서 체크박스 선택 설정
                setSelectedParticipants(data.participants); 
            } 
        });  
    }

    // 수정 모달 닫기 
    $('#reservation_close').on('click', function () {
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
	           $('#reservationModal').hide();
	        }
        });
    }); 

    // 회의실 전체 정보
    function fetchMeetings(callback) {
        $.ajax({
            url: '/api/meetings',
            type: 'GET',
            success: function(data) {
                meetings = data; 
                populateRoomSelect(meetings);  
                if (callback) callback();
            } 
        });
    }
    
    // 예약 정보  
    function fetchReservations(date) {
        $.ajax({
            url: '/date/reservations',
            type: 'GET',
            data: { date: date },
            success: function(reservations) {  
                updateReservationTable(reservations, date);
            } 
        });
    }
   
    // 회의실 선택 옵션  
    function populateRoomSelect(rooms) {
        const roomSelect = $('#reservation_room');
        roomSelect.empty().append('<option value="">회의실 선택</option>');
        rooms.forEach(room => {
            roomSelect.append(`<option value="${room.meeting_no}" data-start="${room.meeting_available_start}" data-end="${room.meeting_available_end}">${room.meeting_name}</option>`);
        });
    }

    // 회의실 선택 
    $('#reservation_room').on('change', function() {
        const selectedRoom = $(this).find(':selected');
        const startTime = selectedRoom.data('start');
        const endTime = selectedRoom.data('end');
        
        if (startTime && endTime) {
            const date = $('#reservation_date').val();
            
            if (date) {
                populateTimeSelect(startTime, endTime, 'start');
                populateTimeSelect(startTime, endTime, 'end');
                
                fetchRoomReservations(date, $(this).val(), function(filteredReservations) {
                    currentReservations = filteredReservations;
                    disableReservedTimes(filteredReservations); 
                });
                 
                $('#reservation_start_time').prop('disabled', false);
            } else {
                $('#reservation_start_time').prop('disabled', true).empty().append('<option value="">시작 시간</option>');
                $('#reservation_end_time').prop('disabled', true).empty().append('<option value="">종료 시간</option>');
            }
        } else {
            $('#reservation_start_time').prop('disabled', true).empty().append('<option value="">시작 시간</option>');
            $('#reservation_end_time').prop('disabled', true).empty().append('<option value="">종료 시간</option>');
        }
    });
    
    // 날짜 선택  
    document.getElementById("reservation_date_input").setAttribute('min', today);
    $('#reservation_date_input').on('change', function() {
        const roomId = $('#reservation_room').val();
        const date = $(this).val();
        
        if (roomId && date) {
            const startTime = $('#reservation_room').find(':selected').data('start');
            const endTime = $('#reservation_room').find(':selected').data('end');
            
            populateTimeSelect(startTime, endTime, 'start');
            populateTimeSelect(startTime, endTime, 'end');
            
            fetchRoomReservations(date, roomId, function(filteredReservations) {
                currentReservations = filteredReservations; 
                disableReservedTimes(filteredReservations);  
            });
             
            $('#reservation_start_time').prop('disabled', false);
        } else {
            $('#reservation_start_time').prop('disabled', true).empty().append('<option value="">시작 시간</option>');
            $('#reservation_end_time').prop('disabled', true).empty().append('<option value="">종료 시간</option>');
        }
    });

    // 시작 시간 선택  
    $('#reservation_start_time').on('change', function() {
        const startTime = $(this).val(); 
        if (startTime) {
            updateEndTimeOptions(startTime);
            $('#reservation_end_time').prop('disabled', false); 
        } else {
            $('#reservation_end_time').prop('disabled', true).html('<option value="">종료 시간</option>'); 
        }
    });
     
    let meetings = [];
    const timeSlots = [];
    for (let hour = 7; hour < 23; hour++) {
        for (let minute = 0; minute < 60; minute += 30) {
            timeSlots.push(`${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`);
        }
    }
    timeSlots.push('23:00');
    
    // 시간 선택 옵션 
	function populateTimeSelect(startTime, endTime, type) {
	        const select = type === 'start' ? $('#reservation_start_time') : $('#reservation_end_time');
	        const selectedDate = $('#reservation_date').val();
	        const currentTime = getCurrentTime();
	        
	        select.empty().append(`<option value="">${type === 'start' ? '시작 시간' : '종료 시간'}</option>`);
	        
	        timeSlots.forEach(time => {
	            let shouldDisable = false;
	            if (selectedDate === today && time <= currentTime) {
	                shouldDisable = true;
	            }
	            
	            if (type === 'start' && time >= startTime && time < endTime) {
	                select.append(`<option value="${time}" ${shouldDisable ? 'disabled' : ''}>${time}</option>`);
	            } else if (type === 'end' && time > startTime && time <= endTime) {
	                select.append(`<option value="${time}" ${shouldDisable ? 'disabled' : ''}>${time}</option>`);
	            }
	        });
	    }
     
     
    // 예약된 시간 비활성화  
	function disableReservedTimes(reservations) {
        const startSelect = $('#reservation_start_time');
        const selectedDate = $('#reservation_date_input').val();
        const currentTime = getCurrentTime(); 
        
        if (selectedDate === today) {
            startSelect.find('option').filter(function() {
                return $(this).val() <= currentTime;
            }).prop('disabled', true);
        }
        
		reservations.forEach(reservation => {
            if (reservation.meeting_reservation_no != currentReservationNo) {
                const start = reservation.meeting_reservation_start_time;
                const end = reservation.meeting_reservation_end_time;
                
                startSelect.find('option').filter(function() {
                    return $(this).val() >= start && $(this).val() <= end;
                }).prop('disabled', true);
            }
        });
    }

    // 종료 시간 옵션 업데이트  
    function updateEndTimeOptions(startTime) {
        const endSelect = $('#reservation_end_time');
        const roomEndTime = $('#reservation_room').find(':selected').data('end');
        
        endSelect.empty().append('<option value="">종료 시간</option>');

        let nextReservationStart = null;
        currentReservations.forEach(reservation => {
            if (reservation.meeting_reservation_no != currentReservationNo &&
                reservation.meeting_reservation_start_time > startTime &&
                (!nextReservationStart || reservation.meeting_reservation_start_time < nextReservationStart)) {
                nextReservationStart = reservation.meeting_reservation_start_time;
            }
        });

        timeSlots.forEach(time => {
            if (time > startTime && time <= roomEndTime) {
                const option = $(`<option value="${time}">${time}</option>`);
                
                if (nextReservationStart && time >= nextReservationStart) {
                    option.prop('disabled', true);
                }

                endSelect.append(option);
            }
        });
    }
    
    // 특정 회의실 예약 정보 
    function fetchRoomReservations(date, roomId, callback) {
        $.ajax({
            url: '/date/reservations',
            type: 'GET',
            data: { date: date },
            success: function(reservations) {
                const filteredReservations = [];
    
                for (var i = 0; i < reservations.length; i++) {
                    if ((reservations[i].meeting_reservation_date === date) && 
                        (reservations[i].meeting_no == roomId)) {
                        filteredReservations.push(reservations[i]);
                    }
                } 
                callback(filteredReservations);
            } 
        });
    } 
    
    // 조직도
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

    
    $('#openOrganizationChartButton').on('click', function() {
        openOrganizationChartModal();
    });
	
	function openOrganizationChartModal() {
	    selectedMembers = [];  
	 
	    const displayElement = document.getElementById('selected-members');
	    if (displayElement) {
	        displayElement.innerHTML = '';
	    }
	
	    $('#organizationChartModal').modal('show');
	
	    loadOrganizationChart();
	}

	 
    // 조직도 로딩
    function loadOrganizationChart() {
        $.ajax({
            url: '/meeting/chart',
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
        
		selectedMembers.push(reservationOwnerNo.toString());
        $('#selectedMembers').val(selectedMembers.join(',')); 

        localStorage.setItem('selectedMembers', JSON.stringify(selectedMembers));
    }  
    
    // 조직도 확인 -> 수정 모달 사원 출력 
	$('#participate_confirmButton').click(function()  {
	    const reservationArea = $('.selected-participants-container');  
	    const selectedMembersContainer = $('#selected-members');  
	    const selectedMembersList = selectedMembersContainer.find('.selected-member');
	    
	    reservationArea.find('.selected-participants').remove();   
	     
	    const reservationOwnerItem = $('<span class="selected-participants"></span>');
        reservationOwnerItem.text(reservationOwnerName + " " + reservationOwnerPosition);
        reservationArea.append(reservationOwnerItem);
         
	    selectedMembersList.each(function() {
	        const memberName = $(this).find('span').text();
	        const participantItem = $('<span class="selected-participants"></span>');
	        participantItem.text(memberName); 
	        reservationArea.append(participantItem);
	    }); 
	    $('#organizationChartModal').modal('hide');
	 });
	 
	 // 수정 확인 
	 $("#reservationForm").submit(function (e) {
	    e.preventDefault(); 
	    
	    var reservationRoom = $('#reservation_room').val();
	    var reservationDate = $('#reservation_date_input').val();
	    var reservationStartTime = $('#reservation_start_time').val();
	    var reservationEndTime = $('#reservation_end_time').val();
	    var reservationPurpose = $('#reservation_purpose').val();
	 
	    if (!reservationRoom) {
	        Swal.fire({
	            text: '회의실을 선택해 주세요.',
	            icon: 'warning',
	            confirmButtonColor: '#0056b3',
	            confirmButtonText: '확인'
	        });
	        return;
	    }
	
	    if (!reservationDate) {
	        Swal.fire({
	            text: '예약일을 선택해 주세요.',
	            icon: 'warning',
	            confirmButtonColor: '#0056b3',
	            confirmButtonText: '확인'
	        });
	        return;
	    }
	
	    if (!reservationStartTime) {
	        Swal.fire({
	            text: '시작 시간을 선택해 주세요.',
	            icon: 'warning',
	            confirmButtonColor: '#0056b3',
	            confirmButtonText: '확인'
	        });
	        return;
	    }
	
	    if (!reservationEndTime) {
	        Swal.fire({
	            text: '종료 시간을 선택해 주세요.',
	            icon: 'warning',
	            confirmButtonColor: '#0056b3',
	            confirmButtonText: '확인'
	        });
	        return;
	    }
	
	    if (reservationEndTime <= reservationStartTime) {
	        Swal.fire({
	            text: '종료 시간은 시작 시간 이후로 설정해 주세요.',
	            icon: 'warning',
	            confirmButtonColor: '#0056b3',
	            confirmButtonText: '확인'
	        });
	        return;
	    }
	
	    if (!reservationPurpose) {
	        Swal.fire({
	            text: '예약 목적을 입력해 주세요.',
	            icon: 'warning',
	            confirmButtonColor: '#0056b3',
	            confirmButtonText: '확인'
	        });
	        return;
	    }
    
	    var formData = new FormData(this); 
        
	    $.ajax({
	        url: '/reservation/update',
	        type: 'POST',
	        data: formData,
	        processData: false,
	        contentType: false,
	        headers: {
	            'X-CSRF-TOKEN': csrfToken
	        },
	        success: function (response) {
	            if (response.res_code === "200") {
	                Swal.fire({
	                    text: response.res_msg,
	                    icon: 'success',
	                    confirmButtonColor: '#0056b3',
	                    confirmButtonText: '확인'
	                }).then(() => {
	                    location.reload();
	                });
	                
	                const memberNo = $('#memberNo').val();
			        const selectedMembers = $('#selectedMembers').val();
			        const reservationDate = $('#reservation_date_input').val(); 
			         
			        alarmSocket.send(JSON.stringify({
			            type: 'noficationParticipantMeeting', 
			            memberNo: memberNo,
			            participants: selectedMembers,
			            reservationDate: reservationDate 
			        })); 
	            } else {
	                Swal.fire({
	                    text: response.res_msg,
	                    icon: 'error',
	                    confirmButtonColor: '#0056b3',
	                    confirmButtonText: '확인'
	                });
	            }
	            $('#reservationModal').modal('hide');
	        } 
	    });
	}); 
	 
	// 예약 취소  
	$('#cancelReservationButton').on('click', function() { 
	    const reservationNo = $(this).data('reservation-no'); 
	    
	    cancelReservation(reservationNo); 
	});
	
	function cancelReservation(reservationNo) {
	    Swal.fire({
	        text: '예약을 취소하시겠습니까?', 
	        icon: 'warning',
	        showCancelButton: true,
	        confirmButtonColor: '#0056b3',
	        cancelButtonColor: '#f8f9fa',
	        confirmButtonText: '확인',
	        cancelButtonText: '취소'
	    }).then((result) => {
	        if (result.isConfirmed) { 
	            $.ajax({
	                type: 'POST',
	                url: '/reservation/cancel',
	                contentType: 'application/json',
	                headers: {
	                    'X-CSRF-TOKEN': csrfToken
	                },
	                data: JSON.stringify({ reservationNo: reservationNo }),
	                success: function(response) {
	                    if (response.res_code === "200") {
	                        Swal.fire({
							    text: response.res_msg,
							    icon: 'success',
							    confirmButtonColor: '#0056b3',
							    confirmButtonText: '확인',
							}).then(() => {
	                            window.location.href = '/employee/meeting/reservation/list';
	                        });
	                    } else {
	                        Swal.fire({
							    text: response.res_msg,
							    icon: 'error',
							    confirmButtonColor: '#0056b3',
							    confirmButtonText: '확인',
							});
	                    }
	                },
	                error: function() {
	                    Swal.fire('서버 오류', '서버에서 오류가 발생했습니다.', 'error');
	                }
	            });
	        }
	    });
	} 
 
    const reservationButtons = document.getElementById('reservation-buttons'); 
	const reservationDate = $('#reservation_date').text();  
    const reservationStartTime = $('#reservation_start_time').text();  
	const currentTime = getCurrentTime(); 
	 
    if (reservationDate <= today && reservationStartTime < currentTime) { 
        reservationButtons.style.display = 'none';
    }
     
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '회의실&emsp;&gt;&emsp;예약 내역&emsp;&gt;&emsp;예약 상세';
 
});
