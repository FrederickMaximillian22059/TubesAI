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

    static double fitnessTotal(Individual ind) {
        double baseFitness = fitness(ind);

        double polaScore =
                pola0(ind) +
                pola4(ind) +
                pola6(ind) +
                pola9(ind);

        return baseFitness + 0.01 * polaScore;
    }


    static double fitness(Individual ind) {
        int totalError = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int expected = mosaic[i][j];
                if (expected >=0) {
                    int actual = hitung3x3(ind, i, j);
                    totalError += Math.abs(actual - expected);
                }
            }
        }
        return 1.0 / (1 + totalError);
    }

    //Untuk menghitung jumlah pada area 3x3 di sekitar (r,c)
    static int hitung3x3(Individual ind, int r, int c) {
        int sum = 0;
        for (int i = r - 1; i <= r + 1; i++) {
            for (int j = c - 1; j <= c + 1; j++) {
                if (i >= 0 && i < rows && j >= 0 && j < cols) {
                    sum += ind.gene[i][j];
                }
            }
    }
        return sum;
    }

    //Untuk hitung fitness berdasarkan pola-pola yang diketahui (memang belum diisi)
    static double pola0(Individual ind) {
        double score = 0.0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mosaic[r][c] == 0) {
                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {
                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                if (ind.gene[i][j] == 0) score += 1;
                                else score -= 2;
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    static double pola9(Individual ind) {
        double score = 0.0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mosaic[r][c] == 9) {
                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {
                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                if (ind.gene[i][j] == 1) score += 1;
                                else score -= 2;
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    static double pola4(Individual ind) {
        double score = 0.0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (mosaic[r][c] == 4 && isCorner(r, c)) {

                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {

                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                if (ind.gene[i][j] == 1) {
                                    score += 2;
                                } else {
                                    score -= 5;
                                }
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    static double pola6(Individual ind) {
        double score = 0.0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (mosaic[r][c] == 6 && isEdge(r, c)) {

                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {

                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                if (ind.gene[i][j] == 1) {
                                    score += 2;
                                } else {
                                    score -= 4;
                                }
                            }
                        }
                    }
                }
            }
        }
        return score;
    }


    //Method untuk mengecek apakah suatu angka ada di corner atau sisi, digunakan di pola4 dan pola6
    static boolean isCorner(int r, int c) {
        return (r == 0 || r == rows - 1) && (c == 0 || c == cols - 1);
    }

    static boolean isEdge(int r, int c) {
        return (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) && !isCorner(r, c);
    }
}
