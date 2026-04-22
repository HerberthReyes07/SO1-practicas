package com.so1;

/**
 * Avión en tierra que desea despegar.
 *
 * VERSIÓN CON DEADLOCK:
 * El deadlock ocurre porque el avión agarra una sección de forma sincronizada
 * en la pista completa, y luego intenta agarrar otra sección de la misma pista.
 * Si dos aviones en pistas distintas esperan secciones cruzadas, se bloquean
 * mutuamente.
 *
 * Condiciones de Coffman presentes:
 * 1. Exclusión mutua: cada sección solo puede usarla un avión a la vez.
 * 2. Retención y espera: el avión retiene su sección inicial y espera la
 *    segunda.
 * 3. No apropiación: nadie le quita la sección al avión que ya la tiene.
 * 4. Espera circular: Avion A espera sección de B, y B espera sección de A.
 */

public class AvionTierraDeadlock extends Avion {

    public AvionTierraDeadlock(String id, Pista[] pistasDisponibles) {
        super(id, pistasDisponibles);
    }

    @Override
    public void run() {
        try {
            // --- ETAPA 1: Posición inicial ---
            SeccionPista seccionInicial = null;
            pistaAsignada = null;

            // Intentar conseguir una sección inicial aleatoria
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
                    mostrarEstado("Etapa 1: seccion " + intento + " ocupada, reintentando...");
                    esperarAleatorio(300, 700);
                }
            }

            // Simular preparación pre-despegue
            esperarAleatorio(500, 1500);

            // --- ETAPA 2: Carrera de despegue ---
            // PUNTO DE DEADLOCK: adquiere el lock de la pista completa para reservar
            // las secciones restantes. Si otro avión ya tiene el lock esperando
            // la seccion que este tiene, se forma deadlock.
            estado = EstadoAvion.DESPEGANDO;
            mostrarEstado("Etapa 2: intentando reservar tramo completo para despegue...");

            synchronized (pistaAsignada) { // <-- lock en el objeto pista completo
                for (SeccionPista s : pistaAsignada.getSecciones()) {
                    if (s != seccionInicial) {
                        // Retiene el lock de la pista mientras espera otra sección.
                        // Si otro avión ya tiene esa sección y también espera la nuestra
                        // dentro de este mismo bloque synchronized → DEADLOCK DURO:
                        // ningún hilo puede entrar a liberar porque el lock está tomado.
                        while (!pistaAsignada.reservarSeccion(s)) {
                            mostrarEstado("Esperando seccion " + s + " (bloqueado)...");
                            pistaAsignada.wait(); // sin timeout: nunca despierta solo
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
