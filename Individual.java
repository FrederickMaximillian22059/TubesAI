import java.util.Random;
/**
 * Class Individual merepresentasikan satu kandidat solusi dalam algoritma genetika
 * Setiap individu memiliki representasi genetik berupa matriks 2D (int[][] gene)
 * nilai fitness yang menunjukkan seberapa baik individu tersebut
 * dan totalError yang menunjukkan total error dari individu tersebut terhadap mosaic yang ada
 * 
 */
public class Individual implements Comparable<Individual>{
    /**
     * Matriks 2D yang merepresentasikan gen individu (solusi)
     * Ukuran mengikuti ukuran mosaic yang diinput
     */
    int[][] gene;

    /**
     * Nilai fitness individu
     * Semakin besar nilai fitness, semakin baik kualitas individu
     */
    double fitness = 0.0;

    /** Total error individu terhadap mosaic yang ada
     * Semakin kecil total error, semakin baik kualitas individu
     */
    int totalError = Integer.MAX_VALUE;

    /**
     * Konstruktor
     * Digunakan untuk membuat individu baru dengan gene kosong
     */
    public Individual(){
        gene = new int[MosaicGA.rows][MosaicGA.cols];
    }

    /**
     * Konstruktor untuk mengcopy individu lain
     * @param other individu yang akan di-copy
     */
    public Individual(Individual other) {
        gene = new int[MosaicGA.rows][MosaicGA.cols];
        //Seluruh nilai gene di-copy dari individu lain
        for(int i = 0 ; i < MosaicGA.rows ; i++){
            for(int j = 0 ; j < MosaicGA.cols ; j++){
                this.gene[i][j] = other.gene[i][j];
            }
        }

        //Copy nilai evaluasinya
        this.fitness = other.fitness;
        this.totalError = other.totalError;
    }

    /**
     * Digunakan untuk menginisialisasi individu dengan nilai gene acak (0 atau 1)
     * @param rng objek Random untuk menghasilkan nilai acak
     */
    public void randomInit(Random rng){
        //Loop untuk mengisi matriks gene dengan nilai acak 0 atau 1
        for(int i = 0 ; i < MosaicGA.rows ; i++){
            for(int j = 0 ; j < MosaicGA.cols; j++){
                //Jika random menghasilkan true, maka gene di posisi (i,j) diisi 1, else diisi 0
                gene[i][j] = rng.nextBoolean() ? 1 : 0;
            }
        }
    }

    /**
     * Melakukan flip pada bit di posisi (r,c) (satu gene)
     * Jika sebelumnya 0, maka jadi 1, dan sebaliknya
     * @param r indeks baris gene
     * @param c indeks kolom gene
     */
    public void flip(int r, int c){
        gene[r][c] = 1 - gene[r][c];
    }

    /**
     * Melakukan mutasi pada individu dengan probabilitas tertentu
     * Hanya terjadi jika nilai random lebih kecil dari mutationRate
     * @param rng objek Random untuk menghasilkan nilai acak
     * @param mutationRate probabilitas mutasi (antara 0 dan 1)
     */
    public void mutate(Random rng, double mutationRate){
        //Cek apakah individu ini akan dimutasi atau tidak
        if(rng.nextDouble() < mutationRate){
            int r = rng.nextInt(MosaicGA.rows);
            int c = rng.nextInt(MosaicGA.cols);
            flip(r, c);
        }
    }

    /**
     * Melakukan guided mutation pada individu dengan probabilitas tertentu
     * Mutasi diarahkan ke gene yang memiliki kontribusi error terbesar
     * @param rng objek Random untuk menghasilkan nilai acak
     * @param mutationRate probabilitas mutasi (antara 0 dan 1)
     */
    public void guidedMutate(Random rng, double mutationRate){
        //Cek apakah individu ini akan dimutasi atau tidak
        if(rng.nextDouble() < mutationRate){
            MosaicGA.guidedFlipBit(this, rng);
        }
    }

    /**
     * Menghitung dan memperbarui nilai fitness individu
     * @return nilai fitness yang telah diperbarui
     */
    public double updateFitness(){
        this.fitness = MosaicGA.fitnessTotal(this);
        return this.fitness;
    }

    /**
     * Membandingkan individu ini dengan individu lain berdasarkan nilai fitness
     * Dipakai untuk mencari individu terbaik dalam populasi
     * @param other individu lain yang akan dibandingkan
     * @return nilai negatif jika individu ini lebih baik, positif jika lebih buruk
     */
    @Override
    public int compareTo(Individual other){
        return Double.compare(other.fitness, this.fitness);
    }
}
