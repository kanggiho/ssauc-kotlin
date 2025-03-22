// 로그아웃 함수
function logout() {
    alert("로그아웃 되었습니다.");
    window.location.href = "/admin/logout";
}

// 토글 서브메뉴 함수 (클릭한 메뉴에 active 클래스 추가 및 동적 포커스)
function toggleSubmenu(element, submenuId) {
    const menuItems = document.querySelectorAll('.menu-item');
    const submenus = document.querySelectorAll('.submenu');

    // 모든 메뉴 항목에서 active 클래스 제거 및 포커스 해제
    menuItems.forEach(item => {
        item.classList.remove('active');
        item.blur();
    });

    // 모든 서브메뉴 숨기기
    submenus.forEach(submenu => {
        submenu.style.display = 'none';
    });

    // 클릭한 메뉴 항목이 active 상태가 아니라면 활성화 후 포커스
    if (!element.classList.contains('active')) {
        element.classList.add('active');
        element.focus();
        const submenu = document.getElementById(submenuId);
        if (submenu) {
            submenu.style.display = 'block';
        }
    } else {
        // 이미 활성화된 상태이면 토글(닫기) 처리
        element.classList.remove('active');
        element.blur();
    }
}

// 서브메뉴 항목 활성화 함수 (클릭한 항목에 active 클래스 부여 및 포커스)
function activateSubmenu(element) {
    const submenuItems = document.querySelectorAll('.submenu-item');
    submenuItems.forEach(item => {
        item.classList.remove('active');
        item.blur();
    });
    element.classList.add('active');
    element.focus();
}
