package com.nominet.gestion_empresas.repository;

import com.nominet.gestion_empresas.model.ColillaPago;
import com.nominet.gestion_empresas.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ColillaPagoRepository extends JpaRepository<ColillaPago, Integer> {
    // Colillas por empleado
    List<ColillaPago> findByEmpleado(Empleado empleado);

    // Colilla por empleado y período
    Optional<ColillaPago> findByEmpleadoAndPeriodoInicioAndPeriodoFin(Empleado empleado, LocalDate periodoInicio, LocalDate periodoFin);
}