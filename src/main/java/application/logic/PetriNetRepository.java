package application.logic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
public class PetriNetRepository {

    private Map<String, PetriNet> repo;

    public PetriNetRepository() {
        this.repo=new HashMap<String, PetriNet>();
    }


    public void add(PetriNet net) {
        if(repo.containsKey(net.getId()))
            throw new IllegalArgumentException("PetriNet already exists");
        repo.put(net.getId(),net);
    }

    public void remove(String id) {
        repo.remove(id);
    }

    public PetriNet get(String id){
        return repo.get(id);
    }

    public Collection<PetriNet> getAll(){
        return Collections.unmodifiableCollection(repo.values());
    }
}