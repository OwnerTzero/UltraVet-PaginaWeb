package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Servicio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long>, JpaSpecificationExecutor<Servicio> {

    @EntityGraph(attributePaths = { "categoria" })
    List<Servicio> findByActivoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = { "categoria" })
    Page<Servicio> findByActivoTrueOrderByIdAsc(Pageable pageable);

    @EntityGraph(attributePaths = { "categoria" })
    Optional<Servicio> findByIdAndActivoTrue(Long id);

    long countByActivoTrue();
}
