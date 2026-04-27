package com.nominet.gestion_empresas.controller;

import com.nominet.gestion_empresas.model.Empresa;
import com.nominet.gestion_empresas.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Verificar sesión
        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");
        if (empresa == null) {
            return "redirect:/login";
        }

        // Pasar datos al dashboard
        model.addAttribute("empresa", empresa);
        model.addAttribute("totalEmpleados",
            empleadoService.listarPorEmpresa(empresa.getIdempresa()).size());

        return "dashboard";   // → templates/dashboard.html
    }
}