package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.configuration.ParametrosLiquidacion;
import com.unla.ghsicilianotfi.dtos.LiquidacionDTO;
import com.unla.ghsicilianotfi.dtos.LiquidacionDeHaberDTO;
import com.unla.ghsicilianotfi.dtos.ResultadoLiquidacionDTO;
import com.unla.ghsicilianotfi.entities.Cajero;
import com.unla.ghsicilianotfi.entities.Cocinero;
import com.unla.ghsicilianotfi.entities.Festival;
import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.repositories.ILiquidacionDeHaberRepository;
import com.unla.ghsicilianotfi.services.ILiquidacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service("liquidacionService")
public class LiquidacionService implements ILiquidacionService {

    private final ParametrosLiquidacion parametros;
    private final ILiquidacionDeHaberRepository liquidacionDeHaberRepository;

    public LiquidacionService(ParametrosLiquidacion parametros,
                              ILiquidacionDeHaberRepository liquidacionDeHaberRepository) {
        this.parametros = parametros;
        this.liquidacionDeHaberRepository = liquidacionDeHaberRepository;
    }

    /**
     * Calcula los días trabajados tomando en cuenta la fecha de inicio del festival
     * y la fecha de ingreso real del empleado (regla de negocio fundamental).
     */
    @Override
    public int calcularDiasTrabajados(Personal empleado, Festival festival, LocalDate fechaCorte) {
        if (empleado == null) {
            return 0;
        }

        LocalDate corte = (fechaCorte != null) ? fechaCorte : LocalDate.now();

        if (festival == null) {
            // Si no hay festival asociado, se calcula desde el ingreso hasta la fecha de corte
            if (empleado.getFechaIngreso() == null || empleado.getFechaIngreso().isAfter(corte)) {
                return 0;
            }
            return (int) ChronoUnit.DAYS.between(empleado.getFechaIngreso(), corte) + 1;
        }

        // Si el empleado ingresó después del fin del festival, no trabajó ningún día en él
        if (empleado.getFechaIngreso() != null && empleado.getFechaIngreso().isAfter(festival.getFechaFin())) {
            return 0;
        }

        // Fecha de inicio efectiva: la más tardía entre el inicio del festival y el ingreso del empleado
        LocalDate inicioEfectivo = festival.getFechaInicio();
        if (empleado.getFechaIngreso() != null && empleado.getFechaIngreso().isAfter(inicioEfectivo)) {
            inicioEfectivo = empleado.getFechaIngreso();
        }

        // Fecha de fin efectiva: la más temprana entre el fin del festival y la fecha de corte
        LocalDate finEfectivo = festival.getFechaFin();
        if (corte.isBefore(finEfectivo)) {
            finEfectivo = corte;
        }

        if (finEfectivo.isBefore(inicioEfectivo)) {
            return 0;
        }

        return (int) ChronoUnit.DAYS.between(inicioEfectivo, finEfectivo) + 1;
    }

    @Override
    public List<YearMonth> obtenerPeriodosFestival(Festival festival) {
        List<YearMonth> periodos = new ArrayList<>();
        if (festival == null || festival.getFechaInicio() == null || festival.getFechaFin() == null) {
            return periodos;
        }
        YearMonth ym = YearMonth.from(festival.getFechaInicio());
        YearMonth fin = YearMonth.from(festival.getFechaFin());
        while (!ym.isAfter(fin)) {
            periodos.add(ym);
            ym = ym.plusMonths(1);
        }
        return periodos;
    }

