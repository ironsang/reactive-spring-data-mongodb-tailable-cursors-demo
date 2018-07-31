package be.reactiveprogramming.demo.reactivespringdatatailablecursors;

import be.reactiveprogramming.demo.reactivespringdatatailablecursors.data.ConcertTicket;
import be.reactiveprogramming.demo.reactivespringdatatailablecursors.repository.ConcertTicketRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class ReactiveSpringDataStartApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext application = SpringApplication.run(ReactiveSpringDataStartApplication.class, args);

        final MongoOperations mongoOperations = application.getBean(MongoOperations.class);
        final ConcertTicketRepository concertTicketRepository = application.getBean(ConcertTicketRepository.class);

        // Capped collections need to be created manually
        if(mongoOperations.collectionExists("concertTicket")) {
           mongoOperations.dropCollection("concertTicket");
        }

        mongoOperations.createCollection("concertTicket", CollectionOptions.empty().capped().size(9999999L).maxDocuments(100L));

        final Mono<ConcertTicket> saveTicketOne = concertTicketRepository.save(new ConcertTicket("ABBA", "Jerry"));
        final Mono<ConcertTicket> saveTicketTwo = concertTicketRepository.save(new ConcertTicket("Hawkwind", "Kris"));

        saveTicketOne.subscribe();

        Thread.sleep(200);

        concertTicketRepository.findWithTailableCursorBy()
            .flatMap(ticket -> printTicketInformation(ticket))
            .subscribe();

        System.out.println("Let's wait a bit before saving the second ticket, the tailable cursor stays open for new events");
        Thread.sleep(2000);

        saveTicketTwo.subscribe();

        System.out.println("Will wait for the information to be printed from the database");
        Thread.sleep(1000);
    }

    private static Mono<ConcertTicket> printTicketInformation(ConcertTicket concertTicket) {
        System.out.println(String.format("Ticket Artist: %s Buyer: %s", concertTicket.getArtist(), concertTicket.getBuyer()));
        return Mono.just(concertTicket);
    }
}
