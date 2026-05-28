// NAVBAR DINÁMICO
const authArea = document.getElementById("authArea");
const usuario = localStorage.getItem("usuario");

if (usuario) {
    authArea.innerHTML = `
    <div class="d-flex align-items-center gap-3">

        <div class="user-profile">

            <span class="user-name">
                ${usuario}
            </span>

        </div>

        <button class="btn btn-sm btn-logout" onclick="logout()">
            <i class="bi bi-box-arrow-right"></i>
        </button>

    </div>
`;
} else {
    authArea.innerHTML = `
        <button class="btn-login-custom" data-bs-toggle="modal" data-bs-target="#authModal">
            <i class="bi bi-person-circle me-1"></i> Iniciar sesión
        </button>
    `;
}

function logout() {
    localStorage.removeItem("usuario");
    location.reload();
}


// CAMBIO LOGIN / REGISTER
let modoLogin = true;

function toggleAuth() {
    modoLogin = !modoLogin;

    document.getElementById("loginForm").style.display = modoLogin ? "block" : "none";
    document.getElementById("registerForm").style.display = modoLogin ? "none" : "block";

    document.getElementById("toggleBtn").textContent = modoLogin
        ? "¿No tienes cuenta? Regístrate"
        : "¿Ya tienes cuenta? Inicia sesión";

    document.getElementById("mensaje").style.display = "none";
}

function togglePassword() {

    const input = document.getElementById("loginPass");

    input.type =
        input.type === "password"
            ? "text"
            : "password";
}


// LOGIN
document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const user = document.getElementById("loginUser").value;
    const pass = document.getElementById("loginPass").value;

    let usuarios = JSON.parse(localStorage.getItem("usuarios")) || [];

    let encontrado = usuarios.find(u => u.user === user && u.pass === pass);

    if (encontrado) {
        localStorage.setItem("usuario", user);

        const modal = bootstrap.Modal.getInstance(document.getElementById('authModal'));
        modal.hide();

        location.reload();
    } else {
        mostrarMensaje("Usuario o contraseña incorrectos");
    }
});


// REGISTRO
document.getElementById("registerForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const user = document.getElementById("regUser").value;
    const pass = document.getElementById("regPass").value;

    let usuarios = JSON.parse(localStorage.getItem("usuarios")) || [];

    let existe = usuarios.find(u => u.user === user);

    if (existe) {
        mostrarMensaje("El usuario ya existe");
        return;
    }

    usuarios.push({ user, pass });
    localStorage.setItem("usuarios", JSON.stringify(usuarios));

    mostrarMensaje("Registro exitoso ✅", "green");

    toggleAuth();
});


function mostrarMensaje(texto, color = "red") {
    const msg = document.getElementById("mensaje");
    msg.style.display = "block";
    msg.style.color = color;
    msg.textContent = texto;
}