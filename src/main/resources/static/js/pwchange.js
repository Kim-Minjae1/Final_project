		// 입력 시 다음으로
		const frontInput = document.getElementById('national_number_front');
		const midInput = document.getElementById('national_number_mid');
		const backInput = document.getElementById('national_number_back');
		const newPw = document.getElementById('new_password');
		
		
		frontInput.addEventListener('input', function() {
		    if (frontInput.value.length === 6) {
		        midInput.focus();
		    }
		});
		
		midInput.addEventListener('input', function() {
		    if (midInput.value.length === 1) {
		        backInput.focus();
		    }
		});
		
		backInput.addEventListener('input', function() {
		    if (backInput.value.length === 6) {
		        newPw.focus();
		    }
		});
		
		// 비밀번호 보이기 안보이기
		$(document).ready(function() {
			$('#pwHidden').on('click', function() {
				$(this).hide(); 
				$('#pwShow').show(); 
				$('#new_password').attr('type', 'text'); 
			});
		
			$('#pwShow').on('click', function() {
				$(this).hide(); 
				$('#pwHidden').show(); 
				$('#new_password').attr('type', 'password'); 
			});
		});
		
		// 비밀번호 변경 폼
		const pwChangeFrm = document.getElementById('pwchangeFrm');
		
		pwChangeFrm.addEventListener('submit',(e)=>{
			e.preventDefault();
			
			let vali_check = false;
			let vali_text = "";
			
			if(pwChangeFrm.user_id.value.trim() == ""){
				vali_text += '아이디(사번)을 입력하세요.';
				pwChangeFrm.user_id.focus();
			} else if(pwChangeFrm.national_number_front.value.trim() == ""){
				vali_text += '주민번호를 입력하세요.';
				pwChangeFrm.national_number_front.focus();
			} else if(pwChangeFrm.national_number_mid.value.trim() == ""){
				vali_text += '주민번호를 입력하세요.';
				pwChangeFrm.national_number_mid.focus();
			} else if(pwChangeFrm.national_number_back.value.trim() == ""){
				vali_text += '주민번호를 입력하세요.';
				pwChangeFrm.national_number_back.focus();
			} else if(pwChangeFrm.new_password.value.trim() == ""){
				vali_text += '새로운 비밀번호를 입력하세요.';
				pwChangeFrm.new_password.focus();
			} else {
				vali_check = true;
			}
			
			const csrfToken = document.getElementById('csrf_token').value;
			const userId = pwChangeFrm.user_id.value;
			const naNum1 = pwChangeFrm.national_number_front.value;
			const naNum2 = pwChangeFrm.national_number_back.value;
			const newpw = pwChangeFrm.new_password.value;
			if(vali_check == false){
				Swal.fire({
					icon : 'error',
					text : vali_text,
					confirmButtonColor: '#0056b3', 
					confirmButtonText : "확인"
				});
			} else{
				const formData = new FormData(pwChangeFrm);
				
				fetch('/pwchange',{
					method :'put',
					headers : {
						'X-CSRF-TOKEN' : csrfToken
					},
					body : formData
				})
				.then(response => response.json())
				.then(data => {
					if(data.res_code == '200'){
						Swal.fire({
						icon : 'success',
						text : data.res_msg,
						confirmButtonColor: '#0056b3', 
						confirmButtonText : "확인"
				}).then((result) => {
					location.href = ("/login");
				});
					} else if(data.res_code == '409'){
						Swal.fire({
						icon : 'error',
						text : data.res_msg,
						confirmButtonColor: '#0056b3', 
						confirmButtonText : "확인"
					}).then(() => {
						pwChangeFrm.user_id.value = '';
						pwChangeFrm.national_number_front.value = '';
						pwChangeFrm.national_number_mid.value = '';
						pwChangeFrm.national_number_back.value = '';
						pwChangeFrm.new_password.value = '';
				});
				} else{
						Swal.fire({
						icon : 'error',
						text : data.res_msg,
						confirmButtonColor: '#0056b3', 
						confirmButtonText : "확인"
					}).then(() => {
						pwChangeFrm.user_id.value = '';
						pwChangeFrm.national_number_front.value = '';
						pwChangeFrm.national_number_mid.value = '';
						pwChangeFrm.national_number_back.value = '';
						pwChangeFrm.new_password.value = '';
				});
				}
				
				})
			}
		})

	      document.addEventListener('keydown', function(event) {
	            const capsLockAlert = document.getElementById('capsLockAlert');
	            if (event.getModifierState && event.getModifierState('CapsLock')) {
	                capsLockAlert.style.display = 'block';
	            } else {
	                capsLockAlert.style.display = 'none'; 
	            }
	        });