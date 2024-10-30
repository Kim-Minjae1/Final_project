		$(document).ready(function() {
			$('#pwHidden').on('click', function() {
				$(this).hide(); 
				$('#pwShow').show(); 
				$('#member_pw').attr('type', 'text'); 
			});
		
			$('#pwShow').on('click', function() {
				$(this).hide(); 
				$('#pwHidden').show(); 
				$('#member_pw').attr('type', 'password'); 
			});
		});
		
		// 로그인 유효성
		document.addEventListener('DOMContentLoaded', function () {
		    const loginForm = document.getElementById('login_form');
		    const memberNumberInput = document.getElementById('member_number');
		    const memberPwInput = document.getElementById('member_pw');
		
		    loginForm.addEventListener('submit', function (event) {
		        let vali_check = false;
		        let vali_text = "";
		
		        if (memberNumberInput.value.trim() === "") {
		            vali_text = '아이디(사번)을 입력하세요.';
		            memberNumberInput.focus();
		        }
		        else if (memberPwInput.value.trim() === "") {
		            vali_text = '비밀번호를 입력하세요.';
		            memberPwInput.focus();
		        } else {
		            vali_check = true;
		        }
		
		        if (vali_check == false) {
					Swal.fire({
						icon : 'error',
						text : vali_text,
						confirmButtonColor: '#0056b3', 
						confirmButtonText : "확인"
					});
					 event.preventDefault();
		        }
		    });
		});

	      document.addEventListener('keydown', function(event) {
	            const capsLockAlert = document.getElementById('capsLockAlert');
	            if (event.getModifierState && event.getModifierState('CapsLock')) {
	                capsLockAlert.style.display = 'block';
	            } else {
	                capsLockAlert.style.display = 'none'; 
	            }
	        });
