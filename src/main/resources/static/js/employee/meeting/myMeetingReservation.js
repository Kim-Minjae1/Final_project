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
    
	const location_text = document.getElementById('header_location_text');
	location_text.innerHTML = '회의실&emsp;&gt;&emsp;예약 내역';
});