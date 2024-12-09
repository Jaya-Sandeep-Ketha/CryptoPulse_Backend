package com.sandy.cryptopulse.CryptoPulse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.cryptopulse.CryptoPulse.model.Coin;
import com.sandy.cryptopulse.CryptoPulse.repository.CoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Optional;

@Service
public class CoinServiceImpl implements CoinService {
    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Coin> getCoinList(int page) {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=10&page=" + page;
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Prepare headers if needed (e.g., for custom headers or API tokens)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json"); // Optional: Set Accept header
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the GET request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // Use ObjectMapper to deserialize the JSON response into a List<Coin>
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.getBody(), new TypeReference<List<Coin>>() {});
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Log the exception and rethrow or handle it gracefully
            System.err.println("Error fetching coins from API: " + e.getMessage());
            throw new RuntimeException("Error fetching coin data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch and handle general exceptions
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while fetching coin data", e);
        }
    }

    @Override
    public String getMarketChart(String coinId, int days) {
        String url = "https://api.coingecko.com/api/v3/coins/"+coinId+"/market_chart?vs_currency=usd&days=" + days;
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Prepare headers if needed (e.g., for custom headers or API tokens)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json"); // Optional: Set Accept header
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the GET request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Log the exception and rethrow or handle it gracefully
            System.err.println("Error fetching coins from API: " + e.getMessage());
            throw new RuntimeException("Error fetching coin data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch and handle general exceptions
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while fetching coin data", e);
        }
    }

    @Override
    public String getCoinDetails(String coinId) {
        String url = "https://api.coingecko.com/api/v3/coins/"+coinId;
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Prepare headers if needed (e.g., for custom headers or API tokens)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json"); // Optional: Set Accept header
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the GET request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            Coin coin = new Coin();
            coin.setId(coinId);
            coin.setName(node.get("name").asText());
            coin.setSymbol(node.get("symbol").asText());
            coin.setImage(node.get("image").get("large").asText());
            JsonNode marketData = node.get("marketData");
            coin.setCurrentPrice(marketData.get("current_price").get("usd").asDouble());
            coin.setMarketCap(marketData.get("market_cap").get("usd").asLong());
            coin.setMarketCapRank(marketData.get("market_cap_rank").asInt());
            coin.setTotalVolume(marketData.get("total_volume").get("usd").asLong());
            coin.setHigh24h(marketData.get("high_24h").get("usd").asDouble());
            coin.setLow24h(marketData.get("low_24h").get("usd").asDouble());
            coin.setPriceChange24h(marketData.get("price_change_24h").get("usd").asDouble());
            coin.setMarketCapChange24h(marketData.get("market_cap_change_24h").asLong());
            coin.setMarketCapChangePercentage24h(marketData.get("market_cap_change_percentage_24h").asLong());
            coin.setTotalSupply(marketData.get("total_supply").get("usd").asLong());
            coinRepository.save(coin);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Log the exception and rethrow or handle it gracefully
            System.err.println("Error fetching coins from API: " + e.getMessage());
            throw new RuntimeException("Error fetching coin data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch and handle general exceptions
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while fetching coin data", e);
        }
    }

    @Override
    public Coin findById(String coinId) throws Exception {
        Optional<Coin> coin = coinRepository.findById(coinId);
        if(coin.isEmpty()){
            throw new Exception("Coin Not Found");
        }
        return coin.get();
    }

    @Override
    public String searchCoin(String keyWord) {
        String url = "https://api.coingecko.com/api/v3/search?query="+keyWord;
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Prepare headers if needed (e.g., for custom headers or API tokens)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json"); // Optional: Set Accept header
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the GET request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Log the exception and rethrow or handle it gracefully
            System.err.println("Error fetching coins from API: " + e.getMessage());
            throw new RuntimeException("Error fetching coin data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch and handle general exceptions
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while fetching coin data", e);
        }

        return "";
    }

    @Override
    public String getTop50CoinsByMarketCapRank() {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&page=1";
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Prepare headers if needed (e.g., for custom headers or API tokens)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json"); // Optional: Set Accept header
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the GET request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Log the exception and rethrow or handle it gracefully
            System.err.println("Error fetching coins from API: " + e.getMessage());
            throw new RuntimeException("Error fetching coin data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch and handle general exceptions
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while fetching coin data", e);
        }
        return "";
    }

    @Override
    public String getTrendingCoins() {
        String url = "https://api.coingecko.com/api/v3/search/trending";
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Prepare headers if needed (e.g., for custom headers or API tokens)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json"); // Optional: Set Accept header
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the GET request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Log the exception and rethrow or handle it gracefully
            System.err.println("Error fetching coins from API: " + e.getMessage());
            throw new RuntimeException("Error fetching coin data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch and handle general exceptions
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while fetching coin data", e);
        }

        return "";
    }
}
