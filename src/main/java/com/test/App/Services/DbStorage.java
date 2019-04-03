package com.test.App.Services;

import com.test.App.Entities.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.concurrent.*;

@Service
public class DbStorage
{
    static Logger LOG = LoggerFactory.getLogger(DbStorage.class);
    private static final int DB_RECONNECT_INTERVAL = 5000; //5 seconds

    private EntityManagerFactory emFactory;
    private ScheduledExecutorService scheduledExecutorService;
    private Queue<Timestamp> queue = new ConcurrentLinkedQueue<>();
    private Comparator<Timestamp> comparator = (Timestamp t1, Timestamp t2) -> {
        return t1.getTimestamp().compareTo(t2.getTimestamp());
    };
    private Date waitTill;


    @Autowired
    public DbStorage(EntityManagerFactory entityManagerFactory)
    {
        this.emFactory = entityManagerFactory;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> backgroundSync(), 0, 1,TimeUnit.SECONDS);
    }

    public void saveAsync(Timestamp timestamp)
    {
        queue.add(timestamp);
    }

    public List<Timestamp> fetchAll()
    {
        EntityManager em = emFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        List<Timestamp> result = em.createQuery("SELECT t FROM com.test.App.Entities.Timestamp t", Timestamp.class).getResultList();
        transaction.commit();

        return  result;
    }

    private void backgroundSync()
    {
        if(queue.isEmpty())
            return;

        Date currentTime = new Date();

        if(waitTill != null && currentTime.compareTo(waitTill) < 0)
        {
            LOG.info("too early to retry");
            return;
        }

        TreeSet<Timestamp> sortedSet = new TreeSet<>(comparator);

        try {
            LOG.info("starting background sync to db");

            EntityManager em = emFactory.createEntityManager();
            EntityTransaction transaction = em.getTransaction();

            transaction.begin();

            while (!queue.isEmpty())
                sortedSet.add(queue.poll());

            /*
             * Здесь если данных потенциально слишком много то нужно будет разбить еще на батчи
             */
            for (Timestamp timestamp : sortedSet)
                em.persist(timestamp);

            em.flush();

            transaction.commit();
            em.close();
        }
        catch (Exception e)
        {
            /*
             * Здесь можно добавить проверку на конкретные виды ошибок, которые мы хотим обрабатывать.
             * Для простоты пока любой эксепшн ловим
              */
            LOG.info("background sync error, will try later");
            queue.addAll(sortedSet);
            waitTill = new Date( (new Date()).getTime() + DB_RECONNECT_INTERVAL);
            return;
        }

        LOG.info("background sync successfully complete for " + sortedSet.size() + " items");
    }
}
