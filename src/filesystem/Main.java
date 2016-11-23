/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author fm010
 */
public class Main {
    public static void main(String args[]) {
        FileSystem f = new FileSystem();
        try {
            f.start();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
       // System.out.println("f1.txt".matches("[0-9A-Za-z()-_]+\\.[A-Za-z]+"));
    }
}
