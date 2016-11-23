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
                            String valor = obtenerString(input);
                            subString=input.split("\"(.*?)\"");
                            mod(subString[1].replaceAll("\\s+",""),valor);
                            break;
                        case "con":
                            con(subString[1]);
                            break;
                        case "prop":
                            prop(subString[1]);
                            break;
                        case "cp":
                            cp(subString[1],subString[2]);
                            break;
                        case "mov":
                            mov(subString[1],subString[2]);
                            break;
                        case "find":
                            find(subString[1], root);
                            break;
                        case "tree":
                            System.out.println(root.tree(0));
                            break;
                        case "rm":
                            rm(subString[1],dirActual);
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
                Archivo nuevo = new Archivo(dirActual.getPath(),nombre, contenido.length());
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
        Directorio actual = dirActual;
        String[] rutas = nombre.split("\\\\");
        if (rutas.length == 1) {
            if (nombre.equals("..")) {
                dirActual = dirActual.getPadre() == null ? dirActual : dirActual.getPadre();
            }else if(nombre.equals("root")){
                dirActual = root;
            }else{
                dirActual = dirActual.obtenerDirectorio(nombre);
            }
        }
         else {
            dirActual = root;
            if (!rutas[0].equals("root")) {
                throw new IllegalArgumentException("Directorio no existe");
            }
            for (int i = 1; i < rutas.length; i++) {
                try {
                    dirActual = dirActual.obtenerDirectorio(rutas[i]);
                } catch (IllegalArgumentException e) {
                    dirActual = actual;
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }
    }
    
    private void ls(){
        dirActual.imprimirContenido();
    }
    
    private void mod (String nombre, String contenido) throws IOException{
        if (nombre.matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+")) {
            Archivo archivo = new Archivo(dirActual.getPath(),nombre, contenido.length());
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
                    actualizarFechaModificacion(nombre,contenido.length());
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
    
    private void rm(String nombre, Directorio directorio) throws IOException {
        if (directorio.contieneSubDirectorio(nombre)){
            Directorio subDirectorio = directorio.obtenerDirectorio(nombre);
            eliminarArchivos(subDirectorio);
            eliminarSubdirectorios(subDirectorio,subDirectorio.getSubDirectorios());
            directorio.eliminarSubdirectorio(subDirectorio);
        }else{
            removeArchivo(nombre, directorio);
        }
    }
    private void eliminarSubdirectorios(Directorio padre, ArrayList<Directorio> subDirectorios) throws IOException{
        int total = subDirectorios.size()-1;
        for (; total >=0 ; total--) {
            Directorio subdirectorio = subDirectorios.get(total);
            eliminarArchivos(subdirectorio);
            eliminarSubdirectorios(subdirectorio, subdirectorio.getSubDirectorios());
            padre.eliminarSubdirectorio(subdirectorio);
        }
    }
    
    private void eliminarArchivos(Directorio subDirectorio) throws IOException{
        ArrayList<Archivo> archivos = subDirectorio.getArchivos();
        int total = archivos.size() - 1;
        for (; total >=0; total--) {
            Archivo archivo = archivos.get(total);
            removeArchivo(archivo.getNombre(), subDirectorio);
        }    
    }
    
    private void removeArchivo(String nombre, Directorio directorio) throws IOException{
        if (nombre.matches("[0-9A-Za-z-_()]+\\.[A-Za-z]+")) {
            Archivo archivo = new Archivo(directorio.getPath(),nombre, 0);
            if(tabla.containsKey(archivo.getPathCompleto())){
                int[] sectores = (int[])tabla.get(archivo.getPathCompleto());
                liberarSectores(sectores);
                tabla.remove(archivo.getPathCompleto(), sectores);
                directorio.eliminarArchivo(archivo);
            }else{
                throw new IllegalArgumentException("El nombre del archivo ingresado no existe.");
            }
        }else{
            throw new IllegalArgumentException("Formato de archivo invalido");
        }
    }
    
    private void con(String nombre) throws IOException{
        if (nombre.matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+")) {
            Archivo archivo = new Archivo(dirActual.getPath(),nombre, 0);
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
            Archivo archivo = new Archivo(dirActual.getPath(),nombre, 0);
            if(tabla.containsKey(archivo.getPathCompleto())){
                archivo = dirActual.obtenerArchivo(nombre);
                String res = "Directorio completo: "+archivo.getPathCompleto()+"\n";
                res += "Nombre: "+archivo.getNombre()+"\n";
                res += "Fecha de creación: "+archivo.getFechaCreacion()+"\n";
                res += "Fecha de modificación: "+archivo.getFechaModificacion()+"\n";
                res += "Tamaño: "+archivo.getSize()+"\n";
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
    
    private void actualizarFechaModificacion(String nombre, int size){
        dirActual.ActualizarFechaModificacion(nombre,size);
    }
       
    private void cp(String fuente, String destino) throws IOException{
        if (existeArchivoReal(fuente) && existeDirectorio(destino)){
            copiarReal_Virtual(fuente, destino);
        }else if (existeArchivo(fuente) && existeDirectorioReal(destino)){
            copiarVirtual_Real(fuente, destino);
        }else if(existeArchivo(fuente) && existeDirectorio(destino)){
            copiaVirtual_Virtual(fuente, destino);
        }else{
            throw new IllegalArgumentException("Directorios invalidos ingresados.");
        }
    }
    
    private void copiaVirtual_Virtual(String fuente, String destino) throws IOException{
        String nombre = obtenerNombreArchivo(fuente);
        int[] sectores = tabla.get(fuente);
        String contenido = leerArchivo(sectores);
        Directorio directorio = buscarDirectorio(destino);
        nombre =  validaNombre(directorio, nombre);
        
        //creo el archivo 
        Archivo archivoNuevo = new Archivo(directorio.getPath(), nombre,contenido.length());
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
            Archivo archivoNuevo = new Archivo(directorio.getPath(), nombre, contenido.length());
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
        String archivo = directorios[directorios.length-1];
        return archivo.matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+");
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
    private void mov(String fuente, String destino){
        if (existeArchivo(fuente) && existeDirectorio(destino)) {
            String dirFuente = ObtenerDirectorioFuente(fuente);
            String nombre = obtenerNombreArchivo(fuente);
            Directorio directorioFuente = buscarDirectorio(dirFuente);
            Archivo archivo = directorioFuente.obtenerArchivo(nombre);
            int[] sectores = tabla.get(archivo.getPathCompleto());
            
            Directorio directorioDestino = buscarDirectorio(destino);
            String res = "s";
            Scanner scanner = new Scanner(System.in);
            if (directorioDestino.contieneArchivo(nombre)){
                System.out.println("El archivo "+nombre+", existe en el directorio destino"
                        + ", ¿desea sobreescribirlo? S/N.");
                res = scanner.nextLine();
                
            }
            res = res.replaceAll("\\s+", "");
            res = res.toLowerCase();
            if (res.equals("s")){
                System.out.println("¿Desea cambiarle el nombre? S/N");
                res = scanner.nextLine();
                res = res.replaceAll("\\s+", "");
                res = res.toLowerCase();
                if (res.equals("s")){
                    System.out.println("Digite el nombre del archivo:");
                    nombre = scanner.nextLine();
                    if(!nombre.matches("[0-9A-Za-z]+\\.[A-Za-z]+")){
                        throw new IllegalArgumentException("Formato de archivo invalido.");
                    }
                }
                directorioFuente.eliminarArchivo(archivo);
                tabla.remove(archivo.getPathCompleto());
                destino = formatearDestino(destino);
                Archivo archivoNuevo = new Archivo(destino, nombre,archivo.getSize());
                tabla.put(archivoNuevo.getPathCompleto(), sectores);
                directorioDestino.añadirArchivo(archivoNuevo);
            }else {
                System.out.println("Operación cancelada.");
            }
            
        }else{
            System.out.println("Los Directorios ingresados invalidos.");
        }
    }
    
    private String formatearDestino(String destino){
        if (!((destino.charAt(destino.length()-1)) == '\\')){
            return destino+"\\";
        }
        return destino;
    }
    
    private String ObtenerDirectorioFuente(String fuente){
        String[] datos = fuente.split("\\\\");
        String res = "";
        for (int i = 0; i < datos.length-2; i++) {
            String dato = datos[i];
            res += dato+"\\";
        }
        res += datos[datos.length-2];
        return res;
    }
    
    private void find(String nombre, Directorio directorio){
        if(contieneArchivo(nombre)){
            String[] partes = nombre.split("\\.");
            if(partes[0].equals("*")){
                imprimePorExtension(partes[1], directorio.getArchivos());
            }else{
                imprimePorNombre(nombre, directorio.getArchivos());
            }
        }else{
            imprimeDirectorios(nombre, directorio.getSubDirectorios());
        }
        for (int contador = 0; contador < directorio.getSubDirectorios().size(); contador++) {
            Directorio subdirectorio = directorio.getSubDirectorios().get(contador);
            find(nombre, subdirectorio);
        }
    }
    
    private void imprimePorExtension(String extension,ArrayList<Archivo> archivos){
        for (int contador = 0; contador < archivos.size(); contador++) {
            Archivo archivo = archivos.get(contador);
            String[] partes = archivo.getNombre().split("\\.");
            if(partes[1].equals(extension))
                System.out.println(archivo.getPathCompleto());
        }
    }
    
    private void imprimePorNombre(String nombre, ArrayList<Archivo> archivos){
        for (int contador = 0; contador < archivos.size(); contador++) {
            Archivo archivo = archivos.get(contador);
            if(archivo.getNombre().equals(nombre))
                System.out.println(archivo.getPathCompleto());
        }
    }

    private void imprimeDirectorios(String nombre, ArrayList<Directorio> subDirectorios) {
        for (int i = 0; i < subDirectorios.size(); i++) {
            Directorio directorio = subDirectorios.get(i);
            if (directorio.getNombre().equals(nombre))
                System.out.println(directorio.getPath());
        }
    }
}
