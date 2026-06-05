document.addEventListener("DOMContentLoaded", () => {
    iniciarFiltrosServicios();
    iniciarDetalleServicios();
    iniciarMisCitas();
    iniciarFormularioCitas();
});

const CITAS_STORAGE_KEY = "ultravetCitas";

function normalizarTexto(texto) {
    return texto
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "");
}

function escaparHTML(texto) {
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

        sinResultados.classList.toggle("d-none", visibles > 0);
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

    reservar.addEventListener("click", () => {
        if (servicioSelect && servicioSeleccionado) {
            servicioSelect.value = servicioSeleccionado;
        }

        const modalDetalle = bootstrap.Modal.getOrCreateInstance(detalleModal);
        const modalCita = bootstrap.Modal.getOrCreateInstance(citaModal);

        detalleModal.addEventListener("hidden.bs.modal", () => modalCita.show(), { once: true });
        modalDetalle.hide();
    });
}

function iniciarFormularioCitas() {
    const formulario = document.getElementById("formCotizar");
    const fecha = document.getElementById("cotizaFecha");
    const resumen = document.getElementById("resumenCotizacion");
    const modalCotizar = document.getElementById("modalCotizar");
    const modalConfirmacion = document.getElementById("modalConfirmacionCita");

    if (!formulario || !fecha || !resumen) {
        return;
    }

    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, "0");
    const dia = String(hoy.getDate()).padStart(2, "0");
    fecha.min = `${hoy.getFullYear()}-${mes}-${dia}`;

    formulario.addEventListener("submit", (event) => {
        event.preventDefault();

        if (!formulario.checkValidity()) {
            formulario.classList.add("was-validated");
            return;
        }

        const datos = {
            id: `cita-${Date.now()}`,
            nombre: document.getElementById("cotizaNombre").value.trim(),
            correo: document.getElementById("cotizaCorreo").value.trim(),
            telefono: document.getElementById("cotizaTelefono").value.trim(),
            mascota: document.getElementById("cotizaMascota").value.trim(),
            servicio: document.getElementById("cotizaServicio").value,
            fecha: document.getElementById("cotizaFecha").value,
            hora: document.getElementById("cotizaHora").value,
            comentario: document.getElementById("cotizaComentario").value.trim(),
            estado: "Pendiente",
            creadaEn: new Date().toISOString()
        };

        guardarCita(datos);
        renderizarCitas();

        resumen.innerHTML = crearResumenCita(datos);

        modalCotizar.addEventListener("hidden.bs.modal", () => {
            bootstrap.Modal.getOrCreateInstance(modalConfirmacion).show();
            formulario.reset();
            formulario.classList.remove("was-validated");
        }, { once: true });

        modalConfirmacion.addEventListener("hidden.bs.modal", () => {
            document.getElementById("misCitas")?.scrollIntoView({ behavior: "smooth", block: "start" });
        }, { once: true });

        bootstrap.Modal.getOrCreateInstance(modalCotizar).hide();
    });
}

function iniciarMisCitas() {
    const tabla = document.getElementById("tablaCitasBody");
    const limpiarBtn = document.getElementById("limpiarCitasBtn");

    if (!tabla) {
        return;
    }

    tabla.addEventListener("click", (event) => {
        const boton = event.target.closest("[data-cita-accion]");

        if (!boton) {
            return;
        }

        const citaId = boton.dataset.citaId;
        const accion = boton.dataset.citaAccion;

        if (accion === "detalle") {
            mostrarDetalleCita(citaId);
        }

        if (accion === "cancelar") {
            cancelarCita(citaId);
        }
    });

    if (limpiarBtn) {
        limpiarBtn.addEventListener("click", () => {
            const citasActivas = obtenerCitas().filter((cita) => cita.estado !== "Cancelada");
            guardarCitas(citasActivas);
            renderizarCitas();
        });
    }

    renderizarCitas();
}

function obtenerCitas() {
    try {
        return JSON.parse(localStorage.getItem(CITAS_STORAGE_KEY)) || [];
    } catch {
        return [];
    }
}

