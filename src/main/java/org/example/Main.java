package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    private static final String site = "https://www.moscowmap.ru/";
    private static final String pathFolder = "data";
    private static final String pathFile = "data/metromap.json";

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect(site).maxBodySize(0).get();
        Elements elements = doc.select(".t-text-simple [data-line]");
        createFolderAndFile();
        ArrayList<String> lines = listLines(elements);
        ArrayList<String> stations = listStations(elements);
        createJson(lines, stations);
        printCountStationsOnTheLine(lines, stations);
    }
    private static void createFolderAndFile() throws IOException {
        if (Files.notExists(Path.of(pathFolder))) {
            Files.createDirectory(Path.of(pathFolder));
        }
        if (Files.exists(Path.of(pathFile))) {
            Files.delete(Path.of(pathFile));
        }
        Files.createFile(Path.of(pathFile));
    }
    private static ArrayList<String> listLines (Elements elements) {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < elements.size(); i = i + 2) {
            lines.add(elements.get(i).text());
        }
        return lines;
    }
    private static ArrayList<String> listStations (Elements elements) {
        ArrayList<String> stations = new ArrayList<>();
        for (int i = 1; i < elements.size(); i = i + 2) {
            String text = elements.get(i).text();
            stations.add(text);
        }
        return modificationListStations(stations);
    }
    private static ArrayList<String> modificationListStations(ArrayList<String> stations) {
        ArrayList<String> list = new ArrayList<>();
        for (String item : stations) {
            item = item.replaceAll("[0-9]+\\.\\s", "/");
            item = item.substring(1);
            item = item.replaceAll("\\s\\/", "/");
            list.add(item);
        }
        return list;
    }
    private static void createJson(ArrayList<String> lines, ArrayList<String> stations) throws IOException {
        JSONObject list = new JSONObject();
        JSONArray jsonLines = new JSONArray();
        for (int i = 0; i < lines.size(); i++) {
            JSONObject number2Name = new JSONObject();
            number2Name.clear();
            number2Name.put("number", i + 1);
            number2Name.put("name", lines.get(i));
            jsonLines.add(number2Name);
        }
        JSONObject jsonStations = new JSONObject();
        int counter = 1;
        for (int i = 0; i < stations.size(); i++) {
            JSONArray stations2line = new JSONArray();
            stations2line.clear();
            jsonStations.put(counter, modificationStationsList(stations, i));
            counter++;
        }
        list.put("lines", jsonLines);
        list.put("stations", jsonStations);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(list);
        Files.write(Paths.get(pathFile), json.getBytes());
    }
    private static String[] modificationStationsList(ArrayList<String> stationsOnTheLine, int i) {
        return stationsOnTheLine.get(i).split("/");
    }
    private static void printCountStationsOnTheLine(ArrayList<String> lines, ArrayList<String> stations) {
        for (int i = 0; i < lines.size(); i++) {
            int count = countingStationCount(stations, i);
            System.out.println(lines.get(i) + " - " + count + caseStationWord(count));
        }
    }
    private static int countingStationCount(ArrayList<String> stationsOnTheLine, int i) {
        String[] stations = stationsOnTheLine.get(i).split("/");
        int count = stations.length;
        return count;
    }
    private static String caseStationWord (int count) {
        String end = "";
        int remainder = count % 10;
        if (count >= 5 && count <= 19 || remainder == 0) {
            end = " станций";
        } else if (remainder == 1 ) {
            end = " станция";
        } else if (remainder >= 2 && remainder <= 4) {
            end = " станции";
        } else if (remainder >= 5 && remainder <= 9) {
            end = " станций";
        }
        return end;
    }
}