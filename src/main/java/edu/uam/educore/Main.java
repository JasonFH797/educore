package edu.uam.educore;

import edu.uam.educore.view.MenuPrincipalView;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            System.setOut(
                new java.io.PrintStream(
                    System.out,
                    true,
                    java.nio.charset.StandardCharsets.UTF_8
                )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);

        new MenuPrincipalView(scanner).iniciar();

        scanner.close();
    }
}