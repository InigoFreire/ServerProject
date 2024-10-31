/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLogicTier;
import java.util.Scanner;


public class HiloLector {
    
    private boolean closed = false; 

    public HiloLector() {
    }

    public boolean lectorTecladoHilo() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese un comando (escriba 'close' para salir): ");

        while (!closed) { 
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("close")) {
                closed = true;
                System.out.println("Comando 'close' detectado. Saliendo...");
            } else {
                System.out.println("Comando ingresado: " + input);
            }
        }
        
        scanner.close(); 
        return closed; 
    }

   
    public boolean isClosed() {
        return closed;
    }


}

    
    
    

