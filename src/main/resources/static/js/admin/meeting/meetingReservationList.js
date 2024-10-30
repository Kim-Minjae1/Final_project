document.addEventListener('DOMContentLoaded', function() {
    var startDateInput = document.getElementById('startDate');
    var endDateInput = document.getElementById('endDate');
 
    function updateMinEndDate() {
        var startDate = startDateInput.value;
        if (startDate) {
            endDateInput.setAttribute('min', startDate);
        } else {
            endDateInput.removeAttribute('min');
        }
    }
 
    startDateInput.addEventListener('change', updateMinEndDate);
 
    updateMinEndDate();
    
    // 예약 등록일 
	document.querySelectorAll('tr').forEach(row => {
	    const lastDateElement = row.querySelector('td.create_date');
	    if (lastDateElement) {
	        const dateStr = lastDateElement.textContent.trim();
	        const formattedDate = dateStr.substring(0, 10); 
	        lastDateElement.textContent = formattedDate;
	    }
	});
	
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '회의실 관리&emsp;&gt;&emsp;예약 관리';

});