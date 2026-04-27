package com.nominet.gestion_empresas.service;

import com.nominet.gestion_empresas.model.Empresa;
import com.nominet.gestion_empresas.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ── CREATE ────────────────────────────────────────────
    public Empresa registrar(Empresa empresa) {
        empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
        return empresaRepository.save(empresa);
    }

    // ── READ ──────────────────────────────────────────────
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    public Optional<Empresa> buscarPorId(Integer id) {
        return empresaRepository.findById(id);
    }

    // ── UPDATE ────────────────────────────────────────────
    public Empresa actualizar(Integer id, Empresa datos) {
        Empresa existente = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada: " + id));

        existente.setNit(datos.getNit());
        existente.setNombre(datos.getNombre());
        existente.setCorreo(datos.getCorreo());
        existente.setCelular(datos.getCelular());

        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return empresaRepository.save(existente);
    }

    // ── LOGIN ─────────────────────────────────────────────
    // Retorna la empresa si las credenciales son correctas
    public Optional<Empresa> login(String correo, String password) {
        Optional<Empresa> empresa = empresaRepository.findByCorreo(correo);
        if (empresa.isPresent() && passwordEncoder.matches(password, empresa.get().getPassword())) {
            return empresa;
        }
        return Optional.empty();
    }

    // ── VALIDACIONES ──────────────────────────────────────
    public boolean existeNit(String nit) {
        return empresaRepository.findByNit(nit).isPresent();
    }

    public boolean existeCorreo(String correo) {
        return empresaRepository.findByCorreo(correo).isPresent();
    }
}