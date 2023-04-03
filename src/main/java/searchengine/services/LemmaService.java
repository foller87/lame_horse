package searchengine.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Data
public class LemmaService {
    private final LemmaRepository lemmaRepository;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС", "ЧАСТ"};

    public HashMap<String, Integer> getLemmasFromHTML(String textHTML){
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
    public List<Lemma> saveAllLemmas(List<Lemma> lemmas) {
        return lemmaRepository.saveAllAndFlush(lemmas);
    }
    public Lemma saveLemma(Site site, String lemmaName){
        List<Lemma> lemmaList = lemmaRepository.findByLemma(lemmaName);
        Lemma lemma;
        if (!lemmaList.isEmpty()) {
            lemma = lemmaList.get(0);
            lemmaRepository.delete(lemma);
            lemma.setFrequency(lemma.getFrequency() + 1);
        } else {
            lemma = Lemma.builder().site(site).lemma(lemmaName).frequency(1).build();
        }
        lemma = lemmaRepository.save(lemma);
        return lemma;
    }
}
