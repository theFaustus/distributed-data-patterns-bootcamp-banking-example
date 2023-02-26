package net.chrisrichardson.bankingexample.apigateway.apigateway;

import net.chrisrichardson.bankingexample.apigateway.apigateway.proxies.AccountServiceProxy;
import net.chrisrichardson.bankingexample.apigateway.apigateway.proxies.CustomerServiceProxy;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.apigateway.AccountWithCustomer;
import net.chrisrichardson.bankingexample.accountservice.common.GetAccountResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

public class AccountHandlers {

    private AccountServiceProxy orderService;
    private CustomerServiceProxy customerService;

    public AccountHandlers(AccountServiceProxy orderService, CustomerServiceProxy customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @NotNull
    public Mono<ServerResponse> getAccountWithCustomer(ServerRequest serverRequest) {
        String accountId = serverRequest.pathVariable("accountId");

        return orderService.findAccountById(accountId)
                .flatMap(maybeAccount -> maybeAccount.map(account -> getAccountWithCustomerMono(account)
                                .flatMap(AccountHandlers::serverResponse)).orElseGet(() -> ServerResponse.notFound().build()));

    }

    private static Mono<ServerResponse> serverResponse(AccountWithCustomer accountWithCustomer) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(accountWithCustomer));
    }

    private Mono<AccountWithCustomer> getAccountWithCustomerMono(GetAccountResponse account) {
        return customerService.findCustomerById(account.getAccountInfo().getCustomerId()).map(customer -> new AccountWithCustomer(account, customer));
    }

}
