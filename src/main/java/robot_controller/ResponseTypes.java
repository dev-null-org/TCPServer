package robot_controller;

public enum ResponseTypes {
    SERVER_MOVE,
    SERVER_TURN_LEFT,
    SERVER_TURN_RIGHT,
    SERVER_PICK_UP,
    SERVER_LOGOUT,
    SERVER_KEY_REQUEST,
    SERVER_OK,
    SERVER_LOGIN_FAILED,
    SERVER_SYNTAX_ERROR,
    SERVER_LOGIC_ERROR,
    SERVER_KEY_OUT_OF_RANGE_ERROR,
    CLIENT_RECHARGING,
    CLIENT_FULL_POWER,
    ;

    @Override
    public String toString() {
        switch (this) {
            case SERVER_MOVE:
                // Příkaz pro pohyb o jedno pole vpřed
                return "102 MOVE";
            case SERVER_TURN_LEFT:
                // Příkaz pro otočení doleva
                return "103 TURN LEFT";
            case SERVER_TURN_RIGHT:
                // Příkaz pro otočení doprava
                return "104 TURN RIGHT";
            case SERVER_PICK_UP:
                // Příkaz pro vyzvednutí zprávy
                return "105 GET MESSAGE";
            case SERVER_LOGOUT:
                // Příkaz pro ukončení spojení po úspěšném vyzvednutí zprávy
                return "106 LOGOUT";
            case SERVER_KEY_REQUEST:
                // Žádost serveru o Key ID pro komunikaci
                return "107 KEY REQUEST";
            case SERVER_OK:
                // Kladné potvrzení
                return "200 OK";
            case SERVER_LOGIN_FAILED:
                // Nezdařená autentizace
                return "300 LOGIN FAILED";
            case SERVER_SYNTAX_ERROR:
                // Chybná syntaxe zprávy
                return "301 SYNTAX ERROR";
            case SERVER_LOGIC_ERROR:
                // Zpráva odeslaná ve špatné situaci
                return "302 LOGIC ERROR";
            case SERVER_KEY_OUT_OF_RANGE_ERROR:
                // Key ID není v očekávaném rozsahu
                return "303 KEY OUT OF RANGE";
            case CLIENT_RECHARGING:
                // Robot se začal dobíjet a přestal reagovat na zprávy.		12
                return "RECHARGING";
            case CLIENT_FULL_POWER:
                // Robot doplnil energii a opět příjímá příkazy.		12
                return "FULL POWER";
        }
        return "ERROR";
    }
}
