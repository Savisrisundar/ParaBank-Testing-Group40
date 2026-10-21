package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.pulkit.AssignedTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Searches are checked against transactions created in the same test, not old shared data. */
@Tag("pulkit")
public class FindTransactionsTest extends AssignedTestSupport {
    private Payment knownPayment() { return pay(freshAccount(), new BigDecimal("15.00")); }

    @Test @DisplayName("TXN-001: amount search includes known payment and excludes another amount")
    void TC_TXN_001_findByAmount() {
        Payment first = knownPayment();
        Payment other = pay(first.source(), new BigDecimal("23.00"));
        openFind(first.source().id());
        typeId("amount", "15.00");
        List<Transaction> actual = search("findByAmount");
        assertContainsTransaction(actual, first.transaction());
        assertTrue(actual.stream().noneMatch(t -> t.id().equals(other.transaction().id())), "Different amount must be excluded");
        assertAll("Every result matches amount", actual.stream().map(t ->
                (org.junit.jupiter.api.function.Executable) () -> {
                    BigDecimal value = t.debit() != null ? t.debit() : t.credit();
                    assertNotNull(value);
                    assertMoney(new BigDecimal("15.00"), value, "Search result amount");
                }));
    }

    @Test @DisplayName("TXN-002: unmatched amount yields completed empty results")
    void TC_TXN_002_noMatchingAmount() {
        Payment payment = knownPayment();
        openFind(payment.source().id());
        typeId("amount", "999999999.99");
        assertEquals(0, search("findByAmount").size(), "Unused amount must return no transactions");
    }

    @Test @DisplayName("TXN-003: inclusive date range includes its known boundary transaction")
    void TC_TXN_003_findByDateRange() {
        Payment payment = knownPayment();
        LocalDate boundary = payment.transaction().date();
        openFind(payment.source().id());
        typeId("fromDate", DATE.format(boundary));
        typeId("toDate", DATE.format(boundary));
        List<Transaction> actual = search("findByDateRange");
        assertContainsTransaction(actual, payment.transaction());
        assertTrue(actual.stream().allMatch(t -> t.date().equals(boundary)), "Every date must be within the inclusive one-day range");
    }

    @Test @DisplayName("TXN-004: date before fixture data yields completed empty results")
    void TC_TXN_004_findByDateWithoutResults() {
        Payment payment = knownPayment();
        openFind(payment.source().id());
        typeId("transactionDate", DATE.format(payment.transaction().date().minusYears(1)));
        assertTrue(search("findByDate").isEmpty());
    }

    @Test @DisplayName("TXN-005: transaction ID search returns the exact known record")
    void TC_TXN_005_findById() {
        Payment payment = knownPayment();
        openFind(payment.source().id());
        typeId("transactionId", payment.transaction().id());
        List<Transaction> actual = search("findById");
        assertEquals(1, actual.size(), "ID search must return exactly one record");
        assertContainsTransaction(actual, payment.transaction());
    }

    @Test @DisplayName("TXN-006: single-date search returns the known payment")
    void TC_TXN_006_findByDate() {
        Payment payment = knownPayment();
        openFind(payment.source().id());
        typeId("transactionDate", DATE.format(payment.transaction().date()));
        List<Transaction> actual = search("findByDate");
        assertContainsTransaction(actual, payment.transaction());
        assertTrue(actual.stream().allMatch(t -> t.date().equals(payment.transaction().date())));
    }

    private void invalidSearch(String field, String value, String button, String error, String message) {
        Account account = freshAccount();
        openFind(account.id());
        typeId(field, value);
        driver.findElement(By.id(button)).click();
        assertEquals(message, visibleText(By.id(error)));
        assertTrue(visible(By.id("formContainer")));
        assertFalse(visible(By.id("resultContainer")), "Invalid input must not display search results");
        assertFalse(visible(By.id("errorContainer")), "Technical failure is not validation");
    }

    @Test @DisplayName("TXN-007: nonnumeric ID shows field validation")
    void TC_TXN_007_invalidTransactionId() {
        invalidSearch("transactionId", "ABC", "findById", "transactionIdError", "Invalid transaction ID");
    }

    @Test @DisplayName("TXN-008: malformed date shows field validation")
    void TC_TXN_008_invalidDateFormat() {
        invalidSearch("transactionDate", "not-a-date", "findByDate", "transactionDateError", "Invalid date format");
    }

    @Test @DisplayName("TXN-009: nonnumeric amount shows field validation")
    void TC_TXN_009_invalidAmount() {
        invalidSearch("amount", "ABC", "findByAmount", "amountError", "Invalid amount");
    }

    @Test @DisplayName("TXN-010: date range excluding fixture data returns no records")
    void TC_TXN_010_excludingDateRange() {
        Payment payment = knownPayment();
        LocalDate end = payment.transaction().date().minusYears(1);
        openFind(payment.source().id());
        typeId("fromDate", DATE.format(end.minusDays(2)));
        typeId("toDate", DATE.format(end));
        assertTrue(search("findByDateRange").isEmpty(), "Out-of-range transaction must be excluded");
    }
}
