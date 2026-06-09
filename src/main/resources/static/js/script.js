let cart = JSON.parse(localStorage.getItem("cart")) || [];
console.log("Loaded cart:", cart);

   
//let cart = JSON.parse(localStorage.getItem("cart")) || [];
const hamburger = document.getElementById('hamburger');
const navLinks = document.getElementById('navLinks');
const cartIcon = document.getElementById('cartIcon');
const cartDrawer = document.getElementById('cartDrawer');
const closeCart = document.getElementById('closeCart');


// 🔹 CART ELEMENTS

const cartCounter = document.getElementById('cart-counter');
const cartItemsDiv = document.getElementById('cart-items');
const cartTotal = document.getElementById('cart-total');
const cartCloseBtn = document.getElementById('cart-close');
const clearCartBtn = document.getElementById('clear-cart');


const cartButton = document.querySelector('.cart');
const cartCloseBtnMain = document.getElementById('cart-close');
// let cart = [];


hamburger.addEventListener('click', () => {
  navLinks.classList.toggle('show');
});

cartIcon.addEventListener('click', () => {
  cartDrawer.classList.add('show');
});

closeCart.addEventListener('click', () => {
  cartDrawer.classList.remove('show');
});











//let cart = JSON.parse(localStorage.getItem("cart")) || [];

// 🛒 ADD TO CART (with working weight buttons)
document.querySelectorAll('.add-cart').forEach(button => {
  button.addEventListener('click', () => {

    const product = button.closest('.product');
    const name = product.dataset.name;
    const basePrice = parseInt(product.dataset.price);

    const activeWeightBtn = product.querySelector('.weight-btn.active');
    const extra = parseInt(activeWeightBtn.dataset.extra);
    const weightLabel = activeWeightBtn.textContent.trim();

    const finalPrice = basePrice + extra;

    const itemName = `${name} (${weightLabel})`;

    const existing = cart.find(item => item.name === itemName);

    if (existing) {
      existing.quantity += 1;
    } else {
      cart.push({ name: itemName, price: finalPrice, quantity: 1 });
    }

    updateCart();

    // Button effect
    button.classList.add("added");
    const original = button.textContent;
    button.textContent = "Added!";
    setTimeout(() => {
      button.classList.remove("added");
      button.textContent = original;
    }, 1200);
  });
});




// ✅ Handle live weight selection and price updates
document.querySelectorAll('.product').forEach(product => {
  const basePrice = parseInt(product.dataset.price);
  const priceElement = product.querySelector('.price');
  const weightButtons = product.querySelectorAll('.weight-btn');

  weightButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      // Remove active class from all buttons
      weightButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      // Get extra price and calculate new total
      const extra = parseInt(btn.dataset.extra);
      const newPrice = basePrice + extra;

      // Update the price in UI instantly
      priceElement.textContent = `₹${newPrice}`;
    });
  });
});

// 🔹 UPDATE CART DISPLAY
function updateCart() {
  cartItemsDiv.innerHTML = '';
  let total = 0;
  let totalQty = 0;

  cart.forEach((item, index) => {
    total += item.price * item.quantity;
    totalQty += item.quantity;

    const div = document.createElement('div');
    div.className = 'cart-item';
    div.innerHTML = `
      <div class="cart-item-info">
        <span>${item.name}</span>
        <div class="cart-qty">
          <button class="qty-btn" onclick="changeQty(${index}, -1)">−</button>
          <span class="qty-count">${item.quantity}</span>
          <button class="qty-btn" onclick="changeQty(${index}, 1)">+</button>
        </div>
        <span>₹${item.price * item.quantity}</span>
      </div>
      <button class="remove-btn" onclick="removeItem(${index})">✖</button>
    `;
    cartItemsDiv.appendChild(div);
  });

  cartCounter.textContent = totalQty;
  cartTotal.textContent = `Total: ₹${total}`;
}

// 🔹 CHANGE QUANTITY
function changeQty(index, delta) {
  cart[index].quantity += delta;
  if (cart[index].quantity <= 0) cart.splice(index, 1);
  updateCart();
}

// 🔹 REMOVE ITEM
function removeItem(index) {
  cart.splice(index, 1);
  updateCart();
}

// 🔹 CLEAR CART
if (clearCartBtn) {
  clearCartBtn.addEventListener('click', () => {
    cart = [];
    updateCart();
  });
}


//Close drawer when clicking outside
document.addEventListener('click', (e) => {
  if (!cartDrawer.contains(e.target) && !cartButton.contains(e.target)) {
   cartDrawer.classList.remove('open');
  }
});




/* ===== Full-screen Search Overlay + Quick View ===== */
// selectors
const navSearchIcon = document.querySelector('.search-icon') || document.querySelector('.search-toggle') || null;
const searchOverlay = document.getElementById('search-overlay');
const overlayInput = document.getElementById('overlay-search-input');
const resultsContainer = document.getElementById('search-results');
const searchCloseBtn = document.querySelector('.search-close');

