/**
 * plp.js (검색/필터/정렬/페이지네이션/자동완성/연관검색어)
 * + 상품 렌더링 시, list.html과 동일한 정보(즉시구매가격, 입찰 횟수, 남은 시간, 위치, 좋아요수) 표시
 */

document.addEventListener("DOMContentLoaded", function () {
    console.log("🚀 PLP 페이지 로딩됨");

    // -------------------------
    // 주요 DOM 요소
    // -------------------------
    const searchInput       = document.getElementById("searchInput");
    const searchIcon        = document.getElementById("searchIcon");
    const searchAlert       = document.getElementById("searchAlert");
    const productGrid       = document.getElementById("productGrid");
    const paginationEl      = document.getElementById("pagination");
    const sortSelect        = document.getElementById("sortSelect");
    const auctionOnlyCheckbox = document.getElementById("auctionOnlyCheckbox");
    const filterCategoryEl  = document.getElementById("filterCategory");
    const minPriceInput     = document.getElementById("minPriceInput");
    const maxPriceInput     = document.getElementById("maxPriceInput");
    const filterResetBtn    = document.getElementById("filterResetBtn");
    const totalCountLabel   = document.getElementById("totalCountLabel");

    const autoCompleteList  = document.getElementById("autoCompleteList");
    const relatedSearchContainer = document.getElementById("relatedSearchContainer");
    const relatedSearchList = document.getElementById("relatedSearchList");

    // -------------------------
    // 전역 상태
    // -------------------------
    let currentKeyword   = "";
    let currentPage      = 1;
    let pageSize         = 30;
    let currentSort      = "VIEW_DESC";
    let auctionOnly      = false;
    let selectedCategories = [];
    let minPrice         = null;
    let maxPrice         = null;

    // URL 파라미터에서 keyword 추출
    const urlParams = new URLSearchParams(window.location.search);
    const keywordParam = urlParams.get("keyword");
    if (keywordParam) {
        currentKeyword = keywordParam.trim();
        if (searchInput) searchInput.value = currentKeyword;
    }

    // -------------------------
    // 상품 목록 로딩 (백엔드 API 호출)
    // -------------------------
    function loadProducts() {
        const params = new URLSearchParams();

        if (currentKeyword) params.append("keyword", currentKeyword);
        params.append("page", currentPage);
        params.append("size", pageSize);
        params.append("sort", currentSort);
        if (auctionOnly) params.append("auctionOnly", "true");
        if (selectedCategories.length > 0) {
            params.append("categories", selectedCategories.join(","));
        }
        if (minPrice !== null && maxPrice !== null) {
            params.append("minPrice", minPrice);
            params.append("maxPrice", maxPrice);
        }

        // 실제 구현 시, plp용 엔드포인트(/api/products/plp) 맞추세요
        const url = "/api/products/plp?" + params.toString();
        console.log("📡 [PLP] API 호출:", url);

        fetch(url)
            .then(res => res.json())
            .then(data => {
                // data 구조 가정: { products:[], totalCount:0, page:1, totalPages:1 }
                renderProducts(data.products);
                renderPagination(data.page, data.totalPages);
                if (totalCountLabel) {
                    totalCountLabel.textContent = `총 ${data.totalCount}개의 상품`;
                }
            })
            .catch(err => console.error("❌ 상품 API 오류:", err));
    }

    // -------------------------
    // 상품 렌더링
    // (list.html처럼: 사진, 상품명, 즉시구매가, 입찰횟수, 남은시간, 판매자 위치, 좋아요 수)
    // -------------------------
    function renderProducts(products) {
        if (!productGrid) return;
        if (!products || products.length === 0) {
            productGrid.innerHTML = `<p class="text-center">❌ 검색된 상품이 없습니다.</p>`;
            return;
        }

        productGrid.innerHTML = products.map(product => {
            // 남은 시간 계산(예: endAt vs 현재시간)
            let gapText = "⏳ 입찰 마감";
            let isExpired = false; // 입찰 마감 여부 플래그

            if (product.endAt) {
                const now = new Date();
                const endTime = new Date(product.endAt);
                const diffMs = endTime - now;
                if (diffMs > 0) {
                    // 남아있는 경우
                    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
                    const diffHours = Math.floor((diffMs / (1000 * 60 * 60)) % 24);
                    gapText = `⏳ ${diffDays}일 ${diffHours}시간`;
                } else {
                    isExpired = true; // 입찰 마감됨
                }
            }

            // ✅ 입찰 마감이거나 판매 완료된 경우
            const isSoldOut = product.status === "판매완료"; // 판매완료 여부
            const applyOpacity = isExpired || isSoldOut; // 하나라도 true면 흐리게 처리
            const imageStyle = applyOpacity ? 'style="filter: opacity(0.3) drop-shadow(0 0 0 #000000);"' : '';

            // 상품 가격 (즉시 구매가)
            const displayPrice = (product.price || 0).toLocaleString() + "원";

            // 입찰 횟수
            const bidCountText = `입찰 수: ${product.bidCount || 0}회`;

            // 좋아요 수
            const likeCountText = product.likeCount || 0;

            // 판매자 위치 (Users 엔티티의 seller 객체에서 가져옴)
            const locationText = product.location ? product.location : "위치정보 없음";

            // 좋아요 여부 => 버튼 아이콘
            const heartClass = product.liked ? 'bi bi-heart-fill' : 'bi bi-heart';

            return `
        <div class="col">
            <div class="card product-card">
                <button class="icon-btn" data-product-id="${product.productId}" onclick="toggleHeart(this)">
                    <i class="${heartClass}"></i>
                </button>
                <a href="/bid/bid?productId=${product.productId}">
                    <div class="image-container">
                        <img class="product-img" ${imageStyle} src="${product.imageUrl}" alt="상품이미지">
                        ${applyOpacity ? `<div class="overlay-text">⏳ 입찰 마감</div>` : ''}
                    </div>
                    <div class="card-body">
                        <p class="product-title">${product.name || '상품명'}</p>
                        <p class="product-price">${displayPrice}</p>
                        <p class="product-info">${bidCountText} | ${gapText}</p>
                        <p class="product-info">
                            ${locationText} | ❤️ <span class="like-count">${likeCountText}</span>
                        </p>
                    </div>
                </a>
            </div>
        </div>
    `;
        }).join('');

    }

    // -------------------------
    // 좋아요 토글
    // (기존 plp.js와 동일)
    // -------------------------
    window.toggleHeart = function (button) {
        const productId = button.getAttribute("data-product-id");
        const icon = button.querySelector("i");
        const likeCountElement = button.closest(".product-card").querySelector(".like-count");

        let currentCount = parseInt(likeCountElement?.textContent?.replace(/,/g, ''), 10) || 0;

        fetch("/api/like", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ productId })
        })
            .then(res => res.json())
            .then(data => {
                // liked=true => fill, liked=false => empty
                icon.classList.toggle("bi-heart-fill", data.liked);
                icon.classList.toggle("bi-heart", !data.liked);
                if (data.liked) {
                    currentCount++;
                } else {
                    currentCount = Math.max(0, currentCount - 1);
                }
                likeCountElement.textContent = currentCount.toLocaleString();
            })
            .catch(err => console.error("❌ 좋아요 API 오류:", err));
    };

    // -------------------------
    // 검색 기능
    // -------------------------
    function performSearch() {
        const query = searchInput.value.trim();
        if (!query) {
            if (searchAlert) searchAlert.style.display = "block";
            return;
        }
        if (searchAlert) searchAlert.style.display = "none";
        currentKeyword = query;
        currentPage = 1;

        // 검색어 저장 API
        fetch("/api/save-search", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ keyword: query })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("검색어 저장 실패");
                }
                return response.json();
            })
            .then(data => {
                console.log("✅ 검색어 저장 완료:", query);
                // 새로고침 or loadProducts() 호출
                window.location.href = `/plp?keyword=${encodeURIComponent(query)}`;
            })
            .catch(error => {
                console.error("❌ 검색어 저장 오류:", error);
                window.location.href = `/plp?keyword=${encodeURIComponent(query)}`;
            });
    }

    // 검색 input 'Enter'
    if (searchInput) {
        searchInput.addEventListener("keypress", function(e) {
            if (e.key === "Enter") {
                e.preventDefault();
                performSearch();
            }
        });
    }
    // 검색 아이콘 클릭
    if (searchIcon) {
        searchIcon.addEventListener("click", performSearch);
    }

    // -------------------------
    // 자동완성 + 연관검색어
    // (기존 plp.js 로직)
    // -------------------------
    searchInput.addEventListener("input", function(e) {
        const prefix = e.target.value.trim();
        if (!prefix) {
            autoCompleteList.style.display = "none";
            relatedSearchContainer.style.display = "none";
            return;
        }
        // 자동완성
        fetch(`/api/autocomplete?prefix=${encodeURIComponent(prefix)}`)
            .then(res => res.json())
            .then(suggestions => showAutoComplete(suggestions))
            .catch(err => console.error("❌ 자동완성 API 오류:", err));

        // 연관 검색어
        fetch(`/api/related-search?keyword=${encodeURIComponent(prefix)}`)
            .then(res => res.json())
            .then(relatedData => showRelatedKeywords(relatedData))
            .catch(err => console.error("❌ 연관 검색어 API 오류:", err));
    });

    function showAutoComplete(suggestions) {
        if (!suggestions || suggestions.length === 0) {
            autoCompleteList.style.display = "none";
            return;
        }
        const rect = searchInput.getBoundingClientRect();
        autoCompleteList.style.left = rect.left + "px";
        autoCompleteList.style.top  = (rect.bottom + window.scrollY) + "px";
        autoCompleteList.style.width= rect.width + "px";

        autoCompleteList.innerHTML = "";
        suggestions.forEach(sugg => {
            const li = document.createElement("li");
            li.textContent = sugg;
            li.style.cursor = "pointer";
            li.style.padding = "5px 10px";
            li.addEventListener("click", () => {
                searchInput.value = sugg;
                autoCompleteList.style.display = "none";
                performSearch();
            });
            autoCompleteList.appendChild(li);
        });
        autoCompleteList.style.display = "block";
    }

    function showRelatedKeywords(keywords) {
        if (!keywords || keywords.length === 0) {
            relatedSearchContainer.style.display = "none";
            relatedSearchList.innerHTML = "";
            return;
        }
        relatedSearchContainer.style.display = "block";
        relatedSearchList.innerHTML = keywords.map(k => `<li style="cursor:pointer; color:blue; list-style:none;">${k}</li>`).join("");
        relatedSearchList.querySelectorAll("li").forEach(item => {
            item.addEventListener("click", () => {
                searchInput.value = item.textContent;
                performSearch();
            });
        });
    }

    // -------------------------
    // 페이지네이션
    // -------------------------
    function renderPagination(currentPageNum, totalPages) {
        if (!paginationEl) return;
        paginationEl.innerHTML = "";
        if (totalPages <= 1) return;

        for (let i = 1; i <= totalPages; i++) {
            const pageBtn = document.createElement("button");
            pageBtn.className = `page-btn ${i === currentPageNum ? "active" : ""}`;
            pageBtn.textContent = i;
            pageBtn.addEventListener("click", () => {
                currentPage = i;
                loadProducts();
            });
            paginationEl.appendChild(pageBtn);
        }
    }

    // -------------------------
    // 카테고리 목록 로딩
    // (필요 시 API로 불러오거나, 상수로 둬도 됨)
    // -------------------------
    function loadCategories() {
        // 예시: /api/products/categories
        fetch("/api/products/categories")
            .then(res => res.json())
            .then(categories => {
                if (!categories || categories.length === 0) {
                    categories = ["기타"];
                }
                filterCategoryEl.innerHTML = categories.map(cat => `
                    <li>
                        <label>
                            <input type="checkbox" value="${cat}"> ${cat}
                        </label>
                    </li>
                `).join("");
                filterCategoryEl.querySelectorAll("input[type=checkbox]").forEach(chk => {
                    chk.addEventListener("change", function() {
                        selectedCategories = Array.from(
                            filterCategoryEl.querySelectorAll("input[type=checkbox]:checked")
                        ).map(c => c.value);
                        currentPage = 1;
                        loadProducts();
                    });
                });
            })
            .catch(err => console.error("❌ 카테고리 API 오류:", err));
    }

    // -------------------------
    // 정렬
    // -------------------------
    if (sortSelect) {
        sortSelect.addEventListener("change", function() {
            currentSort = this.value;
            currentPage = 1;
            loadProducts();
        });
    }

    // -------------------------
    // 경매만 보기
    // -------------------------
    if (auctionOnlyCheckbox) {
        auctionOnlyCheckbox.addEventListener("change", function() {
            auctionOnly = this.checked;
            currentPage = 1;
            loadProducts();
        });
    }

    // -------------------------
    // 가격 필터
    // -------------------------
    window.filterByInputPrice = function() {
        const minVal = parseInt(minPriceInput.value) || 0;
        const maxVal = parseInt(maxPriceInput.value) || 999999999;
        if (minVal > maxVal) {
            alert("최소 가격이 최대 가격보다 큽니다.");
            return;
        }
        minPrice = minVal;
        maxPrice = maxVal;
        currentPage = 1;
        loadProducts();
    };

    // -------------------------
    // 필터 초기화
    // -------------------------
    filterResetBtn.addEventListener("click", function() {
        auctionOnlyCheckbox.checked = false;
        auctionOnly = false;
        selectedCategories = [];
        minPrice = null;
        maxPrice = null;
        minPriceInput.value = "";
        maxPriceInput.value = "";
        sortSelect.value = "VIEW_DESC";
        currentSort = "VIEW_DESC";
        currentPage = 1;
        filterCategoryEl.querySelectorAll("input[type=checkbox]").forEach(chk => chk.checked = false);
        loadProducts();
    });

    // -------------------------
    // 초기 로드
    // -------------------------
    loadCategories(); // 카테고리 목록 불러오기
    loadProducts();   // 상품 목록 불러오기
});
