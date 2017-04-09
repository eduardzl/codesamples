package com.verint.itunes;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 4/7/2017.
 */
public class AppIdsFileSystemResource implements AppIdsResource {
    private final String appIdsFilePath = "c:\\temp\\ids.txt";

    public List<String> readAppsIds() {
        List<String> appsIds = new ArrayList<String>();

        BufferedReader buffRd = null;
        try {
            if (!StringUtils.isEmpty(appIdsFilePath)) {
                buffRd = new BufferedReader(new FileReader(appIdsFilePath));

                String appId = null;
                while ((appId = buffRd.readLine()) != null) {
                    appsIds.add(appId);
                }
            }
        } catch (Exception ex) {

        } finally {
            try {
                if (buffRd != null) {
                    buffRd.close();
                }
            } catch (Exception ex) { }
        }

        return appsIds;
    }
}
