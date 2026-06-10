package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Servicio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @EntityGraph(attributePaths = { "categoria" })
    List<Servicio> findByActivoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = { "categoria" })
    Optional<Servicio> findByIdAndActivoTrue(Long id);
}
