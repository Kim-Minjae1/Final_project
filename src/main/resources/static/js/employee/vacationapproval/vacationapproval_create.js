// editor

import {
    ClassicEditor,
    AccessibilityHelp,
    Autosave,
    Bold,
    Essentials,
    FontBackgroundColor,
    FontColor,
    FontFamily,
    FontSize,
    Heading,
    ImageBlock,
    ImageInline,
    ImageToolbar,
    Italic,
    Link,
    List,
    Paragraph,
    SelectAll,
    Strikethrough,
    Table,
    TableCaption,
    TableCellProperties,
    TableColumnResize,
    TableProperties,
    TableToolbar,
    Underline,
    Undo
} from 'ckeditor5';

const editorConfig = {
    toolbar: {
        items: [
            'undo',
            'redo',
            '|',
            'heading',
            '|',
            'fontSize',
            'fontFamily',
            'fontColor',
            'fontBackgroundColor',
            '|',
            'bold',
            'italic',
            'underline',
            'strikethrough',
            '|',
            'link',
            'insertTable',
            '|',
            'bulletedList',
            'numberedList'
        ],
        shouldNotGroupWhenFull: false
    },
    plugins: [
        AccessibilityHelp,
        Autosave,
        Bold,
        Essentials,
        FontBackgroundColor,
        FontColor,
        FontFamily,
        FontSize,
        Heading,
        ImageBlock,
        ImageInline,
        ImageToolbar,
        Italic,
        Link,
        List,
        Paragraph,
        SelectAll,
        Strikethrough,
        Table,
        TableCaption,
        TableCellProperties,
        TableColumnResize,
        TableProperties,
        TableToolbar,
        Underline,
        Undo
    ],
    fontFamily: {
        options: [
            '돋움, Dotum, sans-serif',
            '굴림, Gulim, sans-serif',
            '바탕, Batang, serif',
            '나눔고딕, NanumGothic, sans-serif'
        ],
        supportAllValues: true
    },
    fontSize: {
        options: [10, 12, 14, '16', 18, 20, 22],
        supportAllValues: true
    },
    heading: {
        options: [
            { model: 'paragraph', title: '굵기', class: 'ck-heading_paragraph' },
            { model: 'heading1', view: 'h1', title: '굵기 1', class: 'ck-heading_heading1' },
            { model: 'heading2', view: 'h2', title: '굵기 2', class: 'ck-heading_heading2' },
            { model: 'heading3', view: 'h3', title: '굵기 3', class: 'ck-heading_heading3' },
            { model: 'heading4', view: 'h4', title: '굵기 4', class: 'ck-heading_heading4' },
            { model: 'heading5', view: 'h5', title: '굵기 5', class: 'ck-heading_heading5' },
            { model: 'heading6', view: 'h6', title: '굵기 6', class: 'ck-heading_heading6' }
        ]
    },
    image: {
        toolbar: ['imageTextAlternative']
    },
    table: {
        contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
    }
};

// 시작 기간 선택 전 disable
document.addEventListener("DOMContentLoaded", function() {
	
const startDateInput = document.getElementById("vacationapproval_start_date");
const endDateInput = document.getElementById("vacationapproval_end_date");
const dateCountInput = document.getElementById("vacationapproval_date_count");

endDateInput.disabled = true;

let holidaysData = {};

function fetchHolidays(startDate) {
    const year = new Date(startDate).getFullYear();

    // 공휴일 API
    const xhr = new XMLHttpRequest();
    const url = 'http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo';
    const serviceKey = 'h8Gz1P5QVyOyAWHg7Cfs2N+UQCYcsX80WVLB8Za9h0fgYD4CRN5+9L/eVnb2nG85zzjN+0i/gkQ5VeUSTqlTfg=='; 
    let queryParams = '?' + encodeURIComponent('serviceKey') + '=' + encodeURIComponent(serviceKey);
    queryParams += '&' + encodeURIComponent('solYear') + '=' + encodeURIComponent(year);
    queryParams += '&' + encodeURIComponent('numOfRows') + '=' + encodeURIComponent('100');

    xhr.open('GET', url + queryParams);
    
    xhr.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(this.responseText, "text/xml");
                const items = xmlDoc.getElementsByTagName("item");

                for (let i = 0; i < items.length; i++) {
                    const locdate = items[i].getElementsByTagName("locdate")[0].textContent;
                    const dateName = items[i].getElementsByTagName("dateName")[0].textContent;

                    holidaysData[locdate] = dateName;
                }
                
            } else {
                console.error('오류 :', this.status);
            }
        }
    };

    xhr.send();
}

