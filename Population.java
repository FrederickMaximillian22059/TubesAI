import java.util.Random;

/**
 * Class Population merepresentasikan satu populasi
 * yang berisi sekumpulan individu (Individual[])
 * 
 * Populasi bertanggung jawab untuk mengelola individu-individu
 * seperti inisialisasi, seleksi individu terbaik, dan metode seleksi
 */
public class Population {
    /**
     * Array yang menyimpan individu-individu dalam populasi
     */
    Individual[] individuals;

    /**
     * Kosntruktor Population
     * @param size jumlah individu dalam populasi
     * @param rand objek Random untuk inisialisasi acak
     * @param init jika true, populasi akan diinisialisasi dengan individu acak
     */
    public Population(int size, Random rand, boolean init){
        individuals = new Individual[size];
        
        //Inisialisasi populasi dengan individu acak jika init = true
        if(init){
            for(int i = 0 ; i < size ; i++){
                Individual newInd = new Individual();
                newInd.randomInit(rand);
                newInd.updateFitness();
                individuals[i] = newInd;
            }
        }
    }

    /**
     * Mengembalikan individu dengan nilai fitness terbaik dalam populasi
     * @return individu dengan fitness terbaik
     */
    public Individual getFittest(){
        Individual best = individuals[0];
        //Loop untuk mencari individu dengan fitness terbaik
        for(int i = 1 ; i < individuals.length ; i++){
            if(individuals[i].compareTo(best) < 0){
                best = individuals[i];
            }
        }
        return best;
    }

    /**
     * Melakukan tournament selection untuk memilih individu terbaik dari subset populasi
     * Beberapa individu dipilih secara acak, 
     * lalu individu dengan fitness terbaik akan di return
     * @param rand objek Random untuk pemilihan acak
     * @param tournamentSize ukuran subset populasi untuk tournament
     * @return individu terbaik dari subset yang dipilih
     */
    public Individual tournamentIndividual(Random rand, int tournamentSize){
        Population tournament = new Population(tournamentSize, rand, false);
        //Pilih individu secara acak untuk dimasukkan ke dalam tournament
        for(int i = 0 ; i < tournamentSize ; i++){
            int randomIndex = rand.nextInt(individuals.length);
            tournament.individuals[i] = new Individual(individuals[randomIndex]);
        }
        //Kembalikan individu terbaik dari tournament
        return tournament.getFittest();
    }

    /**
     * Melakukan roulette wheel selection untuk memilih individu
     * Probabilitas terpilihnya individu sebanding dengan nilai fitnessnya
     * @param rand objek Random untuk pemilihan acak
     * @return individu yang dipilih berdasarkan roulette wheel selection
     */
    public Individual rouletteWheelSelection(Random rand){
        double minFitness = Double.MAX_VALUE;

        //Cari nilai fitness minimum dalam populasi
        for(Individual ind : individuals){
            if(ind.fitness < minFitness){
                minFitness = ind.fitness;
            }
        }

        //Shift digunakan untuk menghindari nilai fitness negatif
        double shift = 0.0;
        if(minFitness < 0){
            shift = -minFitness;
        }

        double totalFitness = 0.0;

        //Hitung total fitness setelah dishift
        for(Individual ind : individuals){
            totalFitness += ind.fitness + shift;
        }

        //Memilih titik acak pada roulette wheel
        double randomPoint = rand.nextDouble() * totalFitness;
        double cumulativeFitness = 0.0;

        //Cari individu yang sesuai dengan titik acak
        for(Individual ind : individuals){
            cumulativeFitness += ind.fitness + shift;
            if(cumulativeFitness >= randomPoint){
                return new Individual(ind);
            }
        }

        //Fallback, kembalikan individu terakhir jika tidak ada yang terpilih
        return new Individual(individuals[individuals.length - 1]);
    }
}
