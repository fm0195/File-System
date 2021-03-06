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
    private String path;
    private Date fechaCreacion;
    private Date fechaModificacion;
    private int copias = 0;
    private int size;

    public void setSize(int size) {
        this.size = size;
    }

    public int getCopias() {
        return copias;
    }
    
    public void aumentarCopias(){
        copias++;
    }
    
    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getFechaModificacion() {
        return fechaModificacion.toString();
    }
    
    public String getFechaCreacion() {
        return fechaCreacion.toString();
    }

    public Archivo(String path, String nombre, int size) {
        this.path = path;
        this.nombre = nombre;
        this.fechaCreacion = new Date();
        this.fechaModificacion = new Date();
        this.size = size;
    }
    
    public String getPathCompleto(){
        return this.path + this.nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public int getSize() {
        return size;
    }
    
    
}
