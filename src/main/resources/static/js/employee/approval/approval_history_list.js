document.addEventListener('DOMContentLoaded', function() {
    const approvalRow = document.querySelectorAll('.approval_row');
    window.functionTypes = [3, 5, 7, 9]; 
    approvalRow.forEach(row => {
        row.addEventListener('click', function() {
            const approvalNo = this.getAttribute('data-approval_no');
            const approvalType = this.getAttribute('data-type');
            
            if (approvalType === 'VACATION') {
                window.location.href = '/employee/approval/approval_history_vacation_detail/' + approvalNo;
                 if (window.functionTypes.includes(3) || window.functionTypes.includes(5)) {
                                     markApprovalAsRead(3, approvalNo); 
                                     markApprovalAsRead(5, approvalNo); 
                                 }
            } else {
                window.location.href = '/employee/approval/approval_history_detail/' + approvalNo;
                 if (window.functionTypes.includes(7) || window.functionTypes.includes(9)) {
                                     markApprovalAsRead(7, approvalNo); 
                                     markApprovalAsRead(9, approvalNo); 
                                 }
            }
        });
    });
    
});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '전자결재&emsp;&gt;&emsp;결재 수신함&emsp;&gt;&emsp;결재 내역함';