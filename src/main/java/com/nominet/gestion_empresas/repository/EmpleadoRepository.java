package com.nominet.gestion_empresas.repository;

import com.nominet.gestion_empresas.model.Empleado;
import com.nominet.gestion_empresas.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    // Todos los empleados de una empresa
    List<Empleado> findByEmpresa(Empresa empresa);
    // Buscar por cédula
    Optional<Empleado> findByCedula(Integer cedula);
}
