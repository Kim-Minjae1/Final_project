document.addEventListener("DOMContentLoaded", function() {

const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');
document.getElementById('header_location_text').style.display = 'none';

if (sendButton && messageInput) {
    function toggleSendButton() {
        sendButton.disabled = messageInput.value.trim() === '';
        if (messageInput.value.trim() !== '') {
            sendButton.classList.remove("btn-disabled");
        } else {
            sendButton.classList.add("btn-disabled");
        }
    }

    messageInput.addEventListener('input', toggleSendButton);
    toggleSendButton();
} else {
    console.error("버튼 또는 입력 필드를 찾을 수 없습니다.");
}

    // 채팅방 자동완성 기능
    document.getElementById('searchInput').oninput = searchMem;
    function searchMem() {
        let input = document.getElementById('searchInput').value.toLowerCase().replace(/\s+/g, '');
        let chatItems = document.getElementsByClassName('chatItem');
        for (let i = 0; i < chatItems.length; i++) {
            let chatName = chatItems[i].getElementsByTagName('p')[0].innerText.toLowerCase().replace(/\s+/g, '');
            chatItems[i].style.display = chatName.includes(input) ? "" : "none";
        }
    }
    window.toggleSearch = function() {
        let searchContainer = document.getElementById("searchContainer");
        let searchChatButton = document.getElementById("searchChatButton");
        if (searchContainer.style.display === "none") {
            searchContainer.style.display = "flex";
            searchChatButton.classList.remove("btn-primary");
            searchChatButton.classList.add("btn-secondary");
        } else {
            searchContainer.style.display = "none";
            searchChatButton.classList.remove("btn-secondary");
            searchChatButton.classList.add("btn-primary");
            searchInput.value = '';
           searchMem();
        }
    };
       function closeEditChat() {
           const editChatRoomModal = document.getElementById('editChatRoomModal');
          $('#editChatRoomModal').modal('hide');
       }
       const closeButton = document.querySelector('.modal-content-element button');
       if (closeButton) {
           closeButton.addEventListener('click', closeEditChat);
       } else {
           console.error("닫기 버튼을 찾을 수 없습니다.");
       }


    const editChatRoomNameInput = document.getElementById('chatRoomNameInput');
    const confirmEditButton = document.getElementById('confirmEditButton');

    function toggleConfirmEditButton() {
        confirmEditButton.disabled = editChatRoomNameInput.value.trim() === '';
         if (confirmEditButton.disabled) {
                confirmEditButton.classList.add('button-disabled');
            } else {
                confirmEditButton.classList.remove('button-disabled');
            }
    }

    editChatRoomNameInput.addEventListener('input', toggleConfirmEditButton);
    toggleConfirmEditButton();

        $('#editChatRoomModal').on('show.bs.modal', function () {
            toggleConfirmEditButton();
        });

      $('#editChatRoomModal').on('hide.bs.modal', function () {
          editChatRoomNameInput.value = '';
          confirmEditButton.disabled = true;
          confirmEditButton.classList.add('update-button');
      });

    //필수로 넣기
    window.functionType = 1;
    console.log("현재 기능 타입: " +window.functionType);

    if (window.functionType === 1) {
        markNotificationsAsRead(window.functionType);
    }

});

