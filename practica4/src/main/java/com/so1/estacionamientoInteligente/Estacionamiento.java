package com.so1.estacionamientoInteligente;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controla reglas de acceso al estacionamiento usando semaforos.
 */
public class Estacionamiento {
    // 15 cupos generales (20 totales - 5 VIP) y control separado para zona VIP.
    private final Semaphore cuposGenerales = new Semaphore(15, true);
    private final Semaphore cuposVip = new Semaphore(5, true);
    // Limita la cantidad de camiones simultaneos dentro del recinto.
    private final Semaphore limiteCamiones = new Semaphore(3, true);

    // Contadores atomicos para estado actual y estadisticas.
    private final AtomicInteger carrosDentro = new AtomicInteger(0);
    private final AtomicInteger camionesDentro = new AtomicInteger(0);
    private final AtomicInteger carrosEsperando = new AtomicInteger(0);
    private final AtomicInteger camionesEsperando = new AtomicInteger(0);
    private final AtomicInteger desistimientos = new AtomicInteger(0);

    private final Random random = new Random();
    // Encapsula toda la salida a archivo y consola.
    private final ParkingLogger logger;

    /**
     * Inicializa el logger del ejercicio.
     */
    public Estacionamiento() {
        this.logger = new ParkingLogger();
    }

    /**
     * Flujo completo para un vehiculo: esperar cupo, entrar, permanecer y salir.
     */
    public void procesarVehiculo(String nombre, TipoVehiculo tipo) {
        int esperaMaxMs = 2500 + random.nextInt(4500);
        long inicioEspera = System.currentTimeMillis();
        boolean entro = false;
        boolean usoVip = false;
        boolean tomoLimiteCamion = false;
        boolean esperandoContabilizado = false;

        // Se registra como "en espera" antes de intentar adquirir recursos.
        incrementarEsperando(tipo);
        esperandoContabilizado = true;

        try {
            if (tipo == TipoVehiculo.CARRO) {
                // El carro intenta primero en cupo general y, si no puede, intenta VIP.
                entro = intentarEntradaCarro(esperaMaxMs);
                if (!entro) {
                    long transcurrido = System.currentTimeMillis() - inicioEspera;
                    long restante = Math.max(1, esperaMaxMs - transcurrido);
                    entro = cuposVip.tryAcquire(restante, TimeUnit.MILLISECONDS);
                    usoVip = entro;
                }
            } else {
                // El camion requiere permiso de "maximo camiones" y 2 cupos generales.
                entro = intentarEntradaCamion(esperaMaxMs);
                tomoLimiteCamion = entro;
            }

            if (esperandoContabilizado) {
                decrementarEsperando(tipo);
                esperandoContabilizado = false;
            }

            if (!entro) {
                desistimientos.incrementAndGet();
                int libresTotal = cuposGenerales.availablePermits() + cuposVip.availablePermits();
                int libresVip = cuposVip.availablePermits();
                int esperando = carrosEsperando.get() + camionesEsperando.get();
                logger.registrarDesistimiento(
                    nombre,
                    tipo,
                    esperaMaxMs,
                    carrosDentro.get(),
                    camionesDentro.get(),
                    libresTotal,
                    libresVip,
                    esperando,
                    desistimientos.get());
                return;
            }

            if (tipo == TipoVehiculo.CARRO) {
                carrosDentro.incrementAndGet();
            } else {
                camionesDentro.incrementAndGet();
            }

            registrarEvento("ENTRADA", tipo, nombre);

            // Permanencia aleatoria dentro del estacionamiento.
            int tiempoDentroMs = 1000 + random.nextInt(4000);
            Thread.sleep(tiempoDentroMs);

            if (tipo == TipoVehiculo.CARRO) {
                carrosDentro.decrementAndGet();
                if (usoVip) {
                    cuposVip.release();
                } else {
                    cuposGenerales.release();
                }
            } else {
                camionesDentro.decrementAndGet();
                cuposGenerales.release(2);
                if (tomoLimiteCamion) {
                    limiteCamiones.release();
                }
            }

            registrarEvento("SALIDA", tipo, nombre);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (esperandoContabilizado) {
                decrementarEsperando(tipo);
            }
        }
    }

    /**
     * Intenta adquirir un cupo general para carro en un tiempo maximo.
     */
    private boolean intentarEntradaCarro(int esperaMaxMs) throws InterruptedException {
        return cuposGenerales.tryAcquire(esperaMaxMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Para camion: primero controla limite de camiones y luego adquiere 2 cupos.
     */
    private boolean intentarEntradaCamion(int esperaMaxMs) throws InterruptedException {
        long inicio = System.currentTimeMillis();

        if (!limiteCamiones.tryAcquire(esperaMaxMs, TimeUnit.MILLISECONDS)) {
            return false;
        }

        // Se adquieren 2 espacios. Si falla, se revierte el permiso de camion para no
        // bloquear el sistema.
        long transcurrido = System.currentTimeMillis() - inicio;
        long restante = Math.max(1, esperaMaxMs - transcurrido);
        if (!cuposGenerales.tryAcquire(2, restante, TimeUnit.MILLISECONDS)) {
            limiteCamiones.release();
            return false;
        }

        return true;
    }

    /**
     * Incrementa el contador de esperando para el tipo de vehiculo dado.
     */
    private void incrementarEsperando(TipoVehiculo tipo) {
        if (tipo == TipoVehiculo.CARRO) {
            carrosEsperando.incrementAndGet();
        } else {
            camionesEsperando.incrementAndGet();
        }
    }

    /**
     * Decrementa el contador de esperando para el tipo de vehiculo dado.
     */
    private void decrementarEsperando(TipoVehiculo tipo) {
        if (tipo == TipoVehiculo.CARRO) {
            carrosEsperando.decrementAndGet();
        } else {
            camionesEsperando.decrementAndGet();
        }
    }

    /**
     * Registra el estado justo en el evento de entrada o salida.
     */
    private void registrarEvento(String evento, TipoVehiculo tipo, String nombre) {
        int libresTotal = cuposGenerales.availablePermits() + cuposVip.availablePermits();
        int libresVip = cuposVip.availablePermits();
        int esperando = carrosEsperando.get() + camionesEsperando.get();

        logger.registrar(
                evento,
                tipo,
                nombre,
                carrosDentro.get(),
                camionesDentro.get(),
                libresTotal,
                libresVip,
                esperando,
                desistimientos.get());
    }

    public void cerrarLogger() {
        logger.cerrar();
    }
}