const quickviewModal = document.getElementById('quickview-modal');
const quickviewClose = document.querySelector('.quickview-close');
const qvImg = document.getElementById('qv-img');
const qvTitle = document.getElementById('qv-title');
const qvPrice = document.getElementById('qv-price');
const qvWeightArea = document.getElementById('qv-weight-area');
const qvAddCart = document.getElementById('qv-add-cart');

let lastMatchedProducts = []; // store references

// helpers
function openOverlay() {
  searchOverlay.classList.add('open');
  overlayInput.value = '';
  resultsContainer.innerHTML = '';
  setTimeout(()=> overlayInput.focus(), 120);
}

function closeOverlay() {
  searchOverlay.classList.remove('open');
}

function openCart() {
   cartDrawer.classList.add('open');
}

function openQuickView() { quickviewModal.classList.add('open'); }
function closeQuickView() { quickviewModal.classList.remove('open'); }

// find page products
function gatherProducts() {
  // returns array of elements
  return Array.from(document.querySelectorAll('.product'));
}

// compute extra rules (adjust if you have custom rules per product)
function extraForValue(val) {
  if (val === '2') return 150;
  if (val === '3') return 350;
  return 0;
}

// render search result item (small card)
function renderResultCard(prodElem) {
  const name = prodElem.dataset.name || prodElem.querySelector('h3')?.innerText || '';
  // attempt to find image
  const img = prodElem.querySelector('img') ? prodElem.querySelector('img').src : '';
  // price text
  const priceText = prodElem.querySelector('.price') ? prodElem.querySelector('.price').textContent : (`₹${prodElem.dataset.price || ''}`);

  const wrapper = document.createElement('div');
  wrapper.className = 'search-result';
  wrapper.innerHTML = `
    <img src="${img}" alt="${name}">
    <div class="sr-info">
      <h4>${name}</h4>
      <p>${priceText}</p>
    </div>
  `;
  // click opens quick view for that product element
  wrapper.addEventListener('click', () => {
    openQuickViewForProduct(prodElem);
  });
  return wrapper;
}

// perform search and show results
function performOverlaySearch(query) {
  const needle = query.trim().toLowerCase();
  resultsContainer.innerHTML = '';

  const products = gatherProducts();
  lastMatchedProducts = products.filter(p => {
    const name = (p.dataset.name || p.querySelector('h3')?.textContent || '').toLowerCase();
    return name.includes(needle);
  });

  if (!needle) {
    // optionally show top products or nothing
    resultsContainer.innerHTML = '<p style="color:#ccc;padding:12px">Type to search items...</p>';
    return;
  }

  if (lastMatchedProducts.length === 0) {
    resultsContainer.innerHTML = '<p style="color:#ccc;padding:12px">No items found.</p>';
    return;
  }

  // append cards
  lastMatchedProducts.forEach(p => {
    resultsContainer.appendChild(renderResultCard(p));
  });
}

// create weight controls for quick view (uses source product's select options if present)
function buildWeightControls(sourceProduct, selectedValue='1') {
  qvWeightArea.innerHTML = ''; // reset
  // try to find weight-select or weight-btns
  const select = sourceProduct.querySelector('.weight-select');
  if (select) {
    // use select options
    Array.from(select.options).forEach(opt => {
      const btn = document.createElement('button');
      btn.className = 'weight-btn';
      btn.textContent = opt.textContent;
      btn.dataset.value = opt.value;
      if (opt.value === selectedValue) btn.classList.add('active');
      btn.addEventListener('click', () => {
        qvWeightArea.querySelectorAll('.weight-btn').forEach(b=>b.classList.remove('active'));
        btn.classList.add('active');
        updateQvPriceFromSource(sourceProduct);
      });
      qvWeightArea.appendChild(btn);
    });
  } else {
    // fallback default buttons
    const defaults = [{v:'1',t:'250g'},{v:'2',t:'500g'},{v:'3',t:'1kg'}];
    defaults.forEach(d => {
      const btn = document.createElement('button');
      btn.className = 'weight-btn' + (d.v===selectedValue ? ' active' : '');
      btn.textContent = d.t;
      btn.dataset.value = d.v;
      btn.addEventListener('click', () => {
        qvWeightArea.querySelectorAll('.weight-btn').forEach(b=>b.classList.remove('active'));
        btn.classList.add('active');
        updateQvPriceFromSource(sourceProduct);
      });
      qvWeightArea.appendChild(btn);
    });
  }
}

// update price shown in quick view using source product base price and selected weight
function updateQvPriceFromSource(sourceProduct) {
  // attempt to read base price from product dataset or displayed .price
  let base = parseInt(sourceProduct.dataset.price) || 0;
  // If displayed price contains currency, extract number
  const priceEl = sourceProduct.querySelector('.price');
  if (priceEl) {
    const match = priceEl.textContent.replace(/,/g,'').match(/\d+/);
    if (match) base = parseInt(match[0]);
  }

  // get selected weight in quick view
  const active = qvWeightArea.querySelector('.weight-btn.active');
  const val = active ? active.dataset.value : '1';
  const extra = extraForValue(val);
  const finalPrice = base + extra;
  qvPrice.textContent = `₹${finalPrice}`;
  return finalPrice;
}

