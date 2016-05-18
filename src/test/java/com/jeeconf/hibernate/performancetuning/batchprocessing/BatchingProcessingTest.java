package com.jeeconf.hibernate.performancetuning.batchprocessing;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.jeeconf.hibernate.performancetuning.BaseTest;
import com.jeeconf.hibernate.performancetuning.batchprocessing.entity.Account;
import com.jeeconf.hibernate.performancetuning.batchprocessing.entity.Client;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.junit.Test;
import org.springframework.test.annotation.Commit;

import java.util.List;

/**
 * Created by Igor Dmitriev / Mikalai Alimenkou on 4/30/16
 */
@DatabaseSetup("/batchprocessing.xml")
public class BatchingProcessingTest extends BaseTest {

    @Commit
    @Test
    public void batchInsert() {
        for (int i = 0; i < 20; i++) {
            Client client = new Client();
            client.setName("Robot# " + i);

            Account account = new Account();
            client.getAccounts().add(account);
            account.setClient(client);

            em.persist(client);
            em.persist(account);

            if (i % 10 == 0) { // the same as JDBC batch size
                em.flush();
                em.clear();
            }
        }
    }

    @Commit
    @Test
    public void batchUpdate() {
        Query query = getSession().createQuery("select c from " +
                "com.jeeconf.hibernate.performancetuning.batchprocessing.entity.Client c");
        ScrollableResults scroll = query.setFetchSize(50)
                .setCacheMode(CacheMode.IGNORE)
                .scroll(ScrollMode.FORWARD_ONLY);
        int count = 0;
        while (scroll.next()) {
            Client client = (Client) scroll.get(0);
            client.setName("NEW NAME");

            if (++count % 10 == 0) { // the same as JDBC batch size
                em.flush();
                em.clear();
            }
        }
    }

    @Commit
    @Test
    public void batchCascadeDelete() {
        List<Client> clients = em.createQuery("select c from " +
                "com.jeeconf.hibernate.performancetuning.batchprocessing.entity.Client c", Client.class)
                .getResultList();
        for (Client client : clients) {
            em.remove(client);
        }
    }
}