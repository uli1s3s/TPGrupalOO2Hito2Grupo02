
package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ICierreCajaRepository
        extends JpaRepository<CierreCaja, Long> {

    // Busca el registro de cierre de una unidad y fecha.
    Optional<CierreCaja> findByUnidadVenta_IdUnidadAndFecha(
            Long idUnidad,
            LocalDate fecha
    );

    // Comprueba si la caja está efectivamente cerrada.
    // Si fue reabierta, devuelve false.
    boolean existsByUnidadVenta_IdUnidadAndFechaAndCerradaTrue(
            Long idUnidad,
            LocalDate fecha
    );
}
