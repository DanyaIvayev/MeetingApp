package com.example.rest;

/**
 * Created by Дамир on 10.11.2015.
 */

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Request;

import com.example.meeting.Meeting;
import com.example.participant.Participant;
import com.sun.grizzly.util.Charsets;
import com.sun.jersey.api.view.Viewable;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.ext.Provider;


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
    public static final String APP_MEETING_NAME = "name";     // название встречи
    public static final String APP_BEGIN_DATE = "begindate";  //дата начала
    public static final String APP_END_DATE = "enddate";      //дата конца
    public static final String APP_PREFERENCES_NAME = "username"; // имя пользователя
    public static final String APP_PREFERENCES_PASSWORD = "password"; // пароль
    public static final String APP_ID = "id";

    @GET
    @Path("/getMeeting")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getMeeting(@QueryParam("username") String username,
                             @QueryParam("password") String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            ArrayList<Meeting> currentMeetings = findMeetingsForDate();
            return currentMeetings.toString();
        } else
            return "[{\"response\":\"false\"}]";

    }

    @GET
    @Path("/setMeeting")
    public String setMeeting(@QueryParam("name") String name,
                             @QueryParam("description") String description,
                             @QueryParam("begindate") String begindate,
                             @QueryParam("enddate") String enddate,
                             @QueryParam("priority") String priority
    ) {
        addMeeting(name, description, begindate, enddate, priority);
        return meetings.toString();
    }

    @POST
    @Path("/mobileSetMeeting")
    public void setMeetings(@QueryParam("name") String name,
                            @QueryParam("description") String description,
                            @QueryParam("begindate") String begindate,
                            @QueryParam("enddate") String enddate,
                            @QueryParam("priority") String priority,
                            @QueryParam("username") String username,
                            @QueryParam("password") String password) {
        addMeeting(name, description, begindate, enddate, priority);
    }

    @GET
    @Path("/getDescription")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getDescription(@QueryParam("username") String username,
                                 @QueryParam("password") String password,
                                 @QueryParam("id") int id) {

        String res = null;
        if (this.username.equals(username) && this.password.equals(password)) {
            try {
                Meeting result = findMeeting(id);
                if (result != null) {
                    res = result.getDetailedInformation();
                    throw new Exception(res);
                } else
                    res = "[]";
                return res;
            } catch (Exception e) {
                System.err.print(e.getMessage());
            } finally {
                return res;
            }
        } else
            return "[{\"response\":\"false\"}]";
    }

    @PUT
    @Path("/addParticipant/{username}/{password}/{id}/{firstname}/{lastname}/{patronymic}/{post}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED+ ";charset=UTF-8")
    public String addParticipant(@PathParam("username") String username,
                                 @PathParam("password") String password,
                                 @PathParam("id") String id,
                                 @PathParam("firstname") String firstname,
                                 @PathParam("lastname") String lastname,
                                 @PathParam("patronymic") String patronymic,
                                 @PathParam("post") String post
                                 ) {
        if (this.username.equals(username) && this.password.equals(password)) {
            try {
                lastname = URLDecoder.decode(lastname, "UTF-8");
                Participant participant = new Participant();
                participant.setLastName(lastname);
                participant.setFirstName(URLDecoder.decode(firstname, "UTF-8"));
                participant.setPatronymic(URLDecoder.decode(patronymic, "UTF-8"));
                participant.setPost(URLDecoder.decode(post, "UTF-8"));
                Meeting m = findMeeting(Integer.parseInt(id));

                if (m != null) {
                    ArrayList<Participant> participants = m.getParticipants();
                    if (participants == null)
                        participants = new ArrayList<Participant>();
                    participants.add(participant);
                    m.setParticipants(participants);
                    meetings.add(m);
                }
                return "[{\"response\":\"true\"}]";

            } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
                return "[{\"response\":\"false\"}]";
            }
        }
            else
               return "[{\"response\":\"false\"}]";

    }


        // @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")

        @DELETE
        @Path("/deleteMeeting")
        @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
        public String deleteMeeting () {
            int id = Integer.parseInt((request.getHeader(APP_ID)));
            String result = "[{\"response\":\"false\"}]";
            try {
                if (this.username.equals(username) && this.password.equals(password)) {
                    Meeting m = findMeeting(id);
                    //Meeting m = findMeeting(name, begindate, enddate);
                    if (m != null) {
                        meetings.remove(m);
                        result = "[{\"response\":\"true\"}]";
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                return result;
            }
        }


    private Meeting findMeeting(String name, String begindate, String enddate) {
        Meeting meeting = null;
        for (Meeting m : meetings) {
            if (m.getName().equals(name) && m.getBeginData().equals(begindate) && m.getEndData().equals(enddate)) {
                meeting = m;
            }
        }
        return meeting;
    }

    private Meeting findMeeting(int id) {
        Meeting meeting = null;
        for (Meeting m : meetings) {
            if (m.getId() == id) {
                meeting = m;
            }
        }
        return meeting;
    }

    private ArrayList<Meeting> findMeetingsForDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = format.format(date);
        ArrayList<Meeting> currentMeetings = new ArrayList<Meeting>();
        for (Meeting m : meetings) {
            if (m.getBeginData().equals(currentDate)) {
                currentMeetings.add(m);
            }
        }
        return currentMeetings;
    }

    @POST
    @Path("/getMeetOnDes")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String findMeetingOnDescription(String description) {
        Meeting meeting = null;
        description = description.substring(description.indexOf("=") + 1);
        description = description.substring(0, description.length() - 2);
        for (Meeting m : meetings) {
            if (m.getDescription().equals(description))
                meeting = m;
        }
        if (meeting != null)
            return meeting.toString();
        else
            return null;
    }


    private void addMeeting(String name,
                            String description,
                            String begindate,
                            String enddate,
                            String priority) {
        Meeting meeting = null;
        try {
            //String decodedValue1 = URLDecoder.decode(data, "UTF-8");
            //String[] splitStr = decodedValue1.split("[=&]");
            meeting = new Meeting();
            meeting.setName(URLDecoder.decode(name, "UTF-8"));
            meeting.setDescription(URLDecoder.decode(description, "UTF-8"));
            meeting.setBeginData(URLDecoder.decode(begindate, "UTF-8"));
            meeting.setEndData(URLDecoder.decode(enddate, "UTF-8"));
            String prior = URLDecoder.decode(priority, "UTF-8");
            if (prior.endsWith("\r\n"))
                prior = prior.substring(0, prior.length() - 2);
            Meeting.Priority p = meeting.getPriority();
            switch (prior) {
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
