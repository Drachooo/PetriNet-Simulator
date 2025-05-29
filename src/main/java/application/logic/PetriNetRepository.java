package application.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PetriNetRepository {

    private Map<String,PetriNet> repo=new HashMap<>();

    private Map<List<String>, PetriNet> subscribers=new HashMap<>();




    private final File file=new File("src/main/resources/data/petriNetRepository.csv");

    private PetriNetRepository(){

        if (!file.exists() || file.length() == 0) {
            try {
                createFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void createFile() throws IOException {
        try(BufferedReader writer=new BufferedReader(new FileReader(file))){
            writeHeader(writer);
        }
    }

    private void writeHeader(BufferedReader writer) {
        String header=""
    }

}