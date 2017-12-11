package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class Transaction {

    @NotNull
    @NotEmpty
    String fromAccountId;

    @NotNull
    @NotEmpty
    String toAccountId;

    @NotNull
    @Min(value = 0, message = "Initial amount must be positive.")
    BigDecimal amount;

    @JsonCreator
    public Transaction(@JsonProperty("fromAccountId") String fromAccountId,
                   @JsonProperty("toAccountId") String toAccountId, @JsonProperty("amount") BigDecimal amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

}
