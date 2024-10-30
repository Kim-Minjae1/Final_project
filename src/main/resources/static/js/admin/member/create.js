// 주민등록번호 

const frontInput = document.getElementById('national_number_front');
const backInput = document.getElementById('national_number_back');

frontInput.addEventListener('input', function() {
	if (frontInput.value.length === 6) {
		backInput.focus();
	}
	
    const restrictInput = (inputElement) => {
        inputElement.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, ''); 
        });
    };

    restrictInput(frontInput);
    restrictInput(backInput);	
});

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

// 이미지 등록 
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
                text: 'PNG 또는 JPEG 파일만 등록 가능합니다.',
                confirmButtonColor: '#0056b3',
                confirmButtonText: '확인'
            });

            document.getElementById('profile_image').value = "";
        }
    }
}

// 유효성 검사 및 등록

const createFrm = document.getElementById('createMemberFrm');

createFrm.addEventListener('submit',(e)=>{
	e.preventDefault();
	
	let vali_check = false;
	let vali_text ="";
	
	if(createFrm.profile_image.value.trim() == ""){
		vali_text += '이미지를 등록해주세요.';
		createFrm.profile_image.focus();
	} else if(createFrm.name.value == ""){
		vali_text += '사원명을 입력해주세요.';
		createFrm.name.focus();
	} else if(createFrm.national_number_front.value.trim() == ""){
		vali_text += '주민번호 앞자리를 입력해주세요.';
		national_number_front.focus();
	 } else if(createFrm.national_number_back.value.trim() == ""){
		vali_text += '주민번호 뒷자리를 입력해주세요';
		national_number_back.focus();
	 } else if(createFrm.hire_date.value.trim() == ""){
		vali_text += '입사일을 지정해주세요.';
		hire_date.focus();
	 } else if(createFrm.mobile2.value.trim() == "" || createFrm.mobile2.value.length < 4){
		vali_text += '전화번호 두번째 부분을 4자리로 입력해주세요.';
		mobile2.focus();
	 } else if(createFrm.mobile3.value.trim() =="" || createFrm.mobile3.value.length < 4){
		vali_text += '전화번호 세번째 부분을 4자리로 입력해주세요.';
		mobile3.focus();
	 } else if(createFrm.internal.value.trim() == ""){
		vali_text += '내선번호를 입력해주세요.';
		internal.focus();
	 } else {
		vali_check = true;
	 }
	 
	 const csrfToken = document.getElementById("csrf_token").value;
	 
	 if(vali_check == false){
		Swal.fire({
			icon : 'warning',
			text : vali_text,
			confirmButtonColor: '#0056b3',
			confirmButtonText : "확인"
		});
	 } else{
		const formData = new FormData(createFrm);
        fetch('/admin/member/create', {
            method: 'post',
            headers : {
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
			location.href = "/admin/member/list";
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
location_text.innerHTML = '사원 관리&emsp;&gt;&emsp;사원 등록';
