package com.so1.estacionamientoInteligente;

/**
 * Representa un hilo de vehiculo que delega su logica al estacionamiento.
 */
public class Vehiculo implements Runnable {
    private final String nombre;
    private final TipoVehiculo tipo;
    private final Estacionamiento estacionamiento;

    public Vehiculo(String nombre, TipoVehiculo tipo, Estacionamiento estacionamiento) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.estacionamiento = estacionamiento;
    }

    @Override
    public void run() {
        // Cada hilo notifica al controlador central para aplicar reglas de acceso.
        estacionamiento.procesarVehiculo(nombre, tipo);
    }
}
