package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
}