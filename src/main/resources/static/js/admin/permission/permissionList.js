let selectedMembers = [];
let selectedMenuNo = null;
let assignedMemberNos = []; 

let currentPage = 1;
const itemsPerPage = 10;
let totalItems = 0;

let allPermissionData = [];  
let sortOrder = 'newest'; 

const defaultMenuNo = 2; 
const defaultPermissionLink = document.querySelector(`.permission_Lists a[data-id="${defaultMenuNo}"]`);

if (defaultPermissionLink) {
    fetchPermissionMembers(defaultPermissionLink);
}

// 정렬 
document.getElementById('sortOrderSelect').value = 'newest';
 
document.getElementById('sortOrderSelect').addEventListener('change', function() {
    sortOrder = this.value;
    fetchPermissionMembers(document.querySelector(`.permission_Lists a[data-id="${selectedMenuNo}"]`));
});

function fetchPermissionMembers(element) {
	$('#selectAllCheckbox').prop('checked', false);
    $('.member-checkbox').prop('checked', false)
    
    selectedMenuNo = element.getAttribute('data-id');
    const menuNo = selectedMenuNo;
    const functionName = element.textContent;
    const sectionTitle = `${functionName} 권한자 목록`;
    document.getElementById('sectionTitle').textContent = sectionTitle;

    document.getElementById('searchMember').value = '';

    var selectedElements = document.querySelectorAll('.permission_Lists a.selected');
    selectedElements.forEach(function(item) {
        item.classList.remove('selected');
    });

    element.classList.add('selected');

    $.ajax({
        url: `/permission/members?menuNo=${menuNo}`,
        method: 'GET',
        success: function(data) {
            allPermissionData = sortOrder === 'newest'
                ? data.sort((a, b) => new Date(b[4]) - new Date(a[4]))
                : data.sort((a, b) => new Date(a[4]) - new Date(b[4]));

            totalItems = allPermissionData.length;  
            currentPage = 1;  
            displayMembers(allPermissionData, currentPage);
            setupPagination();

            if ($('#organization-chart').jstree(true)) {
                updateOrganizationChartCheckboxes();
            }
        },
        error: function(xhr, status, error) {
            Swal.fire({
                icon: 'error',
                title: '오류',
                text: '권한자 목록을 가져오는 중 오류가 발생했습니다.',
                confirmButtonText: '닫기'
            });
        }
    });
}

