#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

// Estructura para representar un cajero con sus atributos
typedef struct
{
    char nombre[30]; // Nombre del cajero
    double cantidadTransaccion; // Cantidad que se deposita o retira en cada transacción
    int tiempoTransaccion; // Tiempo en microsegundos que tarda cada transacción (simulación de procesamiento)
} Cajero;

// Variable global compartida que representa el saldo de la cuenta
double saldo = 100;

// Configuración de la simulación
const int NUMERO_TRANSACCIONES = 20;
const double CANTIDADES_TRANSACCIONES[] = {3, 2, 2, 3}; // Dígitos (5-8) del registro académico (3023 con 0 reemplazado por 2)
const int TIEMPOS_TRANSACCION[] = {500000, 750000, 1000000, 1500000}; // Tiempos en microsegundos (0.5s, 0.75s, 1s, 1.5s)

// Función que simula depósitos en la cuenta (condición de carrera presente)
void *depositar(void *arg)
{
    Cajero* cajero = (Cajero*)arg;

    for (int i = 0; i < NUMERO_TRANSACCIONES; i++)
    {
        double temp = saldo; // Leer el saldo actual
        temp += cajero->cantidadTransaccion; // Calcular el nuevo saldo
        usleep(cajero->tiempoTransaccion); // Simular tiempo de procesamiento
        saldo = temp; // Actualizar el saldo con el nuevo valor

        printf("  [Transaccion #%02d] %s deposito  Q.%.2f | Saldo: Q.%.2f\n", i + 1, cajero->nombre, cajero->cantidadTransaccion, saldo);
    }
    return NULL;
}

// Función que simula retiros de la cuenta (condición de carrera presente)
void *retirar(void *arg)
{
    Cajero* cajero = (Cajero*)arg;

    for (int i = 0; i < NUMERO_TRANSACCIONES; i++)
    {
        double temp = saldo; // Leer el saldo actual
        if (temp < cajero->cantidadTransaccion)
        {
            printf("  [Transaccion #%02d] %s RECHAZADO  Q.%.2f | Saldo insuficiente\n", i + 1, cajero->nombre, cajero->cantidadTransaccion);
            usleep(cajero->tiempoTransaccion);
            continue;
        }
        temp -= cajero->cantidadTransaccion; // Calcular el nuevo saldo
        usleep(cajero->tiempoTransaccion); // Simular tiempo de procesamiento
        saldo = temp; // Actualizar el saldo

        printf("  [Transaccion #%02d] %s retiro   Q.%.2f | Saldo: Q.%.2f\n", i + 1, cajero->nombre, cajero->cantidadTransaccion, saldo);
    }
    return NULL;
}

int main()
{
    printf("\n========================================\n");
    printf("   SIMULADOR DE TRANSACCIONES BANCARIAS\n");
    printf("========================================\n\n");

    // Configuración inicial de cajeros
    Cajero cajeros[4] = {
        {"Cajero 1", CANTIDADES_TRANSACCIONES[0], TIEMPOS_TRANSACCION[0]},
        {"Cajero 2", CANTIDADES_TRANSACCIONES[1], TIEMPOS_TRANSACCION[1]},
        {"Cajero 3", CANTIDADES_TRANSACCIONES[2], TIEMPOS_TRANSACCION[2]},
        {"Cajero 4", CANTIDADES_TRANSACCIONES[3], TIEMPOS_TRANSACCION[3]}};

    printf("CONFIGURACION INICIAL:\n");
    printf("----------------------------------------\n");
    printf("  Saldo Inicial:        Q.%.2f\n", saldo);
    printf("  Numero Transacciones: %d por cajero\n", NUMERO_TRANSACCIONES);
    printf("\n  Cajero 1: Deposita Q.%.2f (Tiempo: %.2f s)\n", cajeros[0].cantidadTransaccion, (cajeros[0].tiempoTransaccion / 1000000.0));
    printf("  Cajero 2: Deposita Q.%.2f (Tiempo: %.2f s)\n", cajeros[1].cantidadTransaccion, (cajeros[1].tiempoTransaccion / 1000000.0));
    printf("  Cajero 3: Retira   Q.%.2f (Tiempo: %.2f s)\n", cajeros[2].cantidadTransaccion, (cajeros[2].tiempoTransaccion / 1000000.0));
    printf("  Cajero 4: Retira   Q.%.2f (Tiempo: %.2f s)\n", cajeros[3].cantidadTransaccion, (cajeros[3].tiempoTransaccion / 1000000.0));
    printf("\n========================================\n\n");

    printf("Iniciando simulacion...\n\n");

    // Crear hilos para cada cajero
    pthread_t cajero1, cajero2, cajero3, cajero4;

    // Iniciar hilos concurrentes
    pthread_create(&cajero1, NULL, depositar, &cajeros[0]);
    pthread_create(&cajero2, NULL, depositar, &cajeros[1]);
    pthread_create(&cajero3, NULL, retirar, &cajeros[2]);
    pthread_create(&cajero4, NULL, retirar, &cajeros[3]);

    // Esperar a que todos los cajeros terminen
    pthread_join(cajero1, NULL);
    pthread_join(cajero2, NULL);
    pthread_join(cajero3, NULL);
    pthread_join(cajero4, NULL);

    // Mostrar resumen final
    printf("\n========================================\n");
    printf("           RESUMEN FINAL\n");
    printf("========================================\n");
    printf("  Saldo Inicial:        Q.%.2f\n", 100.0);
    printf("  Depositos Esperados:  + Q.%.2f\n", (CANTIDADES_TRANSACCIONES[0] + CANTIDADES_TRANSACCIONES[1]) * NUMERO_TRANSACCIONES);
    printf("  Retiros Esperados:    - Q.%.2f\n", (CANTIDADES_TRANSACCIONES[2] + CANTIDADES_TRANSACCIONES[3]) * NUMERO_TRANSACCIONES);
    printf("  Saldo Esperado:       = Q.%.2f\n", 100.0 + (CANTIDADES_TRANSACCIONES[0] + CANTIDADES_TRANSACCIONES[1] - CANTIDADES_TRANSACCIONES[2] - CANTIDADES_TRANSACCIONES[3]) * NUMERO_TRANSACCIONES);
    printf("----------------------------------------\n");
    printf("  Saldo Final Real:     Q.%.2f\n", saldo);
    printf("========================================\n\n");

    return 0;
}