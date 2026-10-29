package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.dtos.CostoFestivalFormDTO;
import com.unla.ghsicilianotfi.dtos.FestivalFormDTO;
import com.unla.ghsicilianotfi.entities.Costo;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.TipoCostoFestival;
import com.unla.ghsicilianotfi.entities.TipoUnidadAplicable;
import com.unla.ghsicilianotfi.repositories.IFestivalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FestivalServiceTest {

    private IFestivalRepository festivalRepository;
    private FestivalService festivalService;

    @BeforeEach
    void setUp() {
        festivalRepository = org.mockito.Mockito.mock(IFestivalRepository.class);
        festivalService = new FestivalService(festivalRepository);
    }

    @Test
    void guardarRechazaFechasInvertidas() {
        FestivalFormDTO datos = festivalValido();
        datos.setFechaInicio(LocalDate.of(2026, 11, 10));
        datos.setFechaFin(LocalDate.of(2026, 11, 9));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> festivalService.guardar(datos));

        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", error.getMessage());
        verifyNoInteractions(festivalRepository);
    }

    @Test
    void guardarPermiteNoConfigurarCostosOpcionales() {
        FestivalFormDTO datos = festivalValido();
        when(festivalRepository.save(any(Festival.class))).thenAnswer(invocation -> {
            Festival guardado = invocation.getArgument(0);
            guardado.setIdFestival(1L);
            return guardado;
        });

        FestivalFormDTO resultado = festivalService.guardar(datos);

        assertEquals(1L, resultado.getIdFestival());
        assertEquals(0, resultado.getCostos().size());
        verify(festivalRepository).save(any(Festival.class));
    }

    @Test
    void guardarPersisteSoloCostosHabilitados() {
        FestivalFormDTO datos = festivalValido();
        CostoFestivalFormDTO costoActivo = datos.getCostos().get(0);
        costoActivo.setAplicar(true);
        costoActivo.setMonto(new BigDecimal("1250.50"));

        when(festivalRepository.save(any(Festival.class))).thenAnswer(invocation -> {
            Festival guardado = invocation.getArgument(0);
            guardado.setIdFestival(1L);
            return guardado;
        });

        FestivalFormDTO resultado = festivalService.guardar(datos);

        assertEquals(1L, resultado.getIdFestival());
        assertEquals(1, resultado.getCostos().size());
        assertEquals(TipoCostoFestival.POR_SUPERFICIE, resultado.getCostos().get(0).getTipoCosto());
        assertEquals(new BigDecimal("1250.50"), resultado.getCostos().get(0).getMonto());
        verify(festivalRepository).save(any(Festival.class));
    }

    @Test
    void guardarRechazaCostoHabilitadoSinMonto() {
        FestivalFormDTO datos = festivalValido();
        datos.getCostos().get(0).setAplicar(true);

        assertThrows(IllegalArgumentException.class, () -> festivalService.guardar(datos));

        verifyNoInteractions(festivalRepository);
    }

    @Test
    void guardarEliminaCostosDeshabilitadosAlEditar() {
        Festival existente = new Festival();
        existente.setIdFestival(5L);
        existente.setNombre("Festival existente");
        existente.setTemporada("2026");
        existente.setFechaInicio(LocalDate.of(2026, 11, 1));
        existente.setFechaFin(LocalDate.of(2026, 11, 7));
        for (TipoCostoFestival tipo : TipoCostoFestival.configurables()) {
            Costo costo = new Costo();
            costo.setFestival(existente);
            costo.setTipoCosto(tipo);
            costo.setTipoUnidadAplicable(TipoUnidadAplicable.TODAS);
            costo.setMonto(BigDecimal.ONE);
            existente.getCostos().add(costo);
        }
        when(festivalRepository.findById(5L)).thenReturn(java.util.Optional.of(existente));
        when(festivalRepository.save(any(Festival.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FestivalFormDTO datos = festivalValido();
        datos.setIdFestival(5L);
        datos.getCostos().get(1).setAplicar(true);
        datos.getCostos().get(1).setMonto(new BigDecimal("50.00"));

        FestivalFormDTO resultado = festivalService.guardar(datos);

        assertEquals(1, resultado.getCostos().size());
        assertEquals(TipoCostoFestival.POR_MONTAJE, resultado.getCostos().get(0).getTipoCosto());
        assertEquals(1, existente.getCostos().size());
    }

    @Test
    void listarActivosDevuelveFestivalesActivosConSusDatos() {
        Festival festival = new Festival();
        festival.setIdFestival(1L);
        festival.setNombre("Festival activo");
        festival.setTemporada("2026");
        festival.setFechaInicio(LocalDate.of(2026, 11, 1));
        festival.setFechaFin(LocalDate.of(2026, 11, 7));
        festival.setActivo(true);
        festival.setCostos(List.of());
        when(festivalRepository.findByActivoTrue()).thenReturn(List.of(festival));

        List<FestivalFormDTO> resultado = festivalService.listarActivos();

        assertEquals(1, resultado.size());
        assertEquals("Festival activo", resultado.get(0).getNombre());
        assertEquals(true, resultado.get(0).isActivo());
        verify(festivalRepository).findByActivoTrue();
    }

    private FestivalFormDTO festivalValido() {
        FestivalFormDTO datos = new FestivalFormDTO();
        datos.setNombre("Festival de prueba");
        datos.setTemporada("2026");
        datos.setFechaInicio(LocalDate.of(2026, 11, 1));
        datos.setFechaFin(LocalDate.of(2026, 11, 7));
        List<CostoFestivalFormDTO> costos = TipoCostoFestival.configurables().stream()
                .map(tipo -> {
                    CostoFestivalFormDTO costo = new CostoFestivalFormDTO();
                    costo.setTipoCosto(tipo);
                    costo.setTipoUnidadAplicable(TipoUnidadAplicable.TODAS);
                    return costo;
                })
                .toList();
        datos.setCostos(new ArrayList<>(costos));
        return datos;
    }
}
