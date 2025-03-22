// user-modal.js (공통 JS 파일에 포함)
function openUserInfoModal(userName) {
    fetch(`/mypage/info/json?userName=${encodeURIComponent(userName)}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById('modalProfileImage').src = user.profileImage;
            document.getElementById('modalUserName').innerText = user.userName;
            document.getElementById('modalReputation').innerText = user.reputation;
            document.getElementById('modalLocation').innerText = user.location;
            document.getElementById('modalCreatedAt').innerText = user.createdAt;
            document.getElementById('modalLastLogin').innerText = user.lastLogin;
            document.getElementById('modalReviewTitle').innerText = user.userName + "님에 대한 리뷰 요약";

            const summary = user.reviewSummary;
            document.getElementById('modalReviewSummary').innerText =
                summary ? summary : "아직 리뷰가 충분하지 않네요. 리뷰를 작성해보시는 건 어떨까요?";

            document.getElementById('userInfoModal').classList.remove('hidden');
            document.getElementById('userInfoModal').classList.add('show');
        })
        .catch(error => {
            console.error("사용자 정보를 불러오는 중 오류 발생:", error);
        });
}

function closeModal() {
    document.getElementById('userInfoModal').classList.remove('show');
    document.getElementById('userInfoModal').classList.add('hidden');
}

function handleSellerClick(event) {
    event.stopPropagation();
    const sellerName = event.currentTarget.dataset.sellername;
    console.log("Seller clicked:", sellerName);
    openUserInfoModal(sellerName);
}