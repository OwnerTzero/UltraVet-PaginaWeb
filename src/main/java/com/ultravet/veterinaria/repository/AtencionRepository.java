package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Atencion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    @EntityGraph(attributePaths = { "cita", "cita.usuario", "cita.mascota", "cita.servicio" })
    List<Atencion> findByActivoTrueOrderByFechaRegistroDesc();

    Optional<Atencion> findByCitaIdAndActivoTrue(Long citaId);
}
