package com.verint.itunes;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 4/7/2017.
 */
public class ITunesApp {
    private  AppIdsResource appIdsResource;
    private AppInfoRepository appInfoRepo;

    public ITunesApp() {
        this.appIdsResource = new AppIdsFileSystemResource();
        this.appInfoRepo = new AppInfoRestRepository();
    }

    public List<String> loadAppIds() {
        return appIdsResource.readAppsIds();
    }

    public List<AppInfo> loadAppsInfo(List<String> appsIds) {
        return appInfoRepo.readAppInfo(appsIds);
    }
}
