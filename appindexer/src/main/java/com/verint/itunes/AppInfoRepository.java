package com.verint.itunes;

import java.util.List;

/**
 * Created by home on 4/7/2017.
 */
public interface AppInfoRepository {

    List<AppInfo> readAppInfo(List<String> appIds);
}
