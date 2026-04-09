package com.so1;

import java.util.Scanner;

import com.so1.estacionamientoInteligente.Ejercicio1;
import com.so1.laMesa.Ejercicio2;

/**
 * Punto de entrada de la practica.
 * Muestra un menu simple para ejecutar cualquiera de los dos ejercicios.
 */
public class App {
    /**
     * Bucle principal del menu.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            System.out.println("\n====================================");
            System.out.println("PRACTICA 4 - PROGRAMACION CONCURRENTE");
            System.out.println("====================================");
            System.out.println("1. Ejercicio 1 - Estacionamiento Inteligente");
            System.out.println("2. Ejercicio 2 - La Mesa");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opcion: ");

            String opcion = scanner.nextLine().trim();

            // Se delega cada opcion a su clase de ejercicio para mantener App limpio.
            switch (opcion) {
                case "1":
                    System.out.println("\nEjecutando Ejercicio 1...");
                    new Ejercicio1().ejecutar();
                    break;
                case "2":
                    System.out.println("\nEjecutando Ejercicio 2...");
                    new Ejercicio2().ejecutar();
                    break;
                case "3":
                    continuar = false;
                    System.out.println("Finalizando programa.");
                    break;
                default:
                    System.out.println("Opcion invalida. Ingrese 1, 2 o 3.");
                    break;
            }
        }

        scanner.close();
    }
}
