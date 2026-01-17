import java.util.Random;
//pakai comparable supaya saya bisa cari best individunya, membantu saat melakukan elitsm, dan di tournament selection jdi lebih mudah
public class Individual implements Comparable<Individual>{//Ini untuk mapping individu
    int[][] gene;
    double fitness = 0.0;
    int totalError = Integer.MAX_VALUE;


    public Individual(){
        gene = new int[MosaicGA.rows][MosaicGA.cols];
    }

    public Individual(Individual other) {
        //inisialisasi gene kosong biar ga share reference sama parentnya
        gene = new int[MosaicGA.rows][MosaicGA.cols];
        //ini buat mengcopynya
        for(int i = 0 ; i < MosaicGA.rows ; i++){
            for(int j = 0 ; j < MosaicGA.cols ; j++){
                this.gene[i][j] = other.gene[i][j];
            }
        }

        //copy nilai evaluasinya
        this.fitness = other.fitness;
        this.totalError = other.totalError;
    }

    //dipakai untuk membuat kandidat solusi awal saja 
    public void randomInit(Random rng){
        for(int i = 0 ; i < MosaicGA.rows ; i++){
            for(int j = 0 ; j < MosaicGA.cols; j++){
                gene[i][j] = rng.nextBoolean() ? 0 : 1;
            }
        }
    }

    //untuk mutasi pakai bit-flip mutation 
    public void flip(int r, int c){
        gene[r][c] = 1 - gene[r][c];
    }

    //ini method mutasinya 
    public void mutate(Random rng, double mutationRate){
        //kita harus check dulu apakah boleh di mutasi atau tidak
        if(rng.nextDouble() < mutationRate){
            int r = rng.nextInt(MosaicGA.rows);
            int c = rng.nextInt(MosaicGA.cols);
            flip(r, c);
        }
    }

    //method untuk mengupdate fitness setiap kali muncul individu baru
    public double updateFitness(){
        this.fitness = MosaicGA.fitnessTotal(this);
        return this.fitness;
    }

    @Override
    public int compareTo(Individual other){
        return Double.compare(other.fitness, this.fitness);
    }
}
