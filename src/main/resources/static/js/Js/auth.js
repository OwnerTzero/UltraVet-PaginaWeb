/* Bloque anterior desactivado: el render activo del navbar esta en el flujo cliente/admin.
document.addEventListener("DOMContentLoaded", () => {

    const authArea = document.getElementById("authArea");

    if (!authArea) return;

    const usuario = localStorage.getItem("usuario");

    if (usuario) {

        authArea.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                <span class="user-name">${usuario}</span>

                <button class="btn btn-sm btn-logout"
                    onclick="logout()">

                    <i class="bi bi-box-arrow-right"></i>

                </button>
            </div>
        `;

    } else {

        authArea.innerHTML = `
            <button
                class="btn-login-custom"
                data-bs-toggle="modal"
                data-bs-target="#authModal">

                <i class="bi bi-person-circle me-1"></i>
                Iniciar sesión

            </button>
        `;
    }

});
*/

// ===========================
// CONFIGURACIÓN INICIAL
// ===========================

if (!localStorage.getItem("usuarios")) {
    localStorage.setItem("usuarios", JSON.stringify([]));
}

if (!localStorage.getItem("admins")) {
    const admins = [
        {
            dni: "12345678",
            pass: "admin123",
            nombre: "Administrador"
        }
    ];
    localStorage.setItem("admins", JSON.stringify(admins));
}

// ===========================
// NAVBAR DINÁMICO
// ===========================

const usuarioActivo =
    localStorage.getItem("usuario") ||
    sessionStorage.getItem("usuario");

const tipoUsuarioActual =
    localStorage.getItem("tipoUsuario") ||
    sessionStorage.getItem("tipoUsuario");

const authArea = document.getElementById("authArea");

if (authArea) {

    if (usuarioActivo) {

        authArea.innerHTML = `
            <div class="d-flex align-items-center gap-3">
                <span class="user-name">
                    ${tipoUsuarioActual === "ADMIN" ? "👨‍💼 Admin" : "👤 " + usuarioActivo}
                </span>
                <button class="btn btn-sm btn-logout" onclick="logout()">
                    <i class="bi bi-box-arrow-right"></i>
                </button>
            </div>
        `;

    } else {

        authArea.innerHTML = `
            <button
                class="btn-login-custom"
                data-bs-toggle="modal"
                data-bs-target="#authModal">
                <i class="bi bi-person-circle me-1"></i>
                Iniciar sesión
            </button>
        `;

    }

}

// ===========================
// LOGOUT
// ===========================

function logout() {

    localStorage.removeItem("usuario");
    localStorage.removeItem("tipoUsuario");
    sessionStorage.removeItem("usuario");
    sessionStorage.removeItem("tipoUsuario");

    window.location.href = "/";

}

// ===========================
// TIPO DE ACCESO
// ===========================

function cambiarTipoAcceso(tipo) {

    document.getElementById("tipoAcceso").value = tipo;

    const clienteTab = document.getElementById("clienteTab");
    const adminTab = document.getElementById("adminTab");
    const clienteLogin = document.getElementById("clienteLogin");
    const adminLogin = document.getElementById("adminLogin");
    const recuperar = document.getElementById("recuperarContainer");
    const recordar = document.getElementById("recordarContainer");
    const toggleBtn = document.getElementById("toggleBtn");

    if (tipo === "CLIENTE") {

        clienteTab.classList.add("active");
        adminTab.classList.remove("active");

        clienteLogin.style.display = "block";
        adminLogin.style.display = "none";

        recuperar.style.display = "block";
        recordar.style.display = "block";
        toggleBtn.style.display = "block";

        // habilita el correo para que el submit funcione
        document.getElementById("loginCorreo").removeAttribute("disabled");

    } else {

        adminTab.classList.add("active");
        clienteTab.classList.remove("active");

        clienteLogin.style.display = "none";
        adminLogin.style.display = "block";

        recuperar.style.display = "none";
        recordar.style.display = "none";
        toggleBtn.style.display = "none";

        document.getElementById("registerForm").style.display = "none";
        document.getElementById("recoverForm").style.display = "none";
        document.getElementById("loginForm").style.display = "block";

        // deshabilita el correo oculto para que no bloquee el submit
        document.getElementById("loginCorreo").setAttribute("disabled", "true");

    }

}

// ===========================
// MOSTRAR PASSWORD
// ===========================

