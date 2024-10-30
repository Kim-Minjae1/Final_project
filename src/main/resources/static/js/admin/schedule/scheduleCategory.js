document.addEventListener('DOMContentLoaded', function() {
	var csrfToken = document.querySelector('input[name="_csrf"]').value;
    const editButtons = document.querySelectorAll('.editButton');
    var createButtons = document.getElementById('openModal');
    var createModal = document.getElementById("createModal");
    const deleteButtons = document.querySelectorAll('.deleteButton');  
    var createForm = document.getElementById("createForm");

	// 수정 모달
    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            var categoryId = parseInt(this.getAttribute('data-category-id'), 10); 

            $.ajax({
                type: 'GET',
                url: `/schedule/category/get/${categoryId}`, 
                headers: {
	                'X-CSRF-TOKEN': csrfToken
	            },
                success: function (response) {  
                    document.getElementById("editScheduleCategoryId").value = response.schedule_category_no;
                    document.getElementById("editScheduleCategoryName").value = response.schedule_category_name;
                    document.getElementById("editScheduleCategoryColor").value = `#${response.schedule_category_color}`;
                    document.getElementById("editOnlyAdmin").checked = response.schedule_category_admin == 1;

                    document.getElementById('editModal').style.display = "block"; 
                }
            });
        });
    });
 
 	// 수정 모달 닫기
    document.querySelector('.close').addEventListener('click', function() {
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
	                document.getElementById('editModal').style.display = 'none';  
	            }
	        }); 
    });
    
    // 등록 모달 닫기
    document.querySelector('.create_close').addEventListener('click', function() {
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
	                document.getElementById('createModal').style.display = 'none'; 
	                createForm.reset();
	            }
	        }); 
    });
    
    // 수정 저장
    document.getElementById('editForm').addEventListener('submit', function(event) {
	    event.preventDefault(); 
	
	    var formData = {
	        scheduleCategoryId: document.getElementById("editScheduleCategoryId").value,
	        scheduleCategoryName: document.getElementById("editScheduleCategoryName").value,
	        scheduleCategoryColor: document.getElementById("editScheduleCategoryColor").value.slice(1),  
	        onlyAdmin: document.getElementById("editOnlyAdmin").checked
	    };
	    
	    var scheduleCategoryName = document.getElementById("editScheduleCategoryName").value.trim();
	    
	    if (!scheduleCategoryName || scheduleCategoryName.length == 0) {
            Swal.fire({
			    text: '카테고리명을 입력해 주세요.',
			    icon: 'warning',
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: '확인',
			});
            return;
        }

	    $.ajax({
	        type: 'POST',
	        url: '/schedule/category/update',  
	        contentType: 'application/json',
	        data: JSON.stringify(formData),
	        headers: {
	            'X-CSRF-TOKEN': csrfToken
	        },
	        success: function (response) {
			        if (response.res_code === "200") {
			            Swal.fire({ 
			                text: response.success,
			                icon: 'success', 
			                confirmButtonColor: '#0056b3', 
			                confirmButtonText: '확인', 
			            }).then(() => {
			                location.reload();
			            });
			        } else {
			            let errorMsg = "";
			            if (response.name) {
			                errorMsg += response.name;  
			            } else if (response.color) {
			                errorMsg += response.color; 
			            }
			            Swal.fire({ 
						    text: errorMsg,
						    icon: 'warning', 
						    confirmButtonColor: '#0056b3', 
						    confirmButtonText: '확인', 
						});
			        }
			    },
			    error: function () {
			        Swal.fire('서버 오류', '서버와의 통신에 문제가 발생했습니다.', 'error');
			    } 
	    });
	});
	
	// 삭제
	deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            var categoryId = parseInt(this.getAttribute('data-category-id'), 10);  
            
            Swal.fire({
                text: '카테고리를 삭제하시겠습니까?',
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
                        url: '/schedule/category/delete',   
                        contentType: 'application/json',
                        data: JSON.stringify({ scheduleCategoryId: categoryId }),  
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
                                     location.href = "/schedule/category";  
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
        });
    });
    
	// 등록 모달
    if (createButtons) {
        createButtons.onclick = function () {
            createModal.style.display = "block";
        };
    }
    
    // 등록
    if (createForm) {
	    createForm.onsubmit = function (event) {
	        event.preventDefault();
	        var scheduleCategoryName = document.getElementById("createScheduleCategoryName").value.trim();
	        var scheduleCategoryColor = document.getElementById("createScheduleCategoryColor").value.slice(1);
	        var onlyAdmin = document.getElementById("createOnlyAdmin").checked; 

	        if (!scheduleCategoryName || scheduleCategoryName.length == 0) {
	            Swal.fire({
				    text: '카테고리명을 입력해 주세요.',
				    icon: 'warning',
				    confirmButtonColor: '#0056b3',
				    confirmButtonText: '확인',
				});
	            return;
	        }
        
	        $.ajax({
			    type: "POST",
			    url: "/schedule/category/add",
			    contentType: "application/json",
			    data: JSON.stringify({
			        scheduleCategoryName: scheduleCategoryName,
			        scheduleCategoryColor: scheduleCategoryColor,
			        onlyAdmin: onlyAdmin
			    }),
			    headers: {
			        'X-CSRF-TOKEN': csrfToken
			    },
			    success: function (response) {
			        if (response.res_code === "200") {
			            Swal.fire({ 
			                text: response.success,
			                icon: 'success', 
			                confirmButtonColor: '#0056b3', 
			                confirmButtonText: '확인', 
			            }).then(() => {
			                location.reload();
			            });
			        } else {
			            let errorMsg = "";
			            if (response.name) {
			                errorMsg += response.name;  
			            } else if (response.color) {
			                errorMsg += response.color; 
			            }
			            Swal.fire({ 
						    text: errorMsg,
						    icon: 'warning', 
						    confirmButtonColor: '#0056b3', 
						    confirmButtonText: '확인', 
						});
			        }
			    },
			    error: function () {
			        Swal.fire('서버 오류', '서버와의 통신에 문제가 발생했습니다.', 'error');
			    }
			}); 
	    };
	}
	
	const urlParams = new URLSearchParams(window.location.search);
    const selectedCategoryId = urlParams.get('id');
 
    if (selectedCategoryId) {
        const selectedCategoryElement = document.querySelector(`li[data-category-id='${selectedCategoryId}']`);
        if (selectedCategoryElement) {
            selectedCategoryElement.classList.add('selected');
        }
    } else { 
        const firstCategoryElement = document.querySelector('li[data-category-id]');
        if (firstCategoryElement) {
            firstCategoryElement.classList.add('selected');
        }
    }
    
    document.getElementById("createScheduleCategoryColor").addEventListener('input', function() {
	    document.getElementById('colorCode').textContent = this.value; 
	});
	
    document.getElementById("editScheduleCategoryColor").addEventListener('input', function() {
	    document.getElementById('edit_colorCode').textContent = this.value; 
	});
	
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '일정 관리&emsp;&gt;&emsp;카테고리 관리'; 
});
