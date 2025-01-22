import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class WritingSoup2 {
    public static void main(String[] args) throws Exception {
      // Base URL for Walmart product reviews  
      String baseUrl = "https://www.walmart.com/reviews/product/659685769?page=";
      String csvFileName = "reviews.csv"; // Output CSV file

      BufferedWriter writer = new BufferedWriter(new FileWriter(csvFileName));
      // Writer header first
      writer.write("ReviewNumber,Reviewer,Date,Rating,Verified,Content\n");

      int reviewCount = 0;
      // Loop through all pages of reviews
      for (int page = 1; page <=100; page++) {
        // Stop looping when program has collected >=50 reviews.
        if (reviewCount >= 50) {
          break;
        }
        // Construct the full URL for the current page
        String finalUrl = baseUrl + page;
        Document doc = Jsoup.connect(finalUrl)
          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0")
          .header("Connection", "keep-alive")
          .header("Accept-Language", "en-US,en;q=0.9")
          .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
          .get();


          if (doc.toString().contains("Robot or human?")) {
            System.out.println("Page: " + page + " download not successful, skipping.");
            Thread.sleep(5000);
            continue;
         }
         
         System.out.println("Processing page: " + page);
         
         // Select the elements containing review data  
         Elements elements = doc.select("div.et4-l.ma8.dark-gray");
         
         // Loop through each review element on the page
         for (Element element : elements) {
            String reviewer = element.selectFirst("span.f7.b.mb0").ownText();
            String rating = element.selectFirst("span.w-lh7").ownText();
            String reviewText = element.selectFirst("span.fl-n.db-m").ownText();
            String date = element.selectFirst("span.f7.gray.flex.flex-auto.flex-none-l.tr.tl-l.justify-end.justify-start-l").ownText();
            String verification = element.selectFirst("span.b.f7.dark-gray").ownText();
            reviewCount++;
            //Write review data to CSV file
            String lineTemplate = "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n";
            String line = String.format(lineTemplate, reviewCount, reviewer, date, rating, verification, reviewText);
            writer.write(line);
         }
      }
      writer.close();
      // Success output message
      System.out.println("CSV file with reviews created successfully: " + csvFileName);

    }












  // Method to assign point values to reviews based on target words
  private static List<ReviewScore> assignPointsToReviews(String csvFileName, String targetWordsFile) throws IOException {
    Map<String, Integer> targetWords = loadTargetWords(targetWordsFile);
    List<ReviewScore> reviewScores = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new FileReader(csvFileName));
    reader.readLine(); // Skip the header line

    String line;
    // Read each review and calculate its score based on target words
    while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        String reviewer = parts[1].trim();
        String reviewContent = parts[5].trim();

        // Calculate score based on target words
        int score = 0;
        for (Map.Entry<String, Integer> entry : targetWords.entrySet()) {
            if (reviewContent.toLowerCase().contains(entry.getKey().toLowerCase())) {
                score += entry.getValue();
            }
        }
        reviewScores.add(new ReviewScore(reviewer, score));
    }
  }





  // Method to load target words from a file
  private static Map<String, Integer> loadTargetWords(String fileName) throws IOException {
    Map<String, Integer> targetWords = new HashMap<>();
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    String line;
    
    while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 2) {
            targetWords.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
        }
    }
    reader.close();
    return targetWords;
  }

  // Method to get the bottom N reviews based on score
  private static List<String> getBottomReviews(List<ReviewScore> reviewScores, int bottomN) {
    List<String> bottomReviewers = new ArrayList<>();
    for (int i = 0; i < Math.min(bottomN, reviewScores.size()); i++) {
        bottomReviewers.add(reviewScores.get(i).getReviewer());
    }
    return bottomReviewers;
  }

  // Method to save target market reviewers to file, removing duplicates
  private static void saveTargetMarket(List<String> targetMarket, String fileName) throws IOException {
    Set<String> uniqueReviewers = new HashSet<>(targetMarket); // Remove duplicates
    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
    // Write each unique reviewer to file
    for (String reviewer : uniqueReviewers) {
        writer.write(reviewer + "\n");
    }
    writer.close();
  }

  // Method to load advertisements from a file
  private static List<String> loadAdvertisements(String fileName) throws IOException {
    List<String> advertisements = new ArrayList<>();
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    String line;
    // Load each advertisement into a list
    while ((line = reader.readLine()) != null) {
        advertisements.add(line.trim());
    }
    reader.close();
    return advertisements;
  }

  // Method to print reviews with a random advertisement for each targeted review
  private static void printReviewsWithAdvertisements(List<String> targetMarketReviewers, List<String> advertisements) {
    Random random = new Random();
    // For each targeted review, print the reviewer and a random advertisement
    for (String reviewer : targetMarketReviewers) {
        String randomAd = advertisements.get(random.nextInt(advertisements.size()));
        System.out.println("Reviewer: " + reviewer);
        System.out.println("Random Advertisement: " + randomAd);
        System.out.println("-----------");
    }
  }
}