startDateInput.addEventListener("change", function() {
    if (startDateInput.value) {
        endDateInput.disabled = false; 
        fetchHolidays(startDateInput.value);
        calculateDateDifference();
    } else {
        endDateInput.disabled = true; 
        endDateInput.value = '';
        dateCountInput.value = ''; 
    }
});

endDateInput.addEventListener("change", function() {
    const startDate = new Date(startDateInput.value);
    const endDate = new Date(endDateInput.value);

    if (startDate && endDate && endDate < startDate) {
        Swal.fire({
            icon: 'warning',
            text: "종료일은 시작일보다 이전일 수 없습니다.",
            confirmButtonColor: '#0056b3',
            confirmButtonText: "확인"
        });
        endDateInput.value = '';
        dateCountInput.value = ''; 
    } else {
        calculateDateDifference(); 
    }
});


// 오늘 이전 날짜 막기 재설정
const today = new Date();
today.setDate(today.getDate());  
const minDate = today.toISOString().split("T")[0];

document.getElementById("vacationapproval_start_date").setAttribute("min", minDate);
document.getElementById("vacationapproval_end_date").setAttribute("min", minDate);




// 공휴일, 주말 제외
function calculateDateDifference() {
    const startDate = new Date(startDateInput.value);
    const endDate = new Date(endDateInput.value);

    if (startDate && endDate && endDate >= startDate) {
        let daysDifference = 0;
        
        for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
            const dayOfWeek = d.getDay();
            const locdate = d.toISOString().slice(0, 10).replace(/-/g, ''); 
            if (dayOfWeek !== 0 && dayOfWeek !== 6 && !holidaysData[locdate]) {
                daysDifference++;
            }
        }

        dateCountInput.value = daysDifference;
    } else {
        dateCountInput.value = '';
    }
}

    
       document.getElementById('vacationapproval_file').addEventListener('change', function (event) {
        const vacationFile = event.target.files[0]; 
        
        if (vacationFile) {
            const fileType = vacationFile.type;
            const validTypes = [
                'image/png', 
                'image/jpeg', 
                'application/pdf', 
                'application/vnd.ms-excel', 
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            ];

            if (!validTypes.includes(fileType)) {
                Swal.fire({
                    icon: 'error',
                    text: 'PNG, JPG, PDF, EXCEL만 등록 가능합니다.',
                    confirmButtonColor: '#0056b3',
                    confirmButtonText: '확인'
                });
                event.target.value = ''; 
            }
        }
    });
    
});



