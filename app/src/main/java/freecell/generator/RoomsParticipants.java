package freecell.generator;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table ROOMS_PARTICIPANTS.
 */
public class RoomsParticipants {

    private Long id;
    private Long chat_id;
    private String account;

    public RoomsParticipants() {
    }

    public RoomsParticipants(Long id) {
        this.id = id;
    }

    public RoomsParticipants(Long id, Long chat_id, String account) {
        this.id = id;
        this.chat_id = chat_id;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChat_id() {
        return chat_id;
    }

    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}