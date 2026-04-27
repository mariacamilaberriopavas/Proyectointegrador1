// ─── EmpresaRepository.java ───────────────────────────────────────────────────
package com.nominet.gestion_empresas.repository;

import com.nominet.gestion_empresas.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    Optional<Empresa> findByNit(String nit);
    Optional<Empresa> findByCorreo(String correo);
    // Login empresa: correo + password
    Optional<Empresa> findByCorreoAndPassword(String correo, String password);
}
