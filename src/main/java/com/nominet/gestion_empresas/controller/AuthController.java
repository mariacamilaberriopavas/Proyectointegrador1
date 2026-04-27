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

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private EmpresaService empresaService;

    // ─── GET /login ───────────────────────────────────────
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute("empresaLogueada") != null) {
            return "redirect:/dashboard";
        }
        // Busca templates/auth/login.html
        return "auth/login";
    }

    // ─── POST /login ──────────────────────────────────────
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes flash) {

        Optional<Empresa> empresa = empresaService.login(correo, password);

        if (empresa.isPresent()) {
            session.setAttribute("empresaLogueada", empresa.get());
            return "redirect:/dashboard";
        }

        flash.addFlashAttribute("error", "Correo o contraseña incorrectos");
        return "redirect:/login";
    }

    // ─── GET /registro ────────────────────────────────────
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("empresa", new Empresa());
        // Busca templates/auth/registro.html
        return "auth/registro";
    }

    // ─── POST /registro ───────────────────────────────────
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("empresa") Empresa empresa,
                                   BindingResult result,
                                   RedirectAttributes flash) {

        if (result.hasErrors()) {
            return "auth/registro";
        }

        if (empresaService.existeNit(empresa.getNit())) {
            result.rejectValue("nit", "error.empresa", "Ya existe una empresa con ese NIT");
            return "auth/registro";
        }

        if (empresaService.existeCorreo(empresa.getCorreo())) {
            result.rejectValue("correo", "error.empresa", "Ya existe una empresa con ese correo");
            return "auth/registro";
        }

        empresaService.registrar(empresa);
        flash.addFlashAttribute("exito", "¡Empresa registrada! Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    // ─── GET /logout ──────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}