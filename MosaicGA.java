import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class MosaicGA {
    static int rows;
    static int cols;
    static int[][] mosaic;
    static Random rand = new Random(12345);

    //Parameter yang nanti bisa ganti untuk eksperimen
    static int populationSize = 1000;
    static double mutationRate = 0.001;
    static int generations = 1000;
    public static void main(String[] args) {
        int genFound = -1;
        String stopReason = "";
        double bestFitnessNow = Double.NEGATIVE_INFINITY;
        int stagnantCount = 0;
        int maxStagnant = 500;
        try {
            //Baca papan
            readMosaic("mosaic5x5_Hard.txt");

            //Inisialisasi populasi awal
            Population population = new Population(populationSize, rand, true);

            //Evolusi generasi
            for (int gen = 0; gen < generations; gen++) {

                Individual best = population.getFittest();

                //Berhenti jika solusi sempurna ditemukan
                if (best.totalError == 0) {
                    genFound = gen;
                    stopReason = "Solusi ditemukan";
                    break;
                }

                if (best.fitness > bestFitnessNow) {
                    bestFitnessNow = best.fitness;
                    stagnantCount = 0;
                }
                else{
                    stagnantCount++;
                }

                if (stagnantCount>=maxStagnant) {
                    genFound = gen;
                    stopReason = "Stagnant";
                    break;
                }

                population = evolvePopulation(population, rand);
            }

            if (stopReason.equals("")) {
                stopReason = "Maksimum generasi";
            }

            //Ambil solusi terbaik akhir
            Individual best = population.getFittest();

            //Pastikan fitness up-to-date
            best.updateFitness();

            //Cetak solusi akhir
            printFinalSolution(best, stopReason, genFound);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void printFinalSolution(Individual best, String stopReason, int genFound) {
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
        System.out.println("Generation ditemukan: " + genFound);
        System.out.println("Alasan berhenti: " + stopReason);
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

    //Method untuk random flip-bit
    static void randomFlipBit(Individual ind, Random rng){
        int r =  rng.nextInt(rows);
        int c = rng.nextInt(cols);
        ind.flip(r, c);
    }

    //Mutasi guided: cari clue paling salah, lalu flip yang memperbaiki
    static void guidedFlipBit(Individual ind, Random rng){
        int bestR = -1;
        int bestC = -1;
        int bestExpected = 0;
        int bestActual = 0;
        int bestError = 0;

        // cari clue yang error nya paling besar
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                int expected = mosaic[r][c];
                if(expected<0){
                    continue;
                }
                int actual = hitung3x3(ind, r, c);
                int err = Math.abs(actual- expected);

                if(err>bestExpected){
                    bestError = err;
                    bestR = r;
                    bestC = c;
                    bestExpected = expected;
                    bestActual = actual;
                }
            }
        }
        //kalau sudah ketemu cluenya -> lakukan
        if(bestError==0 || bestR==-1){
            randomFlipBit(ind, rng);
            return;
        }

        //tentukan arah kebalikannya
        boolean needDecrease = (bestActual>bestExpected);
        boolean needIncrease = (bestActual<bestExpected);

        //kumpulkan kandidat sel yang bisa di flip di 3x3
        ArrayList<int[]> candidates = new ArrayList<>();

        for(int r = bestR - 1; r <= bestR+1; r++){
            for (int c = bestC - 1; c <= bestC + 1; c++) {
                if (r < 0 || r >= rows || c < 0 || c >= cols) {
                    continue;
                }
                int val = ind.gene[r][c];
                if(needDecrease && val==1){
                    candidates.add(new int[]{r, c}); //flip 1 - 0
                }else if(needIncrease && val==0){
                    candidates.add(new int[]{r, c}); //flip 0 - 1
                }
            }
        }
        //kalau kandidat kosong - fallback random
        if(candidates.isEmpty()){
            randomFlipBit(ind, rng);
            return;
        }

        //pilih kandidat random lalu flip
        int pick = rng.nextInt(candidates.size());
        int[] cell = candidates.get(pick);
        ind.flip(cell[0], cell[1]);

    }

    // Uniform crossover: tiap gen diambil dari parent1 atau parent2 secara acak
    static Individual uniformCrossover(Individual p1, Individual p2, Random rng){
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

    // Deep copy individu (biar child tidak share reference dengan parent)
    static Individual deepCopy(Individual src) {
        Individual dst = new Individual();
        for (int r = 0; r < rows; r++) {
            System.arraycopy(src.gene[r], 0, dst.gene[r], 0, cols);
        }
        return dst;
    }

    // Rectangle crossover: swap 1 blok persegi panjang antara parent1 dan parent2
// Hasil: 2 anak (child1, child2)
    static Individual[] rectangleCrossover(Individual parent1, Individual parent2, Random rng) {

        Individual child1 = deepCopy(parent1);
        Individual child2 = deepCopy(parent2);

        // 1) pilih 2 baris acak untuk membentuk batas rectangle
        int r1 = rng.nextInt(rows);
        int r2 = rng.nextInt(rows);
        if (r2 < r1) { int tmp = r1; r1 = r2; r2 = tmp; }

        // 2) pilih 2 kolom acak untuk membentuk batas rectangle
        int c1 = rng.nextInt(cols);
        int c2 = rng.nextInt(cols);
        if (c2 < c1) { int tmp = c1; c1 = c2; c2 = tmp; }

        // 3) tukar isi gene di dalam rectangle
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {

                int temp = child1.gene[r][c];
                child1.gene[r][c] = child2.gene[r][c];
                child2.gene[r][c] = temp;
            }
        }

        return new Individual[]{child1, child2};
    }


    static Population evolvePopulation(Population pop, Random rng){
        Population newPop = new Population(pop.individuals.length, rng, false);
        
        int elitism = 1;

        newPop.individuals[0] = new Individual(pop.getFittest());

        for(int i = elitism; i<newPop.individuals.length; i++){
            // Individual parent1 = pop.tournamentIndividual(rng, 5);
            // Individual parent2 = pop.tournamentIndividual(rng, 5);
            Individual parent1 = pop.rouletteWheelSelection(rng);
            Individual parent2 = pop.rouletteWheelSelection(rng);
            
            // PILIHAN CROSSOVER:
            // Uncomment salah satu untuk percobaan:

            // 1. Uniform Crossover (default, per-gene ambil dari parent1/parent2 secara acak)
            //Individual child = uniformCrossover(parent1, parent2, rng);

            // 2. Rectangle Crossover (swap satu blok persegi panjang antara parent1 & parent2)
            Individual[] children = rectangleCrossover(parent1, parent2, rng);
            Individual child = rng.nextBoolean() ? children[0] : children[1];

            // 1. Random Flip Bit Mutation (default)
            child.mutate(rng, mutationRate);

            // 2. Guided Mutation (menggunakan clue untuk memilih bit yang akan di-flip)
            // child.guidedMutate(rng, mutationRate);

            child.updateFitness();

            newPop.individuals[i] = child;
        }
        return newPop;
    }
}
