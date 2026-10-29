package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("usuarioRepository")
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.nombre = :nombre")
    Optional<Usuario> findByNombreConRol(@Param("nombre") String nombre);

    Optional<Usuario> findByEmailIgnoreCase(String email);

}