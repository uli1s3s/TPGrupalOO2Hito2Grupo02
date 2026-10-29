package com.unla.ghsicilianotfi.services;

import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;

public interface IReciboSueldoPDFService {

    /**
     * Genera el recibo de haberes oficial en formato PDF en memoria (byte[]).
     */
    byte[] generarReciboPDF(LiquidacionDeHaber liquidacion);
}

