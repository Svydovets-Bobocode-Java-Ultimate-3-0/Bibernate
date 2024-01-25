package demo;

import demo.entity.User;
import org.svydovets.session.Session;
import org.svydovets.session.SessionFactory;

public class DemoApp {
    public static void main(String[] args) {
        SessionFactory sessionFactory = new SessionFactory();

        Session session = sessionFactory.createSession();

        User user = session.findById(User.class, 2);

        System.out.println(user);

        session.close();
    }
}