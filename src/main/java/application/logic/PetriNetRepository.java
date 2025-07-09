package application.logic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class PetriNetRepository {

    private Map<String, PetriNet> petriNets = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final File file = new File("src/main/resources/data/petriNetRepository.json");

    public PetriNetRepository() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        if (!file.exists() || file.length() == 0) {
            try {
                createFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadPetriNets();
        }
    }

    private void loadPetriNets() {
        if (!file.exists() || file.length() == 0) return;

        try {
            petriNets = mapper.readValue(
                    file,
                    mapper.getTypeFactory().constructMapType(HashMap.class, String.class, PetriNet.class)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePetriNets() {
        try {
            mapper.writeValue(file, petriNets);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile() throws IOException {
        mapper.writeValue(file, petriNets);
    }

    public Map<String, PetriNet> getPetriNets() {
        return petriNets;
    }

    public void savePetriNet(PetriNet net) {
        String id = net.getId();
        // Sovrascrive se l'ID esiste, altrimenti aggiunge
        petriNets.put(id, net);
        savePetriNets();
    }

    public void deletePetriNet(String id) {
        if (petriNets.containsKey(id)) {
            petriNets.remove(id);
            savePetriNets();
        }
    }


}
