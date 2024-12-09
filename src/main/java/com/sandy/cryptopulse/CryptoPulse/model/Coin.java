package com.sandy.cryptopulse.CryptoPulse.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Data
public class Coin {
    @Id
    private String id; // Unique identifier for the cryptocurrency

    private String symbol;
    private String name;
    private String image;
    private double currentPrice;
    private long marketCap;
    private int marketCapRank;
    private long fullyDilutedValuation;
    private long totalVolume;
    private double high24h;
    private double low24h;
    private double priceChange24h;
    private double priceChangePercentage24h;
    private long marketCapChange24h;
    private double marketCapChangePercentage24h;
    private double circulatingSupply;
    private double totalSupply;
    private double maxSupply;
    private double ath; // All-time high
    private double athChangePercentage;
    private Date athDate;
    private double atl; // All-time low
    private double atlChangePercentage;
    private Date atlDate;
    private Date lastUpdated;
}
