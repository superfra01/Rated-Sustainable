package utilities;

import java.util.regex.Pattern;

public class FieldValidator {

    // Validazione username
    public static boolean validateUsername(final String username) { // Parametro final
        // Regex: solo lettere, numeri, underscore, e punto. Lunghezza 3-30.
        final String usernameRegex = "^[a-zA-Z0-9_.]{3,30}$"; // Locale final
        return Pattern.matches(usernameRegex, username);
    }

    // Validazione password
    public static boolean validatePassword(final String password) { // Parametro final
        // Regex: almeno una maiuscola, una minuscola, un numero, un carattere speciale. Lunghezza 8-64.
        final String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$"; // Locale final
        return Pattern.matches(passwordRegex, password);
    }

    // Validazione email
    public static boolean validateEmail(final String email) { // Parametro final
        // Regex: formato email valido secondo standard base
        final String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; // Locale final
        return Pattern.matches(emailRegex, email);
    }
}