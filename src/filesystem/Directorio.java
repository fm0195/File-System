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
    private String nombre;
    private Date fechaCreacion;
    private Directorio padre;
    private ArrayList<Directorio> subDirectorios;
    private ArrayList<Archivo> archivos;

    public Directorio(String path, String nombre, Directorio padre) {
        this.nombre = nombre;
        this.path = path + "/" + nombre;
        this.fechaCreacion = new Date();
        this.subDirectorios = new ArrayList<>();
        this.archivos = new ArrayList<>();
    }
    
    public void añadirSubdirectorio(Directorio subDirectorio){
        if (!this.existeDirectorio(subDirectorio.nombre)) {
            subDirectorios.add(subDirectorio);
        } else {
            throw new IllegalArgumentException("Directorio ya existe");
        }
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

    public String getPath() {
        return path;
    }
    
    private boolean existeDirectorio(String nombre){
        for (int i = 0; i < subDirectorios.size(); i++) {
            if (subDirectorios.get(i).nombre.equals(nombre)) {
                return true;
            }
        }
        return false;
    }
    
    public Directorio obtenerDirectorio(String nombre){
        for (int i = 0; i < subDirectorios.size(); i++) {
            if (subDirectorios.get(i).nombre.equals(nombre)) {
                return subDirectorios.get(i);
            }
        }
        throw new IllegalArgumentException("Directorio no existe");
    }

    public Directorio getPadre() {
        return padre;
    }
    
    public void imprimirContenido(){
        for (int i = 0; i < subDirectorios.size(); i++) {
            Directorio dir = subDirectorios.get(i);
            System.out.println(dir.getNombre()+" | Directorio");
        }
        
        for (int i = 0; i < archivos.size(); i++) {
            Archivo archivo = archivos.get(i);
            System.out.println(archivo.getNombre()+" | Archivo");
        }
    }

    public String getNombre() {
        return nombre;
    }
    
    
}
