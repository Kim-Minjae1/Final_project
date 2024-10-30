document.addEventListener("DOMContentLoaded", function() {
    const vacationMemberNo = document.getElementById('attendance_memberNo').value;

    fetch(`/user/vacationCount/${vacationMemberNo}`)
        .then(response => response.json())
        .then(data => {
              const vacationDaysElement = document.getElementById('vacationRemainCount');
              vacationDaysElement.textContent = `${data.remainingVacationDays}일`;

               const vacationCount = document.getElementById('vacationCount');
               vacationCount.textContent = `${data.count}일`;
        }) 
});