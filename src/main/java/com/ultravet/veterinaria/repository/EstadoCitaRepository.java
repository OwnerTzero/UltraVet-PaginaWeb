package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadoCitaRepository extends JpaRepository<EstadoCita, Long> {

    Optional<EstadoCita> findByNombre(String nombre);

}