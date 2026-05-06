package com.nominet.gestion_empresas.repository;

import com.nominet.gestion_empresas.model.Empleado;
import com.nominet.gestion_empresas.model.HorasExtras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HorasExtrasRepository extends JpaRepository<HorasExtras, Integer> {

    List<HorasExtras> findByEmpleado(Empleado empleado);

    // Para cargar automáticamente en la colilla
    List<HorasExtras> findByEmpleadoAndFechaBetween(
        Empleado empleado, LocalDate inicio, LocalDate fin);
}