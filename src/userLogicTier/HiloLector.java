/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLogicTier;
import java.util.Scanner;


public class HiloLector {
    
    private boolean closed = false; // Variable para indicar si el hilo debe terminar

    public HiloLector() {
    }

    
    
    // Método que lee la entrada del usuario y devuelve true cuando se introduce "close"
    public boolean lectorTeclado() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese un comando (escriba 'close' para salir): ");

        while (!closed) { // Continua el bucle hasta que se introduzca "close"
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("close")) {
                closed = true;
                System.out.println("Comando 'close' detectado. Saliendo...");
            } else {
                System.out.println("Comando ingresado: " + input);
            }
        }
        
        scanner.close(); // Cierra el Scanner cuando termina el bucle
        return closed; // Devuelve true cuando se introduce "close"
    }

    // Método para obtener el estado de cerrado
    public boolean isClosed() {
        return closed;
    }
}

    
    
    

