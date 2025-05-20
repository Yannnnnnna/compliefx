package com.example.compliefx2;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Token库，用于管理Token类型的映射
 * 提供从JSON文件读取Token类型映射的功能以及双向映射查询
 */
public class TokenLibrary {
    // 从Token名称到Token类型编码的映射
    private static Map<String, Integer> nameToTypeMap;

    // 从Token类型编码到Token名称的映射（反向映射）
    private static Map<Integer, String> typeToNameMap;

    /**
     * 从JSON文件读取Token类型映射
     * @param filePath JSON文件路径
     * @return 从Token名称到类型编码的映射
     */
    public static Map<String, Integer> readToken(String filePath) {
        if (nameToTypeMap != null) {
            return nameToTypeMap; // 如果已经加载，直接返回
        }

        nameToTypeMap = new HashMap<>();
        typeToNameMap = new HashMap<>();

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            // 遍历JSON对象，构建映射
            for (Object key : jsonObject.keySet()) {
                String tokenName = (String) key;
                Integer tokenType = ((Long) jsonObject.get(tokenName)).intValue();

                nameToTypeMap.put(tokenName, tokenType);
                typeToNameMap.put(tokenType, tokenName);
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return nameToTypeMap;
    }

    /**
     * 根据Token名称获取对应的类型编码
     * @param tokenName Token名称
     * @return 对应的类型编码，如果不存在则返回-1
     */
    public static int getTokenType(String tokenName) {
        if (nameToTypeMap == null) {
            throw new IllegalStateException("Token map has not been initialized. Call readToken() first.");
        }

        return nameToTypeMap.getOrDefault(tokenName, -1);
    }

    /**
     * 根据Token类型编码获取对应的名称
     * @param tokenType Token类型编码
     * @return 对应的名称，如果不存在则返回"UNKNOWN_TOKEN"
     */
    public static String getTokenName(int tokenType) {
        if (typeToNameMap == null) {
            throw new IllegalStateException("Token map has not been initialized. Call readToken() first.");
        }

        return typeToNameMap.getOrDefault(tokenType, "UNKNOWN_TOKEN");
    }

    /**
     * 检查是否存在指定名称的Token
     * @param tokenName Token名称
     * @return 如果存在则返回true，否则返回false
     */
    public static boolean hasToken(String tokenName) {
        if (nameToTypeMap == null) {
            throw new IllegalStateException("Token map has not been initialized. Call readToken() first.");
        }

        return nameToTypeMap.containsKey(tokenName);
    }

    /**
     * 检查是否存在指定类型编码的Token
     * @param tokenType Token类型编码
     * @return 如果存在则返回true，否则返回false
     */
    public static boolean hasTokenType(int tokenType) {
        if (typeToNameMap == null) {
            throw new IllegalStateException("Token map has not been initialized. Call readToken() first.");
        }

        return typeToNameMap.containsKey(tokenType);
    }
}