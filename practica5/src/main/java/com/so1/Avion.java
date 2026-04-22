package com.so1;

/**
 * Clase base que representa un avión en la simulación.
 * Contiene los atributos y comportamientos comunes para
 * aviones en tierra (despegue) y en el aire (aterrizaje).
 */

public abstract class Avion implements Runnable {

    private static final Object IMPRESION_LOCK = new Object();
    private static int contadorEventos = 0;

    protected final String id;
    protected volatile EstadoAvion estado;
    protected final Pista[] pistasDisponibles;
    protected volatile Pista pistaAsignada;
    protected volatile Avion[] todosLosAviones;

    public Avion(String id, Pista[] pistasDisponibles) {
        this.id = id;
        this.estado = EstadoAvion.ESPERANDO;
        this.pistasDisponibles = pistasDisponibles;
    }

    public String getId() {
        return id;
    }

    public EstadoAvion getEstado() {
        return estado;
    }

    public Pista getPistaAsignada() {
        return pistaAsignada;
    }

    public void setTodosLosAviones(Avion[] todosLosAviones) {
        this.todosLosAviones = todosLosAviones;
    }

    /**
     * Imprime el estado actual del avión de forma clara en consola.
     */
    public void mostrarEstado(String mensaje) {
        synchronized (IMPRESION_LOCK) {
            contadorEventos++;

            String pistaLocal = pistaAsignada != null ? pistaAsignada.getNombre() : "Sin pista";
            String tipo = id.startsWith("Tierra-") ? "TIERRA" : "AIRE";

            System.out.println("\n------------------------------------------------------------");
            System.out.println("EVENTO #" + contadorEventos);
            System.out.println("  Avion  : " + id + " (" + tipo + ")");
            System.out.println("  Accion : " + mensaje);
            System.out.println("  Estado : " + estado);
            System.out.println("  Pista  : " + pistaLocal);
            System.out.println("------------------------------------------------------------");

            if (todosLosAviones != null) {
                System.out.println("RESUMEN GLOBAL:");
                System.out.printf("  %-12s | %-12s | %-22s%n", "Avion", "Estado", "Pista");
                System.out.println("  -----------------------------------------------------------");
                for (Avion avion : todosLosAviones) {
                    if (avion == null) {
                        continue;
                    }
                    Pista pista = avion.getPistaAsignada();
                    String pistaNombre = pista != null ? pista.getNombre() : "Sin pista";
                    System.out.printf("  %-12s | %-12s | %-22s%n",
                            avion.getId(), avion.getEstado(), pistaNombre);
                }
                System.out.println("------------------------------------------------------------");
            }
        }
    }

    /**
     * Espera un tiempo aleatorio entre min y max milisegundos.
     */
    protected void esperarAleatorio(int minMs, int maxMs) throws InterruptedException {
        int tiempo = minMs + (int) (Math.random() * (maxMs - minMs));
        Thread.sleep(tiempo);
    }

    /**
     * Selecciona una pista aleatoria del arreglo de pistas.
     */
    protected Pista pistaAleatoria() {
        return pistasDisponibles[(int) (Math.random() * pistasDisponibles.length)];
    }

    @Override
    public abstract void run();
}
