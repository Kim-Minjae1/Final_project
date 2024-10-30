document.addEventListener('DOMContentLoaded',function(){
	const formRows = document.querySelectorAll('.form_row');
	
	formRows.forEach(row => {
		row.addEventListener('click',function(){
			const formNo = this.getAttribute('data_form_no');
			
			window.location.href = '/admin/approval/detail/'+formNo;
		});
	});
});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '전자 결재 양식 관리&emsp;&gt;&emsp;결재 양식함';