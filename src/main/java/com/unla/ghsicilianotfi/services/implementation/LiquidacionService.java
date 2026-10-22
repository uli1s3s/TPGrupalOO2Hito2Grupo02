package com.unla.ghsicilianotfi.services.implementation;

import com.unla.ghsicilianotfi.configuration.ParametrosLiquidacion;
import com.unla.ghsicilianotfi.dtos.LiquidacionDTO;
import com.unla.ghsicilianotfi.entities.Cajero;
import com.unla.ghsicilianotfi.entities.Cocinero;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.entities.UnidadVenta;
import com.unla.ghsicilianotfi.services.ILiquidacionService;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service("liquidacionService")
public class LiquidacionService implements ILiquidacionService {

    private final ParametrosLiquidacion parametros;

    public LiquidacionService(ParametrosLiquidacion parametros) {
        this.parametros = parametros;
    }

    @Override
    public LiquidacionDTO calcular(Personal empleado, UnidadVenta unidad, int diasFestival) {

        if (diasFestival < 1) {
            throw new IllegalArgumentException("Los días del festival deben ser al menos 1");
        }

        BigDecimal sueldoBase = parametros.getSueldoBase();
        BigDecimal adicional;
        String detalleAdicional;
        String tipo;

        if (empleado instanceof Cocinero cocinero) {
            // Cocina: sueldo base + plus fijo segun su categoria
            tipo = "Cocinero";
            String categoria = (cocinero.getCategoria() != null)
                    ? cocinero.getCategoria().getNombre()
                    : null;
            adicional = plusPorCategoria(categoria);
            detalleAdicional = (adicional.signum() > 0)
                    ? "Plus " + categoria
                    : "Categoría no reconocida: " + categoria;

        } else if (empleado instanceof Cajero) {
            // Cajero: sueldo base + monto por cada año de antiguedad
            tipo = "Cajero";
            long anios = ChronoUnit.YEARS.between(empleado.getFechaIngreso(), LocalDate.now());
            if (anios < 0) {
                anios = 0;
            }
            adicional = parametros.getMontoAntiguedadAnual().multiply(BigDecimal.valueOf(anios));
            detalleAdicional = anios + " año(s) de antigüedad";

        } else {
            throw new IllegalArgumentException("Tipo de empleado no reconocido");
        }

        BigDecimal porcentaje = calcularPorcentaje(diasFestival);

        BigDecimal total = sueldoBase.add(adicional)
                .multiply(porcentaje)
                .setScale(2, RoundingMode.HALF_UP);

        return new LiquidacionDTO(
                empleado.getIdPersonal(),
                empleado.getApellido() + ", " + empleado.getNombre(),
                empleado.getDni(),
                tipo,
                unidad.getNombreComercial(),
                sueldoBase,
                adicional,
                detalleAdicional,
                diasFestival,
                porcentaje.multiply(BigDecimal.valueOf(100)).intValue(),
                total
        );
    }

    // Hasta 15 dias: 50% | de 16 a 30: 100% | mas de 30: (int(dias/15) + 1) * 50%
    @Override
    public BigDecimal calcularPorcentaje(int diasFestival) {

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

    // Plus fijo segun la categoria del cocinero
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