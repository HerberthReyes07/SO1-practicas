#include <stdio.h>     // Proporciona funciones de entrada/salida estándar (printf, scanf, etc.)
#include <stdlib.h>    // Proporciona funciones de utilidad general (malloc, free, exit, etc.)
#include <sys/types.h> // Define tipos de datos utilizados por el sistema (pid_t, size_t, etc.)
#include <unistd.h>    // Proporciona acceso a llamadas al sistema POSIX (fork, getpid, sleep, etc.)
#include <sys/wait.h>  // Proporciona el uso de funciones y macros para esperar procesos hijos
#include <stdbool.h>   // Proporciona el tipo de datos booleano (bool, true, false)

void convertirADolares(double quetzales)
{
  double dolares = quetzales / 7.75;
  printf("\n========================================\n");
  printf("   CONVERSION: GTQ -> USD\n");
  printf("========================================\n");
  printf("  Cantidad:        Q.%.2lf\n", quetzales);
  printf("  Tasa cambio:     7.75 GTQ = 1 USD\n");
  printf("  Resultado:       $%.2lf USD\n", dolares);
  printf("========================================\n\n");
}

// Convierte quetzales a pesos mexicanos
// Utiliza una tasa de cambio aproximada de 0.39 GTQ = 1 MXN
void convertirAPesosMexicanos(double quetzales)
{
  double pesosMexicanos = quetzales / 0.39;
  printf("\n========================================\n");
  printf("   CONVERSION: GTQ -> MXN\n");
  printf("========================================\n");
  printf("  Cantidad:        Q.%.2lf\n", quetzales);
  printf("  Tasa cambio:     0.39 GTQ = 1 MXN\n");
  printf("  Resultado:       $%.2lf MXN\n", pesosMexicanos);
  printf("========================================\n\n");
}

// Convierte quetzales a euros
// Utiliza una tasa de cambio aproximada de 8.40 GTQ = 1 EUR
void convertirAEuros(double quetzales)
{
  double euros = quetzales / 8.40;
  printf("\n========================================\n");
  printf("   CONVERSION: GTQ -> EUR\n");
  printf("========================================\n");
  printf("  Cantidad:        Q.%.2lf\n", quetzales);
  printf("  Tasa cambio:     8.40 GTQ = 1 EUR\n");
  printf("  Resultado:       €%.2lf EUR\n", euros);
  printf("========================================\n\n");
}

// Valida que una cadena represente un número válido (entero o decimal)
// Retorna true si es numérico, false en caso contrario
// Permite un máximo de un punto decimal
bool esNumerico(char *str)
{
  int puntos = 0; // Contador de puntos decimales

  for (int i = 0; str[i] != '\0'; i++)
  {
    if (str[i] == '.')
    {
      puntos++;
      if (puntos > 1) // Rechaza múltiples puntos decimales
        return false;
    }
    else if (str[i] < '0' || str[i] > '9') // Verifica que sea dígito
    {
      return false;
    }
  }
  return true;
}

int main()
{

  printf("\n============================================\n");
  printf("   EJERCICIO 1 - CONVERSOR DE MONEDAS\n");
  printf("============================================\n\n");

  char input[50]; // Buffer para almacenar la entrada del usuario
  printf("Ingrese la cantidad de Quetzales (GTQ) para convertir: Q.");
  scanf("%s", input); // Lee la entrada como cadena de caracteres

  // Valida que la entrada sea un valor numérico
  if (!esNumerico(input))
  {
    printf("\nError: la cantidad ingresada no es un valor numérico válido.\n");
    return EXIT_FAILURE; // Termina el programa con un código de error
  }

  // Convierte la cadena a un número decimal
  double quetzales = atof(input);
  printf("\n");

  // Valida que el valor sea positivo
  if (quetzales <= 0)
  {
    printf("Error: La cantidad ingresada debe ser mayor a cero.\n");
    return EXIT_FAILURE; // Termina el programa con un código de error
  }

  // Crea tres procesos hijo, cada uno realiza una conversión diferente
  for (int i = 0; i < 3; i++)
  {
    pid_t pid = fork(); // Crea un nuevo proceso hijo

    if (pid == 0) // Código ejecutado por el proceso hijo
    {
      printf("Soy el hijo %d con PID %d. Mi padre tiene el PID %d\n", i + 1, getpid(), getppid());
      // Ejecuta la conversión según el índice del hijo
      if (i == 0)
      {
        convertirADolares(quetzales);
      }
      else if (i == 1)
      {
        convertirAPesosMexicanos(quetzales);
      }
      else if (i == 2)
      {
        convertirAEuros(quetzales);
      }
      return EXIT_SUCCESS; // El hijo termina con éxito después de realizar su conversión
    }
  }
  // El proceso padre termina después de crear los tres hijos
  return 0;
}