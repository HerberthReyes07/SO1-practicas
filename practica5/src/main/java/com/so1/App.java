package com.so1;

import java.util.*;

/**
 * Clase principal de la simulación del aeropuerto.
 *
 * Ejecuta dos escenarios:
 * Escenario 1: Con deadlock (synchronized mal usado → sistema se congela)
 * Escenario 2: Sin deadlock (synchronized correcto → todo termina bien)
 *
 * El usuario ingresa la cantidad de aviones al inicio.
 */

public class App {

    private static final String LINEA = "============================================================";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        imprimirCabecera();

        int totalAviones = leerEntero(scanner, "Ingrese el número total de aviones a simular: ", 1, 20);
        int enTierra;
        int enAire;

        while (true) {
            enTierra = leerEntero(scanner, "¿Cuántos aviones están en tierra (despegando)? ", 1, totalAviones - 1);
            enAire = leerEntero(scanner, "¿Cuántos aviones están en aire (aterrizando)? ", 1, totalAviones - 1);

            if (enTierra + enAire == totalAviones) {
                break;
            }

            System.out.println("  Error: tierra + aire debe ser igual a " + totalAviones + ". Intente de nuevo.");
        }

        System.out.println("\nConfiguracion elegida:");
        System.out.println("  Total de aviones : " + totalAviones);
        System.out.println("  Aviones en tierra: " + enTierra);
        System.out.println("  Aviones en aire  : " + enAire);

        // -----------------------------------------------------------
        // ESCENARIO 1: CON DEADLOCK
        // -----------------------------------------------------------
        imprimirEscenario("ESCENARIO 1: CON DEADLOCK", "synchronized mal usado - puede congelarse");

        ejecutarSimulacion(enTierra, enAire, true);

        // Pequeña pausa entre escenarios
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        // -----------------------------------------------------------
        // ESCENARIO 2: SIN DEADLOCK
        // -----------------------------------------------------------
        imprimirEscenario("ESCENARIO 2: SIN DEADLOCK", "synchronized correcto - todo termina bien");

        ejecutarSimulacion(enTierra, enAire, false);

        scanner.close();
    }

    /**
     * Arma y ejecuta la simulación según el modo indicado.
     * 
     * @param modoDeadlock true = escenario con deadlock, false = escenario seguro
     */
    private static void ejecutarSimulacion(int cantTierra, int cantAire, boolean modoDeadlock) {

        // Crear las dos pistas del aeropuerto
        // Pista 1: Norte-Sur → secciones ARRIBA, GENERAL, ABAJO
        // Pista 2: Este-Oeste → secciones IZQUIERDA, GENERAL, DERECHA
        Pista pista1 = new Pista("Pista1-NorteSur",
                SeccionPista.ARRIBA, SeccionPista.GENERAL, SeccionPista.ABAJO);
        Pista pista2 = new Pista("Pista2-EsteOeste",
                SeccionPista.IZQUIERDA, SeccionPista.GENERAL, SeccionPista.DERECHA);

        Pista[] pistas = { pista1, pista2 };

        // Crear aviones en tierra
        Avion[] avionesEnTierra = new Avion[cantTierra];
        for (int i = 0; i < cantTierra; i++) {
            String id = "Tierra-" + (i + 1);
            if (modoDeadlock) {
                avionesEnTierra[i] = new AvionTierraDeadlock(id, pistas);
            } else {
                avionesEnTierra[i] = new AvionTierraSinDeadlock(id, pistas);
            }
        }

        // Crear aviones en el aire (necesitan referencia a aviones en tierra para
        // verificar Etapa 1)
        Avion[] avionesEnAire = new Avion[cantAire];
        for (int i = 0; i < cantAire; i++) {
            String id = "Aire-" + (i + 1);
            avionesEnAire[i] = new AvionAire(id, pistas, avionesEnTierra, modoDeadlock);
        }

        Avion[] todosLosAviones = new Avion[cantTierra + cantAire];
        int pos = 0;
        for (Avion a : avionesEnTierra) {
            todosLosAviones[pos++] = a;
        }
        for (Avion a : avionesEnAire) {
            todosLosAviones[pos++] = a;
        }
        for (Avion a : todosLosAviones) {
            a.setTodosLosAviones(todosLosAviones);
        }

        // Lanzar todos los hilos
        Thread[] hilos = new Thread[cantTierra + cantAire];
        int idx = 0;

        for (Avion a : avionesEnTierra) {
            hilos[idx++] = new Thread(a, a.getId());
        }
        for (Avion a : avionesEnAire) {
            hilos[idx++] = new Thread(a, a.getId());
        }

        for (Thread t : hilos) {
            t.start();
        }

        // Esperar a que terminen (con timeout para detectar deadlock)
        long limite = modoDeadlock ? 8000 : 30000; // 8s para deadlock, 30s para normal
        long inicio = System.currentTimeMillis();

        for (Thread t : hilos) {
            try {
                long restante = limite - (System.currentTimeMillis() - inicio);
                if (restante > 0) {
                    t.join(restante);
                }
            } catch (InterruptedException ignored) {
            }
        }

        // Verificar si quedó algún hilo bloqueado
        boolean hayDeadlock = false;
        for (Thread t : hilos) {
            if (t.isAlive()) {
                hayDeadlock = true;
                t.interrupt(); // interrumpir para limpiar
            }
        }

        if (hayDeadlock) {
            System.out.println("\n" + LINEA);
            System.out.println("RESULTADO: DEADLOCK DETECTADO (sistema congelado)");
            System.out.println("Condiciones de Coffman presentes:");
            System.out.println("  1) Exclusion mutua: secciones de uso exclusivo");
            System.out.println("  2) Retencion y espera: avion retiene seccion y espera otra");
            System.out.println("  3) No apropiacion: nadie quita la seccion a otro avion");
            System.out.println("  4) Espera circular: A espera lo que tiene B, B espera lo que tiene A");
            System.out.println(LINEA);
        } else {
            System.out.println("\n" + LINEA);
            System.out.println("RESULTADO: simulacion finalizada sin deadlock");
            System.out.println("Todos los aviones completaron su operacion correctamente.");
            System.out.println(LINEA);
        }
    }

    private static void imprimirCabecera() {
        System.out.println(LINEA);
        System.out.println("SIMULADOR DE AEROPUERTO - Practica No.5 Interbloqueo");
        System.out.println(LINEA + "\n");
    }

    private static void imprimirEscenario(String titulo, String subtitulo) {
        System.out.println("\n" + LINEA);
        System.out.println(titulo);
        System.out.println("(" + subtitulo + ")");
        System.out.println(LINEA + "\n");
    }

    /**
     * Lee un entero del usuario con validación de rango.
     */
    private static int leerEntero(Scanner sc, String mensaje, int min, int max) {
        int valor = -1;
        while (valor < min || valor > max) {
            System.out.print(mensaje);
            try {
                valor = Integer.parseInt(sc.nextLine().trim());
                if (valor < min || valor > max) {
                    System.out.println("  Error: ingrese un valor entre " + min + " y " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("  Error: ingrese solo números enteros.");
            }
        }
        return valor;
    }
}
