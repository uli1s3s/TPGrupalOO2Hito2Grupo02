package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.dtos.LiquidacionDTO;
import com.unla.ghsicilianotfi.dtos.LiquidacionDeHaberDTO;
import com.unla.ghsicilianotfi.dtos.ResultadoLiquidacionDTO;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface ILiquidacionService {

    // Calcula días trabajados considerando fecha de inicio del festival y fecha de ingreso del empleado
    int calcularDiasTrabajados(Personal empleado, Festival festival, LocalDate fechaCorte);

    // Calcula días trabajados de un empleado en un mes/período específico de un festival
    int calcularDiasTrabajadosEnPeriodo(Personal empleado, Festival festival, YearMonth periodo, LocalDate fechaCorte);

    // Retorna la lista de meses calendario (YearMonth) que abarca un festival
    List<YearMonth> obtenerPeriodosFestival(Festival festival);

    // Calcula monto a cobrar de un empleado según días trabajados
    LiquidacionDTO calcular(Personal empleado, UnidadVenta unidad, int diasTrabajados);

    // Calcula el porcentaje según tramo de días
    BigDecimal calcularPorcentaje(int diasFestival);

    // Genera y persiste una liquidación considerando festival, fecha de ingreso y validaciones de negocio
    LiquidacionDeHaber liquidarYGuardar(Personal empleado, UnidadVenta unidad, Festival festival,
                                        String periodo, LocalDate fechaCorte);

    // Genera y persiste liquidaciones masivas para unidades participantes de un festival
    List<LiquidacionDeHaber> liquidarFestivalUnidades(Festival festival, List<UnidadVenta> unidades,
                                                      String periodo, LocalDate fechaCorte);

    // Procesa la liquidación masiva retornando métricas detalladas (generadas, omitidas, ya existentes)
    ResultadoLiquidacionDTO procesarLiquidacionFestival(Festival festival, List<UnidadVenta> unidades,
                                                         String periodo, LocalDate fechaCorte);

    // Historial de recibos del empleado autenticado
    List<LiquidacionDeHaberDTO> listarRecibosPorEmpleado(String username);

    // Historial de recibos para el Responsable (solo de sus unidades a cargo)
    List<LiquidacionDeHaberDTO> listarRecibosPorResponsable(String username);

    // Todos los recibos persistidos (para Administradores)
    List<LiquidacionDeHaberDTO> listarTodosLosRecibos();

    // Busca un recibo por su ID para descargar el PDF
    LiquidacionDeHaber buscarPorId(Long idLiquidacion);
}
