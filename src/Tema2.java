
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Tema2 {

    public static File orderFile; // fisierul din care se citesc comenzile
    public static File productFile; // fisierul din care se citesc produsele
    public static Semaphore productSem; // semafor pt a limita nr de thread-uri ProductFinder
    public static BufferedWriter orderWriter; // fiserul in care se scriu comenzile livrate
    public static BufferedWriter productWriter; // fisierul in care se scriu produsele livrate
    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Format: <folder_input> <nr_max_threads>");
            return;
        }
        int P; // numarul maxim de thread-uri la fiecare nivel
        try {
            P = Integer.parseInt(args[1]);
        }catch (NumberFormatException ex) {
            System.out.println("Format: <folder_input> <nr_max_threads>");
            return;
        }
        productSem = new Semaphore(P);

        orderFile = new File(args[0] + "/orders.txt");
        productFile = new File(args[0] + "/order_products.txt");
        try {
            orderWriter = new BufferedWriter(new FileWriter("orders_out.txt"));
            productWriter = new BufferedWriter(new FileWriter("order_products_out.txt"));
        } catch (IOException e) {
            System.out.println("Format: <folder_input> <nr_max_threads>");
            return;
        }


        Thread[] t = new Thread[P];
        for(int i = 0; i < P; i++){ // pornire thread-uri OrderProcessor
            t[i] = new Thread(new OrderProcessor(i, P));
            t[i].start();
        }
        for (int i = 0; i < P; i++){
            try {
                t[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            orderWriter.close();
            productWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
