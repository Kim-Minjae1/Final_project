document.addEventListener('DOMContentLoaded', function() {
    const location_text = document.getElementById('header_location_text');
    location_text.innerHTML = '휴가관리&emsp;&gt;&emsp;휴가 기준';

    const vacationForm = document.getElementById('vacationStandard');
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const designatedCheckbox = document.getElementById('designated');
    const joinedCheckbox = document.getElementById('joined');
    const designatedDateInput = document.getElementById('designatedDate');

    vacationForm.addEventListener('submit', function(event) {
        event.preventDefault();

        let formData = {};

        if (!designatedCheckbox.checked && !joinedCheckbox.checked) {
            Swal.fire({
                icon: 'warning',
                text: '지정일 또는 입사일 기준을 선택해 주세요.',
                confirmButtonText: "확인",
                customClass: {
                    confirmButton: 'custom-confirm-button'
                }
            });
            return;
        }

        if (designatedCheckbox.checked && !designatedDateInput.value) {
            Swal.fire({
                icon: 'warning',
                text: '지정일 날짜를 입력해 주세요.',
                confirmButtonText: "확인",
                customClass: {
                    confirmButton: 'custom-confirm-button'
                }
            });
            return;
        }

        if (designatedCheckbox.checked) {
            formData = {
                type: 'designated',
                designatedDate: designatedDateInput.value
            };
        } else if (joinedCheckbox.checked) {
            formData = {
                type: 'joined'
            };
        }

        fetch('/vacation/checkStandard', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버가 응답하지 않음');
            }
            return response.json();
        })
        .then(data => {
            console.log(data);
            if (data.res_code === '200') {
                Swal.fire({
                    icon: 'success',
                    text: data.res_msg,
                    confirmButtonText: "확인",
                    customClass: {
                        confirmButton: 'custom-confirm-button'
                    }
                }).then((result) => {
                    if (result.isConfirmed) {
                        location.reload();
                    }
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    text: data.res_msg,
                    confirmButtonColor: '#0056b3',
                    confirmButtonText: "확인"
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                text: '서버와의 통신 중 오류가 발생했습니다.',
                confirmButtonText: "확인",
                customClass: {
                    confirmButton: 'custom-confirm-button'
                }
            });
        });
    });

    designatedCheckbox.addEventListener('change', function() {
        if (designatedCheckbox.checked) {
            joinedCheckbox.checked = false;
            designatedDateInput.disabled = false;
        } else {
            designatedDateInput.disabled = true;
        }
    });

    joinedCheckbox.addEventListener('change', function() {
        if (joinedCheckbox.checked) {
            designatedCheckbox.checked = false;
            designatedDateInput.disabled = true;
        }
    });

    designatedDateInput.disabled = true;
});
