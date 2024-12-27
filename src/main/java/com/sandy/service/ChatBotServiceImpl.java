package com.sandy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.sandy.model.CoinDTO;
import com.sandy.response.ApiResponse;
import com.sandy.response.FunctionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatBotServiceImpl implements ChatBotService{

    @Value("${gemini.api.key}")
    private String API_KEY;
    private static final Logger logger = LoggerFactory.getLogger(ChatBotServiceImpl.class);

    private double convertToDouble(Object value) {
        if (value == null) {
            return 0.0; // Handle null values gracefully
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse String to double: {}", value);
            }
        }
        throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
    }

    public CoinDTO makeApiRequest(String currencyName) {
        System.out.println("coin name "+currencyName);
        String url = "https://api.coingecko.com/api/v3/coins/"+currencyName.toLowerCase();

        RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();


            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody != null) {
                Map<String, Object> image = (Map<String, Object>) responseBody.get("image");

                Map<String, Object> marketData = (Map<String, Object>) responseBody.get("market_data");

                CoinDTO coinInfo = new CoinDTO();
                coinInfo.setId((String) responseBody.get("id"));
                coinInfo.setSymbol((String) responseBody.get("symbol"));
                coinInfo.setName((String) responseBody.get("name"));
                coinInfo.setImage((String) image.get("large"));

                coinInfo.setCurrentPrice(convertToDouble(((Map<String, Object>) marketData.get("current_price")).get("usd")));
                coinInfo.setMarketCap(convertToDouble(((Map<String, Object>) marketData.get("market_cap")).get("usd")));
                coinInfo.setMarketCapRank((int) responseBody.get("market_cap_rank"));
                coinInfo.setTotalVolume(convertToDouble(((Map<String, Object>) marketData.get("total_volume")).get("usd")));
                coinInfo.setHigh24h(convertToDouble(((Map<String, Object>) marketData.get("high_24h")).get("usd")));
                coinInfo.setLow24h(convertToDouble(((Map<String, Object>) marketData.get("low_24h")).get("usd")));
                coinInfo.setPriceChange24h(convertToDouble(marketData.get("price_change_24h")) );
                coinInfo.setPriceChangePercentage24h(convertToDouble(marketData.get("price_change_percentage_24h")));
                coinInfo.setMarketCapChange24h(convertToDouble(marketData.get("market_cap_change_24h")));
                coinInfo.setMarketCapChangePercentage24h(convertToDouble( marketData.get("market_cap_change_percentage_24h")));
                coinInfo.setCirculatingSupply(convertToDouble(marketData.get("circulating_supply")));
                coinInfo.setTotalSupply(convertToDouble(marketData.get("total_supply")));

                return coinInfo;

             }
       return null;
    }



    public FunctionResponse getFunctionResponse(String prompt){
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + prompt + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"functionDeclarations\": [\n" +
                "        {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"description\": \"Get the coin details from given currency object\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"currencyName\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The currency name, id, symbol.\"\n" +
                "              },\n" +
                "              \"currencyData\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"Currency Data id, symbol, name, image, current_price, market_cap, market_cap_rank, fully_diluted_valuation, total_volume, high_24h, low_24h, price_change_24h, price_change_percentage_24h, market_cap_change_24h, market_cap_change_percentage_24h, circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date, last_updated.\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\"currencyName\", \"currencyData\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        // Create the HTTP entity with headers and request body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        // Make the POST request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);


        String responseBody = response.getBody();

        ReadContext ctx = JsonPath.parse(responseBody);

        // Extract specific values
        String currencyName = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyName");
        String currencyData = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyData");
        String name = ctx.read("$.candidates[0].content.parts[0].functionCall.name");

        // Print the extracted values
        FunctionResponse res=new FunctionResponse();
        res.setCurrencyName(currencyName);
        res.setCurrencyData(currencyData);
        res.setFunctionName(name);

        System.out.println(name +" ------- "+currencyName+"-----"+currencyData);

        return res;
    }




    @Override
    public ApiResponse getCoinDetails(String prompt) {

        FunctionResponse res=getFunctionResponse(prompt);
        String apiResponse=makeApiRequest(res.getCurrencyName()).toString();



         String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);




