package com.so1;

/**
 * Avión en el aire que desea aterrizar.
 *
 * El avión selecciona una pista aleatoriamente y verifica que no tenga
 * un avión en Etapa 1 (EN_POSICION). Si está disponible, reserva todas
 * las secciones de forma secuencial y aterriza.
 *
 * Existe una versión que puede participar en deadlock (aterriza en orden
 * inverso al de despegue) y una versión segura (mismo orden de secciones).
 */

public class AvionAire extends Avion {

    // Lista de aviones en tierra para verificar si la pista tiene avión en Etapa 1
    private final Avion[] avionesEnTierra;
    private final boolean modoDeadlock;

    public AvionAire(String id, Pista[] pistasDisponibles, Avion[] avionesEnTierra, boolean modoDeadlock) {
        super(id, pistasDisponibles);
        this.avionesEnTierra = avionesEnTierra;
        this.modoDeadlock = modoDeadlock;
    }

    @Override
    public void run() {
        try {
            estado = EstadoAvion.ATERRIZANDO;
            pistaAsignada = null;

            // Buscar una pista disponible (sin avión en Etapa 1)
            while (pistaAsignada == null) {
                Pista candidata = pistaAleatoria();

                if (pistaConAvionEtapa1(candidata)) {
                    mostrarEstado("Pista " + candidata.getNombre() + " tiene avión en Etapa 1, esperando...");
                    esperarAleatorio(400, 800);
                    continue;
                }

                // Intentar reservar todas las secciones de forma secuencial
                SeccionPista[] secciones = candidata.getSecciones();

                // Modo deadlock: orden inverso (puede cruzarse con despegue)
                // Modo seguro: mismo orden siempre
                if (modoDeadlock) {
                    secciones = invertir(secciones);
                }

                boolean exito = reservarTodas(candidata, secciones);

                if (exito) {
                    pistaAsignada = candidata;
                } else {
                    mostrarEstado("No pudo reservar pista completa " + candidata.getNombre() + ", reintentando...");
                    esperarAleatorio(500, 1000);
                }
            }

            mostrarEstado("¡ATERRIZÓ exitosamente en " + pistaAsignada.getNombre() + "!");
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

    /**
     * Verifica si la pista dada tiene un avión en tierra en Etapa 1 (EN_POSICION).
     */
    private boolean pistaConAvionEtapa1(Pista pista) {
        for (Avion a : avionesEnTierra) {
            if (a.getEstado() == EstadoAvion.EN_POSICION && pista.equals(a.getPistaAsignada())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Intenta reservar todas las secciones dadas de la pista.
     * Si alguna falla, libera las ya reservadas y devuelve false.
     */
    private boolean reservarTodas(Pista pista, SeccionPista[] secciones) throws InterruptedException {
        for (int i = 0; i < secciones.length; i++) {
            if (!pista.reservarSeccion(secciones[i])) {
                // Liberar las anteriores
                for (int j = 0; j < i; j++) {
                    pista.liberarSeccion(secciones[j]);
                }
                return false;
            }
            mostrarEstado("Reservada seccion " + secciones[i] + " en " + pista.getNombre());
            esperarAleatorio(100, 300);
        }
        return true;
    }

    /**
     * Invierte el orden de un arreglo de secciones.
     */
    private SeccionPista[] invertir(SeccionPista[] original) {
        SeccionPista[] inv = new SeccionPista[original.length];
        for (int i = 0; i < original.length; i++) {
            inv[i] = original[original.length - 1 - i];
        }
        return inv;
    }
}
