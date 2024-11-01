/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccessTier;

/**
 *
 * @author Pebble
 */
public class DAOFactory {
    
    public static DAO getDAO(){
     return new DAO();}
    
}
