package searchengine.dto.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Component
public class Lemmatizer {
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС", "ЧАСТ"};

    public HashMap<String, Integer> getLemmas (String textHTML){
        HashMap<String, Integer> lemmasMap = new HashMap<>();
        LuceneMorphology luceneMorphology;
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String text = Jsoup.parse(textHTML).text();
        String[] textArray = arrayRussianWords(text);
        for (String word : textArray) {
            if (word.isBlank()) continue;

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if(anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) continue;

            String lemma = normalForms.get(0);
            if (lemmasMap.containsKey(lemma) ) {
                int count = lemmasMap.get(lemma) + 1;
                lemmasMap.replace(lemma, count);
            } else lemmasMap.put(lemma, 1);
        }
        return lemmasMap;
    }
    public String[] arrayRussianWords(String text){
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }
    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }
    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

}
