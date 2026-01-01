package it.unimib.CasHub.source.transaction;

import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.utils.Constants;
import it.unimib.CasHub.utils.JSONParserUtils;

public class TransactionMockDataSource extends BaseTransactionDataSource {

    private final JSONParserUtils jsonParserUtils;

    public TransactionMockDataSource(JSONParserUtils jsonParserUtils) {
        this.jsonParserUtils = jsonParserUtils;
    }

    @Override
    public void getTransactions() {
        List<TransactionEntity> mockTransactions = jsonParserUtils.parseTransactions(Constants.SAMPLE_TRANSACTIONS_JSON);
        if (mockTransactions != null) {
            callback.onTransactionsSuccess(mockTransactions);
        } else {
            callback.onTransactionsFailure(new Exception("Error parsing mock transactions"));
        }
    }

    @Override
    public void insertTransaction(TransactionEntity transaction) {
        // In a mock data source, you might not need to implement this
        // or you could add the transaction to an in-memory list.
    }
    @Override
    public void deleteTransaction(String transactionId) {
        // In a mock data source, you might not need to implement this
    }
}