(function() {
    let currentChatRoomNo = null;
    let socket = null;
    const currentMember = document.getElementById("currentMember").value;

    if (!socket || socket.readyState !== WebSocket.OPEN) {
        socket = new WebSocket(`ws://localhost:8080/websocket/chat`);
    }

    socket.onopen = function() {
        console.log("웹소켓이 연결되었습니다.");
        const initialRequest = {
            type: 'getUnreadCounts',
            currentMember: currentMember
        };
        socket.send(JSON.stringify(initialRequest));
    };

    socket.onclose = function(event) {
        console.log("웹소켓 연결이 해제되었습니다.", event);

    };

    socket.onerror = function(error) {
        console.error("에러 발생", error);
    };

    let selectedMembers = [];
    let selectNames = [];
    let globalMemberNumbers = [];
    let newMembers = [];
    // 페이지 로드 시 이전 선택된 사원 로드
    function loadSelectedMembers() {
        const savedMembers = localStorage.getItem('selectedMembers');
        if (savedMembers) {
            selectedMembers = JSON.parse(savedMembers);
        }
    }

    // 페이지 로드 시 기존 선택 상태를 복원
    function restoreSelection(instance) {
        selectedMembers.forEach(memberName => {
            const node = instance.get_node(instance.get_container().find(`:contains('${memberName}')`).attr('id'));
            if (node) {
                instance.check_node(node);
            }
        });
    }

    document.getElementById('openChart').addEventListener('click', function() {
        $('#organizationChartModal').modal('show');
        loadOrganizationChart();
    });

    document.getElementById('addChatItem').addEventListener('click', function(){
        event.preventDefault();

        $('#organizationAddModal').modal('show');
        loadOrganizationAddChart();

        fetchExistingMembers(currentChatRoomNo).then(() => {
              disableCheckedMembers(globalMemberNumbers);
          });
    });

    function fetchExistingMembers(currentChatRoomNo) {
        return fetch(`/api/chat/exist/${currentChatRoomNo}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("서버가 응답하지 않음: " + response.statusText);
                }
                return response.json();
            })
            .then(memberNumbers => {
                globalMemberNumbers = memberNumbers;
                return memberNumbers;
            })
            .catch(error => {
                console.error('전송 중 오류 발생:', error);
                globalMemberNumbers = [];
                return [];
            });
    }
    // 조직도 로딩
        function loadOrganizationChart() {
            $.ajax({
                url: '/chat/chart',
                method: 'GET',
                success: function(data) {
                    $('#organization-chart').jstree('destroy').empty();
                    $('#organization-chart').jstree({
                        'core': {
                            'data': data,
                            'themes': {
                                'icons': true,
                                'dots': false,
                            }
                        },
                        'plugins': ['checkbox', 'types', 'search'],
                        'types': {
                            'default': {
                                'icon': 'fa fa-users'
                            },
                            'department': {
                                'icon': 'fa fa-users'
                            },
                            'member': {
                                'icon': 'fa fa-user'
                            }
                        }
                    }).on('ready.jstree', function (e, data) {
                        restoreSelection(data.instance);
                    }).on('changed.jstree', function (e, data) {
                        updateSelectedMembers(data.selected, data.instance);
                    });
                    $('#organization_search').on('keyup', function() {
                        const searchString = $(this).val();

                        $('#organization-chart').jstree(true).search(searchString);
                    });
                },
                error: function(xhr, status, error) {
                    console.error('조직도 로딩 오류:', error);
                }
            });
        }
    // 모달이 닫힐 때 검색 내용을 리셋
    $('#organizationChartModal').on('hide.bs.modal', function () {
        $('#organization_search').val(''); // 검색 입력 필드 비우기
        $('#organization-chart').jstree(true).search('');
         selectedMembers = [];
         localStorage.removeItem('selectedMembers');
    });
    // 선택된 사원 업데이트
    function updateSelectedMembers(selectedIds, instance) {
        const selectedMembersContainer = $('#selected-members');
        const permissionPickList = $('.permission_pick_list');
        const selectedMembersList = $('#selectedMembersList');
        selectedMembersContainer.empty();
        permissionPickList.empty();
        selectedMembersList.empty();

        const selectedNodes = instance.get_selected(true);
        selectedMembers = [];
        selectNames = [];

        selectedNodes.forEach(function(node) {
            if (node.original.type === 'member') {
                const memberId = node.id;
                const memberNumber = memberId.replace('member_', '');
                const memberElement = $('<div class="selected-member"></div>');
                const memberName = $('<span></span>').text(node.text);
                const removeButton = $('<button class="remove-member">&times;</button>');

                memberElement.append(memberName).append(removeButton);
                selectedMembersContainer.append(memberElement);
                selectedMembersList.append(`<p><i class="fa-solid fa-user"></i> ${node.text}</p>`);
                selectedMembers.push(memberNumber);
                selectNames.push(node.text);

                removeButton.click(function() {
                    instance.uncheck_node(node);
                    memberElement.remove();
                    const index = selectedMembers.indexOf(memberNumber);
                    if (index !== -1) {
                        selectedMembers.splice(index, 1);
                    }

                    localStorage.setItem('selectedMembers', JSON.stringify(selectedMembers));

                    permissionPickList.find(`.permission-item[data-name="${node.text}"]`).remove();
                });

                const permissionItem = $(`<div class="permission-item" data-name="${node.text}"></div>`);
                permissionItem.text(node.text);
                permissionPickList.append(permissionItem);
            }
        });

        localStorage.setItem('selectedMembers', JSON.stringify(selectedMembers));
    }

   socket.onmessage = function(event) {
       const message = JSON.parse(event.data);
       const chatContentDiv = document.getElementById("chatContent");

           if (message.type === "unreadCounts") {
                   message.data.forEach(item => {
                         const chatRoomNo = item.chatRoomNo;
                              const unreadCount = item.unreadCount;
                              const inputElement = document.querySelector(`input[value="${chatRoomNo}"]`);

                              if (inputElement) {
                                  const chatItem = inputElement.closest('.chatItem');
                                  if (chatItem) {
                                      let unreadCountElement = chatItem.querySelector('.unread-count');

                                      if (unreadCountElement) {
                                          // 읽지 않은 개수 업데이트
                                          unreadCountElement.innerText = unreadCount;
                                      } else {
                                          // unread-count 요소가 없으면 추가
                                          unreadCountElement = document.createElement('div');
                                          unreadCountElement.classList.add('unread-count');
                                          unreadCountElement.innerText = unreadCount;
                                          chatItem.appendChild(unreadCountElement);
                                      }

                                        // 읽지 않은 개수가 0 이상일 때 배경색을 변경
                                      if (unreadCount > 0) {
                                          unreadCountElement.style.backgroundColor = 'red';
                                          unreadCountElement.style.color = 'white';
                                          unreadCountElement.style.width = '24px';
                                          unreadCountElement.style.height = '24px';
                                          unreadCountElement.style.lineHeight = '24px';
                                          unreadCountElement.style.textAlign = 'center';
                                          unreadCountElement.style.borderRadius = '50%';
                                      }
                                  }
                              }
                   });
       }
        else if (message.type === "unreadCountMember") {
            let currentMemberNo = parseInt(currentMember);
            const chatList = document.querySelector('.chatList-container');
            const chatItems = document.querySelectorAll('.chatItem');

            if (message.data && Array.isArray(message.data)) {
                message.data.forEach(item => {
                    const chatRoomNo = item.chatRoomNo;
                    const unreadCount = item.unreadCount;
                    const memberNo = item.memberNo;

                    const inputElement = document.querySelector(`input[value="${chatRoomNo}"]`);
                    if (inputElement) {
                        const chatItem = inputElement.closest('.chatItem');
                        if (chatItem) {

                            let  unreadCountContainer = chatItem.querySelector('.unread-count-container');
                            if (!unreadCountContainer) {
                                unreadCountContainer = document.createElement('div');
                                unreadCountContainer.id = 'unreadCountContainer';
                                unreadCountContainer.classList.add('unread-count-container');
                                chatItem.appendChild(unreadCountContainer);
                            }

                              console.log("unreadCountContainer:"+unreadCountContainer);
                            if (unreadCountContainer) {
                                let unreadCountElement = unreadCountContainer.querySelector('.unread-count');
                                if (!unreadCountElement) {
                                    unreadCountElement = document.createElement('span');
                                    unreadCountElement.id = 'unreadCount';
                                    unreadCountElement.classList.add('unread-count');
                                    unreadCountContainer.appendChild(unreadCountElement);
                                }
                                // 현재 멤버 번호와 비교하여 읽지 않은 메시지 수를 업데이트
                                if (memberNo === currentMemberNo) {
                                    // 현재 채팅방과 비교
                                    if (currentChatRoomNo === chatRoomNo) {
                                        // 현재 채팅방인 경우 읽음 개수 표시하지 않음
                                        if (unreadCountElement) {
                                            unreadCountElement.style.display = 'none'; // 읽음 개수 숨김
                                        }
                                    } else {
                                        // 현재 채팅방이 아닐 경우 읽음 개수 업데이트
                                        if (unreadCountElement) {
                                            unreadCountElement.innerText = unreadCount;

                                            if (unreadCount > 0) {
                                                unreadCountElement.style.display = 'block';
                                                unreadCountElement.style.backgroundColor = 'red';
                                                unreadCountElement.style.color = 'white';
                                                unreadCountElement.style.width = '24px';
                                                unreadCountElement.style.height = '24px';
                                                unreadCountElement.style.lineHeight = '24px';
                                                unreadCountElement.style.textAlign = 'center';
                                                unreadCountElement.style.borderRadius = '50%';
                                            } else {
                                                unreadCountElement.style.display = 'none';
                                            }
                                        } else {
                                            if (unreadCount > 0) {
                                                unreadCountElement = document.createElement('span');
                                                unreadCountElement.classList.add('unread-count');
                                                unreadCountElement.innerText = unreadCount;

                                                unreadCountElement.style.backgroundColor = 'red';
                                                unreadCountElement.style.color = 'white';
                                                unreadCountElement.style.width = '24px';
                                                unreadCountElement.style.height = '24px';
                                                unreadCountElement.style.lineHeight = '24px';
                                                unreadCountElement.style.textAlign = 'center';
                                                unreadCountElement.style.borderRadius = '50%';

                                                // Append to unread-count-container
                                                unreadCountContainer.appendChild(unreadCountElement);
                                            }
                                        }
                                    }

                                    // 채팅방 리스트에서 채팅방 위치 조정
                                    for (let i = 0; i < chatItems.length; i++) {
                                        const currentChatRoomNo = parseInt(chatItems[i].querySelector('input[type=hidden][id=chatRoomNo]').value, 10);
                                        if (currentChatRoomNo === chatRoomNo) {
                                            const isPinned = chatItems[i].querySelector('.fa-thumbtack') !== null;
                                            if (!isPinned) {
                                                const pinnedItems = document.querySelectorAll('.chatItem .fa-thumbtack');
                                                if (pinnedItems.length > 0) {
                                                    const lastPinnedItem = pinnedItems[pinnedItems.length - 1].closest('.chatItem');
                                                    lastPinnedItem.after(chatItems[i]);
                                                } else {
                                                    chatList.insertBefore(chatItems[i], chatList.firstChild);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } else {
                console.error("데이터를 찾을 수 없다", message);
            }
        }

      else if (message.type === "chatRoomCreation") {
          if (message.chatRoomNo && message.memberInfoList) {
              const currentMemberNo = parseInt(document.getElementById('currentMember').value, 10);
              const memberInfo = message.memberInfoList.find(info => info.memberNo === currentMemberNo);
              const chatListContainer = document.querySelector('.chatList-container');
              const newChatList = document.createElement('div');
              newChatList.id ='chatList';
              newChatList.classList.add('chatList');
              if (memberInfo) {
                  const newChatItem = document.createElement("div");
                  newChatItem.classList.add("chatItem");
                  newChatItem.setAttribute("onclick", `handleChatRoomClick(${message.chatRoomNo})`);

                    getMemberImage(currentMemberNo, message.chatRoomNo)
                        .then(image => {
                         const profileImage = (image && image.trim() !== '') ? '/linkOfficeImg/member/profile/' + image : '/img/user_profile.png';

                            newChatItem.innerHTML = `
                                <img src="${profileImage}"
                                     alt="프로필 이미지"
                                     style="border-radius: 50%; width: 70px; height: 70px; margin: 20px; margin-left:40px;" />
                                <h3><p>${memberInfo.roomName}</p></h3>
                                <input type="hidden" id="memberNo" value="${currentMemberNo}"/>
                                <input type="hidden" id="chatRoomNo" value="${message.chatRoomNo}" />
                            `;
                            newChatList.appendChild(newChatItem);

                            const pinnedItems = chatListContainer.querySelectorAll('.chatItem .fa-thumbtack');
                            if (pinnedItems.length > 0) {
                                const lastPinnedItem = pinnedItems[pinnedItems.length - 1].closest('.chatItem');
                                lastPinnedItem.after(newChatList);
                            } else {
                                chatListContainer.insertBefore(newChatList, chatListContainer.firstChild);
                            }
                        })
                    .catch(error => {
                        console.error("프로필 이미지 가져오는 중 오류 발생:", error);

                    });
              }
                resetSelectedMembers();
                $('#organizationChartModal').modal('hide');
          }
      }else if (message.type === "groupChatCreate") {
         if (message.chatRoomNo) {
                 let chatListContainer = document.querySelector('.chatList-container');

                 const newChatList = document.createElement('div');
                 newChatList.id ='chatList';
                 newChatList.classList.add('chatList');
                 message.members.forEach(member => {

                     if (member === currentMember) {
                         const firstLetter = message.names.charAt(0).toUpperCase();

                         const newChatItem = document.createElement("div");
                         newChatItem.classList.add("chatItem");
                         newChatItem.setAttribute("onclick", `handleChatRoomClick(${message.chatRoomNo})`);

                         newChatItem.innerHTML = `
                             <div class="group-info">
                                 <div class="group-image" style="border-radius: 50%; width: 70px; height: 70px; margin: 20px;">
                                    ${firstLetter}
                                 </div>
                             </div>
                             <h3><p>${message.names}</p></h3>
                             <input type="hidden" id="memberNo" value="${currentMember}"/>
                             <input type="hidden" id="chatRoomNo" value="${message.chatRoomNo}" />
                         `;
                        newChatList.appendChild(newChatItem);

                         const pinnedItems = chatListContainer.querySelectorAll('.chatItem .fa-thumbtack');
                           if (pinnedItems.length > 0) {
                               const lastPinnedItem = pinnedItems[pinnedItems.length - 1].closest('.chatItem');
                               lastPinnedItem.after(newChatList);
                           } else {

                               chatListContainer.insertBefore(newChatList,chatListContainer.firstChild);
                           }

                     }
                 });

                 resetSelectedMembers();
                 $('#organizationChartModal').modal('hide');
             }
      } else if (message.type === "chatRoomUpdate") {
           const updatedChatRoomNo = message.roomNo;
           const updatedChatRoomName = message.updatedChatRoomName;
           const chatItem = document.querySelector(`input[value="${updatedChatRoomNo}"]`);
           if (chatItem) {
               const chatItemDiv = chatItem.closest('.chatItem');
               const chatNameElement = chatItemDiv.querySelector('h3 p');
               chatNameElement.textContent = updatedChatRoomName;
                const title = document.getElementById('chatRoomTitle');
                title.textContent = updatedChatRoomName;
                const firstLetter = updatedChatRoomName.charAt(0).toLowerCase();
                const groupImageElement = chatItemDiv.querySelector('.group-image');
                if (groupImageElement) {
                    groupImageElement.textContent = firstLetter;
                }
           }

           document.getElementById('chatRoomNameInput').value = '';

      }
      else if (message.type === "memberAdded") {
         const chatRoomNo = message.chatRoomNo;
         const chatRoomName = message.chatRoomName;
          const countPeople = parseInt(message.countPeople, 10)
         const existingChatRoom = document.querySelector(`input[type="hidden"][value="${chatRoomNo}"]`);


         if(currentChatRoomNo === chatRoomNo){
            console.log(countPeople);
            const participantCount = document.getElementById('participantCountSpan');
             const currentMember = parseInt(participantCount.textContent, 10) || 0;
            participantCount.textContent =currentMember+ countPeople;
            const messageElement = document.createElement('div');
             messageElement.classList.add("system-message", "messageItem");
             messageElement.innerHTML = `
                <div class="message-ele">
                    <div class="system-content">
                        <p>${message.invitedNames}님이 초대되었습니다.</p>
                    </div>
                </div>
            `;

            const chatContainer = document.getElementById('chatContent');
            chatContainer.appendChild(messageElement);
             chatContainer.scrollTop = chatContainer.scrollHeight;
         }
         // 기존에 동일한 채팅방이 없을 때만 추가
         if (!existingChatRoom) {
             const chatListContainer = document.querySelector('.chatList-container');
            const firstLetter =chatRoomName.charAt(0).toUpperCase();
             const newChatList = document.createElement('div');
             newChatList.id ='chatList';
             newChatList.classList.add('chatList');

             const newChatItem = document.createElement("div");
             newChatItem.classList.add("chatItem");
             newChatItem.setAttribute("onclick", `handleChatRoomClick(${message.chatRoomNo})`);

             newChatItem.innerHTML = `
                    <div class="group-info">
                        <div class="group-image" style="border-radius: 50%; width: 70px; height: 70px; margin: 20px;">
                             ${firstLetter}
                        </div>
                    </div>
                <h3><p>${chatRoomName}</p></h3>
                <input type="hidden" id="memberNo" value="${currentMember}"/>
                <input type="hidden" id="chatRoomNo" value="${chatRoomNo}" />
             `;
             newChatList.appendChild(newChatItem);
             const pinnedItems = chatListContainer.querySelectorAll('.chatItem .fa-thumbtack');
             if (pinnedItems.length > 0) {
                 const lastPinnedItem = pinnedItems[pinnedItems.length - 1].closest('.chatItem');
                 lastPinnedItem.after(newChatList);
             } else {
                 chatListContainer.insertBefore(newChatList,chatListContainer.firstChild);
              }

         }
      }else if(message.type === "updateUnreadCount") {
                const chatRoomNo = message.chatRoomNo;
                const unreadCount = message.unreadCount;
                const chatItem = document.querySelector(`input[value="${chatRoomNo}"]`).closest('.chatItem');
                 if (chatItem) {
                    const unreadCountElement = chatItem.querySelector('.unread-count');
                    if (unreadCountElement) {
                       if (unreadCount === 0) {
                          unreadCountElement.innerText = '';
                          unreadCountElement.style.backgroundColor = '';
                          unreadCountElement.style.color = '';
                          unreadCountElement.style.padding = '';
                          unreadCountElement.style.borderRadius = '';
                       } else {
                          unreadCountElement.innerText = '';
                       }
                    }
                 }
      }
      else if(message.type === "chatMemCount") {
         const count = message.data;
         const chatRoom = message.currentChatRoomNo;
         console.log(count);
         if(currentChatRoomNo === chatRoom){
            const participantCount = document.getElementById('participantCountSpan');
            participantCount.textContent = count;
            const messageElement = document.createElement('div');
             messageElement.classList.add("system-message", "messageItem");
             messageElement.innerHTML = `
                <div class="message-ele">
                    <div class="system-content">
                        <p>${message.outSentence}님이 나가셨습니다.</p>
                    </div>
                </div>
            `;

            const chatContainer = document.getElementById('chatContent');
            chatContainer.appendChild(messageElement);
             chatContainer.scrollTop = chatContainer.scrollHeight;
         }

      } else {


           if (message.chat_room_no === currentChatRoomNo) {
                markAllMessagesAsRead(currentChatRoomNo);
               const messageElement = document.createElement("div");
               const now = new Date();
               const formattedTime = formatDateTime(now);

               const memberNo = parseInt(document.getElementById("currentMember").value, 10);
               const memberNoCheck = parseInt(message.chat_sender_no, 10);

               if (memberNoCheck === memberNo) {
                   messageElement.classList.add("my-message", "messageItem");
                   messageElement.innerHTML = `
                       <div class="message-ele">
                           <span class="message-time">${formattedTime}</span>
                           <div class="message-content">
                               <p>${message.chat_content}</p>
                           </div>
                       </div>
                   `;
               } else {
                   messageElement.classList.add("other-message", "messageItem");
                   messageElement.innerHTML = `
                       <div class="message-sender">
                           <p style="margin-left:8px; margin-bottom:2px;">${message.chat_sender_name}</p>
                       </div>
                       <div class="message-ele">
                           <div class="message-content">
                               <p>${message.chat_content}</p>
                           </div>
                           <span class="message-time">${formattedTime}</span>
                       </div>
                   `;
               }
               chatContentDiv.appendChild(messageElement);
               chatContentDiv.scrollTop = chatContentDiv.scrollHeight;
               // 채팅방 목록에서 해당 채팅방을 찾고, 이동시키기
                const chatItems = document.getElementsByClassName('chatItem');
                const chatList = document.querySelector('.chatList-container');

                for (let i = 0; i < chatItems.length; i++) {
                    const chatRoomNo = parseInt(chatItems[i].querySelector('input[type=hidden][id=chatRoomNo]').value, 10);
                    if (chatRoomNo === message.chat_room_no) {
                        const isPinned = chatItems[i].querySelector('.fa-thumbtack') !== null;
                        if (!isPinned) {
                            const pinnedItems = document.querySelectorAll('.chatItem .fa-thumbtack');
                            if (pinnedItems.length > 0) {
                                const lastPinnedItem = pinnedItems[pinnedItems.length - 1].closest('.chatItem');
                                 lastPinnedItem.after(chatItems[i]);

                            } else {
                                chatList.insertBefore(chatItems[i], chatList.firstChild);
                            }
                        }
                        break;
                    }
                }

           }
       }
   };
//실시간 사용자 채팅방 사진
function getMemberImage(memberNo, chatRoomNo) {
    return fetch(`/api/getMemberImage/${memberNo}/${chatRoomNo}`)
         .then(response => {
             if (!response.ok) {
                 throw new Error('네트워크 응답이 좋지 않습니다.');
             }
             return response.text(); // 프로필 이미지를 반환
         })
         .catch(error => {
             console.error("오류 발생:", error);
             return null; // 오류 발생 시 null 반환
         });
}
// chatList가 없을 경우 생성하는 함수
function createChatListIfNotExists() {

    let chatList = document.getElementById('chatList');

    if (!chatList) {

        chatList = document.createElement('div');
        chatList.id = 'chatList';
        chatList.classList.add('chatList');

        const chatContainer = document.querySelector('.chatList-container');

        if (chatContainer) {
            chatContainer.appendChild(chatList);
        } else {
            console.error("chatList-container를 찾을 수 없습니다.");
        }
    }
}





    window.sendMessage = function() {
        const chat_sender_no = document.getElementById("userNo").value;
        const chat_content = document.getElementById("messageInput").value;
        const chat_sender_name = document.getElementById("userName").value;
        const sendButton = document.getElementById("sendButton");

        if (currentChatRoomNo === null) {
            console.error("방이 선택되지 않음");
            return;
        }
        if (chat_content.trim() === '') {
            console.error("메시지 내용이 비어 있음");
            return;
        }
        sendButton.disabled = true;
        sendButton.classList.add("btn-disabled");

        const message = {
            chat_sender_no: chat_sender_no,
            chat_room_no: currentChatRoomNo,
            chat_content: chat_content,
            chat_sender_name: chat_sender_name,
        };
        socket.send(JSON.stringify(message));
        document.getElementById("messageInput").value = "";

         const notificationMessage = {
             type: 'noficationChat',
             chat_sender_no: chat_sender_no,
             chat_room_no: currentChatRoomNo
         };

         // 알림 소켓에 메시지 전송
         alarmSocket.send(JSON.stringify(notificationMessage));


    };

    window.confirmButton = function() {
        const currentMemberNo = document.getElementById("currentMemberNo").value;
        const csrfToken = document.querySelector('input[name="_csrf"]').value;
        const currentMemberName = document.getElementById("currentMemberName").value;

        if (selectedMembers.length > 1) {
            $('#groupChatNameModal').modal('show');
        } else {
             checkDuplicateChatRoom(currentMemberNo, selectedMembers).then(isDuplicate => {
                 if (!isDuplicate) {
                     createChatRoom(currentMemberNo, currentMemberName, csrfToken);
                 } else {
                     Swal.fire({
                         icon: 'warning',
                         text: '동일한 멤버와의 채팅방이 이미 존재합니다.',
                         confirmButtonText: '확인',
                         customClass: {
                               confirmButton: 'custom-confirm-button'
                         }
                     });
                 }
             }).catch(error => {
                 console.error('채팅방 중복 확인 중 오류 발생:', error);
             });
        }
    };
    //개인 채팅방 중복 확인
    function checkDuplicateChatRoom(currentMemberNo, selectedMembers) {
    console.log(selectedMembers);
    console.log(currentMemberNo);
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
        return fetch('/api/chat/checkDuplicateChatRoom', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                currentMemberNo: currentMemberNo,
                selectedMembers: selectedMembers
            })
        })
        .then(response => response.json())
        .then(data => data.isDuplicate);
    }
    function createChatRoom(currentMemberNo, currentMemberName, csrfToken) {
        const groupChatName = selectedMembers.length > 1 ? document.getElementById('groupChatNameInput').value : null;
        const message = {
            type: "chatRoomCreation",
            members: selectedMembers,
            currentMemberNo: currentMemberNo,
            names: selectNames,
            currentMemberName: currentMemberName,
            groupChatName: groupChatName,
            csrfToken: csrfToken
        };

        socket.send(JSON.stringify(message));
            if (groupChatName) {
                document.getElementById('groupChatNameInput').value = '';
            }
        $('#groupChatNameModal').modal('hide');
    }
   // Enter 키로도 메시지 전송 가능하게 구성
   document.getElementById("messageInput").addEventListener("keydown", function(event) {
       if (event.key === "Enter" && !event.shiftKey) {
           event.preventDefault();
           const sendButton = document.getElementById("sendButton");
           if (!sendButton.disabled) {
               sendMessage();
           }
       }
   });
    document.getElementById('groupChatNameInput').addEventListener('input', function() {
        const groupChatNameInput = document.getElementById('groupChatNameInput');
        const confirmButton = document.getElementById('confirmGroupChatName');

        if (groupChatNameInput.value.trim() !== '') {
            confirmButton.disabled = false;
        } else {
            confirmButton.disabled = true;
        }
    });
   document.getElementById('confirmButton').addEventListener('click', function(event) {
      event.preventDefault();
      window.confirmButton();
   });
   document.getElementById('addButton').addEventListener('click', function(event) {
      event.preventDefault();
      window.addButton();
   });

   document.getElementById('confirmGroupChatName').addEventListener('click', function(event) {
      event.preventDefault();
      const currentMemberNo = document.getElementById("currentMemberNo").value;
          const currentMemberName = document.getElementById("currentMemberName").value;
          const csrfToken = document.querySelector('input[name="_csrf"]').value;

          const groupChatName = document.getElementById('groupChatNameInput').value;

          checkDuplicateChatRoom(currentMemberNo, selectedMembers).then(isDuplicate => {
              if (!isDuplicate) {

                  createChatRoom(currentMemberNo, currentMemberName, csrfToken);
                  $('#groupChatNameModal').modal('hide');
              } else {
                  Swal.fire({
                      icon: 'warning',
                      text: '동일한 멤버의 그룹방이 이미 존재합니다.',
                      confirmButtonText: '확인',
                      customClass: {
                          confirmButton: 'custom-confirm-button'
                      }
                  });
                  $('#groupChatNameModal').modal('hide');
              }
          }).catch(error => {
              console.error('채팅방 중복 확인 중 오류 발생:', error);
          });

   });

   document.getElementById('editChatRoomButton').addEventListener('click', function(event) {
       event.preventDefault();
       $('#editChatRoomModal').modal('show');
    });

   document.getElementById('openDrop').addEventListener('click', function(event) {
        event.preventDefault();
        const dropdownMenu = document.getElementById('chatDropdownMenu');
        dropdownMenu.style.display = dropdownMenu.style.display === 'block' ? 'none' : 'block';
   });
   //채팅방 나가기
   document.getElementById('leaveChatItem').addEventListener('click', function(event) {
         event.preventDefault();
         const csrfToken = document.querySelector('input[name="_csrf"]').value;
         Swal.fire({
                text: "채팅방에서 나가시겠습니까?",
                icon: "warning",
                showCancelButton: true,
                confirmButtonText: "확인",
                cancelButtonText: "취소",
                dangerMode: true,
                customClass: {
                        confirmButton: 'custom-confirm-button',
                        cancelButton: 'custom-cancel-button'
                    }
            }).then((result) => {
                if (result.isConfirmed) {
                    // 사용자가 나가기를 원하면 서버에 요청 보내기
                    fetch(`/api/chat/out/${currentChatRoomNo}`, {
                        method: 'POST',
                        headers: {
                           'Content-Type': 'application/json',
                           'X-CSRF-Token': csrfToken
                        },
                        body: JSON.stringify({ currentMember: currentMember })
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            socket.send(JSON.stringify({
                                type: 'outCount',
                                chat_room_no: currentChatRoomNo,
                                currentMember: currentMember
                            }));
                            window.location.reload();
                        } else {
                            // 실패 시 경고 메시지
                            Swal.fire("실패", "채팅방 나가기에 실패했습니다.", "error");
                        }
                    })
                    .catch(error => {
                        console.error('오류 발생:', error);
                        Swal.fire("오류", "서버와의 통신 중 오류가 발생했습니다.", "error");
                    });
                }
            });
      });
      // 채팅방 이름 수정 함수
      document.getElementById('confirmEditButton').addEventListener('click', function(event) {
         event.preventDefault();

         const currentMemberNo = document.getElementById("currentMemberNo").value;
         const csrfToken = document.querySelector('input[name="_csrf"]').value;
         const chatRoomName = document.getElementById('chatRoomNameInput').value;

          if (!chatRoomName.trim()) {
              alert("채팅방 이름을 입력하세요.");
              return;
          }

          // 채팅방 수정 함수 호출
          updateChatRoom(currentMemberNo, currentChatRoomNo, csrfToken, chatRoomName);
      });
      //채팅방 고정
      document.getElementById('pinChatItem').addEventListener('click', function() {
        event.preventDefault();
        updatePinStatus(true);
      });
      //채팅방 고정 해제
      document.getElementById('pinDeleteChatItem').addEventListener('click', function() {
        event.preventDefault();
        updatePinStatus(false);
      });

      function updatePinStatus(status){
         const csrfToken = document.querySelector('input[name="_csrf"]').value;
         let statusValue = 0;

         if (status === true) {
             statusValue = 1;
         }
          // 상태에 따른 메시지 설정
         const messageText = statusValue === 1 ? "채팅방이 상단에 고정됩니다." : "채팅방 고정을 해제합니다.";
         Swal.fire({
            text: messageText,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "확인",
            cancelButtonText: "취소",
            dangerMode: true,
            customClass: {
               confirmButton: 'custom-confirm-button',
               cancelButton: 'custom-cancel-button'
            }
         }).then((result) => {
            if (result.isConfirmed) {
            // 채팅방 상단 고정요청
            fetch(`/api/chat/pin/${currentChatRoomNo}`, {
               method: 'POST',
               headers: {
                  'Content-Type': 'application/json',
                  'X-CSRF-Token': csrfToken
               },
                  body: JSON.stringify({
                  currentMember: currentMember,
                  statusValue: statusValue,
                  updatedAt: new Date().toISOString()})
               })
               .then(response => response.json())
               .then(data => {
                  if (data.success) {
                     window.location.reload();
                  } else {
                     Swal.fire("실패", "고정에 실패했습니다.", "error");
                  }
               })
               .catch(error => {
                  console.error('오류 발생:', error);
                   Swal.fire("오류", "서버와의 통신 중 오류가 발생했습니다.", "error");
               });
            }
         });
      }


       function updateChatRoom(currentMemberNo, roomNo, csrfToken, chatRoomName) {

           if (!roomNo) {
               console.error("채팅방이 선택되지 않았습니다.");
               return;
           }

           const message = {
               type: "chatRoomUpdate",
               roomNo: roomNo,
               currentMemberNo: currentMemberNo,
               chatRoomName: chatRoomName,
               csrfToken: csrfToken
           };

           socket.send(JSON.stringify(message));

           $('#editChatRoomModal').modal('hide');
       }
    window.addEventListener('click', function(e) {
        const dropdownMenu = document.getElementById('chatDropdownMenu');

        if (!e.target.matches('#openDrop') && !e.target.closest('.chatDropdown')) {
            dropdownMenu.style.display = 'none';
        }
    });

function formatDateTime(date) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}
    function loadChatMessages(chatRoomNo) {
        fetch(`/api/chat/messages/${chatRoomNo}/${currentMember}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("서버가 응답하지 않음" + response.statusText);
                }
                return response.json();
            })
            .then(data => {
             console.log(data);

                displayChatMessages(data);
                console.log();
            })
            .catch(error => {
                console.error('전송 중 오류 발생', error);
            });
    }
    //채팅방 타입
    function getChatRoomType(chatRoomNo) {
        return fetch(`/api/chat/roomType/${chatRoomNo}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("채팅방 타입을 찾을 수 없습니다.");
                }
                return response.text();
            })
            .catch(error => {
                console.error("에러", error);
                return null;
            });
    }
    //채팅방 고정 확인
    function checkPinStatus(chatRoomNo) {
        return fetch(`/api/chat/pin/status/${chatRoomNo}/${currentMember}`)
              .then(response => {
                          if (!response.ok) {
                              throw new Error("최대 10개까지 고정할 수 있습니다.");
                          }
                          return response.json();
                      })
                      .then(data => {
                          return data.isPinned;
                      })
                      .catch(error => {
                          console.error("에러", error);
                          return null;
                      });
    }
    function displayChatMessages(messages) {
        const chatContentDiv = document.getElementById("chatContent");
        chatContentDiv.innerHTML = '';

        const memberNo = parseInt(document.getElementById("memberNo").value, 10);

        messages.forEach(function(message) {
            const messageElement = document.createElement("div");
            const formattedTime = new Date(message.chatMessageCreateDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            if (message.senderNo === memberNo) {
                messageElement.classList.add("my-message", "messageItem");
                messageElement.innerHTML = `
                    <div class="message-ele">
                        <span class="message-time">${formattedTime}</span>
                        <div class="message-content">
                            <p>${message.chatContent}</p>
                        </div>
                    </div>
                `;
            } else if (message.senderNo === 0) {

                messageElement.classList.add("system-message", "messageItem");
                messageElement.innerHTML = `
                    <div class="message-ele">
                        <div class="system-content">
                            <p>${message.chatContent}</p>
                        </div>
                    </div>
                `;
            } else {
                messageElement.classList.add("other-message", "messageItem");
                messageElement.innerHTML = `
                    <div class="message-sender">
                        <p style="margin-left:8px; margin-bottom:2px;">${message.senderName}</p>
                    </div>
                    <div class="message-ele">
                        <div class="message-content">
                            <p>${message.chatContent}</p>
                        </div>
                        <span class="message-time">${formattedTime}</span>
                    </div>
                `;
            }
            chatContentDiv.appendChild(messageElement);
        });

        chatContentDiv.scrollTop = chatContentDiv.scrollHeight;
    }
    let previousChatRoomNo = null;
    window.handleChatRoomClick = function(element) {
            document.getElementById("messageInput").value = "";
            currentChatRoomNo = element;
            console.log(previousChatRoomNo);
         // 처음 클릭하거나 이전 채팅방과 다를 때 읽음 처리
           if (previousChatRoomNo === null || previousChatRoomNo !== currentChatRoomNo) {
               // 읽음 처리
               markAllMessagesAsRead(currentChatRoomNo);
           }
            // 현재 채팅방 번호를 이전 채팅방 번호로 업데이트
            previousChatRoomNo = element;

            let chatItems = document.getElementsByClassName('chatItem');
            for (let i = 0; i < chatItems.length; i++) {
                chatItems[i].classList.remove('selected');
            }
            let selectedChatItem = document.querySelector(`input[value="${element}"]`).closest('.chatItem');
            if (selectedChatItem) {
                selectedChatItem.classList.add('selected');
            }
            const dropdownMenu = document.getElementById('chatHeader-buttons');
            dropdownMenu.style.display = 'block';

            const countParticipant = document.getElementById('countParticipant');
            countParticipant.style.display = 'block';

         // 검색 버튼 보이기
            const searchDiv = document.getElementById('chatInput');
            searchDiv.style.display = 'flex';

            getChatRoomName(element).then(chatRoomName => {
                if (chatRoomName) {
                    document.getElementById('chatRoomTitle').innerText = chatRoomName;
                }
                loadChatMessages(element);
            }).catch(error => {
                console.error('error', error);
            });
             getChatRoomType(element).then(chatRoomType => {
              const dropdownMenu = document.getElementById('chatDropdownMenu');
              const editChatItem = document.getElementById('editChatRoomButton');
              const addChatItem = document.getElementById('addChatItem');
              if (chatRoomType === '1') {
                   editChatItem.style.display = 'block';
                   addChatItem.style.display = 'block';
              } else {
                   editChatItem.style.display = 'none';
                   addChatItem.style.display = 'none';
              }
              }).catch(error => {
                    console.error('error', error);
              });

              // 고정 상태 확인
                checkPinStatus(element).then(isPinned => {
                    const pinChatItem = document.getElementById('pinChatItem');
                    const pinDeleteChatItem = document.getElementById('pinDeleteChatItem');
                    const pin = document.getElementById('pin');
                    if (isPinned) {
                        pinChatItem.style.display = 'none';
                        pinDeleteChatItem.style.display = 'block';
                    } else {
                        pinChatItem.style.display = 'block';
                        pinDeleteChatItem.style.display = 'none';

                    }
                }).catch(error => {
                    console.error('고정 상태 확인 중 오류 발생:', error);
                });

             // 참여자 수 가져오기
                 getParticipantCount(element).then(count => {
                     const participantCountElement = document.querySelector('.countParticipant span');
                     participantCountElement.innerText = count;
                 }).catch(error => {
                     console.error('참여자 수를 가져오는 중 오류 발생:', error);
                 });

                // 읽음 처리
                //markAllMessagesAsRead(element);

    }

    // 참여자 수 가져오는 함수
    function getParticipantCount(chatRoomNo) {
        return fetch(`/api/chat/participants/count/${chatRoomNo}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('참여자 수를 가져올 수 없습니다.');
                }
                return response.json();
            })
            .then(data => {
                return data.count;
            });
    }
    function getChatRoomName(chatRoomNo) {

        return fetch(`/api/chat/roomName/${chatRoomNo}/${currentMember}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("채팅방 이름을 찾지 못했습니다.");
                }
                return response.text();
            })
            .catch(error => {
                console.error("error", error);
                return null;
            });
    }
    // 읽음 처리
    function markAllMessagesAsRead(chatRoomNo) {
        const message = {
            type: 'markAsRead',
            chatRoomNo: chatRoomNo,
            currentMember: currentMember
        };

        socket.send(JSON.stringify(message));
    }

    function resetSelectedMembers() {
        selectedMembers = [];
        selectNames = [];

        $('#organization-chart').jstree(true).uncheck_all();
        $('#selected-members').empty();
        $('.permission_pick_list').empty();

        localStorage.removeItem('selectedMembers');
    }

    //추가
    function loadOrganizationAddChart() {
        $.ajax({
            url: '/chat/chart',
            method: 'GET',
            success: function(data) {
                const excludedMembers = globalMemberNumbers.map(memberNo => 'member_' + memberNo);

                const filteredData = data.filter(node => !excludedMembers.includes(node.id));

                $('#organization-chart-add').jstree('destroy').empty();
                $('#organization-chart-add').jstree({
                    'core': {
                        'data': filteredData,
                        'themes': {
                            'icons': true,
                            'dots': false,
                        }
                    },
                    'plugins': ['checkbox', 'types', 'search'],
                    'types': {
                        'default': {
                            'icon': 'fa fa-users'
                        },
                        'department': {
                            'icon': 'fa fa-users'
                        },
                        'member': {
                            'icon': 'fa fa-user'
                        }
                    }
                }).on('ready.jstree', function (e, data) {
                    restoreSelection(data.instance);
                    disableCheckedMembers(globalMemberNumbers); // 기존에 선택된 노드 복원
                }).on('changed.jstree', function (e, data) {
                    updateSelectedMembersAdd(data.selected, data.instance); // 선택된 멤버 업데이트
                });
                   $('#organization_add_search').on('keyup', function() {
                       const searchString = $(this).val();
                       $('#organization-chart-add').jstree(true).search(searchString);
                   });
            },
            error: function(xhr, status, error) {
                console.error('조직도 로딩 오류:', error);
            }
        });
    }
    // 모달이 닫힐 때 검색 내용을 리셋
    $('#organizationAddModal').on('hide.bs.modal', function () {
        $('#organization_add_search').val(''); // 검색 입력 필드 비우기
        $('#organization-chart-add').jstree(true).search(''); // jstree 검색 리셋
    });
//function loadOrganizationAddChart() {
//            $.ajax({
//                url: '/chat/chart',
//                method: 'GET',
//                success: function(data) {
//                    $('#organization-chart').jstree('destroy').empty();
//                    $('#organization-chart').jstree({
//                        'core': {
//                            'data': data,
//                            'themes': {
//                                'icons': true,
//                                'dots': false,
//                            }
//                        },
//                        'plugins': ['checkbox', 'types', 'search'],
//                        'types': {
//                            'default': {
//                                'icon': 'fa fa-users'
//                            },
//                            'department': {
//                                'icon': 'fa fa-users'
//                            },
//                            'member': {
//                                'icon': 'fa fa-user'
//                            }
//                        }
//                    }).on('ready.jstree', function (e, data) {
//                         restoreSelection(data.instance);
//                         disableCheckedMembers(globalMemberNumbers);
//                    }).on('changed.jstree', function (e, data) {
//                        updateSelectedMembers(data.selected, data.instance);
//                    });
//                    $('#organization_search').on('keyup', function() {
//                        const searchString = $(this).val();
//
//                        $('#organization-chart').jstree(true).search(searchString);
//                    });
//                },
//                error: function(xhr, status, error) {
//                    console.error('조직도 로딩 오류:', error);
//                }
//            });
//        }

//
//    function updateSelectedMembersAdd(selectedIds, instance) {
//        const selectedMembersContainer = $('#selected-member'); // 선택된 멤버 표시할 곳
//        selectedMembersContainer.empty();
//
//        const selectedNodes = instance.get_selected(true);
//        selectedMembers = [];
//        selectNames = [];
//
//        selectedNodes.forEach(function(node) {
//            if (node.original.type === 'member') {
//                const memberId = node.id;
//                const memberNumber = memberId.replace('member_', '');
//
//                if (!selectedMembers.includes(memberNumber)) {
//                    const memberElement = $('<div class="selected-member"></div>');
//                    const memberName = $('<span></span>').text(node.text);
//
//                    memberElement.append(memberName);
//                    selectedMembersContainer.append(memberElement);
//                    selectedMembers.push(memberNumber);
//                    selectNames.push(node.text);
//                }
//            }
//        });
//
//        // 저장된 선택된 멤버를 로컬 저장소에 저장
//        localStorage.setItem('selectedMembersAdd', JSON.stringify(selectedMembers));
//    }
     function updateSelectedMembersAdd(selectedIds, instance) {
         const selectedMembersContainer = $('#selected-member');
         selectedMembersContainer.empty();

         const selectedNodes = instance.get_selected(true);
         selectedMembers = [];
         selectNames = [];
         newMembers = [];

         selectedNodes.forEach(function(node) {
             if (node.original.type === 'member') {
                 const memberId = node.id;
                 const memberNumber = memberId.replace('member_', '');
                 const memberElement = $('<div class="selected-member"></div>');
                 const memberName = $('<span></span>').text(node.text);


                let isExistingMember = globalMemberNumbers.includes(Number(memberNumber));

                if (isExistingMember) {
                    memberElement.append(memberName);
                } else {
                    newMembers.push(memberNumber);
                    selectNames.push(node.text);
                    const removeButton = $('<button class="remove-member">&times;</button>');
                    memberElement.append(memberName).append(removeButton);
                    removeButton.click(function() {
                        instance.uncheck_node(node);
                        memberElement.remove();
                        const index = selectedMembers.indexOf(memberNumber);
                        if (index !== -1) {
                            selectedMembers.splice(index, 1);
                        }
                    });
                }
                 selectedMembersContainer.append(memberElement);
                 selectedMembers.push(memberNumber);

             }
         });

         localStorage.setItem('selectedMembersAdd', JSON.stringify(newMembers));
     }
        function disableCheckedMembers(globalMemberNumbers) {
            var jstree = $('#organization-chart-add').jstree(true);
            if (jstree) {
                globalMemberNumbers.forEach(function(memberNo) {
                    var nodeId = 'member_' + memberNo;
                    if (jstree.get_node(nodeId)) {
                        jstree.disable_node(nodeId);
                        jstree.check_node(nodeId);
                    }
                });
            }
        }
          function getMemberChatRoomName(chatRoomNo) {
                    return fetch(`/api/chat/room/name/${chatRoomNo}`)
                        .then(response => {
                            if (!response.ok) {
                                throw new Error("채팅방 이름을 찾지 못했습니다.");
                            }
                            return response.text();
                        })
                        .catch(error => {
                            console.error("에러", error);
                            return null;
                        });
                }
        window.addButton = async function() {
             try {
                    const name = await getMemberChatRoomName(currentChatRoomNo);

                    if (name) {

                        addChatRoom(currentChatRoomNo, newMembers, name);
                    } else {
                        console.error("채팅방 이름을 가져오는 데 실패했습니다.");
                    }
                } catch (error) {
                    console.error("오류 발생:", error);
                }

        };
        function addChatRoom(currentChatRoomNo, newMembers, name) {

            const add = {
                type: "chatRoomAdd",
                currentChatRoomNo: currentChatRoomNo,
                newMembers : newMembers,
                name: name,
                selectNames : selectNames
            };

            socket.send(JSON.stringify(add));
            $('#organizationAddModal').modal('hide');
        }

})();