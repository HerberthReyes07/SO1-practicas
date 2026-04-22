package com.so1;

/**
 * Avión en tierra que desea despegar.
 *
 * VERSIÓN SIN DEADLOCK:
 * Para evitar el deadlock, el avión NO retiene el lock de la pista mientras
 * espera las secciones restantes. Suelta y reintenta en lugar de bloquearse.
 * Esto elimina la condición de "retención y espera" de Coffman.
 */

public class AvionTierraSinDeadlock extends Avion {

    public AvionTierraSinDeadlock(String id, Pista[] pistasDisponibles) {
        super(id, pistasDisponibles);
    }

    @Override
    public void run() {
        try {
            // --- ETAPA 1: Posición inicial ---
            SeccionPista seccionInicial = null;
            pistaAsignada = null;

            while (seccionInicial == null) {
                Pista candidata = pistaAleatoria();
                SeccionPista[] secciones = candidata.getSecciones();
                SeccionPista intento = secciones[(int) (Math.random() * secciones.length)];

                if (candidata.reservarSeccion(intento)) {
                    pistaAsignada = candidata;
                    seccionInicial = intento;
                    estado = EstadoAvion.EN_POSICION;
                    mostrarEstado("Etapa 1: posicionado en seccion " + seccionInicial);
                } else {
                    mostrarEstado("Etapa 1: seccion ocupada, reintentando...");
                    esperarAleatorio(300, 600);
                }
            }

            // Simular preparación pre-despegue
            esperarAleatorio(500, 1500);

            // --- ETAPA 2: Carrera de despegue ---
            estado = EstadoAvion.DESPEGANDO;
            boolean pistaCompleta = false;

            while (!pistaCompleta) {
                pistaCompleta = true;
                mostrarEstado("Etapa 2: intentando reservar tramo restante...");

                for (SeccionPista s : pistaAsignada.getSecciones()) {
                    if (s != seccionInicial) {
                        if (!pistaAsignada.reservarSeccion(s)) {
                            // NO bloquea: si falla, suelta todo y reintenta después
                            pistaCompleta = false;
                            mostrarEstado("Seccion " + s + " no disponible, reintentando en breve...");
                            // Liberar las secciones ya tomadas en este intento
                            for (SeccionPista liberada : pistaAsignada.getSecciones()) {
                                if (liberada != seccionInicial) {
                                    pistaAsignada.liberarSeccion(liberada);
                                }
                            }
                            esperarAleatorio(400, 800);
                            break;
                        }
                    }
                }
            }

            mostrarEstado("¡DESPEGÓ exitosamente!");
            estado = EstadoAvion.COMPLETADO;

            // Liberar todas las secciones
            for (SeccionPista s : pistaAsignada.getSecciones()) {
                pistaAsignada.liberarSeccion(s);
            }
            mostrarEstado("Pista liberada.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            mostrarEstado("Hilo interrumpido.");
        }
    }
}
