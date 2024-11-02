/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverLogicTier;
import dataAccessTier.Pool;
import java.sql.SQLException;
import java.util.Scanner;
/**
 *
 * @author Pebble
 */

public class HiloLector {
    
    private boolean closed = false; 

    public HiloLector() {
    }

    public boolean lectorTecladoHilo() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese un comando (escriba 'close' para salir): ");

        while (!closed) { 
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("close")) {
                closed = true;
                    Pool.close();
        }     
        scanner.close(); 
        return closed; 
    }

   
    public boolean isClosed() {
        boolean closed = false;
        
        return closed;
    }


}

    
    
    

