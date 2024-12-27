package com.sandy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sandy.domain.USER_ROLE;
import com.sandy.domain.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    // Getters and setters (if not using Lombok)
    @Getter
    @Setter
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Getter
    @Setter
    private String fullName;
	@Getter
    @Setter
    private String email;
	@Getter
    @Setter
    private String mobile;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	
	private UserStatus status= UserStatus.PENDING;

	private boolean isVerified = false;

	@Embedded
	private TwoFactorAuth twoFactorAuth= new TwoFactorAuth();

	private String picture;

	private USER_ROLE role= USER_ROLE.ROLE_USER;

}
