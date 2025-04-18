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

    // ✅ Added only what's missing below:
    exports com.mmhachem.exchange.statistics;
    opens com.mmhachem.exchange.statistics to javafx.fxml;

    exports com.mmhachem.exchange.offers;
    opens com.mmhachem.exchange.offers to javafx.fxml;

    exports com.mmhachem.exchange.profile;
    opens com.mmhachem.exchange.profile to javafx.fxml;
}
