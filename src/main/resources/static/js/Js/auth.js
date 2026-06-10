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

    window.togglePassword = () => {
        const input = loginField("password");
        if (input) {
            input.type = input.type === "password" ? "text" : "password";
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

    document.addEventListener("DOMContentLoaded", () => {
        const authModal = byId("authModal");
        const authMode = authModal?.dataset.mode || "login";
        const tipoAcceso = byId("tipoAcceso")?.value || "CLIENTE";

        window.cambiarTipoAcceso(tipoAcceso === "ADMIN" ? "ADMIN" : "CLIENTE");
        setLoginMode(authMode !== "registro");

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
