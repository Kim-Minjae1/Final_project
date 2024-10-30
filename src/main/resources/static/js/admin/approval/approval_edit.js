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

// 수정 폼
            const approvalTitle = document.querySelector('#approval_title').value;
ClassicEditor.create(document.querySelector('#editor'), editorConfig)
    .then(editor => {
        editor.ui.view.editable.element.style.height = '500px';

        document.querySelector('.submit_button').addEventListener('click', (e) => {
            const formNo = document.querySelector('#form_no').value;
            const editorData = editor.getData();
            const csrfToken = document.querySelector('#csrf_token').value;
				
				 let vali_check = false;
            let vali_text = "";

            if (approvalTitle.trim() === "") {  
                vali_text += '양식 이름을 입력해주세요.';
                document.querySelector('#approval_title').focus();
            } else if (editorData.trim() === "") {
                vali_text += '양식 입력해주세요.';
                editor.ui.view.editable.element.focus();  
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
				
			}else{
                const formData = new FormData();
                formData.append('form_no', formNo);
                formData.append('approval_title', approvalTitle);
                formData.append('editor_content', editorData);
                formData.append('csrf', csrfToken);

                fetch('/admin/approval/edit', {
                    method: 'put',
                    body: formData,
                    headers: {
                        'Accept': 'application/json',
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
                            location.href = "/admin/approval/detail/"+formNo;
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
location_text.innerHTML = '전자 결재 양식 관리&emsp;&gt;&emsp;결재 양식 수정&emsp;&gt;&emsp;'+approvalTitle;