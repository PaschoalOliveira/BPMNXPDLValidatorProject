/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bpmnxpdlvalidator;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 *
 * @author Paschoal new Dell
 */
public class BPMNXPDLValidator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
 /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                InitialFrame initialFrame = new InitialFrame();
                initialFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
                initialFrame.setVisible(true);
            }
        });
    }
    
}
