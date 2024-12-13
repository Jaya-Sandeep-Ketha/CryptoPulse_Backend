package com.sandy.cryptopulse.CryptoPulse.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User user;

    private OrderType orderType;

}
