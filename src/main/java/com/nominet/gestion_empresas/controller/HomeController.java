package com.nominet.gestion_empresas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Ruta raíz → landing page
    @GetMapping("/")
    public String home() {
        return "index";   // → templates/index.html
    }

    // Ruta /inicio también va al index
    @GetMapping("/inicio")
    public String inicio() {
        return "index";
    }
}