package com.nominet.gestion_empresas.service;

import com.nominet.gestion_empresas.model.HorasExtras;
import com.nominet.gestion_empresas.model.Empleado;
import com.nominet.gestion_empresas.repository.HorasExtrasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class HorasExtrasService {

    @Autowired
    private HorasExtrasRepository horasExtrasRepository;

    public List<HorasExtras> listarPorEmpleado(Empleado empleado) {
        return horasExtrasRepository.findByEmpleado(empleado);
    }

    public List<HorasExtras> listarPorEmpleadoYPeriodo(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin) {
        return horasExtrasRepository.findByEmpleadoAndFechaBetween(empleado, fechaInicio, fechaFin);
    }

    public HorasExtras guardar(HorasExtras horasExtras) {
        return horasExtrasRepository.save(horasExtras);
    }
}