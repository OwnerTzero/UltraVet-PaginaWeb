(() => {
    const $ = (selector) => document.querySelector(selector);
    const byId = (id) => document.getElementById(id);

    let modoLogin = true;

    function setDisplay(element, visible) {
        if (element) {
            element.style.display = visible ? "block" : "none";
        }
    }

    function loginField(name) {
        return $(`#loginForm [name="${name}"]`);
    }

    function formField(formId, name) {
        return $(`#${formId} [name="${name}"]`);
    }

    function setLoginMode(visible) {
        modoLogin = visible;
        setDisplay(byId("loginForm"), visible);
        setDisplay(byId("registerForm"), !visible);
        setDisplay(byId("recoverForm"), false);

        const toggleBtn = byId("toggleBtn");
        if (toggleBtn) {
            toggleBtn.textContent = visible
                ? "No tienes cuenta? Registrate"
                : "Ya tienes cuenta? Inicia sesion";
        }
    }

    window.cambiarTipoAcceso = (tipo) => {
        const tipoAcceso = byId("tipoAcceso") || loginField("tipoAcceso");
        const clienteTab = byId("clienteTab");
        const adminTab = byId("adminTab");
        const clienteLogin = byId("clienteLogin");
        const adminLogin = byId("adminLogin");
        const correo = loginField("correo");
        const dni = loginField("dni");

        const esAdmin = tipo === "ADMIN";

        if (tipoAcceso) {
            tipoAcceso.value = esAdmin ? "ADMIN" : "CLIENTE";
        }

        clienteTab?.classList.toggle("active", !esAdmin);
        adminTab?.classList.toggle("active", esAdmin);
        setDisplay(clienteLogin, !esAdmin);
        setDisplay(adminLogin, esAdmin);

        if (correo) {
            correo.disabled = esAdmin;
            correo.required = !esAdmin;
        }

        if (dni) {
            dni.disabled = !esAdmin;
            dni.required = esAdmin;
        }

        setLoginMode(true);
    };

    window.togglePassword = (formId = "loginForm", fieldName = "password", button = null) => {
        const input = formField(formId, fieldName);
        if (!input) {
            return;
        }

        input.type = input.type === "password" ? "text" : "password";

        const icon = button?.querySelector("i");
        if (icon) {
            icon.classList.toggle("bi-eye-fill");
            icon.classList.toggle("bi-eye-slash-fill");
        }
    };

    window.toggleAuth = () => {
        setLoginMode(!modoLogin);
    };

    window.mostrarRecuperarPassword = () => {
        setDisplay(byId("loginForm"), false);
        setDisplay(byId("registerForm"), false);
        setDisplay(byId("recoverForm"), true);

        const toggleBtn = byId("toggleBtn");
        if (toggleBtn) {
            toggleBtn.textContent = "Volver al inicio de sesion";
        }
    };

    window.logout = () => {
        window.location.href = "/logout";
    };

    window.mostrarMensaje = (texto, color = "red") => {
        const msg = byId("mensaje");
        if (!msg) {
            return;
        }

        msg.style.display = "block";
        msg.style.color = color;
        msg.textContent = texto;
    };

    function initValidacionRegistro() {
        const password = formField("registerForm", "password");
        const confirmPassword = formField("registerForm", "confirmPassword");

        if (!password || !confirmPassword) {
            return;
        }

        const validarCoincidencia = () => {
            if (confirmPassword.value && confirmPassword.value !== password.value) {
                confirmPassword.setCustomValidity("Las contrasenas no coinciden.");
                return;
            }

            confirmPassword.setCustomValidity("");
        };

        password.addEventListener("input", validarCoincidencia);
        confirmPassword.addEventListener("input", validarCoincidencia);
    }

    document.addEventListener("DOMContentLoaded", () => {
        const authModal = byId("authModal");
        const authMode = authModal?.dataset.mode || "login";
        const tipoAcceso = byId("tipoAcceso")?.value || "CLIENTE";

        window.cambiarTipoAcceso(tipoAcceso === "ADMIN" ? "ADMIN" : "CLIENTE");
        setLoginMode(authMode !== "registro");
        initValidacionRegistro();

        byId("recoverForm")?.addEventListener("submit", (event) => {
            event.preventDefault();
            window.mostrarMensaje(
                "Por seguridad, solicita el restablecimiento de contrasena al administrador.",
                "#198754"
            );
        });

        if (authModal?.dataset.open === "true" && window.bootstrap) {
            bootstrap.Modal.getOrCreateInstance(authModal).show();
        }
    });
})();