function togglePassword() {
    const input = document.getElementById("loginPass");
    input.type = input.type === "password" ? "text" : "password";
}

// ===========================
// LOGIN / REGISTRO
// ===========================

let modoLogin = true;

function toggleAuth() {

    modoLogin = !modoLogin;

    document.getElementById("loginForm").style.display = modoLogin ? "block" : "none";
    document.getElementById("registerForm").style.display = modoLogin ? "none" : "block";
    document.getElementById("recoverForm").style.display = "none";

    document.getElementById("toggleBtn").textContent = modoLogin
        ? "¿No tienes cuenta? Regístrate"
        : "¿Ya tienes cuenta? Inicia sesión";

}

// ===========================
// RECUPERAR PASSWORD
// ===========================

function mostrarRecuperarPassword() {
    document.getElementById("loginForm").style.display = "none";
    document.getElementById("registerForm").style.display = "none";
    document.getElementById("recoverForm").style.display = "block";
}

// ===========================
// MENSAJES
// ===========================

function mostrarMensaje(texto, color = "red") {
    const msg = document.getElementById("mensaje");
    msg.style.display = "block";
    msg.style.color = color;
    msg.textContent = texto;
}

// ===========================
// LOGIN
// ===========================

document.getElementById("loginForm").addEventListener("submit", function (e) {

    e.preventDefault();

    const tipo = document.getElementById("tipoAcceso").value;
    const pass = document.getElementById("loginPass").value.trim();

    if (tipo === "CLIENTE") {

        const correo = document.getElementById("loginCorreo").value;

        const usuarios = JSON.parse(localStorage.getItem("usuarios")) || [];

        const usuario = usuarios.find(u => u.correo === correo && u.pass === pass);

        if (!usuario) {
            mostrarMensaje("Correo o contraseña incorrectos");
            return;
        }

        const recordar = document.getElementById("recordarSesion").checked;

        if (recordar) {
            localStorage.setItem("usuario", usuario.nombre);
            localStorage.setItem("tipoUsuario", "CLIENTE");
        } else {
            sessionStorage.setItem("usuario", usuario.nombre);
            sessionStorage.setItem("tipoUsuario", "CLIENTE");
        }

        window.location.href = "/";

    } else {

        // ADMIN
        const dni = document.getElementById("loginDni").value.trim();

        const admins = JSON.parse(localStorage.getItem("admins")) || [];

        const admin = admins.find(a =>
            String(a.dni).trim() === dni &&
            String(a.pass).trim() === pass
        );

        if (!admin) {
            mostrarMensaje("DNI o contraseña incorrectos");
            return;
        }

        localStorage.setItem("usuario", admin.nombre);
        localStorage.setItem("tipoUsuario", "ADMIN");

        window.location.replace("/admin");

    }

});

// ===========================
// REGISTRO CLIENTE
// ===========================

document.getElementById("registerForm").addEventListener("submit", function (e) {

    e.preventDefault();

    const nombre = document.getElementById("regNombre").value;
    const correo = document.getElementById("regCorreo").value;
    const telefono = document.getElementById("regTelefono").value;
    const pass = document.getElementById("regPass").value;
    const pass2 = document.getElementById("regPass2").value;

    if (pass !== pass2) {
        mostrarMensaje("Las contraseñas no coinciden");
        return;
    }

    let usuarios = JSON.parse(localStorage.getItem("usuarios")) || [];

    const existe = usuarios.find(u => u.correo === correo);

    if (existe) {
        mostrarMensaje("Ya existe una cuenta con ese correo");
        return;
    }

    usuarios.push({ nombre, correo, telefono, pass, tipo: "CLIENTE" });

    localStorage.setItem("usuarios", JSON.stringify(usuarios));

    mostrarMensaje("Cuenta creada correctamente", "green");

    toggleAuth();

});

// ===========================
// RECUPERAR PASSWORD
// ===========================

document.getElementById("recoverForm").addEventListener("submit", function (e) {

    e.preventDefault();

    const correo = document.getElementById("recoverCorreo").value;

    const usuarios = JSON.parse(localStorage.getItem("usuarios")) || [];

    const usuario = usuarios.find(u => u.correo === correo);

    if (!usuario) {
        mostrarMensaje("No existe una cuenta con ese correo");
        return;
    }

    mostrarMensaje("Tu contraseña es: " + usuario.pass, "green");

});
