import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductFinder implements Runnable {
    private final String orderID; // id-ul comenzii
    private final AtomicInteger foundProductCount; // contor pentru numarul de produse gasite
    private final BufferedReader productReader;

    public ProductFinder(String orderID, AtomicInteger foundProductCount, BufferedReader productReader) {
        this.orderID = orderID;
        this.foundProductCount = foundProductCount;
        this.productReader = productReader;
    }

    @Override
    public void run() {

        try {
            while (true){
                // citeste o linie din fisier
                // intrucat BufferedReader este thread-safe, fiecare linie va fi citita o singura data
                String line = productReader.readLine();
                if(line == null) break; // s-a terminat fisierul
                if(line.startsWith(orderID)){ // am gasit un produs

                    //incrementam contorul
                    foundProductCount.incrementAndGet();

                    //scriem in "order_products_out.txt" ca am livrat produsul
                    Tema2.productWriter.write(line + ",shipped\n");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tema2.productSem.release();
    }
}
