package com.so1.estacionamientoInteligente;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Configura y ejecuta la simulacion del estacionamiento inteligente.
 */
public class Ejercicio1 {

	private static final int TOTAL_CARROS = 50;
	private static final int TOTAL_CAMIONES = 10;

	/**
	 * Crea los hilos de carros y camiones, los inicia y espera su finalizacion.
	 */
	public void ejecutar() {
		System.out.println("\n--- EJERCICIO 1: ESTACIONAMIENTO INTELIGENTE ---");
		System.out.println("Iniciando simulacion con 50 carros y 10 camiones.");
		System.out.println("Mostrando eventos en tiempo real y guardando tambien en logs/ejercicio1.log.");

		Estacionamiento estacionamiento = new Estacionamiento();
		List<Thread> vehiculos = new ArrayList<Thread>();

		// Primero se agregan los carros definidos por el enunciado.
		for (int i = 1; i <= TOTAL_CARROS; i++) {
			vehiculos.add(new Thread(new Vehiculo("CARRO-" + i, TipoVehiculo.CARRO, estacionamiento)));
		}

		// Luego se agregan los camiones, que ocupan 2 espacios cada uno.
		for (int i = 1; i <= TOTAL_CAMIONES; i++) {
			vehiculos.add(new Thread(new Vehiculo("CAMION-" + i, TipoVehiculo.CAMION, estacionamiento)));
		}

		// Se inicia con una pequena separacion para simular llegadas progresivas.
		for (Thread vehiculo : vehiculos) {
			vehiculo.start();
			try {
				Thread.sleep(80 + new Random().nextInt(150));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		// Espera a que todos los hilos terminen para cerrar el log de forma segura.
		for (Thread vehiculo : vehiculos) {
			try {
				vehiculo.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		estacionamiento.cerrarLogger();
		System.out.println("Simulacion finalizada correctamente.");
		System.out.println("Puede revisar el detalle completo en logs/ejercicio1.log.");
		System.out.println("Semaforos usados: semaforos contadores justos (fair=true) para cupos generales, VIP y limite de camiones.");
	}
}
