package edu.uam.educore;

import edu.uam.educore.api.ServidorApi;

public class Main {

  public static void main(String[] args) throws Exception {
    int puerto = Integer.parseInt(System.getProperty("api.port", "8080"));
    ServidorApi.iniciar(puerto);
  }
}
