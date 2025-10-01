package com.treyhutson.gym_occupancy.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ScraperService {

    private static final String GYM_URL = "https://www.studentaffairs.pitt.edu/campus-recreation/facilities-hours/live-facility-counts";

    /**
     * Scrapes the gym occupancy website and returns a mapping
     * of facility name -> last count.
     */
    public Map<String, Integer> fetchOccupancyData() throws IOException {
        Map<String, Integer> results = new HashMap<>();

        Document doc = Jsoup.connect(GYM_URL).get();
        Elements facilities = doc.select("div.barChart");

        for (Element facility : facilities) {
            String html = facility.html();
            String text = facility.text();

            // Extract name (before "(Open)")
            String name = text.split("\\(Open\\)")[0].trim();

            // Extract the count after "Last Count:"
            if (html.contains("Last Count:")) {
                String countStr = html.split("Last Count:")[1].split("<br>")[0].trim();
                try {
                    int count = Integer.parseInt(countStr);
                    results.put(name, count);
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse count for: " + name);
                }
            }
        }

        return results;
    }
}
