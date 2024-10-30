// 연차 개수 제출 시 처리
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById("vacationForm");
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    form.addEventListener('submit', function(event) {
        event.preventDefault();

        Swal.fire({
            text: '입력되지 않은 연차의 개수는 0으로 지정됩니다. 계속하시겠습니까?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '확인',
            cancelButtonText: '취소',
            customClass: {
                 confirmButton: 'custom-confirm-button',
                 cancelButton: 'custom-cancel-button'
            }
        }).then((result) => {
            if (result.isConfirmed) {
                // 사용자가 확인했을 때 처리 진행
                const memberNo = document.getElementById('memberNo').value;
                const yearInputs = document.querySelectorAll('[id^="year"]');
                const countVacation = document.getElementById('countVacation').value;
                const vacationPkElements = document.querySelectorAll('[id^="vacationPk"]');

                let count = 1;
                const vacationData = {};
                const vacationPkData = [];

                if (countVacation > 0) {
                    vacationPkElements.forEach(input => {
                        if (input.value) {
                            vacationPkData.push(input.value);
                        }
                    });
                    yearInputs.forEach(input => {
                        const year = input.id.replace('year', '');
                        const vacationDays = input.value;

                        // 0으로 지정
                        vacationData[count] = vacationDays.trim() === "" ? 0 : parseInt(vacationDays);
                        count++;
                    });

                    const requestData = {
                        memberNo: memberNo,
                        vacationData: vacationData,
                        lessThanOneYear: lessThanOneYear,
                        countVacation: countVacation,
                        count: count,
                        vacationPkData: vacationPkData
                    };

                    fetch('/vacation/addVacationAction', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-CSRF-TOKEN': csrfToken
                        },
                        body: JSON.stringify(requestData)
                    })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        return response.json();
                    })
                    .then(data => {
                        if (data.res_code === '200') {
                            Swal.fire({
                                icon: 'success',
                                text: data.res_msg,
                                confirmButtonText: "닫기",
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
                                confirmButtonText: "닫기",
                                customClass: {
                                     confirmButton: 'custom-confirm-button'

                                }
                            });
                        }
                    })
                    .catch(error => {
                        Swal.fire({
                            icon: 'error',
                            text: '서버와의 통신 중 오류가 발생했습니다.',
                            confirmButtonText: "닫기",
                            customClass: {
                                confirmButton: 'custom-confirm-button'
                            }
                        });
                    });
                } else {
                    yearInputs.forEach(input => {
                        const year = input.id.replace('year', '');
                        const vacationDays = input.value;

                        vacationData[year] = vacationDays.trim() === "" ? 0 : parseInt(vacationDays);
                    });

                    const requestData = {
                        memberNo: memberNo,
                        vacationData: vacationData,
                        lessThanOneYear: lessThanOneYear,
                        countVacation: countVacation
                    };

                    fetch('/vacation/addVacationAction', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-CSRF-TOKEN': csrfToken
                        },
                        body: JSON.stringify(requestData)
                    })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        return response.json();
                    })
                    .then(data => {
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
                                confirmButtonText: "확인",
                                customClass: {
                                      confirmButton: 'custom-confirm-button'

                                }
                            });
                        }
                    })
                    .catch(error => {
                        Swal.fire({
                            icon: 'error',
                            text: '서버와의 통신 중 오류가 발생했습니다.',
                            confirmButtonText: "닫기",
                            customClass: {
                                 confirmButton: 'custom-confirm-button'

                            }
                        });
                    });
                }
            }
        });
    });
});


