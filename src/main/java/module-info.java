module com.mmhachem.exchange {
    requires javafx.controls;
    requires javafx.fxml;
    requires retrofit2;
    requires java.sql;
    requires gson;
    requires retrofit2.converter.gson;
    requires java.prefs;
    requires okhttp3;

    opens com.mmhachem.exchange to javafx.fxml;
    opens com.mmhachem.exchange.api.model to javafx.base, gson;
    opens com.mmhachem.exchange.transactions to javafx.fxml;

    exports com.mmhachem.exchange;
    exports rates;
    opens rates to javafx.fxml;
    exports com.mmhachem.exchange.login;
    opens com.mmhachem.exchange.login to javafx.fxml;
    exports com.mmhachem.exchange.register;
    opens com.mmhachem.exchange.register to javafx.fxml;
}

