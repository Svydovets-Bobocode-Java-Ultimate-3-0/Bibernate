package org.svydovets;

import org.svydovets.entity.User;
import org.svydovets.session.Session;
import org.svydovets.session.SessionFactory;

public class DemoApp {
    public static void main(String[] args) {
        SessionFactory sessionFactory = new SessionFactory();

        Session session = sessionFactory.createSession();

        User user = session.findById(User.class, 1);

        System.out.println(user);

        session.close();
    }
}