String body="{\n" +
        "  \"contents\": [\n" +
        "    {\n" +
        "      \"role\": \"user\",\n" +
        "      \"parts\": [\n" +
        "        {\n" +
        "          \"text\": \"" + prompt + "\"\n" +
        "        }\n" +
        "      ]\n" +
        "    },\n" +
        "    {\n" +
        "      \"role\": \"model\",\n" +
        "      \"parts\": [\n" +
        "        {\n" +
        "          \"functionCall\": {\n" +
        "            \"name\": \"getCoinDetails\",\n" +
        "            \"args\": {\n" +
        "              \"currencyName\": \"" +res.getCurrencyName() +"\",\n" +
        "              \"currencyData\": \""+ res.getCurrencyData() + "\"\n" +
        "            }\n" +
        "          }\n" +
        "        }\n" +
        "      ]\n" +
        "    },\n" +
        "    {\n" +
        "      \"role\": \"function\",\n" +
        "      \"parts\": [\n" +
        "        {\n" +
        "          \"functionResponse\": {\n" +
        "            \"name\": \"getCoinDetails\",\n" +
        "            \"response\": {\n" +
        "              \"name\": \"getCoinDetails\",\n" +
        "              \"content\": " + apiResponse + "\n" +
        "            }\n" +
        "          }\n" +
        "        }\n" +
        "      ]\n" +
        "    }\n" +
        "  ],\n" +
        "  \"tools\": [\n" +
        "    {\n" +
        "      \"functionDeclarations\": [\n" +
        "        {\n" +
        "          \"name\": \"getCoinDetails\",\n" +
        "          \"description\": \"Get crypto currency data from given currency object.\",\n" +
        "          \"parameters\": {\n" +
        "            \"type\": \"OBJECT\",\n" +
        "            \"properties\": {\n" +
        "              \"currencyName\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"The currency Name, id, symbol .\"\n" +
        "              },\n" +
        "              \"currencyData\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"The currency data id, symbol, current price, image, market cap extra... \"\n" +
        "              }\n" +
        "            },\n" +
        "            \"required\": [\"currencyName\",\"currencyData\"]\n" +
        "          }\n" +
        "        },\n" +
        "        {\n" +
        "          \"name\": \"find_theaters\",\n" +
        "          \"description\": \"find theaters based on location and optionally movie title which is currently playing in theaters\",\n" +
        "          \"parameters\": {\n" +
        "            \"type\": \"OBJECT\",\n" +
        "            \"properties\": {\n" +
        "              \"location\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"The city and state, e.g. San Francisco, CA or a zip code e.g. 95616\"\n" +
        "              },\n" +
        "              \"movie\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"Any movie title\"\n" +
        "              }\n" +
        "            },\n" +
        "            \"required\": [\"location\"]\n" +
        "          }\n" +
        "        },\n" +
        "        {\n" +
        "          \"name\": \"get_showtimes\",\n" +
        "          \"description\": \"Find the start times for movies playing in a specific theater\",\n" +
        "          \"parameters\": {\n" +
        "            \"type\": \"OBJECT\",\n" +
        "            \"properties\": {\n" +
        "              \"location\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"The city and state, e.g. San Francisco, CA or a zip code e.g. 95616\"\n" +
        "              },\n" +
        "              \"movie\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"Any movie title\"\n" +
        "              },\n" +
        "              \"theater\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"Name of the theater\"\n" +
        "              },\n" +
        "              \"date\": {\n" +
        "                \"type\": \"STRING\",\n" +
        "                \"description\": \"Date for requested showtime\"\n" +
        "              }\n" +
        "            },\n" +
        "            \"required\": [\"location\", \"movie\", \"theater\", \"date\"]\n" +
        "          }\n" +
        "        }\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}";



        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, request, String.class);

        System.out.println("Response: " + response.getBody());
        ReadContext ctx = JsonPath.parse(response.getBody());

        String text = ctx.read("$.candidates[0].content.parts[0].text");
        ApiResponse ans=new ApiResponse();
        ans.setMessage(text);

        return ans;
    }

    @Override
    public CoinDTO getCoinByName(String coinName) {
        return this.makeApiRequest(coinName);
//        return null;
    }

    @Override
    public String simpleChat(String prompt) {

        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the request body using JSONObject
        JSONObject requestBody = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject contentsObject = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject textObject = new JSONObject();
        textObject.put("text", prompt);
        partsArray.put(textObject);
        contentsObject.put("parts", partsArray);
        contentsArray.put(contentsObject);
        requestBody.put("contents", contentsArray);

        // Create the HTTP entity with headers and request body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        // Make the POST request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);


        String responseBody = response.getBody();

        System.out.println("Response Body: " + responseBody);

        return responseBody;
    }

