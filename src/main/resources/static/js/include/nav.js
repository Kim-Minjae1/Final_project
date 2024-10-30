let currentMember = parseInt(document.getElementById("currentMember").value, 10);
//알림 출력을 위한 멤버값
document.addEventListener("DOMContentLoaded", function() {
	const dropdownToggles = document.querySelectorAll(".dropdown-toggle");
	const dropdowns = document.querySelectorAll(".dropdown");
	const links = document.querySelectorAll(".dropdown a");
	let currentDropdown = null;

	function saveDropdownState(dropdownId) {
		localStorage.setItem("activeDropdown", dropdownId);
	}

	function loadDropdownState() {
		const activeDropdownId = localStorage.getItem("activeDropdown");
		if (activeDropdownId) {
			const activeDropdown = document.querySelector(`.dropdown[data-dropdown-id="${activeDropdownId}"]`);
			if (activeDropdown) {
				openDropdown(activeDropdown);
			}
		}
	}

	function openDropdown(dropdown) {
		closeAllDropdowns();
		dropdown.style.display = "block";
		dropdown.closest('li').classList.add('active');
		currentDropdown = dropdown;
		const toggle = dropdown.previousElementSibling;
		toggle.classList.add('active-toggle');
	}

	function closeAllDropdowns() {
		dropdowns.forEach(function(dropdown) {
			dropdown.style.display = "none";
		});
		dropdownToggles.forEach(function(toggle) {
			toggle.closest('li').classList.remove('active');
			toggle.classList.remove('active-toggle');
		});
		currentDropdown = null;
	}

	dropdownToggles.forEach(function(toggle) {
		toggle.addEventListener("click", function(e) {
			e.preventDefault();
			const dropdownId = this.nextElementSibling.getAttribute("data-dropdown-id");
			const dropdown = document.querySelector(`.dropdown[data-dropdown-id="${dropdownId}"]`);

			if (dropdown === currentDropdown) {
				closeAllDropdowns();
				localStorage.removeItem("activeDropdown");
			} else {
				openDropdown(dropdown);
				saveDropdownState(dropdownId);
			}
		});
	});

	const nestedDropdownToggles = document.querySelectorAll(".dropdown > .dropdown-toggle");
	nestedDropdownToggles.forEach(function(toggle) {
		toggle.addEventListener("click", function(e) {
			e.preventDefault();
			e.stopPropagation();

			const dropdownId = this.nextElementSibling.getAttribute("data-dropdown-id");
			const nestedDropdown = document.querySelector(`.dropdown[data-dropdown-id="${dropdownId}"]`);

			if (nestedDropdown.style.display === "block") {
				nestedDropdown.style.display = "none";
			} else {
				nestedDropdown.style.display = "block";
			}
		});
	});

	document.addEventListener("click", function(e) {
		if (!e.target.closest(".dropdown-toggle") && !e.target.closest(".dropdown")) {
			closeAllDropdowns();
			localStorage.removeItem("activeDropdown");
		}
	});

	loadDropdownState();

	links.forEach(function(link) {
		link.addEventListener("click", function() {
			links.forEach(l => {
				l.classList.remove('active-link');
				l.classList.remove('single-menu-active');
			});

			this.classList.add('active-link');

			// single-menu 클래스일 경우
			if (this.classList.contains('single-menu')) {
				this.classList.add('single-menu-active');
			}

			localStorage.setItem("activeLink", this.getAttribute("href"));
		});
	});

	const activeLinkHref = localStorage.getItem("activeLink");
	if (activeLinkHref) {
		const activeLink = document.querySelector(`.dropdown a[href="${activeLinkHref}"]`);
		if (activeLink) {
			activeLink.classList.add('active-link');
			if (activeLink.classList.contains('single-menu')) {
				activeLink.classList.add('single-menu-active');
			}
		}
	}
});

document.addEventListener('click', function(event) {
	if (!event.target.closest('#nav_wrap')) {
		closeDropdowns();
	}
});

function closeDropdowns() {
	document.querySelectorAll('.dropdown').forEach(dropdown => {
		dropdown.style.display = 'none';
	});
	document.querySelectorAll('#nav_wrap > ul > li').forEach(li => {
		li.classList.remove('active');
	});
}



//알림 모달
const alarmModal = document.getElementById("notification-modal");
const closeButton = document.querySelector(".close-notification-modal");

