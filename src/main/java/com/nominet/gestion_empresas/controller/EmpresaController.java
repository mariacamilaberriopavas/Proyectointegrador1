package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.Empresa;
import com.nominet.gestion_empresas.service.EmpresaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    // ── Formulario editar empresa ─────────────────────────
    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable Integer id,
                                HttpSession session,
                                Model model) {
        Empresa empresa = verificarSesion(session);
        if (empresa == null) return "redirect:/login";

        // Solo puede editar sus propios datos
        if (!empresa.getIdempresa().equals(id)) return "redirect:/dashboard";

        model.addAttribute("empresa", empresaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada")));
        return "empresa/editar";
    }

    // ── Guardar cambios empresa ───────────────────────────
    @PostMapping("/editar/{id}")
    public String guardarEditar(@PathVariable Integer id,
                                @Valid @ModelAttribute("empresa") Empresa empresa,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes flash) {
        Empresa empresaSession = verificarSesion(session);
        if (empresaSession == null) return "redirect:/login";
        if (!empresaSession.getIdempresa().equals(id)) return "redirect:/dashboard";

        if (result.hasErrors()) return "empresa/editar";

        Empresa actualizada = empresaService.actualizar(id, empresa);
        session.setAttribute("empresaLogueada", actualizada);  // actualizar sesión
        flash.addFlashAttribute("exito", "Datos de empresa actualizados correctamente");
        return "redirect:/dashboard";
    }

    private Empresa verificarSesion(HttpSession session) {
        return (Empresa) session.getAttribute("empresaLogueada");
    }
}
