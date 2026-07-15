package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Mascota;
import com.ultravet.veterinaria.model.SolicitudAdopcion;
import com.ultravet.veterinaria.model.Usuario;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcion, Long>,
        JpaSpecificationExecutor<SolicitudAdopcion> {

    boolean existsByUsuarioAndMascota(Usuario usuario, Mascota mascota);

    @EntityGraph(attributePaths = { "usuario", "mascota", "estado" })
    List<SolicitudAdopcion> findByActivoTrueOrderByFechaEnvioDesc();

    @EntityGraph(attributePaths = { "usuario", "mascota", "estado" })
    Page<SolicitudAdopcion> findByActivoTrueOrderByFechaEnvioDesc(Pageable pageable);

    long countByActivoTrue();

    long countByActivoTrueAndEstadoNombre(String nombre);
}
