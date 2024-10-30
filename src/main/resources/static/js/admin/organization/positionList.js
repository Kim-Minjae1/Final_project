document.addEventListener("DOMContentLoaded", function () {
    var createModal = document.getElementById("createModal");
    var editModal = document.getElementById("editModal");
    var openModalBtn = document.getElementById("openModal");
    var closeButtons = document.getElementsByClassName("close");
    var createForm = document.getElementById("positionForm");
    var editForm = document.getElementById("editForm");
    var csrfToken = document.querySelector('input[name="_csrf"]').value;

    if (openModalBtn) {
        openModalBtn.onclick = function () {
            createModal.style.display = "block";
        };
    }
    
    Array.from(closeButtons).forEach(function (closeButton) {
        closeButton.onclick = closeModal;
    });

    // 수정
    document.querySelectorAll("button#editButton").forEach(function (editButton) {
        editButton.onclick = function () {
            var positionId = this.getAttribute("data-position-id");

            $.ajax({
                type: "GET",
                url: `/position/get?id=${positionId}`,
                success: function (response) {
                    if (response.res_code === "200") {
                        const position = response.position;
                        document.getElementById("editPositionId").value = position.position_no;
                        document.getElementById("editPositionName").value = position.position_name;
                        document.getElementById("editPositionHigh").value = position.position_high;

                        editModal.style.display = "block";
                    } else {
                        Swal.fire("오류", response.res_msg, "error");
                    }
                },
                error: function () {
                    Swal.fire("오류", "서버 요청 중 오류가 발생했습니다.", "error");
                }
            });
        };
    }); 

    // 삭제
    document.querySelectorAll("button#deleteButton").forEach(function (deleteButton) {
        deleteButton.onclick = function () {
            var positionId = this.getAttribute("data-position-id");

            Swal.fire({ 
                text: '직위를 삭제하시겠습니까?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#dc3545',
                cancelButtonColor: '#f8f9fa',
                confirmButtonText: '삭제',
                cancelButtonText: '취소'
            }).then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        type: "POST",
                        url: "/position/delete",
                        data: { id: positionId },
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
                                    location.href = "/position"; 
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
                            Swal.fire('서버 오류', "서버 요청 중 오류가 발생했습니다.", 'error');
                        }
                    });
                }
            });
        };
    }); 

    function closeModal() {
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
		        createModal.style.display = "none";
		        editModal.style.display = "none";
		        resetForm(createForm);
		        resetForm(editForm);
            }
        });   
    }

    function resetForm(form) {
        form.reset();
    }

    // 등록
    if (createForm) {
        createForm.onsubmit = function (event) {
            event.preventDefault();
            var positionName = document.getElementById("positionName").value.trim(); 
            var positionHigh = document.getElementById("positionHigh").value;
  
		    if (!positionName || positionName.length == 0) {
	            Swal.fire({
				    text: '직위명을 입력해 주세요.',
				    icon: 'warning',
				    confirmButtonColor: '#0056b3',
				    confirmButtonText: '확인',
				});
	            return;
	        }
        
            $.ajax({
                type: "POST",
                url: "/position/add",
                contentType: "application/json",
                data: JSON.stringify({
                    positionName: positionName,
                    positionHigh: positionHigh
                }),
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
						    icon: 'warning', 
						    confirmButtonColor: '#0056b3', 
						    confirmButtonText: '확인', 
						});
                    }
                },
                error: function () {
                    Swal.fire('서버 오류', "서버 요청 중 오류가 발생했습니다.", 'error');
                }
            });
        };
    }

    // 수정
    if (editForm) {
        editForm.onsubmit = function (event) {
            event.preventDefault();

            var positionId = document.getElementById("editPositionId").value;
            var positionName = document.getElementById("editPositionName").value;
            var positionHigh = document.getElementById("editPositionHigh").value;
			 
            $.ajax({
                type: "POST",
                url: "/position/update",
                contentType: "application/json",
                data: JSON.stringify({
                    positionId: positionId,
                    positionName: positionName,
                    positionHigh: positionHigh
                }),
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
						    icon: 'warning', 
						    confirmButtonColor: '#0056b3', 
						    confirmButtonText: '확인', 
						});
                    }
                },
            });
        };
    } 
    
    const positionLinks = document.querySelectorAll('.position_li a');  
    const currentUrl = window.location.href;  
 
    positionLinks.forEach(link => {
        if (currentUrl.includes(link.getAttribute('href'))) {
            link.classList.add('selected');
        }
    });
 
    positionLinks.forEach(link => {
        link.addEventListener('click', function(event) { 
            positionLinks.forEach(l => l.classList.remove('selected')); 
            link.classList.add('selected');
        });
    });
    
    const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '조직 관리&emsp;&gt;&emsp;직위 관리';
});
