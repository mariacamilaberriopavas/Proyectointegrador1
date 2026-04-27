package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.Empleado;
import com.nominet.gestion_empresas.model.Empresa;
import com.nominet.gestion_empresas.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    // =========================
    // LISTAR
    // =========================
    @GetMapping
    public String listar(HttpSession session, Model model) {

        Empresa empresa = verificarSesion(session);
        if (empresa == null) return "redirect:/login";

        model.addAttribute("empleados",
                empleadoService.listarPorEmpresa(empresa.getIdempresa()));

        model.addAttribute("empresa", empresa);

        return "empleado/lista";
    }

    // =========================
    // FORM NUEVO
    // =========================
    @GetMapping("/nuevo")
    public String nuevoEmpleado(Model model, HttpSession session) {

    Empresa empresa = verificarSesion(session);

    if (empresa == null) {
        return "redirect:/login";
    }

    model.addAttribute("empleado", new Empleado());
    model.addAttribute("empresa", empresa);
    model.addAttribute("modo", "crear");

    return "empleados/formulario";
    }

    // =========================
    // GUARDAR NUEVO
    // =========================
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("empleado") Empleado empleado,
                          BindingResult result,
                          @RequestParam("rol") String rol,
                          HttpSession session,
                          Model model,
                          RedirectAttributes flash) {

        Empresa empresa = verificarSesion(session);
        if (empresa == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("modo", "crear");
            model.addAttribute("empresa", empresa);
            return "empleados/formulario";
        }

        if (empleadoService.existeCedula(empleado.getCedula())) {
            result.rejectValue("cedula", "error.empleado",
                    "Ya existe un empleado con esa cédula");

            model.addAttribute("modo", "crear");
            model.addAttribute("empresa", empresa);
            return "empleados/formulario";
        }

        empleado.setEmpresa(empresa);
        empleadoService.registrar(empleado, rol);

        flash.addFlashAttribute("exito", "Empleado y usuario creados correctamente");
        return "redirect:/dashboard";
    }

    // =========================
    // FORM EDITAR
    // =========================
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id,
                         HttpSession session,
                         Model model) {

        Empresa empresa = verificarSesion(session);
        if (empresa == null) return "redirect:/login";

        Empleado empleado = empleadoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        model.addAttribute("empleado", empleado);
        model.addAttribute("empresa", empresa);
        model.addAttribute("modo", "editar");

        return "empleado/formulario";
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("empleado") Empleado empleado,
                             BindingResult result,
                             HttpSession session,
                             Model model,
                             RedirectAttributes flash) {

        Empresa empresa = verificarSesion(session);
        if (empresa == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("modo", "editar");
            model.addAttribute("empresa", empresa);
            return "empleado/formulario";
        }

        empleado.setEmpresa(empresa);
        empleadoService.actualizar(id, empleado);

        flash.addFlashAttribute("exito", "Empleado actualizado correctamente");
        return "redirect:/empleados";
    }

    // =========================
    // DESACTIVAR
    // =========================
    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Integer id,
                             HttpSession session,
                             RedirectAttributes flash) {

        Empresa empresa = verificarSesion(session);
        if (empresa == null) return "redirect:/login";

        empleadoService.desactivar(id);

        flash.addFlashAttribute("exito", "Empleado desactivado");
        return "redirect:/empleados";
    }

    // =========================
    // SESIÓN
    // =========================
    private Empresa verificarSesion(HttpSession session) {
        return (Empresa) session.getAttribute("empresaLogueada");
    }
}