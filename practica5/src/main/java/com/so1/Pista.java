package com.so1;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa una pista del aeropuerto.
 * Cada pista tiene varias secciones que pueden reservarse de forma
 * independiente.
 * Los métodos de reserva/liberación son sincronizados para manejo seguro de
 * hilos.
 *
 * Se usa LinkedHashMap para garantizar que el orden de las secciones sea
 * siempre
 * el mismo que se pasó al constructor. Esto es importante para el escenario sin
 * deadlock, donde el orden consistente de adquisición evita la espera circular.
 */

public class Pista {

    private final String nombre;
    // LinkedHashMap garantiza el orden de inserción (orden fijo de secciones)
    private final Map<SeccionPista, Boolean> secciones = new LinkedHashMap<>();

    public Pista(String nombre, SeccionPista... seccionesPista) {
        this.nombre = nombre;
        for (SeccionPista s : seccionesPista) {
            secciones.put(s, false); // todas libres al inicio
        }
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Intenta reservar una sección específica de esta pista.
     * 
     * @return true si se logró reservar, false si ya estaba ocupada
     */
    public synchronized boolean reservarSeccion(SeccionPista seccion) {
        if (!secciones.containsKey(seccion))
            return false;
        if (secciones.get(seccion))
            return false; // ya ocupada
        secciones.put(seccion, true);
        return true;
    }

    /**
     * Libera una sección específica de la pista y notifica a los hilos
     * que estén esperando que algo cambió (uso correcto de wait/notify).
     */
    public synchronized void liberarSeccion(SeccionPista seccion) {
        if (secciones.containsKey(seccion)) {
            secciones.put(seccion, false);
            notifyAll(); // avisar a todos los hilos que esperan en esta pista
        }
    }

    /**
     * Verifica si una sección está disponible en este momento.
     */
    public synchronized boolean estaDisponible(SeccionPista seccion) {
        return secciones.containsKey(seccion) && !secciones.get(seccion);
    }

    /**
     * Verifica si alguna sección de la pista está ocupada
     * (para saber si se puede asignar a aterrizaje).
     */
    public synchronized boolean tieneSeccionOcupada() {
        for (boolean ocupada : secciones.values()) {
            if (ocupada)
                return true;
        }
        return false;
    }

    /**
     * Devuelve las secciones de esta pista.
     */
    public SeccionPista[] getSecciones() {
        return secciones.keySet().toArray(new SeccionPista[0]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(nombre + " [");
        for (Map.Entry<SeccionPista, Boolean> e : secciones.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue() ? "OCUPADA" : "LIBRE").append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
