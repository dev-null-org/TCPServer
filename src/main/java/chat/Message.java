package chat;

public class Message {
    protected final User author;
    private final String content;

    public Message(User author, String content) {
        this.author = author;
        this.content = content;
    }

    @Override
    public String toString() {
        return author.toString() +":" + content+"\u001B[0m";
    }
}
