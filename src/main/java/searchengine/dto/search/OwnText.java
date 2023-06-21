package searchengine.dto.search;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class OwnText {
    private static final int HALFTEXTFRAGMENT = 250;
  public String getFragment(String textHtml, int indexTheBeginningOfTheFragment) {
      int theIndexOfTheFragmentAtTheEndOfTheText = textHtml.length() - indexTheBeginningOfTheFragment;
      StringBuilder fragment = new StringBuilder();
      log.info(textHtml);
      log.info("Длина текста " + textHtml.length());
      log.info("Индекс конца текста " + theIndexOfTheFragmentAtTheEndOfTheText);

      int startIndex;
      if (indexTheBeginningOfTheFragment < HALFTEXTFRAGMENT) startIndex = 0;
      else startIndex = textHtml.indexOf(" ", indexTheBeginningOfTheFragment - HALFTEXTFRAGMENT);
      int endIndex;
      if (textHtml.length() - 1 < indexTheBeginningOfTheFragment + HALFTEXTFRAGMENT) endIndex = textHtml.length() - 1;
      else endIndex = textHtml.substring(startIndex, indexTheBeginningOfTheFragment + HALFTEXTFRAGMENT).lastIndexOf(" ");
//      int startIndex = textHtml.substring(0, indexTheBeginningOfTheFragment).indexOf(".") + 1;
      if (startIndex > 0 && endIndex < textHtml.length() - 1) {
          fragment.append("...")
                  .append(textHtml, startIndex, endIndex)
                  .append("...");
          return fragment.toString();
      }
      if (endIndex == textHtml.length() - 1) {
          fragment.append("...")
                  .append(textHtml, startIndex, endIndex);
          return fragment.toString();
      }
      if (startIndex == 0) {
          fragment.append(textHtml, startIndex, endIndex)
                  .append("...");
      }
      return fragment.toString();
  }
}
