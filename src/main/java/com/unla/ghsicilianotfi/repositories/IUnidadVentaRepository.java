package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.UnidadVenta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {

    // Verifica si ya existe una unidad con ese código
    boolean existsByCodigoUnico(String codigoUnico);

    // Unidades asignadas a un responsable (por su nombre de usuario)
    List<UnidadVenta> findByResponsable_Nombre(String nombreUsuario);

    // Última unidad cargada con ese prefijo (para generar el siguiente código)
    Optional<UnidadVenta> findTopByCodigoUnicoStartingWithOrderByCodigoUnicoDesc(String prefijo);
}