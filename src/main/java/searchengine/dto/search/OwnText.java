package searchengine.dto.search;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class OwnText {
    private static final int HALFTEXTFRAGMENT = 150;
  public String getFragment(String textHtml, String fragment, int indexTheBeginningOfTheFragment) {
      int theIndexOfTheFragmentAtTheEndOfTheText = textHtml.length() - indexTheBeginningOfTheFragment;
      StringBuilder fragmentBuilder = new StringBuilder();
      log.info(textHtml);
      log.info("Длина текста " + textHtml.length());
      log.info("Индекс конца текста " + theIndexOfTheFragmentAtTheEndOfTheText);

      int startIndex;
      if (indexTheBeginningOfTheFragment < HALFTEXTFRAGMENT) startIndex = 0;
      else startIndex = textHtml.indexOf(" ", indexTheBeginningOfTheFragment - HALFTEXTFRAGMENT);
      int endIndex = indexTheBeginningOfTheFragment + fragment.length();
      if (textHtml.length() - HALFTEXTFRAGMENT - 1 < endIndex)
          endIndex = textHtml.length() - 1;
      else {
          String preliminaryFragment = textHtml.substring(startIndex, endIndex);
          endIndex = textHtml.indexOf(" ", textHtml.indexOf(preliminaryFragment)
                  + preliminaryFragment.length());
      }
      int endIndexFragment = startIndex + fragment.length() - 5;
      if (startIndex > 0 && endIndex < textHtml.length() - 1) {
          fragmentBuilder.append("...")
                  .append(textHtml, startIndex, indexTheBeginningOfTheFragment)
                  .append(fragment)
                  .append(textHtml, endIndexFragment, endIndexFragment) //от конца фрагмента добавить контекстную часть текста
                  .append("...");
          return fragmentBuilder.toString();
      }
      if (endIndex == textHtml.length() - 1) {
          fragmentBuilder.append("...")
                  .append(textHtml, startIndex, indexTheBeginningOfTheFragment)
                  .append(textHtml, endIndexFragment, textHtml.length() - 1);
          return fragmentBuilder.toString();
      }
      if (startIndex == 0) {
          fragmentBuilder.append(textHtml, startIndex, endIndexFragment)
                  .append("...");
      }
      return fragmentBuilder.toString();
  }
}
