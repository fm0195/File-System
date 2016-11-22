/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author fm010
 */
public class FileSystem {

    private HashMap<String, int[]> tabla; //Hash. Key=ruta, Valor=lista de sectores
    private int cantSectores;
    private int tamañoSectores;
    private ArrayList<Integer> sectoresDisponibles;
    RandomAccessFile archivo;
    private Directorio root;
    private Directorio dirActual;
    
    public void start() {
            Scanner scanner = new Scanner(System.in);
            root = new Directorio("", "root",null);
            dirActual = root;
            String command;
            String input;
            String[] subString;
            while(true) {
                try{
                    System.out.print("\n"+ dirActual.getPath()+" >");
                    input = scanner.nextLine();
                    subString = input.split("\\s+");
                    command = subString[0];
                    switch(command){
                        case "create":
                            create(subString[1], subString[2]);
                            break;
                        case "file":
                            String contenido = obtenerString(input);
                            subString = input.split("\"(.*?)\"");
                            file(contenido, subString[1].replaceAll("\\s+",""));
                            break;
                        case "mkdir":
                            mkdir(subString[1]);
                            break;
                        case "cd":
                            cd(subString[1]);
                            break;
                        case "ls":
                            ls();
                            break;
                        case "mod":
                            if (subString.length == 3){
                                String valor = obtenerString(input);
                                mod(subString[2],valor);
                            }else{
                                System.out.println("El comando mod no es válido con la cantidad de parámetros ingresados.");
                            }
                            break;
                        case "con":
                            if (subString.length == 2){
                                con(subString[1]);
                            }else{
                                System.out.println("El comando con no es válido con la cantidad de parámetros ingresados.");
                            }
                            break;
                        case "prop":
                            if (subString.length == 2){
                                prop(subString[1]);
                            }else{
                                System.out.println("El comando prop no es válido con la cantidad de parámetros ingresados.");
                            }
                            break;
                        case "cp":
                            if (subString.length == 3){
                                cp(subString[1],subString[2]);
                            }else{
                                System.out.println("El comando prop no es válido con la cantidad de parámetros ingresados.");
                            }
                            break;
                        case "mov":
                            break;
                        case "find":
                            break;
                        case "tree":
                            if(subString.length==1){
                                System.out.println(root.tree(0));
                            }else{
                                System.out.println("El comando tree no es válido con la cantidad de parámetros ingresados.");
                            }
                            break;
                        case "rm":
                            if(subString.length==2){
                                rm(subString[1]);
                            }else{
                                System.out.println("El comando rm no es válido con la cantidad de parámetros ingresados.");
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Comando inicial invalido");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("\nNumero ilegal de argumentos.\n");
                continue;
            }catch(IllegalArgumentException e){
                System.out.println(e.getMessage());
                continue;
            }   catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
        }
    }
    
    private void create(String sectores, String tamaño){
        try {
            this.cantSectores = Integer.parseInt(sectores);
            this.tamañoSectores =  Integer.parseInt(tamaño);
            this.tabla = new HashMap<>();
            sectoresDisponibles = new ArrayList<>(cantSectores);
            for (int i = 0; i < this.cantSectores; i++) {
                sectoresDisponibles.add(i);
            }
            inicializarArchivo(cantSectores * tamañoSectores);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato erróneo de numero");
        } catch (IOException ex) {
            System.out.println("Error creando el archivo.");
        }
    }

    private void inicializarArchivo(int tamaño) throws FileNotFoundException, IOException {
        this.archivo = new RandomAccessFile("disk.txt", "rw");
        this.archivo.setLength(0);
        this.archivo.setLength(tamaño);
        this.archivo.seek(0);
        byte[] b = new byte[tamaño];
        for (int i = 0; i < tamaño; i++) {
            b[i] = 0x20;
        }
        this.archivo.write(b);
    }

    private void file(String contenido, String nombre) throws IOException {
        if (nombre.matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+")) {
            //obtener una lista de N strings, cada uno de tamaño maximo
            //tamañoSectores
            String[] stringSectores = split(contenido, this.tamañoSectores);
            //revisar si hay sectores disponibles
            if (haySectoresDisponibles(stringSectores.length)) {
                //crear archivo 
                Archivo nuevo = new Archivo(dirActual.getPath(),nombre);
                if (!tabla.containsKey(nuevo.getPathCompleto())) {//debe no existir
                    //asignar sectores
                    int[] sectoresAsignados = obtenerSectores(stringSectores.length);
                    tabla.put(nuevo.getPathCompleto(), sectoresAsignados);
                    //escribir en archivo
                    escribirSectores(stringSectores, sectoresAsignados);
                    this.dirActual.añadirArchivo(nuevo);
                } else {
                    throw new IllegalArgumentException("Archivo ya existe");
                }
            }else{
                throw new IllegalArgumentException("No hay sectores disponibles");
            }
        }else{
            throw new IllegalArgumentException("Formato de archivo invalido");
        }
    }
    
    private String[] split(String str, int size){
        List<String> strings = new ArrayList<String>();
        int index = 0;
        while (index < str.length()) {
            strings.add(str.substring(index, Math.min(index + size,str.length())));
            index += size;
        }
        return strings.toArray(new String[strings.size()]);
    }

    private boolean haySectoresDisponibles(int length) {
        return sectoresDisponibles.size() >= length;
    }

    private void escribirSectores(String[] stringSectores, int[] sectores) throws IOException {
        for (int i = 0; i < sectores.length; i++) {
            archivo.seek(sectores[i] * tamañoSectores);
            archivo.write(stringSectores[i].getBytes());
        }
    }
    //obtiene los primeros N sectores de la lista. Se asume que esta ordenada
    //para cumplir con criterio first fit. 
    private int[] obtenerSectores(int cantidad){
        int[] res = new int[cantidad];
        for (int i = 0; i < res.length; i++) {
            res[i] = this.sectoresDisponibles.remove(0);
        }
        return res;
    }
    
    private String obtenerString(String s){
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find())
        {
            return matcher.group(1);
        }else
            throw new IllegalArgumentException("Formato inadecuado de String");
    }
    
    private void mkdir(String nombre){
        if (nombre.matches("[a-zA-Z0-9()_-]+")) {
            dirActual.añadirSubdirectorio(new Directorio(dirActual.getPath(), nombre, dirActual));
        } else {
            throw new IllegalArgumentException("Formato de directorio invalido");
        }
    }
    
    private void cd(String nombre){
        if (nombre.equals("..")) {
            dirActual = dirActual.getPadre() == null ? dirActual : dirActual.getPadre();
        } else {
            dirActual = dirActual.obtenerDirectorio(nombre);
        }
    }
    
    private void ls(){
        dirActual.imprimirContenido();
    }
    
    private void mod (String nombre, String contenido) throws IOException{
        if (nombre.matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+")) {
            Archivo archivo = new Archivo(dirActual.getPath(),nombre);
            if(tabla.containsKey(archivo.getPathCompleto())){
                String[] stringSectores = split(contenido, this.tamañoSectores);
                    int[] sectores = (int[])tabla.get(archivo.getPathCompleto());
                if((sectores.length + sectoresDisponibles.size()) >= stringSectores.length){
                    //borrar sectores anteriores
                    liberarSectores(sectores);
                    
                    //se escribe el nuevo archivo
                    int[] sectoresAsignados = obtenerSectores(stringSectores.length);
                    escribirSectores(stringSectores, sectoresAsignados);
                    tabla.put(archivo.getPathCompleto(), sectoresAsignados);
                    actualizarFechaModificacion(nombre);
                }else{
                    throw new IOException("No hay suficiente espacio para modificar el archivo.");
                }
                
            }else{
                throw new IllegalArgumentException("El nombre del archivo ingresado no existe.");
            }
        }else{
            throw new IllegalArgumentException("Formato de archivo invalido");
        }
    }
    
    private void ordenarSectores(){
        Collections.sort(this.sectoresDisponibles);
    }
    
    private void liberarSectores(int[] sectores) throws IOException{
        int sector,direccion;
        byte[] b = new byte[(int)this.archivo.length()];
        this.archivo.seek(0);
        this.archivo.read(b);
        for(int contador = 0; contador<sectores.length; contador++){
            sector = sectores[contador];
            for (int contador2 = 0; contador2 < this.tamañoSectores; contador2++) {
                direccion = (sector * this.tamañoSectores) + contador2;
                b[direccion]= 0x20;
            }
            this.sectoresDisponibles.add(sector);
        }
        ordenarSectores();
        this.archivo.seek(0);
        archivo.write(b);
    }
    
    private void rm(String nombre) throws IOException{
        if (nombre.matches("[0-9A-Za-z]+\\.[A-Za-z]+")) {
            Archivo archivo = new Archivo(dirActual.getPath(),nombre);
            if(tabla.containsKey(archivo.getPathCompleto())){
                int[] sectores = (int[])tabla.get(archivo.getPathCompleto());
                liberarSectores(sectores);
                    
                tabla.remove(archivo.getPathCompleto(), sectores);
                dirActual.eliminarArchivo(archivo);
            }else{
                throw new IllegalArgumentException("El nombre del archivo ingresado no existe.");
            }
        }else{
            throw new IllegalArgumentException("Formato de archivo invalido");
        }
    }
    
    private void con(String nombre) throws IOException{
        if (nombre.matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+")) {
            Archivo archivo = new Archivo(dirActual.getPath(),nombre);
            if(tabla.containsKey(archivo.getPathCompleto())){
                int[] sectores = (int[])tabla.get(archivo.getPathCompleto());
                System.out.println(leerArchivo(sectores));
            }else{
                throw new IllegalArgumentException("El nombre del archivo ingresado no existe.");
            }
        }else{
            throw new IllegalArgumentException("Formato de archivo invalido");
        }
    }
    
    private String leerArchivo(int[] sectores) throws IOException{
        StringBuilder resultado = new StringBuilder();
        byte[] b = new byte[tamañoSectores];
        for (int contador = 0; contador < sectores.length; contador++) {
            int sector = sectores[contador];
            this.archivo.seek(sector*tamañoSectores);
            this.archivo.read(b);
            resultado.append(new String(b,"UTF-8"));
        }
        return resultado.toString();
    }
    
    private boolean existeDirectorio(String direccion){
        String[] directorios = direccion.split("\\\\");
        if (directorios.length == 1){
            return directorios[0].equals(root.getNombre());
        }
        if(!directorios[0].equals(root.getNombre())){
            return false;
        }
        Directorio temp = root;
        for (int contador = 1; contador < directorios.length; contador++) {
            String directorio = directorios[contador];
            temp =temp.obtenerDirectorio(directorio);
        }
        return true;
    }
    
    private void prop(String nombre){
        if (nombre.matches("[0-9A-Za-z]+\\.[A-Za-z]+")) {
            Archivo archivo = new Archivo(dirActual.getPath(),nombre);
            if(tabla.containsKey(archivo.getPathCompleto())){
                archivo = dirActual.obtenerArchivo(nombre);
                String res = "Directorio completo: "+archivo.getPathCompleto()+"\n";
                res += "Nombre: "+archivo.getNombre()+"\n";
                res += "Fecha de creación: "+archivo.getFechaCreacion()+"\n";
                res += "Fecha de modificación: "+archivo.getFechaModificacion()+"\n";
                System.out.println(res);
            }else{
                throw new IllegalArgumentException("El nombre del archivo ingresado no existe.");
            }
        }else{
            throw new IllegalArgumentException("Formato de archivo invalido");
        }
    }
    
    private boolean existeDirectorioReal(String nombre){
        File file = new File(nombre);
        return file.isDirectory();
    }
    
    private boolean existeArchivoReal(String nombre){
        File file = new File(nombre);
        return file.exists();
    }
    
    private void actualizarFechaModificacion(String nombre){
        dirActual.ActualizarFechaModificacion(nombre);
    }
       
    private void cp(String fuente, String destino) throws IOException{
        if (existeArchivoReal(fuente) && existeDirectorio(destino)){
            copiarReal_Virtual(fuente, destino);
        }else if (existeArchivo(fuente) && existeDirectorioReal(destino)){
            copiarVirtual_Real(fuente, destino);
        }else if(existeArchivo(fuente) && existeDirectorio(destino)){
            copiaVirtual_Virtual(fuente, destino);
        }else{
                System.out.println("Directorios invalidos ingresados.");
        }
    }
    
    private void copiaVirtual_Virtual(String fuente, String destino) throws IOException{
        String nombre = obtenerNombreArchivo(fuente);
        int[] sectores = tabla.get(fuente);
        String contenido = leerArchivo(sectores);
        Directorio directorio = buscarDirectorio(destino);
        nombre =  validaNombre(directorio, nombre);
        
        //creao el archivo 
        Archivo archivoNuevo = new Archivo(directorio.getPath(), nombre);
        String[] stringSectores = split(contenido, this.tamañoSectores);
        if(stringSectores.length <= sectoresDisponibles.size()){
            int[] sectoresAsignados = obtenerSectores(stringSectores.length);
            escribirSectores(stringSectores, sectoresAsignados);
            tabla.put(archivoNuevo.getPathCompleto(), sectoresAsignados);
            directorio.añadirArchivo(archivoNuevo);
            System.out.println("Se creo el archivo: "+nombre);
        }else{
            throw new IOException("No hay suficiente espacio para copiar el archivo.");
        }
    }
    
    private void copiarVirtual_Real(String fuente, String destino) throws IOException{
        String nombre = obtenerNombreArchivo(fuente);
        int[] sectores = tabla.get(fuente);
        String contenido = leerArchivo(sectores);
        
        
        //escribirArchivo
        nombre = directorioReal(destino, nombre);
        escribirArchivoReal(contenido, nombre);
    }
    
    private String directorioReal(String destino, String nombre){
        int indice = destino.indexOf("\\");
        if (indice==destino.length()-1) {
            return destino+nombre;
        }
        return destino+"\\"+nombre;
    }
    
    private void copiarReal_Virtual(String fuente, String destino) throws IOException{
            File archivo = new File(fuente);
            byte[] datos = Files.readAllBytes(archivo.toPath());
            String contenido = new String(datos, "UTF-8");
            String nombre = obtenerNombreArchivo(fuente);
            Directorio directorio = buscarDirectorio(destino);
            nombre =  validaNombre(directorio, nombre);
            
            //creao el archivo 
            Archivo archivoNuevo = new Archivo(directorio.getPath(), nombre);
            String[] stringSectores = split(contenido, this.tamañoSectores);
            if(stringSectores.length <= sectoresDisponibles.size()){
                int[] sectoresAsignados = obtenerSectores(stringSectores.length);
                escribirSectores(stringSectores, sectoresAsignados);
                tabla.put(archivoNuevo.getPathCompleto(), sectoresAsignados);
                directorio.añadirArchivo(archivoNuevo);
                System.out.println("Se creo el archivo: "+nombre);
            }else{
                throw new IOException("No hay suficiente espacio para copiar el archivo.");
            }
    }
    
    private String validaNombre(Directorio directorio, String nombre){
        if (directorio.contieneArchivo(nombre)){
            Archivo archivo = directorio.obtenerArchivo(nombre);
            archivo.aumentarCopias();
            int cantidad = archivo.getCopias();
            String[] partes = nombre.split("\\.");
            nombre =partes[0]+ "("+cantidad+")."+partes[1];
        }
        return nombre;
    }
    
    private boolean contieneArchivo(String direccion){
        String[] directorios = direccion.split("\\\\");
        String archivo = directorios[direccion.length()-1];
        return archivo.matches("[0-9A-Za-z()]+\\.[A-Za-z]+");
    }
    
    private String obtenerNombreArchivo(String direccion){
        String[] directorios = direccion.split("\\\\");
        String archivo = directorios[directorios.length-1];
        return archivo;
    }
    
    private Directorio buscarDirectorio(String direccion){
        String[] directorios = direccion.split("\\\\");
        if (directorios.length == 1&& directorios[0].equals(root.getNombre())){
            return root;
        }
        if(!directorios[0].equals(root.getNombre())){
            throw new IllegalArgumentException("Directorio no existe");
        }
        Directorio temp = root;
        for (int contador = 1; contador < directorios.length; contador++) {
            String directorio = directorios[contador];
            temp =temp.obtenerDirectorio(directorio);
        }
        return temp;
    }
    
    private boolean existeArchivo(String direccion){
        String[] directorios = direccion.split("\\\\");
        String nombre = directorios[directorios.length-1];
        if (nombre.matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+")) {
            if (directorios.length == 1){
                return directorios[0].equals(root.getNombre());
            }
            if(!directorios[0].equals(root.getNombre())){
                return false;
            }
            Directorio temp = root;
            for (int contador = 1; contador < directorios.length-1; contador++) {
                String directorio = directorios[contador];
                temp =temp.obtenerDirectorio(directorio);
            }
            return temp.contieneArchivo(nombre);
        }else{
            throw new IllegalArgumentException("El archivo a copiar es invalido.");
        }
    }
    
    private void escribirArchivoReal(String datos,String direccion) throws IOException{
        BufferedWriter output = null;
        try {
            File file = new File(direccion);
            output = new BufferedWriter(new FileWriter(file));
            output.write(datos);
        } catch ( IOException e ) {
            System.out.println(e.getMessage());
        } finally {
          if ( output != null ) {
            output.close();
          }
        }
    }
}
