package searchengine.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.dto.search.OwnText;
import searchengine.model.Index;
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
public class LemmaServiceImpl implements LemmaService {
    private final LemmaRepository lemmaRepository;
    private final IndexService indexService;
    private static final String[] particlesNames = new String[]{"ПРЕДЛ", "СОЮЗ", "ЧАСТ", "МС-П"};
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private double percentageOfLemmaElimination = 0.7;
    private int minimumNumberOfLemmas = 5;

    @Override
    public HashMap<String, Integer> getLemmasFromHTML(String textHTML) {
        HashMap<String, Integer> lemmasMap = new HashMap<>();
        String text = Jsoup.parse(textHTML).text();
        String[] textArray = arrayRussianWords(text);
        LuceneMorphology luceneMorphology = getLuceneMorphology();
        for (String word : textArray) {
            if (word.length() > 2 && !word.isBlank() && isCorrectWordForm(word, luceneMorphology)) {
                String lemma = getLemmaFromTheWord(word);
                if (lemma.isBlank()) continue;
                if (lemmasMap.containsKey(lemma)) {
                    int count = lemmasMap.get(lemma) + 1;
                    lemmasMap.replace(lemma, count);
                } else lemmasMap.put(lemma, 1);
            }
        }
        return lemmasMap;
    }

    private String[] getArrayWords(String text) {
        String newText = Jsoup.parse(text).text();
        return arrayRussianWords(newText);
    }

    private LuceneMorphology getLuceneMorphology() {
        LuceneMorphology luceneMorphology;
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            log.error("luceneMorphology не запустился" + e);
            throw new RuntimeException(e);
        }
        return luceneMorphology;
    }

    private String getLemmaFromTheWord(String word) {
        LuceneMorphology luceneMorphology = getLuceneMorphology();
        String lemma = "";
        List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
        if (anyWordBaseBelongToParticle(wordBaseForms)) {
            return lemma;
        }
        List<String> normalForms = luceneMorphology.getNormalForms(word);
        if (normalForms.isEmpty()) return lemma;
        lemma = normalForms.get(0);
        return lemma;
    }

    @Override
    public String getFragmentText(String contentText, List<Lemma> queryLemmas) {
        String[] textContentArray = getArrayWords(contentText);
        LuceneMorphology luceneMorphology = getLuceneMorphology();
        List<String> nameOfTheLemmasFromTheQuery = new ArrayList<>();
        queryLemmas.forEach(lemma -> nameOfTheLemmasFromTheQuery.add(lemma.getLemma()));
        StringBuilder builder = new StringBuilder();
        OwnText ownText = new OwnText();
        for (String word : textContentArray) {
            if (!word.isBlank() && isCorrectWordForm(word, luceneMorphology)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) continue;

                List<String> normalForms = luceneMorphology.getNormalForms(word);
                if (normalForms.isEmpty()) continue;

                String lemma = normalForms.get(0);
                int beginIndex;
                if (nameOfTheLemmasFromTheQuery.contains(lemma)) {
                    if (builder.length() == 0) {
                        beginIndex = contentText.toLowerCase(Locale.ROOT).indexOf(word) - word.length();
                        builder.append(ownText.getFragment(contentText, beginIndex));
                        return highlightingText(builder.toString(), queryLemmas);
                    }
                }
            }
        }
        return builder.toString();
    }
    private String highlightingText(String fragmentText, List<Lemma> queryLemmas) {
        String text = Jsoup.parse(fragmentText).text();
        String[] textArray = arrayRussianWords(text);
        List<String> nameOfTheLemmasFromTheQuery = new ArrayList<>();
        queryLemmas.forEach(lemma -> nameOfTheLemmasFromTheQuery.add(lemma.getLemma()));
        StringBuilder builderBolderText = new StringBuilder();
        String builderOldText = "";
        int startIndex;
        int endIndex = 0;
        LuceneMorphology luceneMorphology = getLuceneMorphology();
        for (String word : textArray) {
            if (!word.isBlank() && isCorrectWordForm(word, luceneMorphology)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) continue;

                List<String> normalForms = luceneMorphology.getNormalForms(word);
                if (normalForms.isEmpty()) continue;

                String lemma = normalForms.get(0);
                if (nameOfTheLemmasFromTheQuery.contains(lemma)) {

                    if (builderBolderText.length() != 0)
                        fragmentText = fragmentText.substring(builderOldText.length());

                        startIndex = fragmentText.toLowerCase(Locale.ROOT).indexOf(word);
                        endIndex = startIndex + word.length();
                        String newText = fragmentText.substring(startIndex, endIndex);
                        builderOldText = fragmentText.substring(0, endIndex);
                        builderBolderText.append(fragmentText, 0, startIndex)
                                .append("<b>").append(newText).append("</b>");
                }
            }
        }
        if (builderBolderText.length() < fragmentText.length())
            builderBolderText.append(fragmentText.substring(endIndex));
        return builderBolderText.toString();
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
        return lemma;
    }

    @Override
    public void saveLemmasByPageAndSite(Site site, Page page, boolean flag) {
        HashMap<String, Integer> lemmasMap = getLemmasFromHTML(page.getContent());
        List<Index> indexList = new ArrayList<>();
        List<Lemma> lemmaList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            if (flag) return;
            Lemma lemma = saveLemma(site, entry);
            lemmaList.add(lemma);
            indexList.add(Index.builder().page(page).lemma(lemma).rank(entry.getValue()).build());
        }
        lemmaRepository.saveAllAndFlush(lemmaList);
        indexService.saveAllInAndFlush(indexList);
    }

    @Override
    public List<Lemma> getLemmasByQuery(String query) {
        HashMap<String, Integer> lemmaMap = getLemmasFromHTML(query);
        List<Lemma> lemmaList = new LinkedList<>();
        lemmaMap.keySet().forEach(lemma -> {
            List<Lemma> lemmas = lemmaRepository.findByLemma(lemma);
            if (!lemmas.isEmpty()) lemmaList.add(lemmas.get(0));
        });
        return filterPercentageOfLemmaElimination(lemmaList);
    }

    //TODO: Если размер коллекции лемм больше minimumNumberOfLemmas, то оставить
    // percentageOfLemmaElimination лемм по frequency
    private List<Lemma> filterPercentageOfLemmaElimination(List<Lemma> lemmaList) {

        Map<Integer, List<Lemma>> lemmaMapByFrequency = new TreeMap<>();
        lemmaList.forEach(lemma -> {
            if (lemmaMapByFrequency.containsKey(lemma.getFrequency()))
                lemmaMapByFrequency.get(lemma.getFrequency()).add(lemma);
            else {
                List<Lemma> lemmaList1 = new ArrayList<>();
                lemmaList1.add(lemma);
                lemmaMapByFrequency.put(lemma.getFrequency(), lemmaList1);
            }
        });
        if (lemmaList.size() < minimumNumberOfLemmas) return sortLemma(lemmaMapByFrequency, lemmaList.size());
        else {
            int newSizeSortedSet = (int) (lemmaList.size() * percentageOfLemmaElimination);
            return sortLemma(lemmaMapByFrequency, newSizeSortedSet);
        }
    }
    private List<Lemma> sortLemma(Map<Integer, List<Lemma>> lemmaMapByFrequency, int sizeCollection){
        List<Lemma> result = new LinkedList<>();
        for (List<Lemma> lemma : lemmaMapByFrequency.values()) {
            lemma.forEach(l -> {
                if (result.size() > sizeCollection) return;
                result.add(l);
            });
        }
        return result;
    }
}
