/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import java.util.Date;

/**
 *
 * @author fm010
 */
public class Archivo {
    private String nombre;
    private String extension;
    private Date fechaCreacion;
    private Date fechaModificacion;

    public Archivo(String nombre, String extension) {
        this.nombre = nombre;
        this.extension = extension;
        this.fechaCreacion = new Date();
        this.fechaModificacion = new Date();
    }
    
    
}
