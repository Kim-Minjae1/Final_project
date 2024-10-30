$(function () {
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '문서함&emsp;&gt;&emsp;휴지통';	
    // memberNo 받아오기 
    var memberNo = document.getElementById("mem_no").textContent;
    const csrfToken = $('input[name="_csrf"]').val();
    
    // 페이지당 리스트 10개, 페이징 요소들 넣을 영역 지정 
    const pageSize = 10; 
    const paginationDiv = document.getElementById('pagination');
    let totalPages = 0;
    let currentPage = 0;
    
    $('#select_delete').prop('disabled', true);
    $('#update_button').prop('disabled', true);
    
	// 체크박스 상태 변경 이벤트
    $(document).on('change', '.file_checkbox', function() {
        const checkedFiles = $('.file_checkbox:checked').length > 0;
        $('#select_delete').prop('disabled', !checkedFiles);
        $('#update_button').prop('disabled', !checkedFiles);
    });    
    
    // 날짜 포맷 함수
    function formatDate(dateString) {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = ('0' + (date.getMonth() + 1)).slice(-2);
        const day = ('0' + date.getDate()).slice(-2);
        return `${year}-${month}-${day}`;
    }   
    // 휴지통 파일 목록을 불러오기
    function loadFiles(searchInput = '') {
        $.ajax({
            type: 'GET',
            url: '/file/bin',
            data: { memberNo: memberNo },
            dataType: 'json',
            success: function(data) {
                // 정렬 기준 가져오기
                const sortOption = $('#sort_select').val();
                const fileList = data;

                // 날짜&검색 필터링
                const startDate = new Date(startDateInput.value);
                startDate.setHours(0, 0, 0, 0);
                const endDate = new Date(endDateInput.value);
                endDate.setHours(23, 59, 59, 999); 

                const filteredFiles = fileList.filter(file => {
                    const fileDate = new Date(file.document_file_update_date);
                    const isDateInRange = fileDate >= startDate && fileDate <= endDate;
                    const normalizedFileName = file.document_ori_file_name.normalize('NFC');
                    const normalizedSearchInput = searchInput.trim().normalize('NFC');

                    // 검색어가 파일 이름에 포함되는지 체크
                    const isSearchMatch = normalizedFileName.includes(normalizedSearchInput);
                    return isDateInRange && isSearchMatch;                
                });

                // 정렬
                if (sortOption === 'latest') {
                    filteredFiles.sort((a, b) => new Date(b.document_file_update_date) - new Date(a.document_file_update_date));
                } else if (sortOption === 'oldest') {
                    filteredFiles.sort((a, b) => new Date(a.document_file_update_date) - new Date(b.document_file_update_date));
                }

                // 몇 페이지인지 계산 
                totalPages = Math.ceil(filteredFiles.length / pageSize);
                const fileTableBody = document.getElementById('file_table_body');
                fileTableBody.innerHTML = '';
	            // 체크박스 초기화 
				$('#select_all').off('change');
				$('#select_all').prop('checked', false);
                // 파일 목록이 존재할 때
                if (filteredFiles.length > 0) {
                    $('.document_file_list').show();                
                    // 체크박스 활성화 
	                $('#select_all').prop('disabled', false).prop('checked', false);                 
                    // 한 페이지에 10개씩 추가 
                    const start = currentPage * pageSize;
                    const end = Math.min(start + pageSize, filteredFiles.length);
                    const paginatedFiles = filteredFiles.slice(start, end);

                    paginatedFiles.forEach(file => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td><input type="checkbox" class="file_checkbox"></td>
                            <td>${file.document_ori_file_name}</td>
                            <td> 
                            	${file.document_box_type === 0 ? '개인 문서함' : 
              					file.document_box_type === 1 ? '부서 문서함' : 
              					file.document_box_type === 2 ? '사내 문서함' : ''}			   
                            </td>
                            <td>${formatDate(file.document_file_update_date)}</td>
                            <td>${file.document_file_size}</td>
                            <td>
                            <svg class="file_update_button" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512">
                            <path d="M163.8 0L284.2 0c12.1 0 23.2 6.8 28.6 17.7L320 32l96 0c17.7 
                            0 32 14.3 32 32s-14.3 32-32 32L32 96C14.3 96 0 81.7 0 64S14.3 32 32 
                            32l96 0 7.2-14.3C140.6 6.8 151.7 0 163.8 0zM32 128l384 0 0 320c0 35.3-28.7 
                            64-64 64L96 512c-35.3 0-64-28.7-64-64l0-320zm192 64c-6.4 0-12.5 2.5-17 
                            7l-80 80c-9.4 9.4-9.4 24.6 0 33.9s24.6 9.4 33.9 0l39-39L200 408c0 13.3 10.7 
                            24 24 24s24-10.7 24-24l0-134.1 39 39c9.4 9.4 24.6 9.4 33.9 0s9.4-24.6 
                            0-33.9l-80-80c-4.5-4.5-10.6-7-17-7z"/></svg>
                            </td>
                            <td>
                            <svg class="delete_button" id="${file.document_file_no} "xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512">
                            <path d="M290.7 57.4L57.4 290.7c-25 25-25 65.5 0 90.5l80 80c12 12 28.3 
                            18.7 45.3 18.7L288 480l9.4 0L512 480c17.7 0 32-14.3 32-32s-14.3-32-32-32l-124.1 
                            0L518.6 285.3c25-25 25-65.5 0-90.5L381.3 57.4c-25-25-65.5-25-90.5 0zM297.4 416l-9.4 
                            0-105.4 0-80-80L227.3 211.3 364.7 348.7 297.4 416z"/></svg>
                            </td>
                        `;
                        fileTableBody.appendChild(row);
                    });

                    // 페이징 업데이트 
                    updatePagination();
                } else {
                    $('.document_file_list').show();
                    fileTableBody.innerHTML = '<tr><td colspan="7">조회된 목록이 없습니다.</td></tr>';
                    // 페이징 버튼 숨기기
                    paginationDiv.innerHTML = '';
 	                // 체크박스 비활성화 
	                $('#select_all').prop('disabled', true);                   
                }
                // 파일 영구 삭제 
				$('.delete_button').on('click', function() {
	                const fileNo = this.id;
	                deleteFile(fileNo);
	            });
                // th 체크박스 클릭하면 전부 선택
                $('#select_all').on('change', function() {
                    const isChecked = this.checked; 
                    $('.file_checkbox').prop('checked', isChecked); 
	                $('#select_delete').prop('disabled', !isChecked);
	                $('#update_button').prop('disabled', !isChecked);                    
                });
                // 하위 체크박스 클릭 시 th 체크박스 상태 변경
				$('.file_checkbox').on('change', function() {
				    const allChecked = $('.file_checkbox').length === $('.file_checkbox:checked').length;
				    $('#select_all').prop('checked', allChecked);
				    $('#select_delete').prop('disabled', $('.file_checkbox:checked').length === 0);
				    $('#update_button').prop('disabled', $('.file_checkbox:checked').length === 0);
				});
 	            // 파일 선택 삭제
	            $('#select_delete').on('click', function() {
	                const selectedFileNos = []; 
	                
	                // 체크된 파일들의 fileNo 가져오기 
	                $('.file_checkbox:checked').each(function() {
	                    const fileNo = $(this).closest('tr').find('.delete_button').attr('id');
	                    selectedFileNos.push(fileNo); 
	                });
	                if (selectedFileNos.length > 0) {
						$('#select_delete').prop('disabled', false);
	                    deleteSelectedFile(selectedFileNos);
	                } else {
	                    $('#select_delete').prop('disabled', true);
	                }
	            });      
	            // 파일 복구 
				$('.file_update_button').on('click', function() {
	                const fileNo = $(this).closest('tr').find('.delete_button').attr('id');
	                updateFile(fileNo);
	            });     
	            // 파일 선택 복구
	            $('#update_button').on('click', function() {
	                const selectedFileNos = []; 
	                
	                // 체크된 파일들의 fileNo 가져오기 
	                $('.file_checkbox:checked').each(function() {
	                    const fileNo = $(this).closest('tr').find('.delete_button').attr('id');
	                    selectedFileNos.push(fileNo); 
	                });
	                if (selectedFileNos.length > 0) {
						$('#update_button').prop('disabled', false);
	                    updateSelectedFile(selectedFileNos);
	                } else {
	                    $('#update_button').prop('disabled', true);
	                }
	            }); 
            }
        });
    }

    // 페이징 버튼 업데이트 
    function updatePagination() {
        paginationDiv.innerHTML = '';

        // 총 페이지 수가 1일 때
        if (totalPages <= 1) {
            const pageButton = document.createElement('span');
            pageButton.className = 'pagination_button active';
            pageButton.textContent = '1';
            paginationDiv.appendChild(pageButton);
            return;
        }

        // 처음 페이지 버튼 (<<)
        if (currentPage > 0) {
            const firstButton = document.createElement('span');
            firstButton.className = 'go_first_page_button';
            firstButton.textContent = '<<';
            firstButton.onclick = () => {
                currentPage = 0;
                loadFiles();
            };
            paginationDiv.appendChild(firstButton);
        }

        // 이전 페이지 버튼 (<)
        if (currentPage > 0) {
            const prevButton = document.createElement('span');
            prevButton.className = 'pagination_button';
            prevButton.textContent = '<';
            prevButton.onclick = () => {
                currentPage--;
                loadFiles();
            };
            paginationDiv.appendChild(prevButton);
        }

        // 페이지 번호 버튼 (최대 3개 표시)
        let startPage = Math.max(0, currentPage - 1);
        let endPage = Math.min(totalPages - 1, currentPage + 1);

        for (let page = startPage; page <= endPage; page++) {
            const pageButton = document.createElement('span');
            pageButton.className = `pagination_button ${page === currentPage ? 'active' : ''}`;
            pageButton.textContent = page + 1;
            pageButton.onclick = () => {
                currentPage = page;
                loadFiles();
            };
            paginationDiv.appendChild(pageButton);
        }

        // 다음 페이지 버튼 (>)
        if (currentPage < totalPages - 1) {
            const nextButton = document.createElement('span');
            nextButton.className = 'pagination_button';
            nextButton.textContent = '>';
            nextButton.onclick = () => {
                currentPage++;
                loadFiles();
            };
            paginationDiv.appendChild(nextButton);
        }

        // 마지막 페이지 버튼 (>>)
        if (currentPage < totalPages - 1) {
            const lastButton = document.createElement('span');
            lastButton.className = 'go_last_page_button';
            lastButton.textContent = '>>';
            lastButton.onclick = () => {
                currentPage = totalPages - 1;
                loadFiles();
            };
            paginationDiv.appendChild(lastButton);
        }
    }

	// 파일 영구 삭제
	function deleteFile(fileNo){
		Swal.fire({
			icon: 'warning',
		    text: '파일을 영구 삭제하시겠습니까?',
		    showCancelButton: true,
		    confirmButtonColor: '#dc3545',
		    confirmButtonText: '삭제',
		    cancelButtonText: '취소'
		}).then((result) => {
			if (result.isConfirmed) {
				$.ajax({
					type: 'POST',
					url: '/document/file/permanent/delete',
					data: {
						fileNo: fileNo
					},
					headers: {
	                    'X-Requested-With': 'XMLHttpRequest',
	                    'X-CSRF-TOKEN': csrfToken
	                },
					success: function(response){
						if (response.res_code === '200') {
			                    Swal.fire({
			                        icon: 'success',
			                        text: response.res_msg,
			                        confirmButtonText: '확인'
			                    });
			            	loadFiles();
	                    } else {
	                        Swal.fire({
		                        icon: 'error',
		                        text: response.res_msg,
		                        confirmButtonText: '확인'
		                    });
	                    }
	                     $('#file_name_input').val(''); 
					}
				});
			}
		})
	}
	// 파일 선택 영구 삭제
	function deleteSelectedFile(fileNos) {
	    Swal.fire({
	        icon: 'warning',
	        text: '파일을 영구 삭제하시겠습니까?',
	        showCancelButton: true,
	        confirmButtonColor: '#dc3545',
	        confirmButtonText: '삭제',
	        cancelButtonText: '취소'
	    }).then((result) => {
	        if (result.isConfirmed) {
	            $.ajax({
	                type: 'POST',
	                url: '/document/fileList/permanent/delete',  
	                contentType: 'application/json',
	                data: JSON.stringify({ 
						fileNos: fileNos 
					}), 
	                headers: {
	                    'X-Requested-With': 'XMLHttpRequest',
	                    'X-CSRF-TOKEN': csrfToken
	                },
	                success: function(response) {
	                    if (response.res_code === '200') {
	                        Swal.fire({
	                            icon: 'success',
	                            text: response.res_msg,
	                            confirmButtonText: '확인'
	                        });
	                        loadFiles();
	                        $('#select_delete').prop('disabled', true);
    						$('#update_button').prop('disabled', true);
	                    } else {
	                        Swal.fire({
	                            icon: 'error',
	                            text: response.res_msg,
	                            confirmButtonText: '확인'
	                        });
	                    }
	                     $('#file_name_input').val(''); 
	                }
	            });
	        }
	    });
	}	
	// 파일 복구 
	function updateFile(fileNo){
		$.ajax({
			type: 'POST',
			url: '/document/file/update',
			data: JSON.stringify({ fileNo: fileNo }),
			contentType: 'application/json',
			headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-TOKEN': csrfToken
            },
			success: function(response){
				if (response.res_code === '200') {
	                    Swal.fire({
	                        icon: 'success',
	                        text: response.res_msg,
	                        confirmButtonText: '확인'
	                    });
	            	loadFiles();
                } else {
                    Swal.fire({
                        icon: 'error',
                        text: response.res_msg,
                        confirmButtonText: '확인'
                    });
                }
                 $('#file_name_input').val(''); 
			}
		});
	}	
	// 파일 선택 복구 
	function updateSelectedFile(selectedFileNos){
		$.ajax({
			type: 'POST',
			url: '/document/fileList/update',
			data: JSON.stringify({ fileNos: selectedFileNos }),
			contentType: 'application/json',
			headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-TOKEN': csrfToken
            },
			success: function(response){
				if (response.res_code === '200') {
					if(response.res_status === "0"){					
	                    Swal.fire({
	                        icon: 'success',
	                        text: response.res_msg,
	                        confirmButtonText: '확인'
	                    });
	            	loadFiles();
					} else{
						Swal.fire({
	                        icon: 'success',
	                        html: "복구할 수 있는 폴더가 존재하지 않는 파일을 제외한<br>모든 파일 복구가 완료되었습니다.",
	                        confirmButtonText: '확인'
	                    });
	            	loadFiles();
					}
                } else {
                    Swal.fire({
                        icon: 'error',
                        text: response.res_msg,
                        confirmButtonText: '확인'
                    });
                }
                $('#select_delete').prop('disabled', true);
    			$('#update_button').prop('disabled', true);
                $('#file_name_input').val(''); 
			}
		});	
	}
	// 날짜 검색 
	var today = new Date();
	const todayStr = formatDate(today);	
	// 1년 전 날짜 계산
	const oneYearAgo = new Date();
	oneYearAgo.setFullYear(today.getFullYear() - 1);
	const oneYearAgoStr = formatDate(oneYearAgo);
	const startDateInput = document.getElementById('start_date');
    const endDateInput = document.getElementById('end_date');
    // 시작 날짜를 끝나는 날보다 나중 날짜로 설정 못하게 하는 함수 
    function startDateLimit() {
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);
        // endDate가 startDate보다 이전일 때 startDate를 endDate와 같게 설정
	    if (endDate < startDate) {
	        startDateInput.value = formatDate(endDate);
	    }	    
	    // startDate의 최대값을 endDate로 설정
	    startDateInput.max = formatDate(endDate);
    }
    // startDate의 기본값을 오늘로부터 1년 전으로 설정
	startDateInput.value = oneYearAgoStr;
    // endDate의 기본 값을 오늘로 설정 
    endDateInput.value = todayStr;
	
	// startDate와 endDate를 오늘 이후의 날짜를 설정할 수 없게 설정 
    startDateInput.max = todayStr;
    endDateInput.max = todayStr;

    // 삭제 버튼 클릭 시 자동으로 초기 지정 날짜로 설정
    startDateInput.addEventListener('input', function() {
        if (!this.value) {
            this.value = oneYearAgoStr;
        }
    });
    endDateInput.addEventListener('input', function() {
        if (!this.value) {
            this.value = todayStr; 
        }
    });
	// 검색 입력 시 파일 목록을 다시 로드
    $('#file_name_input').on('keyup', function() {
        searchInputValue = $(this).val(); 
        loadFiles(searchInputValue);

    });        
    // 페이지가 로드될 때 파일 목록을 불러옴
    $(document).ready(function() {
        loadFiles(); // 페이지 로드 시 파일 목록 로드

        // 정렬 선택이 변경될 때 파일 목록을 다시 불러옴
        $('#sort_select').on('change', function() {
			const searchInput = $('#file_name_input').val();
			loadFiles(searchInput);
            loadFiles(searchInputValue);
        });

        // 날짜가 변경될 때 파일 목록을 새로 로드
        startDateInput.addEventListener('change', function() {
            startDateLimit();
            const searchInput = $('#file_name_input').val();
			loadFiles(searchInput);
            loadFiles(searchInputValue);
        });

        endDateInput.addEventListener('change', function() {
			startDateLimit();
			const searchInput = $('#file_name_input').val();
			loadFiles(searchInput);
            loadFiles(searchInputValue);
        });
    });  
});
