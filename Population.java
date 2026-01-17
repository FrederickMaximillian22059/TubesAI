import java.util.Random;

public class Population {
    Individual[] individuals;

    //jika init true, maka population akan diinisialisasi dengan individu acak
    public Population(int size, Random rand, boolean init){
        individuals = new Individual[size];
        
        if(init){
            for(int i = 0 ; i < size ; i++){
                Individual newInd = new Individual();
                newInd.randomInit(rand);
                newInd.updateFitness();
                individuals[i] = newInd;
            }
        }
    }

    //method untuk mendapatkan individu terbaik di population
    public Individual getFittest(){
        Individual best = individuals[0];
        for(int i = 1 ; i < individuals.length ; i++){
            if(individuals[i].compareTo(best) < 0){
                best = individuals[i];
            }
        }
        return best;
    }

    //method untuk melakukan tournament selection
    public Individual tournamentIndividual(Random rand, int tournamentSize){
        Population tournament = new Population(tournamentSize, rand, false);
        for(int i = 0 ; i < tournamentSize ; i++){
            int randomIndex = rand.nextInt(individuals.length);
            tournament.individuals[i] = new Individual(individuals[randomIndex]);
        }
        return tournament.getFittest();
    }
}
