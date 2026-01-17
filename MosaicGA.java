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
    // public static void main(String[] args) {
    //     try {
    //         readMosaic("mosaic1.txt");
    //         Individual ind = createRandomIndividual();
    //         cekEncoding(ind);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    //Main untuk testing sementara saja
    public static void main(String[] args) {
        try {
            readMosaic("mosaic1.txt");

            //Buat 1 individu acak
            Individual ind = new Individual();
            ind.randomInit(rand);

            System.out.println("=== GENOTYPE ===");
            cekEncoding(ind);

            //Cek hitung3x3 manual
            System.out.println("\n=== TEST hitung3x3 ===");
            int r = rows / 2;
            int c = cols / 2;
            int sum = hitung3x3(ind, r, c);
            System.out.println("hitung3x3(" + r + "," + c + ") = " + sum);

            //Cek fitness dasar
            double baseFit = fitness(ind);
            System.out.println("\nBase fitness = " + baseFit);
            System.out.println("Total error  = " + ind.totalError);

            //Cek pola satu per satu
            double p0 = pola0(ind);
            double p4 = pola4(ind);
            double p6 = pola6(ind);
            double p9 = pola9(ind);

            System.out.println("\n=== POLA SCORE ===");
            System.out.println("pola0 = " + p0);
            System.out.println("pola4 = " + p4);
            System.out.println("pola6 = " + p6);
            System.out.println("pola9 = " + p9);

            //Fitness total
            double totalFit = fitnessTotal(ind);
            System.out.println("\nTotal fitness = " + totalFit);

            if (totalFit < 0) {
                System.out.println("WARNING: fitness negatif");
            }

            //Mutasi
            System.out.println("\n=== TEST MUTATION ===");
            int countBefore = countOnes(ind);
            ind.mutate(rand, 1.0);
            int countAfter = countOnes(ind);

            System.out.println("Jumlah 1 sebelum = " + countBefore);
            System.out.println("Jumlah 1 sesudah = " + countAfter);


            //Copy
            System.out.println("\n=== TEST COPY ===");
            Individual copy = new Individual(ind);
            copy.flip(r, c);

            System.out.println("Original gene = " + ind.gene[r][c]);
            System.out.println("Copy gene     = " + copy.gene[r][c]);

            testGetFittest();
            testTournamentSelection();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void testTournamentSelection() {
        System.out.println("\n=== TEST tournamentIndividual ===");

        Random rng = new Random(2);

        Population pop = new Population(10, rng, false);

        for (int i = 0; i < 10; i++) {
            pop.individuals[i] = new Individual();
            pop.individuals[i].fitness = i; // fitness naik
        }

        Individual selected = pop.tournamentIndividual(rng, 3);

        System.out.println("Selected fitness = " + selected.fitness);
        System.out.println("Note: should be relatively high (>= 7)");
    }


    static void testGetFittest() {
        System.out.println("=== TEST getFittest ===");

        Random rng = new Random(1);

        Population pop = new Population(5, rng, false);

        for (int i = 0; i < 5; i++) {
            pop.individuals[i] = new Individual();
            pop.individuals[i].randomInit(rng);
            pop.individuals[i].fitness = i; // set manual, terkontrol
        }

        Individual best = pop.getFittest();

        System.out.println("Expected fitness = 4");
        System.out.println("Actual fitness   = " + best.fitness);
    }


    static int countOnes(Individual ind) {
        int cnt = 0;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                cnt += ind.gene[i][j];
        return cnt;
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
        ind.totalError = totalError;
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
