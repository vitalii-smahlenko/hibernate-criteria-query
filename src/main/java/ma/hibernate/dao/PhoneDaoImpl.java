package ma.hibernate.dao;

import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't create phone " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return phone;
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = criteriaBuilder.createQuery(Phone.class);
            Root<Phone> phoneRoot = query.from(Phone.class);
            CriteriaBuilder.In<String> paramsPredicate = null;
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                paramsPredicate = criteriaBuilder.in(phoneRoot.get(entry.getKey()));
                for (String model : entry.getValue()) {
                    paramsPredicate.value(model);
                }
                criteriaBuilder.and(paramsPredicate);
            }
            query.where(criteriaBuilder.and(paramsPredicate));
            return session.createQuery(query).getResultList();
        } catch (RuntimeException e) {
            throw new RuntimeException("Can't find phones with params "
                    + showInfoAboutParams(params), e);
        }
    }

    private String showInfoAboutParams(Map<String, String[]> params) {
        StringBuilder paramsToString = new StringBuilder();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            paramsToString.append(entry.getKey());
            paramsToString.append(" ");
            paramsToString.append(String.join(" ", entry.getValue()));
            paramsToString.append("; ");
        }
        return paramsToString.toString();
    }
}
