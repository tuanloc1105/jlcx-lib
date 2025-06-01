package vn.com.lcx.jpa.context;

public interface JpaConstant {

    String TRANSACTION_IS_OPENED = "transaction.is.opened";
    String TRANSACTION_ISOLATION = "transaction.isolation";
    String TRANSACTION_MODE = "transaction.mode";
    String TRANSACTION_ON_ROLLBACK = "transaction.on.rollback";
    String SESSION_KEY_TEMPLATE = "session-%s";
    String TRANSACTION_KEY_TEMPLATE = "trans-%s";

    int USE_EXISTING_TRANSACTION_MODE = 1;
    int CREATE_NEW_TRANSACTION_MODE = 2;

}
