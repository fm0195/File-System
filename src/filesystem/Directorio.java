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
        this.padre =padre;
        this.path = path + nombre + "\\" ;
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
        Archivo elemento;
        for (int contador = 0; contador < archivos.size(); contador++) {
            elemento = archivos.get(contador);
            if ((elemento.getNombre()).equals(archivo.getNombre())){
                archivos.remove(contador);
            }
        }
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
    
    public Archivo obtenerArchivo(String nombre){
        Archivo resultado;
        for (int contador = 0; contador < archivos.size(); contador++) {
            resultado = archivos.get(contador);
            if (resultado.getNombre().equals(nombre)){
                return resultado;
            }
        }
        throw new IllegalArgumentException("Archivo no existe");
    }
    public void ActualizarFechaModificacion(String nombre){
        Archivo archivo;
        for (int contador = 0; contador < archivos.size(); contador++) {
            archivo = archivos.get(contador);
            if (archivo.getNombre().equals(nombre)){
                archivos.get(contador).setFechaModificacion(new Date());
                return;
            }
        }
        throw new IllegalArgumentException("Archivo no existe");
    }
    public boolean contieneArchivo(String nombre){
        for (int contador = 0; contador < archivos.size(); contador++) {
            Archivo archivo = archivos.get(contador);
            if (archivo.getNombre().equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    
    
    public String tree(int cantidad){
        String resultado="";
        resultado += repeat(" ", cantidad)+ "|->" + this.nombre+"\n";
        for(int contador = 0; contador < subDirectorios.size();contador++){
            resultado += subDirectorios.get(contador).tree(cantidad+2);
        }
        
        for(int contador = 0; contador < archivos.size();contador++){
            resultado +=repeat(" ", cantidad)+ archivos.get(contador).getNombre()+"\n";
        }
        
        return resultado;
    }
    
    private String repeat(String caracter,int cantidad){
        String resultado="";
        for (int contador = 0; contador < cantidad; contador++) {
            resultado+=caracter;
        }
        return resultado;
    }
}
