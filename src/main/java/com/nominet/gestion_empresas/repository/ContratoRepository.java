package com.nominet.gestion_empresas.repository;

import com.nominet.gestion_empresas.model.Contrato;
import com.nominet.gestion_empresas.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Integer> {
    List<Contrato> findByEmpleado(Empleado empleado);
    Optional<Contrato> findTopByEmpleadoOrderByFechaInicioDesc(Empleado empleado);
}