package com.ultravet.veterinaria.repository;

import com.ultravet.veterinaria.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByDni(String dni);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByDni(String dni);

    @EntityGraph(attributePaths = { "rol" })
    List<Usuario> findByActivoTrueOrderByIdAsc();
}
