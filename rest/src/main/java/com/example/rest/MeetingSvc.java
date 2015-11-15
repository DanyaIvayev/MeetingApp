package com.example.rest;

/**
 * Created by Дамир on 10.11.2015.
 */
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Request;

import com.example.meeting.Meeting;
import com.example.participant.Participant;
import com.sun.jersey.api.view.Viewable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

@Path("/meeting")
public class MeetingSvc {
    @Context
    ServletContext _context;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;
    public static ArrayList<Meeting> meetings = new ArrayList<>();
    private String username = "user";
    private String password = "password";

    @GET
    @Path("/getMeeting")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getMeeting(@QueryParam("username") String username,
                             @QueryParam("password") String password) {
        if(this.username.equals(username)&& this.password.equals(password))
            return meetings.toString();
        else
            return "{\"response\":\"false\"}";

    }

    @GET
    @Path("/setMeeting")
    public String setMeeting(@QueryParam("name") String name,
                               @QueryParam("description") String description,
                               @QueryParam("begindate") String begindate,
                               @QueryParam("enddate") String enddate,
                               @QueryParam("priority") String prioty
    ) {
        addMeeting(name, description, begindate, enddate, prioty);
        return meetings.toString();
    }

//    @POST
//    @Path("/mobileSetMeeting")
//    public void setMeetings(String data){
//        addMeeting(data);
//    }

    @POST
    @Path("/getDescription")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getDescription(String data) {
        String res = null;
        try {
            String decodedValue1 = URLDecoder.decode(data, "UTF-8");
            String[] splitStr = decodedValue1.split("[=&]");
            String name = splitStr[1];
            String begindate = splitStr[3];
            String enddate = splitStr[5].substring(0, splitStr[5].length()-2);
            Meeting result = findMeeting(name, begindate, enddate);
            if (result != null)
                res = "\"description\":"+result.getDescription();
            else
                res = "[]";
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } finally {
            return res;
        }
    }

    @PUT
    @Path("/addParticipant")
    public void addParticipant(String data) {
        try {
            String decodedValue1 = URLDecoder.decode(data, "UTF-8");
            String[] splitStr = decodedValue1.split("[=&]");
            String name = splitStr[1];
            String begindate = splitStr[3];
            String enddate = splitStr[5];
            Participant participant = new Participant();
            participant.setLastName(splitStr[7]);
            participant.setFirstName(splitStr[9]);
            participant.setPatronymic(splitStr[11]);
            participant.setPost(splitStr[13].substring(0, splitStr[13].length() - 2));
            Meeting m = findMeeting(name, begindate, enddate);

            if (m != null) {
                ArrayList<Participant> participants = m.getParticipants();
                if (participants == null)
                    participants = new ArrayList<Participant>();
                participants.add(participant);
                m.setParticipants(participants);
                meetings.add(m);
            }


        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    @DELETE
    @Path("/deleteMeeting")
    public void deleteMeeting(String data) {
        try {
            String decodedValue1 = URLDecoder.decode(data, "UTF-8");
            String[] splitStr = decodedValue1.split("[=&]");
            String name = splitStr[1];
            String begindate = splitStr[3];
            String enddate = splitStr[5].substring(0, splitStr[5].length()-2);
            Meeting m = findMeeting(name, begindate, enddate);
            if (m != null) {
                meetings.remove(m);
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }


    private Meeting findMeeting(String name, String begindate, String enddate) {
        Meeting meeting = null;
        for (Meeting m : meetings) {
            if (m.getName().equals(name) && m.getBeginData().equals(begindate) && m.getEndData().equals(enddate)) {
                meeting = m;
                return m;
            }
        }
        return meeting;
    }
    @POST
    @Path("/getMeetOnDes")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String findMeetingOnDescription(String description){
        Meeting meeting =null;
        description = description.substring(description.indexOf("=")+1);
        description = description.substring(0, description.length()-2);
        for(Meeting m : meetings){
            if(m.getDescription().equals(description))
                meeting=m;
        }
        if(meeting!=null)
            return meeting.toString();
        else
            return null;
    }

    private void addMeeting(String name,
                            String description,
                            String begindate,
                            String enddate,
                            String prioty) {
        Meeting meeting = null;
        try {
            //String decodedValue1 = URLDecoder.decode(data, "UTF-8");
            //String[] splitStr = decodedValue1.split("[=&]");
            meeting = new Meeting();
            meeting.setName(URLDecoder.decode(name, "UTF-8"));
            meeting.setDescription(URLDecoder.decode(description, "UTF-8"));
            meeting.setBeginData(URLDecoder.decode(begindate, "UTF-8"));
            meeting.setEndData(URLDecoder.decode(enddate, "UTF-8"));
            String priority = URLDecoder.decode(prioty, "UTF-8");
            if(priority.endsWith("\r\n"))
                priority = priority.substring(0, priority.length()-2);
            Meeting.Priority p = meeting.getPriority();
            switch (priority) {
                case "Срочная": {
                    p = Meeting.Priority.URGENT;
                }
                break;
                case "Плановая": {
                    p = Meeting.Priority.ROUTINE;
                }
                break;
                case "По возможности": {
                    p = Meeting.Priority.POSSIBLE;
                }
                break;
            }
            meeting.setPriority(p);
            meetings.add(meeting);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }
}
