/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fm010
 */
public class FileSystem {

    private HashMap<String, int[]> tabla; //Hash. Key=ruta, Valor=lista de sectores
    private int cantSectores;
    private int tamañoSectores;
    private int sectoresDesponibles;
    RandomAccessFile archivo;
    private Directorio root;
    
    public void start() {
            Scanner scanner = new Scanner(System.in);
            root = new Directorio("root/");
            String command;
            String[] subString;
            while(true) {
                try{
                    System.out.print("\n> ");
                    command = scanner.nextLine();
                    subString = command.split("\\s+");
                    command = subString[0];
                    switch(command){
                        case "create":
                            create(subString[1], subString[2]);
                            break;
                        case "file":
                            break;
                        case "mkdir":
                            break;
                        case "cd":
                            break;
                        case "ls":
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
            }
        }
    }
    
    private void create(String sectores, String tamaño){
        try {
            this.cantSectores = Integer.parseInt(sectores);
            this.tamañoSectores =  Integer.parseInt(tamaño);
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
        this.archivo.close();//quitar
    }
    
}
