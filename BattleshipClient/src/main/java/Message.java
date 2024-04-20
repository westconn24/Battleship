/*
 * WARNING: This class must be identical to the message class in the server. If it is not, no messages will be accepted
 * WARNING: This class does not dictate how the data should be gathered. The data can be gathered from text input,
 * drop down menus, etc.
 * */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    private String message; // Message to be sent
    private String groupName; // either name of new group or name of group being sent to
    private String userName; // either name of new username or name of user message is being sent to
    private String userNameToSendTo;

    private Boolean isNewUser; // False if user is existing, True if user is new
    private Boolean isNewGroup; // False is user is not creating a group, True otherwise
    private Boolean isSendAll; // False if the message is private, True otherwise
    private Boolean isServer; // Only the server is allowed to have this prefix true
    private Boolean isDeletedUser; // Tells if the user should be removed from the local list
    private Boolean isWhisper; // If the message is a pm
    public Boolean isServerGroupMes; // back end group stuff
    public Boolean isToGroup; // so server knows if the message is to a group

    private ArrayList<String> userNames; // List of names for the server
    public ArrayList<String> groupNames; // List of names for group to be created with
    public HashMap<String, ArrayList<String>> actualGroups; // Map of group names with the users in those groups

    // getters and setters for private variables
    public void setMessage(String message) {this.message = message;}
    public String getMessage() {return message;}

    public void setGroupName(String groupName) {this.groupName = groupName;}
    public String getGroupName() {return groupName;}

    public void setUserName(String userName) {this.userName = userName;}
    public String getUserName() {return userName;}

    public void setUserNameToSendTo(String userNameToSendTo) {this.userNameToSendTo = userNameToSendTo;}
    public String getUserNameToSendTo() {return userNameToSendTo;}

    public void setIsNewUser(Boolean isNewUser) {this.isNewUser = isNewUser;}
    public Boolean getIsNewUser() {return isNewUser;}

    public void setIsNewGroup(Boolean isNewGroup) {this.isNewGroup = isNewGroup;}
    public Boolean getIsNewGroup() {return isNewGroup;}

    public void setIsSendAll(Boolean isSendAll) {this.isSendAll = isSendAll;}
    public Boolean getIsSendAll() {return isSendAll;}

    public void setIsServer(Boolean isServer) {this.isServer = isServer;}
    public Boolean getIsServer() {return isServer;}

    public void setIsDeletedUser(Boolean isDeletedUser) {this.isDeletedUser = isDeletedUser;}
    public Boolean getIsDeletedUser() {return isDeletedUser;}

    public void setIsWhisper(Boolean isWhisper) {this.isWhisper = isWhisper;}
    public Boolean getIsWhisper() {return isWhisper;}

    public void addUsers(ArrayList<String> userNames) {this.userNames = userNames;}
    public ArrayList<String> getUsers() {return userNames;}

    // todo: Add anymore attributes that can be helpful
}
