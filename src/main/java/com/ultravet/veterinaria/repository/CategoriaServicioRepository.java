package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.CategoriaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, Long> {
}
