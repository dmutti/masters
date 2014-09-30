package debuggee;

import java.math.*;
import java.util.*;

public class Target {

    public static void main(String[] args) throws Exception {

        /*System.out.println("waiting");
        Thread.sleep(10000);
        System.out.println("starting");*/

        Random rand = new Random();
        int contador = Integer.parseInt(args[0]);

        for (int i = 0; i < contador; i++) {
            String nome = args[1];
            int idade = Integer.parseInt(args[2]);
            int nextInt = rand.nextInt(nome.length());
            BigDecimal fator = new BigDecimal(nextInt * idade + i);
            System.out.println(fator);
        }
    }
}
