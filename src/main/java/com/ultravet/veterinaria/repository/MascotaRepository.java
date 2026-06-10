package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Mascota;
import com.ultravet.veterinaria.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    @EntityGraph(attributePaths = { "estado", "prioridad" })
    List<Mascota> findByActivoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = { "estado", "prioridad" })
    Optional<Mascota> findByIdAndActivoTrue(Long id);

    Optional<Mascota> findFirstByUsuarioAndNombreIgnoreCaseAndActivoTrue(Usuario usuario, String nombre);
}
