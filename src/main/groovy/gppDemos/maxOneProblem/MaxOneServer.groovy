package gppDemos.maxOneProblem


import gppDemos.UniversalResponse
import gppLibrary.DataClass

class MaxOneServer extends DataClass{
    List <MaxOneIndividual> population = []
    Double requiredFitness = 0.0D
    Double worstFitness = 0.0D
    Double bestFitness = 1.0D
    int worstLocation, bestLocation
    long seed = 0L
    static Random rng = new Random()

    static int requestedParents = 0
    static int improvements = 0
    static int modifications = 0
    static int bitsPerGene = 0
    static float editProportion = 0.0F

    static String selectParentsFunction = "selectParents"
    static String incorporateChildrenMethod = "addChildren"
    static String addIndividualsMethod = "addIndividuals"
    static String carryOnFunction = "carryOn"
    static String initMethod = "initialise"
    static String finaliseMethod = "finalise"

    int initialise (List d) {
        seed = (long)d[0]
        bitsPerGene = (int)d[1]
        editProportion = (float)d[2]
        rng.setSeed(seed)
        return completedOK
    }

    UniversalResponse selectParents(int parents) {
        requestedParents = requestedParents + parents // for analysis
        def response = new UniversalResponse()
        for ( i in 0 ..< parents) {
            int p = rng.nextInt(population.size())
            response.payload[i] = population[p]
        }
        return response
    }

    int addChildren(List <MaxOneIndividual> children) {
        boolean childAdded = false
        for ( c in 0 ..< children.size()) {
            MaxOneIndividual child = children[c]
            // only add child if it is better than the worst child in the population
            if (child.fitness < worstFitness) {
                childAdded = true
                improvements = improvements + 1 // for analysis
//                print "improvement $improvements with fit $child.fitness after $requestedParents parent requests"
                worstFitness = child.fitness
                population[worstLocation] = child
                // new child could be better than the current best
                if (child.fitness < bestFitness) {
                    bestFitness = child.fitness
                    bestLocation = worstLocation
//                    print " new best"
//                    println "$child.fitness after ${requestedParents/2} evolutions "
                }
                // now update minFitness
                worstFitness = bestFitness
                for ( p in 0 ..< population.size()) {
                    if (population[p].fitness > worstFitness) {
                        // found a new minimum fitness
                        worstFitness = population[p].fitness
                        worstLocation = p
                    }
                }
//                println " $worstFitness, $bestFitness"
            } // end if
        } // end for loop
        if (childAdded) {
            if (bestFitness == worstFitness)
                editPopulation()            
        }        
        return completedOK        
    }

    void editPopulation(){
        int populationSize = population.size()
        int editNumber = (int)(populationSize * editProportion)
        for ( c in 1 .. editNumber) {
            int id = rng.nextInt(populationSize)
//            print "$c = $id: ${population[id]} ->"
            int m1 = rng.nextInt(bitsPerGene) + 1
            ((MaxOneIndividual)population[id]).gene.flip(m1)
            ((MaxOneIndividual)population[id]).fitness = ((MaxOneIndividual)population[id]).doFitness(((MaxOneIndividual)population[id]).gene)
//            println "$m1, $m2, ${population[id]}"
        }
        determineBestWorst()
        modifications += 1
    }
    
    void determineBestWorst(){
        bestFitness = 1.0D
        worstFitness = 0.0D
        for ( p in 0..< population.size()) {
            if (population[p].fitness < bestFitness) {
                // update max fitness data
                bestFitness = population[p].fitness
                bestLocation = p
            }
            if (population[p].fitness > worstFitness) {
                // update min fitness data
                worstFitness = population[p].fitness
                worstLocation = p
            }
        }
    }

    int addIndividuals(List <MaxOneIndividual> individuals) {
        //add the new individuals to the population
        for ( i in 0 ..< individuals.size()) {
            population.add(individuals[i])
        }
        determineBestWorst()
        return completedOK
    }

    boolean carryOn() { // returns true if the server should continue
        if ( bestFitness != requiredFitness)
            return true
        else
            return false
    }

    int finalise(List d) {
//        println "Best ${population[bestLocation]}"
//        println "$requestedParents parents requested; creating $improvements improvements"
        print "$requestedParents, $improvements, $modifications, "
        return completedOK
    }

}
