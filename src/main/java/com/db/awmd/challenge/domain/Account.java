package com.db.awmd.challenge.domain;

import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class Account {

  @NotNull
  @NotEmpty
  private final String accountId;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
      private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }


    public boolean withdraw(BigDecimal amt) {
        if (amt.doubleValue() < 0.0 || amt.doubleValue() > this.balance.doubleValue()) {
            return false;
        }
        else {

            this.balance = balance.add(amt.negate());
        }
        return true;
    }

    public void deposit(BigDecimal amt){
      this.balance = this.balance.add(amt);
    }


  public boolean transferAmount(Account toAccount, BigDecimal amount) throws InsufficientBalanceException {
    Object lock1 = this.balance.intValue() > toAccount.balance.intValue() ? this : toAccount;
    Object lock2 = this.balance.intValue() < toAccount.balance.intValue() ? this : toAccount;

    boolean transferDone = false;
    synchronized(lock1){
      synchronized (lock2) {
            if(this.withdraw(amount))  //check on how to make this operation atomic or transactional.
            {
                toAccount.deposit(amount);
                transferDone = true;
            }
            else {
                throw new InsufficientBalanceException("Balance not sufficient to transfer from account" + this.accountId);
            }
      }
    }
    return transferDone;
  }
}
