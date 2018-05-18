/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.aspera.locapp.dao;

/**
 *
 * @author daniel
 */
public class DatabaseException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 8730754063101789715L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
