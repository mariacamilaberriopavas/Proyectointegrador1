package com.nominet.gestion_empresas.service;

import com.nominet.gestion_empresas.model.*;
import com.nominet.gestion_empresas.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Liquidación de nómina según ley colombiana vigente 2025.
 *
 * SMMLV 2025: $1.423.500
 * Auxilio de transporte: $202.612 (para salarios <= 2 SMMLV)
 *
 * DEDUCCIONES EMPLEADO:
 *   - Salud:     4% del salario base
 *   - Pensión:   4% del salario base
 *   - Fondo solidaridad: 1% si salario > 4 SMMLV ($5.694.000)
 *
 * HORAS EXTRAS (recargo sobre valor hora ordinaria):
 *   - Diurna          (6am-9pm L-S):  +25%  → factor 1.25
 *   - Nocturna        (9pm-6am):       +75%  → factor 1.75
 *   - Dominical/fest. diurna:          +75%  → factor 1.75
 *   - Dominical/fest. nocturna:       +150%  → factor 2.50
 *
 * Valor hora ordinaria = salario mensual / 240
 */
@Service
public class NominaColombiaService {

    // ── Constantes 2025 ───────────────────────────────────
    public static final BigDecimal SMMLV          = new BigDecimal("1423500");
    public static final BigDecimal AUX_TRANSPORTE = new BigDecimal("202612");
    public static final BigDecimal SALUD_PCT      = new BigDecimal("0.04");
    public static final BigDecimal PENSION_PCT    = new BigDecimal("0.04");
    public static final BigDecimal SOLIDARIDAD_PCT= new BigDecimal("0.01");
    public static final BigDecimal LIMITE_AUX     = SMMLV.multiply(new BigDecimal("2"));   // 2 SMMLV
    public static final BigDecimal LIMITE_SOLID   = SMMLV.multiply(new BigDecimal("4"));   // 4 SMMLV
    public static final BigDecimal HORAS_MES      = new BigDecimal("240");

    // Factores hora extra (1 + recargo)
    public static final BigDecimal FACTOR_ED  = new BigDecimal("1.25"); // extra diurna
    public static final BigDecimal FACTOR_EN  = new BigDecimal("1.75"); // extra nocturna
    public static final BigDecimal FACTOR_DD  = new BigDecimal("1.75"); // dominical diurna
    public static final BigDecimal FACTOR_DN  = new BigDecimal("2.50"); // dominical nocturna

    @Autowired
    private HorasExtrasRepository horasExtrasRepository;

    @Autowired
    private ColillaPagoRepository colillaPagoRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    // ── Calcular valor hora ordinaria ─────────────────────
    public BigDecimal valorHoraOrdinaria(BigDecimal salario) {
        return salario.divide(HORAS_MES, 2, RoundingMode.HALF_UP);
    }

    // ── Calcular recargo según tipo ───────────────────────
    public BigDecimal factorPorTipo(String tipo) {
        if (tipo == null) return FACTOR_ED;
        return switch (tipo.toUpperCase()) {
            case "NOCTURNA"          -> FACTOR_EN;
            case "DOMINICAL_DIURNA"  -> FACTOR_DD;
            case "DOMINICAL_NOCTURNA"-> FACTOR_DN;
            default                  -> FACTOR_ED; // DIURNA
        };
    }

    public BigDecimal recargoPorTipo(String tipo) {
        if (tipo == null) return new BigDecimal("0.25");
        return switch (tipo.toUpperCase()) {
            case "NOCTURNA"          -> new BigDecimal("0.75");
            case "DOMINICAL_DIURNA"  -> new BigDecimal("0.75");
            case "DOMINICAL_NOCTURNA"-> new BigDecimal("1.50");
            default                  -> new BigDecimal("0.25");
        };
    }

