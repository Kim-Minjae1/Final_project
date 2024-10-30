// 조회한 폴더리스트를 담을 배열 
let folderList = [];

$(function () {
	// 알림
    window.functionType = 2;
    if (window.functionType === 2) {
        markNotificationsAsRead(window.functionType);
    }
	 
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '문서함&emsp;&gt;&emsp;부서 문서함';	
	// 전역 변수로 selectedFolderNo 정의
	let selectedFolderNo = null;
	// 폴더 이름 변경 여부 
	let isFolderNameChange = false;
	// 폴더 생성 여부 
	let folderCreated = 0;	
    // memberNo 받아오기 
    var memberNo = document.getElementById("mem_no").textContent;
    var deptNo = document.getElementById("dept_no").textContent;
	const csrfToken = $('input[name="_csrf"]').val();
	
    // 페이지당 리스트 10개, 페이징 요소들 넣을 영역 지정 
    const pageSize = 10; 
    const paginationDiv = document.getElementById('pagination');
    let totalPages = 0;
    let currentPage = 0;

    $('#select_delete').prop('disabled', true);
    $('#select_down').prop('disabled', true);

	// 체크박스 상태 변경 이벤트
    $(document).on('change', '.file_checkbox', function() {
        const checkedFiles = $('.file_checkbox:checked').length > 0;
        $('#select_delete').prop('disabled', !checkedFiles);
        $('#select_down').prop('disabled', !checkedFiles);
    });
    
    // 날짜 포맷 함수
    function formatDate(dateString) {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = ('0' + (date.getMonth() + 1)).slice(-2);
        const day = ('0' + date.getDate()).slice(-2);
        return `${year}-${month}-${day}`;
    }
    // 폴더 리스트 받아오기
    function getFolders() {
		return new Promise((resolve, reject) => {
	        $.ajax({
	            type: 'GET',
	            url: '/department/folder',
	            data: { deptNo: deptNo },
	            dataType: 'json',
	            success: function(data) {
					// 폴더리스트 초기화 
	                folderList = []; 
	                $.each(data, function(idx, item) {
	                    folderList.push({
							// 폴더 번호, 부모폴더가 있는지, 폴더 이름을 배열에 담음 
	                        id: item.document_folder_no,
	                        parent: item.document_folder_parent_no ? item.document_folder_parent_no : '#',
	                        text: item.document_folder_name
	                    });
	                });
	
	                // 폴더 리스트가 존재할 때
	                if (folderList.length > 0) {
	                    $('#tree').show();
	                    $('#tree').jstree('destroy').empty();
	                    $('#tree').jstree({
	                        'core': {
	                            'data': folderList,
	                            'animation': 0
	                        },
	                        'themes': {
	                            'name': 'proton',
	                            'responsive': true,
	                            'dots': false
	                        },
	                        'types': {
	                            'default': {
	                                'icon': 'fa fa-folder'
	                            }
	                        },
	                        'plugins': ['wholerow', 'types']
	                    }).on('ready.jstree', function () {
	                        if (folderList.length > 0) {
								// 처음 폴더 로드 시 
								if(folderCreated == 0){
									selectedFolderNo = folderList[0].id;	
								// 폴더 생성 시 																			
								} else{
									// 다시 리셋 
									folderCreated = 0;
								}
								$('#tree').jstree('select_node', selectedFolderNo);
	                            if (isFolderNameChange) {
	                                openFolderToNode(selectedFolderNo);
	                                updateFolderName(selectedFolderNo);
	                                loadFiles(selectedFolderNo);
	                            }
	                        } else {
	                            $('.document_no_folder').show();
	                        }
	                        $('.document_no_folder').hide();	                       
	                        $('.folder_buttons').show();
	                        $('.box_size').show();
	                        $('.document_folder_tree').show();
	                        $('#document_hr').show();  
	                        resolve(); 
	                    });
	                    $('#tree').on('changed.jstree', function (e, data) {
							// 현재페이지를 1페이지로 리셋 
	                        currentPage = 0; 
	                        selectedFolderNo = data.node.id;
	                        // 폴더 이름 출력을 위한 함수 
	                        updateFolderName(selectedFolderNo); 
	                        // 폴더 안에 든 파일을 불러올 함수 
	                        loadFiles(selectedFolderNo);                  
	                    });         
	                } else {
						// 폴더가 없으면 폴더 생성 버튼 띄우기 
	                    $('#tree').hide();
	                    $('.document_no_folder').show();
	                    resolve();
	                }
	            },
	            error: function(err) {
	                reject(err); 
	            }
	    	});
        });
    }
    
    // 선택된 폴더까지 열린 상태로 있게 하기 
    function openFolderToNode(folderNo) {
        var node = $('#tree').jstree(true).get_node(folderNo);
        
        // 부모 폴더가 존재할 때까지 반복
        while (node && node.parent !== '#') {
            $('#tree').jstree('open_node', node);
            node = $('#tree').jstree(true).get_node(node.parent);
        }
    }
    
    // 모든 파일 사이즈 가져오기 
    function getAllFileSize(){
		$.ajax({
            type: 'GET',
            url: '/department/fileSize',
            data: { deptNo: deptNo },
            dataType: 'json',
            success: function(data) {
				const totalSize = 50;
				const currentSize = $('#current_size_text');
				const currentPercent = $('#print_size');	
				const sizeBar = $('#bar_foreground');
				if(data != null){
					currentSize.text('');	
					currentSize.text('50GB 중 ' + data + 'GB 사용');	
					currentPercent.text('');
					currentPercent.text('저장용량(' + (data/totalSize)*100 + '% 사용 중)');		
					sizeBar.css('width', (data/totalSize)*100+'%');							
				} else{
					currentSize.text('');	
					currentSize.text('50GB 중 0GB 사용');	
					currentPercent.text('');
					currentPercent.text('저장용량(0% 사용 중)');	
					sizeBar.css('width', '0%');									
				}
			}
		});
	}
 
    // 선택된 폴더의 이름을 출력 
    function updateFolderName(folderNo) {
        // folderList에서 폴더 이름을 찾아서 div에 추가 
        const folderName = folderList.find(f => f.id == folderNo);
        if (folderName) {
            const folderNameDiv = document.getElementById('folder_name');
            folderNameDiv.innerHTML = folderName.text; 
        }
    }

    // 선택된 폴더의 파일 목록을 불러오기
    function loadFiles(folderNo, searchInput = '', searchOption = 'search_all') {
        $.ajax({
            type: 'GET',
            url: '/folder/file',
            data: { folderNo: folderNo },
            dataType: 'json',
            success: function(data) {
                // 정렬 기준 가져오기
                const sortOption = $('#sort_select').val();
                const fileList = data;
                const searchOption = $('#name_select').val();

	            // 날짜&검색 필터링
	            const startDate = new Date(startDateInput.value);
				startDate.setHours(0, 0, 0, 0);
	            const endDate = new Date(endDateInput.value);
				endDate.setHours(23, 59, 59, 999); 
	            
	            const filteredFiles = fileList.filter(file => {
	                const fileDate = new Date(file.document_file_upload_date);
	                const isDateInRange = fileDate >= startDate && fileDate <= endDate;
	                const normalizedFileName = file.document_ori_file_name.normalize('NFC');
	                const allName = normalizedFileName+file.member_name.toLowerCase();
	                let isSearchMatch = false;
	                if(searchOption === 'search_all'){
						isSearchMatch = allName.includes(searchInput.toLowerCase().trim());
					} else if(searchOption === 'file_name'){
						isSearchMatch = normalizedFileName.includes(searchInput.toLowerCase().trim());					
					} else{
						isSearchMatch = file.member_name.toLowerCase().includes(searchInput.toLowerCase().trim());
					}
					
				    return isDateInRange && isSearchMatch;
	            });
	            
	            // 정렬
	            if (sortOption === 'latest') {
	                filteredFiles.sort((a, b) => new Date(b.document_file_upload_date) - new Date(a.document_file_upload_date));
	            } else if (sortOption === 'oldest') {
	                filteredFiles.sort((a, b) => new Date(a.document_file_upload_date) - new Date(b.document_file_upload_date));
	            }
                
                // 몇 페이지인지 계산 
                totalPages = Math.ceil(filteredFiles.length / pageSize);
                const fileTableBody = document.getElementById('file_table_body');
                fileTableBody.innerHTML = '';
	            // 체크박스 초기화 
				$('#select_all').off('change');
				$('#select_all').prop('checked', false);
                // 파일 목록이 존재할 때
                if (fileList.length > 0) {
                    $('.document_select_folder').hide();
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
                        	<td><input type="checkbox" class="file_checkbox" id="${file.member_no}"></td>
                            <td>${file.document_ori_file_name}</td>
                            <td>${file.member_no == memberNo ? '본인' : (file.member_name + ' ' + file.position_name)}</td>
                            <td>${formatDate(file.document_file_upload_date)}</td>
	                        <td>${file.document_ori_file_name.endsWith('.pdf') ? 
	                            `<a href="/document/file/view/${file.document_file_no}" target="_blank">
	                            <svg class="file_show_button" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
	                            <path d="M64 464l48 0 0 48-48 0c-35.3 0-64-28.7-64-64L0 64C0 28.7 28.7 0 
	                            64 0L229.5 0c17 0 33.3 6.7 45.3 18.7l90.5 90.5c12 12 18.7 28.3 18.7 45.3L384 304l-48 
	                            0 0-144-80 0c-17.7 0-32-14.3-32-32l0-80L64 48c-8.8 0-16 7.2-16 16l0 384c0 8.8 7.2 16 
	                            16 16zM176 352l32 0c30.9 0 56 25.1 56 56s-25.1 56-56 56l-16 0 0 32c0 8.8-7.2 16-16 
	                            16s-16-7.2-16-16l0-48 0-80c0-8.8 7.2-16 16-16zm32 80c13.3 0 24-10.7 
	                            24-24s-10.7-24-24-24l-16 0 0 48 16 0zm96-80l32 0c26.5 0 48 21.5 48 48l0 64c0 
	                            26.5-21.5 48-48 48l-32 0c-8.8 0-16-7.2-16-16l0-128c0-8.8 7.2-16 16-16zm32 128c8.8 0 
	                            16-7.2 16-16l0-64c0-8.8-7.2-16-16-16l-16 0 0 96 16 0zm80-112c0-8.8 7.2-16 16-16l48 
	                            0c8.8 0 16 7.2 16 16s-7.2 16-16 16l-32 0 0 32 32 0c8.8 0 16 7.2 16 16s-7.2 16-16 16l-32 0 
	                            0 48c0 8.8-7.2 16-16 16s-16-7.2-16-16l0-64 0-64z"/></svg></a>` 
	                            : ''}
	                        </td>
                            <td>${file.document_file_size}</td>
							<td>
							    <a href="/document/file/download/${file.document_file_no}">
							    <svg class="file_down_button" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
							    <path d="M288 32c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 
							    242.7-73.4-73.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l128 128c12.5 
							    12.5 32.8 12.5 45.3 0l128-128c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L288 
							    274.7 288 32zM64 352c-35.3 0-64 28.7-64 64l0 32c0 35.3 28.7 64 64 64l384 0c35.3 
							    0 64-28.7 64-64l0-32c0-35.3-28.7-64-64-64l-101.5 0-45.3 45.3c-25 25-65.5 25-90.5 
							    0L165.5 352 64 352zm368 56a24 24 0 1 1 0 48 24 24 0 1 1 0-48z"/></svg>
							    </a>
							</td>
                            <td>
					            ${file.member_no == memberNo ? 
					            `<svg class="delete_button" id="${file.document_file_no}"
	                            xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512">
	                            <path d="M135.2 17.7C140.6 6.8 151.7 0 163.8 0L284.2 0c12.1 0 23.2 6.8 28.6 
	                            17.7L320 32l96 0c17.7 0 32 14.3 32 32s-14.3 32-32 32L32 96C14.3 96 0 81.7 0 
	                            64S14.3 32 32 32l96 0 7.2-14.3zM32 128l384 0 0 320c0 35.3-28.7 64-64 64L96 
	                            512c-35.3 0-64-28.7-64-64l0-320zm96 64c-8.8 0-16 7.2-16 16l0 224c0 8.8 7.2 16 
	                            16 16s16-7.2 16-16l0-224c0-8.8-7.2-16-16-16zm96 0c-8.8 0-16 7.2-16 16l0 224c0 
	                            8.8 7.2 16 16 16s16-7.2 16-16l0-224c0-8.8-7.2-16-16-16zm96 0c-8.8 0-16 7.2-16 
	                            16l0 224c0 8.8 7.2 16 16 16s16-7.2 16-16l0-224c0-8.8-7.2-16-16-16z"/></svg>` : ''}
					        </td>
                        `;
                        fileTableBody.appendChild(row);
                    });

                    // 페이징 업데이트 
                    updatePagination();
                } else {
                    $('.document_file_list').show();
                    $('.document_select_folder').hide();
                    fileTableBody.innerHTML = '<tr><td colspan="8">조회된 목록이 없습니다.</td></tr>';

                    // 페이징 버튼 숨기기
                    paginationDiv.innerHTML = '';
	                // 체크박스 비활성화 
	                $('#select_all').prop('disabled', true);                   
                }
                // 파일 삭제
                 $('.delete_button').on('click', function() {
	                const fileNo = this.id;
	                deleteFile(fileNo);
	            });
	            // th 체크박스 클릭하면 전부 선택
	             $('#select_all').on('change', function() {
                	const isChecked = this.checked; 
                	$('.file_checkbox').prop('checked', isChecked); 
                	$('#select_delete').prop('disabled', !isChecked);
	                $('#select_down').prop('disabled', !isChecked);
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
				    const fileNos = []; 
				    // 삭제 가능 여부 
				    let canDeleteFiles = true; 
				
				    // 체크된 파일들의 fileNo 가져오기 
				    $('.file_checkbox:checked').each(function() {
				        const fileNo = $(this).closest('tr').find('.delete_button').attr('id');
				        const memberNoOfFile = this.id;
				
				        // 본인이 등록한 파일만 삭제 가능 
				        if (memberNoOfFile != memberNo) {
				            canDeleteFiles = false; 
				        } else {
				            fileNos.push(fileNo); 
				        }
				    });
				
				    if (!canDeleteFiles) {
				        Swal.fire({
				            icon: 'warning',
				            text: '본인이 등록한 파일만 삭제 가능합니다.',
				            confirmButtonText: '확인'
				        });
				    } else if (fileNos.length > 0) {
				        deleteSelectedFile(fileNos); 
				        $('#select_delete').prop('disabled', false);	
				    } else {
				        $('#select_delete').prop('disabled', true);	
				    }
				});
				// 파일 선택 다운
				$('#select_down').off('click').on('click', function() {
				    const selectedFileNos = [];				
				    $('.file_checkbox:checked').each(function() {
				        const fileNo = $(this).closest('tr').find('.delete_button').attr('id');
				        selectedFileNos.push(fileNo);
				    });
				    if (selectedFileNos.length > 0) {
						$('#update_button').prop('disabled', false);
				        selectedFileNos.forEach(fileNo => {
				            const downloadLink = document.createElement('a');
				            downloadLink.href = `/document/file/download/${fileNo}`;
				            downloadLink.download = '';
				            document.body.appendChild(downloadLink);
				            downloadLink.click();
				            document.body.removeChild(downloadLink);
				        });
				        $('.file_checkbox:checked').prop('checked', false);
				        $('#select_all:checked').prop('checked', false);
				        $('#select_delete').prop('disabled', true);
    					$('#select_down').prop('disabled', true);
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
                loadFiles($('#tree').jstree('get_selected')[0]); 
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
                loadFiles($('#tree').jstree('get_selected')[0]); 
            };
            paginationDiv.appendChild(prevButton);
        }

        // 페이지 번호 버튼 (최대 3개 표시)
        let startPage = Math.max(0, currentPage - 1);
        let endPage = Math.min(totalPages - 1, currentPage + 1);

        if (currentPage < 1) {
            endPage = Math.min(totalPages - 1, endPage + 1);
        } else if (currentPage > totalPages - 2) {
            startPage = Math.max(0, startPage - 1);
        }

        for (let page = startPage; page <= endPage; page++) {
            const pageButton = document.createElement('span');
            pageButton.className = `pagination_button ${page === currentPage ? 'active' : ''}`;
            pageButton.textContent = page + 1;
            pageButton.onclick = () => {
                currentPage = page;
                loadFiles($('#tree').jstree('get_selected')[0]);
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
                loadFiles($('#tree').jstree('get_selected')[0]); 
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
                loadFiles($('#tree').jstree('get_selected')[0]); 
            };
            paginationDiv.appendChild(lastButton);
        }
    }
    
    // 폴더가 없을 때 폴더 생성 버튼 
    $('#first_folder_add, #document_no_folder_msg').on('click', function(event){
		event.preventDefault();
		$('.modal_div').show();
		$('.first_folder_add_modal').show();
	});
		
	$('#first_folder_add_button').on('click', function(){
		// 입력된 폴더 이름 
		const folderName = $('#first_folder_name').val();
		if(folderName.trim() === ''){
			Swal.fire({
        		text: '폴더명을 입력해주세요.',
        		icon: 'warning',
        		confirmButtonText: '확인'
    		});
		} else{			
			$.ajax({
                type: 'POST',
                url: '/department/first/folder',
                contentType: 'application/json',
                data: JSON.stringify({ 
			        folderName: folderName,
			        memberNo: memberNo,
			        deptNo: deptNo
			    }),
				headers: {
            		'X-Requested-With': 'XMLHttpRequest',
            		'X-CSRF-TOKEN': csrfToken
        		},
                success: function(response) {
					if(response.res_code === '200'){	
						Swal.fire({
                    		icon: 'success',
                    		text: response.res_msg,
                    		confirmButtonText: '확인'
                		}).then(() => {
                        	// 새로 생성된 폴더 번호를 selectedFolderNo로 지정
                       	 	const newFolderNo = response.folderNo; 
                        	selectedFolderNo = newFolderNo;

                        	// 폴더 생성 성공 처리
                        	$('.modal_div').hide();
                        	// 폴더 리스트를 다시 가져오기 
                       		getFolders().then(() => {
                            	// 새로 생성된 폴더를 열기
                            	const tree = $('#tree').jstree(true);
                            	tree.select_node(newFolderNo);
                            	openFolderToNode(newFolderNo);
                            	loadFiles(newFolderNo);
                        	});	
                        	$('#first_folder_name').val('');			      
                        	$('.first_folder_add_modal').hide();               				
                        	$('.document_no_folder').hide();
                    		$('.document_select_folder').show();
                    		$('.folder_buttons').show();
                    		$('.box_size').show();
                    	});
					} else{
						Swal.fire({
                        	icon: 'error',
                        	text: response.res_msg,
                        	confirmButtonText: '확인'
                		});
					}
                }
            });				
		}					
	});
	// X 버튼 
	$('.cancel_div').on('click', function(){
		$('.modal_div').hide();
		$('.change_name_modal').hide();
		$('.folder_create_modal').hide();
		$('.file_upload_modal').hide();
		$('#first_folder_name').val('');
		$('#change_folder_name').val('');
		$('#create_folder_name').val('');
		$('#file_input').val('');
	});
	
	// 폴더 이름 변경 
	$('#change_name_button').on('click', function(event){
		event.preventDefault();		
		$('.modal_div').show();
		$('.change_name_modal').show();		
	});
	
	$('#folder_name_change_button').on('click', function(){
		changeFolderName();
	});
	
	// 폴더 이름 변경 함수 
	function changeFolderName() {
	    // 입력된 폴더 이름 
	    const newFolderName = $('#change_folder_name').val();
	    
	    if (newFolderName.trim() === '') {
	        Swal.fire({
	            text: '폴더명을 입력해주세요.',
	            icon: 'warning',
	            confirmButtonText: '확인'
	        });
	    } else {
	        $.ajax({
	            type: 'POST',
	            url: '/change/folder/name',
	            contentType: 'application/json',
	            data: JSON.stringify({
	                folderName: newFolderName, 
	                folderNo: selectedFolderNo, 
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
	
	                    // 폴더 이름 변경 성공 처리
	                    $('.modal_div').hide();
	                    
	                    // 폴더 이름 업데이트
                        const updatedFolder = folderList.find(f => f.id == selectedFolderNo);
                        if (updatedFolder) {
							// 새로운 폴더 이름을 출력하는 곳에 반영 
                            updatedFolder.text = newFolderName;
	                        $('#tree').jstree(true).set_text(selectedFolderNo, newFolderName);
                            updateFolderName(selectedFolderNo);
                        }
                        // 폴더랑 파일 다시 로드 
                        isFolderNameChange = true;
                        openFolderToNode(selectedFolderNo);
                        loadFiles(selectedFolderNo);
	                    $('.change_name_modal').hide();	
	                    $('.document_no_folder').hide();
	                    $('.document_select_folder').show();
	                    $('.folder_buttons').show();
	                    $('.box_size').show();
	                    $('#change_folder_name').val('');
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
	}

	// 폴더 생성
	$('#folder_add_button').on('click', function(event){
		event.preventDefault();		
		$('.modal_div').show();
		$('.folder_create_modal').show();		
	});
	
	$('#folder_create_button').on('click', function(){
		createFolder();
	});
	
	// 폴더 생성 함수
	function createFolder() {
	    // 입력된 폴더 이름 
	    const folderName = $('#create_folder_name').val();
	    
	    if (folderName.trim() === '') {
	        Swal.fire({
	            text: '폴더명을 입력해주세요.',
	            icon: 'warning',
	            confirmButtonText: '확인'
	        });
	    } else {
	        $.ajax({
	            type: 'POST',
	            url: '/department/create/folder',
	            contentType: 'application/json',
	            data: JSON.stringify({
	                folderName: folderName, 
	                parentFolderNo: selectedFolderNo, 
	                memberNo: memberNo,
	                deptNo: deptNo
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
	
	                    // 폴더 생성 성공 처리
	                    $('.modal_div').hide();
	                    folderCreated = 1; 
	                    // 폴더 리스트를 다시 가져오기
	                    const newFolderId = response.folderNo;
	                    selectedFolderNo = newFolderId;
						getFolders().then(() => {
							// 기존 선택 해제
	                        const tree = $('#tree').jstree(true);
	                        const prevSelectedNode = tree.get_selected(true)[0];
	                        if (prevSelectedNode) {
	                            tree.deselect_node(prevSelectedNode);
	                        }
	                        // 새로 생성된 폴더를 선택하고 열기
                            tree.select_node(newFolderId);
                            openFolderToNode(newFolderId);
							$('.folder_create_modal').hide();	
	                        $('.document_no_folder').hide();
	                        $('.document_select_folder').show();
	                        $('.folder_buttons').show();
	                        $('.box_size').show();
	                        $('#create_folder_name').val('');
	                    });
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
	}
	
	// 폴더 삭제
	$('#folder_delete_button').on('click', function(){
		$.ajax({
	            type: 'POST',
	            url: '/document/parent/existence',
	            contentType: 'application/json',
	            data: JSON.stringify({
	                folderNo: selectedFolderNo
	            }),
	            headers: {
	                'X-Requested-With': 'XMLHttpRequest',
	                'X-CSRF-TOKEN': csrfToken
	            },
	            success: function(response) {
					// 삭제할 폴더가 최상위 폴더가 아닐 경우 
	                if (response.res_result == 0) {
	                    Swal.fire({
	                        icon: 'warning',
	                        html: "해당 폴더의 하위 폴더가 모두 삭제되고<br>모든 파일이 최상위 폴더로 이동합니다.<br>폴더를 삭제하시겠습니까?",
	                        showCancelButton: true,
	                        confirmButtonColor: '#dc3545',
	                        confirmButtonText: '삭제',
	                        cancelButtonText: '취소'
	                    }).then((result) => {
							if(result.isConfirmed){
								$.ajax({
		                            type: 'POST',
		                            url: '/document/department/folder/delete',
		                            contentType: 'application/json',
		                            data: JSON.stringify({
		                                folderNo: selectedFolderNo,
		                                memberNo: memberNo
		                            }),
			                        headers: {
		                                'X-Requested-With': 'XMLHttpRequest',
		                                'X-CSRF-TOKEN': csrfToken
		                            },
		                            success: function(deleteResponse){
										if(deleteResponse.res_code === '200'){
											Swal.fire({
		                                        icon: 'success',
		                                        text: deleteResponse.res_msg,
		                                        confirmButtonText: '확인'
	                                    	});
	                                    	// 폴더 리스트를 다시 가져오기
											getFolders().then(() => {
												// 기존 선택 해제
						                        const tree = $('#tree').jstree(true);
						                        const prevSelectedNode = tree.get_selected(true)[0];
						                        if (prevSelectedNode) {
						                            tree.deselect_node(prevSelectedNode);
						                        }
						                        // 최상위 폴더를 선택하고 열기
		                                        const parentFolderNo = deleteResponse.parentNo;
		                                        tree.select_node(parentFolderNo);
		                                        openFolderToNode(parentFolderNo);
		                                        loadFiles(parentFolderNo);
						
						                        $('.document_no_folder').hide();
						                        $('.document_select_folder').show();
						                        $('.folder_buttons').show();
						                        $('.box_size').show();
						                    });
										} else{
											Swal.fire({
		                                        icon: 'error',
		                                        text: deleteResponse.res_msg,
		                                        confirmButtonText: '확인'
		                                    });
	                                    }
									}
								});
							}
						});
	                // 삭제할 폴더가 최상위 폴더일 경우 
	                } else {
	                    Swal.fire({
	                        icon: 'warning',
	                        html: "해당 폴더의 하위 폴더가 모두 삭제되고<br>모든 파일이 휴지통으로 이동합니다.<br>폴더를 삭제하시겠습니까?",
	                        showCancelButton: true,
	                        confirmButtonColor: '#dc3545',
	                        confirmButtonText: '삭제',
	                        cancelButtonText: '취소'
	                   }).then((result) => {
	                    if (result.isConfirmed) {                     
	                        $.ajax({
	                            type: 'POST',
	                            url: '/document/top/folder/delete',
	                            contentType: 'application/json',
	                            data: JSON.stringify({
	                                folderNo: selectedFolderNo,
	                                memberNo: memberNo
	                            }),
	                            headers: {
	                                'X-Requested-With': 'XMLHttpRequest',
	                                'X-CSRF-TOKEN': csrfToken
	                            },
	                            success: function(deleteResponse) {
	                                if (deleteResponse.res_code === '200') {
	                                    Swal.fire({
	                                        icon: 'success',
	                                        text: deleteResponse.res_msg,
	                                        confirmButtonText: '확인'
	                                    });
	                                    $('#tree').hide();
                                    	$('.document_no_folder').show();
                                    	$('.document_file_list').hide();
                                   	 	$('.folder_buttons').hide();
                                   	 	$('.box_size').hide();                                  	 	
	                                } else {
	                                    Swal.fire({
	                                        icon: 'error',
	                                        text: deleteResponse.res_msg,
	                                        confirmButtonText: '확인'
	                                    });
	                                }
	                            }
                        	});
                        }
                    });
                }
                $('#file_name_input').val(''); 
            }
        });
	});
	// 파일 업로드 
	$('#upload_button').on('click', function(event){
		event.preventDefault();		
		$('.modal_div').show();
		$('.file_upload_modal').show();	
	});
	$('#file_upload_button').on('click', function(){
		const fileInput = $('#file_input')[0];
		// 유효성 검사 
		// 선택된 파일이 존재하지 않을 때
	    if (fileInput.files.length === 0) {
	        Swal.fire({
	            icon: 'warning',
	            text: '업로드할 파일을 선택해주세요.',
	            confirmButtonText: '확인'
	        });
	        return; 
	    }
		if (fileInput.files.length > 0) {
			const file = fileInput.files[0]; 
	        const allowedExtensions = /(\.pdf|\.hwp|\.doc|\.docx|\.ppt|\.pptx|\.xls|\.xlsx)$/i;
	        const maxSizeBytes = 25 * 1024 * 1024; 
			// 파일 용량 초과 
	        if (file.size > maxSizeBytes) {
	            Swal.fire({
	                icon: 'warning',
	                text: '25MB 미만의 파일만 업로드 가능합니다.',
	                confirmButtonText: '확인'
	            });
	            return; 
	        } else if(!allowedExtensions.exec(file.name)){
				Swal.fire({
		            icon: 'warning',
		            text: '허용된 파일 형식이 아닙니다.',
		            confirmButtonText: '확인'
		        });
		        return;
	        } else {
				const formData = new FormData();
    			formData.append('file', file);
			    formData.append('folderNo', selectedFolderNo);
			    formData.append('memberNo', memberNo);
				$.ajax({
		            type: 'POST',
		            url: '/document/file/upload',
		            data: formData,
		            processData: false, 
        			contentType: false, 
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
		                    
		                    // 알림 전송 
		                    alarmSocket.send(JSON.stringify({
					           	type: 'noficationDocument',
					           	deptNo: deptNo,
					           	memberNo: memberNo
					        }));
					        				                  
		                    $('.modal_div').hide();
		                    $('#file_input').val('');
		                    $('.file_upload_modal').hide();
		                    loadFiles(selectedFolderNo);
		                    getAllFileSize();		                  						
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
	    }			
	});	
	// 파일 삭제 함수
	function deleteFile(fileNo){
		Swal.fire({
			icon: 'warning',
		    text: '파일을 삭제하시겠습니까?',
		    showCancelButton: true,
		    confirmButtonColor: '#dc3545',
		    confirmButtonText: '삭제',
		    cancelButtonText: '취소'
		}).then((result) => {
			if (result.isConfirmed) {
				$.ajax({
					type: 'POST',
					url: '/document/file/delete',
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
			            	loadFiles(selectedFolderNo);
			            	getAllFileSize();
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
	// 파일 선택 삭제
	function deleteSelectedFile(fileNos) {
	    Swal.fire({
	        icon: 'warning',
	        text: '파일을 삭제하시겠습니까?',
	        showCancelButton: true,
	        confirmButtonColor: '#dc3545',
	        confirmButtonText: '삭제',
	        cancelButtonText: '취소'
	    }).then((result) => {
	        if (result.isConfirmed) {
	            $.ajax({
	                type: 'POST',
	                url: '/document/fileList/delete',  
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
	                        loadFiles(selectedFolderNo); 
	                        getAllFileSize();
				        	$('#select_delete').prop('disabled', true);
    						$('#select_down').prop('disabled', true);
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
    // 페이지가 로드될 때 폴더 리스트를 불러옴
    $(document).ready(function() {
		let previousFolderNo = null;
		let searchInputValue = '';
	    getFolders().then(() => {
	        getAllFileSize();
	
	        // 파일 목록을 로드하는 함수 호출
	        const selectedFolderNo = $('#tree').jstree('get_selected')[0];
	        if (selectedFolderNo) {
	            loadFiles(selectedFolderNo);
	            previousFolderNo = selectedFolderNo;
	        }
	    });       
        // 폴더 선택 변경 함수 
		$('#tree').on('select_node.jstree', function(e, data) {
		    if (data.selected && data.selected.length > 0) {
		        const selectedFolderNo = data.selected[0];
		        if (selectedFolderNo !== previousFolderNo) {
		            $('#file_name_input').val('');
		            searchInputValue = '';
		            loadFiles(selectedFolderNo, searchInputValue = '');
		            previousFolderNo = selectedFolderNo;
		            startDateInput.value = oneYearAgoStr;
		            endDateInput.value = todayStr;
		        }
		    } else {
		        console.error("선택된 폴더가 존재하지 않습니다.");
		    }
		});
	    // 정렬 선택이 변경될 때 파일 목록을 다시 불러옴
	    $('#sort_select').on('change', function() {
	        const selectedFolderNo = $('#tree').jstree('get_selected')[0];
	        if (selectedFolderNo) {
	            const searchInput = $('#file_name_input').val(); 
	            const searchOption = $('#name_select').val(); 
	            loadFiles(selectedFolderNo, searchInput, searchOption); 
	        }
	    });
	
	    // 날짜가 변경될 때 파일 목록을 새로 로드
	    startDateInput.addEventListener('change', function() {
	        startDateLimit();
	        const selectedFolderNo = $('#tree').jstree('get_selected')[0];
	        if (selectedFolderNo) {
	            const searchInput = $('#file_name_input').val(); 
	            const searchOption = $('#name_select').val(); 
	            loadFiles(selectedFolderNo, searchInput, searchOption); 
	        }
	    });
	    
	    endDateInput.addEventListener('change', function() {
	        startDateLimit();
	        const selectedFolderNo = $('#tree').jstree('get_selected')[0];
	        if (selectedFolderNo) {
	            const searchInput = $('#file_name_input').val(); 
	            const searchOption = $('#name_select').val(); 
	            loadFiles(selectedFolderNo, searchInput, searchOption); 
	        }
	    });
		// 검색 입력 시 파일 목록을 다시 로드
	    $('#file_name_input').on('keyup', function() {
	        searchInputValue = $(this).val(); 
	        const selectedFolderNo = $('#tree').jstree('get_selected')[0];
	        if (selectedFolderNo) {
	            loadFiles(selectedFolderNo, searchInputValue);
	        }
	    });
	    // 검색 옵션이 변경될 때 입력된 검색어를 사용하여 파일 목록을 다시 로드
	    $('#name_select').on('change', function() {
	        const selectedFolderNo = $('#tree').jstree('get_selected')[0];
	        if (selectedFolderNo) {
	            const searchInput = $('#file_name_input').val(); 
	            const searchOption = $('#name_select').val(); 
	            loadFiles(selectedFolderNo, searchInput, searchOption);
	        }
	    });
	});
});