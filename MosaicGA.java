import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class utama untuk menjalankan Genetic Algorithm (GA) untuk masalah mosaic
 * Class ini bertugas:
 * - Membaca input mosaic dari file
 * - Menginisialisasi populasi awal
 * - Melakukan evolusi populasi
 * - Mennghentikan evolusi berdasarkan kriteria tertentu
 * - Menampilkan solusi terbaik yang ditemukan dan juga errornya (jika ada)
 */
public class MosaicGA {
    /**Jumlah baris papan mosaic */
    static int rows;

    /**Jumlah kolom papan mosaic */
    static int cols;

    /**
     * Papan mosaic
     * Jika isinya >= 0, berarti itu adalah clue (angka target)
     * Jika isinya -1, berarti itu bukan clue
    */
    static int[][] mosaic;

    /**Objek Random untuk operasi acak di GA */
    static Random rand = new Random(12345);

    /**
     * PARAMETER-PARAMETER UNTUK EKSPERIMEN GA
     */

    /**Jumlah individu dalam populasi */
    static int populationSize = 1000;

    /**Probabilitas mutasi*/
    static double mutationRate = 0.001;

    /**Jumlah generasi maksimum */
    static int generations = 1000;

    /**Probabilitas crossover */
    static double crossoverRate = 1.0;

