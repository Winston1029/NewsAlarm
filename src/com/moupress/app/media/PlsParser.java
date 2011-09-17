package com.moupress.app.media;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PlsParser implements PlaylistParser {
  private final BufferedReader reader;

  public PlsParser(File file) throws FileNotFoundException {
    this.reader = new BufferedReader(new FileReader(file), 1024);
  }

  @Override
  public List<String> getUrls() {
    LinkedList<String> urls = new LinkedList<String>();
    while (true) {
      try {
        String line = reader.readLine();
        if (line == null) {
          break;
        } 
        String url = parseLine(line);
        if (url != null && !url.equals("")) {
          urls.add(url);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return urls;
  }

  private String parseLine(String line) {
    if (line == null) {
      return null;
    }
    String trimmed = line.trim();
    if (trimmed.indexOf("http") >= 0) {
      return trimmed.substring(trimmed.indexOf("http"));
    }
    return "";
  }
}
