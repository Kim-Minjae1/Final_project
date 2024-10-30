// 삭제 폼

const form = document.getElementById("appDeleteFrm");
const formTitle = document.getElementById("form_title").innerHTML;

form.addEventListener('submit', (e) => {
    e.preventDefault();
    const formNo = form.form_no.value;
    const csrfToken = document.getElementById("csrf_token").value;

    Swal.fire({
        text: '삭제하시겠습니까?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '##f8f9fa',
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
    }).then((result) => {
        if (result.isConfirmed) {  
            const payload = new FormData(form);

            fetch('/admin/approval/delete/' + formNo, {
                method: 'put',
                headers: {
                    'X-CSRF-TOKEN': csrfToken
                },
                body: payload
            })
            .then(response => response.json())
            .then(data => {
                if (data.res_code == '200') {
                    Swal.fire({
                        icon: 'success',
                        text: data.res_msg,
                        confirmButtonColor: '#0056b3',
                        confirmButtonText: "확인"
                    }).then((result) => {
                        location.href = "/admin/approval/form";
                    });
                } else {
                    Swal.fire({
                        icon: 'error',
                        text: data.res_msg,
                        confirmButtonColor: '#0056b3',
                        confirmButtonText: "확인"
                    });
                }
            });
        }
    });
});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '전자 결재 양식 관리&emsp;&gt;&emsp;결재 양식함&emsp;&gt;&emsp;'+formTitle;
