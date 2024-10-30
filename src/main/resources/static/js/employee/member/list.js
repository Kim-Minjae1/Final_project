document.addEventListener('DOMContentLoaded', function(){
	const memberRows = document.querySelectorAll('.member_row');
	
	memberRows.forEach(row => {
		row.addEventListener('click',function(){
			const memberNo = this.getAttribute('data_member_no');
			
			window.location.href = '/employee/member/detail/'+memberNo;
		});
	});
});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '주소록';