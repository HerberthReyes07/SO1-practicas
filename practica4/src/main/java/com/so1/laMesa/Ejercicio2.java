package com.so1.laMesa;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta la ejecucion del problema clasico de la mesa (sin prevencion de deadlock).
 */
public class Ejercicio2 {
    private static final int TOTAL_ESTUDIANTES = 5;
    private static final int VECES_COMER = 3;

    public void ejecutar() {
        System.out.println("\n--- EJERCICIO 2: LA MESA ---");
        System.out.println("Iniciando simulacion con 5 estudiantes y 5 tenedores.");
        System.out.println("Cada estudiante estudiara y comera 3 veces.");
        System.out.println("Modo clasico activado: esta version no incluye mecanismo de prevencion de deadlock.");
        Mesa mesa = new Mesa(TOTAL_ESTUDIANTES);

        List<Thread> hilos = new ArrayList<Thread>();
        // Crea un hilo por estudiante.
        for (int i = 0; i < TOTAL_ESTUDIANTES; i++) {
            Thread hilo = new Thread(new Estudiante(i, mesa, VECES_COMER), "Estudiante-" + i);
            hilos.add(hilo);
            hilo.start();
        }

        // Espera a que todos finalicen; si hay deadlock, este join no concluye.
        for (Thread hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Simulacion finalizada: todos los estudiantes completaron sus 3 comidas.");
        System.out.println("Se usaron semaforos binarios para controlar cada tenedor.");
    }
}
