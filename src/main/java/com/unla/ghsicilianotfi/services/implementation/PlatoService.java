package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.PlatoFormDTO;
import com.unla.ghsicilianotfi.dtos.ReporteRentabilidadPlatoDTO;
import com.unla.ghsicilianotfi.entities.Plato;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.repositories.IPlatoRepository;
import com.unla.ghsicilianotfi.repositories.PlatoRentabilidadProjection;
import com.unla.ghsicilianotfi.services.IPlatoService;
import com.unla.ghsicilianotfi.services.IUnidadVentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlatoService implements IPlatoService {

    private final IPlatoRepository platoRepository;
    private final IUnidadVentaService unidadVentaService;

    public PlatoService(IPlatoRepository platoRepository,
                        IUnidadVentaService unidadVentaService) {
        this.platoRepository = platoRepository;
        this.unidadVentaService = unidadVentaService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatoFormDTO> listarPorResponsable(String nombreResponsable) {
        return platoRepository.findByUnidadVenta_Responsable_NombreOrderByNombreAsc(nombreResponsable)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteRentabilidadPlatoDTO> listarRentabilidadPorResponsable(
            String nombreResponsable) {
        return platoRepository.buscarRentabilidadPorResponsable(nombreResponsable)
                .stream()
                .map(this::toReporteRentabilidad)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> listarIdsPlatosEstrellaPorResponsable(String nombreResponsable) {
        Set<Long> idsUnidades = unidadVentaService.listarPorResponsable(nombreResponsable)
                .stream()
                .map(UnidadVenta::getIdUnidad)
                .collect(Collectors.toSet());
        if (idsUnidades.isEmpty()) {
            return Set.of();
        }
        Map<Long, Long> platoEstrellaPorUnidad = new LinkedHashMap<>();
        platoRepository.buscarPlatosMasVendidosPorUnidad(idsUnidades).stream()
                .filter(ranking -> idsUnidades.contains(ranking.getIdUnidad()))
                .forEach(ranking -> platoEstrellaPorUnidad.putIfAbsent(
                        ranking.getIdUnidad(), ranking.getIdPlato()));
        return Set.copyOf(platoEstrellaPorUnidad.values());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlatoFormDTO> buscarPorId(Long id, String nombreResponsable) {
        return platoRepository.findById(id)
                .filter(plato -> perteneceAlResponsable(plato, nombreResponsable))
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public PlatoFormDTO guardar(PlatoFormDTO datos, String nombreResponsable) {
        validar(datos);

        List<UnidadVenta> unidades = unidadVentaService.listarPorResponsable(nombreResponsable);
        UnidadVenta unidad = unidades.stream()
                .filter(item -> item.getIdUnidad().equals(datos.getIdUnidad()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La unidad seleccionada no pertenece a tu usuario"));

        Plato plato;
        if (datos.getIdPlato() == null) {
            plato = new Plato();
        } else {
            plato = platoRepository.findById(datos.getIdPlato())
                    .filter(item -> perteneceAUnidades(item, unidades))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró el plato en tus unidades"));
        }

        plato.setNombre(datos.getNombre().trim());
        plato.setPrecioVenta(datos.getPrecioVenta().doubleValue());
        plato.setCostoProduccion(datos.getCostoProduccion().doubleValue());
        plato.setUnidadVenta(unidad);

        return toDTO(platoRepository.save(plato));
    }

    @Override
    @Transactional
    public boolean cambiarEstado(Long id, String nombreResponsable, boolean activo) {
        Optional<Plato> resultado = platoRepository.findById(id)
                .filter(plato -> perteneceAlResponsable(plato, nombreResponsable));
        if (resultado.isEmpty()) {
            return false;
        }
        resultado.get().setActivo(activo);
        return true;
    }

    private void validar(PlatoFormDTO datos) {
        if (datos == null
                || datos.getNombre() == null
                || datos.getNombre().isBlank()
                || datos.getNombre().trim().length() > 100
                || datos.getPrecioVenta() == null
                || datos.getCostoProduccion() == null
                || datos.getIdUnidad() == null
                || !montoValido(datos.getPrecioVenta())
                || !montoValido(datos.getCostoProduccion())) {
            throw new IllegalArgumentException("Los datos del plato no son válidos");
        }
    }

    private boolean montoValido(BigDecimal monto) {
        return monto.signum() >= 0
                && monto.scale() <= 2
                && monto.precision() - monto.scale() <= 10;
    }

    private boolean perteneceAlResponsable(Plato plato, String nombreResponsable) {
        return plato.getUnidadVenta() != null
                && plato.getUnidadVenta().getResponsable() != null
                && nombreResponsable.equals(plato.getUnidadVenta().getResponsable().getNombre());
    }

    private boolean perteneceAUnidades(Plato plato, List<UnidadVenta> unidades) {
        return plato.getUnidadVenta() != null
                && unidades.stream().anyMatch(unidad ->
                        unidad.getIdUnidad().equals(plato.getUnidadVenta().getIdUnidad()));
    }

    private PlatoFormDTO toDTO(Plato plato) {
        PlatoFormDTO dto = new PlatoFormDTO();
        dto.setIdPlato(plato.getIdPlato());
        dto.setNombre(plato.getNombre());
        dto.setPrecioVenta(BigDecimal.valueOf(plato.getPrecioVenta()));
        dto.setCostoProduccion(BigDecimal.valueOf(plato.getCostoProduccion()));
        dto.setIdUnidad(plato.getUnidadVenta().getIdUnidad());
        dto.setNombreUnidad(plato.getUnidadVenta().getNombreComercial());
        dto.setActivo(plato.isActivo());
        return dto;
    }

    private ReporteRentabilidadPlatoDTO toReporteRentabilidad(
            PlatoRentabilidadProjection plato) {
        BigDecimal precioVenta = BigDecimal.valueOf(plato.getPrecioVenta());
        BigDecimal costoProduccion = BigDecimal.valueOf(plato.getCostoProduccion());
        BigDecimal rentabilidadUnitaria = precioVenta.subtract(costoProduccion);
        BigDecimal margenPorcentaje = precioVenta.signum() == 0
                ? null
                : rentabilidadUnitaria
                        .multiply(BigDecimal.valueOf(100))
                        .divide(precioVenta, 2, RoundingMode.HALF_UP);
        BigDecimal facturacionTotal = BigDecimal.valueOf(plato.getFacturacionTotal());
        BigDecimal costoTotal = BigDecimal.valueOf(plato.getCostoTotal());

        return new ReporteRentabilidadPlatoDTO(
                plato.getIdPlato(),
                plato.getNombre(),
                plato.getNombreUnidad(),
                precioVenta,
                costoProduccion,
                rentabilidadUnitaria,
                margenPorcentaje,
                plato.getCantidadVendida(),
                facturacionTotal,
                facturacionTotal.subtract(costoTotal),
                Boolean.TRUE.equals(plato.getActivo()));
    }
}
