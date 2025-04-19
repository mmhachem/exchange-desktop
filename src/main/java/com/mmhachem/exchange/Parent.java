package com.mmhachem.exchange;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Parent implements Initializable, OnPageCompleteListener {
    public static Parent instance;
    public BorderPane borderPane;
    public Button transactionButton;
    public Button loginButton;
    public Button registerButton;
    public Button logoutButton;
    public Button ratesButton;
    public Button statisticsButton;
    public Button offersButton;
    public Button profileButton;
    public Button notificationsButton;
    public Button balanceButton;
    public Button chatButton;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;   // ✅ Save the current instance
        updateNavigation();
    }

    public void ratesSelected() {
        swapContent(Section.RATES);
    }

    public void transactionsSelected() {
        swapContent(Section.TRANSACTIONS);
    }

    public void loginSelected() {
        swapContent(Section.LOGIN);
    }
    public void chatSelected() {
        swapContent(Section.CHAT);
    }


    public void registerSelected() {
        swapContent(Section.REGISTER);
    }

    public void logoutSelected() {
        Authentication.getInstance().deleteToken();
        swapContent(Section.RATES);
    }

    public void statisticsSelected() {
        swapContent(Section.STATISTICS);
    }

    public void offersSelected() {
        swapContent(Section.OFFERS);
    }

    public void profileSelected() {
        swapContent(Section.PROFILE);
    }
    public void notificationsSelected() {
        swapContent(Section.NOTIFICATIONS);
    }

    public void balanceSelected() {
        swapContent(Section.BALANCE);
    }


    public void swapContent(Section section) {
        try {
            assert section.getResource() != null;
            URL url = getClass().getResource(section.getResource());
            FXMLLoader loader = new FXMLLoader(url);
            borderPane.setCenter(loader.load());
            if (section.doesComplete()) {
                ((PageCompleter) loader.getController()).setOnPageCompleteListener(this);
            }
            updateNavigation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNavigation() {
        boolean authenticated = Authentication.getInstance().getToken() != null;

        transactionButton.setManaged(authenticated);
        transactionButton.setVisible(authenticated);
        loginButton.setManaged(!authenticated);
        loginButton.setVisible(!authenticated);
        registerButton.setManaged(!authenticated);
        registerButton.setVisible(!authenticated);
        logoutButton.setManaged(authenticated);
        logoutButton.setVisible(authenticated);
        statisticsButton.setManaged(authenticated);
        statisticsButton.setVisible(authenticated);
        offersButton.setManaged(authenticated);
        offersButton.setVisible(authenticated);
        profileButton.setManaged(authenticated);
        profileButton.setVisible(authenticated);
        notificationsButton.setManaged(authenticated);
        notificationsButton.setVisible(authenticated);
        balanceButton.setManaged(authenticated);
        balanceButton.setVisible(authenticated);
        chatButton.setManaged(authenticated);
        chatButton.setVisible(authenticated);





    }


    @Override
    public void onPageCompleted() {
        swapContent(Section.RATES);
    }

    // ✅ Changed to public static
    public static enum Section {
        RATES,
        TRANSACTIONS,
        STATISTICS,
        OFFERS,
        PROFILE,
        LOGIN,
        REGISTER,
        NOTIFICATIONS,
        BALANCE,
        CHAT;

        public String getResource() {
            return switch (this) {
                case RATES -> "/com/mmhachem/exchange/rates/rates.fxml";
                case TRANSACTIONS -> "/com/mmhachem/exchange/transactions/transactions.fxml";
                case STATISTICS -> "/com/mmhachem/exchange/statistics/statistics.fxml";
                case OFFERS -> "/com/mmhachem/exchange/offers/offers.fxml";
                case PROFILE -> "/com/mmhachem/exchange/profile/profile.fxml";
                case LOGIN -> "/com/mmhachem/exchange/login/login.fxml";
                case REGISTER -> "/com/mmhachem/exchange/register/register.fxml";
                case NOTIFICATIONS -> "/com/mmhachem/exchange/notifications/notifications.fxml";
                case BALANCE -> "/com/mmhachem/exchange/balance/balance.fxml";
                case CHAT -> "/com/mmhachem/exchange/chat/chat.fxml";


            };
        }

        public boolean doesComplete() {
            return switch (this) {
                case LOGIN, REGISTER -> true;
                default -> false;
            };
        }
    }
}
