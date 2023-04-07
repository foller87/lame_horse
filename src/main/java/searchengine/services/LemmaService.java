package searchengine.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Data
@Slf4j
public class LemmaService {
    private final LemmaRepository lemmaRepository;
    private final IndexService indexService;
    private static final String[] particlesNames = new String[]{"ПРЕДЛ", "СОЮЗ", "ЧАСТ", "МС-П"};
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";

    public HashMap<String, Integer> getLemmasFromHTML(String textHTML) {
        HashMap<String, Integer> lemmasMap = new HashMap<>();
        String text = Jsoup.parse(textHTML).text();
        String[] textArray = arrayRussianWords(text);
        LuceneMorphology luceneMorphology;
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            log.error("luceneMorphology не запустился" + e);
            throw new RuntimeException(e);
        }
        for (String word : textArray) {
            if (!word.isBlank() && isCorrectWordForm(word, luceneMorphology)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;
                }

                List<String> normalForms = luceneMorphology.getNormalForms(word);
                if (normalForms.isEmpty()) continue;

                String lemma = normalForms.get(0);
                if (lemmasMap.containsKey(lemma)) {
                    int count = lemmasMap.get(lemma) + 1;
                    lemmasMap.replace(lemma, count);
                } else lemmasMap.put(lemma, 1);
            }
        }
        return lemmasMap;
    }

    private String[] arrayRussianWords(String text) {
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

    private boolean isCorrectWordForm(String word, LuceneMorphology luceneMorphology) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }

    private Lemma saveLemma(Site site, Map.Entry<String, Integer> lemmaEntry) {
        List<Lemma> lemmaList = lemmaRepository.findByLemma(lemmaEntry.getKey());
        Lemma lemma;
        if (!lemmaList.isEmpty()) {
            lemma = lemmaList.get(0);
            lemma.setFrequency(lemma.getFrequency() + 1);
        } else {
            lemma = Lemma.builder().site(site).lemma(lemmaEntry.getKey()).frequency(1).build();
        }
        lemma = lemmaRepository.save(lemma);

        return lemma;
    }
    public void saveLemmasByPageAndSite(Site site, Page page, boolean flag){
        HashMap<String, Integer> lemmasMap = getLemmasFromHTML(page.getContent());
        for(Map.Entry<String, Integer> entry : lemmasMap.entrySet()){
            if (flag) return;
            Lemma lemma = saveLemma(site, entry);
            indexService.saveIndexInRepository(page, lemma, entry.getValue());
        }
    }

}
