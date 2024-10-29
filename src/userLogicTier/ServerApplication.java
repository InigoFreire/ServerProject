/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLogicTier;

/**
 *
 * @author inifr
 */
public class ServerApplication {
    
    private int threadCounter = 0;
    
    public static void main(String[] args) {
        
    }
    
    public sinchronized int threadCounterUpwards(){
        threadCounter++;
    }
    
    public sinchronized int threadCounterDownwards(){
        threadCounter--;
    }
}
