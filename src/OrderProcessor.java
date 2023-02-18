import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderProcessor implements Runnable {
    private final int index; // indexul procesatorului de comenzi
    private final int total; // nr total de procesatori de comenzi


    /** cauta produsele asociate comenzii
     * @param orderID id-ul comenzii
     * @param productCount numarul de produse in comanda
     * @return true : avem suficiente produse
     *         false : altfel
     */
    private boolean findProducts(String orderID, int productCount){
        // contor pt numarul de produse gasite
        AtomicInteger foundProductCount = new AtomicInteger(0);

        // citire thread-safe din fisier
        BufferedReader productReader = null;
        try {
            productReader = new BufferedReader(new FileReader(Tema2.productFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Thread[] threads = new Thread[productCount];
        for(int i = 0; i < productCount; i++){
            //creaza productCount thread-uri ProductFinder
            threads[i] = new Thread(new ProductFinder(orderID, foundProductCount, productReader));
            try {
                // foloseste semaforul pt a limita nr de thread-uri care ruleaza in acelasi timp
                Tema2.productSem.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            threads[i].start();
        }

        for (int i = 0; i < productCount; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return foundProductCount.get() == productCount;
    }

    public OrderProcessor(int index, int total) {
        this.index = index;
        this.total = total;
    }

    @Override
    public void run() {

        try {
            RandomAccessFile file = new RandomAccessFile(Tema2.orderFile, "r");
            long byteCount = file.length(); // lungimea fisierului
            long chunkSize = byteCount / total;
            long startPos = chunkSize * index; // pozitia de unde incepe citirea
            long endPos = startPos + chunkSize; // pozitia unde se termina citirea
            if(index == total - 1) // ultimul thread va merge pana la capatul fisierului
                endPos = byteCount;
            if(startPos != 0){
                // incrementam startPos pana cand ajunge la inceputul unei linii
                file.seek(startPos-1);
                while (file.read() != '\n')
                    startPos ++;
            }
            // incrementam endPos pana cand ajunge la sfarsitul unei linii
            file.seek(endPos-1);
            while (file.read() != '\n')
                endPos++;

            long pos = startPos; // pozitia in fisier
            file.seek(startPos);

            while (pos < endPos){

                String line = file.readLine();
                if(line == null) break;
                pos += (line.length() + 1); // adaugam 1 pt sfarsitul de linie

                String[] parts = line.split(",");
                String orderID = parts[0];

                int productCount = Integer.parseInt(parts[1]);
                if(productCount == 0) continue; // comanda tip Empty Order

                if(findProducts(orderID, productCount)){
                    // daca am gasit suficiente produse, marcam comanda ca livrata
                    Tema2.orderWriter.write(line + ",shipped\n");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
