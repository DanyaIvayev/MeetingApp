package com.example.meeting;

import com.example.participant.Participant;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Дамир on 09.11.2015.
 */
public class Meeting {
    public enum Priority {URGENT, ROUTINE, POSSIBLE}
    private static int codeId=0;
    private int id;
    private String name;
    private String description;
    private String beginData;
    private String endData;
    private ArrayList<Participant> participants;
    Priority priority = Priority.URGENT;

    public Meeting() {
        id = ++codeId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBeginData() {
        return beginData;
    }

    public void setBeginData(String beginData) {
        this.beginData = beginData;
    }

    public ArrayList<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Participant> participants) {
        this.participants = participants;
    }

    public void addParticipant(Participant participant){
        if(participants==null){
            participants = new ArrayList<Participant>();
        }
        participants.add(participant);
    }


    public String getEndData() {

        return endData;
    }

    public void setEndData(String endData) {
        this.endData = endData;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("{" +"\"id\":\""+id+"\", "
                + "\"name\":");
        result.append("\"" + name + "\"")
                .append(", \"beginData\":" + "\"" + beginData + "\"" +
                        ", \"endData\":" + "\"" + endData + "\"");
        result.append(", \"priority\":\"" + priority.toString() + "\"}");
        return result.toString();
    }

    public String getDetailedInformation(){
        StringBuffer result = new StringBuffer("[{\"description\":\"" +description+"\"");
        if (participants != null) {
            result.append(", \"participants\":" );

            for (Participant p : participants) {
                result.append(participants.toString() + ", ");
            }
            result=new StringBuffer(result.substring(0, result.length()-2));

        }
        result.append( "}]");
        return result.toString();
    }
}
