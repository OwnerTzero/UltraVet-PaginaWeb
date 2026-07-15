package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Atencion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Long>, JpaSpecificationExecutor<Atencion> {

    @EntityGraph(attributePaths = { "cita", "cita.usuario", "cita.mascota", "cita.servicio" })
    List<Atencion> findByActivoTrueOrderByFechaRegistroDesc();

    @EntityGraph(attributePaths = { "cita", "cita.usuario", "cita.mascota", "cita.servicio" })
    Page<Atencion> findByActivoTrueOrderByFechaRegistroDesc(Pageable pageable);

    Optional<Atencion> findByCitaIdAndActivoTrue(Long citaId);

    long countByActivoTrue();
}
