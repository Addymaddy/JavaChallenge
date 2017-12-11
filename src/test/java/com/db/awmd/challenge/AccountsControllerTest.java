package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  //test happy case for transfer
  @Test
  public void transferAmount() throws Exception {
    Account account1 = new Account("Id-123");
    Account account2 = new Account("Id-124");

    account1.setBalance(BigDecimal.valueOf(100));
    account2.setBalance(BigDecimal.valueOf(20));
    accountsService.createAccount(account1);
    accountsService.createAccount(account2);

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\":\"Id-124\", \"amount\":10}")).andExpect(status().isAccepted());

    account1 = accountsService.getAccount("Id-123");
    account2 = accountsService.getAccount("Id-124");
    assertThat(account1.getBalance().intValue()).isEqualTo(90);
    assertThat(account2.getBalance().intValue()).isEqualTo(30);
  }
  //test insufficient balance --> mock account service here
  @Test
  public void transferWithInsufficientBalance() throws Exception {
    Account account1 = new Account("Id-123");
    Account account2 = new Account("Id-124");

    account1.setBalance(BigDecimal.valueOf(100));
    account2.setBalance(BigDecimal.valueOf(20));
    accountsService.createAccount(account1);
    accountsService.createAccount(account2);

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\":\"Id-124\", \"amount\":200}")).andExpect(status().isExpectationFailed());
  }
  //test invalid account exception
  @Test
  public void transferWithInvalidAccount() throws Exception {
    Account account1 = new Account("Id-123");

    account1.setBalance(BigDecimal.valueOf(100));
    accountsService.createAccount(account1);

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\":\"Id-124\", \"amount\":200}")).andExpect(status().isExpectationFailed());
  }
  //test the transaction with no body
  @Test
  public void transferNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  //test the transaction with no from account
  @Test
  public void transferWithNoFromAccount() throws Exception {

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"toAccountId\":\"Id-124\", \"amount\":200}")).andExpect(status().isBadRequest());
  }

  //test the transaction with no to account
  @Test
  public void transferWithNoToAccount() throws Exception {

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"amount\":200}")).andExpect(status().isBadRequest());
  }

  //test the transaction with no amount to transfer
  @Test
  public void transferWithNoTransferAmount() throws Exception {

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\":\"Id-124\"}")).andExpect(status().isBadRequest());
  }

  //test the transaction with  negative amount to transfer
  @Test
  public void transferWithNegTransferAmount() throws Exception {

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\":\"Id-124\", \"amount\":-200}")).andExpect(status().isBadRequest());
  }
}
