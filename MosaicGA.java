import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class MosaicGA {
    static int rows;
    static int cols;
    static int[][] mosaic;
    static Random rand = new Random();

    //Parameter yang nanti bisa ganti untuk eksperimen
    static int populationSize = 100;
    static double mutationRate = 0.01;
    static int generations = 1000;
    public static void main(String[] args) {
        try {
            readMosaic("mosaic1.txt");
            Individual ind = createRandomIndividual();
            cekEncoding(ind);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Untuk membaca file txt yang berisi ukuran dan isi mosaic
    static void readMosaic(String filename) throws Exception {
        BufferedReader buffread = new BufferedReader(new FileReader(filename));

        String[] size = buffread.readLine().split(" ");
        rows = Integer.parseInt(size[0]);
        cols = Integer.parseInt(size[1]);

        mosaic = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            String[] line = buffread.readLine().split(" ");
            for (int j = 0; j < cols; j++) {
                mosaic[i][j] = Integer.parseInt(line[j]);
            }
        }
        buffread.close();
    }

    //Untuk membuat individu secara acak
    static Individual createRandomIndividual() {
        Individual ind = new Individual();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ind.gene[i][j] = rand.nextBoolean() ? 1 : 0;
            }
        }
        return ind;
    }

    //Untuk cek encoding individu saja, nanti tidak dipakai
    static void cekEncoding(Individual ind) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(ind.gene[i][j] + " ");
            }
            System.out.println();
        }
    }

    //Untuk hitung fitness berdasarkan pola-pola yang diketahui (memang belum diisi)
    static Double pola0(Individual ind) {
        return 0.0;
    }

    static Double pola6(Individual ind) {
        return 0.0;
    }

    static Double pola4(Individual ind) {
        return 0.0;
    }

    static Double pola9(Individual ind) {
        return 0.0;
    }
}
