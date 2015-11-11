package com.example.participant;

/**
 * Created by Дамир on 09.11.2015.
 */
public class Participant {
    private String lastName;
    private String firstName;
    private String patronymic;
    private String post;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    @Override
    public String toString() {
        return "{" +
                "\"lastName\":" +"\""+ lastName + "\""+
                ", \"firstName\":" +"\""+ firstName + "\""+
                ", \"patronymic\":" +"\""+ patronymic + "\""+
                ", \"post\":" + "\""+post + "\""+
                '}';
    }
}
