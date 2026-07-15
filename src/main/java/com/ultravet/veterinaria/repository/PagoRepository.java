package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Pago;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long>, JpaSpecificationExecutor<Pago> {

    @EntityGraph(attributePaths = { "cita", "cita.usuario", "cita.mascota", "metodoPago", "estadoPago" })
    List<Pago> findByActivoTrueOrderByFechaPagoDescIdDesc();

    @EntityGraph(attributePaths = { "cita", "cita.usuario", "cita.mascota", "metodoPago", "estadoPago" })
    Page<Pago> findByActivoTrueOrderByFechaPagoDescIdDesc(Pageable pageable);

    @Query("select coalesce(sum(p.monto), 0) from Pago p where p.activo = true")
    BigDecimal sumMontoActivo();

    long countByActivoTrue();
}
