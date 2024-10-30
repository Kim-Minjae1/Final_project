const memberName = document.getElementById('member_name').innerHTML;


function previewImage(event) {
    const file = event.target.files[0];

    if (file) {
        const fileType = file.type;
        const validTypes = ['image/png', 'image/jpeg'];

        if (validTypes.includes(fileType)) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = document.getElementById('profileImg');
                img.src = e.target.result;
            }

            reader.readAsDataURL(file);
        } else {
            Swal.fire({
                icon: 'warning',
                text: 'PNG 또는 JPG 파일만 등록 가능합니다.',
                confirmButtonColor: '#0056b3',
                confirmButtonText: '확인'
            });

            document.getElementById('profile_img').value = "";
        }
    }
}

// 전화번호 
document.addEventListener('DOMContentLoaded', function() {
    const mobile1 = document.getElementById('mobile1');
    const mobile2 = document.getElementById('mobile2');
    const mobile3 = document.getElementById('mobile3');
	const internal = document.getElementById('internal');
	
    mobile1.addEventListener('input', function() {
        if (mobile1.value.length === 3) {
            mobile2.focus();
        }
    });

    mobile2.addEventListener('input', function() {
        if (mobile2.value.length === 4) {
            mobile3.focus();
        }
    });
    
    mobile3.addEventListener('input',function(){
		if(mobile3.value.length === 4){
			internal.focus();
		}
	});
	
    const restrictInput = (inputElement) => {
        inputElement.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '');
        });
    };

    restrictInput(mobile1);
    restrictInput(mobile2);
    restrictInput(mobile3);
    restrictInput(internal);
});


const editFrm = document.getElementById('editFrm');

editFrm.addEventListener('submit',(e)=>{
	e.preventDefault();
	
	const csrfToken = document.getElementById("csrf_token").value;	
	const memberNo = document.getElementById("member_no").value;	
	
	let vali_check = false;
	let vali_text ="";	
	
	if(editFrm.mobile2.value.trim() == "" || editFrm.mobile2.value.length < 4){
		vali_text += '전화번호 두번째 부분을 4자리로 입력해주세요.';
		editFrm.focus();
	 } else if(editFrm.mobile3.value.trim() =="" || editFrm.mobile3.value.length < 4){
		vali_text += '전화번호 세번째 부분을 4자리로 입력해주세요.';
		editFrm.focus();
	 } else {
		vali_check = true;
	 }
	 
	 if(vali_check == false){
		Swal.fire({
			icon : 'warning',
			text : vali_text,
			confirmButtonColor: '#0056b3',
			confirmButtonText : "확인"
		});
	} else{
	const formData = new FormData(editFrm);
	fetch('/admin/member/edit/'+memberNo,{
		method:'put',
		headers :{
			'X-CSRF-TOKEN':csrfToken
		},
		body: formData
	})
	  .then(response => response.json())
        .then(data => {
			if(data.res_code == '200'){
				Swal.fire({
				icon : 'success',
				text : data.res_msg,
				confirmButtonColor: '#0056b3',
				confirmButtonText : "확인"
		}).then((result)=>{
			location.href = "/admin/member/detail/"+memberNo;
		});
			}else if(data.res_code == '409'){
				Swal.fire({
				icon : 'warning',
				text : data.res_msg,
				confirmButtonColor: '#0056b3',
				confirmButtonText : "확인"
			});	
			}else {
				Swal.fire({
				icon : 'error',
				text : data.res_msg,
				confirmButtonColor: '#0056b3',
				confirmButtonText : "확인"
				});
			}
		})
	}})
	
const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '사원 관리&emsp;&gt;&emsp;사원 수정&emsp;&gt;&emsp;'+memberName;