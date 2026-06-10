package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoPagoRepository extends JpaRepository<EstadoPago, Long> {
}
