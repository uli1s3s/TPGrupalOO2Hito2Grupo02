
package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITokenRecuperacionRepository
        extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    List<TokenRecuperacion> findByUsuario_IdUsuarioAndUtilizadoFalse(
            Long idUsuario);
}
