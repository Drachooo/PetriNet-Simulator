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

    private PetriNetRepository() {
        mapper.registerModule(new JavaTimeModule()); // supporto serializzazione date
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // evita timestamp array

        if (!file.exists() || file.length() == 0) {
            try {
                createFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPetriNets() {
        /* Se il file non esiste oppure è vuoto non succede niente sium*/
        if(!file.exists() || file.length() == 0)
            return;

        /*Altrimenti carico le mappe, "costruendo" l'hashmap che associa l'id (stringa) alla singola PetriNet*/
        try{
            petriNets=mapper.readValue(file, mapper.getTypeFactory().constructMapType(HashMap.class, String.class,PetriNet.class));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void savePetriNets() {
        try{
            //Aggiorno il file JSON
            mapper.writeValue(file,petriNets);
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    private void createFile() throws IOException {
        mapper.writeValue(file, petriNets); /* Se il file non esiste o è vuoto creo un file con una mappa vuota*/
    }
}
