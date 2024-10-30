////chatMember
//document.addEventListener("DOMContentLoaded", function () {
//
//
//// 확인 버튼
//    document.getElementById('confirmButton').addEventListener('click', function() {
//        const currentMemberNo = document.getElementById("currentMemberNo").value;
//        const csrfToken = document.querySelector('input[name="_csrf"]').value;
//        const currentMemberName = document.getElementById("currentMemberName").value;
//
//        fetch('/api/chat/memberAddRoom', {
//               method: 'POST',
//               headers: {
//                   'Content-Type': 'application/json',
//                   'X-CSRF-TOKEN': csrfToken
//               },
//               body: JSON.stringify({
//                   members: selectedMembers,
//                   currentMemberNo: currentMemberNo,
//                   names: selectNames,
//                   currentMemberName: currentMemberName
//               })
//           })
//           .then(response => response.json())
//           .then(data => {
//               console.log('서버 응답:', data);
//               // 웹소켓 다시 연결 (모달이 닫히면 이미 재연결되지만 필요시 추가)
//               if (webSocket && webSocket.readyState !== WebSocket.OPEN) {
//                   initWebSocket();
//               }
//           })
//           .catch(error => {
//               console.error('선택한 사원 저장 오류:', error);
//               Swal.fire({
//                   icon: 'error',
//                   title: '오류',
//                   text: '선택한 사원을 저장하는데 오류가 발생했습니다.',
//                   confirmButtonText: '확인',
//                   allowOutsideClick: false,
//                   allowEscapeKey: false,
//                   allowEnterKey: false
//               });
//           });
//       });
//
//    function updateChatList(updatedChatList) {
//        const chatListElement = document.getElementById('chatList');
//        chatListElement.innerHTML = '';
//
//        updatedChatList.forEach(chat => {
//            const chatItem = document.createElement('div');
//            chatItem.classList.add('chatItem');
//            chatItem.setAttribute('onclick', `handleChatRoomClick(${chat.chat_room_no})`);
//
//            const chatName = document.createElement('h3');
//            const chatNameText = document.createElement('p');
//            chatNameText.textContent = chat.chat_member_room_name;
//            chatName.appendChild(chatNameText);
//
//            chatItem.appendChild(chatName);
//
//            const memberNoInput = document.createElement('input');
//            memberNoInput.type = 'hidden';
//            memberNoInput.id = 'memberNo';
//            memberNoInput.value = chat.member_no;
//
//            const chatRoomNoInput = document.createElement('input');
//            chatRoomNoInput.type = 'hidden';
//            chatRoomNoInput.id = 'chatRoomNo';
//            chatRoomNoInput.value = chat.chat_room_no;
//
//            chatItem.appendChild(memberNoInput);
//            chatItem.appendChild(chatRoomNoInput);
//
//            chatListElement.appendChild(chatItem);
//        });
//    }
//
//});