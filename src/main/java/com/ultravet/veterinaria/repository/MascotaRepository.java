package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Mascota;
import com.ultravet.veterinaria.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long>, JpaSpecificationExecutor<Mascota> {

    @EntityGraph(attributePaths = { "estado", "prioridad" })
    List<Mascota> findByActivoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = { "estado", "prioridad" })
    Page<Mascota> findByActivoTrueOrderByIdAsc(Pageable pageable);

    @EntityGraph(attributePaths = { "estado", "prioridad" })
    List<Mascota> findByActivoTrueAndEstadoNombreInOrderByIdAsc(List<String> nombres);

    @EntityGraph(attributePaths = { "estado", "prioridad" })
    Page<Mascota> findByActivoTrueAndEstadoNombreInOrderByIdAsc(List<String> nombres, Pageable pageable);

    @EntityGraph(attributePaths = { "estado", "prioridad" })
    Optional<Mascota> findByIdAndActivoTrue(Long id);

    Optional<Mascota> findFirstByUsuarioAndNombreIgnoreCaseAndActivoTrue(Usuario usuario, String nombre);

    long countByActivoTrue();

    long countByActivoTrueAndEstadoNombreIn(List<String> nombres);
}