    // ── Calcular total de una hora extra ──────────────────
    public BigDecimal calcularTotalHoraExtra(BigDecimal horas, BigDecimal valorHora, String tipo) {
        BigDecimal factor = factorPorTipo(tipo);
        return horas.multiply(valorHora).multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Liquidar colilla completa ─────────────────────────
    @Transactional
    public ResumenNomina liquidar(Empleado empleado, LocalDate inicio, LocalDate fin) {

        // 1. Obtener salario del contrato vigente
        Contrato contrato = contratoRepository
                .findTopByEmpleadoOrderByFechaInicioDesc(empleado)
                .orElseThrow(() -> new RuntimeException(
                        "El empleado " + empleado.getNombre() + " no tiene contrato vigente"));

        BigDecimal salario = contrato.getSalario();
        BigDecimal valorHora = valorHoraOrdinaria(salario);

        // 2. Auxilio de transporte (solo si salario <= 2 SMMLV)
        BigDecimal auxTransporte = salario.compareTo(LIMITE_AUX) <= 0
                ? AUX_TRANSPORTE : BigDecimal.ZERO;

        // 3. Cargar horas extras del período
        List<HorasExtras> extras = horasExtrasRepository
                .findByEmpleadoAndFechaBetween(empleado, inicio, fin);

        BigDecimal totalExtras = extras.stream()
                .map(he -> he.getTotal() != null ? he.getTotal() :
                        calcularTotalHoraExtra(he.getHoras(), valorHora, he.getTipo()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHoras = extras.stream()
                .map(HorasExtras::getHoras)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Deducciones
        BigDecimal descSalud    = salario.multiply(SALUD_PCT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal descPension  = salario.multiply(PENSION_PCT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal descSolid    = salario.compareTo(LIMITE_SOLID) > 0
                ? salario.multiply(SOLIDARIDAD_PCT).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalDeducciones = descSalud.add(descPension).add(descSolid);

        // 5. Total a pagar
        BigDecimal totalPagar = salario
                .add(auxTransporte)
                .add(totalExtras)
                .subtract(totalDeducciones)
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Armar resumen
        ResumenNomina resumen = new ResumenNomina();
        resumen.setEmpleado(empleado);
        resumen.setContrato(contrato);
        resumen.setSalarioBase(salario);
        resumen.setValorHoraOrdinaria(valorHora);
        resumen.setAuxTransporte(auxTransporte);
        resumen.setHorasExtras(extras);
        resumen.setTotalHorasExtras(totalHoras);
        resumen.setTotalValorExtras(totalExtras);
        resumen.setDescSalud(descSalud);
        resumen.setDescPension(descPension);
        resumen.setDescSolidaridad(descSolid);
        resumen.setTotalDeducciones(totalDeducciones);
        resumen.setTotalPagar(totalPagar);
        resumen.setPeriodoInicio(inicio);
        resumen.setPeriodoFin(fin);
        resumen.setAplicaAuxTransporte(salario.compareTo(LIMITE_AUX) <= 0);
        resumen.setAplicaSolidaridad(salario.compareTo(LIMITE_SOLID) > 0);

        return resumen;
    }

    // ── Guardar colilla desde resumen ─────────────────────
    @Transactional
    public ColillaPago guardarColilla(ResumenNomina resumen) {
        ColillaPago colilla = new ColillaPago();
        colilla.setEmpleado(resumen.getEmpleado());
        colilla.setPeriodoInicio(resumen.getPeriodoInicio());
        colilla.setPeriodoFin(resumen.getPeriodoFin());
        colilla.setSalarioBase(resumen.getSalarioBase());
        colilla.setHorasExtras(resumen.getTotalHorasExtras());
        colilla.setValorHorasExtras(resumen.getTotalValorExtras());
        colilla.setDeducciones(resumen.getTotalDeducciones());
        colilla.setTotalPagar(resumen.getTotalPagar());
        return colillaPagoRepository.save(colilla);
    }

    // ── DTO resumen liquidación ───────────────────────────
    public static class ResumenNomina {
        private Empleado empleado;
        private Contrato contrato;
        private BigDecimal salarioBase;
        private BigDecimal valorHoraOrdinaria;
        private BigDecimal auxTransporte;
        private List<HorasExtras> horasExtras;
        private BigDecimal totalHorasExtras;
        private BigDecimal totalValorExtras;
        private BigDecimal descSalud;
        private BigDecimal descPension;
        private BigDecimal descSolidaridad;
        private BigDecimal totalDeducciones;
        private BigDecimal totalPagar;
        private LocalDate periodoInicio;
        private LocalDate periodoFin;
        private boolean aplicaAuxTransporte;
        private boolean aplicaSolidaridad;

        // getters y setters
        public Empleado getEmpleado() { return empleado; }
        public void setEmpleado(Empleado e) { this.empleado = e; }
        public Contrato getContrato() { return contrato; }
        public void setContrato(Contrato c) { this.contrato = c; }
        public BigDecimal getSalarioBase() { return salarioBase; }
        public void setSalarioBase(BigDecimal v) { this.salarioBase = v; }
        public BigDecimal getValorHoraOrdinaria() { return valorHoraOrdinaria; }
        public void setValorHoraOrdinaria(BigDecimal v) { this.valorHoraOrdinaria = v; }
        public BigDecimal getAuxTransporte() { return auxTransporte; }
        public void setAuxTransporte(BigDecimal v) { this.auxTransporte = v; }
        public List<HorasExtras> getHorasExtras() { return horasExtras; }
        public void setHorasExtras(List<HorasExtras> v) { this.horasExtras = v; }
        public BigDecimal getTotalHorasExtras() { return totalHorasExtras; }
        public void setTotalHorasExtras(BigDecimal v) { this.totalHorasExtras = v; }
        public BigDecimal getTotalValorExtras() { return totalValorExtras; }
        public void setTotalValorExtras(BigDecimal v) { this.totalValorExtras = v; }
        public BigDecimal getDescSalud() { return descSalud; }
        public void setDescSalud(BigDecimal v) { this.descSalud = v; }
        public BigDecimal getDescPension() { return descPension; }
        public void setDescPension(BigDecimal v) { this.descPension = v; }
        public BigDecimal getDescSolidaridad() { return descSolidaridad; }
        public void setDescSolidaridad(BigDecimal v) { this.descSolidaridad = v; }
        public BigDecimal getTotalDeducciones() { return totalDeducciones; }
        public void setTotalDeducciones(BigDecimal v) { this.totalDeducciones = v; }
        public BigDecimal getTotalPagar() { return totalPagar; }
        public void setTotalPagar(BigDecimal v) { this.totalPagar = v; }
        public LocalDate getPeriodoInicio() { return periodoInicio; }
        public void setPeriodoInicio(LocalDate v) { this.periodoInicio = v; }
        public LocalDate getPeriodoFin() { return periodoFin; }
        public void setPeriodoFin(LocalDate v) { this.periodoFin = v; }
        public boolean isAplicaAuxTransporte() { return aplicaAuxTransporte; }
        public void setAplicaAuxTransporte(boolean v) { this.aplicaAuxTransporte = v; }
        public boolean isAplicaSolidaridad() { return aplicaSolidaridad; }
        public void setAplicaSolidaridad(boolean v) { this.aplicaSolidaridad = v; }
    }
}