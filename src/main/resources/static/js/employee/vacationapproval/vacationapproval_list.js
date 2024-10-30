document.addEventListener('DOMContentLoaded', function() {
    const vacationapprovalRows = document.querySelectorAll('.vacationapproval_row');
    window.functionTypes = [6, 14]; 
    console.log("현재 기능 타입: " + window.functionTypes);	

    vacationapprovalRows.forEach(row => {
        row.addEventListener('click', function() {
            const vacationapprovalNo = this.getAttribute('data_vacationapproval_no');

            window.location.href = '/employee/vacationapproval/detail/' + vacationapprovalNo;
            
            if (window.functionTypes.includes(14)) {
                markApprovalAsRead(14, vacationapprovalNo); 
                console.log("문서번호 14 : " + vacationapprovalNo);
            }

            if (window.functionTypes.includes(6)) {
                markApprovalAsRead(6, vacationapprovalNo); 
                console.log("문서번호 6 : " + vacationapprovalNo);
            }

        });
    });

       // 개수를 업데이트하는 함수를 호출
        updateVacationCounts();



});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '휴가&emsp;&gt;&emsp;휴가 신청함';


 // 개수를 업데이트하는 함수
function updateVacationCounts() {
    fetch(`/user/vacationCount/${headerCurrentMember}`)
        .then(response => response.json())
        .then(data => {
            const vacationTotalElem = document.getElementById('vacationTotal');
            const vacationRemainElem = document.getElementById('vacationRemain');

            if (vacationTotalElem && vacationRemainElem) {
                vacationTotalElem.textContent = `${data.count}`;
                vacationRemainElem.textContent = `${data.remainingVacationDays}`;
            }
        })
}
