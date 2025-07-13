package org.tiff.aiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Image search tool
 */
@Service
public class ImageSearchTool {

    @Value("${pexels.api.key}")
    private String API_KEY;

    // Pexels API url
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * Search for medium size photos
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query) {
        // header includes API key
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // set request params that includes query etc
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        // send GET request
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // Parse the response JSON
        // (assuming the response structure contains a "photos" array,
        // each element contains a "medium" field)
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}



