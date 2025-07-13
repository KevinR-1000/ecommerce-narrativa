// Espera a que todo el contenido del DOM esté cargado para empezar a trabajar.
document.addEventListener('DOMContentLoaded', () => {

    // ===================================================================
    // === LÓGICA PARA MOVER LOS PANELES DE LOGIN/REGISTRO ===========
    // ===================================================================
    const container = document.getElementById('container');
    const registerBtn = document.getElementById('register');
    const loginBtn = document.getElementById('login');

    if (registerBtn && container) {
        registerBtn.addEventListener('click', () => container.classList.add("active"));
    }

    if (loginBtn && container) {
        loginBtn.addEventListener('click', () => container.classList.remove("active"));
    }

    // ===================================================================
    // === LÓGICA PARA LAS ALERTAS PERSONALIZADAS (VERSIÓN ROBUSTA) ====
    // ===================================================================

    // Buscamos los datos de alerta en la etiqueta <body>.
    const alertData = document.body.dataset;

    // Usamos ?. (optional chaining) para evitar errores si el atributo no existe.
    // Esto es crucial para que la página no falle si no hay nada que mostrar.
    if (alertData?.loginSuccess) {
        showAlert('¡Has iniciado sesión correctamente!');
    } else if (alertData?.logoutSuccess) {
        showAlert('¡Has cerrado sesión, ¡Vuelve pronto!');
    } else if (alertData?.registrationSuccess) {
        showAlert(alertData.registrationSuccess);
    }
});


// ===================================================================
// === FUNCIÓN GLOBAL PARA MOSTRAR LA ALERTA MODAL ==================
// ===================================================================
/**
 * Muestra una alerta modal personalizada.
 * @param {string} message - El mensaje a mostrar en la alerta.
 */
function showAlert(message) {
    // No hacer nada si el mensaje está vacío, nulo o es indefinido.
    if (!message || message.trim() === '') return;

    // Crear el fondo oscuro
    const overlay = document.createElement('div');
    overlay.className = 'alert-overlay';

    // Crear la caja de la alerta
    const alertBox = document.createElement('div');
    alertBox.className = 'alert-box';

    // Crear el contenido interno de la alerta
    alertBox.innerHTML = `
        <div class="alert-icon"><i class="fa-solid fa-check-circle"></i></div>
        <div class="alert-message">${message}</div>
        <button class="alert-button">Aceptar</button>
    `;

    // Añadir la lógica para cerrar la alerta
    const closeAlert = () => {
        overlay.classList.remove('show');
        // Esperamos a que la transición termine para eliminar el elemento del DOM de forma segura.
        overlay.addEventListener('transitionend', () => {
            if (overlay.parentNode) {
                overlay.parentNode.removeChild(overlay);
            }
        }, { once: true });
    };

    alertBox.querySelector('.alert-button').addEventListener('click', closeAlert);

    // Cerrar la alerta haciendo clic fuera de la caja.
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            closeAlert();
        }
    });

    // Añadir la alerta a la página y mostrarla con una animación.
    document.body.appendChild(overlay);
    setTimeout(() => overlay.classList.add('show'), 10);
}