package com.verint.itunes;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static void main (String[] args) {
        ITunesApp indexer = new ITunesApp();

        // read app ids to load description
        List<String> appdsIds = indexer.loadAppIds();

        indexer.loadAppsInfo(appdsIds);

    }
}
