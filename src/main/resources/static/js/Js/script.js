document.addEventListener("DOMContentLoaded", () => {
    iniciarFiltrosServicios();
    iniciarDetalleServicios();
    iniciarFormularioCitas();
    iniciarDetalleCitas();
});

function normalizarTexto(texto = "") {
    return texto
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "");
}

function escaparHTML(texto = "") {
    const contenedor = document.createElement("div");
    contenedor.textContent = texto;
    return contenedor.innerHTML;
}

function iniciarFiltrosServicios() {
    const buscador = document.getElementById("buscadorServicios");
    const botonesFiltro = document.querySelectorAll("[data-service-filter]");
    const servicios = document.querySelectorAll(".service-item");
    const categorias = document.querySelectorAll(".service-category");
    const sinResultados = document.getElementById("sinResultadosServicios");

    if (!buscador || servicios.length === 0) {
        return;
    }

    let categoriaActiva = "todos";

    function aplicarFiltros() {
        const termino = normalizarTexto(buscador.value.trim());
        let visibles = 0;

        servicios.forEach((servicio) => {
            const coincideCategoria = categoriaActiva === "todos" || servicio.dataset.category === categoriaActiva;
            const contenido = normalizarTexto(servicio.dataset.search || servicio.textContent);
            const coincideBusqueda = contenido.includes(termino);
            const mostrar = coincideCategoria && coincideBusqueda;

            servicio.classList.toggle("d-none", !mostrar);

            if (mostrar) {
                visibles += 1;
            }
        });

        categorias.forEach((categoria) => {
            const tieneServiciosVisibles = categoria.querySelectorAll(".service-item:not(.d-none)").length > 0;
            categoria.classList.toggle("d-none", !tieneServiciosVisibles);
        });

        sinResultados?.classList.toggle("d-none", visibles > 0);
    }

    botonesFiltro.forEach((boton) => {
        boton.addEventListener("click", () => {
            botonesFiltro.forEach((item) => item.classList.remove("active"));
            boton.classList.add("active");
            categoriaActiva = boton.dataset.serviceFilter;
            aplicarFiltros();
        });
    });

    buscador.addEventListener("input", aplicarFiltros);
}

function iniciarDetalleServicios() {
    const botonesDetalle = document.querySelectorAll(".service-detail-btn");
    const titulo = document.getElementById("detalleServicioTitulo");
    const categoria = document.getElementById("detalleServicioCategoria");
    const descripcion = document.getElementById("detalleServicioDescripcion");
    const duracion = document.getElementById("detalleServicioDuracion");
    const precio = document.getElementById("detalleServicioPrecio");
    const reservar = document.getElementById("btnReservarDesdeDetalle");
    const servicioSelect = document.getElementById("cotizaServicio");
    const detalleModal = document.getElementById("modalDetalleServicio");
    const citaModal = document.getElementById("modalCotizar");

    if (!botonesDetalle.length || !titulo) {
        return;
    }

    let servicioSeleccionado = "";

    botonesDetalle.forEach((boton) => {
        boton.addEventListener("click", () => {
            servicioSeleccionado = boton.dataset.title || "";
            titulo.textContent = servicioSeleccionado;
            categoria.textContent = boton.dataset.categoryLabel || "";
            descripcion.textContent = boton.dataset.description || "";
            duracion.textContent = boton.dataset.duration || "";
            precio.textContent = boton.dataset.price || "";
        });
    });

    reservar?.addEventListener("click", () => {
        seleccionarServicioPorNombre(servicioSelect, servicioSeleccionado);

        const modalDetalle = bootstrap.Modal.getOrCreateInstance(detalleModal);
        const modalCita = bootstrap.Modal.getOrCreateInstance(citaModal);

        detalleModal.addEventListener("hidden.bs.modal", () => modalCita.show(), { once: true });
        modalDetalle.hide();
    });
}

function seleccionarServicioPorNombre(select, nombre) {
    if (!select || !nombre) {
        return;
    }

    const objetivo = normalizarTexto(nombre);
    const opcion = Array.from(select.options).find((item) => {
        const texto = normalizarTexto(item.textContent.trim());
        return texto.startsWith(objetivo);
    });

    if (opcion) {
        select.value = opcion.value;
    }
}

function iniciarFormularioCitas() {
    const formulario = document.getElementById("formCotizar");
    const fecha = document.getElementById("cotizaFecha");
    const fechaNacimientoMascota = document.getElementById("cotizaMascotaFechaNacimiento");
    const modalCotizar = document.getElementById("modalCotizar");

    if (!formulario || !fecha) {
        return;
    }

    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, "0");
    const dia = String(hoy.getDate()).padStart(2, "0");
    fecha.min = `${hoy.getFullYear()}-${mes}-${dia}`;

    if (fechaNacimientoMascota) {
        fechaNacimientoMascota.max = fecha.min;
    }

    formulario.addEventListener("submit", (event) => {
        if (!formulario.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }

        formulario.classList.add("was-validated");
    });

    if (document.body.dataset.openCitaModal === "true" && modalCotizar) {
        formulario.classList.add("was-validated");
        bootstrap.Modal.getOrCreateInstance(modalCotizar).show();
    }
}

function iniciarDetalleCitas() {
    const tabla = document.getElementById("tablaCitasBody");
    const contenido = document.getElementById("detalleCitaContenido");
    const titulo = document.getElementById("detalleCitaTitulo");
    const modal = document.getElementById("modalDetalleCita");

    if (!tabla || !contenido || !titulo || !modal) {
        return;
    }

    tabla.addEventListener("click", (event) => {
        const boton = event.target.closest("[data-cita-accion='detalle']");

        if (!boton) {
            return;
        }

        const cita = boton.dataset;
        titulo.textContent = `${cita.servicio || "Cita"} - ${cita.mascota || "Mascota"}`;
        contenido.innerHTML = crearResumenCita(cita);
        bootstrap.Modal.getOrCreateInstance(modal).show();
    });
}

function crearResumenCita(cita) {
    return `
        <p><strong>Cliente:</strong> ${escaparHTML(cita.cliente)}</p>
        <p><strong>Mascota:</strong> ${escaparHTML(cita.mascota)}</p>
        <p><strong>Servicio:</strong> ${escaparHTML(cita.servicio)}</p>
        <p><strong>Fecha y hora:</strong> ${escaparHTML(cita.fecha)} ${escaparHTML(cita.hora)}</p>
        <p><strong>Contacto:</strong> ${escaparHTML(cita.contacto)}</p>
        <p><strong>Estado:</strong> ${escaparHTML(cita.estado)}</p>
        ${cita.comentario ? `<p><strong>Comentario:</strong> ${escaparHTML(cita.comentario)}</p>` : ""}
    `;
}
