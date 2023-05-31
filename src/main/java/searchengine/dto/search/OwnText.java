package searchengine.dto.search;
import lombok.Data;

@Data
public class OwnText {
  public String getFragment(String textHtml, int indexTheBeginningOfTheFragment) {
      int theIndexOfTheFragmentAtTheEndOfTheText = textHtml.length() - indexTheBeginningOfTheFragment;
      StringBuilder fragment = new StringBuilder();
      int startIndex = textHtml.substring(0, indexTheBeginningOfTheFragment).lastIndexOf(".") + 1;
      StringBuilder newText = new StringBuilder();
      if (indexTheBeginningOfTheFragment > 50 && theIndexOfTheFragmentAtTheEndOfTheText > 50) {
          newText.append(textHtml, startIndex, startIndex + 300);
          fragment.append("...")
                  .append(newText.substring(0, newText.lastIndexOf(" ") - 1))
                  .append("...");
          return fragment.toString();
      }
      if (theIndexOfTheFragmentAtTheEndOfTheText < 300) {
          startIndex = textHtml.substring(0, startIndex - 300).lastIndexOf(" ");
          fragment.append("...")
                  .append(textHtml, startIndex, textHtml.length() - 1);
          return fragment.toString();
      }
      if (indexTheBeginningOfTheFragment < 50) {
          theIndexOfTheFragmentAtTheEndOfTheText = textHtml.substring(0,startIndex + 300).lastIndexOf(" ");
          fragment.append(textHtml, 0, theIndexOfTheFragmentAtTheEndOfTheText)
                  .append("...");
      }
      return fragment.toString();
  }
}