    @Override
    public int calcularDiasTrabajadosEnPeriodo(Personal empleado, Festival festival, YearMonth periodo, LocalDate fechaCorte) {
        if (empleado == null || festival == null || periodo == null) {
            return 0;
        }

        LocalDate inicioFest = festival.getFechaInicio();
        LocalDate finFest = festival.getFechaFin();

        LocalDate inicioMes = periodo.atDay(1);
        LocalDate finMes = periodo.atEndOfMonth();

        // Si el mes está totalmente fuera del festival
        if (finMes.isBefore(inicioFest) || inicioMes.isAfter(finFest)) {
            return 0;
        }

        LocalDate inicioEfectivoMes = inicioFest.isAfter(inicioMes) ? inicioFest : inicioMes;
        LocalDate finEfectivoMes = finFest.isBefore(finMes) ? finFest : finMes;

        // Considerar fecha de ingreso del empleado
        LocalDate ingreso = empleado.getFechaIngreso();
        if (ingreso != null) {
            if (ingreso.isAfter(finEfectivoMes)) {
                return 0; // Ingresó después del fin de este período
            }
            if (ingreso.isAfter(inicioEfectivoMes)) {
                inicioEfectivoMes = ingreso;
            }
        }

        // Si el mes o festival está en curso, se computa únicamente hasta la fecha de corte (hoy)
        if (fechaCorte != null && fechaCorte.isBefore(finEfectivoMes)) {
            finEfectivoMes = fechaCorte;
        }

        if (finEfectivoMes.isBefore(inicioEfectivoMes)) {
            return 0;
        }

        return (int) ChronoUnit.DAYS.between(inicioEfectivoMes, finEfectivoMes) + 1;
    }

    @Override
    public LiquidacionDTO calcular(Personal empleado, UnidadVenta unidad, int diasTrabajados) {
        if (diasTrabajados < 0) {
            throw new IllegalArgumentException("Los días trabajados no pueden ser negativos");
        }

        BigDecimal sueldoBase = (empleado.getSueldoBase() > 0)
                ? BigDecimal.valueOf(empleado.getSueldoBase())
                : parametros.getSueldoBase();

        BigDecimal adicional;
        String detalleAdicional;
        String tipo;

        if (empleado instanceof Cocinero cocinero) {
            tipo = "Cocinero";
            String categoria = (cocinero.getCategoria() != null)
                    ? cocinero.getCategoria().getNombre()
                    : null;
            adicional = plusPorCategoria(categoria);
            detalleAdicional = (adicional.signum() > 0)
                    ? "Plus " + categoria
                    : "Categoría no reconocida: " + categoria;

        } else if (empleado instanceof Cajero) {
            tipo = "Cajero";
            long anios = 0;
            if (empleado.getFechaIngreso() != null) {
                anios = ChronoUnit.YEARS.between(empleado.getFechaIngreso(), LocalDate.now());
                if (anios < 0) {
                    anios = 0;
                }
            }
            adicional = parametros.getMontoAntiguedadAnual().multiply(BigDecimal.valueOf(anios));
            detalleAdicional = anios + " año(s) de antigüedad";

        } else {
            throw new IllegalArgumentException("Tipo de empleado no reconocido");
        }

        BigDecimal porcentaje = calcularPorcentaje(diasTrabajados);

        BigDecimal total = sueldoBase.add(adicional)
                .multiply(porcentaje)
                .setScale(2, RoundingMode.HALF_UP);

        String nombreUnidad = (unidad != null) ? unidad.getNombreComercial() : "Sin asignar";

        return new LiquidacionDTO(
                empleado.getIdPersonal(),
                empleado.getApellido() + ", " + empleado.getNombre(),
                empleado.getDni(),
                tipo,
                nombreUnidad,
                sueldoBase,
                adicional,
                detalleAdicional,
                diasTrabajados,
                porcentaje.multiply(BigDecimal.valueOf(100)).intValue(),
                total,
                empleado.getFechaIngreso()
        );
    }

    @Override
    public BigDecimal calcularPorcentaje(int diasFestival) {
        if (diasFestival < 15) {
            return BigDecimal.ZERO;
        }
        int tramo = parametros.getDiasPorTramo();
        BigDecimal porTramo = parametros.getPorcentajePorTramo();

        if (diasFestival <= tramo) {
            return porTramo;
        }
        if (diasFestival <= tramo * 2) {
            return porTramo.multiply(BigDecimal.valueOf(2));
        }
        return porTramo.multiply(BigDecimal.valueOf(diasFestival / tramo + 1));
    }

    @Override
    @Transactional
    public LiquidacionDeHaber liquidarYGuardar(Personal empleado, UnidadVenta unidad, Festival festival,
                                                String periodo, LocalDate fechaCorte) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        if (periodo == null || periodo.trim().isEmpty()) {
            throw new IllegalArgumentException("El período a liquidar es obligatorio");
        }

        LocalDate corte = (fechaCorte != null) ? fechaCorte : LocalDate.now();
        int diasTrabajados = calcularDiasTrabajados(empleado, festival, corte);