function showNotification(title, content, memberNo, time, type, pk) {

	if (memberNo === currentMember) {
		const notificationContainer = document.getElementById("notificationContainer");
		const notificationModal = document.createElement("div");

		notificationModal.classList.add("notification-modal");

		notificationModal.innerHTML = `
            <div class="notification-modal-content" data-notification-type="${type}" data-notification-type-pk="${pk}">
                <strong>${title}</strong>
                <p>${content}</p>
                <input type="hidden" name="memberNo" value="${memberNo}">
                <span class="notification-time">${time}</span>
                <span class="close-notification-modal">&times;</span>
            </div>
        `;

		notificationContainer.appendChild(notificationModal);
		let unreadCountElement = document.getElementById('unread-bell-count');
		if (!unreadCountElement) {
			unreadCountElement = document.createElement('span');
			unreadCountElement.id = 'unread-bell-count';
			unreadCountElement.className = 'badge';
			document.getElementById('notification-bell').appendChild(unreadCountElement);
		}
		// 현재 카운트 가져오기 및 증가
		const currentCount = parseInt(unreadCountElement.textContent) || 0;
		unreadCountElement.textContent = currentCount + 1;
		setTimeout(() => {
			notificationModal.classList.add("show");
		}, 10);

		setTimeout(() => {
			notificationModal.classList.remove("show");
			setTimeout(() => {
				notificationModal.remove();
			}, 400);
		}, 7000);

		notificationModal.querySelector('.close-notification-modal').addEventListener('click', function() {
			notificationModal.classList.remove("show");
			setTimeout(() => {
				notificationModal.remove();
			}, 400);
		});

		notificationModal.querySelector('.notification-modal-content').addEventListener('click', function() {
			const notificationType = this.getAttribute('data-notification-type');
			const notificationNo = this.getAttribute('data-notification-no');
			const notificationTypePk = this.getAttribute('data-notification-type-pk');
			if (noficationTypeUrl[notificationType]) {
				if (notificationTypePk === null || notificationTypePk === '') {
					window.location.href = noficationTypeUrl[notificationType];
				} else {
					window.location.href = noficationTypeUrl[notificationType] + notificationTypePk;
				}
			} else {
				console.log('알 수 없는 알림 타입:', notificationType);
			}
		});
	}

}
window.onclick = function(event) {
	if (event.target === alarmModal) {
		alarmModal.style.display = "none";
	}
}

// 웹소켓 선언 (공용)
// 전역 웹소켓 변수
let alarmSocket;

