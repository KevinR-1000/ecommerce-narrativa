/**
 * =================================================================
 * ARCHIVO JAVASCRIPT PRINCIPAL PARA NARRATIVA.IO (CON MERCADO PAGO)
 * =================================================================
 * Contiene la lógica para:
 * 1. Alertas personalizadas (Toasts)
 * 2. Menú de navegación responsivo (Hamburguesa)
 * 3. Cambio de tema (Claro/Oscuro) con persistencia
 * 4. Funcionalidad completa del carrito de compras
 * 5. Inicio del flujo de pago con Mercado Pago
 * 6. Buscador con autocompletado en el header
 * 7. Service Worker para PWA
 * =================================================================
 */

// --- FUNCIONES GLOBALES Y VARIABLES DE ESTADO ---
let shoppingCart = [];

/** Muestra una alerta personalizada flotante (Toast). */
function showCustomAlert(message, type = 'success') {
    const alertElement = document.getElementById('custom-alert');
    if (!alertElement) return;
    const messageElement = alertElement.querySelector('#custom-alert-message');
    if (!messageElement) return;

    messageElement.textContent = message;
    alertElement.className = 'custom-alert';
    alertElement.classList.add(`alert-${type}`);
    alertElement.classList.add('show');
    setTimeout(() => alertElement.classList.remove('show'), 4000);
}

/** Guarda el carrito en el LocalStorage. */
const saveCartToLocalStorage = () => {
    localStorage.setItem('shoppingCart', JSON.stringify(shoppingCart));
};

/** Renderiza los items en el panel del carrito. */
const renderCartItems = () => {
    const cartSidebar = document.getElementById('carrito');
    if (!cartSidebar) return;
    const cartItemsContainer = cartSidebar.querySelector('.carrito-items');
    const cartTotalPriceDisplay = cartSidebar.querySelector('.carrito-precio-total');
    const btnPagar = cartSidebar.querySelector('.btn-pagar');

    cartItemsContainer.innerHTML = '';
    let totalPrice = 0;

    if (shoppingCart.length === 0) {
        cartItemsContainer.innerHTML = '<p>El carrito está vacío.</p>';
        if (btnPagar) btnPagar.disabled = true;
    } else {
        if (btnPagar) btnPagar.disabled = false;
        shoppingCart.forEach(item => {
            const itemTotal = item.price * item.quantity;
            totalPrice += itemTotal;
            const cartItemElement = document.createElement('div');
            cartItemElement.classList.add('carrito-item');
            cartItemElement.dataset.productId = item.productId;
            cartItemElement.innerHTML = `<img src="${item.imageUrl}" alt="${item.name}" class="carrito-item-img"><div class="carrito-item-details"><span class="carrito-item-titulo">${item.name}</span><div class="selector-cantidad"><i class="fa-solid fa-minus minus-btn"></i><span class="carrito-item-cantidad">${item.quantity}</span><i class="fa-solid fa-plus plus-btn"></i></div><span class="carrito-item-precio">$${itemTotal.toFixed(2)}</span></div><span class="btn-eliminar"><i class="fa-solid fa-trash-can"></i></span>`;
            cartItemsContainer.appendChild(cartItemElement);
        });
    }
    cartTotalPriceDisplay.textContent = `$${totalPrice.toFixed(2)}`;
    saveCartToLocalStorage();
};

/** Carga el carrito desde LocalStorage al iniciar. */
const loadCartFromLocalStorage = () => {
    const storedCart = localStorage.getItem('shoppingCart');
    if (storedCart) {
        shoppingCart = JSON.parse(storedCart).filter(Boolean); // Filtra items nulos si los hubiera
        renderCartItems();
    }
};

/** Función global para agregar al carrito, llamada desde el HTML. */
window.agregarAlCarritoClicked = (event) => {
    const cartSidebar = document.getElementById('carrito');
    if (!cartSidebar) return;

    const button = event.target.closest('[data-product-id]');
    if (!button) return;

    const { productId, productName, productPrice, productImage } = button.dataset;

    const existingItem = shoppingCart.find(item => item.productId === parseInt(productId));
    if (existingItem) {
        existingItem.quantity++;
    } else {
        shoppingCart.push({
            productId: parseInt(productId),
            name: productName,
            price: parseFloat(productPrice),
            imageUrl: productImage,
            quantity: 1
        });
    }
    renderCartItems();
    cartSidebar.classList.add('open');
};


