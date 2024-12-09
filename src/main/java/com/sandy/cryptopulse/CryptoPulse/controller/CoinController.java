package com.sandy.cryptopulse.CryptoPulse.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.cryptopulse.CryptoPulse.model.Coin;
import com.sandy.cryptopulse.CryptoPulse.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Repository
@RequestMapping("/coins")
public class CoinController {
    @Autowired
    private CoinService coinService;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    ResponseEntity<List<Coin>>getCoinList(@RequestParam("page") int page) throws Exception{
        List<Coin> coinList = coinService.getCoinList(page);
        return new ResponseEntity<>(coinList, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{coinId}/chart")
    ResponseEntity<JsonNode>getMarketChart(@RequestParam int days, @PathVariable String coinId) throws Exception{
        String res = coinService.getMarketChart(coinId, days);
        JsonNode node = objectMapper.readTree(res);
        return new ResponseEntity<>(node, HttpStatus.ACCEPTED);
    }

    @GetMapping("/search")
    ResponseEntity<JsonNode>searchCoin(@RequestParam("q") String keyWord) throws Exception{
        String res = coinService.searchCoin(keyWord);
        JsonNode node = objectMapper.readTree(res);
        return ResponseEntity.ok(node);
    }

    @GetMapping("/top50")
    ResponseEntity<JsonNode>getTop50CoinByMarketCapRank() throws Exception{
        String res = coinService.getTop50CoinsByMarketCapRank();
        JsonNode node = objectMapper.readTree(res);
        return ResponseEntity.ok(node);
    }

    @GetMapping("/top50")
    ResponseEntity<JsonNode>getTrendingCoin() throws Exception{
        String res = coinService.getTrendingCoins();
        JsonNode node = objectMapper.readTree(res);
        return ResponseEntity.ok(node);
    }

    @GetMapping("/details/{coinId}")
    ResponseEntity<JsonNode>getCoinDetails(@PathVariable String coinId) throws Exception{
        String res = coinService.getCoinDetails(coinId);
        JsonNode node = objectMapper.readTree(res);
        return ResponseEntity.ok(node);
    }
}
