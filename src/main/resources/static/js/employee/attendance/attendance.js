// 시계 
function updateClock() {
	var now = new Date();
    let hours = now.getHours();
    let minutes = now.getMinutes();
    let seconds = now.getSeconds();
    	
    if(hours < 10){
		hours = '0' + hours;
	}
	if(minutes < 10){
		minutes = '0' + minutes;
	}
	if(seconds < 10){
		seconds = '0' + seconds;
	}
    var clock = hours + ':' + minutes + ':' + seconds;
    document.getElementById('clock').textContent = clock;
}

// 매 초마다 업데이트 
setInterval(updateClock, 1000);

// 페이지 로드 시에도 시계 업데이트
document.addEventListener('DOMContentLoaded', function() {
	updateClock();
});

// 출퇴근 기능 
document.addEventListener('DOMContentLoaded', function() {
    // memberNo 값 가져오기 
    var memberNo = document.getElementById('attendance_memberNo').value;
	
	// 출근 버튼과 시간 표시 div
   	var checkInButton = document.getElementById('check_in_button');
    var checkOutButton = document.getElementById('check_out_button');
    var checkInTimeDiv = document.getElementById('check_in_time');
    var checkOutTimeDiv = document.getElementById('check_out_time');
    checkOutButton.disabled = true;
    checkInCheck();
    
	// 현재 시간을 'HH:mm:ss' 형식으로 반환하는 함수
    function getCurrentTimeString() {
        var now = new Date();
        var hours = String(now.getHours()).padStart(2, '0');
        var minutes = String(now.getMinutes()).padStart(2, '0');
        var seconds = String(now.getSeconds()).padStart(2, '0');
        return `${hours}:${minutes}:${seconds}`;
    }
    // 출근 기록이 없으면 퇴근 버튼 비활성화 
    function checkInCheck(){
		var csrfToken = document.querySelector('input[name="_csrf"]').value;
        var url = '/attendance/check';
        var jsonData = JSON.stringify({ memberNo: memberNo });	
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-TOKEN': csrfToken
            },
            body: jsonData
        })
	    .then(response => response.json())
	    .then(data => {
	        if (data.result === '0') {
	            checkOutButton.disabled = true;
	        } else {
	            checkOutButton.disabled = false;
	        }
	    })
	}
    
    
    // 출근버튼이 활성화된 상태라면 출근 기능 실행 
    if(!checkInButton.disabled){		
		checkInButton.addEventListener('click', function(event) {
			// submit이 자동으로 되지 않게 해줌 
			event.preventDefault();
			
            var csrfToken = document.querySelector('input[name="_csrf"]').value;
            var url = '/attendance/checkIn';
            var jsonData = JSON.stringify({ memberNo: memberNo });

            // 버튼 비활성화 및 스타일 업데이트
            checkInButton.disabled = true;

            // AJAX 요청 보내기
            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: jsonData
            })
            .then(response => response.json())
            .then(data => {
                if (data.res_code === '200') {
                    Swal.fire({
                        icon: 'success',
                        text: data.res_msg,
                        confirmButtonText: '확인'
                    });
                    // 출근 시간 
                    checkInTimeDiv.textContent = getCurrentTimeString();
                    checkOutButton.disabled = false;
                } else {
                    Swal.fire({
                        icon: 'error',
                        text: data.res_msg,
                        confirmButtonText: '확인'
                    });
                }
            });
	    });
	}
   	// 퇴근 기능 
    document.getElementById('check_out_button').addEventListener('click', function() {
		// submit이 자동으로 되지 않게 해줌 
		event.preventDefault();
		
        var csrfToken = document.querySelector('input[name="_csrf"]').value;
        var url = '/attendance/checkOut';
        var jsonData = JSON.stringify({ memberNo: memberNo });

        // AJAX 요청 보내기
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-TOKEN': csrfToken
            },
            body: jsonData
        })
        .then(response => response.json())
        .then(data => {
            if (data.res_code === '200') {
                Swal.fire({
                    icon: 'success',
                    text: data.res_msg,
                    confirmButtonText: '확인'
                });
                checkOutTimeDiv.textContent = getCurrentTimeString();
            } else {
                Swal.fire({
                    icon: 'error',
                    text: data.res_msg,
                    confirmButtonText: '확인'
                });
            }
        });
    });
});