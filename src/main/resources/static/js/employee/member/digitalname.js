document.addEventListener('DOMContentLoaded', function () {
const modifyBtn = document.getElementById('modifyBtn');
const saveBtn = document.getElementById('saveBtn');
const clearBtn = document.getElementById('clearBtn');
const digitalBox = document.querySelector('.digital_box');
const canvas = document.getElementById('canvas');
const ctx = canvas ? canvas.getContext('2d') : null;
const currentSignature = document.getElementById('currentSignature');
let painting = false;
let lastX = 0;
let lastY = 0;

function resizeCanvas() {
    if (canvas && digitalBox) {
        canvas.width = digitalBox.offsetWidth;
        canvas.height = digitalBox.offsetHeight;
    }
}

// 수정 버튼
if (modifyBtn) {
    modifyBtn.addEventListener('click', function () {
        modifyBtn.style.display = 'none';
        if (saveBtn) saveBtn.style.display = 'inline-block';
        if (clearBtn) clearBtn.style.display = 'inline-block';
        if (currentSignature) currentSignature.style.display = 'none';
        if (canvas) {
            canvas.style.display = 'block';
            resizeCanvas();
            setupCanvas();
        }
    });
}

if (canvas) {
    if (currentSignature) {
        canvas.style.display = 'none';
    } else {
        digitalBox.style.display = 'block';
        canvas.style.display = 'block';
        resizeCanvas();
        setupCanvas();
    }
}

function setupCanvas() {
    if (!ctx) return;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.strokeStyle = '#000000';

    ctx.beginPath(); 
    ctx.closePath();

    canvas.addEventListener('mousedown', startPainting);
    canvas.addEventListener('mousemove', draw);
    canvas.addEventListener('mouseup', stopPainting);
    canvas.addEventListener('mouseout', stopPainting);

    canvas.addEventListener('touchstart', handleStart, false);
    canvas.addEventListener('touchmove', handleMove, false);
    canvas.addEventListener('touchend', handleEnd, false);
}

function startPainting(e) {
    painting = true;
    [lastX, lastY] = [e.offsetX, e.offsetY];
}

function stopPainting() {
    painting = false;
}

function draw(e) {
    if (!painting) return;
    ctx.lineWidth = 10;
    ctx.beginPath();
    ctx.moveTo(lastX, lastY);
    ctx.lineTo(e.offsetX, e.offsetY);
    ctx.stroke();
    [lastX, lastY] = [e.offsetX, e.offsetY];
}

function handleStart(e) {
    e.preventDefault();
    const touch = e.touches[0];
    const mouseEvent = new MouseEvent("mousedown", {
        clientX: touch.clientX,
        clientY: touch.clientY
    });
    canvas.dispatchEvent(mouseEvent);
}

function handleMove(e) {
    e.preventDefault();
    const touch = e.touches[0];
    const mouseEvent = new MouseEvent("mousemove", {
        clientX: touch.clientX,
        clientY: touch.clientY
    });
    canvas.dispatchEvent(mouseEvent);
}

function handleEnd(e) {
    e.preventDefault();
    const mouseEvent = new MouseEvent("mouseup", {});
    canvas.dispatchEvent(mouseEvent);
}

// 지우기 버튼
if (clearBtn) {
    clearBtn.addEventListener('click', function (e) {
        e.preventDefault();
        clearCanvas();
    });
}

function clearCanvas() {
    if (ctx) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
}

if (saveBtn) {
    if (currentSignature) {
        saveBtn.style.display = 'none';
        clearBtn.style.display = 'none';
    } else {
        saveBtn.style.display = 'inline-block';
    }
}


window.addEventListener('resize', resizeCanvas);

resizeCanvas();
    
// 전자서명 등록
    const form = document.getElementById('digitalFrm');
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const payload = new FormData(form);
        const memberNo = form.member_no.value;
        const csrfToken = document.getElementById('csrf_token').value;
        const signatureDataUrl = canvas.toDataURL('image/png');

        payload.append('signatureData', signatureDataUrl);
        payload.append('strokeWidth', 10);

        fetch('/employee/member/digitalname/' + memberNo, {
            method: 'post',
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
                    confirmButtonText: '확인'
                }).then(() => {
                    location.href = "/employee/member/digitalname";
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    text: data.res_msg,
                    confirmButtonColor: '#0056b3', 
                    confirmButtonText: '확인'
                });
            }
        });
    });
});

const location_text = document.getElementById('header_location_text');
location_text.innerHTML = '마이페이지&emsp;&gt;&emsp;전자결재 서명 등록';