// open quick view and populate from product element
function openQuickViewForProduct(prodElem) {
  // populate
  const name = prodElem.dataset.name || prodElem.querySelector('h3')?.textContent;
  const imgSrc = prodElem.querySelector('img') ? prodElem.querySelector('img').src : '';
  qvImg.src = imgSrc;
  qvImg.alt = name;
  qvTitle.textContent = name;

  // build weight controls based on product
  buildWeightControls(prodElem);

  // compute and set price
  updateQvPriceFromSource(prodElem);

  // show modal
  openQuickView();

  // attach add to cart handler for quick view
  qvAddCart.onclick = () => {
    const active = qvWeightArea.querySelector('.weight-btn.active');
    const weightLabel = active ? active.textContent : '250g';
    const priceNumber = parseInt(qvPrice.textContent.replace(/\D/g,'') || '0');

    // uses global cart array and updateCart() from your site scripts
    if (typeof cart !== 'undefined' && typeof updateCart === 'function') {
      const itemName = `${name} (${weightLabel})`;
      const existing = cart.find(it => it.name === itemName);
      if (existing) existing.quantity += 1;
      else cart.push({ name: itemName, price: priceNumber, quantity: 1 });
      updateCart();
    } else {
      // fallback: show alert
      alert('Item ready to add: ' + name + ' - ' + weightLabel + ' - ₹' + priceNumber);
    }

    // visual feedback
    qvAddCart.classList.add('added');
    qvAddCart.textContent = '✔ Added';
    setTimeout(() => {
      qvAddCart.classList.remove('added');
      qvAddCart.textContent = 'Add to Cart';
    }, 1000);

    // close quick view after short delay
    setTimeout(() => closeQuickView(), 600);
  };
}

// events
if (navSearchIcon) {
  navSearchIcon.addEventListener('click', (e) => {
    e.stopPropagation();
    openOverlay();
  });
}

// close overlay
// searchCloseBtn.addEventListener('click', closeOverlay);
// searchOverlay.addEventListener('click', (e) => {
//  if (e.target === searchOverlay) closeOverlay();
// });

// input live search
// overlayInput.addEventListener('input', (e) => {
//   performOverlaySearch(e.target.value);
// });

// ESC to close overlay or quick view
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    if (quickviewModal.classList.contains('open')) closeQuickView();
    else if (searchOverlay.classList.contains('open')) closeOverlay();
  }
});

// quick view close handlers
//quickviewClose.addEventListener('click', closeQuickView);
// quickviewModal.addEventListener('click', (e) => {
//   if (e.target === quickviewModal) closeQuickView();
// });

document.addEventListener("DOMContentLoaded", () => {
  const menuToggle = document.querySelector(".menu-toggle");
  const mobileMenuOverlay = document.querySelector(".mobile-menu-overlay");
  const closeMenu = document.querySelector(".close-menu");

  if (menuToggle && mobileMenuOverlay && closeMenu) {
    menuToggle.addEventListener("click", () => {
      mobileMenuOverlay.classList.add("active");
    });

    closeMenu.addEventListener("click", () => {
      mobileMenuOverlay.classList.remove("active");
    });

    mobileMenuOverlay.addEventListener("click", (e) => {
      if (e.target === mobileMenuOverlay) {
        mobileMenuOverlay.classList.remove("active");
      }
    });
  }
});


function showInfo(name, image, ingredients, description) {
  const oldPopup = document.querySelector('.info-popup');
  if (oldPopup) oldPopup.remove();

  const popup = document.createElement('div');
  popup.className = 'info-popup';
  popup.innerHTML = `
    <div class="info-content">
      <button class="info-close" onclick="closeInfo()">×</button>
      <img src="${image}" alt="${name}">
      <h2>${name}</h2>

      <div class="info-section">
        <h4 style="padding-bottom:9px;">Ingredients:</h4>
        <ul>
          ${ingredients.map(item => `<li style="list-style: square; padding-bottom:7px">${item}</li>`).join('')}
        </ul>
      </div>
      <br><br>
      <div class="info-section">
        <h4 style="padding-bottom:3px;">Description:</h4>
        <p style="font-weight:600;">${description}</p>
      </div>
    </div>
  `;

  document.body.appendChild(popup);
  popup.addEventListener('click', e => {
    if (e.target === popup) closeInfo();
  });
}

function closeInfo() {
  const popup = document.querySelector('.info-popup');
  if (popup) {
    popup.style.animation = 'fadeOut 0.25s ease forwards';
    setTimeout(() => popup.remove(), 250);
  }
}

// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
document.addEventListener("DOMContentLoaded", function () {
  const iframes = document.querySelectorAll("iframe[data-src]");

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        let iframe = entry.target;
        iframe.src = iframe.dataset.src; 
        observer.unobserve(iframe);
      }
    });
  }, { threshold: 0.5 });

  iframes.forEach(iframe => {
    observer.observe(iframe);
  });
});

window.addEventListener("load", updateCart);


