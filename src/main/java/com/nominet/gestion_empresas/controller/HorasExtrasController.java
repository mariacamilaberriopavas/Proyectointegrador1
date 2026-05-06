package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.HorasExtras;
import com.nominet.gestion_empresas.model.Usuario;
import com.nominet.gestion_empresas.service.HorasExtrasService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/horas-extras")
public class HorasExtrasController {

    @Autowired
    private HorasExtrasService horasExtrasService;

    @GetMapping
    public String mostrarHorasExtras(HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        List<HorasExtras> horasExtras = horasExtrasService.listarPorEmpleado(usuario.getEmpleado());
        model.addAttribute("horasExtras", horasExtras);
        model.addAttribute("usuario", usuario);

        return "horas-extras/lista";
    }

    @GetMapping("/consultar")
    public String consultarHorasExtras(@RequestParam(required = false) String fechaInicio,
                                       @RequestParam(required = false) String fechaFin,
                                       HttpSession session, Model model, RedirectAttributes flash) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        List<HorasExtras> horasExtras;
        if (fechaInicio != null && fechaFin != null) {
            try {
                LocalDate inicio = LocalDate.parse(fechaInicio, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate fin = LocalDate.parse(fechaFin, DateTimeFormatter.ISO_LOCAL_DATE);

                horasExtras = horasExtrasService.listarPorEmpleadoYPeriodo(usuario.getEmpleado(), inicio, fin);
                if (horasExtras.isEmpty()) {
                    flash.addFlashAttribute("mensaje", "No hay horas extras registradas para el período seleccionado.");
                }
            } catch (Exception e) {
                flash.addFlashAttribute("error", "Formato de fecha inválido.");
                horasExtras = horasExtrasService.listarPorEmpleado(usuario.getEmpleado());
            }
        } else {
            horasExtras = horasExtrasService.listarPorEmpleado(usuario.getEmpleado());
        }

        model.addAttribute("horasExtras", horasExtras);
        model.addAttribute("usuario", usuario);
        return "horas-extras/consulta";
    }

    private Usuario verificarSesion(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogueado");
    }
}