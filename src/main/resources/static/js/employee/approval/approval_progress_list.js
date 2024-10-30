document.addEventListener('DOMContentLoaded', function(){
	const approvalRows = document.querySelectorAll('.approval_row');
	    window.functionTypes = [15]; 
	approvalRows.forEach(row => {
		row.addEventListener('click',function(){
			const approvalNo = this.getAttribute('data_approval_no');
			
			window.location.href = '/employee/approval/approval_progress_detail/'+approvalNo;
			
				if (window.functionTypes.includes(15)) {
					markApprovalAsRead(15, approvalNo); 
				}
		});
	});
});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '전자결재&emsp;&gt;&emsp;결재 상신함&emsp;&gt;&emsp;결재 진행함';