package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.Empresa;
import com.nominet.gestion_empresas.model.Usuario;
import com.nominet.gestion_empresas.service.EmpresaService;
import com.nominet.gestion_empresas.service.UsuarioService;
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

    @Autowired
    private UsuarioService usuarioService;

    // ─── GET /login ───────────────────────────────────────
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute("empresaLogueada") != null) {
            return "redirect:/dashboard";
        }
        if (session.getAttribute("usuarioLogueado") != null) {
            Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
            return redirectByRol(u.getRol());
        }
        return "auth/login";
    }

    // ─── POST /login ──────────────────────────────────────
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes flash) {

        // 1) Login como empresa (rol ADMIN)
        Optional<Empresa> empresa = empresaService.login(correo, password);
        if (empresa.isPresent()) {
            session.setAttribute("empresaLogueada", empresa.get());
            session.setAttribute("tipoUsuario", "ADMIN");
            return "redirect:/dashboard";
        }

        // 2) Login como usuario (NOMINA / TRABAJADOR / SUPER_ADMIN)
        Optional<Usuario> usuario = usuarioService.login(correo, password);
        if (usuario.isPresent()) {
            session.setAttribute("usuarioLogueado", usuario.get());
            session.setAttribute("tipoUsuario", usuario.get().getRol());
            return redirectByRol(usuario.get().getRol());
        }

        flash.addFlashAttribute("error", "Correo o contraseña incorrectos");
        return "redirect:/login";
    }

    // ─── GET /registro ────────────────────────────────────
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "auth/registro";
    }

    // ─── POST /registro ───────────────────────────────────
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("empresa") Empresa empresa,
                                   BindingResult result,
                                   RedirectAttributes flash) {

        if (result.hasErrors()) return "auth/registro";

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
        return "redirect:/";
    }

    // ─── HELPER ───────────────────────────────────────────
    private String redirectByRol(String rol) {
        if (rol == null) return "redirect:/dashboard-empleado";
        return switch (rol.toUpperCase()) {
            case "SUPER_ADMIN" -> "redirect:/admin/empresas";
            case "NOMINA"      -> "redirect:/nomina/dashboard";
            case "TRABAJADOR"  -> "redirect:/dashboard-empleado";
            default            -> "redirect:/dashboard-empleado";
        };
    }
}