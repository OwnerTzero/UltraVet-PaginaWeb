const tipo = localStorage.getItem("tipoUsuario");

if (tipo !== "ADMIN") {
    alert("Acceso restringido para administradores.");
    window.location.href = "/";
}
