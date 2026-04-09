package com.so1.laMesa;

import java.util.Random;

/**
 * Hilo que representa a un estudiante alternando estudio y comida.
 */
public class Estudiante implements Runnable {
    private final int id;
    private final Mesa mesa;
    private final int vecesComer;
    private final Random random;

    public Estudiante(int id, Mesa mesa, int vecesComer) {
        this.id = id;
        this.mesa = mesa;
        this.vecesComer = vecesComer;
        this.random = new Random();
    }

    /**
     * Ejecuta la secuencia del estudiante durante la cantidad indicada de comidas.
     */
    @Override
    public void run() {
        try {
            // Cada ciclo representa: estudiar, tomar tenedores, comer y soltarlos.
            for (int i = 1; i <= vecesComer; i++) {
                // Tiempo de estudio aleatorio antes de intentar comer.
                mesa.actualizarEstado(id, "estudiando");
                Thread.sleep(randomEntre(500, 1500));

                mesa.tomarTenedores(id, i, vecesComer);
                // Tiempo de comida aleatorio.
                Thread.sleep(randomEntre(1000, 2000));
                mesa.soltarTenedores(id);
            }

            mesa.actualizarEstado(id, "retirado");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Genera un valor aleatorio entre min y max (inclusive).
     */
    private int randomEntre(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
