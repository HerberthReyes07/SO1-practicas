#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/wait.h>
#include <stdbool.h>

// Constantes para gastos operativos mensuales
const double GASTO_SUELDO_EMPLEADO = 85.00;
const double GASTO_ELECTRICIDAD = 40.00;
const double GASTO_AGUA_ESTIMADA = 15.00;
const double GASTO_RENTA_LOCAL = 60.00;

// Dígitos del registro académico (2022 con 0 reemplazado por 1)
// Usados como multiplicadores para calcular gastos
const int DIGITOS[] = {2, 1, 2, 2};

// Imprime el desglose detallado de todos los gastos operativos
void desgloseGastosOperativos(double totalSueldoEmpleados, double totalElectricidad,
                              double totalAgua, double totalRenta, double totalGastosOperativos)
{
    printf("\n========================================\n");
    printf("    DESGLOSE DE GASTOS OPERATIVOS\n");
    printf("========================================\n");
    printf("  Sueldo Empleados: Q.%.2lf x %d = Q.%.2lf\n", GASTO_SUELDO_EMPLEADO, DIGITOS[0], totalSueldoEmpleados);
    printf("  Electricidad:     Q.%.2lf x %d = Q.%.2lf\n", GASTO_ELECTRICIDAD, DIGITOS[1], totalElectricidad);
    printf("  Agua:             Q.%.2lf x %d = Q.%.2lf\n", GASTO_AGUA_ESTIMADA, DIGITOS[2], totalAgua);
    printf("  Renta Local:      Q.%.2lf x %d = Q.%.2lf\n", GASTO_RENTA_LOCAL, DIGITOS[3], totalRenta);
    printf("----------------------------------------\n");
    printf("  TOTAL GASTOS:                Q.%.2lf\n", totalGastosOperativos);
    printf("========================================\n\n");
}

// Calcula el total de gastos operativos multiplicando cada gasto por su dígito correspondiente
// Parámetro imprimirDesglose: si es true, muestra el desglose detallado
double calculoGastosOperativos(bool imprimirDesglose)
{
    // Calcula cada categoría de gasto
    double totalSueldoEmpleados = GASTO_SUELDO_EMPLEADO * DIGITOS[0];
    double totalElectricidad = GASTO_ELECTRICIDAD * DIGITOS[1];
    double totalAgua = GASTO_AGUA_ESTIMADA * DIGITOS[2];
    double totalRenta = GASTO_RENTA_LOCAL * DIGITOS[3];

    // Suma todos los gastos operativos
    double totalGastosOperativos = totalSueldoEmpleados + totalElectricidad + totalAgua + totalRenta;

    // Muestra el desglose si se solicita
    if (imprimirDesglose)
    {
        desgloseGastosOperativos(totalSueldoEmpleados, totalElectricidad, totalAgua, totalRenta, totalGastosOperativos);
    }
    return totalGastosOperativos;
}

// Calcula y muestra las ganancias netas (ganancias - gastos)
// Retorna true si hubo ganancias positivas, false en caso contrario
bool calculoGananciasNetas(double gananciasTotales, double totalGastosOperativos)
{
    double gananciasNetas = gananciasTotales - totalGastosOperativos;

    printf("\n========================================\n");
    printf("    CÁLCULO DE GANANCIAS NETAS\n");
    printf("========================================\n");
    printf("  Ganancias Totales:        Q.%.2lf\n", gananciasTotales);
    printf("  (-) Gastos Operativos:    Q.%.2lf\n", totalGastosOperativos);
    printf("----------------------------------------\n");
    printf("  Ganancias Netas:          Q.%.2lf\n", gananciasNetas);
    printf("========================================\n");

    // Evalúa el resultado y retorna el estado
    if (gananciasNetas > 0)
    {
        printf("\n[RESULTADO] El negocio tuvo GANANCIAS NETAS POSITIVAS.\n\n");
        return true;
    }
    else if (gananciasNetas < 0)
    {
        printf("\n[RESULTADO] El negocio tuvo PERDIDAS NETAS.\n\n");
        return false;
    }
    else
    {
        printf("\n[RESULTADO] El negocio no tuvo ni ganancias ni perdidas (punto de equilibrio).\n\n");
        return false;
    }
}

int main()
{
    printf("\n============================================\n");
    printf("   EJERCICIO 2 - GANANCIAS NETAS DEL DIA\n");
    printf("============================================\n\n");

    double gananciasTotales;
    printf("Ingrese las ganancias totales del dia: Q.");
    scanf("%lf", &gananciasTotales);

    // Valida que las ganancias no sean negativas
    if (gananciasTotales < 0)
    {
        printf("\n[ERROR] La cantidad ingresada no puede ser negativa.\n");
        return EXIT_FAILURE;
    }

    int status;

    printf("\n--- CREANDO PROCESOS HIJO ---\n");

    // Crea dos procesos hijo para realizar los cálculos
    for (int i = 0; i < 2; i++)
    {
        pid_t pid = fork();

        if (pid == 0) // Código del proceso hijo
        {
            printf("\n[HIJO %d] PID: %d | Padre PID: %d\n", i + 1, getpid(), getppid());

            if (i == 0)
            {
                // Hijo 1: Calcula y muestra el desglose de gastos operativos
                printf("[HIJO 1] Calculando gastos operativos...\n");
                calculoGastosOperativos(true);
                return EXIT_SUCCESS;
            }
            else if (i == 1)
            {
                // Hijo 2: Calcula gastos y ganancias netas
                printf("[HIJO 2] Calculando ganancias netas...\n");
                double totalGastosOperativos = calculoGastosOperativos(false);
                bool huboGananciasNetasPositivas = calculoGananciasNetas(gananciasTotales, totalGastosOperativos);

                // Retorna EXIT_SUCCESS si hubo ganancias, EXIT_FAILURE si no
                if (huboGananciasNetasPositivas)
                {
                    return EXIT_SUCCESS; // El hijo termina con éxito si hubo ganancias netas positivas
                }
                return EXIT_FAILURE; // El hijo termina con error si no hubo ganancias netas positivas
            }
        }
        else if (pid < 0)
        {
            // Error al crear el proceso hijo
            perror("[ERROR] No se pudo crear el proceso hijo");
            return EXIT_FAILURE;
        }
    }

    // El proceso padre espera a que terminen los dos hijos
    printf("\n--- PROCESO PADRE ESPERANDO A LOS HIJOS ---\n\n");

    for (int i = 0; i < 2; i++)
    {
        wait(&status);

        // Verifica si el proceso hijo terminó normalmente
        if (WIFEXITED(status))
        {
            int exitStatus = WEXITSTATUS(status);

            if (exitStatus == EXIT_SUCCESS)
            {
                printf("[PADRE] El proceso hijo %d termino exitosamente (EXIT_SUCCESS).\n", i + 1);
                if (i == 1)
                {
                    printf("[PADRE] Interpretacion de HIJO 2: Se obtuvieron ganancias netas positivas.\n");
                }
            }
            else
            {
                printf("[PADRE] El proceso hijo %d termino con error (EXIT_FAILURE).\n", i + 1);
                if (i == 1)
                {
                    printf("[PADRE] Interpretacion de HIJO 2: No se obtuvieron ganancias netas positivas.\n");
                }
            }
        }
        else
        {
            printf("[PADRE] El proceso hijo %d no termino normalmente.\n", i + 1);
        }
    }

    printf("\n[PADRE] Todos los procesos hijo han finalizado.\n");
    printf("============================================\n\n");

    return EXIT_SUCCESS;
}