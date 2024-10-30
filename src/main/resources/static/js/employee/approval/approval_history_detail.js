const approvalTitle = document.querySelector('#approval_title').innerHTML;

// 승인 버튼 업데이트
function approveRequest(){
	 const csrfToken = document.querySelector('#csrf_token').value;
	 const approvalNo = document.querySelector('#approval_no').value;
	 
	fetch('/employee/approval/approve/'+approvalNo,{
		method: 'put',
		headers: {
			 'X-CSRF-TOKEN': csrfToken
		}
	})
	.then(reponse => reponse.json())
	.then(data => {
		if(data.res_code == '200'){
			Swal.fire({
				icon: 'success',
			    text: data.res_msg,
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: "확인"
			}).then((result) => {
				location.href = "/employee/approval/approval_history_detail/" + approvalNo;
			});
		}else{
			Swal.fire({
			     icon: 'error',
			     text: data.res_msg,
			     confirmButtonColor: '#0056b3',
			     confirmButtonText: "확인"
			});
		}
	})
	
    const appprovalMemberNo = document.getElementById('approval_member_no').value;
    const memberNo = document.getElementById('member_no').value;

	alarmSocket.send(JSON.stringify({
		type: 'notificationAppApprove',
		approvalNo : approvalNo,
		appprovalMemberNo :appprovalMemberNo,
		memberNo : memberNo
	}));
}


