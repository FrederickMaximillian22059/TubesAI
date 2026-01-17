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
            //Baca papan
            readMosaic("mosaic3.txt");

            //Inisialisasi populasi awal
            Population population = new Population(populationSize, rand, true);

            //Evolusi generasi
            for (int gen = 0; gen < generations; gen++) {

                Individual best = population.getFittest();

                //Berhenti jika solusi sempurna ditemukan
                if (best.totalError == 0) {
                    break;
                }

                population = evolvePopulation(population, rand);
            }

            //Ambil solusi terbaik akhir
            Individual best = population.getFittest();

            //Pastikan fitness up-to-date
            best.updateFitness();

            //Cetak solusi akhir
            printFinalSolution(best);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void printFinalSolution(Individual best) {
        System.out.println("SOLUSI TERBAIK DITEMUKAN:");
        System.out.println("Fitness     = " + best.fitness);
        System.out.println("Total Error = " + best.totalError);
        System.out.println("Genotype:");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(best.gene[i][j] + " ");
            }
            System.out.println();
        }
    }



    // static void testTournamentSelection() {
    //     System.out.println("\n=== TEST tournamentIndividual ===");

    //     Random rng = new Random(2);

    //     Population pop = new Population(10, rng, false);

    //     for (int i = 0; i < 10; i++) {
    //         pop.individuals[i] = new Individual();
    //         pop.individuals[i].fitness = i; // fitness naik
    //     }

    //     Individual selected = pop.tournamentIndividual(rng, 3);

    //     System.out.println("Selected fitness = " + selected.fitness);
    //     System.out.println("Note: should be relatively high (>= 7)");
    // }


    // static void testGetFittest() {
    //     System.out.println("=== TEST getFittest ===");

    //     Random rng = new Random(1);

    //     Population pop = new Population(5, rng, false);

    //     for (int i = 0; i < 5; i++) {
    //         pop.individuals[i] = new Individual();
    //         pop.individuals[i].randomInit(rng);
    //         pop.individuals[i].fitness = i; // set manual, terkontrol
    //     }

    //     Individual best = pop.getFittest();

    //     System.out.println("Expected fitness = 4");
    //     System.out.println("Actual fitness   = " + best.fitness);
    // }


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

    static Individual crossover(Individual p1, Individual p2, Random rng){
        Individual child = new Individual();
        for(int i = 0 ; i < rows ; i++){
            for(int j = 0 ; j < cols ; j++){
                if(rng.nextBoolean()){
                    child.gene[i][j] = p1.gene[i][j];
                } else {
                    child.gene[i][j] = p2.gene[i][j];
                }
            }
        }
        return child;
    }

    static Population evolvePopulation(Population pop, Random rng){
        Population newPop = new Population(pop.individuals.length, rng, false);
        
        int elitism = 1;

        newPop.individuals[0] = new Individual(pop.getFittest());

        for(int i = elitism; i<newPop.individuals.length; i++){
            Individual parent1 = pop.tournamentIndividual(rng, 5);
            Individual parent2 = pop.tournamentIndividual(rng, 5);
            Individual child = crossover(parent1, parent2, rng);
            child.mutate(rng, mutationRate);
            child.updateFitness();

            newPop.individuals[i] = child;
        }
        return newPop;
    }
}