// --- CÓDIGO QUE SE EJECUTA CUANDO EL DOM ESTÁ COMPLETAMENTE CARGADO ---
document.addEventListener('DOMContentLoaded', () => {

    // 1. Lógica del Menú Móvil
    const hamburgerBtn = document.getElementById('hamburger-btn');
    const mobileNav = document.getElementById('mobile-nav');
    if (hamburgerBtn && mobileNav) {
        const closeMobileNavBtn = document.getElementById('close-mobile-nav-btn');
        hamburgerBtn.addEventListener('click', () => mobileNav.classList.add('open'));
        closeMobileNavBtn.addEventListener('click', () => mobileNav.classList.remove('open'));
        mobileNav.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => mobileNav.classList.remove('open'));
        });
    }

    // 2. Lógica de Alertas de Servidor
    if (typeof serverMessage !== 'undefined' && serverMessage) {
        let alertType = 'success';
        if (serverMessage.toLowerCase().includes('cerrado sesión')) {
            alertType = 'info';
        } else if (serverMessage.toLowerCase().includes('error')) {
            alertType = 'error';
        }
        showCustomAlert(serverMessage, alertType);
    }

    // 3. Lógica del Tema Oscuro Mejorada
    const btnTema = document.getElementById('btnTema');
    const body = document.body;
    const applyTheme = (theme) => {
        if (theme === 'dark') {
            body.classList.add('oscuro');
            if (btnTema) btnTema.textContent = '☀️';
        } else {
            body.classList.remove('oscuro');
            if (btnTema) btnTema.textContent = '🌙';
        }
    };
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        applyTheme(savedTheme);
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        applyTheme('dark');
    }
    if (btnTema) {
        btnTema.addEventListener('click', () => {
            const newTheme = body.classList.contains('oscuro') ? 'light' : 'dark';
            applyTheme(newTheme);
            localStorage.setItem('theme', newTheme);
        });
    }

    // 4. Lógica del Buscador
    const openSearchBtn = document.getElementById('open-search-btn');
    const searchOverlay = document.getElementById('search-overlay');
    if (openSearchBtn && searchOverlay) {
        const closeSearchBtn = document.getElementById('close-search-btn');
        const searchInput = document.getElementById('search-input');
        const suggestionsContainer = document.getElementById('search-suggestions');
        let debounceTimer;

        const openSearch = () => { searchOverlay.classList.add('open'); searchInput.focus(); };
        const closeSearch = () => { searchOverlay.classList.remove('open'); searchInput.value = ''; suggestionsContainer.innerHTML = ''; };
        const fetchSuggestions = async (query) => {
            if (query.length < 2) { suggestionsContainer.innerHTML = ''; return; }
            try {
                const response = await fetch(`/api/productos/buscar?q=${encodeURIComponent(query)}`);
                if (!response.ok) return;
                const productos = await response.json();
                suggestionsContainer.innerHTML = '';
                if (productos.length > 0) {
                    productos.forEach(producto => {
                        const item = document.createElement('div');
                        item.className = 'suggestion-item';
                        item.dataset.productId = producto.idProducto;
                        item.innerHTML = `<img src="${producto.urlImagen}" alt="${producto.titulo}"><div class="info"><div class="title">${producto.titulo}</div><div class="author">${producto.autor}</div></div>`;
                        suggestionsContainer.appendChild(item);
                    });
                } else {
                    suggestionsContainer.innerHTML = '<div class="suggestion-item"><div class="info">No se encontraron resultados.</div></div>';
                }
            } catch (error) { console.error('Error al buscar sugerencias:', error); }
        };

        openSearchBtn.addEventListener('click', openSearch);
        closeSearchBtn.addEventListener('click', closeSearch);
        window.addEventListener('keydown', (e) => { if (e.key === 'Escape' && searchOverlay.classList.contains('open')) closeSearch(); });
        searchInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => fetchSuggestions(searchInput.value), 300);
        });
        suggestionsContainer.addEventListener('click', (e) => {
            const suggestionItem = e.target.closest('.suggestion-item');
            if (suggestionItem && suggestionItem.dataset.productId) {
                const productId = suggestionItem.dataset.productId;
                alert(`Redirigiendo al producto ID: ${productId}. Implementar la página de detalle.`);
                closeSearch();
            }
        });
    }

    // ==========================================================
    // ===== INICIO DE LA MODIFICACIÓN PARA MERCADO PAGO =====
    // ==========================================================

    // 5. LÓGICA DEL CARRITO (Listeners y Carga Inicial)
    const cartSidebar = document.getElementById('carrito');
    if (cartSidebar) {
        const openCartBtn = document.getElementById('open-cart-btn');
        const closeCartBtn = cartSidebar.querySelector('.close-cart-btn');
        const cartItemsContainer = cartSidebar.querySelector('.carrito-items');
        const btnPagar = cartSidebar.querySelector('.btn-pagar');

        if (openCartBtn) openCartBtn.addEventListener('click', () => cartSidebar.classList.add('open'));
        if (closeCartBtn) closeCartBtn.addEventListener('click', () => cartSidebar.classList.remove('open'));

        cartItemsContainer.addEventListener('click', (e) => {
            const cartItemElement = e.target.closest('.carrito-item');
            if (!cartItemElement) return;
            const productId = parseInt(cartItemElement.dataset.productId);
            const itemInCart = shoppingCart.find(item => item.productId === productId);
            if (!itemInCart) return;

            if (e.target.classList.contains('minus-btn')) {
                if (--itemInCart.quantity <= 0) { shoppingCart = shoppingCart.filter(item => item.productId !== productId); }
            } else if (e.target.classList.contains('plus-btn')) { itemInCart.quantity++; }
            else if (e.target.closest('.btn-eliminar')) { shoppingCart = shoppingCart.filter(item => item.productId !== productId); }
            renderCartItems();
        });

        // Listener del botón Pagar (MODIFICADO)
        if (btnPagar) {
            btnPagar.addEventListener('click', async (e) => {
                e.preventDefault();
                if (shoppingCart.length === 0) return showCustomAlert('Tu carrito está vacío.', 'error');
                if (typeof globalIsLoggedIn !== 'undefined' && !globalIsLoggedIn) {
                    showCustomAlert('Debes iniciar sesión para comprar.', 'error');
                    setTimeout(() => window.location.href = '/login', 1500);
                    return;
                }

                btnPagar.disabled = true;
                btnPagar.textContent = 'Procesando...';

                // Prepara el cuerpo de la petición con los items del carrito
                const compraRequest = {
                    items: shoppingCart.map(item => ({
                        productId: item.productId,
                        quantity: item.quantity,
                        name: item.name,      // <--- AÑADIDO
                        price: item.price     // <--- AÑADIDO
                    }))
                };

                try {
                    // Llama al nuevo endpoint del backend para crear la preferencia de pago
                    const response = await fetch('/api/pagos/crear-preferencia', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(compraRequest)
                    });

                    if (!response.ok) {
                        const errorData = await response.json();
                        throw new Error(errorData.error || 'No se pudo crear la preferencia de pago.');
                    }

                    const data = await response.json();

                    // Si el backend devuelve la URL de checkout, redirige al usuario
                    if (data.checkoutUrl) {
                        window.location.href = data.checkoutUrl;
                    }

                } catch (error) {
                    console.error('Error al iniciar el pago:', error);
                    showCustomAlert('Hubo un error al iniciar el pago. Inténtalo de nuevo.', 'error');
                    btnPagar.disabled = false;
                    btnPagar.innerHTML = 'Pagar <i class="fa-solid fa-bag-shopping"></i>';
                }
            });
        }

        loadCartFromLocalStorage();
    }

    // El bloque del modal de pago anterior ya no es necesario,
    // ya que no manejamos el formulario de la tarjeta directamente.
    // El listener para el botón 'Pagar' ahora inicia el flujo de Mercado Pago.
});

// ==========================================================
// ===== FIN DE LA MODIFICACIÓN PARA MERCADO PAGO =====
// ==========================================================


// 7. Lógica del Service Worker
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/service-worker.js')
            .then(reg => console.log('Service Worker: Registrado'))
            .catch(err => console.log(`Service Worker: Error: ${err}`));
    });
}