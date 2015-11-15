package com.example.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Дамир on 11.11.2015.
 */
import javax.ws.rs.*;
import javax.ejb.Stateless;


@Stateless
@Path("/")
public class MainPageSvc {

    private String username = "user";
    private String password = "password";

    @GET
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    public String print() {
        String data = "<html><head>\n" +
                " <meta http-equiv=\"CONTENT-TYPE\" content=\"text/html; charset=UTF-8\"/>\n" +
                " <title>MEETING APP</title>\n" +
                "</head>\n" + "<body><form action=\"meeting/setMeeting\" method=\"GET\"><h1>Создайте встречу</h1>\n" +
                "  <table>\n" +
                "    <tbody>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Название</td>\n" +
                "      <td><input type=\"text\" id=\"name\" required placeholder=\"Введите название\" name=\"name\" size=\"80\"/></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Описание</td>\n" +
                "      <td><input type=\"text\" id=\"description\" required placeholder=\"Введите Описание\" name=\"description\"size=\"80\"/></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Дата Начала</td>\n" +
                "      <td><input type=\"text\" id=\"begindate\" required placeholder=\"Введите Дату (ГГГГ-ММ-ДД)\" pattern=\"^(19|20)\\d\\d-((0((1-(0[1-9]|[12][0-9]|3[01])$)|(2-(0[1-9]|1[0-9]|2[0-8])$)|([3-9]-(0[1-9]|[12][0-9]|3[01])$)))|(1[012]-(0[1-9]|[12][0-9]|3[01])$))$\" name=\"begindate\" size=\"80\"/></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Дата Конца</td>\n" +
                "      <td><input type=\"text\" id=\"enddate\" required placeholder=\"Введите Дату (ГГГГ-ММ-ДД)\" pattern=\"^(19|20)\\d\\d-((0((1-(0[1-9]|[12][0-9]|3[01])$)|(2-(0[1-9]|1[0-9]|2[0-8])$)|([3-9]-(0[1-9]|[12][0-9]|3[01])$)))|(1[012]-(0[1-9]|[12][0-9]|3[01])$))$\" name=\"enddate\" size=\"80\"/></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Приоритет</td>\n" +
                "      <td><input type=\"text\" id=\"priority\" required placeholder=\"Введите Приоритет (Срочная, Плановая, По возможности)\" pattern=\"^(Плановая|Срочная|По возможности)$\" name=\"priority\" size=\"80\"/></td>\n" +
                "    </tr>\n" +
                "    </tbody>\n" +
                "  </table>\n" +
                "<input class=\"button\" type=\"reset\" value=\"Очистить\" name=\"clear\"/>\n" +
                "&nbsp;&nbsp;\n" +
                "<input class=\"button\" type=\"submit\" value=\"Сохранить\" name=\"submit\"/>\n" +
                "&nbsp;&nbsp;</form></body></html>";

        return data;
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMessageQueryParam(@QueryParam("userName") String username,
                                        @QueryParam("password") String password) {
        if(this.username.equals(username)&& this.password.equals(password))
            return "{\"response\":\"true\"}";
        else
            return "{\"response\":\"false\"}";
    }


}
