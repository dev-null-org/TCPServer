package robot_controller;

import robot_controller.exceptions.*;
import server.BasicServerClient;
import server.TCPServer;
import utils.ColorManager;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobotClient extends BasicServerClient {

    public static final int TIMEOUT = 1;
    public static final int TIMEOUT_RECHARGING = 5;
    public static final int[] communicationSeparator = new int[]{7, 8};
    public static final ResponseTypes[] generalMessages = {
            ResponseTypes.CLIENT_FULL_POWER,
            ResponseTypes.CLIENT_RECHARGING
    };
    private static final Pattern CLIENT_OK = Pattern.compile("OK (-?\\d+) (-?\\d+)");
    private static final int[][] keys = {
            {23019, 32037},
            {32037, 29295},
            {18789, 13603},
            {16443, 29533},
            {18189, 21952},
    };

    protected String userName;
    protected long hash;
    protected long confirmHash;
    private boolean recharging = false;
    private Direction direction = null;
    private Point position = null, oldPosition = null;

    public RobotClient(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        super(socket, server, inQueue, logColor);
        this.initiateVariables(socket, server, inQueue, logColor);
    }

    @Override
    public void initiateVariables(Socket socket, TCPServer server, boolean inQueue, ColorManager.Color logColor) {
        super.initiateVariables(socket, server, inQueue, logColor);
        this.recharging = false;
    }

    private void userNameHash(int keyId) {
        byte[] bytes = userName.getBytes();
        long hash = 0;
        for (byte aByte : bytes) {
            hash += aByte;
        }
        hash *= 1000;
        hash %= 65536;
        long serverHash = hash + keys[keyId][0];
        this.hash = serverHash % 65536;
        hash += keys[keyId][1];
        hash %= 65536;
        this.confirmHash = hash;
    }

    private void move() throws RobotException {
        print(ResponseTypes.SERVER_MOVE.toString());
        oldPosition = position;
        position = readPosition();
        if (oldPosition != null && !position.equals(oldPosition))
            direction = oldPosition.getVector(position).toDirection();
    }

    private void rotate(Direction to) throws RobotException {
        log("rotate from: " + direction + " to " + to);
        int leftTurnCount = (direction.value - to.value + 4) % 4;
        switch (leftTurnCount) {
            case 1:
                turnLeft();
                break;
            case 2:
                turnLeft();
                turnLeft();
                break;
            case 3:
                turnRight();
                break;
        }
        direction = to;
    }

    private void turnRight() throws RobotException {
        print(ResponseTypes.SERVER_TURN_RIGHT.toString());
        readPosition();
    }

    private void turnLeft() throws RobotException {
        print(ResponseTypes.SERVER_TURN_LEFT.toString());
        readPosition();
    }

    @Override
    public void run() {
        String input;
        this.fromQueueStart();
        try {
            userName = readLine(20);

            print(ResponseTypes.SERVER_KEY_REQUEST.toString());
            input = readLine(5);
            int keyId;
            try {
                keyId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                throw new SyntaxException("Key ID is not a number.");
            }
            if (keyId < 0 || keyId >= keys.length) {
                throw new OutOfBoundsException("Key id out of range max(" + (keys.length - 1) + ") provided: " + keyId);
            }
            userNameHash(keyId);
            print(String.valueOf(hash));

            input = readLine(7);
            long confirmHash;
            try {
                confirmHash = Long.parseLong(input);
            } catch (NumberFormatException e) {
                throw new SyntaxException("Confirmation hash is not a number.");
            }
            if (confirmHash == this.confirmHash) {
                print(ResponseTypes.SERVER_OK.toString());
            } else {
                throw new LoginException("Confirmation hash doesn't match.");
            }

            move();
            move();

            if (oldPosition.equals(position)) {
                turnRight();
                move();
            }
            assert !oldPosition.equals(position);

            Point target = Point.ZERO;

            while (!position.equals(target)) {
                Direction desiredDirection = position.getVector(target).toDirection();
                rotate(desiredDirection);

                move();

                if (oldPosition.getVector(position).equals(Point.ZERO)) {
                    desiredDirection = position.getVector(target).toDirection(true);
                    rotate(desiredDirection);

                    move();
                    assert !oldPosition.equals(position);
                }
            }

            print(ResponseTypes.SERVER_PICK_UP.toString());
            String secret = readLine(100);
            log(secret);

            print(ResponseTypes.SERVER_LOGOUT.toString());
        } catch (SyntaxException e) {
            logError(e.getMessage());
            print(ResponseTypes.SERVER_SYNTAX_ERROR.toString());
        } catch (LoginException e) {
            logError(e.getMessage());
            print(ResponseTypes.SERVER_LOGIN_FAILED.toString());
        } catch (LogicException e) {
            logError(e.getMessage());
            print(ResponseTypes.SERVER_LOGIC_ERROR.toString());
        } catch (OutOfBoundsException e) {
            logError(e.getMessage());
            print(ResponseTypes.SERVER_KEY_OUT_OF_RANGE_ERROR.toString());
        } catch (RobotException e) {
            logError(e.getMessage());
        }
        close();
    }

    public Point readPosition() throws RobotException {
        String input = readLine(12);
        Matcher m = CLIENT_OK.matcher(input);
        if (m.matches()) {
            String x = m.group(1);
            String y = m.group(2);
            return new Point(x, y);
        }
        throw new SyntaxException("Client OK doesn't match the format required.");
    }

    /**
     * Print message and append separator then flush
     * @param message string message without separator
     */
    @Override
    public void print(String message) {
        log(message);
        output.print(message);
        for (int i : communicationSeparator) {
            output.print((char) i);
        }
        output.flush();
    }

    /**
     * Reads message/line from the network socket separated by {@link #communicationSeparator}
     *
     * @param maxLength max length for the message, gets ignored if {@link #generalMessages} is inputted
     * @return string message without the separator
     * @throws RobotException throws:
     *                        - SyntaxException if message length is more than the maximum given
     *                        - LogicException if message received in wrong state of the bot (example 2 recharging commands in a row)
     *                        - TimeOutException if robot did not receive any communication in minimum time period based on the state
     *                        - RobotException when socket is unexpectedly closed
     */
    public String readLine(int maxLength) throws RobotException {
        StringBuilder input_buffer = new StringBuilder();
        StringBuilder match_buffer = new StringBuilder();
        try {
            int end_match = 0;
            while (end_match < communicationSeparator.length) {
                if (!recharging) {
                    socket.setSoTimeout(TIMEOUT * 1000);
                } else {
                    socket.setSoTimeout(TIMEOUT_RECHARGING * 1000);
                }
                int input = this.input.read();
                char input_char = (char) input;
                assert input == input_char || input == -1;

                if (input == -1) {
                    close();
                    return null;
                }

                if (input == communicationSeparator[end_match]) {
                    match_buffer.append(input_char);
                    end_match++;
                } else {
                    if (match_buffer.length() > 0) {
                        input_buffer.append(match_buffer);
                        match_buffer.setLength(0);
                    }
                    end_match = 0;
                    input_buffer.append(input_char);
                    if (!checkMessageLength(input_buffer.toString(), maxLength - communicationSeparator.length)) {
                        throw new SyntaxException("Message to long for current max length and does not match any of general messages");
                    }
                }
            }
            String input_string = input_buffer.toString();
            boolean fullPowerMessage = input_string.equals(ResponseTypes.CLIENT_FULL_POWER.toString());
            if (recharging) {
                if (!fullPowerMessage) {
                    throw new LogicException("Nothing else then FULL_POWER message is expected after recharging status is received, got: '" + input_string + "'");
                }
                recharging = false;
                return readLine(maxLength);
            } else if (fullPowerMessage) {
                throw new LogicException("Received FULL_POWER message while not recharging");
            }
            if (input_string.equals(ResponseTypes.CLIENT_RECHARGING.toString())) {
                recharging = true;
                return readLine(maxLength);
            }
            // if length of the input is bigger than maximum and does not exactly match any of the general commands
            if (input_string.length() > maxLength - communicationSeparator.length)
                throw new SyntaxException("Message to long for current max length and does not match any of general messages");
            this.server.log(getIdentifier() + " input: " + input_string + "\u001B[0m");
            return input_string;
        } catch (SocketTimeoutException e) {
            throw new TimeOutException("Timeout after limit of " + (recharging ? TIMEOUT_RECHARGING : TIMEOUT) + "s");
        } catch (IOException e) {
            throw new RobotException("Connection closed.");
        }
    }

    /**
     * Check if message input length is more than maxLength
     * if it is it will check if message starts with any of the general commands
     * that can be used any time
     *
     * @param input     current command part
     * @param maxLength max message length
     * @return boolean true if message is valid, else otherwise
     */
    private boolean checkMessageLength(String input, int maxLength) {
        if (input.length() <= maxLength) return true;
        int notMatching = 0;
        boolean[] generalMessageMatch = new boolean[generalMessages.length];
        Arrays.fill(generalMessageMatch, true);
        for (int i = 0; i < input.length(); i++) {
            for (int j = 0; j < generalMessages.length; j++) {
                if (generalMessageMatch[j]) {
                    String messageString = generalMessages[j].toString();
                    if (messageString.length() < input.length() ||
                            messageString.charAt(i) != input.charAt(i)) {
                        generalMessageMatch[j] = false;
                        notMatching++;

                        if (notMatching == generalMessageMatch.length) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Overriding the welcome and bey message we don't need those for this project.
     */

    @Override
    protected String goodBeyMessage() {
        return null;
    }

    @Override
    protected String welcomeMessage() {
        return null;
    }
}
