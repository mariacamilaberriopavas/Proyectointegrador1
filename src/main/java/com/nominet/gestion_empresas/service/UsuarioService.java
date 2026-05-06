package com.nominet.gestion_empresas.service;

import com.nominet.gestion_empresas.model.Usuario;
import com.nominet.gestion_empresas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public Optional<Usuario> login(String correo, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoWithEmpleado(correo);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                // Forzar carga del empleado y su empresa antes de guardar en sesión
                if (usuario.getEmpleado() != null) {
                    usuario.getEmpleado().getNombre();
                    if (usuario.getEmpleado().getEmpresa() != null) {
                        usuario.getEmpleado().getEmpresa().getNombre();
                    }
                }
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }
}