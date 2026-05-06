package com.nominet.gestion_empresas.repository;

import com.nominet.gestion_empresas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);

    // Carga el empleado y su empresa en la misma consulta (evita LazyInitializationException)
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.empleado e LEFT JOIN FETCH e.empresa WHERE u.correo = :correo")
    Optional<Usuario> findByCorreoWithEmpleado(@Param("correo") String correo);
}