function guardarCitas(citas) {
    localStorage.setItem(CITAS_STORAGE_KEY, JSON.stringify(citas));
}

function guardarCita(cita) {
    const citas = obtenerCitas();
    citas.unshift(cita);
    guardarCitas(citas);
}

function renderizarCitas() {
    const tabla = document.getElementById("tablaCitasBody");
    const tablaWrap = document.getElementById("tablaCitasWrap");
    const vacias = document.getElementById("citasVacias");

    if (!tabla || !tablaWrap || !vacias) {
        return;
    }

    const citas = obtenerCitas();
    const hayCitas = citas.length > 0;

    tablaWrap.classList.toggle("d-none", !hayCitas);
    vacias.classList.toggle("d-none", hayCitas);

    tabla.innerHTML = citas.map((cita) => `
        <tr>
            <td>${escaparHTML(cita.mascota)}</td>
            <td>${escaparHTML(cita.servicio)}</td>
            <td>${escaparHTML(formatearFecha(cita.fecha))}</td>
            <td>${escaparHTML(cita.hora)}</td>
            <td><span class="appointment-status ${obtenerClaseEstado(cita.estado)}">${escaparHTML(cita.estado)}</span></td>
            <td class="text-end">
                <div class="appointment-actions">
                    <button type="button" class="btn btn-sm btn-outline-primary"
                        data-cita-accion="detalle" data-cita-id="${escaparHTML(cita.id)}">
                        Ver
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-danger"
                        data-cita-accion="cancelar" data-cita-id="${escaparHTML(cita.id)}"
                        ${cita.estado === "Cancelada" ? "disabled" : ""}>
                        Cancelar
                    </button>
                </div>
            </td>
        </tr>
    `).join("");
}

function mostrarDetalleCita(citaId) {
    const cita = obtenerCitas().find((item) => item.id === citaId);
    const contenido = document.getElementById("detalleCitaContenido");
    const titulo = document.getElementById("detalleCitaTitulo");
    const modal = document.getElementById("modalDetalleCita");

    if (!cita || !contenido || !titulo || !modal) {
        return;
    }

    titulo.textContent = `${cita.servicio} para ${cita.mascota}`;
    contenido.innerHTML = crearResumenCita(cita);
    bootstrap.Modal.getOrCreateInstance(modal).show();
}

function cancelarCita(citaId) {
    const citas = obtenerCitas().map((cita) => {
        if (cita.id === citaId) {
            return { ...cita, estado: "Cancelada" };
        }

        return cita;
    });

    guardarCitas(citas);
    renderizarCitas();
}

function crearResumenCita(cita) {
    return `
        <p><strong>Cliente:</strong> ${escaparHTML(cita.nombre)}</p>
        <p><strong>Mascota:</strong> ${escaparHTML(cita.mascota)}</p>
        <p><strong>Servicio:</strong> ${escaparHTML(cita.servicio)}</p>
        <p><strong>Fecha y hora:</strong> ${escaparHTML(formatearFecha(cita.fecha))} ${escaparHTML(cita.hora)}</p>
        <p><strong>Contacto:</strong> ${escaparHTML(cita.telefono)} / ${escaparHTML(cita.correo)}</p>
        <p><strong>Estado:</strong> ${escaparHTML(cita.estado)}</p>
        ${cita.comentario ? `<p><strong>Comentario:</strong> ${escaparHTML(cita.comentario)}</p>` : ""}
    `;
}

function obtenerClaseEstado(estado) {
    if (estado === "Cancelada") {
        return "text-bg-secondary";
    }

    if (estado === "Confirmada") {
        return "text-bg-success";
    }

    return "text-bg-warning";
}

function formatearFecha(fecha) {
    if (!fecha || !fecha.includes("-")) {
        return fecha || "";
    }

    const [anio, mes, dia] = fecha.split("-");
    return `${dia}/${mes}/${anio}`;
}
