package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CostoFestivalFormDTO;
import com.unla.ghsicilianotfi.dtos.FestivalFormDTO;
import com.unla.ghsicilianotfi.entities.Costo;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.TipoCostoFestival;
import com.unla.ghsicilianotfi.repositories.IFestivalRepository;
import com.unla.ghsicilianotfi.services.IFestivalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FestivalService implements IFestivalService {

    private final IFestivalRepository festivalRepository;

    public FestivalService(IFestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FestivalFormDTO> listar() {
        return festivalRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FestivalFormDTO> listarActivos() {
        return festivalRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FestivalFormDTO> buscarPorId(Long id) {
        return festivalRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional
    public FestivalFormDTO guardar(FestivalFormDTO datos) {
        validar(datos);

        Festival festival = datos.getIdFestival() == null
                ? new Festival()
                : festivalRepository.findById(datos.getIdFestival())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No existe el festival con ID: " + datos.getIdFestival()));

        festival.setNombre(datos.getNombre().trim());
        festival.setTemporada(datos.getTemporada().trim());
        festival.setFechaInicio(datos.getFechaInicio());
        festival.setFechaFin(datos.getFechaFin());
        festival.setActivo(datos.isActivo());

        Map<TipoCostoFestival, Costo> costosExistentes = festival.getCostos().stream()
                .collect(Collectors.toMap(Costo::getTipoCosto, costo -> costo));
        List<CostoFestivalFormDTO> costosEnviados = datos.getCostos() == null
                ? List.of()
                : datos.getCostos();
        for (CostoFestivalFormDTO costoDTO : costosEnviados) {
            if (!costoDTO.isAplicar()) {
                continue;
            }
            Costo costo = costosExistentes.remove(costoDTO.getTipoCosto());
            if (costo == null) {
                costo = new Costo();
                costo.setFestival(festival);
                festival.getCostos().add(costo);
            }
            costo.setTipoCosto(costoDTO.getTipoCosto());
            costo.setTipoUnidadAplicable(costoDTO.getTipoUnidadAplicable());
            costo.setMonto(costoDTO.getMonto());
        }
        festival.getCostos().removeIf(costo -> costosExistentes.containsValue(costo));

        return toDTO(festivalRepository.save(festival));
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        if (!festivalRepository.existsById(id)) {
            return false;
        }
        festivalRepository.deleteById(id);
        return true;
    }

    private void validar(FestivalFormDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del festival son obligatorios");
        }
        if (datos.getFechaInicio() != null
                && datos.getFechaFin() != null
                && datos.getFechaFin().isBefore(datos.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        Set<TipoCostoFestival> tiposIncluidos = EnumSet.noneOf(TipoCostoFestival.class);
        if (datos.getCostos() == null) {
            return;
        }
        for (CostoFestivalFormDTO costo : datos.getCostos()) {
            if (costo == null || !costo.isAplicar()) {
                continue;
            }
            if (costo.getTipoCosto() == null
                    || !TipoCostoFestival.configurables().contains(costo.getTipoCosto())
                    || costo.getTipoUnidadAplicable() == null
                    || costo.getMonto() == null
                    || costo.getMonto().signum() < 0
                    || costo.getMonto().scale() > 2
                    || costo.getMonto().precision() - costo.getMonto().scale() > 10) {
                throw new IllegalArgumentException("Los datos de cada costo deben ser válidos");
            }
            if (!tiposIncluidos.add(costo.getTipoCosto())) {
                throw new IllegalArgumentException("No se puede repetir un tipo de costo");
            }
        }
    }

    private FestivalFormDTO toDTO(Festival festival) {
        FestivalFormDTO dto = new FestivalFormDTO();
        dto.setIdFestival(festival.getIdFestival());
        dto.setNombre(festival.getNombre());
        dto.setTemporada(festival.getTemporada());
        dto.setFechaInicio(festival.getFechaInicio());
        dto.setFechaFin(festival.getFechaFin());
        dto.setActivo(festival.isActivo());
        dto.setCostos(festival.getCostos().stream()
                .filter(costo -> TipoCostoFestival.configurables().contains(costo.getTipoCosto()))
                .map(costo -> {
                    CostoFestivalFormDTO costoDTO = new CostoFestivalFormDTO();
                    costoDTO.setTipoCosto(costo.getTipoCosto());
                    costoDTO.setTipoUnidadAplicable(costo.getTipoUnidadAplicable());
                    costoDTO.setMonto(costo.getMonto());
                    costoDTO.setAplicar(true);
                    return costoDTO;
                })
                .sorted((a, b) -> a.getTipoCosto().compareTo(b.getTipoCosto()))
                .collect(Collectors.toCollection(ArrayList::new)));
        return dto;
    }
}
