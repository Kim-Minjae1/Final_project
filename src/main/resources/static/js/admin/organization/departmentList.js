document.addEventListener("DOMContentLoaded", function () {
    var createModal = document.getElementById("createModal");
    var editModal = document.getElementById("editModal");
    var openModalBtn = document.getElementById("openModal");
    var closeButtons = document.getElementsByClassName("close");
    var createForm = document.getElementById("departmentForm");
    var editForm = document.getElementById("editForm");
    var csrfToken = document.querySelector('input[name="_csrf"]').value; 

    if (openModalBtn) {
        openModalBtn.onclick = function () {
            createModal.style.display = "block";
        };
    }

	// 수정
    document.querySelectorAll("#editButton").forEach(function (editButton) {
        editButton.onclick = function () {
           var departmentId = this.getAttribute("data-department-id");

           $.ajax({
                type: "GET",
                url: `/department/get?id=${departmentId}`,
                success: function (response) {
                    if (response.res_code === "200") {
                        const department = response.department;
                        document.getElementById("editDepartmentId").value = department.department_no;
                        document.getElementById("editDepartmentName").value = department.department_name;
                        document.getElementById("editDepartmentHigh").value = department.department_high;

                        document.getElementById("editDepartmentHigh").disabled = department.subDepartments && department.subDepartments.length > 0;

                        editModal.style.display = "block";
                    } else {
                        Swal.fire("오류", response.res_msg, "error");
                    }
                },
                error: function () {
                    Swal.fire("오류", response.res_msg, "error");
                }
            });
        };
    });

	// 삭제
    document.querySelectorAll("#deleteButton").forEach(function (deleteButton) {
        deleteButton.onclick = function () {
            var departmentId = this.getAttribute("data-department-id");

            Swal.fire({ 
                html: '부서를 삭제하시겠습니까?<br/>상위 부서 삭제 시 하위 부서가 함께 삭제됩니다.',
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
                        url: "/department/delete",
                        data: { id: departmentId },
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
                                    location.href = "/department"; 
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
                            Swal.fire('서버 오류', response.res_msg, 'error');
                        }
                    });
                }
            });
        };
    });

    Array.from(closeButtons).forEach(function (closeButton) {
        closeButton.onclick = closeModal;
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
        if (form.id === 'editForm') {
            document.getElementById("editDepartmentHigh").disabled = false; 
        }
    }

    // 등록
    if (createForm) {
        createForm.onsubmit = function (event) {
            event.preventDefault();
            var departmentName = document.getElementById("departmentName").value.trim();
            var departmentHigh = document.getElementById("departmentHigh").value; 

		    if (!departmentName || departmentName.length == 0) {
	            Swal.fire({
				    text: '부서명을 입력해 주세요.',
				    icon: 'warning',
				    confirmButtonColor: '#0056b3',
				    confirmButtonText: '확인',
				});
	            return;
	        }
	        
            $.ajax({
                type: "POST",
                url: "/department/add",
                contentType: "application/json",
                data: JSON.stringify({
                    departmentName: departmentName,
                    departmentHigh: departmentHigh
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
						    icon: 'error', 
						    confirmButtonColor: '#0056b3', 
						    confirmButtonText: '확인', 
						}); 
                    }
                },
                error: function () {
                    Swal.fire('서버 오류', response.res_msg, 'error');
                }
            });
        };
    }

    // 수정
    if (editForm) {
        editForm.onsubmit = function (event) {
            event.preventDefault();

            var departmentId = document.getElementById("editDepartmentId").value;
            var departmentName = document.getElementById("editDepartmentName").value.trim();
            var departmentHigh = document.getElementById("editDepartmentHigh").value;
            var departmentHighInt = parseInt(departmentHigh, 10);
 
		   if (!departmentName || departmentName.length == 0) {
	            Swal.fire({
				    text: '부서명을 입력해 주세요.',
				    icon: 'warning',
				    confirmButtonColor: '#0056b3',
				    confirmButtonText: '확인',
				});
	            return;
	        }
	        
            $.ajax({
                type: "POST",
                url: "/department/update",
                contentType: "application/json",
                data: JSON.stringify({
                    departmentId: departmentId,
                    departmentName: departmentName,
                    departmentHigh: departmentHighInt
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
						    icon: 'error', 
						    confirmButtonColor: '#0056b3', 
						    confirmButtonText: '확인', 
						});
                    }
                },
                error: function () {
                    Swal.fire("서버 오류", response.res_msg, "error");
                }
            });
        };
    }
    
    const departmentLinks = document.querySelectorAll('.department_li > a');
 
    function getDepartmentIdFromURL() {
        const params = new URLSearchParams(window.location.search);
        return params.get('id');
    }
 
    function clearSelectedClass() {
        departmentLinks.forEach(link => {
            link.classList.remove('selected');
        });
    }
 
    departmentLinks.forEach(link => {
        link.addEventListener('click', function () {
            clearSelectedClass();   
            this.classList.add('selected');   
        });
    });
 
    const selectedDepartmentId = getDepartmentIdFromURL();
    if (selectedDepartmentId) {
        const selectedLink = document.querySelector(`.department_li > a[href*="id=${selectedDepartmentId}"]`);
        if (selectedLink) {
            selectedLink.classList.add('selected');
        }
    }
    
    const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '조직 관리&emsp;&gt;&emsp;부서 관리';
    
});
 
