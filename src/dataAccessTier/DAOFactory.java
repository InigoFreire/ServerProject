/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccessTier;

import userLogicTier.Signable;

/**
 *
 * @author Pebble
 */
public class DAOFactory {
    
    public static Signable getDAO(){
        return new DAO();
    }
    
}
