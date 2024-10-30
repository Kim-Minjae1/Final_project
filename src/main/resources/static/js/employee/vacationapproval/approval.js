document.addEventListener("DOMContentLoaded", function () {
    let selectedMembers = [];
    let approvers = [];
    let references = [];
    let reviewers = [];

    let approverNames = [];
    let referenceNames = [];
    let reviewerNames = [];

    $('#openChart').click(function () {
        $('#organizationChartModal').modal('show');
        resetSelections();
        loadOrganizationChart();
    });

    function resetSelections() {
        selectedMembers = [];
        approvers = [];
        references = [];
        reviewers = [];
        approverNames = [];
        referenceNames = [];
        reviewerNames = [];

        $('#approver-list').empty();
        $('#reference-list').empty();
        $('#reviewer-list').empty();
    }

    function loadOrganizationChart() {
        $.ajax({
            url: '/organizationChart/chart',
            method: 'GET',
            success: function (data) {
                $('#organization-chart').jstree('destroy');
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
                });

                $('#organization-chart').on('changed.jstree', function (e, data) {
                    updateSelectedMembers(data.selected, data.instance);
                });

                $('#organization_search').on('keyup', function () {
                    const searchString = $(this).val();
                    $('#organization-chart').jstree(true).search(searchString);
                });
            },
        });
    }

    function updateSelectedMembers(selectedIds, instance) {
        const selectedNodes = instance.get_selected(true);
        selectedMembers = [];

        selectedNodes.forEach(function (node) {
            if (node.original.type === 'member') {
                const memberId = node.id;
                const memberNumber = memberId.replace('member_', '');
                selectedMembers.push(memberNumber);
            }
        });
    }

    function addMemberToBox(node, boxId, array, nameArray) {
        const memberId = node.id;
        const memberNumber = memberId.replace('member_', '');
        const memberElement = $('<div class="selected-member"></div>');
        const memberName = $('<span></span>').text(node.text).data('member-number', memberNumber);
        const removeButton = $('<button class="remove-member">&times;</button>');
        const checkBox = $('<input type="checkbox" class="remove-checkbox">');
        memberElement.append(checkBox).append(memberName).append(removeButton);
        $(`#${boxId}`).append(memberElement);

        nameArray.push(node.text);

        removeButton.click(function () {
            removeMemberFromBox(memberNumber, memberElement, boxId, array, nameArray);
        });
    }

    function removeMemberFromBox(memberNumber, memberElement, boxId, array, nameArray) {
        const index = array.indexOf(memberNumber);
        if (index > -1) {
            array.splice(index, 1);
        }

        const nameIndex = nameArray.indexOf(memberElement.find('span').text());
        if (nameIndex > -1) {
            nameArray.splice(nameIndex, 1);
        }

        console.log(`${boxId} 배열:`, array);

        memberElement.remove();

        const nodeId = 'member_' + memberNumber;
        $('#organization-chart').jstree(true).enable_node(nodeId);
    }

    function moveToList(event, targetId, array, nameArray) {
        event.preventDefault();

        const selectedNodes = $('#organization-chart').jstree(true).get_selected(true);

		if (targetId === 'approver-list') {
	        const currentApproverCount = approvers.length;
	        const totalSelected = selectedNodes.length;
	        const remainingSlots = 7 - currentApproverCount;
	        if (totalSelected > remainingSlots) { 
	            Swal.fire({
	                icon: 'warning',
	                text: `결재자는 최대 6명만 추가할 수 있습니다.`,
	            });
	            return;
	        }
	    }

        selectedNodes.forEach(function (node) {
            if (node.original.type === 'member' && !$('#organization-chart').jstree(true).is_disabled(node)) {
                const memberId = node.id;
                const memberNumber = memberId.replace('member_', '');

                if (!array.includes(memberNumber)) {
                    addMemberToBox(node, targetId, array, nameArray);
                    $('#organization-chart').jstree(true).disable_node(node);
                    array.push(memberNumber);
                    console.log(`${targetId} 배열:`, array);
                }
            }
        });
    }

    function moveFromList(event, boxId, array, nameArray) {
        event.preventDefault();

        $(`#${boxId} .selected-member .remove-checkbox:checked`).each(function () {
            const memberElement = $(this).closest('.selected-member');
            const memberNumber = memberElement.find('span').data('member-number');
            removeMemberFromBox(memberNumber, memberElement, boxId, array, nameArray);
        });
    }

    $('.move-to-approver').click(function (event) {
        moveToList(event, 'approver-list', approvers, approverNames);
    });

    $('.move-from-approver').click(function (event) {
        moveFromList(event, 'approver-list', approvers, approverNames);
    });

    $('.move-to-reference').click(function (event) {
        moveToList(event, 'reference-list', references, referenceNames);
    });

    $('.move-from-reference').click(function (event) {
        moveFromList(event, 'reference-list', references, referenceNames);
    });

    $('.move-to-reviewer').click(function (event) {
        moveToList(event, 'reviewer-list', reviewers, reviewerNames);
    });

    $('.move-from-reviewer').click(function (event) {
        moveFromList(event, 'reviewer-list', reviewers, reviewerNames);
    });

    function updateApproversDisplay() {
        $('#approvalLineTable th').each(function () {
        $(this).find('input').remove();  
    });

    $('#approvalLineTable tr:nth-child(2) td:not(#approve) div').remove();
    $('#approvalLineTable tr:nth-child(1) td:not(#approve) div span').remove();
    $('#approvalLineTable tr:nth-child(3) td:not(#approve) span').remove();

    $('#referencestCell').empty();
    $('#reviewerCell').empty();

    approvers.forEach(function (memberNumber, index) {
        const fullName = approverNames[index];
        const [name, position] = fullName.split(' ');
        const listItem = $('<span></span>').text(`${name}`);
        const positionItem = $('<span></span>').text(`${position}`);

        $('#approvalLineTable td div').eq(index + 1).append(positionItem);
            
            const targetRow = $('#approvalLineTable tr').eq(2); 
            targetRow.find('td').eq(index + 1).append(listItem);
            
            $('<input type="hidden">').attr('id', 'approverNumbers').val(memberNumber).appendTo('#approvalLineTable th').eq(0);
    });

    references.forEach(function (memberNumber, index) {
        const fullName = referenceNames[index];
        const [name] = fullName.split(' ');
        const listItem = $('<span></span>').text(`${name} `);
        $('#referencestCell').append(listItem);
        $('<input type="hidden">').attr('id', 'referenceNumbers').val(memberNumber).appendTo('#referencestCell');
    });

    reviewers.forEach(function (memberNumber, index) {
        const fullName = reviewerNames[index];
        const [name] = fullName.split(' ');
        const listItem = $('<span></span>').text(`${name} `);
        $('#reviewerCell').append(listItem);
        $('<input type="hidden">').attr('id', 'reviewerNumbers').val(memberNumber).appendTo('#reviewerCell');
    });
}
    

    $('#confirmButton').click(function (event) {
        event.preventDefault();
        updateApproversDisplay(); 
        $('#organizationChartModal').modal('hide');
    });
});