//
//    private RestTemplate restTemplate = new RestTemplate();
//
//    private String createInstructionalPrompt(String userPrompt) {
//        logger.info("Creating instructional prompt for user prompt: {}", userPrompt);
//
//        // Escape special characters to ensure the prompt is JSON-safe
//        String escapedPrompt = userPrompt.replace("\"", "\\\"").replace("\n", " ");
//        logger.debug("Escaped user prompt: {}", escapedPrompt);
//
//        return "Analyze the following user input and return a JSON object with:\n" +
//                "1. 'intent': The purpose of the prompt (e.g., 'compare prices', 'costly coins', 'best performer').\n" +
//                "2. 'coins': A list of coin names mentioned in the prompt.\n" +
//                "3. 'topN': Any numerical limits specified in the prompt (e.g., 'Top 5').\n\n" +
//                "User Input: \"" + escapedPrompt + "\"";
//    }
//
//
//    private Map<String, Object> parsePrompt(String userPrompt) {
//        logger.info("Parsing prompt: {}", userPrompt);
//
//        String instructionalPrompt = createInstructionalPrompt(userPrompt);
//
//        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;
//
//        // Construct the request body in the required format
//        JSONObject requestBody = new JSONObject();
//        JSONArray contentsArray = new JSONArray();
//        JSONObject contentsObject = new JSONObject();
//        JSONArray partsArray = new JSONArray();
//        JSONObject textObject = new JSONObject();
//
//        textObject.put("text", instructionalPrompt);
//        partsArray.put(textObject);
//        contentsObject.put("parts", partsArray);
//        contentsArray.put(contentsObject);
//        requestBody.put("contents", contentsArray);
//
//        logger.info("Request payload: {}", requestBody);
//
//        // Send the request to Gemini
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
//
//        String responseBody = response.getBody();
//        logger.info("Gemini Response: {}", responseBody);
//
//        // Parse the JSON response
//        try {
//            // Extract the raw JSON string from Gemini's response
//            ReadContext ctx = JsonPath.parse(responseBody);
//            String rawText = ctx.read("$.candidates[0].content.parts[0].text", String.class);
//
//            // Remove Markdown-style code block delimiters
//            String cleanedJson = rawText.replace("```json", "").replace("```", "").trim();
//
//            // Parse the cleaned JSON string
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> parsedResponse = objectMapper.readValue(cleanedJson, new TypeReference<>() {});
//            logger.info("Parsed Gemini Response: {}", parsedResponse);
//
//            return parsedResponse;
//        } catch (Exception e) {
//            logger.error("Failed to parse Gemini response: {}", e.getMessage(), e);
//            throw new IllegalArgumentException("Failed to parse Gemini response.", e);
//        }
//    }
//
//    private Map<String, CoinDTO> fetchCoinData(List<String> coinNames) {
//        logger.info("Fetching coin data for coins: {}", coinNames);
//
//        RestTemplate restTemplate = new RestTemplate();
//        Map<String, CoinDTO> coinData = new HashMap<>();
//
//        for (String coinName : coinNames) {
//            try {
//                String url = "https://api.coingecko.com/api/v3/coins/" + coinName.toLowerCase();
//                logger.debug("Fetching data for coin: {} from URL: {}", coinName, url);
//
//                ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
//                Map<String, Object> responseBody = responseEntity.getBody();
//                System.out.println(responseBody);
//                CoinDTO coinInfo = null;
//                if (responseBody != null) {
//                    coinInfo = new CoinDTO();
//                    coinInfo.setId((String) responseBody.get("id"));
//                    coinInfo.setSymbol((String) responseBody.get("symbol"));
//                    coinInfo.setName((String) responseBody.get("name"));
//                    coinInfo.setImage((String) ((Map<String, Object>) responseBody.get("image")).get("large"));
//
//                    Map<String, Object> marketData = (Map<String, Object>) responseBody.get("market_data");
//                    coinInfo.setCurrentPrice(convertToDouble(((Map<String, Object>) marketData.get("current_price")).get("usd")));
//                    coinInfo.setMarketCap(convertToDouble(((Map<String, Object>) marketData.get("market_cap")).get("usd")));
//                    coinInfo.setMarketCapRank((Integer) marketData.get("market_cap_rank"));
//                    coinInfo.setTotalVolume(convertToDouble(((Map<String, Object>) marketData.get("total_volume")).get("usd")));
//                    coinInfo.setHigh24h(convertToDouble(((Map<String, Object>) marketData.get("high_24h")).get("usd")));
//                    coinInfo.setLow24h(convertToDouble(((Map<String, Object>) marketData.get("low_24h")).get("usd")));
//                    coinInfo.setPriceChange24h(convertToDouble((Double) marketData.get("price_change_24h")));
//                    coinInfo.setPriceChangePercentage24h(convertToDouble((Double) marketData.get("price_change_percentage_24h")));
//                    coinInfo.setMarketCapChange24h(convertToDouble((Double) marketData.get("market_cap_change_24h")));
//                    coinInfo.setMarketCapChangePercentage24h(convertToDouble((Double) marketData.get("market_cap_change_percentage_24h")));
//                    coinInfo.setCirculatingSupply(convertToDouble((Double) marketData.get("circulating_supply")));
//                    coinInfo.setTotalSupply(convertToDouble((Double) marketData.get("total_supply")));
//
//                    coinData.put(coinName, coinInfo);
//                }
//
//                logger.info("Fetched data for coin: {}", coinInfo);
//
//            } catch (Exception e) {
//                logger.error("Error fetching data for coin: {}. Error: {}", coinName, e.getMessage(), e);
//            }
//        }
//
//        logger.info("Fetched data for all coins: {}", coinData);
//        return coinData;
//    }
//
//    private String generateFinalResponse(String intent, Map<String, CoinDTO> coinData, Integer topN) {
//        logger.info("Generating final response for intent: {}, coins: {}, topN: {}", intent, coinData, topN);
//
//        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        JSONObject requestBody = new JSONObject();
//        JSONArray contentsArray = new JSONArray();
//        JSONObject contentsObject = new JSONObject();
//
//        // Prepare coin data as JSON
//        JSONObject coinDataJson = new JSONObject();
//        for (Map.Entry<String, CoinDTO> entry : coinData.entrySet()) {
//            CoinDTO coin = entry.getValue();
//            JSONObject coinInfo = new JSONObject();
//            coinInfo.put("id", coin.getId());
//            coinInfo.put("symbol", coin.getSymbol());
//            coinInfo.put("name", coin.getName());
//            coinInfo.put("image", coin.getImage());
//            coinInfo.put("current_price", coin.getCurrentPrice());
//            coinInfo.put("market_cap", coin.getMarketCap());
//            coinInfo.put("market_cap_rank", coin.getMarketCapRank());
//            coinInfo.put("total_volume", coin.getTotalVolume());
//            coinInfo.put("high_24h", coin.getHigh24h());
//            coinInfo.put("low_24h", coin.getLow24h());
//            coinInfo.put("price_change_24h", coin.getPriceChange24h());
//            coinInfo.put("price_change_percentage_24h", coin.getPriceChangePercentage24h());
//            coinInfo.put("market_cap_change_24h", coin.getMarketCapChange24h());
//            coinInfo.put("market_cap_change_percentage_24h", coin.getMarketCapChangePercentage24h());
//            coinInfo.put("circulating_supply", coin.getCirculatingSupply());
//            coinInfo.put("total_supply", coin.getTotalSupply());
//            coinDataJson.put(entry.getKey(), coinInfo);
//        }
//
//        // Prepare prompt
//        String prompt = String.format(
//                "Intent: %s\nTop N: %s\nCoin Data: %s",
//                intent, topN != null ? topN : "N/A", coinDataJson.toString()
//        );
//
//        contentsObject.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
//        contentsArray.put(contentsObject);
//        requestBody.put("contents", contentsArray);
//
//        logger.info("Final request payload for Gemini: {}", requestBody);
//
//        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
//        RestTemplate restTemplate = new RestTemplate();
//
//        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
//        String responseBody = response.getBody();
//
//        logger.info("Received final response from Gemini: {}", responseBody);
//
////        return responseBody; // Or process response further to extract meaningful output
//
//        // Extract plain text from Gemini response
//        JSONObject geminiResponse = new JSONObject(response.getBody());
//        String plainTextAnswer = geminiResponse
//                .getJSONArray("candidates")
//                .getJSONObject(0)
//                .getJSONObject("content")
//                .getJSONArray("parts")
//                .getJSONObject(0)
//                .getString("text");
//
//        return plainTextAnswer;
//    }
//
//
//
//    @Override
//    public String processPromptAndGenerateResponse(String prompt) {
//        // Step 1: Understand the prompt
//        Map<String, Object> parsedPrompt = parsePrompt(prompt);
//        Map<String, Object> responseMap = new HashMap<>();
//        List<Map<String, String>> messages = new ArrayList<>();
//
//        String intent = (String) parsedPrompt.get("intent");
//        List<String> coins = (List<String>) parsedPrompt.getOrDefault("coins", List.of());
//        Integer topN = (Integer) parsedPrompt.getOrDefault("topN", null);
//
//        // Add user message
//        messages.add(Map.of("role", "user", "prompt", prompt));
//
//        // Step 3: Fetch coin data if coins are provided
//        Map<String, CoinDTO> coinData = null;
//        String finalAnswer;
//        if (coins.isEmpty()) {
//            // Fetch all available coins if no specific coins are mentioned
//            logger.info("No specific coins mentioned. Fetching all available coins.");
//            List<Map<String, String>> allCoins = fetchAllCoins();
//            coinData = allCoinsToCoinDTO(allCoins);
//        } else {
//            coinData = fetchCoinData(coins);
//        }
//
//        finalAnswer = generateFinalResponse(intent, coinData, topN);
//
//        // Step 5: Add bot response
//        messages.add(Map.of("role", "bot", "ans", finalAnswer));
//
//        // Step 6: Structure the response
//        responseMap.put("messages", messages);
//
//        System.out.println(responseMap);
//
//        // Convert responseMap to JSON
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            return objectMapper.writeValueAsString(responseMap);
//        } catch (JsonProcessingException e) {
//            throw new IllegalArgumentException("Failed to serialize responseMap to JSON.", e);
//        }
//    }
//
//    private List<Map<String, String>> fetchAllCoins() {
//        logger.info("Fetching all available coins from CoinGecko.");
//
//        String COINGECKO_API_URL = "https://api.coingecko.com/api/v3/coins/markets";
//        RestTemplate restTemplate = new RestTemplate();
//        List<Map<String, Object>> coins;
//        try {
//            coins = restTemplate.exchange(
//                    COINGECKO_API_URL + "?vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=false",
//                    HttpMethod.GET,
//                    null,
//                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
//        } catch (Exception e) {
//            logger.error("Error fetching coins from CoinGecko: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to fetch coins from CoinGecko.", e);
//        }
//
//        List<Map<String, String>> coinData = new ArrayList<>();
//        for (Map<String, Object> coin : coins) {
//            Map<String, String> coinDetails = new HashMap<>();
//            coinDetails.put("id", (String) coin.get("id"));
//            coinDetails.put("name", (String) coin.get("name"));
//            coinDetails.put("symbol", (String) coin.get("symbol"));
//            coinDetails.put("image", (String) coin.get("image"));
//            coinDetails.put("current_price", String.valueOf(coin.get("current_price")));
//            coinDetails.put("market_cap", String.valueOf(coin.get("market_cap")));
//            coinDetails.put("market_cap_rank", String.valueOf(coin.get("market_cap_rank")));
//            coinDetails.put("total_volume", String.valueOf(coin.get("total_volume")));
//            coinDetails.put("high_24h", String.valueOf(coin.get("high_24h")));
//            coinDetails.put("low_24h", String.valueOf(coin.get("low_24h")));
//            coinDetails.put("price_change_24h", String.valueOf(coin.get("price_change_24h")));
//            coinDetails.put("price_change_percentage_24h", String.valueOf(coin.get("price_change_percentage_24h")));
//            coinDetails.put("market_cap_change_24h", String.valueOf(coin.get("market_cap_change_24h")));
//            coinDetails.put("market_cap_change_percentage_24h", String.valueOf(coin.get("market_cap_change_percentage_24h")));
//            coinDetails.put("circulating_supply", String.valueOf(coin.get("circulating_supply")));
//            coinDetails.put("total_supply", String.valueOf(coin.get("total_supply")));
//
//            coinData.add(coinDetails);
//        }
//
//        logger.info("Fetched {} coins.", coinData.size());
//        return coinData;
//    }
//
//    private Map<String, CoinDTO> allCoinsToCoinDTO(List<Map<String, String>> allCoins) {
//        logger.info("Converting all coins to CoinDTO format.");
//
//        Map<String, CoinDTO> coinData = new HashMap<>();
//        for (Map<String, String> coin : allCoins) {
//            CoinDTO coinInfo = new CoinDTO();
//            coinInfo.setId(coin.get("id"));
//            coinInfo.setName(coin.get("name"));
//            coinInfo.setSymbol(coin.get("symbol"));
//            coinInfo.setImage(coin.get("image"));
//            coinInfo.setCurrentPrice(Double.valueOf(coin.get("current_price")));
//            coinInfo.setMarketCap(Double.valueOf(coin.get("market_cap")));
//            coinInfo.setMarketCapRank(Integer.valueOf(coin.get("market_cap_rank")));
//            coinInfo.setTotalVolume(Double.valueOf(coin.get("total_volume")));
//            coinInfo.setHigh24h(Double.valueOf(coin.get("high_24h")));
//            coinInfo.setLow24h(Double.valueOf(coin.get("low_24h")));
//            coinInfo.setPriceChange24h(Double.valueOf(coin.get("price_change_24h")));
//            coinInfo.setPriceChangePercentage24h(Double.valueOf(coin.get("price_change_percentage_24h")));
//            coinInfo.setMarketCapChange24h(Double.valueOf(coin.get("market_cap_change_24h")));
//            coinInfo.setMarketCapChangePercentage24h(Double.valueOf(coin.get("market_cap_change_percentage_24h")));
//            coinInfo.setCirculatingSupply(Double.valueOf(coin.get("circulating_supply")));
//            coinInfo.setTotalSupply(Double.valueOf(coin.get("total_supply")));
//
//            coinData.put(coin.get("symbol"), coinInfo);
//        }
//
//        return coinData;
//    }

}
