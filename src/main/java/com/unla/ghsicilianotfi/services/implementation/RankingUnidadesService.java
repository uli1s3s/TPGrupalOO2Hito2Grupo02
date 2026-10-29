
package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.RankingUnidadDTO;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.repositories.IFestivalRepository;
import com.unla.ghsicilianotfi.repositories.IUnidadVentaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RankingUnidadesService {

    private final IUnidadVentaRepository unidadVentaRepository;
    private final IFestivalRepository festivalRepository;

    public RankingUnidadesService(
            IUnidadVentaRepository unidadVentaRepository,
            IFestivalRepository festivalRepository) {
        this.unidadVentaRepository = unidadVentaRepository;
        this.festivalRepository = festivalRepository;
    }

    public List<RankingUnidadDTO> obtenerRanking(Long idFestival) {

        if (!festivalRepository.existsById(idFestival)) {
            throw new IllegalArgumentException("El festival no existe");
        }

        List<UnidadVenta> unidades =
                unidadVentaRepository.findDistinctByFestivales_IdFestival(idFestival);

        List<RankingUnidadDTO> ranking = new ArrayList<>();

        for (UnidadVenta unidad : unidades) {

            Double recaudacion = unidadVentaRepository.sumarVentas(
                    unidad.getIdUnidad(), idFestival);

            ranking.add(new RankingUnidadDTO(
                    unidad.getIdUnidad(),
                    unidad.getNombreComercial(),
                    unidad.getCodigoUnico(),
                    recaudacion
            ));
        }

        ranking.sort(
                Comparator.comparing(
                        RankingUnidadDTO::getRecaudacion
                ).reversed()
        );

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setPosicion(i + 1);
        }

        return ranking;
    }
}
