package chat;

public class Message {
    protected User author;
    private String content;

    public Message(User author, String content) {
        this.author = author;
        this.content = content;
    }

    @Override
    public String toString() {
        return author.toString() +":" + content;
    }
}
