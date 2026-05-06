package com.nominet.gestion_empresas.service;

import com.nominet.gestion_empresas.model.ColillaPago;
import com.nominet.gestion_empresas.model.Empleado;
import com.nominet.gestion_empresas.repository.ColillaPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ColillaPagoService {

    @Autowired
    private ColillaPagoRepository colillaPagoRepository;

    public List<ColillaPago> listarPorEmpleado(Empleado empleado) {
        return colillaPagoRepository.findByEmpleado(empleado);
    }

    public Optional<ColillaPago> buscarPorEmpleadoYPeriodo(Empleado empleado, LocalDate periodoInicio, LocalDate periodoFin) {
        return colillaPagoRepository.findByEmpleadoAndPeriodoInicioAndPeriodoFin(empleado, periodoInicio, periodoFin);
    }

    public ColillaPago guardar(ColillaPago colillaPago) {
        return colillaPagoRepository.save(colillaPago);
    }
}