        // REGLAS DE NEGOCIO: Bloqueo de liquidación prematura (< 15 días)
        if (festival != null && empleado.getFechaIngreso() != null
                && empleado.getFechaIngreso().isAfter(festival.getFechaFin())) {
            throw new IllegalArgumentException("El empleado no perteneció al festival '" + festival.getNombre()
                    + "' ya que su fecha de ingreso (" + empleado.getFechaIngreso()
                    + ") es posterior a la finalización del mismo (" + festival.getFechaFin() + ").");
        }

        if (diasTrabajados < 15) {
            if (festival != null && festival.getDuracionDias() < 15) {
                throw new IllegalArgumentException("No se puede registrar la liquidación: el festival '" + festival.getNombre()
                        + "' tiene una duración de solo " + festival.getDuracionDias() + " días (el período mínimo liquidable es de 15 días).");
            } else {
                throw new IllegalArgumentException("No se puede registrar la liquidación: el empleado " + empleado.getApellido() + ", " + empleado.getNombre()
                        + " ingresó el " + empleado.getFechaIngreso() + " y cuenta con solo " + diasTrabajados + " días computados (el mínimo es de 15 días).");
            }
        }

        // REGLA DE NEGOCIO: No permitir duplicados en el mismo festival y período
        boolean yaExiste = (festival != null)
                ? liquidacionDeHaberRepository.existsByPersonalAndFestivalAndPeriodo(empleado, festival, periodo.trim())
                : liquidacionDeHaberRepository.existsByPersonalAndPeriodo(empleado, periodo.trim());

        if (yaExiste) {
            String festNombre = (festival != null) ? " en el festival '" + festival.getNombre() + "'" : "";
            throw new IllegalStateException("El empleado " + empleado.getApellido() + ", " + empleado.getNombre()
                    + " ya tiene registrada una liquidación para el período '" + periodo + "'" + festNombre + ".");
        }

        LiquidacionDTO calculo = calcular(empleado, unidad, diasTrabajados);

        LiquidacionDeHaber liquidacion = new LiquidacionDeHaber(
                empleado,
                periodo.trim(),
                corte,
                diasTrabajados,
                calculo.getSueldoBase().doubleValue(),
                calculo.getAdicional().doubleValue(),
                calculo.getDetalleAdicional(),
                calculo.getPorcentaje(),
                calculo.getTotalACobrar().doubleValue(),
                unidad,
                festival
        );

