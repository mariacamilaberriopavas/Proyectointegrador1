package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.*;
import com.nominet.gestion_empresas.repository.*;
import com.nominet.gestion_empresas.service.EmpleadoService;
import com.nominet.gestion_empresas.service.NominaColombiaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/nomina")
public class NominaController {

    @Autowired private EmpleadoService empleadoService;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private HorasExtrasRepository horasExtrasRepository;
    @Autowired private ColillaPagoRepository colillaPagoRepository;
    @Autowired private NominaColombiaService nominaService;

    // ── Dashboard ─────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empresa empresa = usuario.getEmpleado().getEmpresa();
        List<Empleado> empleados = empleadoService.listarPorEmpresa(empresa.getIdempresa());
        long activos = empleados.stream().filter(e -> "activo".equalsIgnoreCase(e.getEstado())).count();

        model.addAttribute("usuario", usuario);
        model.addAttribute("empresa", empresa);
        model.addAttribute("totalEmpleados", empleados.size());
        model.addAttribute("empleadosActivos", activos);
        model.addAttribute("smmlv", NominaColombiaService.SMMLV);
        return "nomina/dashboard";
    }

    // ── Lista empleados ───────────────────────────────────
    @GetMapping("/empleados")
    public String listarEmpleados(HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empresa empresa = usuario.getEmpleado().getEmpresa();
        model.addAttribute("empleados", empleadoService.listarPorEmpresa(empresa.getIdempresa()));
        model.addAttribute("usuario", usuario);
        model.addAttribute("empresa", empresa);
        return "nomina/empleados-lista";
    }

    // ── Contratos de un empleado ──────────────────────────
    @GetMapping("/contratos/{idEmpleado}")
    public String verContratos(@PathVariable Integer idEmpleado,
                               HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empleado emp = empleadoService.buscarPorId(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        model.addAttribute("empleado", emp);
        model.addAttribute("contratos", contratoRepository.findByEmpleado(emp));
        model.addAttribute("usuario", usuario);
        return "nomina/contratos";
    }

    // ── Nuevo contrato ────────────────────────────────────
    @GetMapping("/contratos/nuevo/{idEmpleado}")
    public String nuevoContrato(@PathVariable Integer idEmpleado,
                                HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empleado emp = empleadoService.buscarPorId(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        model.addAttribute("contrato", new Contrato());
        model.addAttribute("empleado", emp);
        model.addAttribute("usuario", usuario);
        model.addAttribute("smmlv", NominaColombiaService.SMMLV);
        return "nomina/contrato-form";
    }

    @PostMapping("/contratos/guardar/{idEmpleado}")
    public String guardarContrato(@PathVariable Integer idEmpleado,
                                  @ModelAttribute Contrato contrato,
                                  HttpSession session, RedirectAttributes flash) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empleado emp = empleadoService.buscarPorId(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        contrato.setEmpleado(emp);
        contratoRepository.save(contrato);
        flash.addFlashAttribute("exito", "Contrato guardado correctamente");
        return "redirect:/nomina/contratos/" + idEmpleado;
    }

    // ── Registrar horas extras ────────────────────────────
    @GetMapping("/horas-extras/nuevo/{idEmpleado}")
    public String nuevoHorasExtras(@PathVariable Integer idEmpleado,
                                   HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empleado emp = empleadoService.buscarPorId(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // Obtener salario del contrato vigente para mostrar valor hora
        BigDecimal valorHora = contratoRepository
                .findTopByEmpleadoOrderByFechaInicioDesc(emp)
                .map(c -> nominaService.valorHoraOrdinaria(c.getSalario()))
                .orElse(BigDecimal.ZERO);

        model.addAttribute("horasExtras", new HorasExtras());
        model.addAttribute("empleado", emp);
        model.addAttribute("usuario", usuario);
        model.addAttribute("valorHoraOrdinaria", valorHora);
        return "nomina/horas-extras-form";
    }

    @PostMapping("/horas-extras/guardar/{idEmpleado}")
    public String guardarHorasExtras(@PathVariable Integer idEmpleado,
                                     @ModelAttribute HorasExtras horasExtras,
                                     HttpSession session, RedirectAttributes flash) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empleado emp = empleadoService.buscarPorId(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // Obtener valor hora ordinaria del contrato
        BigDecimal valorHora = contratoRepository
                .findTopByEmpleadoOrderByFechaInicioDesc(emp)
                .map(c -> nominaService.valorHoraOrdinaria(c.getSalario()))
                .orElse(BigDecimal.ZERO);

        horasExtras.setEmpleado(emp);
        horasExtras.setValorPorHora(valorHora);
        horasExtras.setRecargo(nominaService.recargoPorTipo(horasExtras.getTipo()));
        horasExtras.setTotal(nominaService.calcularTotalHoraExtra(
                horasExtras.getHoras(), valorHora, horasExtras.getTipo()));

        horasExtrasRepository.save(horasExtras);
        flash.addFlashAttribute("exito", "Horas extras registradas correctamente");
        return "redirect:/nomina/contratos/" + idEmpleado;
    }

    // ── Previsualizar colilla (carga extras automáticamente) ──
    @GetMapping("/colilla/preview/{idEmpleado}")
    public String previewColilla(@PathVariable Integer idEmpleado,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
                                 HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empleado emp = empleadoService.buscarPorId(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        model.addAttribute("empleado", emp);
        model.addAttribute("usuario", usuario);
        model.addAttribute("hoy", LocalDate.now());

        if (inicio != null && fin != null) {
            try {
                NominaColombiaService.ResumenNomina resumen =
                        nominaService.liquidar(emp, inicio, fin);
                model.addAttribute("resumen", resumen);
                model.addAttribute("inicio", inicio);
                model.addAttribute("fin", fin);
            } catch (RuntimeException e) {
                model.addAttribute("errorMsg", e.getMessage());
            }
        }
        return "nomina/colilla-preview";
    }

    // ── Confirmar y guardar colilla ───────────────────────
    @PostMapping("/colilla/guardar/{idEmpleado}")
    public String guardarColilla(@PathVariable Integer idEmpleado,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
                                 HttpSession session, RedirectAttributes flash) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        Empleado emp = empleadoService.buscarPorId(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        try {
            NominaColombiaService.ResumenNomina resumen = nominaService.liquidar(emp, inicio, fin);
            nominaService.guardarColilla(resumen);
            flash.addFlashAttribute("exito", "Colilla de pago generada y guardada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nomina/contratos/" + idEmpleado;
    }

    // ── Mis datos (NOMINA ve sus datos de empleado) ───────
    @GetMapping("/mis-datos")
    public String misDatos(HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("empleado", usuario.getEmpleado());
        return "dashboard-empleado";
    }

    private Usuario verificarSesion(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"NOMINA".equalsIgnoreCase(u.getRol())) return null;
        return u;
    }
}