function connectWebSocket() {
    if (!alarmSocket || alarmSocket.readyState === WebSocket.CLOSED) {
        alarmSocket = new WebSocket(`ws://localhost:8080/websocket/notifications`);

        alarmSocket.onopen = function() {
            console.log("웹소켓이 연결되었습니다.");
        };

        alarmSocket.onclose = function(event) {
            console.log("웹소켓 연결이 해제되었습니다.", event);
            // 필요시 재연결 로직
        };

        alarmSocket.onerror = function(error) {
            console.error("에러 발생", error);
        };

        alarmSocket.onmessage = function(event) {
            const message = JSON.parse(event.data);
            const currentType = message.nofication_type;
            const notificationModal = document.getElementById('notification-bell-modal');
            if(message.pk != null){
                if(message.type === 'vacationApprovalAlarm'){
                    const title = message.title;
                    const content = message.content;
                    if (notificationModal.children.length <= 1) {
                        notificationModal.innerHTML = `
                        <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                        `;
                        addMarkAsReadListener();
                    }
                    message.data.forEach(function(item) {
                        const listItem = document.createElement('li');
                        showNotification(title, content, item.memberNo, message.timestamp, message.nofication_type, message.pk);

                        listItem.setAttribute('data-notification-no', item.nofication_pk);
                        listItem.setAttribute('data-notification-type', message.nofication_type);
                        listItem.innerHTML = `
                        <strong style="margin-bottom: 5px;">${title}</strong>
                        <p>${content}</p>
                        <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                        <hr style="border: none; margin: 10px 0;">
                        `;
						listItem.addEventListener('click', () => {
								const notificationType = listItem.getAttribute('data-notification-type');
								const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
								if (notificationType === '3') {
								window.location.href = noficationTypeUrl[3]+notificationTypePk;
							}
						});
                        notificationModal.insertBefore(listItem, notificationModal.children[1]);
                    });
                } else if(message.type === 'vacationApprovalReviewsAlarm'){
                    const title = message.title;
                    const content = message.content;
                    if (notificationModal.children.length <= 1) {
                            notificationModal.innerHTML = `
                               <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                            `;
                            addMarkAsReadListener();
                        }
                        message.data.forEach(function(item) {
                            if (Number(item.memberNo) === currentMember) {
                                 showNotification(title, content, item.memberNo,  message.timestamp, message.nofication_type, message.pk);
                                const listItem = document.createElement('li');

                                listItem.setAttribute('data-notification-no', item.nofication_pk);
                                listItem.setAttribute('data-notification-type', message.nofication_type);
                                listItem.innerHTML = `
                                    <strong style="margin-bottom: 5px;">${title}</strong>
                                    <p>${content}</p>
                                    <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                                    <hr style="border: none; margin: 10px 0;">
                                `;
							    listItem.addEventListener('click', () => {
							        const notificationType = listItem.getAttribute('data-notification-type');
							        const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
							        if (notificationType === '4') {
							            window.location.href = noficationTypeUrl[4]+notificationTypePk;
							        }
							    });
	                                notificationModal.insertBefore(listItem, notificationModal.children[1]);
                            }

                        });

                } else if(message.type === 'vacationAppApproveAlarm'){
                    const title = message.title;
                    const content = message.content;

                        if (notificationModal.children.length <= 1) {
                            notificationModal.innerHTML = `
                            <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                            `;
                            addMarkAsReadListener();
                        }
                        message.data.forEach(function(item) {
                            showNotification(title, content, item.memberNo, message.timestamp, message.nofication_type, message.pk);
                            const listItem = document.createElement('li');

                            listItem.setAttribute('data-notification-no', item.nofication_pk);
                            listItem.setAttribute('data-notification-type', message.nofication_type);
                            listItem.innerHTML = `
                            <strong style="margin-bottom: 5px;">${title}</strong>
                            <p>${content}</p>
                            <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                            <hr style="border: none; margin: 10px 0;">
                            `;
							listItem.addEventListener('click', () => {
							    const notificationType = listItem.getAttribute('data-notification-type');
								const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
							    if (notificationType === '5') {
							    window.location.href = noficationTypeUrl[5]+notificationTypePk;
							    } else if(notificationType === '14'){
								window.location.href = noficationTypeUrl[14]+notificationTypePk;
								}
							 });
                            notificationModal.insertBefore(listItem, notificationModal.children[1]);
                        });

                } else if(message.type === 'vacationAppRejectAlarm'){
                    const title = message.title;
                    const content = message.content;
                    if (notificationModal.children.length <= 1) {
                        notificationModal.innerHTML = `
                        <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                        `;
                        addMarkAsReadListener();
                    }
                    message.data.forEach(function(item) {
                        showNotification(title, content, item.memberNo, message.timestamp, message.nofication_type, message.pk);
                        const listItem = document.createElement('li');

                        listItem.setAttribute('data-notification-no', item.nofication_pk);
                        listItem.setAttribute('data-notification-type', message.nofication_type);
                        listItem.innerHTML = `
                        <strong style="margin-bottom: 5px;">${title}</strong>
                        <p>${content}</p>
                        <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                        <hr style="border: none; margin: 10px 0;">
                        `;
						listItem.addEventListener('click', () => {
							const notificationType = listItem.getAttribute('data-notification-type');
							const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
							if (notificationType === '6') {
							window.location.href = noficationTypeUrl[6]+notificationTypePk;
							}
						});
                        notificationModal.insertBefore(listItem, notificationModal.children[1]);
                    });
                } else if(message.type === 'approvalAlarm'){
                    const title = message.title;
                    const content = message.content;
                    if (notificationModal.children.length <= 1) {
                        notificationModal.innerHTML = `
                        <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                        `;
                        addMarkAsReadListener();
                    }
                    message.data.forEach(function(item) {
                        showNotification(title, content, item.memberNo, message.timestamp, message.nofication_type, message.pk);
                        const listItem = document.createElement('li');

                        listItem.setAttribute('data-notification-no', item.nofication_pk);
                        listItem.setAttribute('data-notification-type', message.nofication_type);
                        listItem.innerHTML = `
                        <strong style="margin-bottom: 5px;">${title}</strong>
                        <p>${content}</p>
                        <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                        <hr style="border: none; margin: 10px 0;">
                        `;
						listItem.addEventListener('click', () => {
							const notificationType = listItem.getAttribute('data-notification-type');
							const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
							if (notificationType === '7') {
							window.location.href = noficationTypeUrl[7]+notificationTypePk;
							}
						});
                        notificationModal.insertBefore(listItem, notificationModal.children[1]);
                    });
                } else if(message.type === 'approvalReviewsAlarm'){
                    const title = message.title;
                    const content = message.content;
                    if (notificationModal.children.length <= 1) {
                            notificationModal.innerHTML = `
                               <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                            `;
                            addMarkAsReadListener();
                        }
                        message.data.forEach(function(item) {
                            if (Number(item.memberNo) === currentMember) {
                                const listItem = document.createElement('li');
                                 showNotification(title, content, item.memberNo,  message.timestamp, message.nofication_type, message.pk);

                                listItem.setAttribute('data-notification-no', item.nofication_pk);
                                listItem.setAttribute('data-notification-type', message.nofication_type);
                                listItem.innerHTML = `
                                    <strong style="margin-bottom: 5px;">${title}</strong>
                                    <p>${content}</p>
                                    <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                                    <hr style="border: none; margin: 10px 0;">
                                `;
								listItem.addEventListener('click', () => {
									const notificationType = listItem.getAttribute('data-notification-type');
									const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
									if (notificationType === '8') {
									window.location.href = noficationTypeUrl[8]+notificationTypePk;
									} else if(notificationType === '15'){
									window.location.href = noficationTypeUrl[15]+notificationTypePk;
									}
								});
                                notificationModal.insertBefore(listItem, notificationModal.children[1]);
                            }

                        });

                } else if(message.type === 'appApproveAlarm'){
                    const title = message.title;
                    const content = message.content;
                    if (notificationModal.children.length <= 1) {
                        notificationModal.innerHTML = `
                        <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                        `;
                        addMarkAsReadListener();
                    }
                    message.data.forEach(function(item) {
                        showNotification(title, content, item.memberNo, message.timestamp, message.nofication_type, message.pk);
                        const listItem = document.createElement('li');

                        listItem.setAttribute('data-notification-no', item.nofication_pk);
                        listItem.setAttribute('data-notification-type', message.nofication_type);
                        listItem.innerHTML = `
                        <strong style="margin-bottom: 5px;">${title}</strong>
                        <p>${content}</p>
                        <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                        <hr style="border: none; margin: 10px 0;">
                        `;
						listItem.addEventListener('click', () => {
							const notificationType = listItem.getAttribute('data-notification-type');
							const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
							if (notificationType === '9') {
							window.location.href = noficationTypeUrl[9]+notificationTypePk;
							}
						});
                        notificationModal.insertBefore(listItem, notificationModal.children[1]);
                    });
                } else if(message.type === 'appRejectAlarm'){
                    const title = message.title;
                    const content = message.content;
                    if (notificationModal.children.length <= 1) {
                        notificationModal.innerHTML = `
                        <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                        `;
                        addMarkAsReadListener();
                    }
                    message.data.forEach(function(item) {
                        showNotification(title, content, item.memberNo, message.timestamp, message.nofication_type, message.pk);
                        const listItem = document.createElement('li');

                        listItem.setAttribute('data-notification-no', item.nofication_pk);
                        listItem.setAttribute('data-notification-type', message.nofication_type);
                        listItem.innerHTML = `
                        <strong style="margin-bottom: 5px;">${title}</strong>
                        <p>${content}</p>
                        <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                        <hr style="border: none; margin: 10px 0;">
                        `;
						listItem.addEventListener('click', () => {
							const notificationType = listItem.getAttribute('data-notification-type');
							const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
							if (notificationType === '10') {
							window.location.href = noficationTypeUrl[10]+notificationTypePk;
							}
						});
                        notificationModal.insertBefore(listItem, notificationModal.children[1]);
                    });
                }
            } else {

                if (message.type === 'chatAlarm') {
                    const title = message.title;
                    const content = message.content;
                    if (currentType === window.functionType) {
                        markNotificationsAsRead(window.functionType);
                    } else {
                        if (notificationModal.children.length <= 1) {
                            notificationModal.innerHTML = `
                               <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
                            `;
                            addMarkAsReadListener();
                        }
                        message.data.forEach(function(item) {
                            showNotification(title, item.content, item.memberNo, message.timestamp, 1, "");
                            const listItem = document.createElement('li');

                            listItem.setAttribute('data-notification-no', item.nofication_pk);
                            listItem.setAttribute('data-notification-type', 1);
                            listItem.innerHTML = `
                                <strong style="margin-bottom: 5px;">${title}</strong>
                                <p>${item.content}</p>
                                <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
                                <hr style="border: none; margin: 10px 0;">
                            `;
                        listItem.addEventListener('click', () => {
                            const notificationType = listItem.getAttribute('data-notification-type');
                            if (notificationType === '1') {
                                window.location.href = noficationTypeUrl[1];
                            }
                        });
                            notificationModal.insertBefore(listItem, notificationModal.children[1]);
                        });
                    }
				} else if(message.type === 'documentAlarm'){
				    const title = message.title;
				    const content = message.content;
				    //실시간 추가를 위한
				        if (notificationModal.children.length <= 1) {
				            notificationModal.innerHTML = `
				               <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
				            `;
				            addMarkAsReadListener();
				        }
				        message.data.forEach(function(item) {
				            if (Number(item.memberNo) === currentMember) {
								if (currentType === window.functionType) {
				       				markNotificationsAsRead(window.functionType);
				       			} else{
				                  showNotification(title, content, item.memberNo, message.timestamp, 2, "");
				                  const listItem = document.createElement('li');

				                  listItem.setAttribute('data-notification-no', item.nofication_pk);
				                  listItem.setAttribute('data-notification-type', 2);
				                  listItem.innerHTML = `
				                      <strong style="margin-bottom: 5px;">${title}</strong>
				                      <p>${content}</p>
				                      <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
				                      <hr style="border: none; margin: 10px 0;">
				                  `;
								    listItem.addEventListener('click', () => {
								        const notificationType = listItem.getAttribute('data-notification-type');
								        if (notificationType === '2') {
								            window.location.href = noficationTypeUrl[2];
								        }
								    });
				                  notificationModal.insertBefore(listItem, notificationModal.children[1]);
								}
				            }

				        });
                } else if(message.type === 'noficationDepartmentSchedule'){
                    const title = message.title;
                    const content = message.content;
                    if (window.functionTypes.includes(currentType)) {
                        markNotificationsAsRead(currentType);
                    } else {
						if (notificationModal.children.length <= 1) {
	                        notificationModal.innerHTML = `
	                        <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
	                        `;
	                        addMarkAsReadListener();
	                    }
	                    message.data.forEach(function(item) {
	                        showNotification(title, content, item.memberNo, message.timestamp, 11, "");
	                        const listItem = document.createElement('li');

	                        listItem.setAttribute('data-notification-no', item.nofication_pk);
	                        listItem.setAttribute('data-notification-type', 11);
	                        listItem.innerHTML = `
	                        <strong style="margin-bottom: 5px;">${title}</strong>
	                        <p>${content}</p>
	                        <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
	                        <hr style="border: none; margin: 10px 0;">
	                        `;

							listItem.addEventListener('click', () => {
						        const notificationType = listItem.getAttribute('data-notification-type');
						        if (notificationType === '11') {
						            window.location.href = noficationTypeUrl[11];
						        }
						    });

	                        notificationModal.insertBefore(listItem, notificationModal.children[1]);
	                    });
					}
                } else if(message.type === 'noficationParticipantSchedule'){
                    const title = message.title;
                    const content = message.content;
                    if (window.functionTypes.includes(currentType)) {
                        markNotificationsAsRead(currentType);
                    } else {
						if (notificationModal.children.length <= 1) {
	                        notificationModal.innerHTML = `
	                        <li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
	                        `;
	                        addMarkAsReadListener();
	                    }
	                    message.data.forEach(function(item) {
	                        showNotification(title, content, item.memberNo, message.timestamp, 12, "");
	                        const listItem = document.createElement('li');

	                        listItem.setAttribute('data-notification-no', item.nofication_pk);
	                        listItem.setAttribute('data-notification-type', 12);
	                        listItem.innerHTML = `
	                        <strong style="margin-bottom: 5px;">${title}</strong>
	                        <p>${content}</p>
	                        <em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
	                        <hr style="border: none; margin: 10px 0;">
	                        `;

							 listItem.addEventListener('click', () => {
						        const notificationType = listItem.getAttribute('data-notification-type');
						        if (notificationType === '12') {
						            window.location.href = noficationTypeUrl[11];
						        }
						     });

	                        notificationModal.insertBefore(listItem, notificationModal.children[1]);
                    	});
					}
                } else if(message.type === 'noficationParticipantMeeting'){
					const title = message.title;
					const content = message.content;
					if (window.functionTypes.includes(currentType)) {
						markNotificationsAsRead(currentType);
					} else {
						if (notificationModal.children.length <= 1) {
							notificationModal.innerHTML = `
            				<li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
            				`;
							addMarkAsReadListener();
						}
						message.data.forEach(function(item) {
							showNotification(title, content, item.memberNo, message.timestamp, 13, "");
							const listItem = document.createElement('li');

							listItem.setAttribute('data-notification-no', item.nofication_pk);
							listItem.setAttribute('data-notification-type', 13);
							listItem.innerHTML = `
            				<strong style="margin-bottom: 5px;">${title}</strong>
            				<p>${content}</p>
            				<em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
            				<hr style="border: none; margin: 10px 0;">
            				`;

							listItem.addEventListener('click', () => {
								const notificationType = listItem.getAttribute('data-notification-type');
								if (notificationType === '13') {
									window.location.href = noficationTypeUrl[11];
								}
							});
							notificationModal.insertBefore(listItem, notificationModal.children[1]);
						});
					}
				} else if (message.type === 'noticeAlarm') {
					const title = message.title;
					const content = message.content;
					console.log(title);
					if (notificationModal.children.length <= 1) {
						notificationModal.innerHTML = `
        					<li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
        					`;
						addMarkAsReadListener();
					}
					message.data.forEach(function(item) {
						showNotification(title, content, item.memberNo, message.timestamp, 14, "");
						const listItem = document.createElement('li');

						listItem.setAttribute('data-notification-no', item.nofication_pk);
						listItem.setAttribute('data-notification-type', 14);
						listItem.innerHTML = `
        					<strong style="margin-bottom: 5px;">${title}</strong>
        					<p>${content}</p>
        					<em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
        					<hr style="border: none; margin: 10px 0;">
        					`;

						listItem.addEventListener('click', () => {
							const notificationType = listItem.getAttribute('data-notification-type');
							if (notificationType === '14') {
								window.location.href = noficationTypeUrl[14];
							}
						});
						notificationModal.insertBefore(listItem, notificationModal.children[1]);
					});
				} else if (message.type === 'surveyCreateAlarm') {  
					const title = message.title;
					const content = message.content;
					if (notificationModal.children.length <= 1) {
						notificationModal.innerHTML = `
					<li id="mark-as-read" class="mark-as-read" style="font-size: 10px; text-align: right; color: gray;">일괄읽음</li>
					`;
						addMarkAsReadListener();
					}
					message.data.forEach(function(item) {
						showNotification(title, content, item.memberNo, message.timestamp, 15, "");  
						const listItem = document.createElement('li');

						listItem.setAttribute('data-notification-no', item.nofication_pk);
						listItem.setAttribute('data-notification-type', 15);  
						listItem.innerHTML = `
					<strong style="margin-bottom: 5px;">${title}</strong>
					<p>${content}</p>
					<em style="display: block; margin-bottom: 5px; float: right;">${message.timestamp}</em>
					<hr style="border: none; margin: 10px 0;">
					`;

						listItem.addEventListener('click', () => {
							const notificationType = listItem.getAttribute('data-notification-type');
							const notificationTypePk = listItem.getAttribute('data-notification-type-pk');
							if (notificationType === '15') {  
								window.location.href = noficationTypeUrl[15] + notificationTypePk;
							}
						});
						notificationModal.insertBefore(listItem, notificationModal.children[1]);
					});
				}
			}
		}

	}
}
if (!alarmSocket) {
	connectWebSocket();

}

// 안읽음을 읽음으로 처리하는 함수(공동 사용)
function markNotificationsAsRead(functionType) {
	fetch(`/api/nofication/type/read/${currentMember}/${functionType}`)
		.then(response => response.json())
		.then(data => {
			if (data.read) {
				console.log('읽음 처리 완료');
				bellUnreadCount();
			} else {
				console.error('읽음 처리 실패');
			}
		})
		.catch(error => {
			console.error('읽음 처리 중 오류 발생:', error);
		});
}

// 안읽음을 읽음으로 처리하는 함수(전자 결재용)
function markApprovalAsRead(functionType, noficationTypePk) {
	fetch(`/api/nofication/type/Approval/read/${currentMember}/${functionType}/${noficationTypePk}`)
		.then(response => response.json())
		.then(data => {
			if (data.read) {
				console.log('읽음 처리 완료');
				bellUnreadCount();
			} else {
				console.error('읽음 처리 실패');
			}
		})
		.catch(error => {
			console.error('읽음 처리 중 오류 발생:', error);
		});
}
//------ 수정불가 ------