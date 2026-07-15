package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Cita;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    @EntityGraph(attributePaths = { "usuario", "mascota", "servicio", "estado" })
    List<Cita> findByUsuarioIdAndActivoTrueOrderByFechaDescHoraDesc(Long usuarioId);

    @EntityGraph(attributePaths = { "usuario", "mascota", "servicio", "estado" })
    Page<Cita> findByUsuarioIdAndActivoTrueOrderByFechaDescHoraDesc(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = { "usuario", "mascota", "servicio", "estado" })
    List<Cita> findByActivoTrueOrderByFechaDescHoraDesc();

    @EntityGraph(attributePaths = { "usuario", "mascota", "servicio", "estado" })
    Page<Cita> findByActivoTrueOrderByFechaDescHoraDesc(Pageable pageable);

    @EntityGraph(attributePaths = { "usuario", "mascota", "servicio", "estado" })
    Optional<Cita> findByIdAndUsuarioId(Long id, Long usuarioId);

    long countByActivoTrue();

    long countByActivoTrueAndEstadoNombre(String nombre);
}
