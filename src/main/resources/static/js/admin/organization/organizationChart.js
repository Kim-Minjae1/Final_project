document.addEventListener("DOMContentLoaded", function () {
    let selectedMembers = [];  // 선택된 사원을 저장할 배열

    // 페이지 로드 시 이전 선택된 사원 로드
    function loadSelectedMembers() {
        const savedMembers = localStorage.getItem('selectedMembers');
        if (savedMembers) {
            selectedMembers = JSON.parse(savedMembers);
        }
    }

    // 페이지 로드 시 기존 선택 상태를 복원
    function restoreSelection(instance) {
        selectedMembers.forEach(memberName => {
            const node = instance.get_node(instance.get_container().find(`:contains('${memberName}')`).attr('id'));
            if (node) {
                instance.check_node(node);
            }
        });
    }

    $('#openChart').click(function() {
        $('#organizationChartModal').modal('show');
        loadOrganizationChart();
    });

    // 조직도 로딩
    function loadOrganizationChart() {
        $.ajax({
            url: '/organizationChart/chart',
            method: 'GET',
            success: function(data) {
                console.log('조직도 데이터:', data);
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
                    restoreSelection(data.instance);
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
	    selectedMembers = [];
	
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
	
	    localStorage.setItem('selectedMembers', JSON.stringify(selectedMembers));
}

    // 확인 버튼 
    $('#confirmButton').click(function() {
        console.log("선택한 사원 정보:", selectedMembers);
        alert("선택한 사원: " + zselectedMembers.join(", "));
    
        var csrfToken = document.querySelector('input[name="_csrf"]').value; 
    
        $.ajax({
            url: '/api/organization/saveSelectedMembers',
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            contentType: 'application/json',
            data: JSON.stringify({ members: selectedMembers }),
            success: function(response) {
                console.log('선택한 사원 저장 성공:', response);
                alert('선택한 사원이 저장되었습니다.');
                
                $('#organizationChartModal').modal('hide');

                localStorage.removeItem('selectedMembers');
                
                $('.permission_pick_list').empty();
            },
            error: function(xhr, status, error) {
                console.error('선택한 사원 저장 오류:', error);
                alert('선택한 사원을 저장하는데 오류가 발생했습니다.');
            }
        });
    });

    loadSelectedMembers();
});