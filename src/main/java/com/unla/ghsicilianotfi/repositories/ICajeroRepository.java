package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Cajero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICajeroRepository extends JpaRepository<Cajero, Long> {

    Optional<Cajero> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Cajero> findByTurnoIgnoreCase(String turno);

    List<Cajero> findByActivoTrue();

    @Query("SELECT c FROM Cajero c WHERE " +
       "(:texto IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
       "AND (:turno IS NULL OR LOWER(c.turno) = LOWER(:turno)) " +
       "AND (:activo IS NULL OR c.activo = :activo) " +
       "AND (:sueldoMinimo IS NULL OR c.sueldoBase >= :sueldoMinimo)")
    List<Cajero> buscarCajeros(@Param("texto") String texto,
                               @Param("turno") String turno,
                               @Param("activo") Boolean activo,
                               @Param("sueldoMinimo") Double sueldoMinimo);
}

