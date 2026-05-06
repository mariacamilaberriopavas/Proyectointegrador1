package com.nominet.gestion_empresas.service;

import com.nominet.gestion_empresas.model.Empleado;
import com.nominet.gestion_empresas.model.Empresa;
import com.nominet.gestion_empresas.model.Usuario;
import com.nominet.gestion_empresas.repository.EmpleadoRepository;
import com.nominet.gestion_empresas.repository.EmpresaRepository;
import com.nominet.gestion_empresas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ── CREATE ────────────────────────────────────────────
    public Empleado registrar(Empleado empleado, String rol) {
        empleado.setEstado("activo");
        Empleado empGuardado = empleadoRepository.save(empleado);

        // Validar que no exista usuario con ese correo
        if (usuarioRepository.findByCorreo(empGuardado.getCorreo()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese correo: " + empGuardado.getCorreo());
        }

        // Crear usuario automáticamente
        Usuario usuario = new Usuario();
        usuario.setCorreo(empGuardado.getCorreo());
        usuario.setPassword(passwordEncoder.encode("123456")); // contraseña por defecto
        usuario.setRol(rol.toUpperCase());
        usuario.setUsuariocl(empGuardado.getNombre());
        usuario.setEmpleado(empGuardado);
        usuarioRepository.save(usuario);

        return empGuardado;
    }

    // ── READ ──────────────────────────────────────────────
    public List<Empleado> listarPorEmpresa(Integer idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        return empleadoRepository.findByEmpresa(empresa);
    }

    public Optional<Empleado> buscarPorId(Integer id) {
        return empleadoRepository.findById(id);
    }

    // ── UPDATE ────────────────────────────────────────────
    public Empleado actualizar(Integer id, Empleado datos) {
        Empleado existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + id));
        existente.setCedula(datos.getCedula());
        existente.setNombre(datos.getNombre());
        existente.setCelular(datos.getCelular());
        existente.setCorreo(datos.getCorreo());
        existente.setEstado(datos.getEstado());
        return empleadoRepository.save(existente);
    }

    // ── DESACTIVAR (soft-delete) ──────────────────────────
    public void desactivar(Integer id) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        emp.setEstado("inactivo");
        empleadoRepository.save(emp);
    }

    // ── REACTIVAR ─────────────────────────────────────────
    public void reactivar(Integer id) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        emp.setEstado("activo");
        empleadoRepository.save(emp);
    }

    // ── VALIDACIONES ──────────────────────────────────────
    public boolean existeCedula(Integer cedula) {
        return empleadoRepository.findByCedula(cedula).isPresent();
    }
}