    /**
     * Method main
     * Menjalankan semua proses GA untuk mencari solusi mosaic
     */
    public static void main(String[] args) {
        /**Generasi saat solusi ditemukan (digunakan untuk data eksperimen) */
        int genFound = -1;

        /**Alasan berhenti (digunakan untuk data eksperimen) */
        String stopReason = "";

        /**Fitness terbaik saat ini*/
        double bestFitnessNow = Double.NEGATIVE_INFINITY;

        /**Counter untuk mendeteksi stagnant*/
        int stagnantCount = 0;

        /**Batas maksimum stagnant*/
        int maxStagnant = 500;
        try {
            /**Baca papan mosaic dari file*/
            readMosaic("mosaic5x5_Easy.txt");

            /**Inisialisasi populasi awal*/
            Population population = new Population(populationSize, rand, true);

            /**Loop evolusi generasi*/
            for (int gen = 0; gen < generations; gen++) {
                /**Ambil individu terbaik dalam populasi saat ini*/
                Individual best = population.getFittest();

                /**Berhenti jika solusi sempurna ditemukan (totalError == 0) */
                if (best.totalError == 0) {
                    genFound = gen;
                    stopReason = "Solusi ditemukan";
                    break;
                }

                /**Untuk mendeteksi stagnant*/
                if (best.fitness > bestFitnessNow) {//Kalau ada perbaikan, reset stagnantCount
                    bestFitnessNow = best.fitness;
                    stagnantCount = 0;
                }
                else{//Kalau tidak ada perbaikan, tambah stagnantCount
                    stagnantCount++;
                }

                /**Jika stagnantCount sudah mencapai batas maksimum, berhenti*/
                if (stagnantCount>=maxStagnant) {
                    genFound = gen;
                    stopReason = "Stagnant";
                    break;
                }

                /**Evolusi populasi ke generasi berikutnya*/
                population = evolvePopulation(population, rand);
            }

            /**Jika loop selesai tanpa menemukan solusi, set alasan berhenti*/
            if (stopReason.equals("")) {
                stopReason = "Maksimum generasi";
            }

            /**Ambil solusi terbaik akhir*/
            Individual best = population.getFittest();

            /**Pastikan fitness terupdate*/
            best.updateFitness();

            /**Cetak solusi akhir*/
            printFinalSolution(best, stopReason, genFound);

            /**Cetak error pada clue*/
            printError(best);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Menampilkan solusi terbaik yang ditemukan
     * Menampilkan :
     * - Nilai fitness
     * - Total error
     * - Genotype
     * - Generasi ditemukan
     * - Alasan berhenti
     * @param best individu terbaik
     * @param stopReason alasan berhenti
     * @param genFound generasi ditemukan
     */
    static void printFinalSolution(Individual best, String stopReason, int genFound) {
        System.out.println("SOLUSI TERBAIK DITEMUKAN:");
        System.out.println("Fitness     = " + best.fitness);
        System.out.println("Total Error = " + best.totalError);
        System.out.println("Genotype:");

        /**Cetak genotipe individu terbaik*/
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(best.gene[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Generation ditemukan: " + genFound);
        System.out.println("Alasan berhenti: " + stopReason);
    }

    /**
     * Membaca papan mosaic dari file
     * Format file:
     * Baris pertama: dua angka (jumlah baris dan kolom)
     * Baris berikutnya: matriks angka (clue atau -1)
     * @param filename nama file input
     * @throws Exception jika terjadi kesalahan saat membaca file
     */
    static void readMosaic(String filename) throws Exception {
        /**Reader untuk membaca file*/
        BufferedReader buffread = new BufferedReader(new FileReader(filename));

        /**Baca ukuran papan mosaic*/
        String[] size = buffread.readLine().split(" ");
        rows = Integer.parseInt(size[0]);
        cols = Integer.parseInt(size[1]);

        /**Inisialisasi dan baca isi papan mosaic*/
        mosaic = new int[rows][cols];

        /**Loop untuk membaca setiap baris papan mosaic*/
        for (int i = 0; i < rows; i++) {
            String[] line = buffread.readLine().split(" ");
            for (int j = 0; j < cols; j++) {
                mosaic[i][j] = Integer.parseInt(line[j]);
            }
        }
        buffread.close();
    }

    /**
     * Menghitung nilai fitness total individu
     * Fitness total adalah kombinasi dari fitness dasar dan skor pola tertentu
     * @param ind individu yang akan dihitung fitness totalnya
     * @return nilai fitness total
     */
    static double fitnessTotal(Individual ind) {
        /**Hitung fitness dasar individu, berdasarkan error total*/
        double baseFitness = fitness(ind);

        /**Hitung skor pola-pola tertentu untuk meningkatkan fitness*/
        double polaScore =
                pola0(ind) +
                pola4(ind) +
                pola6(ind) +
                pola9(ind);

        /**Kombinasikan fitness dasar dan skor pola untuk mendapatkan fitness total*/
        return baseFitness + 0.01 * polaScore;
    }


    /**
     * Menghitung nilai fitness individu berdasarkan total error terhadap clue mosaic
     * Error dihitung dengan cara:
     * - Untuk setiap clue (nilai >= 0), hitung sel 3x3 di sekitarnya
     * - Bandingkan hasil hitungan dengan clue, hitung selisih absolutnya
     * - Jumlahkan semua selisih untuk mendapatkan total error
     * @param ind individu yang akan dihitung fitnessnya
     * @return nilai fitness individu
     */
    static double fitness(Individual ind) {
        /**Hitung total error berdasarkan perbedaan antara nilai yang dihitung dan clue*/
        int totalError = 0;

        /**Loop untuk setiap sel pada papan mosaic*/
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                /**Ambil clue dari papan mosaic di (i,j)*/
                int expected = mosaic[i][j];
                /**Jika clue valid (>=0), hitung error*/
                if (expected >=0) {
                    /**Hitung nilai aktual dari sel 3x3 di sekitar (i,j)*/
                    int actual = hitung3x3(ind, i, j);
                    /**Tambahkan selisih absolut ke total error*/
                    totalError += Math.abs(actual - expected);
                }
            }
        }
        /**Update total error individu*/
        ind.totalError = totalError;

        /**Konversi error menjadi fitness
         * Error kecil -> fitness besar
         * Error besar -> fitness kecil
        */
        return 1.0 / (1 + totalError);
    }

    /**
     * Menghitung jumlah nilai 1 di area 3x3 sekitar sel (r,c) pada individu
     * @param ind individu yang akan dihitung
     * @param r baris
     * @param c kolom
     * @return jumlah nilai 1 di area 3x3 sekitar (r,c)
     */
    static int hitung3x3(Individual ind, int r, int c) {
        /**Menyimpan jumlah nilai 1 di area 3x3 sekitar sel (r,c) */
        int sum = 0;

        /**Loop untuk setiap sel dalam area 3x3 sekitar (r,c) */
        for (int i = r - 1; i <= r + 1; i++) {
            for (int j = c - 1; j <= c + 1; j++) {
                /**Pastikan index berada dalam batas papan mosaic */
                if (i >= 0 && i < rows && j >= 0 && j < cols) {
                    sum += ind.gene[i][j];
                }
            }
    }
        return sum;
    }

    /**
     * Pola untuk clue 0
     * Jika clue adalah 0, maka di area 3x3 sekitarnya harusnya banyak 0
     * Jika ada 0 di sekitar, tambah skor
     * Jika ada 1 di sekitar, kurangi skor
     * @param ind individu yang akan dihitung polanya
     * @return skor pola untuk clue 0
     */
    static double pola0(Individual ind) {
        /**Skor pola untuk clue 0 */
        double score = 0.0;

        /**Loop untuk setiap sel pada papan mosaic */
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                /**Jika clue adalah 0, evaluasi area 3x3 sekitarnya */
                if (mosaic[r][c] == 0) {
                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {
                            /**Pastikan index berada dalam batas papan mosaic */
                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                /**Jika nilai di area 3x3 adalah 0, tambah skor */
                                if (ind.gene[i][j] == 0) score += 1;
                                /**Jika nilai di area 3x3 adalah 1, kurangi skor */
                                else score -= 1;
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    /**
     * Pola untuk clue 9
     * Jika clue adalah 9, maka di area 3x3 sekitarnya harusnya banyak 1
     * Jika ada 1 di sekitar, tambah skor
     * Jika ada 0 di sekitar, kurangi skor
     * @param ind individu yang akan dihitung polanya
     * @return skor pola untuk clue 9
     */
    static double pola9(Individual ind) {
        /**Skor pola untuk clue 9 */
        double score = 0.0;

        /**Loop untuk setiap sel pada papan mosaic */
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                /**Jika clue adalah 9, evaluasi area 3x3 sekitarnya */
                if (mosaic[r][c] == 9) {
                    /**Loop untuk setiap sel dalam area 3x3 sekitar (r,c) */
                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {
                            /**Pastikan index berada dalam batas papan mosaic */
                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                /**Jika nilai di area 3x3 adalah 1, tambah skor */
                                if (ind.gene[i][j] == 1) score += 1;
                                /**Jika nilai di area 3x3 adalah 0, kurangi skor */
                                else score -= 1;
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    /**
     * Pola untuk clue 4
     * Jika clue adalah 4 di corner, maka di area 3x3 sekitarnya harusnya banyak 1
     * Jika ada 1 di sekitar, tambah skor lebih besar
     * Jika ada 0 di sekitar, kurangi skor lebih besar
     * @param ind individu yang akan dihitung polanya
     * @return skor pola untuk clue 4
     */
    static double pola4(Individual ind) {
        /**Skor pola untuk clue 4 */
        double score = 0.0;

        /**Loop untuk setiap sel pada papan mosaic */
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                /**Jika clue adalah 4 di corner, evaluasi area 3x3 sekitarnya */
                if (mosaic[r][c] == 4 && isCorner(r, c)) {

                    /**Loop untuk setiap sel dalam area 3x3 sekitar (r,c) */
                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {

                            /**Pastikan index berada dalam batas papan mosaic */
                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                /**Jika nilai di area 3x3 adalah 1, tambah skor lebih besar */
                                if (ind.gene[i][j] == 1) {
                                    score += 1;
                                } else { /**Jika nilai di area 3x3 adalah 0, kurangi skor lebih besar */
                                    score -= 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    /**
     * Pola untuk clue 6
     * Jika clue adalah 6 di edge, maka di area 3x3 sekitarnya harusnya banyak 1
     * Jika ada 1 di sekitar, tambah skor lebih besar
     * Jika ada 0 di sekitar, kurangi skor lebih besar
     * @param ind individu yang akan dihitung polanya
     * @return skor pola untuk clue 6
     */
    static double pola6(Individual ind) {
        /**Skor pola untuk clue 6 */
        double score = 0.0;

        /**Loop untuk setiap sel pada papan mosaic */
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                /**Jika clue adalah 6 di edge, evaluasi area 3x3 sekitarnya */
                if (mosaic[r][c] == 6 && isEdge(r, c)) {

                    /**Loop untuk setiap sel dalam area 3x3 sekitar (r,c) */
                    for (int i = r - 1; i <= r + 1; i++) {
                        for (int j = c - 1; j <= c + 1; j++) {

                            /**Pastikan index berada dalam batas papan mosaic */
                            if (i >= 0 && i < rows && j >= 0 && j < cols) {
                                /**Jika nilai di area 3x3 adalah 1, tambah skor lebih besar */
                                if (ind.gene[i][j] == 1) {
                                    score += 1;
                                } else {/**Jika nilai di area 3x3 adalah 0, kurangi skor lebih besar */
                                    score -= 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return score;
    }


    /**
     * Cek apakah posisi (r,c) adalah corner
     * @param r indeks baris
     * @param c indeks kolom
     * @return true jika (r,c) adalah corner, false jika bukan
     */
    static boolean isCorner(int r, int c) {
        return (r == 0 || r == rows - 1) && (c == 0 || c == cols - 1);
    }

    /**
     * Cek apakah posisi (r,c) adalah edge (tapi bukan corner)
     * @param r indeks baris
     * @param c indeks kolom
     * @return true jika (r,c) adalah edge, false jika bukan
     */
    static boolean isEdge(int r, int c) {
        return (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) && !isCorner(r, c);
    }

    /**
     * Melakukan random flip bit pada individu
     * @param ind individu yang akan di-flip
     * @param rng objek Random untuk menghasilkan nilai acak
     */
    static void randomFlipBit(Individual ind, Random rng){
        int r =  rng.nextInt(rows);
        int c = rng.nextInt(cols);
        ind.flip(r, c);
    }

    /**
     * Melakukan guided flip bit pada individu berdasarkan clue yang ada
     * Strategi:
     * - Cari clue dengan error terbesar
     * - Tentukan apakah perlu menambah atau mengurangi nilai aktual
     * - Kumpulkan kandidat sel di area 3x3 yang bisa di-flip
     * - Pilih satu kandidat secara acak dan lakukan flip
     * @param ind individu yang akan di-flip
     * @param rng objek Random untuk menghasilkan nilai acak
     */
    static void guidedFlipBit(Individual ind, Random rng){
        /**Untuk menyimpan clue dengan error terbesar */
        int bestR = -1;
        int bestC = -1;

        /**Nilai clue dan aktual terbaik */
        int bestExpected = 0;
        int bestActual = 0;

        /**Besar error terbaik */
        int bestError = 0;

        /**Loop untuk mencari clue dengan error terbesar */
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                int expected = mosaic[r][c];
                /**Lewati jika bukan clue */
                if(expected<0){
                    continue;
                }

                /**Hitung nilai aktual di area 3x3 sekitar (r,c) */
                int actual = hitung3x3(ind, r, c);
                int err = Math.abs(actual- expected);

                /**Update jika ditemukan error yang lebih besar */
                if(err>bestExpected){
                    bestError = err;
                    bestR = r;
                    bestC = c;
                    bestExpected = expected;
                    bestActual = actual;
                }
            }
        }
        //Jika tidak ada clue dengan error, lakukan random flip
        if(bestError==0 || bestR==-1){
            randomFlipBit(ind, rng);
            return;
        }

        //Tentukan apakah perlu menambah atau mengurangi nilai aktual
        boolean needDecrease = (bestActual>bestExpected);
        boolean needIncrease = (bestActual<bestExpected);

        //Menyimpan kandidat sel yang bisa di-flip
        ArrayList<int[]> candidates = new ArrayList<>();

        //Loop area 3x3 sekitar clue dengan error terbesar
        for(int r = bestR - 1; r <= bestR+1; r++){
            for (int c = bestC - 1; c <= bestC + 1; c++) {
                //Pastikan index valid
                if (r < 0 || r >= rows || c < 0 || c >= cols) {
                    continue;
                }
                int val = ind.gene[r][c];
                //Jika perlu decrease dan nilai 1, atau perlu increase dan nilai 0, tambahkan ke kandidat
                if(needDecrease && val==1){
                    candidates.add(new int[]{r, c}); //flip 1 - 0
                }else if(needIncrease && val==0){//Jika perlu increase jumlah 1, cari sel bernilai 0
                    candidates.add(new int[]{r, c}); //flip 0 - 1
                }
            }
        }
        //Jika tidak ada kandidat, lakukan random flip
        if(candidates.isEmpty()){
            randomFlipBit(ind, rng);
            return;
        }

        //Pilih satu kandidat secara acak dan lakukan flip
        int pick = rng.nextInt(candidates.size());
        int[] cell = candidates.get(pick);
        ind.flip(cell[0], cell[1]);

    }

    /**
     * Melakukan uniform crossover antara dua parent untuk menghasilkan satu child
     * Setiap gene di child diambil secara acak dari parent1 atau parent2
     * @param p1 Parent pertama
     * @param p2 Parent kedua
     * @param rng objek Random untuk menghasilkan nilai acak
     * @return Child hasil crossover
     */
    static Individual uniformCrossover(Individual p1, Individual p2, Random rng){

        Individual child = new Individual();
        /**Loop untuk setiap gene pada individu */
        for(int i = 0 ; i < rows ; i++){
            for(int j = 0 ; j < cols ; j++){
                /**Pilih secara acak dari parent1 atau parent2 */
                if(rng.nextBoolean()){
                    child.gene[i][j] = p1.gene[i][j];
                } else {
                    child.gene[i][j] = p2.gene[i][j];
                }
            }
        }
        return child;
    }

    /**
     * Membuat salinan mendalam (deep copy) dari individu
     * @param src individu sumber yang akan di-copy
     * @return individu hasil salinan
     */
    static Individual deepCopy(Individual src) {
        Individual dst = new Individual();
        for (int r = 0; r < rows; r++) {
            System.arraycopy(src.gene[r], 0, dst.gene[r], 0, cols);
        }
        return dst;
    }

    /**
     * Melakukan rectangle crossover antara dua parent untuk menghasilkan dua child
     * Sebuah rectangle acak dipilih, dan isi gene di dalam rectangle ditukar antara kedua parent
     * @param parent1 Parent pertama
     * @param parent2 Parent kedua
     * @param rng objek Random untuk menghasilkan nilai acak
     * @return Array berisi dua child hasil crossover
     */
    static Individual[] rectangleCrossover(Individual parent1, Individual parent2, Random rng) {

        /**Buat salinan mendalam dari kedua parent untuk menjadi child */
        Individual child1 = deepCopy(parent1);
        Individual child2 = deepCopy(parent2);

        //Pilih 2 baris acak untuk membentuk batas rectangle
        int r1 = rng.nextInt(rows);
        int r2 = rng.nextInt(rows);
        if (r2 < r1) { int tmp = r1; r1 = r2; r2 = tmp; }

        //Pilih 2 kolom acak untuk membentuk batas rectangle
        int c1 = rng.nextInt(cols);
        int c2 = rng.nextInt(cols);
        if (c2 < c1) { int tmp = c1; c1 = c2; c2 = tmp; }

        //Tukar isi gene di dalam rectangle
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                /**Tukar isi gene di dalam rectangle */
                int temp = child1.gene[r][c];
                child1.gene[r][c] = child2.gene[r][c];
                child2.gene[r][c] = temp;
            }
        }

        return new Individual[]{child1, child2};
    }

    /**
     * Melakukan evolusi populasi ke generasi berikutnya
     * Menggunakan seleksi, crossover, dan mutasi untuk menghasilkan populasi baru
     * @param pop populasi saat ini
     * @param rng objek Random untuk operasi acak
     * @return populasi baru hasil evolusi
     */
    static Population evolvePopulation(Population pop, Random rng){
        /**Inisialisasi populasi baru */
        Population newPop = new Population(pop.individuals.length, rng, false);
        
        /**Elitism: salin individu terbaik ke populasi baru */
        int elitism = 1;

        /**Salin individu terbaik */
        newPop.individuals[0] = new Individual(pop.getFittest());

        /**Loop untuk mengisi populasi baru */
        for(int i = elitism; i<newPop.individuals.length; i++){
            /**Pilih dua parent menggunakan metode tournament */
            // Individual parent1 = pop.tournamentIndividual(rng, 5);
            // Individual parent2 = pop.tournamentIndividual(rng, 5);

            /**Menggunakan roulette wheel selection sebagai metode seleksi */
            Individual parent1 = pop.rouletteWheelSelection(rng);
            Individual parent2 = pop.rouletteWheelSelection(rng);

            /**Inisialisasi child */
            Individual child;

            /**Lakukan crossover berdasarkan probabilitas crossoverRate */
            if (rng.nextDouble()<crossoverRate) {
                // PILIHAN CROSSOVER:

                //1. Uniform Crossover (default, per-gene ambil dari parent1/parent2 secara acak)
                //Individual child = uniformCrossover(parent1, parent2, rng);

                //2. Rectangle Crossover (swap satu blok persegi panjang antara parent1 & parent2)
                Individual[] children = rectangleCrossover(parent1, parent2, rng);
                child = rng.nextBoolean() ? children[0] : children[1];
            }
            else{
                //jika tidak crossover, child adalah copy dari parent1
                child = new Individual(rng.nextBoolean() ? parent1 : parent2);
            }

            // 1. Random Flip Bit Mutation (default)
            // child.mutate(rng, mutationRate);

            // 2. Guided Mutation (menggunakan clue untuk memilih bit yang akan di-flip)
            child.guidedMutate(rng, mutationRate);

            //Update fitness child setelah mutasi
            child.updateFitness();

            //Masukkan child ke populasi baru
            newPop.individuals[i] = child;
        }
        return newPop;
    }

    /**
     * Menampilkan error pada clue dalam format papan mosaic
     * Clue yang terhitung benar ditampilkan apa adanya
     * @param ind individu yang akan ditampilkan errornya
     */
    static void printError(Individual ind){
        System.out.println("====================");
        /**Loop untuk mencetak error pada clue dalam format papan mosaic */
        for(int i = 0 ; i < rows ; i++){
            for(int j = 0 ; j < cols ; j++){
                /**Ambil clue dari papan mosaic di (i,j) */
                int expected = mosaic[i][j];
                /**Jika nilai clue adalah -1, cetak -1 */
                if (expected==-1) {
                    System.out.print("-1 ");
                }
                /**Jika clue valid, hitung nilai aktual dan bandingkan */
                else{
                    int actual = hitung3x3(ind, i, j);
                    /**Jika aktual sama dengan expected, cetak aktual*/
                    if (actual == expected) {
                        System.out.print(actual + " ");
                    }
                    /**Jika tidak sama, cetak expected dengan tanda X */
                     else {
                        System.out.print(expected + "X ");
                    }
                }
            }
            System.out.println();
        }
    }
}
