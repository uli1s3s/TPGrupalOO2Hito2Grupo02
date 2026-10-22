package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.UnidadVenta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {

    // verifica si ya existe una unidad con ese codigo
    boolean existsByCodigoUnico(String codigoUnico);

    // Unidad asingadas por usuario (responsable)
    List<UnidadVenta> findByResponsable_Nombre(String nombreUsuario);
}