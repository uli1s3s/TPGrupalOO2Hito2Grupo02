package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.CodigoVerificacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("codigoVerificacionRepository")
public interface ICodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    // ultimo codigo generado para un usuario (su nombre de usuario es el email)
    Optional<CodigoVerificacion> findTopByUsuario_NombreOrderByIdCodigoDesc(String nombreUsuario);

    // Codigos todavía sin usar de un usuario (para anularlos al reenviar)
    List<CodigoVerificacion> findByUsuario_IdUsuarioAndUtilizadoFalse(Long idUsuario);
}