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

    private Point move() throws RobotException {
        print(ResponseTypes.SERVER_MOVE.toString());
        return readPosition();
    }

    private void rotate(Direction from, Direction to) throws RobotException {
        int leftTurnCount = (from.value - to.value + 4) % 4;
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

            Point oldPosition = move();

            Point position = move();

            if (oldPosition.equals(position)) {
                turnRight();

                Point helper = position;
                position = move();
                oldPosition = helper;
            }
            assert !oldPosition.equals(position);

            Point target = Point.ZERO;

            while (!position.equals(target)) {
                Direction direction = oldPosition.getVector(position).toDirection();
                Direction desiredDirection = position.getVector(target).toDirection();
                rotate(direction, desiredDirection);

                oldPosition = position;
                position = move();

                if (oldPosition.getVector(position).equals(Point.ZERO)) {
                    desiredDirection = position.getVector(target).toDirection(true);
                    rotate(direction, desiredDirection);

                    position = move();
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

    @Override
    public void print(String message) {
        log(message);
        output.print(message);
        for (int i : communicationSeparator) {
            output.print((char) i);
        }
        output.flush();
    }

    public String readLine(int maxLength) throws RobotException {
        maxLength = maxLength - communicationSeparator.length;
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
                    if (input_buffer.length() > maxLength) {
                        String t = input_buffer.toString();
                        int notMatching = 0;
                        boolean[] generalMessageMatch = new boolean[generalMessages.length];
                        Arrays.fill(generalMessageMatch, true);
                        for (int i = 0; i < input_buffer.length(); i++) {
                            for (int j = 0; j < generalMessages.length; j++) {
                                if (generalMessageMatch[j]) {
                                    String messageString = generalMessages[j].toString();
                                    if (messageString.length() < t.length() ||
                                            messageString.charAt(i) != t.charAt(i)) {
                                        generalMessageMatch[j] = false;
                                        notMatching++;

                                        if (notMatching == generalMessageMatch.length) {
                                            throw new SyntaxException("Message to long for current max length and does not match any of general messages");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            String string_input = input_buffer.toString();
            boolean fullPowerMessage = string_input.equals(ResponseTypes.CLIENT_FULL_POWER.toString());
            if (recharging) {
                if (!fullPowerMessage) {
                    throw new LogicException("Nothing else then FULL_POWER message is expected after recharging status is received, got: '" + string_input + "'");
                }
                recharging = false;
                return readLine(maxLength + communicationSeparator.length);
            } else if (fullPowerMessage) {
                throw new LogicException("Received FULL_POWER message while not recharging");
            }
            if (string_input.equals(ResponseTypes.CLIENT_RECHARGING.toString())) {
                recharging = true;
                return readLine(maxLength + communicationSeparator.length);
            }
            this.server.log(getIdentifier() + " input: " + input_buffer + "\u001B[0m");
            return input_buffer.toString();
        } catch (SocketTimeoutException e) {
            throw new TimeOutException("Timeout after limit of " + (recharging ? TIMEOUT_RECHARGING : TIMEOUT) + "s");
        } catch (IOException e) {
            throw new RobotException("Connection closed.");
        }
    }

    @Override
    protected String goodBeyMessage() {
        return null;
    }

    @Override
    protected String welcomeMessage() {
        return null;
    }
}
