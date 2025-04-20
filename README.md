# Exchange Desktop Application

This is a JavaFX-based desktop application for peer-to-peer currency exchange. The application allows users to register, authenticate, create and request currency exchange offers (USD/LBP), view statistics, receive notifications, and interact with a chatbot. It communicates with a RESTful Flask backend deployed on Railway and uses a cloud MySQL database.

---

## Features

- Secure login and registration with JWT token management
- Create buy or sell offers (USD in exchange for LBP and vice versa)
- Browse available offers and submit requests
- View and manage your own offers and incoming requests
- Notifications for accepted or rejected requests and completed transactions
- Real-time currency conversion between supported currencies
- Chatbot support for exchange-related queries
- User statistics, balance, gamification levels and badge achievements

---

## Prerequisites

- Java JDK 17 or later
- Maven 3.8 or later
- Internet connection (for API communication)
- Access to the backend service URL (already deployed on Railway)

---

## Getting Started

### Clone the Repository


git clone https://github.com/mmhachem/exchange-desktop.git
cd exchange-desktop

Build the Project:
mvn clean install
Run the Application


You can run the application via Maven:
mvn javafx:run


Or by running the built JAR:
java -jar target/exchange-desktop.jar


Alternatively, open the project in IntelliJ IDEA or another IDE and run the Main.java class from the com.mmhachem.exchange package.

## How to Use:
Register a new account or log in with existing credentials.

Create a new exchange offer by selecting offer type, entering the USD amount and rate.

Browse available offers and request any that match your needs.

Manage your offers from the "My Offers" tab and view incoming requests.

Accept or reject requests directly from the interface.

Check your balance and statistics in the respective sections.

Use the converter to convert between currencies.

Use the chatbot to ask exchange-related questions.

## Security Measures
All communication is secured using bearer tokens (JWT).

User input is validated and sanitized to prevent injection or abuse.

No credentials are stored on the frontend.

Sensitive actions (like accepting offers) are rate-limited and state-checked.

Backend responses are gracefully handled with error messages.

## Technologies Used:
JavaFX
Maven
Retrofit (REST API)
OpenAI API (chatbot)
Flask (backend, not included here)
MySQL (database)


## Author
Developed by Mohamad M. Hachem