package searchengine.dto.lemmatizer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LemmatizerTest {
    @Autowired
    private Lemmatizer lemmatizer;

    @Test
    void getLemmas() {
        String textHTML = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает " +
                "в некоторых районах Северного Кавказа.";

        HashMap<String, Integer> lemmasTest = new HashMap<>();
        lemmasTest.put("повторный", 1);
        lemmasTest.put("появление", 1);
        lemmasTest.put("постоянно", 1);
        lemmasTest.put("позволять", 1);
        lemmasTest.put("предположить", 1);
        lemmasTest.put("северный", 1);
        lemmasTest.put("район", 1);
        lemmasTest.put("кавказ", 1);
        lemmasTest.put("осетия", 1);
        lemmasTest.put("леопард", 2);
        lemmasTest.put("обитать", 1);

        HashMap<String, Integer> actual = lemmatizer.getLemmas(textHTML);

        assertEquals(lemmasTest, actual);
        assertEquals(2, actual.get("леопард"));
        assertEquals(12, actual.values().stream().mapToInt(i->i).sum());
    }
}