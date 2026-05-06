package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmpleadoDashboardController {

    @GetMapping("/dashboard-empleado")
    public String dashboardEmpleado(HttpSession session, Model model) {
        // Verificar sesión
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";
        // Tanto TRABAJADOR como NOMINA pueden ver sus datos de empleado
        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!rol.equals("TRABAJADOR") && !rol.equals("NOMINA")) {
            return "redirect:/login";
        }


        // Pasar datos al dashboard
        model.addAttribute("usuario", usuario);
        model.addAttribute("empleado", usuario.getEmpleado());

        return "dashboard-empleado";   // → templates/dashboard-empleado.html
    }
}