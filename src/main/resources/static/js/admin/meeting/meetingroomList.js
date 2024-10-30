document.addEventListener('DOMContentLoaded', function () { 
    var addModal = document.getElementById('addModal');
    var editModal = document.getElementById('editModal'); 
    var createForm = document.getElementById("addForm");
    var addButton = document.getElementById('addMeetingButton');
    var csrfToken = document.querySelector('input[name="_csrf"]').value;
    var addcloseButtons = document.getElementsByClassName('add_close');
    var editcloseButtons = document.getElementsByClassName('edit_close');
    
    const startInput = document.getElementById('meetingAvailableStart');
    const endInput = document.getElementById('meetingAvailableEnd');
    const edit_startInput = document.getElementById('modal-meeting-available-start');
    const edit_endInput = document.getElementById('modal-meeting-available-end');
    const imageInput = document.getElementById('meetingImage');
    const imagePreview = document.getElementById('imagePreview');

    var editForm = document.getElementById('editForm');
    var modalImagePreview = document.getElementById('modal-image-preview');
    var edit_modal_image = document.getElementById('edit_modal_image');
    var initialImageSrc = '';

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
   	edit_startInput.addEventListener('change', handleTimeChange);
    edit_endInput.addEventListener('change', handleTimeChange);

    function previewImage(event) {
        const file = event.target.files[0];
        if (file) {
            const fileType = file.type;
            const validTypes = ['image/png', 'image/jpeg'];
            if (validTypes.includes(fileType)) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    imagePreview.src = e.target.result;
                    imagePreview.style.display = 'block';
                }
                reader.readAsDataURL(file);
            } else {
                Swal.fire({
                    icon: 'warning',
                    text: 'PNG 또는 JPG 파일만 등록 가능합니다.',
                    confirmButtonColor: '#0056b3',
                    confirmButtonText: '확인'
                });
                event.target.value = "";
                imagePreview.src = '';
                imagePreview.style.display = 'none';
            }
        } else {
            imagePreview.src = '';
            imagePreview.style.display = 'none';
        }
    }
    imageInput.addEventListener('change', previewImage);

    addButton.onclick = function() {
        addModal.style.display = 'flex';
    };

     // 모달 닫기
	Array.from(addcloseButtons).forEach(function (btn) {
        btn.addEventListener('click', function () {
	        Swal.fire({
	            text: '작성한 내용이 모두 초기화됩니다.',
	            icon: 'warning',
	            showCancelButton: true,
	            confirmButtonColor: '#0056b3',
	            cancelButtonColor: '#e9ecef',
	            confirmButtonText: '확인',
	            cancelButtonText: '취소',
	        }).then((result) => {
	            if (result.isConfirmed) {
	                addModal.style.display = 'none';
	                resetModal();
	            }
	        });
	    });
	});

    function resetModal() {
        createForm.reset();
        imagePreview.src = '';
        imagePreview.style.display = 'none';
    }

    startInput.addEventListener('change', function () {
        var startTime = startInput.value;
        if (startTime) {
            endInput.disabled = false;
        } else {
            endInput.disabled = true;
        }
    });

 	document.querySelectorAll('.meetingCheckbox').forEach(function (checkbox) {
        checkbox.addEventListener('click', function (event) {
            event.stopPropagation();
        });
    });

    // 수정 모달 열기
	document.querySelector('#meetingList tbody').addEventListener('click', function (event) {
	    if (event.target.closest('tr')) {
	        var meetingId = event.target.closest('tr').getAttribute('data-id');

	        $.ajax({
	            type: 'GET',
	            url: `/meetingroomList/edit/${meetingId}`,
	            headers: {
	                'X-CSRF-TOKEN': csrfToken
	            },
	            success: function (response) {
	                if (response.res_code === '200') {
	                    var data = response.meeting;
	                    document.getElementById('modal-meeting-id').value = data.meeting_no;
	                    document.getElementById('modal-meeting-name').value = data.meeting_name;
	                    document.getElementById('modal-meeting-max').value = data.meeting_max;
	                    document.getElementById('modal-meeting-available-start').value = data.meeting_available_start;
	                    document.getElementById('modal-meeting-available-end').value = data.meeting_available_end;
	                    document.getElementById('modal-meeting-comment').value = data.meeting_comment;

	                    if (data.meeting_new_image) {
	                        initialImageSrc = `/linkOfficeImg/meeting/${data.meeting_new_image}`;
	                        modalImagePreview.src = initialImageSrc;
	                        modalImagePreview.style.display = 'block';
	                    } else {
	                        modalImagePreview.style.display = 'none';
	                    }
	                    editModal.style.display = 'flex';
	                } else {
	                    Swal.fire('오류', response.res_msg, 'error');
	                }
	            },
	            error: function () {
	                Swal.fire('서버 오류', '서버와의 통신 중 오류가 발생했습니다.', 'error');
	            }
	        });
	    }
	});


	// 수정 모달 이미지 미리보기
    edit_modal_image.addEventListener('change', function (event) {
        const file = event.target.files[0];
        if (file) {
            const fileType = file.type;
            const validTypes = ['image/png', 'image/jpeg'];
            if (validTypes.includes(fileType)) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    modalImagePreview.src = '';
                    modalImagePreview.style.display = 'none';

                    modalImagePreview.src = e.target.result;
                    modalImagePreview.style.display = 'block';
                    initialImageSrc = e.target.result;
                }
                reader.readAsDataURL(file);
            } else {
                Swal.fire({
                    icon: 'warning', 
                    text: 'PNG 또는 JPG 파일만 등록 가능합니다.',
                    confirmButtonText: '확인'
                });
                event.target.value = "";
                modalImagePreview.src = initialImageSrc;
                modalImagePreview.style.display = 'block';
            }
        } else {
            modalImagePreview.src = initialImageSrc;
            modalImagePreview.style.display = 'block';
        } 
    });

	// 수정 모달 닫기
	Array.from(editcloseButtons).forEach(function (btn) {
        btn.addEventListener('click', function () {
	        Swal.fire({
	            text: '작성한 내용이 저장되지 않습니다.',
	            icon: 'warning',
	            showCancelButton: true,
	            confirmButtonColor: '#0056b3',
	            cancelButtonColor: '#e9ecef',
	            confirmButtonText: '확인',
	            cancelButtonText: '취소',
	        }).then((result) => {
	            if (result.isConfirmed) {
	                editModal.style.display = 'none';
	                resetEditModal();
	            }
	        });
	    });
	});

    function resetEditModal() {
		editForm.reset();
        imagePreview.src = '';
        editModal.style.display = 'none';
    }

    window.onclick = function (event) {
        if (event.target === editModal) {
            editModal.style.display = 'none';
            resetEditModal();
        }
    };

	createForm.addEventListener('submit', function (event) {
        event.preventDefault();
        var meetingName = document.getElementById('meetingName').value.trim();
        var meetingMax = document.getElementById('meetingMax').value;
        var meetingAvailableStart = document.getElementById('meetingAvailableStart').value;
        var meetingAvailableEnd = document.getElementById('meetingAvailableEnd').value;
        var meetingComment = document.getElementById('meetingComment').value.trim();
        var meetingImage = document.getElementById('meetingImage').files[0];

        if (!meetingName || meetingName.length == 0) {
            Swal.fire({
			    text: '회의실 이름을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (!meetingMax) {
            Swal.fire({
			    text: '수용 인원을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
        }
        if (!meetingAvailableStart) {
            Swal.fire({
			    text: '이용 가능 시작 시간을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (!meetingAvailableEnd) {
			Swal.fire({
			    text: '이용 가능 마감 시간을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (meetingAvailableStart < "07:00") {
			Swal.fire({
			    text: '이용 시작 시간은 오전 7시 이후로 설정해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (meetingAvailableEnd > "23:00") {
			Swal.fire({
			    text: '이용 종료 시간은 오후 11시 이전으로 설정해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (meetingAvailableEnd <= meetingAvailableStart) {
			Swal.fire({
			    text: '이용 종료 시간은 이용 시작 시간 이후로 설정해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (!meetingComment || meetingComment.length == 0) {
			Swal.fire({
			    text: '설명을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (!meetingImage) {
			Swal.fire({
			    text: '이미지를 선택해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }

        var formData = new FormData(createForm);
        formData.append('meetingImage', meetingImage);

        $.ajax({
            type: "POST",
            url: "/meetingroomList/add",
            data: formData,
            contentType: false,
            processData: false,
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            success: function (response) {
                if (response.res_code === "200") {
                    Swal.fire({
			            text: '회의실 정보가 등록되었습니다.',
			            icon: 'success', 
			            confirmButtonColor: '#0056b3', 
			            confirmButtonText: '확인', 
			        }).then(() => {
			                        location.reload();
			                    });
			                }else {
			                    Swal.fire({
								    text: response.res_msg,
								    icon: 'warning',
								    confirmButtonColor: '#0056b3',
								    confirmButtonText: '확인',
								});
			                }
			            },
            error: function () {
                Swal.fire('서버 오류', '서버에서 오류가 발생했습니다.', 'error');
            }
        });
    });

    // 수정 저장
    editForm.addEventListener('submit', function (event) {
        event.preventDefault();

        var meetingName = document.getElementById('modal-meeting-name').value.trim();
        var meetingMax = document.getElementById('modal-meeting-max').value;
        var meetingAvailableStart = document.getElementById('modal-meeting-available-start').value;
        var meetingAvailableEnd = document.getElementById('modal-meeting-available-end').value;
        var meetingComment = document.getElementById('modal-meeting-comment').value.trim();
        var meetingImage = document.getElementById('edit_modal_image').files[0];

        if (!meetingName || meetingName.length == 0) {
            Swal.fire({
			    text: '회의실 이름을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (!meetingMax) {
            Swal.fire({
			    text: '수용 인원을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
        }
        if (!meetingAvailableStart) {
            Swal.fire({
			    text: '이용 가능 시작 시간을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (!meetingAvailableEnd) {
			Swal.fire({
			    text: '이용 가능 마감 시간을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (meetingAvailableStart < "07:00") {
			Swal.fire({
			    text: '이용 시작 시간은 오전 7시 이후로 설정해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (meetingAvailableEnd > "23:00") {
			Swal.fire({
			    text: '이용 종료 시간은 오후 11시 이전으로 설정해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (meetingAvailableEnd <= meetingAvailableStart) {
			Swal.fire({
			    text: '이용 종료 시간은 이용 시작 시간 이후로 설정해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }
        if (!meetingComment || meetingComment.length == 0) {
			Swal.fire({
			    text: '설명을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }

        var formData = new FormData(editForm);

        $.ajax({
            type: 'POST',
            url: '/meetingroomList/edit',
            data: formData,
            contentType: false,
            processData: false,
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            success: function (response) {
                if (response.res_code === "200") {
                    Swal.fire({
			            text: '회의실 정보가 수정되었습니다.',
			            icon: 'success', 
			            confirmButtonColor: '#0056b3', 
			            confirmButtonText: '확인', 
			        }).then(() => {
                        location.reload();
                    });
                } else {
                    Swal.fire({
					    text: response.res_msg,
					    icon: 'warning',
					    confirmButtonColor: '#0056b3',
					    confirmButtonText: '확인',
					});
                }
            },
            error: function () {
                Swal.fire('서버 오류', '서버에서 오류가 발생했습니다.', 'error');
            }
        });
    });

    function resetEditModal() {
        editForm.reset();
        imagePreview.src = '';
        imagePreview.style.display = 'none';
    }

 	// 삭제
 	document.getElementById('selectAllCheckbox').addEventListener('change', function() {
    	const isChecked = this.checked;
	    document.querySelectorAll('.meetingCheckbox').forEach(function(checkbox) {
	        checkbox.checked = isChecked;
	    });
	    updateDeleteButtonState();
	});

 	$(document).on('change', '.meetingCheckbox', function() {
	    updateDeleteButtonState();
	     
	    const allChecked = $('.meetingCheckbox').length === $('.meetingCheckbox:checked').length;
	    $('#selectAllCheckbox').prop('checked', allChecked);
	});


	function updateDeleteButtonState() {
	    const anyChecked = $('.meetingCheckbox:checked').length > 0;
	    $('#deleteMeetingButton').prop('disabled', !anyChecked);
	}

 	document.getElementById('deleteMeetingButton').addEventListener('click', function () {
	    var selectedIds = Array.from(document.querySelectorAll('.meetingCheckbox:checked')).map(function (checkbox) {
	        return checkbox.getAttribute('data-id');
	    });


        Swal.fire({ 
            text: '선택한 회의실을 삭제하시겠습니까?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#e9ecef',
            confirmButtonText: '삭제',
            cancelButtonText: '취소'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    type: "POST",
                    url: "/meetingroomList/delete",
                    data: JSON.stringify(selectedIds),
                    contentType: 'application/json',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    success: function (response) {
                        if (response.res_code === "200") {
                            Swal.fire({
							    text: response.res_msg,
							    icon: 'success',
							    confirmButtonColor: '#0056b3',
							    confirmButtonText: '확인',
							}).then(() => {
                                location.reload();
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
                    error: function () {
                        Swal.fire('서버 오류', '서버에서 오류가 발생했습니다.', 'error');
                    }
                });
            }
        });
    });
    
    const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '회의실 관리&emsp;&gt;&emsp;회의실 정보';
});