/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                            break;
                        case "prop":
                            break;
                        case "cp":
                            break;
                        case "mov":
                            break;
                        case "find":
                            break;
                        case "tree":
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
        if (nombre.matches("[0-9A-Za-z]+\\.[A-Za-z]+")) {
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
        if (nombre.matches("[a-zA-Z0-9]+")) {
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
    
}
