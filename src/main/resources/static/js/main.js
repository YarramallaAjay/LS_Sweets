document.addEventListener("DOMContentLoaded", function () {

    // WEIGHT BUTTON SELECT
    document.querySelectorAll(".product-card").forEach(card => {

        const weightButtons =
            card.querySelectorAll(".weight-btn");

        const priceInput =
            card.querySelector(".price-id");

        if (weightButtons.length > 0) {

            // DEFAULT SELECT FIRST PRICE
            weightButtons[0].classList.add("active");

            priceInput.value =
                weightButtons[0].dataset.priceId;
        }

        weightButtons.forEach(btn => {

            btn.addEventListener("click", function () {

                weightButtons.forEach(b =>
                    b.classList.remove("active"));

                this.classList.add("active");

                priceInput.value =
                    this.dataset.priceId;
            });

        });

    });




    // ADD TO CART AJAX
    document.querySelectorAll(".add-form").forEach(form => {

        form.addEventListener("submit", async function (e) {

            e.preventDefault();

            const productId =
                form.querySelector(
                    'input[name="productId"]'
                ).value;

            const priceId =
                form.querySelector(
                    'input[name="priceId"]'
                ).value;

            const quantity =
                form.querySelector(
                    'input[name="quantity"]'
                ).value;

            // IMPORTANT FIX
            if (!priceId) {

                alert("Please select weight");

                return;
            }

            const csrfToken =
                document.querySelector(
                    'meta[name="_csrf"]'
                ).content;

            const csrfHeader =
                document.querySelector(
                    'meta[name="_csrf_header"]'
                ).content;

            const formData =
                new URLSearchParams();

            formData.append(
                "productId",
                productId
            );

            formData.append(
                "priceId",
                priceId
            );

            formData.append(
                "quantity",
                quantity
            );

            const response = await fetch(
                "/cart/add",
                {
                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/x-www-form-urlencoded",

                        [csrfHeader]:
                            csrfToken
                    },

                    body: formData
                }
            );

            if (response.ok) {

                openMiniCart();
                
                
                const button =
    				form.querySelector(".add-btn");

				button.innerHTML = "✔ Added";

				setTimeout(() => {

    			button.innerHTML = "Add to Cart";

				}, 1200);


                refreshMiniCart();

            } else {

                alert("Failed to add item");

            }

        });

    });

});


/*
window.onload = function() {
    window.scrollTo(0, 0);
};*/

function searchProducts() {

    let input = document.getElementById("searchInput").value.toLowerCase();
    let resultsBox = document.getElementById("searchResults");
    let products = document.querySelectorAll(".product-card");

    resultsBox.innerHTML = "";

    if (input === "") {
        resultsBox.style.display = "none";
        return;
    }

    let found = false;

    products.forEach(function(product) {

        let nameAttr = product.getAttribute("data-name");

        if (!nameAttr) return;   // 🔥 IMPORTANT SAFETY LINE

        let name = nameAttr.toLowerCase();

        if (name.includes(input)) {

            found = true;

            let image = product.getAttribute("data-image");
            let id = product.getAttribute("data-id");

            let item = `
                <div class="result-item" onclick="scrollToProduct('${id}')">
                    <img src="${image}">
                    <span>${nameAttr}</span>
                </div>
            `;

            resultsBox.innerHTML += item;
        }
    });

    if (!found) {
        resultsBox.innerHTML = "<div class='no-result'>No sweets found</div>";
    }

    resultsBox.style.display = "block";
    resultsBox.classList.add("show")
}

function scrollToProduct(id) {
    let element = document.querySelector("[data-id='" + id + "']");
    if (element) {
        element.scrollIntoView({ behavior: "smooth" });
    }
    document.getElementById("searchResults").style.display = "none";
    document.getElementById("searchInput").value = "";
}


document.addEventListener("click", function(event) {

    let searchWrapper = document.querySelector(".search-wrapper");

    if (!searchWrapper.contains(event.target)) {
        document.getElementById("searchResults").style.display = "none";
    }

});



function openMiniCart() {

    document
        .getElementById("miniCart")
        .classList.add("active");

    document
        .getElementById("miniCartOverlay")
        .classList.add("active");
}

function closeMiniCart() {

    document
        .getElementById("miniCart")
        .classList.remove("active");

    document
        .getElementById("miniCartOverlay")
        .classList.remove("active");
}


function updateMiniCart(productId, priceId, quantity) {

    const csrfToken =
        document.querySelector('meta[name="_csrf"]').content;

    const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/cart/update', {

        method: 'POST',

        headers: {

            'Content-Type':
                'application/x-www-form-urlencoded',

            [csrfHeader]: csrfToken
        },

        body:
            'productId=' + productId +
            '&priceId=' + priceId +
            '&quantity=' + quantity
    })

    .then(response => {

        if (response.ok) {

            // RELOAD ONLY MINI CART CONTENT
            refreshMiniCart();

        }

    });

}






async function refreshMiniCart() {

    const response = await fetch("/cart/mini");

    const html = await response.text();

    document.getElementById("miniCartContent").innerHTML = html;

    updateCartBadge();
}



async function updateCartBadge() {

    const response = await fetch("/cart/count");

    const count = await response.text();

    let badge =
        document.getElementById("cartCountBadge");

    if (parseInt(count) > 0) {

        if (!badge) {

            const button =
                document.querySelector(".btn-outline-danger");

            badge = document.createElement("span");

            badge.id = "cartCountBadge";

            badge.className =
                "position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger";

            button.appendChild(badge);
        }

        badge.innerText = count;

    } else {

        if (badge) {
            badge.remove();
        }
    }
}