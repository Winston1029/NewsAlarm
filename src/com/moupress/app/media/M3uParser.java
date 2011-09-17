package com.moupress.app.media;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class M3uParser implements PlaylistParser {
  private final BufferedReader reader;

  public M3uParser(File file) throws FileNotFoundException {
    this.reader = new BufferedReader(new FileReader(file), 1024);
  }

  @Override
  public List<String> getUrls() {
    LinkedList<String> urls = new LinkedList<String>();
    while (true) {
      try {
        String url = reader.readLine();
        if (url == null) {
          break;
        } else if (isUrl(url)) {
          urls.add(url);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return urls;
  }

  public boolean isUrl(String url) {
    String trimmed = url.trim();
    return trimmed.length() > 0 && trimmed.charAt(0) != '#'
        && trimmed.charAt(0) != '<';
  }
}
