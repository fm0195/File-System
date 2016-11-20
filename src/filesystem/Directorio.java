/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author fm010
 */
public class Directorio {
    private String path;
    private Date fechaCreacion;
    private ArrayList<Directorio> subDirectorios;
    private ArrayList<Archivo> archivos;

    public Directorio(String path) {
        this.path = path;
        this.fechaCreacion = new Date();
        this.subDirectorios = new ArrayList<>();
        this.archivos = new ArrayList<>();
    }
    
    public void añadirSubdirectorio(Directorio subDirectorio){
        subDirectorios.add(subDirectorio);
    }
    
    public void añadirArchivo(Archivo archivo){
        archivos.add(archivo);
    }
    
    public void eliminarSubdirectorio(Directorio subDirectorio){
        subDirectorios.remove(subDirectorio);
    }
    
    public void eliminarArchivo(Archivo archivo){
        archivos.remove(archivo);
    }
}
