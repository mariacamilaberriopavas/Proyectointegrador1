package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.ColillaPago;
import com.nominet.gestion_empresas.model.Usuario;
import com.nominet.gestion_empresas.service.ColillaPagoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/colilla")
public class ColillaController {

    @Autowired
    private ColillaPagoService colillaPagoService;

    @GetMapping
    public String mostrarColillas(HttpSession session, Model model) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        List<ColillaPago> colillas = colillaPagoService.listarPorEmpleado(usuario.getEmpleado());
        model.addAttribute("colillas", colillas);
        model.addAttribute("usuario", usuario);

        return "colilla/lista";
    }

    @GetMapping("/consultar")
    public String consultarColilla(@RequestParam(required = false) String periodoInicio,
                                   @RequestParam(required = false) String periodoFin,
                                   HttpSession session, Model model, RedirectAttributes flash) {
        Usuario usuario = verificarSesion(session);
        if (usuario == null) return "redirect:/login";

        if (periodoInicio != null && periodoFin != null) {
            try {
                LocalDate inicio = LocalDate.parse(periodoInicio, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate fin = LocalDate.parse(periodoFin, DateTimeFormatter.ISO_LOCAL_DATE);

                Optional<ColillaPago> colilla = colillaPagoService.buscarPorEmpleadoYPeriodo(usuario.getEmpleado(), inicio, fin);
                if (colilla.isPresent()) {
                    model.addAttribute("colilla", colilla.get());
                } else {
                    flash.addFlashAttribute("mensaje", "No hay registros para el período seleccionado.");
                }
            } catch (Exception e) {
                flash.addFlashAttribute("error", "Formato de fecha inválido.");
            }
        }

        model.addAttribute("usuario", usuario);
        return "colilla/consulta";
    }

    private Usuario verificarSesion(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogueado");
    }
}