// 권한자 목록 조회
function displayMembers(data, page) {
    const memberListTableBody = document.getElementById('memberList').getElementsByTagName('tbody')[0];
    memberListTableBody.innerHTML = ''; 
    selectedMembers = []; 
    
    $('#selectAllCheckbox').prop('checked', false);  
    $('.member-checkbox').prop('checked', false);

    const start = (page - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    const pageData = data.slice(start, end);

    if (pageData.length === 0) {
        $('#memberList tr').hide(); 
        
        const row = memberListTableBody.insertRow();
        row.insertCell().colSpan = 4;
        row.cells[0].textContent = '등록된 권한자가 없습니다.'; 
        
        document.getElementById('pagination').style.display = 'none';
   		document.getElementById('sortOrderSelect').style.display = 'none'; 
    } else {
        pageData.forEach(member => {
			$('#memberList tr').show();
            const row = memberListTableBody.insertRow();
            const checkboxCell = row.insertCell();
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.className = 'member-checkbox';
            checkbox.dataset.memberNo = member[0];
            checkboxCell.appendChild(checkbox);
            
            row.insertCell().textContent = member[2]; // 부서
            row.insertCell().textContent = `${member[1]} ${member[3]}`; // 사원명 + 직위명 
			const date = new Date(member[4]); 
			const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
			row.insertCell().textContent = formattedDate; // 권한 등록일  
        });
 		setupPagination();
        document.getElementById('pagination').style.display = 'flex';
    	 document.getElementById('sortOrderSelect').style.display = 'inline-block'; 
    }

    updateDeleteButtonState();
} 

// 페이징
function setupPagination() {
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    let paginationElement = document.getElementById('pagination');
 
    if (!paginationElement) {
        paginationElement = document.createElement('div');
        paginationElement.id = 'pagination';
        document.getElementById('permissionMembersSection').appendChild(paginationElement);
    }

    let paginationHTML = '';

    // 첫 페이지 버튼 (<<)
    if (currentPage > 1) {
        paginationHTML += `<button onclick="goToFirstPage()">&lt;&lt;</button>`;
    }

    // 이전 버튼 (<)
    if (currentPage > 1) {
        paginationHTML += `<button onclick="changePage(${currentPage - 1})">&lt;</button>`;
    }

    // 페이지 번호 버튼
    let startPage = Math.max(1, currentPage - 1);
    let endPage = Math.min(totalPages, currentPage + 1);

    if (currentPage <= 2) {
        endPage = Math.min(3, totalPages);
    } else if (currentPage >= totalPages - 1) {
        startPage = Math.max(totalPages - 2, 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `<button onclick="changePage(${i})" ${i === currentPage ? 'class="active"' : ''}>${i}</button>`;
    }

    // 다음 버튼 (>)
    if (currentPage < totalPages) {
        paginationHTML += `<button onclick="changePage(${currentPage + 1})">&gt;</button>`;
    }

    // 마지막 페이지 버튼 (>>)
    if (currentPage < totalPages) {
        paginationHTML += `<button onclick="goToLastPage()">&gt;&gt;</button>`;
    }

    paginationElement.innerHTML = paginationHTML;
}

function goToFirstPage() {
    currentPage = 1;
    displayMembers(allPermissionData, currentPage);
    setupPagination();  
}

function goToLastPage() {
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    currentPage = totalPages;
    displayMembers(allPermissionData, currentPage);
    setupPagination();  
}

function changePage(page) {
    currentPage = page;
    displayMembers(allPermissionData, currentPage);
    setupPagination(); 
}

function openOrganizationChartModal() {
    selectedMembers = []; // 선택된 멤버 목록 초기화
 
    const displayElement = document.getElementById('selected-members');
    if (displayElement) {
        displayElement.innerHTML = '';
    }

    $('#organizationChartModal').modal('show');

    loadOrganizationChart();
}

function loadOrganizationChart() {
    $.ajax({
        url: '/permission/chart',
        method: 'GET',
        data: {
            selectedMemberNos: selectedMembers.map(member => member.no),
            menuNo: selectedMenuNo
        },
        success: function(data) {
            $('#organization-chart').jstree('destroy');
            $('#organization-chart').jstree({ 
                'core': {
                    'data': data.chartData,
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
                },
                'checkbox': {
                    tie_selection: false,
                    whole_node: true,
                    three_state: true
                }
            }).on('ready.jstree', function (e, data) {
                updateOrganizationChartCheckboxes(data.instance);
                $.ajax({
                    url: '/permission/assigned-members',
                    method: 'GET',
                    data: { menuNo: selectedMenuNo },
                    success: function(assignedMembers) {
                        assignedMemberNos = assignedMembers;  
                        disableCheckedMembers(assignedMembers);
                    } 
                });
            });

            $('#organization-chart').on('check_node.jstree uncheck_node.jstree', function (e, data) {
                updateSelectedMembers(data.instance);
            });

            $('#organization_search').on('keyup', function() { 
                const searchString = $(this).val();
                $('#organization-chart').jstree(true).search(searchString);
            });
        },
        error: function(xhr, status, error) {
            Swal.fire({
                icon: 'error',
                title: '오류',
                text: '조직도를 불러오는 중 오류가 발생했습니다.',
                confirmButtonText: '닫기'
            });
        }
    });
}

// 등록된 사원 체크박스 비활성화
function disableCheckedMembers(assignedMemberNos) {
    var jstree = $('#organization-chart').jstree(true);
    if (jstree) {
        assignedMemberNos.forEach(function(memberNo) {
            var nodeId = 'member_' + memberNo;
            if (jstree.get_node(nodeId)) {
                jstree.disable_node(nodeId);
                jstree.check_node(nodeId);
            }
        });
    }
}

function updateOrganizationChartCheckboxes(jstreeInstance) {
    if (jstreeInstance) {
        jstreeInstance.uncheck_all();
        selectedMembers.forEach(function(member) {
            var nodeId = 'member_' + member.no;
            jstreeInstance.check_node(nodeId);
        });
    }
}

function updateSelectedMembers(instance) {
    selectedMembers = [];
    var checkedNodes = instance.get_checked(true);
    checkedNodes.forEach(function(node) {
        if (node.type === 'member' && !assignedMemberNos.includes(parseInt(node.id.split('_')[1]))) {
            selectedMembers.push({ 
                no: node.id.split('_')[1], 
                name: node.text 
            });
        }
    });
    updateSelectedMembersDisplay();
}
 
function updateSelectedMembersDisplay() {
    const displayElement = document.getElementById('selected-members');
    displayElement.innerHTML = ''; 
    
    if (selectedMembers.length > 0) {
        selectedMembers.forEach(member => { 
            const memberDiv = document.createElement('div');
            memberDiv.className = 'selected-member';
 
            const memberSpan = document.createElement('span');
            memberSpan.textContent = `${member.name}`;
 
            const removeButton = document.createElement('button');
            removeButton.textContent = '×';
            removeButton.className = 'remove-member';
            removeButton.onclick = function() { 
                const nodeId = `member_${member.no}`;
                const instance = $('#organization-chart').jstree(true);
                if (instance) {
                    instance.uncheck_node(nodeId);
                }
 
                selectedMembers = selectedMembers.filter(m => m.no !== member.no);
                updateSelectedMembersDisplay();
 
                $('.permission_pick_list').find(`.permission-item[data-name="${member.name}"]`).remove();
 
                localStorage.setItem('selectedMembers', JSON.stringify(selectedMembers));
            };
 
            memberDiv.appendChild(memberSpan);
            memberDiv.appendChild(removeButton);
 
            displayElement.appendChild(memberDiv);
        });
    } 
}

$('#confirmButton').click(function() {  
    var csrfToken = document.querySelector('input[name="_csrf"]').value; 
    
    $.ajax({
        url: '/permission/addMembers',
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': csrfToken,
            'Content-Type': 'application/json'
        }, 
        data: JSON.stringify({
            menuNo: selectedMenuNo,
            memberNos: selectedMembers.map(member => member.no)
        }),
        success: function(response) {
            if (response.res_code === "200") {
                Swal.fire({ 
			        text: response.res_msg,
			        icon: 'success', 
			        confirmButtonColor: '#0056b3', 
			        confirmButtonText: '확인', 
			    }).then((result) => {
                    if (result.isConfirmed) {
                        $('#organizationChartModal').modal('hide');
                        currentPage = 1; // 첫 페이지로 리셋
                        fetchPermissionMembers(document.querySelector(`.permission_Lists a[data-id="${selectedMenuNo}"]`));
                    }
                });
            } else {
                Swal.fire('권한자 등록', response.res_msg, 'error');
            }
        },
        error: function () {
            Swal.fire("서버 오류", "권한자 등록 중 오류가 발생했습니다.", "error");
        }
    });
});

$(document).on('change', '.member-checkbox', function() {
    updateDeleteButtonState();
    updateSelectAllCheckbox();  
});

$('#selectAllCheckbox').change(function() {
    const isChecked = this.checked;
    $('.member-checkbox').prop('checked', isChecked);
    updateDeleteButtonState();
});

function updateDeleteButtonState() {
    const anyChecked = $('.member-checkbox:checked').length > 0;
    $('#deleteButton').prop('disabled', !anyChecked);
}

function updateSelectAllCheckbox() {
    const totalCheckboxes = $('.member-checkbox').length;  
    const checkedCheckboxes = $('.member-checkbox:checked').length;  
    $('#selectAllCheckbox').prop('checked', totalCheckboxes === checkedCheckboxes);  
} 

function updateDeleteButtonState() {
    const anyChecked = $('.member-checkbox:checked').length > 0;
    $('#deleteButton').prop('disabled', !anyChecked);
}

$('#deleteButton').click(function() {
    const selectedMemberNos = $('.member-checkbox:checked').map(function() {
        return $(this).data('memberNo');
    }).get();
    Swal.fire({ 
        text: '선택한 권한자를 삭제하시겠습니까?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#f8f9fa',
        confirmButtonText: '삭제',
        cancelButtonText: '취소' 
    }).then((result) => {
        if (result.isConfirmed) {
            const menuNo = Number(selectedMenuNo); 

            const csrfToken = document.querySelector('input[name="_csrf"]').value;

            $.ajax({
                url: '/permission/deleteMembers',
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': csrfToken,
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify({ 
                    memberNos: selectedMemberNos,
                    menuNo: menuNo  
                }),
                success: function(response) {
                    if (response.res_code === "200") {
						Swal.fire({ 
					        text: response.res_msg,
					        icon: 'success', 
					        confirmButtonColor: '#0056b3', 
					        confirmButtonText: '확인', 
					    }).then(() => {
                            currentPage = 1;  
                            fetchPermissionMembers(document.querySelector(`.permission_Lists a[data-id="${menuNo}"]`));
                        });
                    } else {
                        Swal.fire('', response.res_msg, 'error');
                    }
                },
                error: function () {
                    Swal.fire("", "권한자 삭제 중 오류가 발생했습니다.", "error");
                }
            });
        }
    });
}); 

$(document).ready(function() {
    const defaultPermissionLink = document.querySelector(`.permission_Lists a[data-id="${defaultMenuNo}"]`);
    if (defaultPermissionLink) {
        fetchPermissionMembers(defaultPermissionLink);
    }
});

// 검색
function filterMembersBySearch() {
    const searchQuery = document.getElementById('searchMember').value.toLowerCase();

    const filteredData = allPermissionData.filter(member => {
        const department = member[2].toLowerCase();
        const employeeName = `${member[1]} ${member[3]}`.toLowerCase();
        return department.includes(searchQuery) || employeeName.includes(searchQuery);
    });

    totalItems = filteredData.length;  
    displayMembers(filteredData, 1); 
    setupPagination(); 
}

document.getElementById('searchMember').addEventListener('input', filterMembersBySearch);

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '통합 권한 관리';