package nl.lang2619.DLCUpdater;

import nl.lang2619.DLCUpdater.parser.JsonParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 6-4-2017.
 */
public class Main {

  private static File file = new File("DLC_List.txt");
  private static List<String> knownDLC = new ArrayList<>();
  private static List<String> newKnownDLC = new ArrayList<>();
  private static int count = 0;

  public static void main(String[] args) {
    readFile();
    getDLC();
    writeFile();
  }

  private static void writeFile() {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
      for (String aNewKnownDLC : newKnownDLC) {
        writer.newLine();
        writer.write(aNewKnownDLC);
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void getDLC() {
    JSONArray dlc = (JSONArray) getData("252690").get("dlc");
    for (Object aDlc : dlc) {
      String dlcId = String.valueOf(aDlc);
      if (!knownDLC.contains(dlcId)) {
        JSONObject newDLC = getData(dlcId);
        if (newDLC == null) {
          System.out.println("\nAPI Ban while iterating new DLC, updating list.");
          System.out.println("Run this script again in a few mins to get the rest.\n");
          break;
        }
        if (newDLC.get("type").equals("dlc")) {
          count += 1;
          newKnownDLC.add(newDLC.get("steam_appid") + " " + newDLC.get("name"));
          System.out.format("New DLC: %s %s\n", newDLC.get("steam_appid"), newDLC.get("name"));
        }
      }
    }
    System.out.println("# New DLC: " + count);
    System.out.println("# Total DLC: " + (count + knownDLC.size()));
  }

  private static JSONObject getData(String id) {
    try {
      JSONObject
          response =
          (JSONObject) JSONValue.parse(JsonParser.readUrl("http://store.steampowered"
                                                          + ".com/api/appdetails/?appids=" + id));
      if (response == null) {
        System.out.println("Temporarily blocked from steam API, try again later.");
        return null;
      }
      JSONObject game = (JSONObject) response.get(id);
      return (JSONObject) game.get("data");
    } catch (Exception e) {
      System.out.println("Temporarily blocked from steam API, try again later.");
    }
    return null;
  }

  private static void readFile() {
    try {
      BufferedReader input = new BufferedReader(new FileReader((file)));
      String line;
      while ((line = input.readLine()) != null) {
        if (!line.startsWith("#")) {
          String[] linesplit = line.split(" ");
          String dlc = linesplit[0];
          knownDLC.add(dlc);
        }
      }
      System.out.println("# Known DLC: " + knownDLC.size());
    } catch (IOException e) {
      try {
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("# Add DLC here, Format is \"APPID App Name\" APPID MUST be a valid APPID");
        writer.newLine();
        writer.write("# See the following website for a complete list:");
        writer.newLine();
        writer.write("# https://steamdb.info/app/252690/dlc/");
        writer.newLine();
        writer.write("#");
        writer.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }
}