// 승인 취소 업데이트
function cancelApproval(){
	 const csrfToken = document.querySelector('#csrf_token').value;
	 	 const approvalNo = document.querySelector('#approval_no').value;
	 
	fetch('/employee/approval/approvecancel/'+approvalNo,{
		method: 'put',
		headers: {
			 'X-CSRF-TOKEN': csrfToken
		}
	})
	.then(reponse => reponse.json())
	.then(data => {
		if(data.res_code == '200'){
			Swal.fire({
				icon: 'success',
			    text: data.res_msg,
			    confirmButtonColor: '#0056b3',
			    confirmButtonText: "확인"
			}).then((result) => {
				location.href = "/employee/approval/approval_history_detail/" + approvalNo;
			});
		}else{
			Swal.fire({
			     icon: 'error',
			     text: data.res_msg,
			     confirmButtonColor: '#0056b3',
			     confirmButtonText: "확인"
			});
		}
	})
}

    const rejectModal = document.getElementById("rejectModal");
    const closeModal = document.querySelector(".close");

    closeModal.addEventListener('click', function() {
        rejectModal.style.display = "none";
    });
	
	function approvalReject(){
	     rejectModal.style.display = "flex";
	        
		// 반려 버튼 업데이트
		document.getElementById('confirm_reject_button').addEventListener('click', function( ){
			const csrfToken = document.querySelector('#csrf_token').value;
			const approvalNo = document.querySelector('#approval_no').value;
			const rejectReason = document.getElementById('reject_reason').value;
			 
			fetch('/employee/approval/reject/'+approvalNo,{
				method: 'put',
				headers: {
					'Content-Type': 'application/json',
					 'X-CSRF-TOKEN': csrfToken
				},
				body: JSON.stringify({
	                approval_flow_reject_reason: rejectReason
	            })
			})
			.then(reponse => reponse.json())
			.then(data => {
				if(data.res_code == '200'){
					Swal.fire({
						icon: 'success',
					    text: data.res_msg,
					    confirmButtonColor: '#0056b3',
					    confirmButtonText: "확인"
					}).then((result) => {
						location.href = "/employee/approval/approval_history_detail/" + approvalNo;
					});
				}else{
					Swal.fire({
					     icon: 'error',
					     text: data.res_msg,
					     confirmButtonColor: '#0056b3',
					     confirmButtonText: "확인"
					});
				}
			});
			
    const appprovalMemberNo = document.getElementById('approval_member_no').value;
    const memberNo = document.getElementById('member_no').value;

	alarmSocket.send(JSON.stringify({
		type: 'notificationAppReject',
		approvalNo : approvalNo,		
		appprovalMemberNo :appprovalMemberNo,
		memberNo : memberNo
	}));			
		});
	}
	
	document.addEventListener('DOMContentLoaded', function() {

    const previewButton = document.getElementById('previewButton');
    
        previewButton.addEventListener('click', function () {

    // 미리볼 내용
	const approvalLineTable = document.querySelector('.approval_line_table').outerHTML.replace(/<button[^>]*>(.*?)<\/button>/g, '');
    const title = document.querySelector('#approval_title').outerHTML;
    const contentSection = document.querySelector('.content_section').outerHTML;

    // HTML 구조
   			var windowW = 1000;
		    var windowH = 900;
		    var winHeight = document.body.clientHeight;
		    var winWidth = document.body.clientWidth - 500;
		    var winX = window.screenX || window.screenLeft || 500;
		    var winY = window.screenY || window.screenTop || 0;
		    var popX = winX + (winWidth - windowW) / 2;
		    var popY = winY + (winHeight - windowH) / 2;
		    
		    const previewWindow = window.open('', '미리보기', "width=" + windowW + ", height=" + windowH + ", scrollbars=no, menubar=no, top=" + popY + ", left=" + popX);
		    previewWindow.document.write('<html><head><title>휴가결재 미리보기</title>');
		    
		    previewWindow.document.write(`
		        <style>
			            html, body {
			                justify-content: center;
			                display: flex;
			                align-content: center;
			                margin: 0;
			                padding: 0;
			                align-items: center;
			            }
			            .title{
						    display: flex;
						    justify-content: space-between;
						    align-items: center;
						    margin: auto;
						    margin-bottom: 20px						
						}
						.approval_line_table {
						    border-collapse: collapse;
						    width: 75%; 
						    margin-left: auto; 
						}		

						.approval_line_table th {
						    background-color: #f2f2f2;
						    font-weight: bold;
						    width: 10%; 
						    border: 1px solid #ccc;
						    padding: 5px; 
						    text-align: center;
						    vertical-align: middle;
						    font-size: 12px; 
						}
						
						.approval_line_table td {
						    width: 100px; 
						    border: 1px solid #ccc;
						    padding: 5px; 
						    text-align: center;
						    vertical-align: middle;
						    font-size: 12px; 
						}
						
						.signature_box {
						    height: 40px; 
						    width: 50px;
						    align-content: center;
						    margin: auto;
						}
						
						.signature_box img, .signature_box .currentSignature {
						    max-height: 25px;
						}
						
						.date_box {
						    font-size: 8px; 
						    width: 50px;
						    margin: auto;
						}
						
						.approval_status {
						    font-size: 12px; 
						    padding: 1px 3px; 
						}
						
						
						.reference_box {
						    display: flex;
						    flex-wrap: wrap;
						    gap: 10px;
						}
						.approval_status.approved {
						    background-color: #e6f3e6;
						    color: #4CAF50;
						}
						
						.approval_status {
						    font-size: 13px;
						    font-weight: bold;
						    padding: 2px 5px;
						    border-radius: 3px;
						    display: inline-block;
						}
						
						.approved {
						    background-color: #e6f3e6;
						    color: #4CAF50;
						}
						
						.pending {
						    background-color: #fff8e1;
						    color: #FFC107;
						}
						
						.rejected {
						    background-color: #ffebee;
						    color: #F44336;
						}
						
						.reference_box > div {
						    background-color: #f2f2f2;
						    padding: 5px 10px;
						    border-radius: 3px;
						    font-size: 10px;
						}

			            .ck-table-resized {
			                width: 100%;
			                border-collapse: collapse;
			                margin-bottom: 20px;
			            }
			            .ck-table-resized th, .ck-table-resized td {
			                border: 1px solid #ccc;
			                padding: 10px;
			                text-align: center;
			                vertical-align: middle;
			            }
			            .ck-table-resized th {
			                background-color: #f2f2f2;
			                font-weight: bold;
			            }
			            .preview_div{
			                padding: 20px;
			                width: 800px;
			            }
			            .download_button {
							float: right;
						    padding: 8px 15px;
						    background-color: #04AA6D;
						    border: none;
						    border-radius: 5px;
						    cursor: pointer;
						    text-decoration: none;
						    font-size: 14px;
						    color: white;
			            }
			            .download_button:hover{
							opacity: 0.8;
						}
			            .content_section{
						    margin: auto;
	    					width: 780px;
						    margin-left: 10px;
						}
						.reference_box {
						    display: flex;
						    flex-wrap: wrap;
						    gap: 10px;
						}
			            #strong {
			                background-color: #f2f2f2;
			            }						
						.reference_box > div {
						    background-color: #f2f2f2;
						    padding: 5px 10px;
						    border-radius: 3px;
						    font-size: 14px;
						}
		        </style>
		    `);
		    
		    previewWindow.document.write(`
		        <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
		        <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.0/jspdf.umd.min.js"></script>
		    `);
		    previewWindow.document.write('</head><body>');
		    previewWindow.document.write('<div class="preview_div">');
		    previewWindow.document.write('<div class="solid">');
			previewWindow.document.write('<div class="title">');
			previewWindow.document.write('<h2>'+title+'</h2>');
			previewWindow.document.write('<button class="download_button" onclick="downloadPDF()">PDF 다운로드</button>');
			previewWindow.document.write('</div>');
		    previewWindow.document.write(approvalLineTable); 
		    previewWindow.document.write('<hr class="section_separator">');
		    previewWindow.document.write(contentSection);    
		    previewWindow.document.write('</div>');
		    previewWindow.document.write('</div>');
		    previewWindow.document.write('</body></html>');
		    previewWindow.document.close();
		
		   previewWindow.downloadPDF = function() {
			
			    const downloadButton = previewWindow.document.querySelector('.download_button');
				const solid = previewWindow.document.querySelector('.solid');
			    downloadButton.style.display = 'none'; 
			    solid.style.border = 'none';
			
			    previewWindow.html2canvas(previewWindow.document.querySelector('.preview_div'), { scale: 2 })
			    .then(canvas => {
			        const imgData = canvas.toDataURL('image/png');
			        const { jsPDF } = previewWindow.jspdf;
			        const pdf = new jsPDF({
			            orientation: 'portrait',
			            unit: 'mm',
			            format: 'a4'
			        });
			        const imgWidth = 190; 
			        const imgHeight = canvas.height * imgWidth / canvas.width; 
			
			        const x = (pdf.internal.pageSize.getWidth() - imgWidth) / 2; 
			        const y = (pdf.internal.pageSize.getHeight() - imgHeight) / 2; 
			
			        pdf.addImage(imgData, 'PNG', x, y, imgWidth, imgHeight); 
			        pdf.save('결재문서.pdf');
			
			        downloadButton.style.display = 'block'; 
			    }).catch(error => {
			        console.error('PDF 생성 중 오류 발생:', error);
			        
			        downloadButton.style.display = 'block'; 
			    });
			};

		});
		
		window.functionTypes = [7, 9];
			const approvalNo = document.querySelector('#approval_no').value;

			if (window.functionTypes.includes(7) || window.functionTypes.includes(9)) {
				markApprovalAsRead(7, approvalNo);
				markApprovalAsRead(9, approvalNo);
			}

});


const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '전자결재&emsp;&gt;&emsp;결재 수신함&emsp;&gt;&emsp;결재 내역함&emsp;&gt;&emsp;'+approvalTitle;		