        return liquidacionDeHaberRepository.save(liquidacion);
    }

    @Override
    @Transactional
    public List<LiquidacionDeHaber> liquidarFestivalUnidades(Festival festival, List<UnidadVenta> unidades,
                                                              String periodo, LocalDate fechaCorte) {
        return procesarLiquidacionFestival(festival, unidades, periodo, fechaCorte).getLiquidacionesGeneradas();
    }

    @Override
    @Transactional
    public ResultadoLiquidacionDTO procesarLiquidacionFestival(Festival festival, List<UnidadVenta> unidades,
                                                               String periodoFiltro, LocalDate fechaCorte) {
        int totalEvaluados = 0;
        int yaLiquidados = 0;
        int diasInsuficientes = 0;
        List<LiquidacionDeHaber> guardadas = new ArrayList<>();
        LocalDate corte = (fechaCorte != null) ? fechaCorte : LocalDate.now();

        List<YearMonth> periodosMeses = obtenerPeriodosFestival(festival);
        if (periodoFiltro != null && !periodoFiltro.trim().isEmpty() && !periodoFiltro.equalsIgnoreCase("TODOS")) {
            try {
                YearMonth ymEspecifico = YearMonth.parse(periodoFiltro.trim());
                if (periodosMeses.contains(ymEspecifico)) {
                    periodosMeses = List.of(ymEspecifico);
                }
            } catch (Exception ignored) {
            }
        }

        if (unidades != null && !periodosMeses.isEmpty()) {
            for (YearMonth ym : periodosMeses) {
                String strPeriodo = ym.toString();
                for (UnidadVenta u : unidades) {
                    if (u.getEmpleados() != null) {
                        for (Personal p : u.getEmpleados()) {
                            if (p.isActivo()) {
                                // Si el empleado ingresó después del fin del festival, nunca perteneció a él
                                if (festival != null && p.getFechaIngreso() != null
                                        && p.getFechaIngreso().isAfter(festival.getFechaFin())) {
                                    continue;
                                }

                                int dias = calcularDiasTrabajadosEnPeriodo(p, festival, ym, corte);
                                // Si en este período específico no computó días, no pertenece a este período
                                if (dias == 0) {
                                    continue;
                                }

                                totalEvaluados++;
                                boolean yaExiste = (festival != null)
                                        ? liquidacionDeHaberRepository.existsByPersonalAndFestivalAndPeriodo(p, festival, strPeriodo)
                                        : liquidacionDeHaberRepository.existsByPersonalAndPeriodo(p, strPeriodo);
                                if (yaExiste) {
                                    yaLiquidados++;
                                } else {
                                    if (dias < 15) {
                                        diasInsuficientes++;
                                    } else {
                                        // Tope estándar de 30 días para liquidación de mes completo
                                        int diasCalculo = Math.min(dias, 30);
                                        LiquidacionDTO calculo = calcular(p, u, diasCalculo);
                                        LiquidacionDeHaber liquidacion = new LiquidacionDeHaber(
                                                p,
                                                strPeriodo,
                                                corte,
                                                diasCalculo,
                                                calculo.getSueldoBase().doubleValue(),
                                                calculo.getAdicional().doubleValue(),
                                                calculo.getDetalleAdicional(),
                                                calculo.getPorcentaje(),
                                                calculo.getTotalACobrar().doubleValue(),
                                                u,
                                                festival
                                        );
                                        guardadas.add(liquidacionDeHaberRepository.save(liquidacion));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ResultadoLiquidacionDTO(
                totalEvaluados,
                guardadas.size(),
                yaLiquidados,
                diasInsuficientes,
                guardadas
        );
    }

    @Override
    public List<LiquidacionDeHaberDTO> listarRecibosPorEmpleado(String username) {
        return liquidacionDeHaberRepository
                .findByPersonal_Credenciales_NombreOrderByFechaLiquidacionDesc(username)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiquidacionDeHaberDTO> listarRecibosPorResponsable(String username) {
        return liquidacionDeHaberRepository
                .findByUnidadVenta_Responsable_NombreOrderByFechaLiquidacionDesc(username)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiquidacionDeHaberDTO> listarTodosLosRecibos() {
        return liquidacionDeHaberRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getFechaLiquidacion().compareTo(a.getFechaLiquidacion()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LiquidacionDeHaber buscarPorId(Long idLiquidacion) {
        return liquidacionDeHaberRepository.findById(idLiquidacion)
                .orElseThrow(() -> new IllegalArgumentException("No existe el recibo de sueldo con ID: " + idLiquidacion));
    }

    private LiquidacionDeHaberDTO toDTO(LiquidacionDeHaber entidad) {
        Personal p = entidad.getPersonal();
        String tipo = (p instanceof Cocinero) ? "Cocinero" : (p instanceof Cajero) ? "Cajero" : "Personal";
        String unidad = (entidad.getUnidadVenta() != null) ? entidad.getUnidadVenta().getNombreComercial() : "Sin asignar";
        String fest = (entidad.getFestival() != null) ? entidad.getFestival().getNombre() : "General";

        return new LiquidacionDeHaberDTO(
                entidad.getIdLiquidacion(),
                p.getIdPersonal(),
                p.getApellido() + ", " + p.getNombre(),
                p.getDni(),
                tipo,
                unidad,
                entidad.getPeriodo(),
                entidad.getFechaLiquidacion(),
                entidad.getDiasTrabajados(),
                entidad.getSueldoBase(),
                entidad.getAdicional(),
                entidad.getDetalleAdicional(),
                entidad.getPorcentajeAplicado(),
                entidad.getTotalACobrar(),
                fest
        );
    }

    private BigDecimal plusPorCategoria(String categoria) {
        if (categoria == null) {
            return BigDecimal.ZERO;
        }
        String nombre = categoria.trim().toLowerCase(Locale.ROOT);
        if (nombre.contains("lava")) {
            return parametros.getPlusLavaplatos();
        }
        if (nombre.contains("ayudante")) {
            return parametros.getPlusCocineroAyudante();
        }
        if (nombre.equals("cocinero")) {
            return parametros.getPlusCocinero();
        }
        return BigDecimal.ZERO;
    }
}