//휴가 종류 생성
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById("vacationTypeForm");
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    form.addEventListener('submit', function(event) {
        event.preventDefault();
        
        const vacationType = document.getElementById('vacationType').value.trim();
        const vacationValue = document.getElementById('vacationValue').value.trim();

        if (!vacationType || !vacationValue) {
            Swal.fire({
                icon: 'warning',
                text: '모든 필드를 입력해주세요.',
                confirmButtonText: "확인",
                customClass: {
                     confirmButton: 'custom-confirm-button'

                }
            });
            return;
        }

        fetch('/vacation/checkVacationTypeExists', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ vacationType: vacationType })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 불가');
            }
            return response.json();
        })
        .then(data => {
            if (data.exists) {
                Swal.fire({
                    icon: 'warning',
                    text: '이미 존재하는 휴가 종류입니다. 다른 이름을 입력해주세요.',
                     confirmButtonText: "확인",
                     customClass: {
                          confirmButton: 'custom-confirm-button'

                     }
                });
            } else {
                console.log(vacationType);
                 console.log(vacationValue);
                const requestData = {
                    vacationType: vacationType,
                    vacationValue: vacationValue
                };

                fetch('/vacation/addTypeVacation', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: JSON.stringify(requestData)
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.res_code === '200') {
                        Swal.fire({
                            icon: 'success',
                            text: '휴가 종류가 추가되었습니다.',
                            confirmButtonText: "확인",
                            customClass: {
                                 confirmButton: 'custom-confirm-button'

                            }
                        }).then(() => {
                            location.reload();
                        });
                    } else {
                        Swal.fire({
                            icon: 'error',
                            text: data.res_msg,
                            confirmButtonText: "확인",
                            customClass: {
                                confirmButton: 'custom-confirm-button'

                            }
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
});


document.addEventListener('DOMContentLoaded', function () {
    // 수정 버튼 클릭 이벤트 처리
    document.querySelectorAll('.edit-btn').forEach(function (button) {
        button.addEventListener('click', function () {
            const row = this.closest('tr'); // 클릭된 버튼이 속한 기본 행
            const editRow = row.nextElementSibling; // 숨겨진 수정용 행

            // 기본 행 숨기기
            row.style.display = 'none';

            // 수정 행 보이기
            editRow.style.display = 'table-row';

            editRow.querySelector('.cancel-btn').addEventListener('click', function () {
                // 기본 행 다시 보이기
                row.style.display = 'table-row';

                // 수정 행 숨기기
                editRow.style.display = 'none';
            });


        });
    });
});
//휴가 카테고리 수정
document.addEventListener('DOMContentLoaded', function () {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    document.querySelectorAll('.save-btn').forEach(function (button) {
        button.addEventListener('click', function () {

            const form = button.closest('form');
            if (!form) return;

            const vacationTypeNo = form.querySelector('#editVacationTypeNo').value;
            const vacationTypeName = form.querySelector('#editVacationTypeName').value;
            const vacationTypeCalculate = form.querySelector('#editVacationTypeCalculate').value;

            const dataToSend = {
                vacationTypeNo: vacationTypeNo,
                vacationTypeName: vacationTypeName,
                vacationTypeCalculate: vacationTypeCalculate
            };

            fetch('/vacation/updateVacation', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(dataToSend)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 불가');
                }
                return response.json();
            })
            .then(data => {
                if (data.res_code === '200') {
                    Swal.fire({
                        icon: 'success',
                        text: data.res_msg,
                        confirmButtonText: "닫기",
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
                        confirmButtonText: "확인",
                        customClass: {
                            confirmButton: 'custom-confirm-button'

                        }
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
    });
});


/*삭제버튼-상태값 변화*/
document.addEventListener('DOMContentLoaded', function () {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    document.querySelectorAll('.delete-btn').forEach(function (button) {
        button.addEventListener('click', function () {
            const row = button.closest('tr');
            if (!row) return;

            const vacationTypeNo = row.querySelector('#vacationTypeNo').value;
            const vacationTypeName = row.querySelector('#vacationTypeName').value;
            const vacationTypeCal = row.querySelector('#vacationTypeCal').value;

            fetch('/vacation/deleteVacation', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({
                    vacationTypeNo : vacationTypeNo,
                    vacationTypeName : vacationTypeName,
                    vacationTypeCal : vacationTypeCal
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 불가');
                }
                return response.json();
            })
            .then(data => {
                if (data.res_code === '200') {
                    Swal.fire({
                        icon: 'success',
                        text: data.res_msg,
                        confirmButtonText: "닫기",
                        customClass: {
                            confirmButton: 'custom-confirm-button'

                        }
                    }).then((result) => {
                        if (result.isConfirmed) {
                            row.remove();
                        }
                    });
                } else {
                    Swal.fire({
                        icon: 'error',
                        text: data.res_msg,
                        confirmButtonText: "닫기",
                        customClass: {
                            confirmButton: 'custom-confirm-button'

                        }
                    });
                }
            })
            .catch(() => {
                Swal.fire({
                    icon: 'error',
                    text: '서버와의 통신 중 오류가 발생했습니다.',
                    confirmButtonText: "닫기",
                    customClass: {
                        confirmButton: 'custom-confirm-button'

                    }
                });
            });
        });
    });
});

//1년미만 여부
document.addEventListener('DOMContentLoaded', function () {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    document.getElementById('oneUnderForm').addEventListener('submit', function(event) {
        event.preventDefault();

        const isChecked = document.getElementById('lessThanOneYear').checked;

        if (!isChecked) {
            Swal.fire({
                text: '1년 미만 월차 지급 여부를 선택하지 않으셨습니다.',
                icon: 'warning',
                confirmButtonText: '확인',
                customClass: {
                     confirmButton: 'custom-confirm-button'
                }
            });
            return;
        }

        fetch('/vacation/checkOneYear', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                isChecked: isChecked
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 불가');
            }
            return response.json();
        })
        .then(data => {
            console.log(data);
            if (data.res_code === '200') {
                Swal.fire({
                    icon: 'success',
                    text: data.res_msg,
                    confirmButtonText: "닫기",
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
                    confirmButtonText: "닫기",
                    customClass: {
                        confirmButton: 'custom-confirm-button'

                    }
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                text: '서버와의 통신 중 오류가 발생했습니다.',
                confirmButtonText: "닫기",
                customClass: {
                    confirmButton: 'custom-confirm-button'

                }
            });
        });
    });
});


const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '휴가관리&emsp;&gt;&emsp;휴가 생성';