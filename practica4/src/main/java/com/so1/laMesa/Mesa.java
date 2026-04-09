package com.so1.laMesa;

import java.util.concurrent.Semaphore;

/**
 * Gestiona el estado de los tenedores y estudiantes en una mesa circular.
 */
public class Mesa {
    // Cada tenedor es un semaforo binario (1 libre, 0 ocupado).
    private final Semaphore[] tenedores;
    private final String[] estadoEstudiantes;
    private final int[] duenioTenedor;

    /**
     * Inicializa tenedores y estado inicial de los estudiantes.
     */
    public Mesa(int totalEstudiantes) {
        tenedores = new Semaphore[totalEstudiantes];
        estadoEstudiantes = new String[totalEstudiantes];
        duenioTenedor = new int[totalEstudiantes];

        for (int i = 0; i < totalEstudiantes; i++) {
            tenedores[i] = new Semaphore(1, true);
            estadoEstudiantes[i] = "estudiando";
            duenioTenedor[i] = -1;
        }
    }

    public int getTenedorIzquierdo(int idEstudiante) {
        return idEstudiante;
    }

    /**
     * El tenedor derecho es el siguiente en la mesa circular.
     */
    public int getTenedorDerecho(int idEstudiante) {
        return (idEstudiante + 1) % tenedores.length;
    }

    /**
     * Flujo clasico: tomar izquierdo y luego derecho (puede producir espera circular).
     */
    public void tomarTenedores(int idEstudiante, int comidaActual, int totalComidas) throws InterruptedException {
        int izquierdo = getTenedorIzquierdo(idEstudiante);
        int derecho = getTenedorDerecho(idEstudiante);

        actualizarEstado(idEstudiante, "esperando tenedores para comer " + comidaActual);

        // Primero bloquea el tenedor izquierdo.
        tenedores[izquierdo].acquire();
        synchronized (this) {
            duenioTenedor[izquierdo] = idEstudiante;
        }
        imprimirEstado("Estudiante " + idEstudiante + " tomo tenedor izquierdo " + izquierdo);

        // Luego intenta el derecho; aqui puede quedar bloqueado si su vecino lo tiene.
        tenedores[derecho].acquire();
        synchronized (this) {
            duenioTenedor[derecho] = idEstudiante;
        }
        imprimirEstado("Estudiante " + idEstudiante + " tomo tenedor derecho " + derecho);

        actualizarEstado(idEstudiante, "comiendo (" + comidaActual + "/" + totalComidas + ")");
    }

    /**
     * Libera ambos tenedores al terminar de comer.
     */
    public void soltarTenedores(int idEstudiante) {
        int izquierdo = getTenedorIzquierdo(idEstudiante);
        int derecho = getTenedorDerecho(idEstudiante);

        synchronized (this) {
            duenioTenedor[izquierdo] = -1;
        }
        tenedores[izquierdo].release();
        imprimirEstado("Estudiante " + idEstudiante + " solto tenedor izquierdo " + izquierdo);

        synchronized (this) {
            duenioTenedor[derecho] = -1;
        }
        tenedores[derecho].release();
        imprimirEstado("Estudiante " + idEstudiante + " solto tenedor derecho " + derecho);
    }

    /**
     * Actualiza y muestra el estado de un estudiante en una seccion sincronizada.
     */
    public synchronized void actualizarEstado(int idEstudiante, String nuevoEstado) {
        estadoEstudiantes[idEstudiante] = nuevoEstado;
        imprimirEstado("Estudiante " + idEstudiante + " -> " + nuevoEstado);
    }

    /**
     * Imprime una fotografia completa de tenedores y estudiantes.
     */
    public synchronized void imprimirEstado(String evento) {
        StringBuilder estadoTenedores = new StringBuilder();
        estadoTenedores.append("Tenedores: ");
        for (int i = 0; i < duenioTenedor.length; i++) {
            if (duenioTenedor[i] == -1) {
                estadoTenedores.append("[T").append(i).append(":Libre] ");
            } else {
                estadoTenedores.append("[T").append(i).append(":E").append(duenioTenedor[i]).append("] ");
            }
        }

        StringBuilder estadoPersonas = new StringBuilder();
        estadoPersonas.append("Estudiantes: ");
        for (int i = 0; i < estadoEstudiantes.length; i++) {
            estadoPersonas.append("[E").append(i).append(":").append(estadoEstudiantes[i]).append("] ");
        }

        System.out.println("\n------------------------------");
        System.out.println("EVENTO MESA: " + evento);
        System.out.println(estadoTenedores.toString());
        System.out.println(estadoPersonas.toString());
        System.out.println("------------------------------");
    }
}
