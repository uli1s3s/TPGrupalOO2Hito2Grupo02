package com.unla.ghsicilianotfi.repositories;

import com.unla.ghsicilianotfi.entities.Cocinero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICocineroRepository extends JpaRepository<Cocinero, Long> {

    Optional<Cocinero> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Cocinero> findByCategoria_IdCategoria(Long idCategoria);

    List<Cocinero> findByActivoTrue();

    @Query("SELECT c FROM Cocinero c WHERE " +
           "(:texto IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "AND (:idCategoria IS NULL OR c.categoria.idCategoria = :idCategoria) " +
           "AND (:activo IS NULL OR c.activo = :activo) " +
           "AND (:sueldoMinimo IS NULL OR c.sueldoBase >= :sueldoMinimo)")
    List<Cocinero> findConFiltros(@Param("texto") String texto,
                                  @Param("idCategoria") Long idCategoria,
                                  @Param("activo") Boolean activo,
                                  @Param("sueldoMinimo") Double sueldoMinimo);
}