ClassicEditor.create(document.querySelector('#editor'), editorConfig)
    .then(editor => {
		
        editor.ui.view.editable.element.style.height = '500px';
			// 에디터에 휴가 양식 
            editor.setData(`
            <figure class="table" style="width:78.93%;"><table class="ck-table-resized"><colgroup><col style="width:18.8%;"><col style="width:81.2%;"></colgroup><tbody><tr><th>신청일</th><td>&nbsp;</td></tr><tr><th>신청자</th><td>&nbsp;</td></tr><tr><th>부서</th><td>&nbsp;</td></tr><tr><th>휴가 시작일</th><td>&nbsp;</td></tr><tr><th>휴가 종료일</th><td>&nbsp;</td></tr><tr><th>휴가 종류</th><td>&nbsp;</td></tr><tr><th>휴가 사유</th><td>&nbsp;</td></tr></tbody></table></figure><p>&nbsp;</p><figure class="table" style="width:78.93%;"><table class="ck-table-resized"><colgroup><col style="width:100%;"></colgroup><thead><tr><th>특이 사항</th></tr></thead><tbody><tr><td>&nbsp;</td></tr></tbody></table></figure>`);
			
			document.getElementById('previewButton').addEventListener('click', function() {
		    const editorContent = editor.getData();
		    
		    const approvalLineContent = document.getElementById('approvalLineTable').outerHTML;
		    const approval_title = document.getElementById('vacationapproval_title').value;
		    const title = document.querySelector('.vacation_title').outerHTML;
		    
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

						}
						#vacationapproval_title{
							font-size:30px;
							margin-left: 40px;
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
		    previewWindow.document.write(approvalLineContent); 
		    previewWindow.document.write('<hr class="section_separator">');
		    previewWindow.document.write('<div class="field">'+approval_title+'</div>');
		    previewWindow.document.write('<div class="editorPDF">' + editorContent + '</div>');    
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
			        const y = 10;
			
			        pdf.addImage(imgData, 'PNG', x, y, imgWidth, imgHeight); 
			        pdf.save('휴가문서.pdf');
			
			        downloadButton.style.display = 'block'; 
			    }).catch(error => {
			        console.error('PDF 생성 중 오류 발생:', error);
			        
			        downloadButton.style.display = 'block'; 
			    });
			};

		});


		//[김채영] 휴가 결재 pk
		let vacationApprovalPk = null;

			
			// 등록  폼
			const form = document.getElementById('vacAppCreateFrm');
			form.addEventListener('submit', (e) => {
	  		  e.preventDefault();
	    
		    const editorData = editor.getData();
		    const vacationapproval_title = document.querySelector('#vacationapproval_title').value;
		    const vacationtype = document.querySelector('select[name="vacationtype"]').value;
		    const csrfToken = document.querySelector('#csrf_token').value;
		    const memberNo = document.querySelector('#member_no').value;
		    const startDate = document.querySelector('#vacationapproval_start_date').value;
		    const endDate = document.querySelector('#vacationapproval_end_date').value;
		    const dateCount = document.querySelector('#vacationapproval_date_count').value;
		    const vacationFile = document.querySelector('#vacationapproval_file').files[0];
			const approvers = Array.from(document.querySelectorAll('input[id="approverNumbers"]')).map(input => input.value);
			const references = Array.from(document.querySelectorAll('input[id="referenceNumbers"]')).map(input => input.value);
			const reviewers = Array.from(document.querySelectorAll('input[id="reviewerNumbers"]')).map(input => input.value);
			
			const memVacationCount = document.querySelector('#member_vacation_count').value;
			console.log("console"+memVacationCount);
			console.log("dateCount"+dateCount);

		 	let vali_check = false;
            let vali_text = "";
			
            if (vacationapproval_title.trim() === "") {  
                vali_text += '결재 제목을 입력해주세요.';
                document.querySelector('#vacationapproval_title').focus();
            } else if(startDate.trim() === ""){
				 vali_text += '휴가 기간을 입력해주세요.';
                document.querySelector('#vacationapproval_start_date').focus();
			} else if(endDate.trim() === ""){
				 vali_text += '휴가 기간을 입력해주세요.';
                document.querySelector('#vacationapproval_end_date').focus();
			} else if(approvers.length === 0 && references.length === 0 && reviewers.length === 0 ){
				 vali_text += '결재선를 지정해주세요.';
                document.querySelector('#openChart').focus();
			}  else if(approvers.length === 0 && references.length === 0){
				 vali_text += '결재자 / 합의자를 지정해주세요.';
                document.querySelector('#openChart').focus();
			} else if(parseFloat(memVacationCount) < parseFloat(dateCount)){
				vali_text += '남은 휴가 개수가 선택된 기간보다 부족합니다.';
			} else {
                vali_check = true;
            }
			
			if (vali_check == false) {
                Swal.fire({
                    icon: 'warning',
                    text: vali_text,
                    confirmButtonColor: '#0056b3',
                    confirmButtonText: "확인"
                });
            } else {
				const payload = new FormData();
			    payload.append('vacationapprovalContent', editorData);
			    payload.append('vacationapprovalTitle', vacationapproval_title);
			    payload.append('vacationtype', vacationtype);
			    payload.append('memberNo', memberNo);
			    payload.append('startDate', startDate);
			    payload.append('endDate', endDate);
			    payload.append('dateCount', dateCount);
			    
			    if (vacationFile) {
           			payload.append('vacationFile', vacationFile);
        		}
			    payload.append('approvers', approvers);
			    payload.append('references', references);
			    payload.append('reviewers', reviewers);
			
			    fetch('/employee/vacationapproval/create', {
			        method: 'post',
			        body: payload,
			        headers: {
			            'X-CSRF-TOKEN': csrfToken
			        }
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
			                location.href = "/employee/vacationapproval/list";
			            });
			        } else {
			            Swal.fire({
			                icon: 'error',
			                text: data.res_msg,
			                confirmButtonColor: '#0056b3',
			                confirmButtonText: "확인"
			            });
			        }
			        vacationApprovalPk = data.approvalPk;
                    let notificationData = {};

                    if (approvers !== null) {
                        notificationData.approvers = approvers;
                    }

                    if (references !== null) {
                        notificationData.references = references;
                    }
                     console.log(vacationApprovalPk);
                    alarmSocket.send(JSON.stringify({
                       type: 'notificationVacationApproval',
                       notificationData : notificationData,
                       memberNo : memberNo,
                       vacationApprovalPk : vacationApprovalPk,
                       vacationapprovalTitle : vacationapproval_title
                    }));

                    alarmSocket.send(JSON.stringify({
                       type: 'notificationVacationApprovalReviewers',
                       reviewers : reviewers,
                       memberNo : memberNo,
                       vacationApprovalPk : vacationApprovalPk,
                       vacationapprovalTitle : vacationapproval_title
                    }));
			    });
			}
	});
});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '휴가&emsp;&gt;&emsp;휴가 신청';